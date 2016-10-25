/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, enyo, $, console */

(function () {
  OB.DS = window.OB.DS || {};

  OB.DS.MAXSIZE = 100;

  function serviceSuccess(inSender, inResponse, callback) {
    if (inResponse._entityname) {
      callback([inResponse]);
    } else {
      var response = inResponse.response;

      if (!response) {
        // the response is empty
        response = {};
        response.error = {};
        response.error.message = "Unknown error";
        response.status = 501;
      }

      // if the context has changed, lock the terminal
      if (response.contextInfo && OB.MobileApp.model.get('context')) {
        OB.UTIL.checkContextChange(OB.MobileApp.model.get('context'), response.contextInfo, function () {
          //Source version will be checked to ensure that Web POS is updated. Only check for requests from code loaded server.
          if (response && response.sourceVersion && inSender.url.indexOf(document.location.host) !== -1) {
            OB.UTIL.checkSourceVersion(response.sourceVersion, false);
          }
        });
      }

      var status = response.status;

      if (status === 0) {
        callback(response.data, response.message, response.lastUpdated);
        return;
      }

      // an error has been sent in the successful response
      OB.error("serviceSuccess error: status: " + response.status + ((response.error && response.error.message) ? (", error.message: " + response.error.message) : ""));

      // generic error message
      var exception = {
        message: 'Unknown error',
        status: response
      };

      if (response.errors) {
        if (OB.MobileApp.model.get('isLoggingIn')) {
          OB.MobileApp.model.set("datasourceLoadFailed", true);
        }
        exception.message = response.errors.id;
      }

      if (response.error && response.error.message) {
        if (OB.MobileApp.model.get('isLoggingIn') && !response.error.invalidPermission) {
          OB.MobileApp.model.set("datasourceLoadFailed", true);
        }
        exception.invalidPermission = response.error.invalidPermission;
        exception.message = response.error.message;
      }

      // argument checks
      OB.UTIL.Debug.execute(function () {
        if (!exception) {
          console.error("The exception message is missing");
        }
      });

      callback({
        exception: exception
      });
    }
  }

  function serviceError(inResponse, callback, callbackError) {
    if (OB.MobileApp.model.get('isLoggingIn')) {
      OB.MobileApp.model.set("datasourceLoadFailed", true);
    }
    if (callbackError) {
      callbackError({
        exception: {
          message: OB.I18N.getLabel('OBMOBC_MsgApplicationServerNotAvailable'),
          status: inResponse
        }
      });
    } else {
      callback({
        exception: {
          message: OB.I18N.getLabel('OBMOBC_MsgApplicationServerNotAvailable'),
          status: inResponse
        }
      });
    }
  }

  OB.DS.servicePOSTRequests = [];
  function servicePOST(source, dataparams, callback, callbackError, async, timeout) {
    OB.DS.servicePOSTRequests.push(source);
    var rr;
    if (async !== false) {
      async = true;
    }
    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.mobile.core.service.jsonrest/' + source,
      cacheBust: false,
      sync: !async,
      timeout: timeout,
      method: 'POST',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      ignoreForConnectionStatus: dataparams.parameters && dataparams.parameters.ignoreForConnectionStatus ? dataparams.parameters.ignoreForConnectionStatus : false,
      data: JSON.stringify(dataparams),
      success: function (inSender, inResponse) {
        OB.DS.servicePOSTRequests.pop(source);
        if (this.processHasFailed) {
          return;
        }
        serviceSuccess(inSender, inResponse, callback);
      },
      fail: function (inSender, inResponse) {
        OB.DS.servicePOSTRequests.pop(source);
        OB.UTIL.Debug.execute(function () {
          // show an error while in debug mode to help debugging and testing
          console.error("servicePOST error: " + inSender + ", source: " + source);
        });
        this.processHasFailed = true;
        inResponse = inResponse || {};
        if (inSender && inSender === "timeout") {
          inResponse.timeout = true;
        }
        serviceError(inResponse, callback, callbackError);
      }
    });
    rr = new OB.RR.Request({
      ajaxRequest: ajaxRequest
    });
    rr.exec(source);
  }

  OB.DS.serviceGETRequests = [];
  function serviceGET(source, dataparams, callback, callbackError, async, timeout) {
    OB.DS.serviceGETRequests.push(source);
    var rr;
    var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('serviceGET ' + source);
    if (async !== false) {
      async = true;
    }
    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.mobile.core.service.jsonrest/' + source + '/' + encodeURI(JSON.stringify(dataparams)),
      cacheBust: false,
      sync: !async,
      method: 'GET',
      handleAs: 'json',
      timeout: timeout,
      ignoreForConnectionStatus: dataparams.parameters && dataparams.parameters.ignoreForConnectionStatus ? dataparams.parameters.ignoreForConnectionStatus : false,
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        OB.DS.serviceGETRequests.pop(source);
        OB.UTIL.SynchronizationHelper.finished(synchId, 'serviceGET');
        serviceSuccess(inSender, inResponse, callback);
      },
      fail: function (inSender, inResponse) {
        OB.DS.serviceGETRequests.pop(source);
        OB.UTIL.SynchronizationHelper.finished(synchId, 'serviceGET');
        OB.UTIL.Debug.execute(function () {
          // show an error while in debug mode to help debugging and testing
          if (inSender && inSender === 401) {
            console.warn("serviceGET error: " + inSender + ", source: " + source);
          } else {
            console.error("serviceGET error: " + inSender + ", source: " + source);
          }
        });
        serviceError(inResponse, callback, callbackError);
      }
    });
    rr = new OB.RR.Request({
      ajaxRequest: ajaxRequest
    });
    rr.exec(source);
  }

  // Process object
  OB.DS.Process = function (source) {
    this.source = source;
  };

  OB.DS.Process.prototype.exec = function (params, callback, callbackError, async, timeout) {
    var data = {},
        i, attr;

    for (attr in params) {
      if (params.hasOwnProperty(attr)) {
        data[attr] = params[attr];
      }
    }

    if (OB.DS.commonParams) {
      for (i in OB.DS.commonParams) {
        if (OB.DS.commonParams.hasOwnProperty(i)) {
          data[i] = OB.DS.commonParams[i];
        }
      }
    }

    data.appName = OB.MobileApp.model.get('appName') || 'OBMOBC';

    servicePOST(this.source, data, callback, callbackError, async, timeout);
  };

  // Source object
  OB.DS.Request = function (source, lastUpdated) {
    this.model = source && source.prototype && source.prototype.modelName && source; // we're using a Backbone.Model as source
    this.source = (this.model && this.model.prototype.source) || source; // we're using a plain String as source
    OB.UTIL.Debug.execute(function () {
      if (!this.source) {
        console.error("A Request must have a source");
      }
    }, this);

    this.lastUpdated = lastUpdated;
  };

  OB.DS.Request.prototype.exec = function (params, callback, callbackError, async, timeout) {
    var p, i;
    var data = {};

    if (params) {
      if (params._limit) {
        data._limit = params._limit;
        delete params._limit;
      }
      if (params._offset || params._offset === 0) {
        data._offset = params._offset;
        delete params._offset;
      }
      p = {};
      for (i in params) {
        if (params.hasOwnProperty(i)) {
          if (typeof params[i] === 'string') {
            p[i] = {
              value: params[i],
              type: 'string'
            };
          } else if (typeof params[i] === 'number') {
            if (params[i] === Math.round(params[i])) {
              p[i] = {
                value: params[i],
                type: 'long'
              };
            } else {
              p[i] = {
                value: params[i],
                type: 'bigdecimal'
              };
            }
          } else if (typeof params[i] === 'boolean') {
            p[i] = {
              value: params[i],
              type: 'boolean'
            };
          } else {
            p[i] = params[i];
          }
        }
      }
      data.parameters = p;
    }

    if (OB.DS.commonParams) {
      for (i in OB.DS.commonParams) {
        if (OB.DS.commonParams.hasOwnProperty(i)) {
          data[i] = OB.DS.commonParams[i];
        }
      }
    }
    if (this.lastUpdated) {
      data.lastUpdated = this.lastUpdated;
    }

    data.appName = OB.MobileApp.model.get('appName') || 'OBMOBC';

    serviceGET(this.source, data, callback, callbackError, async, timeout);
  };

  function check(elem, filter) {
    var p;

    for (p in filter) {
      if (filter.hasOwnProperty(p)) {
        if (typeof (filter[p]) === 'object') {
          return check(elem[p], filter[p]);
        } else {
          if (filter[p].substring(0, 2) === '%i') {
            if (!new RegExp(filter[p].substring(2), 'i').test(elem[p])) {
              return false;
            }
          } else if (filter[p].substring(0, 2) === '%') {
            if (!new RegExp(filter[p].substring(2)).test(elem[p])) {
              return false;
            }
          } else if (filter[p] !== elem[p]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  function findInData(data, filter) {
    var i, max;

    if ($.isEmptyObject(filter)) {
      return {
        exception: 'filter not defined'
      };
    } else {
      for (i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          return data[i];
        }
      }
      return null;
    }
  }

  function execInData(data, filter, filterfunction) {
    var newdata, info, i, max, f, item;

    if ($.isEmptyObject(filter) && !filterfunction) {
      return {
        data: data.slice(0, OB.DS.MAXSIZE),
        info: (data.length > OB.DS.MAXSIZE ? 'OBMOBC_DataMaxReached' : null)
      };
    } else {
      f = filterfunction ||
      function (item) {
        return item;
      };
      newdata = [];
      info = null;
      for (i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          item = f(data[i]);
          if (item) {
            if (newdata.length >= OB.DS.MAXSIZE) {
              info = 'OBMOBC_DataMaxReached';
              break;
            }
            newdata.push(data[i]);
          }
        }
      }
      return {
        data: newdata,
        info: info
      };
    }
  }

  // DataSource objects
  // OFFLINE GOES HERE
  OB.DS.DataSource = function (request) {
    this.request = request;
    this.cache = null;
  };
  _.extend(OB.DS.DataSource.prototype, Backbone.Events);

  OB.DS.DataSource.prototype.load = function (params, incremental) {
    var me = this,
        handleError, handleIncrementalRequest, dataLoaded = 0;
    handleError = function () {
      if (arguments[1] && arguments[1].message && me.request.model.prototype.modelName) {
        OB.error("Table '" + me.request.model.prototype.modelName + "' load error: " + (arguments[1] ? ("(" + arguments[1].code + ") " + arguments[1].message) : arguments));
      } else {
        OB.error("OB.DS.DataSource.prototype.load:", arguments, me.request.model.prototype);
      }
    };
    handleIncrementalRequest = function (limit, offset, params, incremental) {
      params = params || {};
      params._limit = limit;
      params._offset = offset;
      if (offset === 0) {
        window.localStorage.setItem('requestTimestamp' + me.request.model.prototype.modelName, (new Date()).getTime());
        OB.UTIL.showLoadingMessage(OB.I18N.getLabel('OBMOBC_LoadingMessageModel', [me.request.model.prototype.modelName]));
      } else {
        OB.UTIL.showLoadingMessage(OB.I18N.getLabel('OBMOBC_LoadingMessageModelPage', [me.request.model.prototype.modelName, offset / limit + 1]));
      }
      me.request.exec(params, function (data, message, lastUpdated) {
        function success() {
          dataLoaded += data.length;
          if (data.length >= limit) {
            //remove the lastUpdated timestamp while the paged request is being processed to prevent half loaded models
            window.localStorage.removeItem('lastUpdatedTimestamp' + me.request.model.prototype.modelName);
            handleIncrementalRequest(limit, offset + limit, params, incremental);
          } else {
            OB.UTIL.completeLoadingStep();
            if (lastUpdated && data.length > 0) {
              if (window.localStorage.getItem('requestTimestamp' + me.request.model.prototype.modelName) && window.localStorage.getItem('requestTimestamp' + me.request.model.prototype.modelName) !== 'null') {
                window.localStorage.setItem('lastUpdatedTimestamp' + me.request.model.prototype.modelName, window.localStorage.getItem('requestTimestamp' + me.request.model.prototype.modelName));
              } else {
                OB.error('[lastUpdatedTimestamp] ' + 'local storage (lastUpdatedTimestamp' + me.request.model.prototype.modelName + ') attempted to be set as null. Ignored. Current Value: ' + window.localStorage.getItem('lastUpdatedTimestamp' + me.request.model.prototype.modelName));
              }
              window.localStorage.removeItem('requestTimestamp' + me.request.model.prototype.modelName);
            }
            me.trigger('ready');
          }
        }
        if (data.exception) {
          OB.error('Error in datasource: ' + data.exception);
          me.trigger('ready', 'failed');
          return;
        }
        if (me.request.model && me.request.model.prototype.online) {
          me.cache = (me.cache ? me.cache.concat(data) : data);
        }
        if (me.request.model && !me.request.model.prototype.online) {
          if (offset === 0) {
            OB.Dal.initCache(me.request.model, data, success, handleError, incremental);
          } else {
            OB.Dal.insertData(me.request.model, data, success, handleError, incremental, dataLoaded);
          }
        } else {
          success();
        }
      }, function (data) {
        OB.UTIL.Debug.execute(function () {
          if (data && data.exception && data.exception.message) {
            console.error("OB.DS.DataSource.prototype.load : " + data.exception.message);
            return;
          }
        });
        me.trigger('ready', 'failed');
      });
    };

    this.cache = null;

    handleIncrementalRequest(35000, 0, params, incremental);
  };

  OB.DS.DataSource.prototype.find = function (filter, callback) {
    if (this.cache) {
      callback(findInData(this.cache, filter));
    } else {
      this.on('ready', function () {
        callback(findInData(this.cache, filter));
      }, this);
    }
  };

  OB.DS.DataSource.prototype.exec = function (filter, callback) {
    if (this.cache) {
      var result1 = execInData(this.cache, filter);
      callback(result1.data, result1.info);
    } else {
      this.on('ready', function () {
        var result2 = execInData(this.cache, filter);
        callback(result2.data, result2.info);
      }, this);
    }
  };
}());