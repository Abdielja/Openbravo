/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.process;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryProcessor.ImportEntryProcessRunnable;

/**
 * Mobile specific import entry processing thread.
 * 
 * @author mtaal
 * 
 */
public abstract class MobileImportEntryProcessorRunnable extends ImportEntryProcessRunnable {

  protected void processEntry(ImportEntry importEntry) throws Exception {
    OBContext.setAdminMode(false);
    String importEntryId = null;
    String json = null;
    try {
      importEntryId = importEntry.getId();
      json = importEntry.getJsonInfo();
    } finally {
      OBContext.restorePreviousMode();
    }
    final DataSynchronizationProcess dataSynchronization = WeldUtils
        .getInstanceFromStaticBeanManager(getDataSynchronizationClass());
    dataSynchronization.setImportEntryId(importEntryId);
    dataSynchronization.exec(new JSONObject(json));
    if (SessionHandler.isSessionHandlerPresent()) {
      OBDal.getInstance().commitAndClose();
    }
  }

  protected abstract Class<? extends DataSynchronizationProcess> getDataSynchronizationClass();
}
