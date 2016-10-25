/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.servercontroller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.MobileServerDefinition;
import org.openbravo.mobile.core.authenticate.MobileAuthenticationKeyUtils;

/**
 * @author mtaal
 */
public class MobileServerUtils {

  public static final String MOBILE_SERVER_KEY = "mobile.server.key";
  public static final String MAIN_SERVER = "MAIN";
  public static final String STORE_SERVER = "STORE";
  public static final String OBWSPATH = "/org.openbravo.mobile.core.service.jsonrest/";

  private static Boolean mobileServerControllerEnabled = null;

  /**
   * Return true if the mobile server controller logic is enabled by the corresponding preference.
   * false otherwise.
   */
  public static boolean isMobileServerControllerEnabled() {
    if (mobileServerControllerEnabled != null) {
      return mobileServerControllerEnabled;
    }
    return setMobileServerControllerEnabled();
  }

  private static synchronized boolean setMobileServerControllerEnabled() {
    try {
      OBContext.setAdminMode(false);
      mobileServerControllerEnabled = "Y".equals(Preferences.getPreferenceValue(
          "OBMOBC_MobileStoreServerControllingEnabled", true, OBContext.getOBContext()
              .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
              .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException ignore) {
      mobileServerControllerEnabled = false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return mobileServerControllerEnabled;
  }

  /**
   * Get the server status from the json and update the corresponding server with it.
   */
  public static void updateServerStatusFromJSON(JSONObject json) {
    try {
      OBContext.setAdminMode(false);
      // Update servers status.
      if (json.has("mobileServerKey") && json.has("serverStatus")) {
        OBCriteria<MobileServerDefinition> serverQuery = OBDal.getInstance().createCriteria(
            MobileServerDefinition.class);
        serverQuery.add(Restrictions.eq(MobileServerDefinition.PROPERTY_MOBILESERVERKEY,
            json.get("mobileServerKey")));
        MobileServerDefinition server = (MobileServerDefinition) serverQuery.uniqueResult();
        if (server != null) {
          server.setStatus((String) json.get("serverStatus"));
          OBDal.getInstance().save(server);
        }
      }
    } catch (JSONException e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static boolean isOpenbravoServer(MobileServerDefinition serverDef) {
    return (serverDef.getServerType().equals(MAIN_SERVER) || serverDef.getServerType().equals(
        STORE_SERVER));
  }

  public static String getAuthenticationQueryParams() {
    final OBContext obContext = OBContext.getOBContext();
    return getAuthenticationQueryParams(obContext.getCurrentClient().getId(), obContext
        .getCurrentOrganization().getId(), obContext.getRole().getId(), obContext.getUser().getId());
  }

  public static String getAuthenticationQueryParams(String clientId, String orgId, String roleId,
      String userId) {
    try {
      String authenticationClient = URLEncoder.encode(clientId, "UTF-8");
      String authenticationToken = URLEncoder.encode(MobileAuthenticationKeyUtils
          .getEncryptedAuthenticationToken(clientId, orgId, userId, roleId, "-1"), "UTF-8");
      return "authenticationClient=" + authenticationClient + "&authenticationToken="
          + authenticationToken;
    } catch (UnsupportedEncodingException e) {
      throw new OBException(e);
    }
  }

}
