/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.servercontroller;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.client.kernel.event.TransactionCompletedEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.mobile.core.MobileServerOrganization;
import org.openbravo.mobile.core.MobileServerService;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.importprocess.ImportEntry;

/**
 * Provides methods to obtain and manage the online status of the current server.
 * 
 * @author mtaal
 */
public class MobileServerController {

  private final static Logger log = Logger.getLogger(MobileServerController.class);

  private MobileServerDefinition thisServerDefinition = null;

  private static MobileServerController instance = new MobileServerController();

  public static MobileServerController getInstance() {
    return instance;
  }

  public static void setInstance(MobileServerController instance) {
    MobileServerController.instance = instance;
  }

  private boolean isTransitioningToOnline = false;

  /**
   * Creates the message to be send to another server. Note the caller has to call commit to make
   * sure the message gets send.
   * 
   * Message sending is asynchronous.
   * 
   * @return the created {@link ImportEntry}
   */
  public ImportEntry sendMessageToStore(Organization store, String json) {
    try {
      OBContext.setAdminMode(false);
      final ImportEntry importEntry = OBProvider.getInstance().get(ImportEntry.class);
      importEntry.setImportStatus("Processed");
      importEntry.setImported(new Date());
      importEntry.setClient(store.getClient());
      importEntry.setRole(OBDal.getInstance().get(Role.class,
          OBContext.getOBContext().getRole().getId()));
      importEntry.setOrganization(store);
      importEntry.setJsonInfo(json);
      importEntry.setTypeofdata("OBMOBC_ServerMessage");
      OBDal.getInstance().save(importEntry);
      return importEntry;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Creates the message to be send to a central server (so no specific organization is used). Note
   * the caller has to call commit to make sure the message gets send.
   * 
   * Message sending is asynchronous.
   * 
   * @return the created {@link ImportEntry} containing the message
   */
  public ImportEntry sendMessageToCentral(String json) {
    try {
      OBContext.setAdminMode(false);
      final ImportEntry importEntry = OBProvider.getInstance().get(ImportEntry.class);
      // it is processed on this side, when the message is replicated to the store server
      // it is
      importEntry.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
      importEntry.setRole(OBDal.getInstance().get(Role.class,
          OBContext.getOBContext().getRole().getId()));
      importEntry.setImportStatus("Processed");
      importEntry.setImported(new Date());
      importEntry.setJsonInfo(json);
      importEntry.setTypeofdata("OBMOBC_ServerMessage");
      OBDal.getInstance().save(importEntry);
      return importEntry;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @param mobileServerDefinition
   *          the server for which the state must be returned
   * @return return the enum representing the state of the server
   */
  public MobileServerState getMobileServerState(MobileServerDefinition mobileServerDefinition) {
    return MobileServerState.getMobileServerState(mobileServerDefinition.getStatus());
  }

  /**
   * Utility method to write the current server state to json.
   */
  public void writeServerStatusJSON(Writer w) {
    try {
      final MobileServerState state = getMobileServerState(getThisServerDefinition());
      if (state == null) {
        return;
      }
      w.write("\"serverStatusSignal\": \"" + state.getValue() + "\"");
      return;
    } catch (IOException e) {
      throw new OBException(e);
    }
  }

  /**
   * Return true if the current server state is either
   * {@link MobileServerState#TRANSITION_TO_OFFLINE} or
   * {@link MobileServerState#TRANSITION_TO_ONLINE}. Return false otherwise.
   */
  public boolean serverHasTransitioningStatus() {
    final MobileServerState state = getMobileServerState(getThisServerDefinition());
    if (state == null) {
      return false;
    }
    return MobileServerState.TRANSITION_TO_OFFLINE.equals(state)
        || MobileServerState.TRANSITION_TO_ONLINE.equals(state);
  }

  public MobileServerState getThisMobileServerState() {
    return getMobileServerState(getThisServerDefinition());
  }

  /**
   * Sets and save the state but does not do a commit.
   */
  public synchronized void setMobileServerState(MobileServerState state) {
    MobileServerDefinition thisServer = readThisServerDefinition();
    thisServer.setStatus(state.getValue());
    OBDal.getInstance().save(thisServer);
  }

  /**
   * Returns the {@link MobileServerDefinition} for this server. Note: the instance is cached and is
   * not part of the current Session. If you want to have a server definition which is part of the
   * current thread/session use the {@link #readThisServerDefinition()} method.
   */
  public synchronized MobileServerDefinition getThisServerDefinition() {
    if (thisServerDefinition == null) {

      try {
        OBContext.setAdminMode(false);
        thisServerDefinition = readThisServerDefinition();
        // initialize some things as the server def is cached
        for (MobileServerOrganization serverOrg : thisServerDefinition.getOBMOBCSERVERORGSList()) {
          Hibernate.initialize(serverOrg);
        }
        // initialize some things
        for (MobileServerService serverService : thisServerDefinition.getOBMOBCSERVERSERVICESList()) {
          Hibernate.initialize(serverService);
        }
        Hibernate.initialize(thisServerDefinition.getOBMOBCSERVERSERVICESList());
        SessionHandler.getInstance().getSession().evict(thisServerDefinition);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return thisServerDefinition;
  }

  /**
   * Reads the {@link MobileServerDefinition} for the current server and returns it. Does not cache
   * the instance.
   */
  public MobileServerDefinition readThisServerDefinition() {
    // get mobile.server.key from the Openbravo.properties
    String mobileServerKey = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty(MobileServerUtils.MOBILE_SERVER_KEY);

    if (mobileServerKey == null) {
      return null;
    }

    try {
      OBContext.setAdminMode(false);
      OBCriteria<MobileServerDefinition> serverQuery = OBDal.getInstance().createCriteria(
          MobileServerDefinition.class);
      serverQuery.add(Restrictions.eq(MobileServerDefinition.PROPERTY_MOBILESERVERKEY,
          mobileServerKey));
      // disable the client and organization filters to allow reading this server definition
      // with a client 0 (for instance from an ant task)
      // it is safe to do it because there is a unique constraints on the mobile server key
      serverQuery.setFilterOnReadableClients(false);
      serverQuery.setFilterOnReadableOrganization(false);
      // initialize some things
      MobileServerDefinition serverDefinition = (MobileServerDefinition) serverQuery.uniqueResult();
      return serverDefinition;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public synchronized void resetThisServerDefinitionCache() {
    thisServerDefinition = null;
  }

  /**
   * Trigger the transition to offline code.
   */
  public synchronized void transitionToOffline() {
    if (getThisMobileServerState().equals(MobileServerState.TRANSITION_TO_OFFLINE)) {
      doTransitionToOffline();
      return;
    }
    if (!getThisMobileServerState().equals(MobileServerState.ONLINE)) {
      return;
    }
    setMobileServerState(MobileServerState.TRANSITION_TO_OFFLINE);
    OBDal.getInstance().commitAndClose();
    doTransitionToOffline();
  }

  private void doTransitionToOffline() {
    final MobileMainServerCheck checker = new MobileMainServerCheck(OBContext.getOBContext());
    final Thread thrd = new Thread(checker);
    // daemon ensures that the threads does not block exiting the jvm
    thrd.setDaemon(true);
    thrd.setName("MobileMainServerCheckThread");
    thrd.start();
  }

  /**
   * Check the server status and also checks if the corresponding background logic is running to
   * manage the state. If not it starts the logic.
   */
  public void checkStartServerStateLogic() {
    final MobileServerState serverState = getMobileServerState(getThisServerDefinition());
    if (MobileServerState.TRANSITION_TO_ONLINE.equals(serverState)) {
      // the transition to online is not being executed, restart it
      if (!isTransitioningToOnline) {
        log.debug("Transition to online status but no transitioning to online running, restarting it");
        transitionToOnline();
      }
    } else if (MobileServerState.OFFLINE.equals(serverState)
        || MobileServerState.TRANSITION_TO_OFFLINE.equals(serverState)) {
      if (!MobileMainServerCheck.isThreadRunning()) {
        log.debug("Transition to online status but no transitioning to online running, restarting it");
        doTransitionToOffline();
      }
    }
  }

  /**
   * return true if the server is transitioning to offline currently
   */
  public boolean isTransitioningToOffline() {
    return MobileMainServerCheck.isThreadRunning();
  }

  /**
   * return true if the server is transitioning to online currently
   */
  public boolean isTransitioningToOnline() {
    return isTransitioningToOnline;
  }

  /**
   * Force/start the transition to online procedure.
   */
  public synchronized boolean transitionToOnline() {
    if (isTransitioningToOnline) {
      // already here go away
      return true;
    }
    try {
      log.debug("Starting transition to online");
      isTransitioningToOnline = true;
      setMobileServerState(MobileServerState.TRANSITION_TO_ONLINE);
      OBDal.getInstance().commitAndClose();

      BeanManager beanManager = WeldUtils.getStaticInstanceBeanManager();
      Set<Bean<?>> beans = beanManager.getBeans(MobileServerTransitionToOnlineHandler.class);

      log.debug("Calling online transition handlers");

      final List<MobileServerTransitionToOnlineHandler> handlers = new ArrayList<MobileServerTransitionToOnlineHandler>();
      for (Bean<?> bean : beans) {
        MobileServerTransitionToOnlineHandler handler = (MobileServerTransitionToOnlineHandler) beanManager
            .getReference(bean, MobileServerTransitionToOnlineHandler.class,
                beanManager.createCreationalContext(bean));
        log.debug("Adding handler: " + handler.getClass().getName());
        handlers.add(handler);
      }

      // try the handlers 3 times
      for (int i = 0; i < 3; i++) {
        for (MobileServerTransitionToOnlineHandler handler : new ArrayList<MobileServerTransitionToOnlineHandler>(
            handlers)) {
          log.debug("Calling online transition handler: " + handler.getClass().getName()
              + " iteration " + i);
          handler.processTransactions();
          if (handler.isReadyToGoOnline()) {
            log.debug("Handler is ready to go online " + handler.getClass().getName());
            handlers.remove(handler);
          }
        }
        // all handlers successfull, break away
        if (handlers.isEmpty()) {
          log.debug("All handlers are ready to go online");
          break;
        }
      }
      if (handlers.isEmpty()) {
        log.debug("All handlers were ready to go online, going online");
        setMobileServerState(MobileServerState.ONLINE);
        OBDal.getInstance().commitAndClose();
        return true;
      } else {
        log.debug("Not all handlers were ready to go online, back to offline");
        // failed back offline
        setMobileServerState(MobileServerState.OFFLINE);
        OBDal.getInstance().commitAndClose();
        return false;
      }
    } finally {
      isTransitioningToOnline = false;
    }
  }

  public boolean isThisAStoreServer() {
    final MobileServerDefinition serverDefinition = getThisServerDefinition();
    if (serverDefinition == null) {
      return false;
    }
    return MobileServerUtils.STORE_SERVER.equals(serverDefinition.getServerType());
  }

  public boolean isThisACentralServer() {
    final MobileServerDefinition serverDefinition = getThisServerDefinition();
    if (serverDefinition == null) {
      return false;
    }
    return MobileServerUtils.MAIN_SERVER.equals(serverDefinition.getServerType());
  }

  /**
   * Reset the cached server definition after the transaction completed if anything changed.
   * 
   * @author mtaal
   */
  public static class MobileServerPersistentEventObserver extends EntityPersistenceEventObserver {
    private static Entity[] entities = {
        ModelProvider.getInstance().getEntity(MobileServerDefinition.ENTITY_NAME),
        ModelProvider.getInstance().getEntity(MobileServerOrganization.ENTITY_NAME),
        ModelProvider.getInstance().getEntity(MobileServerService.ENTITY_NAME) };

    protected Logger logger = Logger.getLogger(this.getClass());

    private static ThreadLocal<Boolean> doReset = new ThreadLocal<Boolean>();

    @Override
    protected Entity[] getObservedEntities() {
      return entities;
    }

    public void onSave(@Observes EntityNewEvent event) {
      onAction(event);
    }

    public void onUpdate(@Observes EntityUpdateEvent event) {
      onAction(event);
    }

    public void onDelete(@Observes EntityNewEvent event) {
      onAction(event);
    }

    public void onAction(EntityPersistenceEvent event) {
      if (!isValidEvent(event)) {
        return;
      }
      doReset.set(true);
    }

    public void onTransactionComplete(@Observes TransactionCompletedEvent event) {
      try {
        if (doReset.get() != null && doReset.get()) {
          MobileServerController.getInstance().resetThisServerDefinitionCache();
        }
      } finally {
        doReset.set(null);
      }
    }
  }
}
