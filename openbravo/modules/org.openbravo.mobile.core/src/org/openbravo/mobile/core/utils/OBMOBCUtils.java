package org.openbravo.mobile.core.utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.client.application.window.servlet.CalloutHttpServletResponse;
import org.openbravo.mobile.core.authenticate.MobileAuthenticationKeyUtils;
import org.openbravo.mobile.core.process.PropertyByType;
import org.openbravo.service.json.JsonToDataConverter;

/**
 * @author iperdomo
 * 
 */
/**
 * @author openbravo
 *
 */
public class OBMOBCUtils {

  public static final Logger log = Logger.getLogger(OBMOBCUtils.class);

  // TODO: add isAuthenticated to generic AuthenticationManager api
  public static boolean isAuthenticated(HttpServlet servlet, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {
    if (req.getSession(false) == null) {
      return false;
    }
    if (req.getSession(false).getAttribute("#AD_SESSION_ID") == null) {
      return false;
    }
    if (req.getSession().getAttribute("#Authenticated_user") != null) {
      return true;
    }

    // authentication tokens are there, try to login
    final String token = req.getParameter(MobileAuthenticationKeyUtils.AUTHENTICATION_TOKEN_PARAM);
    final String clientId = req
        .getParameter(MobileAuthenticationKeyUtils.AUTHENTICATION_CLIENT_PARAM);
    if (token != null && clientId != null) {
      CalloutHttpServletResponse fakeResponse = new CalloutHttpServletResponse(resp);
      return AuthenticationManager.getAuthenticationManager(servlet)
          .authenticate(req, fakeResponse) != null;
    }

    return false;
  }

  public static void setCORSHeaders(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String origin = request.getHeader("Origin");

    if (origin != null && !origin.equals("")) {
      response.setHeader("Access-Control-Allow-Origin", origin);
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
      response.setHeader("Access-Control-Allow-Headers",
          "Content-Type, Origin, Accept, X-Requested-With, Access-Control-Allow-Credentials");

      response.setHeader("Access-Control-Max-Age", "10000");
    }
  }

  /**
   * This method will return the correct date which should be stored in the database taking into
   * account the client and the server date
   * 
   * @param orgClientDate
   * @param dateOffset
   * @return
   */
  public static Date calculateClientDatetime(String orgClientDate, Long dateOffset) {
    Date serverDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
        PropertyByType.DATETIME,
        orgClientDate.lastIndexOf(".") != -1 ? orgClientDate.subSequence(0,
            orgClientDate.lastIndexOf(".")) : orgClientDate);
    // date is the date in the server timezone, we need to convert it to the date in the
    // original time zone
    Date dateUTC = convertToUTC(serverDate);
    Date clientDate = new Date();
    clientDate.setTime(dateUTC.getTime() - dateOffset * 60 * 1000);
    return clientDate;
  }

  public static Date calculateServerDate(String orgClientDate, Long dateOffset) {
    Date clientDate = calculateClientDatetime(orgClientDate, dateOffset);
    clientDate = stripTime(clientDate);
    return clientDate;
  }

  private static Date convertToUTC(Date localTime) {
    Calendar now = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(localTime);

    int gmtMillisecondOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET));
    calendar.add(Calendar.MILLISECOND, -gmtMillisecondOffset);

    return calendar.getTime();
  }

  public static Date stripTime(Date dateWithTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateWithTime);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Returns a new UUID using the string parameter
   * 
   * @return a new random UUID
   */
  public static String getUUIDbyString(String txt) {
    return UUID.nameUUIDFromBytes(txt.getBytes()).toString().replace("-", "").toUpperCase();
  }
}
