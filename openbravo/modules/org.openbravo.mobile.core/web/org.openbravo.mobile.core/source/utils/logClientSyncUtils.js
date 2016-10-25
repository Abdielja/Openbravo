/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, console*/

(function () {

  OB.UTIL = window.OB.UTIL || {};

  OB.UTIL.processLogClientAll = function () {
    // if the application is busy, delay flushing the log to the server
    if (!OB.MobileApp.model.loadingErrorsActions.executed) {
      if (OB.MobileApp.model.get('isLoggingIn') || !OB.UTIL.SynchronizationHelper.isSynchronized()) {
        OB.debug("LogClient flushing delayed");
        return false;
      }
    }

    // Processes log client
    if (OB.MobileApp.model.get('connectedToERP')) {
      // batch the queue. be sure that we pick manageable numbers
      var criteria = {
        _limit: 200
      };
      OB.Dal.find(OB.Model.LogClient, criteria, function (logClientsNotProcessed) {
        if (!logClientsNotProcessed || logClientsNotProcessed.length === 0) {
          return;
        }
        OB.UTIL.processLogClients(logClientsNotProcessed, null, null);
      });
    }
  };

  OB.UTIL.processLogClientClass = 'org.openbravo.mobile.core.utils.LogClientLoader';
  OB.UTIL.processLogClients = function (logClients, successCallback, errorCallback) {

    // do not flush if a session is not active
    if (!OB.MobileApp.model.isUserAuthenticated()) {
      OB.UTIL.Debug.execute(function () {
        console.warn("'OB.UTIL.processLogClients' cannot be executed because it requires an autheticated user");
      });
      return;
    }

    // serialize the messages
    var logClientsToJson = [];
    logClients.each(function (logClient) {
      var toJson = logClient.serializeToJSON();
      if (toJson && toJson.msg) {
        if (toJson.msg.toLowerCase().indexOf('logclient') < 0) {
          logClientsToJson.push({
            id: toJson.id,
            json: toJson.json
          });
          return;
        }
        console.warn("Log client failed to log:\n" + toJson.msg);
        OB.Dal.remove(logClient, null, function (tx, err) {
          OB.UTIL.showError(err);
        });
      }
    });

    if (logClientsToJson.length === 0) {
      return;
    }

    // send the messages to the server
    this.proc = new OB.DS.Process(OB.UTIL.processLogClientClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      this.proc.exec({
        logclient: logClientsToJson
      }, function (data) {
        if (data && data.exception) {
          if (errorCallback) {
            errorCallback();
          }
        } else {
          logClients.each(function (logClient) {
            OB.Dal.remove(logClient, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (successCallback) {
            successCallback();
          }
        }
      }, null, null, 20000);
    }

  };
}());