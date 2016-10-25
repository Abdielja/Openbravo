/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Instance;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.mobile.core.model.HQLPropertyList;

/**
 * An HQL Query builder.
 * 
 * @author adrianromero
 */

public class SimpleQueryBuilder {

  private String hql;
  private String client;
  private String org;
  private Date lastUpdated;
  private JSONArray filters;
  private HQLPropertyList properties;
  private Instance<HQLCriteriaProcess> hqlCriteria;

  private Map<String, Object> paramValues;
  private final Logger log4j = Logger.getLogger(SimpleQueryBuilder.class);

  private int parameterCounter = 0;

  public enum Operator {
    equals, //
    notEquals, //
    greaterThan, //
    lessThan, //
    startsWith, //
    contains;
  }

  public SimpleQueryBuilder(String hql, String client, String org, Date lastUpdated) {
    this.hql = hql;
    this.client = client;
    this.org = org;
    this.lastUpdated = lastUpdated;
    // :orgCriteria
    // :clientCriteria
    // :activeCriteria
  }

  public SimpleQueryBuilder(String hql, String client, String org, Date lastUpdated,
      JSONArray filters, HQLPropertyList properties) {
    this.hql = hql;
    this.client = client;
    this.org = org;
    this.lastUpdated = lastUpdated;
    this.filters = filters;
    this.properties = properties;
    // :orgCriteria
    // :clientCriteria
    // :activeCriteria
  }

  private static String getClientFilter(Collection<String> clients) {

    StringBuilder clientfilter = new StringBuilder();

    if (clients.size() == 0) {
      clientfilter.append(" (1=1) ");
    } else {
      clientfilter.append(" ($$$$client.id in (");
      boolean comma = false;
      for (String s : clients) {
        if (comma) {
          clientfilter.append(", ");
        } else {
          comma = true;
        }
        clientfilter.append("'");
        clientfilter.append(s);
        clientfilter.append("'");
      }
      clientfilter.append(")) ");
    }

    return clientfilter.toString();
  }

  private static String getOrgFilter(Collection<String> orgs) {

    StringBuilder orgfilter = new StringBuilder();
    if (orgs.isEmpty()) {
      orgfilter.append(" (1=1) ");
    } else {
      orgfilter.append(" ($$$$organization.id in (");
      boolean comma = false;
      for (String s : orgs) {
        if (comma) {
          orgfilter.append(", ");
        } else {
          comma = true;
        }
        orgfilter.append("'");
        orgfilter.append(s);
        orgfilter.append("'");
      }
      orgfilter.append(")) ");
    }
    return orgfilter.toString();
  }

  private static class NaturalOrganizationCriteria implements PartBuilder {

    private String client;
    private String org;

    public NaturalOrganizationCriteria(String client, String org) {
      this.client = client;
      this.org = org;
    }

    public String getPart() {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client);
      return getOrgFilter(osp.getNaturalTree(org));
    }
  }

  private static class ChildOrganizationCriteria implements PartBuilder {

    private String client;
    private String org;

    public ChildOrganizationCriteria(String client, String org) {
      this.client = client;
      this.org = org;
    }

    public String getPart() {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client);
      return getOrgFilter(osp.getChildTree(org, true));
    }
  }

  private static class ParentOrganizationCriteria implements PartBuilder {

    private String client;
    private String org;

    public ParentOrganizationCriteria(String client, String org) {
      this.client = client;
      this.org = org;
    }

    public String getPart() {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client);
      return getOrgFilter(osp.getParentTree(org, true));
    }
  }

  private static class CurrentOrganizationCriteria implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getCurrentOrganization().getId() + "'";
    }
  }

  private static class CurrentClientCriteria implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getCurrentClient().getId() + "'";
    }
  }

  private static class OrganizationCriteria implements PartBuilder {
    public String getPart() {
      return getOrgFilter(Arrays.asList(OBContext.getOBContext().getReadableOrganizations()));
    }
  }

  private static class ClientCriteria implements PartBuilder {
    public String getPart() {
      return getClientFilter(Arrays.asList(OBContext.getOBContext().getReadableClients()));
    }
  }

  private static class ReadableCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();
    private PartBuilder org = new OrganizationCriteria();
    private PartBuilder active = new ActiveCriteria();

    public String getPart() {
      return " (" + client.getPart() + " and " + org.getPart() + " and " + active.getPart() + ") ";
    }
  }

  private static class ReadableClientCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();
    private PartBuilder active = new ActiveCriteria();

    public String getPart() {
      return " (" + client.getPart() + " and " + active.getPart() + ") ";
    }
  }

  private static class ActiveCriteria implements PartBuilder {
    public String getPart() {
      return " ($$$$active = 'Y') ";
    }
  }

  private static class ReadableSimpleCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();
    private PartBuilder org = new OrganizationCriteria();

    public String getPart() {
      return " (" + client.getPart() + " and " + org.getPart() + ") ";
    }
  }

  private static class ReadableSimpleClientCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();

    public String getPart() {
      return " (" + client.getPart() + ") ";
    }
  }

  private static class RoleId implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getRole().getId() + "'";
    }
  }

  private static class UserId implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getUser().getId() + "'";
    }
  }

  private static class LanguageId implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getLanguage().getId() + "'";
    }
  }

  private static class IncrementalUpdateCriteria implements PartBuilder {
    Date lastUpdate;
    private PartBuilder active = new ActiveCriteria();

    public IncrementalUpdateCriteria(Date lastUpdate) {
      this.lastUpdate = lastUpdate;
    }

    public String getPart() {
      final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateFormat.setLenient(true);

      String part = "";
      if (lastUpdate != null) {
        part += " $$$$updated> TO_TIMESTAMP('" + dateFormat.format(lastUpdate)
            + "', 'YYYY-MM-DD HH24:MI:SS') ";
      } else {
        part += active.getPart();
      }
      return "(" + part + ")";
    }

  }

  private abstract class FiltersPartBuilder implements PartBuilder {

    private Map<String, Object> paramValues;
    private final Logger log4j = Logger.getLogger(FiltersPartBuilder.class);

    public FiltersPartBuilder(Map<String, Object> paramValues) {
      this.paramValues = paramValues;
    }

    public void fillParamValues(String operatorName, Object value) {
      if (!operatorName.equals("")) {
        this.paramValues.put(operatorName + parameterCounter, value);
        parameterCounter++;
      }
    }

    @Override
    public String getPart() {
      return null;
    }
  }

  private class FiltersCriteria extends FiltersPartBuilder {

    JSONArray filters;
    private HQLPropertyList properties;
    private int numValue;
    private Map<String, Object> paramValues;
    private final Logger log4j = Logger.getLogger(FiltersCriteria.class);

    public FiltersCriteria(JSONArray filters, HQLPropertyList properties,
        Map<String, Object> paramValues) {
      super(paramValues);
      this.filters = filters;
      this.properties = properties;

    }

    public String getPart() {
      // numValue = 0;
      if (filters == null || properties == null) {
        return "1=1";
      }
      Object value;
      String operatorValueStart = "";
      String operatorValueName = "";
      String operatorValueFinal = "";
      String operatorValue = "";
      String filterQuery = "";
      boolean addAnd = false;
      for (int i = 0; i < filters.length(); i++) {
        operatorValueStart = "";
        operatorValueName = "";
        operatorValueFinal = "";
        try {
          JSONObject objFilter = (JSONObject) filters.get(i);
          JSONArray columns = objFilter.getJSONArray("columns");
          if (objFilter.getString("value").equals("__all__")
              || objFilter.getString("value").equals("")) {
            continue;
          }
          value = objFilter.getString("value");

          if (objFilter.getString("operator").equals(Operator.equals.toString())) {
            if (objFilter.has("isId") && objFilter.getBoolean("isId")) {
              operatorValueStart = "=:";
              operatorValueName = "valueEqId";
            } else if (objFilter.has("boolean") && objFilter.getBoolean("boolean")) {
              operatorValueStart = "=:";
              operatorValueName = "valueEqB";
              value = objFilter.getBoolean("value");
            } else if (objFilter.has("fieldType")
                && objFilter.getString("fieldType").equals("Number")) {
              operatorValueStart = "=:";
              operatorValueName = "valueEqNum";
              value = new BigDecimal(objFilter.getString("value"));
            } else if (objFilter.has("fieldType")
                && objFilter.getString("fieldType").equals("forceString")) {
              operatorValueName = "valueEqFs";
              operatorValueStart = "='" + value + "'";

            } else {
              operatorValueStart = "=upper(:";
              operatorValueName = "valueEqUp";
              operatorValueFinal = ")";
            }
          } else if (objFilter.getString("operator").equals(Operator.notEquals.toString())) {
            operatorValueStart = "<> :";
            operatorValueName = "valueNEq";
            value = new BigDecimal(objFilter.getString("value"));
          } else if (objFilter.getString("operator").equals(Operator.greaterThan.toString())) {
            operatorValueStart = "> :";
            operatorValueName = "valueGt";
            value = new BigDecimal(objFilter.getString("value"));
          } else if (objFilter.getString("operator").equals(Operator.lessThan.toString())) {
            operatorValueStart = "< :";
            operatorValueName = "valueLt";
            value = new BigDecimal(objFilter.getString("value"));
          } else if (objFilter.getString("operator").equals(Operator.startsWith.toString())) {
            operatorValueStart = " like upper(:";
            operatorValueName = "valueSt";
            operatorValueFinal = ")";
          } else if (objFilter.getString("operator").equals(Operator.contains.toString())) {
            operatorValueStart = " like upper(:";
            operatorValueName = "valueCon";
            operatorValueFinal = ")";
          } else if (objFilter.getString("operator").equals("filter")) {
            continue;
          } else if (objFilter.has("isId") && objFilter.getBoolean("isId")) {
            operatorValueStart = "=:";
            operatorValueName = "valueEqId";
          } else {
            operatorValueStart = "=upper(:";
            operatorValueName = "valueEqUp";
            operatorValueFinal = ")";
          }
          if (addAnd) {
            filterQuery += " AND ";
          }

          if (columns.length() != 0) {
            filterQuery += " (";
            for (int j = 0; j < columns.length(); j++) {
              operatorValue = operatorValueStart + operatorValueName + parameterCounter + " "
                  + operatorValueFinal;
              fillParamValues(operatorValueName, value);
              if ((objFilter.has("isId") && objFilter.getBoolean("isId"))
                  || (objFilter.has("boolean") && objFilter.getBoolean("boolean"))
                  || (objFilter.has("fieldType") && objFilter.getString("fieldType").equals(
                      "Number"))) {
                filterQuery += properties.getHqlProperty((String) columns.get(j)) + operatorValue;
              } else if (objFilter.has("fieldType")
                  && objFilter.getString("fieldType").equals("forceString")) {
                filterQuery += properties.getHqlProperty((String) columns.get(j))
                    + operatorValueStart;

              } else {
                filterQuery += "upper(" + properties.getHqlProperty((String) columns.get(j)) + ")"
                    + operatorValue;
              }
              if (j != columns.length() - 1) {
                filterQuery += " OR ";
              }

            }

            filterQuery += ") ";
          } else {
            operatorValue = operatorValueStart + operatorValueName + parameterCounter + " "
                + operatorValueFinal;
            filterQuery += operatorValue;
            fillParamValues(operatorValueName, value);
          }
          addAnd = true;
        } catch (JSONException ignored) {
        }

      }
      if (filterQuery != "") {
        filterQuery = " (" + filterQuery + ") ";
      } else {
        filterQuery = "1=1";
      }
      return filterQuery;
    }

  }

  private class HQLCriteria extends FiltersPartBuilder {
    JSONArray filters;
    private Instance<HQLCriteriaProcess> hqlCriteria;
    private HQLPropertyList properties;
    private final Logger log4j = Logger.getLogger(HQLCriteria.class);

    public HQLCriteria(JSONArray filters, Instance<HQLCriteriaProcess> hqlCriteria,
        Map<String, Object> paramValues) {
      super(paramValues);
      this.filters = filters;
      this.hqlCriteria = hqlCriteria;
    }

    public String getPart() {

      if (filters == null) {
        return "1=1";
      }

      String filterQuery = "";
      boolean addAnd = false;
      String filter = "";
      for (int i = 0; i < filters.length(); i++) {
        try {
          JSONObject objFilter = (JSONObject) filters.get(i);
          if (objFilter.getString("operator").equals("filter")) {
            if (hqlCriteria != null) {
              HQLCriteriaProcess criteria = hqlCriteria.select(
                  new ComponentProvider.Selector(objFilter.getString("value"))).get();
              String hqlFilter = criteria.getHQLFilter(objFilter.has("params") ? objFilter
                  .getString("params") : null);
              String fieldtype = (objFilter.has("fieldType") ? objFilter.getString("fieldType")
                  : null);
              if (addAnd) {
                if (objFilter.has("filter") && filter.equals(objFilter.getString("filter"))) {
                  filterQuery += " " + criteria.getOperatorForMultipleFilters() + " ( "
                      + replaceParams(hqlFilter, objFilter.getString("params"), fieldtype) + " ) ";
                  filterQuery += " ) ";

                } else {
                  filterQuery += " ) AND ( "
                      + replaceParams(hqlFilter, objFilter.getString("params"), fieldtype);

                }

              } else {
                filterQuery += replaceParams(hqlFilter, objFilter.getString("params"), fieldtype);
              }
              addAnd = true;

              if (objFilter.has("filter")) {
                filter = objFilter.getString("filter");
              }

            }
          }

        } catch (JSONException ignored) {
        }
      }

      if (filterQuery != "") {
        filterQuery = " (" + filterQuery + ") ";
      } else {
        filterQuery = "1=1";
      }
      return filterQuery;
    }

    public String replaceParams(String hqlFilter, String params, String fieldtype) {
      JSONArray paramsArray;
      String newHqlFilter = hqlFilter;
      try {
        paramsArray = new JSONArray(params);
        String operatorValueName = null;
        if (paramsArray.length() > 0) {
          for (int i = 0; i < paramsArray.length(); i++) {

            if (paramsArray.get(i) instanceof Number) {
              if (fieldtype != null && fieldtype.equals("BigDecimal")) {
                operatorValueName = "valueEqBD";
              } else if (fieldtype != null && fieldtype.equals("Long")) {
                operatorValueName = "valueEqLong";
              } else {
                operatorValueName = "valueEqNum";
              }
            } else if (paramsArray.get(i) instanceof Boolean) {
              operatorValueName = "valueEqB";
            } else if (paramsArray.get(i) instanceof JSONArray) {
              operatorValueName = "valueEqArray";
            } else {
              operatorValueName = "valueEqSt";
            }
            newHqlFilter = newHqlFilter.replace("'$" + (i + 1) + "'", "$" + (i + 1));
            newHqlFilter = newHqlFilter.replace("$" + (i + 1), ":" + operatorValueName
                + parameterCounter + " ");
            fillParamValues(operatorValueName, paramsArray.get(i));

          }
        }
      } catch (JSONException ignored) {
      }
      return newHqlFilter;
    }
  }

  public String getHQLQuery() {

    String newhql = hql;

    // These two filters: $filtersCriteria and $hqlCriteria must be done at the very beginning
    // because can include other tags like $naturalOrgCriteria, ...

    this.paramValues = new HashMap<String, Object>();
    newhql = replaceAll(newhql, "$filtersCriteria", new FiltersCriteria(filters, properties,
        this.paramValues));
    newhql = replaceAll(newhql, "$hqlCriteria", new HQLCriteria(filters, hqlCriteria,
        this.paramValues));

    newhql = replaceAll(newhql, "$clientCriteria", new ClientCriteria());

    newhql = replaceAll(newhql, "$orgCriteria", new OrganizationCriteria());
    newhql = replaceAll(newhql, "$clientId", new CurrentClientCriteria());
    newhql = replaceAll(newhql, "$orgId", new CurrentOrganizationCriteria());
    newhql = replaceAll(newhql, "$naturalOrgCriteria", new NaturalOrganizationCriteria(client, org));
    newhql = replaceAll(newhql, "$parentOrgCriteria", new ParentOrganizationCriteria(client, org));
    newhql = replaceAll(newhql, "$childOrgCriteria", new ChildOrganizationCriteria(client, org));
    newhql = replaceAll(newhql, "$activeCriteria", new ActiveCriteria());
    newhql = replaceAll(newhql, "$incrementalUpdateCriteria", new IncrementalUpdateCriteria(
        lastUpdated));

    newhql = replaceAll(newhql, "$readableCriteria", new ReadableCriteria());
    newhql = replaceAll(newhql, "$readableSimpleCriteria", new ReadableSimpleCriteria());
    newhql = replaceAll(newhql, "$readableClientCriteria", new ReadableClientCriteria());
    newhql = replaceAll(newhql, "$readableSimpleClientCriteria", new ReadableSimpleClientCriteria());

    newhql = replaceAll(newhql, "$roleId", new RoleId());
    newhql = replaceAll(newhql, "$userId", new UserId());
    newhql = replaceAll(newhql, "$languageId", new LanguageId());

    return newhql;
  }

  private String replaceAll(String s, String search, PartBuilder part) {
    String news = s;
    int i = news.indexOf(search);
    if (i >= 0) {
      String replacement = part.getPart();
      while (i >= 0) {
        int alias = findalias(news, i);
        if (alias >= 0) {
          news = news.substring(0, alias)
              + replacement.replaceAll("\\$\\$\\$\\$", news.substring(alias, i))
              + news.substring(i + search.length());
        } else {
          news = news.substring(0, i) + replacement.replaceAll("\\$\\$\\$\\$", "")
              + news.substring(i + search.length());
        }

        i = news.indexOf(search);
      }
    }
    return news;
  }

  private int findalias(String sentence, int position) {

    int i = position - 1;
    int s = 0;

    while (i > 0) {
      char c = sentence.charAt(i);
      if (s == 0) {
        if (c == '.') {
          s = 1;
        } else {
          return -1;
        }
      } else if (s == 1) {
        if (Character.isLetterOrDigit(c)) {
          s = 2;
        } else {
          return -1;
        }
      } else if (s == 2) {
        if (!Character.isLetterOrDigit(c) && c != '.' && c != '_') {
          if (Character.isWhitespace(c) || c == ')' || c == '(') {
            return i + 1;
          } else {
            return -1;
          }
        }
      }
      i--;
    }
    return -1;
  }

  public void setHQLCriteria(Instance<HQLCriteriaProcess> hqlCriterias) {
    this.hqlCriteria = hqlCriterias;
  }

  public void fillQueryWithParameters(Query query) {
    Entry<String, Object> value = null;
    try {
      if (paramValues != null) {
        Iterator<Entry<String, Object>> iter = paramValues.entrySet().iterator();
        while (iter.hasNext()) {
          value = iter.next();
          if (!query.toString().contains((value.getKey() + " "))) {
            continue;
          }
          if (value.getKey().startsWith("valueSt")) {
            query.setParameter(value.getKey(), value.getValue().toString() + "%");
          } else if (value.getKey().startsWith("valueCon")) {
            query.setParameter(value.getKey(), "%" + value.getValue().toString() + "%");
          } else if (value.getKey().startsWith("valueEqFs")) {
            query.setParameter(value.getKey(), "'" + value.getValue().toString() + "'");
          } else if (value.getValue() instanceof BigDecimal) {
            query.setParameter(value.getKey(), value.getValue());
          } else if (value.getKey().startsWith("valueEqLong")) {
            query.setParameter(value.getKey(), new Long(value.getValue().toString()));
          } else if (value.getKey().startsWith("valueEqBD")) {
            query.setParameter(value.getKey(), new BigDecimal(value.getValue().toString()));
          } else if (value.getKey().startsWith("valueEqNum")) {
            query.setParameter(value.getKey(), new BigDecimal(value.getValue().toString()));
          } else if (value.getValue() instanceof Boolean) {
            query.setParameter(value.getKey(), Boolean.parseBoolean(value.getValue().toString()));
          } else if (value.getValue() instanceof JSONArray) {
            JSONArray a = (JSONArray) value.getValue();
            List<String> parameter = new ArrayList<String>();
            for (int i = 0; i < a.length(); i++) {
              parameter.add(a.get(i).toString());
            }
            query.setParameterList(value.getKey(), parameter);
          } else {
            query.setParameter(value.getKey(), value.getValue().toString());
          }
        }
      }
    } catch (Exception e) {
      throw new OBException("Error when converting value " + value.getValue(), e);
    }
  }
}
