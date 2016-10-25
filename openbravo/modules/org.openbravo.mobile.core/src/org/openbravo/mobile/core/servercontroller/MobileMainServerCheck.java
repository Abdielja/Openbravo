/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.servercontroller;

import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.MobileServerDefinition;

/**
 * This class implements the thread which checks if the main servers are back online and will start
 * the transitioning back to online if so. It is called when transition to offline starts and sets
 * the server to offline after offlinetime (3 minutes).
 * 
 * @author mdejuana
 */
public class MobileMainServerCheck implements Runnable {

  private static final Logger log = Logger.getLogger(MobileMainServerCheck.class);
  private static final int INTERVALTIME = 10000;
  private static final int OFFLINETIME = 60000 * 3;
  private static boolean threadIsRunning = false;

  public synchronized static boolean isThreadRunning() {
    return threadIsRunning;
  }

  private synchronized static void setThreadIsRunning(boolean isRunning) {
    threadIsRunning = isRunning;
  }

  private OBContext obContext;
  private long startTime = System.currentTimeMillis();

  public MobileMainServerCheck(OBContext context) {
    this.obContext = context;
  }

  public void run() {
    // already running bail out
    if (isThreadRunning()) {
      return;
    }
    setThreadIsRunning(true);
    try {
      log.debug("Started main server check");
      OBContext.setOBContext(obContext);
      OBContext.setAdminMode(false);

      // if this is a main server then check all servers, if this is a store server
      // then check all the servers which are accessible from this organization/store.
      // Note the process needs to be started using the credentials/org of the store in that case
      boolean addOrgParam = false;
      String queryPart = "";
      if (MobileServerController.getInstance().isThisAStoreServer()) {
        log.debug("Is a store server so use org filter in server query");

        addOrgParam = true;
        queryPart = " and (" + MobileServerDefinition.PROPERTY_ALLORGS
            + "=true or :org in elements(" + MobileServerDefinition.PROPERTY_OBMOBCSERVERORGSLIST
            + "))";
      }

      while (true) {
        int onlineServers = 0;

        OBQuery<MobileServerDefinition> servers = OBDal.getInstance().createQuery(
            MobileServerDefinition.class,
            "client.id=:clientId and " + MobileServerDefinition.PROPERTY_STATUS + "='"
                + MobileServerState.OFFLINE.getValue() + "' and "
                + MobileServerDefinition.PROPERTY_TRIGGERSTATE + "=true " + queryPart
                + " order by " + MobileServerDefinition.PROPERTY_PRIORITY);
        servers.setFilterOnReadableClients(false);
        servers.setFilterOnReadableOrganization(false);
        if (addOrgParam) {
          servers.setNamedParameter("org", obContext.getCurrentOrganization().getId());
        }
        servers.setNamedParameter("clientId", obContext.getCurrentClient().getId());
        JSONObject parameters = new JSONObject();

        final MobileServerDefinition thisServerDef = MobileServerController.getInstance()
            .getThisServerDefinition();

        try {
          parameters.put("organization", obContext.getCurrentOrganization().getId());
          parameters.put("client", obContext.getCurrentClient().getId());
          parameters.put("mobileServerKey", thisServerDef.getMobileServerKey());
          parameters.put("serverStatus", thisServerDef.getStatus());
        } catch (JSONException e1) {
          log.error("MobileMainServerCheck: Error setting org and client on parameters", e1);
        }

        List<MobileServerDefinition> offlineServers = servers.list();
        log.debug("Found " + offlineServers.size() + " offline servers");

        for (MobileServerDefinition srv : offlineServers) {
          log.debug("Checking " + srv.getName());
          JSONObject resp = null;
          // get column (Ping service) value
          String pingService = srv.getPingservice() != null ? srv.getPingservice()
              : MobileServerUtils.OBWSPATH + MobileServerStatusInformation.class.getName();
          try {
            log.debug("Calling ping service " + pingService);
            resp = new JSONObject(MobileServerRequestExecutor.getInstance().request(srv,
                pingService, MobileServerUtils.getAuthenticationQueryParams(), "POST", parameters))
                .getJSONObject("response");
            if (resp != null) {
              log.debug("Response " + resp);

              if (resp.has("serverStatus")) {
                srv.setStatus(resp.getString("serverStatus"));
                OBDal.getInstance().save(srv);

                if (resp.get("serverStatus").equals(MobileServerState.ONLINE.getValue())) {
                  log.debug("Server is online, inc online servers");
                  onlineServers++;
                }
              } else {
                log.debug("No data in response, assuming server is online");
                // answer without data, assume online
                srv.setStatus(MobileServerState.ONLINE.getValue());
                OBDal.getInstance().save(srv);
                onlineServers++;
              }
            } else {
              log.debug("Response is null, setting server state to offline");
              srv.setStatus(MobileServerState.OFFLINE.getValue());
              OBDal.getInstance().save(srv);
            }
          } catch (Exception e) {
            srv.setStatus(MobileServerState.OFFLINE.getValue());
            OBDal.getInstance().save(srv);
          }
        }

        // Keep checking every INTERVALTIME milliseconds till all main servers are online
        log.debug("Status check " + offlineServers.size() + " onlineservers " + onlineServers);

        if (offlineServers.size() > 0 && offlineServers.size() != onlineServers) {

          log.debug("Checking how long offline " + System.currentTimeMillis() + " "
              + (startTime + OFFLINETIME));
          // now the system is really offline
          if (System.currentTimeMillis() > (startTime + OFFLINETIME)
              && !MobileServerState.OFFLINE.getValue().equals(thisServerDef.getStatus())) {
            log.debug("Setting status to offline");
            MobileServerController.getInstance().setMobileServerState(MobileServerState.OFFLINE);
          }

          // commit any saves
          OBDal.getInstance().commitAndClose();

          // wait before retrying
          try {
            log.debug("Wait before rechecking");
            Thread.sleep(INTERVALTIME);
          } catch (InterruptedException e) {
            log.error("MobileMainServerCheck interruption when sleeping", e);
          }

        } else {
          // stop the thread if we can transition back to online

          // already online
          if (MobileServerState.ONLINE.getValue().equals(thisServerDef.getStatus())
              || MobileServerState.TRANSITION_TO_ONLINE.getValue()
                  .equals(thisServerDef.getStatus())) {
            log.debug("Server is back online or transitioning to online, returning");
            break;
          }

          if (MobileServerController.getInstance().transitionToOnline()) {
            log.debug("Server is transitioned to online, returning");
            break;
          }
          log.debug("Server is still offline");

          // failed, still offline, continue
          OBDal.getInstance().commitAndClose();
        }
      }
    } finally {
      OBContext.restorePreviousMode();
      setThreadIsRunning(false);
    }
  }
}
