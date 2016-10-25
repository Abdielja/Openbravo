/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.servercontroller.MobileServerController;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;
import org.openbravo.service.json.JsonToDataConverter;

public abstract class ProcessHQLQuery extends SecuredJSONProcess {

  protected abstract List<String> getQuery(JSONObject jsonsent) throws JSONException;

  private final static Logger log = Logger.getLogger(ProcessHQLQuery.class);

  protected boolean isAdminMode() {
    return false;
  }

  protected StrategyQuery getStrategyQuery() {
    return ProcessHQLQuery.StrategyQueryScroll;
  }

  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    return null;
  }

  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    return null;
  }

  @Inject
  @Any
  private Instance<HQLCriteriaProcess> hqlCriterias;

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {

    // don't do any actions when transitioning
    if (MobileServerUtils.isMobileServerControllerEnabled()) {
      // if the request has server status info then make use of it to update the
      // server status
      MobileServerUtils.updateServerStatusFromJSON(jsonsent);

      if (MobileServerController.getInstance().serverHasTransitioningStatus()) {
        MobileServerController.getInstance().writeServerStatusJSON(w);
        return;
      }
    }

    Query rememberQueryForErrorLog = null;
    try {
      boolean streamOpened = false;
      if (isAdminMode()) {
        OBContext.setAdminMode(false);
      }

      Long lastUpdated = null;
      try {
        lastUpdated = (jsonsent.has("lastUpdated") && !jsonsent.isNull("lastUpdated")
            && !jsonsent.get("lastUpdated").equals("null") && !jsonsent.get("lastUpdated").equals(
            "undefined")) ? jsonsent.getLong("lastUpdated") : null;
      } catch (Exception e) {
        lastUpdated = null;
        log.warn("lastUpdated param is not a valid timestamp. Full refresh will be executed this time: "
            + e.getMessage());
      }

      String dateFormat = jsonsent.has("_dateFormat") ? jsonsent.getString("_dateFormat") : null;

      JSONArray remoteFilters = jsonsent.has("remoteFilters") && !jsonsent.isNull("remoteFilters") ? jsonsent
          .getJSONArray("remoteFilters") : null;

      int totalRows = 0;
      int queryRows = 0;
      int limit = -1;
      int offset = -1;
      if (jsonsent.has("_limit") && !jsonsent.isNull("_limit")) {
        limit = jsonsent.getInt("_limit");
      }
      if (jsonsent.has("_offset") && !jsonsent.isNull("_offset")) {
        offset = jsonsent.getInt("_offset");
      }
      boolean firstQuery = true;
      Map<String, Object> parameterValues = this.getParameterValues(jsonsent);
      List<String> queries = getQuery(jsonsent);
      List<HQLPropertyList> properties = getHqlProperties(jsonsent);

      for (int i = 0; i < queries.size(); i++) {
        if (limit == 0) {
          break;
        }
        String hqlQuery = queries.get(i);
        HQLPropertyList hqlProperty;
        if (properties != null) {
          hqlProperty = properties.get(i);
        } else {
          hqlProperty = null;
        }
        rememberQueryForErrorLog = null;

        SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlQuery, OBContext.getOBContext()
            .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
            lastUpdated != null ? new Date(lastUpdated) : null, remoteFilters, hqlProperty);

        if (hqlCriterias != null) {
          querybuilder.setHQLCriteria(hqlCriterias);
        }

        final Session session = OBDal.getInstance().getSession();
        final Query query = session.createQuery(querybuilder.getHQLQuery());
        if (parameterValues != null) {
          for (String paramName : parameterValues.keySet()) {
            query.setParameter(paramName, parameterValues.get(paramName).toString());
          }
        }
        rememberQueryForErrorLog = query;

        if (limit > -1) {
          query.setMaxResults(limit);
        }

        if (offset > -1) {
          query.setFirstResult(offset);
        }

        List<String> queryParams = new ArrayList<String>(Arrays.asList(query.getNamedParameters()));

        if (jsonsent.has("parameters")) {
          JSONObject jsonparams = jsonsent.getJSONObject("parameters");
          Iterator<?> it = jsonparams.keys();
          while (it.hasNext()) {
            String key = (String) it.next();
            if (!queryParams.contains(key)) {
              continue;
            }
            queryParams.remove(key);

            Object value = jsonparams.get(key);
            if (value instanceof JSONObject) {
              JSONObject jsonvalue = (JSONObject) value;
              query.setParameter(
                  key,
                  JsonToDataConverter.convertJsonToPropertyValue(
                      PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
            } else {
              query.setParameter(key, JsonToDataConverter.convertJsonToPropertyValue(
                  PropertyByType.infer(value), value));
            }
          }
        }
        querybuilder.fillQueryWithParameters(query);

        // XXX: for standard params (client, org, pos), no need to add as extra
        if (!queryParams.isEmpty()) {
          for (String param : queryParams) {
            if (jsonsent.has(param)) {
              Object value = jsonsent.get(param);
              if (value instanceof JSONObject) {
                JSONObject jsonvalue = (JSONObject) value;
                query.setParameter(
                    param,
                    JsonToDataConverter.convertJsonToPropertyValue(
                        PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
              } else {
                query.setParameter(param, JsonToDataConverter.convertJsonToPropertyValue(
                    PropertyByType.infer(value), value));
              }
            }
          }
        }

        if (!streamOpened) {
          JSONRowConverter.startResponse(w);
          streamOpened = true;
        }
        if (dateFormat == null) {
          queryRows = getStrategyQuery().buildResponse(w, query, firstQuery);
        } else {
          queryRows = getStrategyQuery().buildResponse(w, query, firstQuery, dateFormat);
        }
        totalRows += queryRows;

        if (totalRows > 0) {
          firstQuery = false;
        }
        if (queryRows < limit) {
          offset = 0;
        }
        if (limit > -1) {
          limit = limit - totalRows;
        }
      }
      if (!streamOpened) {
        JSONRowConverter.startResponse(w);
        streamOpened = true;
        totalRows = 0;
      }
      JSONRowConverter.endResponse(w, totalRows);

    } catch (Exception e) {
      log.error(this.getClass().getName() + ": Error when generating query: "
          + rememberQueryForErrorLog + " (" + e.getMessage() + ")", e);
      JSONRowConverter.addJSONExceptionFields(w, e);
    } finally {
      if (isAdminMode()) {
        OBContext.restorePreviousMode();
      }
    }
  }

  public interface StrategyQuery {
    public int buildResponse(Writer w, Query query, boolean firstQuery) throws JSONException,
        IOException;

    public int buildResponse(Writer w, Query query, boolean firstQuery, String dateFormat)
        throws JSONException, IOException;
  }

  public final static StrategyQuery StrategyQueryScroll = new StrategyQuery() {
    public int buildResponse(Writer w, Query query, boolean firstQuery, String dateFormat)
        throws JSONException, IOException {
      ScrollableResults listdata = query.scroll(ScrollMode.FORWARD_ONLY);
      String[] aliases = query.getReturnAliases();
      return JSONRowConverter.buildResponse(w, Scroll.create(listdata), aliases, firstQuery,
          dateFormat);
    }

    public int buildResponse(Writer w, Query query, boolean firstQuery) throws JSONException,
        IOException {
      return buildResponse(w, query, firstQuery, null);
    }
  };

  public final static StrategyQuery StrategyQueryList = new StrategyQuery() {
    public int buildResponse(Writer w, Query query, boolean firstQuery, String dateFormat)
        throws JSONException, IOException {
      return buildResponse(w, query, firstQuery);
    }

    public int buildResponse(Writer w, Query query, boolean firstQuery) throws JSONException,
        IOException {
      List<?> listdata = query.list();
      String[] aliases = query.getReturnAliases();
      return JSONRowConverter.buildResponse(w, listdata, aliases, firstQuery);
    }
  };
}
