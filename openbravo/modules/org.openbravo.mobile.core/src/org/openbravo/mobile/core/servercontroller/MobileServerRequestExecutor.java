/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.servercontroller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.mobile.core.MobileServerService;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Class which should be used to execute all inter-server requests. It triggers offline behavior if
 * a server can not be reached.
 * 
 * @author mtaal
 */
public class MobileServerRequestExecutor {

  private static MobileServerRequestExecutor instance = new MobileServerRequestExecutor();
  private static int RETRYS = 3;

  public static MobileServerRequestExecutor getInstance() {
    return instance;
  }

  public static void setInstance(MobileServerRequestExecutor instance) {
    MobileServerRequestExecutor.instance = instance;
  }

  protected HttpURLConnection createConnection(String serverUrl, String wsPart, String method,
      JSONObject parameters) throws Exception {
    String localServerUrl = serverUrl;
    // TODO: maybe handle this with a validation on the formfield
    if (!localServerUrl.toLowerCase().startsWith("http://")
        && !localServerUrl.toLowerCase().startsWith("https://")) {
      localServerUrl = "http://" + localServerUrl;
    }
    final URL url = new URL(localServerUrl + wsPart);
    final HttpURLConnection hc = (HttpURLConnection) url.openConnection();
    hc.setRequestMethod(method);
    hc.setAllowUserInteraction(false);
    hc.setDefaultUseCaches(false);
    hc.setDoOutput(true);
    hc.setDoInput(true);
    hc.setInstanceFollowRedirects(true);
    hc.setUseCaches(false);
    hc.setRequestProperty("Content-Type", "application/json;charset=utf-8");
    OutputStream os = new BufferedOutputStream(hc.getOutputStream());
    os.write(parameters.toString().getBytes());
    os.flush();
    return hc;
  }

  public String request(MobileServerDefinition server, String serviceName,
      String authenticationParams, String method, JSONObject parameters) throws Exception {

    String wsPart = serviceName;
    // add authentication parameters
    if (MobileServerUtils.isOpenbravoServer(server)) {
      wsPart = wsPart
          + (authenticationParams == null ? "" : (serviceName.contains("?") ? "&" : "?")
              + authenticationParams);
    }

    final StringBuilder sb = new StringBuilder();

    final HttpURLConnection hc = createConnection(server.getURL(), wsPart, method, parameters);
    hc.connect();
    final InputStream is = hc.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  protected JSONObject tryConnection(MobileServerDefinition server, String serviceName,
      JSONObject parameters, int tries) {
    JSONObject resp = new JSONObject();
    if (tries <= RETRYS) {
      try {
        resp = new JSONObject(request(server, serviceName,
            MobileServerUtils.getAuthenticationQueryParams(), "POST", parameters))
            .getJSONObject("response");
        if (resp.has("serverStatus")
            && resp.get("serverStatus").equals(MobileServerState.OFFLINE.getValue())) {
          return tryConnection(server, serviceName, parameters, tries + 1);
        }
      } catch (Throwable t) {
        return tryConnection(server, serviceName, parameters, tries + 1);
      }
    } else {
      server.setStatus(MobileServerState.OFFLINE.getValue());
      OBDal.getInstance().save(server);
      if (server.isTriggerstate()) {
        MobileServerController.getInstance().transitionToOffline();
      }
    }
    return resp;
  }

  public JSONObject executeRequest(String serviceName, JSONObject parameters) {
    try {
      OBContext.setAdminMode(false);
      MobileServerDefinition server = null;

      final MobileServerDefinition thisServerDef = MobileServerController.getInstance()
          .getThisServerDefinition();

      // Find a server which can execute the service
      OBQuery<MobileServerDefinition> servers = OBDal.getInstance().createQuery(
          MobileServerDefinition.class,
          "(" + MobileServerDefinition.PROPERTY_ALLORGS + "=true or :org in elements("
              + MobileServerDefinition.PROPERTY_OBMOBCSERVERORGSLIST
              + "))  and client.id=:clientId and "
              + MobileServerDefinition.PROPERTY_MOBILESERVERKEY + "!='"
              + thisServerDef.getMobileServerKey() + "' order by "
              + MobileServerDefinition.PROPERTY_PRIORITY);
      servers.setFilterOnReadableClients(false);
      servers.setFilterOnReadableOrganization(false);
      servers.setNamedParameter("org",
          OBDal.getInstance().get(Organization.class, parameters.getString("organization")));

      if (!parameters.has("client")) {
        final Organization org = OBDal.getInstance().get(Organization.class,
            parameters.getString("organization"));
        parameters.put("client", DalUtil.getId(org.getClient()));
      }
      servers.setNamedParameter("clientId", parameters.getString("client"));

      for (MobileServerDefinition srv : servers.list()) {
        // only call online servers
        if (!MobileServerState.ONLINE.getValue().equals(srv.getStatus())) {
          continue;
        }
        server = null;
        if (srv.isAllservices()) {
          server = srv;
        } else {
          for (MobileServerService service : srv.getOBMOBCSERVERSERVICESList()) {
            final String serviceIdentifier = service.getObmobcServices().getService();
            if (serviceName.contains(serviceIdentifier)) {
              server = srv;
            }
          }
        }

        // found a server, call it
        if (server != null) {
          parameters.put("mobileServerKey", thisServerDef.getMobileServerKey());
          parameters.put("serverStatus", thisServerDef.getStatus());

          JSONObject resp = tryConnection(server, serviceName, parameters, 1);
          if (resp.length() != 0) {
            server.setStatus(MobileServerState.ONLINE.getValue());
            OBDal.getInstance().save(server);
            return resp;
          }
          // continue with the next server until all have been
        }
      }
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
    throw new OBException("No server found for service " + serviceName);
  }
}
