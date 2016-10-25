/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _, localStorage */

(function () {
  OB.RR = window.OB.RR || {};

  OB.RR.HandleResponseCallbacks = [];
  OB.RR.HandleResponseCallbacks.push({
    name: 'ON',
    handleAction: function (callback, ajaxRequest) {
      callback();
    }
  }, {
    name: 'OFF',
    handleAction: function (callback, ajaxRequest) {
      callback();
    }
  }, {
    name: 'TON',
    handleAction: function (callback, ajaxRequest) {
      OB.UTIL.showLoading(true);
    }
  }, {
    name: 'TOFF',
    handleAction: function (callback, ajaxRequest) {
      OB.UTIL.showLoading(true);
    }
  });

  OB.RR.handleResponse = function (keyName, callback, ajaxRequest) {

    var cbk = _.filter(OB.RR.HandleResponseCallbacks, function (callbackFunc) {
      return callbackFunc.name === keyName;
    });
    if (cbk && cbk.length > 0) {
      cbk[0].handleAction(callback, ajaxRequest);
    } else {
      callback();
    }

  };

  //Try to retrieve information from servers following the order of the list
  OB.RR.ServTypeFailover = {
    name: 'Failover',
    callServer: function (online, index, service, ajaxRequest) {
      var servers = OB.RR.RequestRouter.servers,
          server = servers.at(index),
          me = this;

      if (index === servers.length) {
        // call the offline servers if we have more than one server. If origFail is not defined is because we didn't do any call, so we have to try with offline servers
        if (!online || (servers.length === 1 && ajaxRequest.origFail)) {
          if (ajaxRequest.origFail) {
            ajaxRequest.origFail();
          }
        } else {
          this.callServer(false, 0, service, ajaxRequest);
        }
        return true;
      }

      if (online === Boolean(server.get('online')) && (server.get('allServices') || _.filter(server.get('services'), function (srvc) {
        return srvc === service.get('name');
      }).length > 0)) {
        if (!ajaxRequest.origUrl) {
          ajaxRequest.origUrl = ajaxRequest.url;
        }
        if (server.get('address')) {
          ajaxRequest.url = ajaxRequest.origUrl.replace('../../', server.get('address'));
        }
        if (!ajaxRequest.origFail) {
          ajaxRequest.origFail = ajaxRequest.fail;

        }
        ajaxRequest.fail = function (inSender, inResponse) {
          if (inSender === 401) {
            //The server is online but need to relogin because token has expired or have been deleted
            // so reset the online status directly
            server.set('online', true);
            me.handle401(server);
          } else {
            server.changeOnlineStatus(false, ajaxRequest.ignoreForConnectionStatus);
            me.callServer(online, index + 1, service, ajaxRequest);
          }
        };
        if (!ajaxRequest.origSuccess) {
          ajaxRequest.origSuccess = ajaxRequest.success;

        }
        ajaxRequest.success = function (inSender, inResponse) {
          var callback = function () {
              if (inResponse.authenticationClient && inResponse.authenticationToken) {
                localStorage.authenticationClient = encodeURIComponent(inResponse.authenticationClient);
                localStorage.authenticationToken = encodeURIComponent(inResponse.authenticationToken);
              }
              if (!ajaxRequest.done) {
                ajaxRequest.done = true;
                ajaxRequest.origSuccess(inSender, inResponse);
              }
              // server is back
              server.changeOnlineStatus(true, ajaxRequest.ignoreForConnectionStatus);
              };
          if (inResponse && inResponse.response && inResponse.response.serverStatusSignal) {
            OB.RR.handleResponse(inResponse.response.serverStatusSignal, callback, ajaxRequest);
          } else {
            callback();
          }
        };
        if (localStorage.authenticationClient && localStorage.authenticationToken) {
          if (ajaxRequest.url.indexOf('?') === -1) {
            ajaxRequest.url = ajaxRequest.url + '?';
          } else {
            ajaxRequest.url = ajaxRequest.url + '&';
          }
          ajaxRequest.url = ajaxRequest.url + 'authenticationClient=' + localStorage.authenticationClient + '&authenticationToken=' + localStorage.authenticationToken;
        }

        ajaxRequest.xhrFields = {
          'withCredentials': true
        };

        // do the real send with a new ajaxRequest object, otherwise 
        // in case of timeouts Enyo get's confused and starts to call the fail method
        // set on the shared ajaxRequest object which is passed around.
        new enyo.Ajax(ajaxRequest).go(ajaxRequest.data).response('success').error('fail');
      } else {
        me.callServer(online, index + 1, service, ajaxRequest);
      }
    },
    handle401: function (server) {
      if (_.find(OB.RR.RequestRouter.servers.models, function (server) {
        return server.get('online') && (server.get('allServices') || _.find(server.get('services'), function (service) {
          return service === "org.openbravo.retail.posterminal.POSLoginHandler";
        }));
      })) {
        //do relogin
        OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Online'), OB.I18N.getLabel('OBMOBC_OnlineConnectionHasReturned'), [{
          label: OB.I18N.getLabel('OBMOBC_LblLoginAgain'),
          action: function () {
            OB.MobileApp.model.lock();
            OB.UTIL.showLoading(true);
          }
        }], {
          autoDismiss: false,
          onHideFunction: function () {
            OB.MobileApp.model.lock();
            OB.UTIL.showLoading(true);
          }
        });
      }
    },
    implementation: function (service, ajaxRequest) {
      this.callServer(true, 0, service, ajaxRequest);
    }
  };

  // Send the request to all servers supporting it, ignore failures
  OB.RR.ServTypeBroadcast = {
    name: 'Broadcast',
    callServer: function (server, ajaxRequest) {
      if (!ajaxRequest.origUrl) {
        ajaxRequest.origUrl = ajaxRequest.url;
      }
      if (server.get('address')) {
        ajaxRequest.url = ajaxRequest.origUrl.replace('../../', server.get('address'));
      }

      // send some authentication info
      if (localStorage.authenticationClient && localStorage.authenticationToken) {
        if (ajaxRequest.url.indexOf('?') === -1) {
          ajaxRequest.url = ajaxRequest.url + '?';
        } else {
          ajaxRequest.url = ajaxRequest.url + '&';
        }
        ajaxRequest.url = ajaxRequest.url + 'authenticationClient=' + localStorage.authenticationClient + '&authenticationToken=' + localStorage.authenticationToken;
      }

      ajaxRequest.xhrFields = {
        'withCredentials': true
      };

      // do the real send with a new ajaxRequest object, otherwise 
      // in case of timeouts Enyo get's confused and starts to call the fail method
      // set on the shared ajaxRequest object which is passed around.
      new enyo.Ajax(ajaxRequest).go(ajaxRequest.data).response('success').error('fail');

      // do nothing on fail/success for next requests
      ajaxRequest.fail = function (inSender, inResponse) {};
      ajaxRequest.success = function (inSender, inResponse) {};
    },

    implementation: function (service, ajaxRequest) {
      var me = this;
      var servers = _.filter(OB.RR.RequestRouter.servers.models, function (server) {
        return server.get('allServices') || _.filter(server.get('services'), function (srvc) {
          return srvc === service.get('name');
        }).length > 0;
      });
      _.each(servers, function (server) {
        me.callServer(server, ajaxRequest);
      });

    }
  };

  // Send information to all servers to save it everywhere, failed messages are retried
  OB.RR.ServTypeTransaction = {
    name: 'Transaction',

    createRequest: function (server, message, pendingMessages) {
      var me = this;
      return {
        url: message.get('url'),
        cacheBust: false,
        sync: false,
        timeout: 610000,
        method: 'POST',
        handleAs: 'json',
        contentType: 'application/json;charset=utf-8',
        ignoreForConnectionStatus: false,
        data: message.get('messageObj'),
        success: function (inSender, inResponse) {
          var callback = function () {
              // get some authentication stuff  
              if (inResponse.authenticationClient && inResponse.authenticationToken) {
                localStorage.authenticationClient = encodeURIComponent(JSON.stringify(inResponse.authenticationClient));
                localStorage.authenticationToken = encodeURIComponent(JSON.stringify(inResponse.authenticationToken));
              }

              // message was successfull, remove it, server is online
              server.changeOnlineStatus(true, this.ignoreForConnectionStatus);

              OB.Dal.remove(message, function () {
                // a main server, tell the popup that the message has been synced
                if (Boolean(server.get('mainServer'))) {
                  OB.UTIL.SynchronizationHelper.eachModelSynchronized();
                }

                // if the context has changed, lock the terminal
                if (inResponse.response && inResponse.response.contextInfo && OB.MobileApp.model.get('context')) {
                  OB.UTIL.checkContextChange(OB.MobileApp.model.get('context'), inResponse.response.contextInfo, function () {
                    //no need to check source version,allow different versions
                    //                    if (inResponse.response && inResponse.response.sourceVersion) {
                    //                      OB.UTIL.checkSourceVersion(inResponse.response.sourceVersion, false);
                    //                    }
                  });
                }

                // process the remaining messages
                me.processMessages(server, pendingMessages);

              }, function (error) {
                OB.error(arguments);
              });
              };

          if (inResponse && inResponse.response && inResponse.response.serverStatusSignal) {
            OB.RR.handleResponse(inResponse.response.serverStatusSignal, callback, this);
          } else {
            callback();
          }
        },
        fail: function (inSender, inResponse) {
          // save the message in error status
          message.set('status', 'failure');
          OB.Dal.save(message, function () {

            me.updateSyncIcon();

            if (inSender === 401) {
              //The server is online but need to relogin because token has expired or have been deleted
              // so reset the online status directly
              server.set('online', true);
              me.handle401(server);
            } else {
              server.changeOnlineStatus(false, this.ignoreForConnectionStatus);
            }

            // stop, don't sync more messages
            server.sendingMessages = false;
          }, function () {
            OB.error(arguments);
          });
        }
      };
    },

    handle401: function (server) {
      if (_.find(OB.RR.RequestRouter.servers.models, function (server) {
        return server.get('online') && (server.get('allServices') || _.find(server.get('services'), function (service) {
          return service === "org.openbravo.retail.posterminal.POSLoginHandler";
        }));
      })) {
        //do relogin
        OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Online'), OB.I18N.getLabel('OBMOBC_OnlineConnectionHasReturned'), [{
          label: OB.I18N.getLabel('OBMOBC_LblLoginAgain'),
          action: function () {
            OB.MobileApp.model.lock();
            OB.UTIL.showLoading(true);
          }
        }], {
          autoDismiss: false,
          onHideFunction: function () {
            OB.MobileApp.model.lock();
            OB.UTIL.showLoading(true);
          }
        });
      }
    },

    updateSyncIcon: function () {
      // update the sync icon/messages
      OB.Dal.find(OB.Model.Message, {
        'mainServer': 'true'
      }, function (msgs) {
        if (_.reduce(msgs.models, function (val, msg) {
          return val || (Boolean(msg.get('mainServer')) && msg.get('status') === 'failure');
        }, false)) {
          OB.UTIL.SynchronizationHelper.modelsNotSynchronized();
        } else if (msgs.length > 0 && !OB.UTIL.SynchronizationHelper.isModelsSynchronizing()) {
          OB.UTIL.SynchronizationHelper.modelsSynchronizing();
        } else if (msgs.length === 0 && !OB.UTIL.SynchronizationHelper.isModelsSynchronized()) {
          OB.UTIL.SynchronizationHelper.modelsSynchronized();
        }
      }, function () {
        OB.error(arguments);
      });
    },

    processMessages: function (server, pendingMessages) {
      var me = this,
          message, ajaxRequest, request;

      // nothing more to process, restart the sending
      // for a final check if new messages arrived
      if (pendingMessages.length === 0) {
        // reset the flag as we are done  
        server.sendingMessages = false;
        this.sendMessages(server);
        return;
      }

      // pop the first message to process it
      message = pendingMessages.shift();
      request = this.createRequest(server, message, pendingMessages);

      // send out the request
      // the pendingMessages are further processed in the request fail/success
      // handlers created in the createRequest call above
      ajaxRequest = new enyo.Ajax(request);
      if (server.get('address')) {
        ajaxRequest.url = ajaxRequest.url.replace('../../', server.get('address'));
      }
      if (localStorage.authenticationClient && localStorage.authenticationToken) {
        if (ajaxRequest.url.indexOf('?') === -1) {
          ajaxRequest.url = ajaxRequest.url + '?';
        } else {
          ajaxRequest.url = ajaxRequest.url + '&';
        }
        ajaxRequest.url = ajaxRequest.url + 'authenticationClient=' + localStorage.authenticationClient + '&authenticationToken=' + localStorage.authenticationToken;
      }

      ajaxRequest.xhrFields = {
        'withCredentials': true
      };

      ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
    },

    sendMessages: function (server) {
      var me = this;

      // not yet initialized
      if (!OB.Model.Message || !OB.Model.Message.areTablesCreated) {
        return;
      }

      me.updateSyncIcon();

      // already doing this stop here
      if (server.sendingMessages) {
        return;
      }
      server.sendingMessages = true;

      OB.Dal.find(OB.Model.Message, {
        'server': server.get('name'),
        '_orderByClause': 'time asc'
      }, function (messages) {
        if (messages.length === 0) {
          server.sendingMessages = false;
        } else {
          me.processMessages(server, messages);
        }
      }, function () {
        OB.error(arguments);
      });
    },

    implementation: function (service, ajaxRequest) {
      var successExecuted = false;
      // only send the message to the servers which supply the service
      var servers = _.filter(OB.RR.RequestRouter.servers.models, function (server) {
        return server.get('allServices') || _.filter(server.get('services'), function (srvc) {
          return srvc === service.get('name');
        }).length > 0;
      }),
          me = this;
      _.each(servers, function (server) {

        var successCallback = function () {
            if (!successExecuted && Boolean(server.get('mainServer'))) {
              // execute the success callbacks
              new enyo.Ajax(ajaxRequest).success(null, {
                response: {
                  contextInfo: null,
                  result: '0',
                  status: 0
                }
              });
              successExecuted = true;
            }
            me.sendMessages(server);
            };

        if (!ajaxRequest.data || !JSON.parse(ajaxRequest.data).data) {
          var errorMessage = OB.I18N.getLabel('OBMOBC_WrongMobileService', [ajaxRequest.url]);
          OB.error(errorMessage);

          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_LblWarning'), OB.I18N.getLabel('OBMOBC_WrongMobileService', [ajaxRequest.url]), [{
            isConfirmButton: true,
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function () {
              OB.MobileApp.model.lock();
              return true;
            }
          }], {
            onHideFunction: function () {
              OB.MobileApp.model.lock();
              return true;
            }
          });
          return;
        }
        var parsedData = JSON.parse(ajaxRequest.data);
        if (parsedData.data.length > 0) {
          var message = new OB.Model.Message();
          message.set('server', server.get('name'));
          message.set('mainServer', server.get('mainServer'));
          message.set('url', ajaxRequest.url);
          message.set('time', new Date().getTime());
          message.set('messageObj', ajaxRequest.data);
          if (!Boolean(server.get('online'))) {
            message.set('status', 'failure');
          }
          OB.Dal.save(message, successCallback, function () {
            OB.error(arguments);
          });
        } else {
          successCallback();
        }
      });
    }
  };

  //Check if offline servers are back
  OB.RR.ServTypePing = {
    name: 'Ping',
    implementation: function (service, ajaxRequest) {
      var servers = OB.RR.RequestRouter.servers,
          me = this,
          offlineServers = _.filter(servers.models, function (server) {
          return !server.get('online');
        });
      _.each(offlineServers, function (server) {
        if (!ajaxRequest.origUrl) {
          ajaxRequest.origUrl = ajaxRequest.url;
        }
        if (server.get('address')) {
          ajaxRequest.url = ajaxRequest.origUrl.replace('../../', server.get('address'));
        }
        if (!ajaxRequest.origFail) {
          ajaxRequest.origFail = ajaxRequest.fail;

        }
        ajaxRequest.fail = function (inSender, inResponse) {
          server.changeOnlineStatus(false, ajaxRequest.ignoreForConnectionStatus);
          if (Boolean(server.get('mainServer'))) {
            ajaxRequest.origFail(inSender, inResponse);
          }
        };

        if (!ajaxRequest.origSuccess) {
          ajaxRequest.origSuccess = ajaxRequest.success;
        }
        ajaxRequest.success = function (inSender, inResponse) {
          if (inResponse.authenticationClient && inResponse.authenticationToken) {
            localStorage.authenticationClient = encodeURIComponent(JSON.stringify(inResponse.authenticationClient));
            localStorage.authenticationToken = encodeURIComponent(JSON.stringify(inResponse.authenticationToken));
          }

          if (Boolean(server.get('mainServer')) && !ajaxRequest.done) {
            ajaxRequest.done = true;
            ajaxRequest.origSuccess(inSender, inResponse);
          }

          server.changeOnlineStatus(true, ajaxRequest.ignoreForConnectionStatus);
        };
        if (localStorage.authenticationClient && localStorage.authenticationToken) {
          if (ajaxRequest.url.indexOf('?') === -1) {
            ajaxRequest.url = ajaxRequest.url + '?';
          } else {
            ajaxRequest.url = ajaxRequest.url + '&';
          }
          ajaxRequest.url = ajaxRequest.url + 'authenticationClient=' + localStorage.authenticationClient + '&authenticationToken=' + localStorage.authenticationToken;
        }

        ajaxRequest.xhrFields = {
          'withCredentials': true
        };

        ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
      });
    }
  };

  OB.RR.Service = Backbone.Model.extend({
    defaults: {
      name: null,
      type: null
    },
    initialize: function (attributes) {
      if (attributes && attributes.name) {
        this.set('name', attributes.name);
        switch (attributes.type) {
        case 'Failover':
          this.set('type', OB.RR.ServTypeFailover);
          break;
        case 'Transaction':
          this.set('type', OB.RR.ServTypeTransaction);
          break;
        case 'Broadcast':
          this.set('type', OB.RR.ServTypeBroadcast);
          break;
        case 'Ping':
          this.set('type', OB.RR.ServTypePing);
          break;
        default:
          this.set('type', OB.RR.ServTypeFailover);
          break;
        }
      }
    }
  });

  OB.RR.Server = Backbone.Model.extend({
    defaults: {
      name: null,
      address: null,
      online: true,
      mainServer: false,
      allServices: false,
      services: []
    },

    changeOnlineStatus: function (newStatus, ignoreForConnectionStatus) {
      var oldStatus = Boolean(this.get('online'));
      this.set('online', newStatus);
      if (newStatus) {
        // we are back, send the remaining messages
        if (!oldStatus) {
          this.sendPendingMessages();
          if (Boolean(this.get('mainServer')) && !ignoreForConnectionStatus) {
            // main server back online
            OB.MobileApp.model.triggerOnLine();
          }
        }
      } else if (oldStatus && Boolean(this.get('mainServer')) && !ignoreForConnectionStatus) {
        // went offline from online for a main server
        OB.MobileApp.model.triggerOffLine();
      }
    },

    sendPendingMessages: function () {
      // send any pending messages
      OB.RR.ServTypeTransaction.sendMessages(this);
    },

    initialize: function (attributes) {
      if (attributes && attributes.name) {
        this.set('name', attributes.name);
        if (attributes.address) {
          this.set('address', document.location.protocol + '//' + attributes.address);
          if (attributes.address.charAt(attributes.address.length - 1) !== '/') {
            this.set('address', document.location.protocol + '//' + attributes.address + '/');
          }
        } else {
          this.set('address', attributes.address);
        }
        this.set('online', attributes.online);
        this.set('mainServer', attributes.mainServer);
        this.set('allServices', attributes.allServices);
        if (attributes.services) {
          this.set('services', attributes.services);
        } else {
          this.set('services', []);
        }
      }
    }
  });

  OB.RR.Request = Backbone.Model.extend({
    defaults: {
      ajaxRequest: null
    },
    initialize: function (attributes) {
      if (attributes && attributes.ajaxRequest) {
        this.set('ajaxRequest', attributes.ajaxRequest);
      }
    },
    exec: function (serviceName) {
      var service, services;

      OB.RR.RequestRouter.initializeProcesses();

      //if servers are not loaded yet, save request to send them after servers are ready
      if (OB.RR.RequestRouter.servers.length === 0) {
        OB.RR.RequestRouter.pendingRequests.push({
          request: this,
          serviceName: serviceName
        });
        return true;
      }
      if (this.get('ajaxRequest')) {
        services = _.filter(OB.RR.RequestRouter.availableServices.models, function (srvc) {
          return serviceName.indexOf(srvc.get('name')) !== -1;
        });
        if (services.length === 0) {
          //If services list is empty we do not have to check if we forget to define a service or not
          if (OB.RR.RequestRouter.availableServices.models.length > 0) {
            OB.error("The service does not exist. Add it to the Mobile Services window in the backend (service requested: '" + serviceName + "'')");
          }
          service = new OB.RR.Service({
            name: 'Generic',
            type: OB.RR.ServTypeFailover
          });
        } else if (services.length > 1) {
          //In case of having more than one service, get longest name service. Longest name service will be the most similar to serviceName
          _.each(services, function (iterSrvc) {
            if (!service || service.get('name').length < iterSrvc.get('name').length) {
              service = iterSrvc;
            }
          });
        } else {
          service = services[0];
        }
        service.get('type').implementation(service, this.get('ajaxRequest'));
      }
    }
  });

  OB.RR.RequestRouter = {
    servers: new Backbone.Collection(),
    availableServices: new Backbone.Collection(),
    pendingRequests: [],
    initialize: function () {
      var me = this;

      this.servers = new Backbone.Collection();
      this.availableServices = new Backbone.Collection();
      if (localStorage.servers) {
        _.each(JSON.parse(localStorage.servers), function (server) {
          me.servers.add(new OB.RR.Server(server));
        });
      }
      if (localStorage.services) {
        _.each(JSON.parse(localStorage.services), function (service) {
          me.availableServices.add(new OB.RR.Service(service));
        });
      }

      //In case current server is not in the list
      if (!localStorage.servers || !_.find(JSON.parse(localStorage.servers), function (server) {
        return server.address.split('/')[0] === document.location.host;
      })) {
        me.servers.add(new OB.RR.Server({
          name: 'Default',
          address: null,
          online: true,
          mainServer: true,
          allServices: true,
          services: []
        }));
      }
      _.each(OB.RR.RequestRouter.pendingRequests, function (obj) {
        obj.request.exec(obj.serviceName);
      });
    },

    sendAllMessages: function () {
      var me = this;
      _.each(this.servers.models, function (server) {
        server.sendPendingMessages();
      });
    },

    initializeProcesses: function () {
      var me = this;
      if (!OB.MobileApp.model || !OB.MobileApp.model.get('permissions')) {
        // not yet ready, bail out
        return;
      }

      if (this.processesInitialized) {
        return;
      }
      this.processesInitialized = true;

      function getPreference(preference, minValue, defaultValue) {
        try {
          var val = OB.MobileApp.model.get('permissions')[preference];
          if (!val || !_.isNumber(parseInt(val, 10)) || parseInt(val, 10) < minValue) {
            return defaultValue;
          }
          return parseInt(val, 10);
        } catch (e) {
          return defaultValue;
        }
      }

      localStorage.offlinePingInterval = getPreference('OBMOBC_RequestRouterPingTime', 60000, localStorage.offlinePingInterval || 60000);
      localStorage.tokenRefreshInterval = getPreference('OBMOBC_RequestRouterTokenRefreshInterval', 10 * 60000, localStorage.tokenRefreshInterval || 30 * 60000);

      if (this.offlinePing) {
        clearInterval(this.offlinePing);
      }

      this.offlinePing = setInterval(function () {
        if (_.filter(me.servers.models, function (server) {
          return !server.get('online');
        }).length > 0) {
          OB.UTIL.checkConnectivityStatus();
        }
      }, localStorage.offlinePingInterval);

      if (this.refreshTokenProcess) {
        clearInterval(this.refreshTokenInterval);
      }
      this.refreshTokenInterval = setInterval(function () {
        me.refreshToken();
      }, localStorage.tokenRefreshInterval);
    },

    refreshToken: function () {
      new OB.DS.Request('org.openbravo.mobile.core.authenticate.GetToken').exec({
        ignoreForConnectionStatus: true
      }, function (data) {
        if (data.authenticationClient && data.authenticationToken) {
          localStorage.authenticationClient = encodeURIComponent(data.authenticationClient);
          localStorage.authenticationToken = encodeURIComponent(data.authenticationToken);
        }
      }, function () {}, true, 20000);
    }
  };

  OB.RR.RequestRouter.initialize();
}());