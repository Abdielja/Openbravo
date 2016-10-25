/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.JSONRowConverter;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.JsonUtils;

public class Context extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(Context.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    OBContext.setAdminMode(false);
    try {
      JSONObject result = new JSONObject();
      if (OBMOBCUtils.isAuthenticated(this, request, response)) {
        final JSONRowConverter converter = new JSONRowConverter(DataResolvingMode.FULL);
        JSONArray data = new JSONArray();

        String hqlUser = "select u as user, img.bindaryData as img, r as role, org as organization, cli as client "
            + "from ADUser u left outer join u.image img left outer join u.businessPartner bp, ADRole r, Organization org, ADClient cli "
            + "where u.id = '"
            + OBContext.getOBContext().getUser().getId()
            + "' and u.active = true "
            + " and r.id = '"
            + OBContext.getOBContext().getRole().getId()
            + "' and org.id ='"
            + OBContext.getOBContext().getCurrentOrganization().getId()
            + "' and cli.id = '"
            + OBContext.getOBContext().getCurrentClient().getId() + "'";

        Query qryUser = OBDal.getInstance().getSession().createQuery(hqlUser);

        for (Object qryUserObject : qryUser.list()) {
          final Object[] qryUserObjectItem = (Object[]) qryUserObject;

          JSONObject item = new JSONObject();
          item.put("user", converter.convert(qryUserObjectItem[0]));
          item.put("img", converter.convert(qryUserObjectItem[1]));
          item.put("role", converter.convert(qryUserObjectItem[2]));
          item.put("organization", converter.convert(qryUserObjectItem[3]));
          item.put("client", converter.convert(qryUserObjectItem[4]));
          data.put(item);
        }
        result.put("data", data);

      } else {
        result.put("exception", "No context loaded");
      }
      writeResult(response, result.toString());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } finally {
      OBContext.restorePreviousMode();
      OBContext.setOBContext((OBContext) null);
    }
  }

  // @Override
  // protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
  // return Arrays
  // .asList(new String[] {
  // "select u as user, img.bindaryData as img, r as role, org as organization, cli as client "
  // + "from ADUser u left outer join u.image img, ADRole r, Organization org, ADClient cli "
  // +
  // "where u.id = $userId and u.$readableSimpleCriteria and u.$activeCriteria and r.id = $roleId and r.$readableSimpleCriteria and r.$activeCriteria and org.id ='"
  // + OBContext.getOBContext().getCurrentOrganization().getId()
  // + "' and cli.id = '"
  // + OBContext.getOBContext().getCurrentClient().getId() + "'" });
  // }
}
