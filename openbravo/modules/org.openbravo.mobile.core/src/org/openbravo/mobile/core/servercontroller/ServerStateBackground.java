/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.servercontroller;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

/**
 * This class can be started as a background process. It will check the server status and will send
 * the status of the current server to the other servers.
 * 
 * @author mdejuana
 */
public class ServerStateBackground extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(ServerStateBackground.class);
  private ProcessLogger logger;

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    OBError result = new OBError();
    try {
      OBContext.setAdminMode(false);
      result.setType("Success");
      result.setTitle(OBMessageUtils.messageBD("Success"));

      final ProcessContext obContext = bundle.getContext();
      final Date fiveMinLess = new Date(new Date().getTime() - 3600 * 5);
      MobileServerDefinition thisServerDef = MobileServerController.getInstance()
          .readThisServerDefinition();

      log4j.debug("Starting ServerStateBackground process");

      // if this is a main server then check all servers, if this is a store server
      // then check all the servers which are accessible from this organization/store.
      // Note the process needs to be started using the credentials/org of the store in that case
      boolean addOrgParam = false;
      String queryPart = "";
      if (MobileServerController.getInstance().isThisAStoreServer()) {
        log4j.debug("Store server, filtering servers using org");
        addOrgParam = true;
        queryPart = " and (" + MobileServerDefinition.PROPERTY_ALLORGS
            + "=true or :org in elements(" + MobileServerDefinition.PROPERTY_OBMOBCSERVERORGSLIST
            + "))";
      }

      OBQuery<MobileServerDefinition> serversQry = OBDal.getInstance().createQuery(
          MobileServerDefinition.class,
          "client.id=:clientId and " + MobileServerDefinition.PROPERTY_MOBILESERVERKEY + "!='"
              + thisServerDef.getMobileServerKey() + "' " + queryPart + " order by "
              + MobileServerDefinition.PROPERTY_PRIORITY);
      serversQry.setFilterOnReadableClients(false);
      serversQry.setFilterOnReadableOrganization(false);
      serversQry.setNamedParameter("clientId", obContext.getClient());
      if (addOrgParam) {
        serversQry.setNamedParameter("org", obContext.getOrganization());
      }

      final JSONObject parameters = new JSONObject();
      parameters.put("organization", obContext.getOrganization());
      parameters.put("client", obContext.getClient());
      parameters.put("mobileServerKey", thisServerDef.getMobileServerKey());
      parameters.put("serverStatus", thisServerDef.getStatus());

      final String queryParams = MobileServerUtils.getAuthenticationQueryParams(
          obContext.getClient(), obContext.getOrganization(), obContext.getRole(),
          obContext.getUser());
      int cntOfflineServers = 0;

      final List<MobileServerDefinition> servers = serversQry.list();

      log4j.debug("Found " + servers.size() + " servers ");

      for (MobileServerDefinition srv : servers) {
        log4j.debug("Server " + srv + " status " + srv.getStatus());

        if (!srv.getStatus().equals(MobileServerState.ONLINE.getValue())
            || (srv.getStatus().equals(MobileServerState.ONLINE.getValue()) && srv.getUpdated()
                .before(fiveMinLess))) {
          JSONObject resp = null;
          // get new column (Ping service) value
          String pingService = srv.getPingservice() != null ? srv.getPingservice()
              : MobileServerUtils.OBWSPATH + MobileServerStatusInformation.class.getName();
          log4j.debug("Calling pingservice " + pingService);
          try {
            resp = new JSONObject(MobileServerRequestExecutor.getInstance().request(srv,
                pingService, queryParams, "POST", parameters)).getJSONObject("response");
            log4j.debug("Response " + resp);
            if (resp != null) {
              if (resp.has("serverStatus")) {
                log4j.debug("Resp has server status " + resp.get("serverStatus"));
                srv.setStatus(resp.getString("serverStatus"));
              } else {
                srv.setStatus(MobileServerState.ONLINE.getValue());
              }
            } else {
              log4j.debug("Resp == null --> offline");
              srv.setStatus(MobileServerState.OFFLINE.getValue());
            }
            OBDal.getInstance().save(srv);
            OBDal.getInstance().flush();
            if (srv.isTriggerstate()
                && MobileServerState.OFFLINE.getValue().equals(srv.getStatus())) {
              log4j.debug("Server is a trigger state server inc cntOfflineServers");
              cntOfflineServers++;
            }
          } catch (Exception e) {
            log4j.debug(e.getMessage(), e);
            srv.setStatus(MobileServerState.OFFLINE.getValue());
            if (srv.isTriggerstate()) {
              cntOfflineServers++;
              log4j.debug("Server is a trigger state server inc cntOfflineServers");
            }
            OBDal.getInstance().save(srv);
            OBDal.getInstance().flush();
          }
        }
      }

      // save any changes we did as the next code can start separate threads
      OBDal.getInstance().commitAndClose();

      // transition depending on the number of online/offline servers
      // read the server def again as the checks above can have taken some time
      thisServerDef = MobileServerController.getInstance().readThisServerDefinition();
      final MobileServerState state = MobileServerController.getInstance().getMobileServerState(
          thisServerDef);
      String logAction = "";
      if (cntOfflineServers > 0) {
        // offline servers, let's go offline
        switch (state) {
        case TRANSITION_TO_OFFLINE:
          // transition again, if the thread is already running nothing will happen
          logAction = "Already transitioning to offline, just retry it";
          log4j.debug("Transition to offline");
          MobileServerController.getInstance().transitionToOffline();
          break;
        case ONLINE:
          logAction = "Transition to offline";
          log4j.debug("Transition to offline");
          MobileServerController.getInstance().transitionToOffline();
          break;
        case TRANSITION_TO_ONLINE:
          // if transitioning to online then check if the transition process is running
          // if not then go offline, if yes, then we need to wait and will try again
          if (!MobileServerController.getInstance().isTransitioningToOnline()) {
            logAction = "Transition to offline";
            log4j.debug("Transition to offline");
            MobileServerController.getInstance().transitionToOffline();
          } else {
            // do nothing in this case, just wait until the transition to offline is done
            // will automatically retry in due course
            logAction = "Already transitioning to online, wait for next cycle for transition to offline";
          }
          break;
        case OFFLINE:
          // already offline do nothing
          logAction = "Already offline";
          break;
        }
      } else if (cntOfflineServers == 0) {
        switch (state) {
        case TRANSITION_TO_OFFLINE:
          // state is transitioning offline, but not really doing that, repair it
          if (!MobileServerController.getInstance().isTransitioningToOffline()) {
            logAction = "Transition to onine";
            MobileServerController.getInstance().transitionToOnline();
          } else {
            // do nothing in this case, just wait until the transition to offline is done
            // will automatically retry in due course
            logAction = "Already transitioning to offline, wait for next cycle for transition to online";
          }
        case ONLINE:
          // already online do nothing
          logAction = "Already online";
          break;
        case TRANSITION_TO_ONLINE:
          // if transitioning to online then check if the transition process is running
          // if not then start it, if yes, then we are fine, no need to do anything
          if (MobileServerController.getInstance().isTransitioningToOnline()) {
            logAction = "Already transitioning to online";
            // just let it go
          } else {
            logAction = "Transition to online";
            log4j.debug("Transition to online");
            MobileServerController.getInstance().transitionToOnline();
          }
          break;
        case OFFLINE:
          logAction = "Transition to online";
          log4j.debug("Transition to online");
          MobileServerController.getInstance().transitionToOnline();
          break;
        }
      }

      result.setMessage("Server count: " + servers.size() + " - offline server count "
          + cntOfflineServers + " action: " + logAction);
      logger.logln(result.getMessage());
      bundle.setResult(result);

      // clean up any open connections
      OBDal.getInstance().commitAndClose();
    } catch (Throwable t) {
      result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(),
          OBContext.getOBContext().getLanguage().getLanguage(), t.getMessage());
      result.setType("Error");
      result.setTitle(OBMessageUtils.messageBD("Error"));
      log4j.error(result.getMessage(), t);
      logger.logln(result.getMessage());
      bundle.setResult(result);
      OBDal.getInstance().rollbackAndClose();
      return;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
