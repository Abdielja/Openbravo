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
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.mobile.core.MobileCoreConstants;
import org.openbravo.mobile.core.MobileDefaultsHandler;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.mobile.core.MobileServerService;
import org.openbravo.mobile.core.MobileServiceDefinition;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.service.json.JsonUtils;

/**
 * Provides basic infrastructure to be used in login.
 * 
 * @author asier
 * 
 */
public class MobileCoreLoginUtilsServlet extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(MobileCoreLoginUtilsServlet.class);

  private static final long serialVersionUID = 1L;

  @Inject
  private MobileDefaultsHandler defaultsHandler;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    OBContext.setAdminMode(false);
    try {
      final String command = request.getParameter("command");
      JSONObject result = new JSONObject();
      if (command.equals("preRenderActions")) {
        result = getPrerrenderData(request);
      } else if (command.equals("companyLogo")) {
        result = getCompanyLogo(request);
      } else if (command.equals("userImages")) {
        result = getUserImages(request);
      } else if (command.equals("preLoginActions")) {
        result = preLogin(request);
      } else if (command.equals("initActions")) {
        result = initActions(request);
      }

      writeResult(response, result.toString());
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } finally {
      OBContext.restorePreviousMode();
      OBContext.setOBContext((OBContext) null);
    }
  }

  protected JSONObject getPrerrenderData(HttpServletRequest request) throws JSONException {
    JSONObject data = new JSONObject();
    JSONObject item = null;

    data.put("labels", LabelsComponent.getLabels(null, getModuleId()));

    if (OBContext.getOBContext().getUser().getId().equals("0")) {
      data.put("activeSession", false);
    } else {
      data.put("activeSession", true);
    }
    item = new JSONObject();
    final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    item.put("date", props.getProperty(KernelConstants.DATE_FORMAT_PROPERTY, "dd-MM-yyyy"));
    item.put("dateTime",
        props.getProperty(KernelConstants.DATETIME_FORMAT_PROPERTY, "dd-MM-yyyy HH:mm:ss"));
    data.put("dateFormats", item);

    return data;
  }

  protected String getModuleId() {
    return MobileCoreConstants.MODULE_ID;
  }

  protected JSONObject getCompanyLogo(HttpServletRequest request) throws JSONException {
    JSONObject result = new JSONObject();

    result.put("logoUrl", getClientLogoData("0"));
    return result;
  }

  protected JSONObject getUserImages(HttpServletRequest request) throws JSONException {
    String formId = defaultsHandler.getDefaults(request.getParameter("appName")).getFormId();

    JSONObject result = new JSONObject();
    JSONArray data = new JSONArray();

    String hqlUser = "select distinct user.name, user.username, user.id "
        + "from ADUser user, ADUserRoles userRoles, ADRole role " //
        + "where user.active = true and " //
        + "userRoles.active = true and " //
        + "role.active = true and " //
        + "user.username is not null and " //
        + "user.password is not null and " //
        + "user.id = userRoles.userContact.id and " //
        + "userRoles.role.id = role.id ";

    if (formId != null) {
      hqlUser += " and exists (select 1 from ADFormAccess a " //
          + " where a.role = role " //
          + " and a.active = true " //
          + " and a.specialForm.id = :formId)";
    }

    hqlUser += "order by user.name";

    Query qryUser = OBDal.getInstance().getSession().createQuery(hqlUser);

    if (formId != null) {
      qryUser.setParameter("formId", formId);
    }

    for (Object qryUserObject : qryUser.list()) {
      final Object[] qryUserObjectItem = (Object[]) qryUserObject;

      JSONObject item = new JSONObject();
      item.put("name", qryUserObjectItem[0]);
      item.put("userName", qryUserObjectItem[1]);

      // Get the image for the current user
      String hqlImage = "select image.mimetype, image.bindaryData "
          + "from ADImage image, ADUser user "
          + "where user.image = image.id and user.id = :theUserId";
      Query qryImage = OBDal.getInstance().getSession().createQuery(hqlImage);
      qryImage.setParameter("theUserId", qryUserObjectItem[2].toString());
      String imageData = "none";
      for (Object qryImageObject : qryImage.list()) {
        final Object[] qryImageObjectItem = (Object[]) qryImageObject;
        imageData = "data:"
            + qryImageObjectItem[0].toString()
            + ";base64,"
            + org.apache.commons.codec.binary.Base64
                .encodeBase64String((byte[]) qryImageObjectItem[1]);
      }
      item.put("image", imageData);

      // Get the session status for the current user
      String hqlSession = "select distinct session.username, session.sessionActive "
          + "from ADSession session "
          + "where session.username = :theUsername and session.sessionActive = 'Y' and "
          + "session.loginStatus = 'OBPOS_POS'";
      Query qrySession = OBDal.getInstance().getSession().createQuery(hqlSession);
      qrySession.setParameter("theUsername", qryUserObjectItem[1].toString());
      qrySession.setMaxResults(1);
      String sessionData = "false";
      if (qrySession.uniqueResult() != null) {
        sessionData = "true";
      }
      item.put("connected", sessionData);

      data.put(item);
    }
    result.put("data", data);
    return result;
  }

  protected JSONObject preLogin(HttpServletRequest request) throws JSONException {
    JSONObject data = new JSONObject();

    return data;
  }

  protected JSONObject createServerJSON(MobileServerDefinition server) throws JSONException {
    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", server.getName());
    // strip the http:// part if there, is not needed in the client
    String serverUrl = server.getURL() != null ? server.getURL().trim() : null;
    if (serverUrl != null && serverUrl.toLowerCase().trim().startsWith("http://")) {
      serverUrl = serverUrl.substring("http://".length());
    }
    jsonObject.put("address", serverUrl);
    jsonObject.put("online", true);
    jsonObject.put("priority", server.getPriority());
    jsonObject.put("allServices", server.isAllservices());
    if ("MAIN".equals(server.getServerType())) {
      jsonObject.put("mainServer", true);
    } else {
      jsonObject.put("mainServer", false);
    }
    JSONArray services = new JSONArray();
    if (!server.isAllservices() && server.getOBMOBCSERVERSERVICESList().size() > 0) {
      if (server.getServiceSelection().equals("N")) {
        for (MobileServerService service : server.getOBMOBCSERVERSERVICESList()) {
          services.put(service.getObmobcServices().getService());
        }
      } else if (server.getServiceSelection().equals("Y")) {
        String hql = "select srvc.service from OBMOBC_SERVICES as srvc where not exists (select 1 from OBMOBC_SERVER_SERVICES where srvc.id=obmobcServices.id and obmobcServerDefinition.id = ?)";
        Query queryServices = OBDal.getInstance().getSession().createQuery(hql);
        queryServices.setString(0, server.getId());

        for (Object obj : queryServices.list()) {
          services.put(obj);
        }
      }
    }
    jsonObject.put("services", services);
    return jsonObject;
  }

  protected JSONArray getServers() throws JSONException {
    JSONArray respArray = new JSONArray();

    boolean multiServerEnabled = false;
    try {
      OBContext.setAdminMode(false);
      multiServerEnabled = "Y".equals(Preferences.getPreferenceValue(
          "OBMOBC_MultiServerArchitecture", true, null, null, null, null, (String) null).trim());
    } catch (PropertyException ignore) {
      // ignore on purpose
    } finally {
      OBContext.restorePreviousMode();
    }

    if (!multiServerEnabled) {
      return respArray;
    }

    OBQuery<MobileServerDefinition> servers = OBDal.getInstance().createQuery(
        MobileServerDefinition.class, " order by " + MobileServerDefinition.PROPERTY_PRIORITY);
    servers.setFilterOnReadableClients(false);
    servers.setFilterOnReadableOrganization(false);
    for (MobileServerDefinition server : servers.list()) {
      respArray.put(createServerJSON(server));
    }

    return respArray;
  }

  protected JSONArray getServices() throws JSONException {
    JSONArray respArray = new JSONArray();

    OBQuery<MobileServiceDefinition> services = OBDal.getInstance().createQuery(
        MobileServiceDefinition.class, "");
    services.setFilterOnReadableOrganization(false);

    for (MobileServiceDefinition service : services.list()) {
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put("name", service.getService());
      jsonObject.put("type", service.getRoutingtype());
      respArray.put(jsonObject);
    }
    return respArray;
  }

  protected JSONObject initActions(HttpServletRequest request) throws JSONException {
    JSONObject data = new JSONObject();
    final String cacheSessionId = request.getParameter("cacheSessionId");
    if (cacheSessionId == null) {
      String generatedCacheSessionId = SequenceIdData.getUUID();
      data.put("cacheSessionId", generatedCacheSessionId);
    }
    data.put("servers", getServers());
    // We do not need to be in a specific terminal to load services
    data.put("services", getServices());
    return data;
  }

  protected String getClientLogoData(String clientId) {
    // POS uses its own image for login, check if it is installed to use it, or use default one
    // other case
    Entity clientEntity = ModelProvider.getInstance().getEntity(ClientInformation.class);
    String imageProperty = clientEntity.hasProperty("obposCompanyLoginImage") ? "obposCompanyLoginImage"
        : "yourCompanyMenuImage";

    String hqlCompanyImage = "select image.mimetype, image.bindaryData "
        + "from ADImage image, ClientInformation clientInfo " + "where clientInfo." + imageProperty
        + " = image.id and clientInfo.client.id = :theClientId";

    Query qryCompanyImage = OBDal.getInstance().getSession().createQuery(hqlCompanyImage);
    qryCompanyImage.setParameter("theClientId", clientId);
    String companyImageData = "../../utility/ShowImageLogo?logo=yourcompanylogin";
    for (Object qryCompanyImageObject : qryCompanyImage.list()) {
      final Object[] qryCompanyImageObjectItem = (Object[]) qryCompanyImageObject;
      companyImageData = "data:"
          + qryCompanyImageObjectItem[0].toString()
          + ";base64,"
          + org.apache.commons.codec.binary.Base64
              .encodeBase64String((byte[]) qryCompanyImageObjectItem[1]);
    }
    return companyImageData;
  }
}