/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.authenticate;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.authentication.AuthenticationException;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.authentication.basic.DefaultAuthenticationManager;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.LoginUtils.RoleDefaults;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.mobile.core.authenticate.MobileAuthenticationKeyUtils.AuthenticationToken;

/**
 * An {@link AuthenticationManager} which can work with encrypted keys.
 * 
 * @author mtaal
 */
public class MobileKeyAuthenticationManager extends DefaultAuthenticationManager {

  private static final Logger logger = Logger.getLogger(MobileKeyAuthenticationManager.class);

  @Override
  protected String doAuthenticate(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, ServletException, IOException {

    final String sUserId = (String) request.getSession().getAttribute("#Authenticated_user");
    if (!StringUtils.isEmpty(sUserId)) {
      return sUserId;
    }

    final String token = request
        .getParameter(MobileAuthenticationKeyUtils.AUTHENTICATION_TOKEN_PARAM);
    final String clientId = request
        .getParameter(MobileAuthenticationKeyUtils.AUTHENTICATION_CLIENT_PARAM);

    if (token == null || clientId == null) {
      return super.doAuthenticate(request, response);
    }

    try {
      final AuthenticationToken authenticationToken = MobileAuthenticationKeyUtils.decrypt(
          clientId, token);

      // code from DefaultAuthenticationManager
      // Using the Servlet API instead of vars.setSessionValue to avoid breaking code
      // vars.setSessionValue always transform the key to upper-case
      HttpSession session = request.getSession(true);
      session.setAttribute("#Authenticated_user", authenticationToken.getUserId());
      session.setAttribute("#Authenticated_by_Token", "true");

      // set the client/org/role
      final VariablesSecureApp vars = new VariablesSecureApp(request);
      vars.setSessionValue("#AD_Client_ID", authenticationToken.getClientId());
      vars.setSessionValue("#AD_Org_ID", authenticationToken.getOrgId());
      vars.setSessionValue("#AD_Role_ID", authenticationToken.getRoleId());
      vars.setSessionValue("POSTerminal", authenticationToken.getTerminalId());

      vars.setSessionValue("#AD_SESSION_ID", session.getId());
      vars.setSessionValue("#LogginIn", "N");

      ConfigParameters globalParameters = ConfigParameters
          .retrieveFrom(session.getServletContext());

      RoleDefaults defaults = LoginUtils.getLoginDefaults(authenticationToken.getUserId(),
          authenticationToken.getRoleId(), conn);

      String whId = defaults.warehouse;
      if (authenticationToken.getOrgId() != defaults.org) {
        whId = LoginUtils.getDefaultWarehouse(conn, authenticationToken.getClientId(),
            authenticationToken.getOrgId(), authenticationToken.getRoleId());
      }
      LoginUtils.fillSessionArguments(conn, vars, authenticationToken.getUserId(),
          LoginUtils.getDefaultLanguage(conn, authenticationToken.getUserId()),
          LoginUtils.isDefaultRtl(conn, authenticationToken.getUserId()),
          authenticationToken.getRoleId(), authenticationToken.getClientId(),
          authenticationToken.getOrgId(), whId);
      LoginUtils.readNumberFormat(vars, globalParameters.getFormatPath());
      LoginUtils.saveLoginBD(request, vars, "0", "0");

      return authenticationToken.getUserId();
    } catch (Throwable t) {
      logger.info("Authentication login failed, continueing with standard login approach", t);
      return super.doAuthenticate(request, response);
    }
  }
}
