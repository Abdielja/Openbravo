/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, document, _ */

enyo.kind({
  name: 'OB.UI.Keyboard',
  commands: {},
  buttons: {},
  maxButtons: 6,
  status: '',
  sideBarEnabled: false,
  destroy: function () {
    this.buttons = null;
    this.commands = null;
    this.inherited(arguments);
  },
  keyMatcher: /^([0-9]|\.|,| |%|[a-z]|[A-Z])$/,
  classes: 'row-fluid',
  components: [{
    kind: 'OB.UI.MoreButtons',
    name: 'OB_UI_MoreButtons'
  }, {
    name: 'toolbarcontainer',
    classes: 'span3'
  }, {
    classes: 'span9',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span8',
        components: [{
          style: 'margin:5px',
          components: [{
            style: 'text-align: right; width: 100%; height: 40px;',
            components: [{
              style: 'margin: 0px 0px 9px 0px; background-color: whiteSmoke; border: 1px solid #CCC; word-wrap: break-word; font-size: 35px; height: 33px; padding: 22px 5px 0px 0px;',
              components: [{
                name: 'editbox',
                style: 'border: 0px; word-wrap: break-word; font-size: 30px; font-family: monospace; height: 54px; text-align: right; width: 99%;',
                adjustFontSize: function () {
                  var contentLength = this.getContent().length;
                  var newFontSize = 30;
                  if (contentLength >= 15) {
                    newFontSize = 15;
                  } else if (contentLength >= 11) {
                    newFontSize = 23;
                  }
                  var newStyle = enyo.format("border: 0px; word-wrap: break-word; font-size: %spx; font-family: monospace; height: 54px; text-align: right; width: 99%;", newFontSize);
                  this.setStyle(newStyle);
                }
              }]
            }]
          }]
        }]
      }, {
        classes: 'span4',
        components: [{
          kind: 'OB.UI.ButtonKey',
          name: 'OBKEY_backspace',
          classButton: 'btn-icon btn-icon-backspace',
          command: 'del'
        }]
      }, {
        classes: 'row-fluid',
        name: 'OBKeyBoard_centerAndRightSideContainer',
        components: [{ // keypadcontainer
          classes: 'span8',
          name: 'keypadcontainer'
        }, {
          name: 'OBKeyBoard_rightSideToolbar',
          classes: 'span4',
          components: [{
            // rigth toolbar with qty, discount... buttons
            name: 'sideenabled',
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: '-',
                  classButton: 'btnkeyboard-num btnkeyboard-minus',
                  command: '-',
                  name: 'minusBtn',
                  init: function () {
                    this.switchButton();
                  }
                }]
              }, {
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: '+',
                  classButton: 'btnkeyboard-num btnkeyboard-plus',
                  command: '+',
                  name: 'plusBtn',
                  init: function () {
                    this.switchButton();
                  }
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  name: 'btnQty',
                  kind: 'OB.UI.ButtonKey',
                  command: 'line:qty',
                  init: function (model) {
                    this.switchButton();
                  }
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'btnPrice',
                  permission: 'OBPOS_order.changePrice',
                  command: 'line:price'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'btnDiscount',
                  permission: 'OBPOS_order.discount',
                  command: 'line:dto',
                  init: function (model) {
                    var receipt = model.get('order'),
                        me = this;
                    if (receipt) {
                      if (OB.MobileApp.model.get('permissions')["OBPOS_retail.discountkeyboard"] === false) {
                        this.command = 'line:dto';
                      } else {
                        receipt.get('lines').on('add remove', function () {
                          if (model.get('leftColumnViewManager') && model.get('leftColumnViewManager').isMultiOrder()) {
                            this.waterfall('onDisableButton', {
                              disabled: true
                            });
                          }
                          if (OB.UTIL.isDisableDiscount) {
                            OB.UTIL.isDisableDiscount(receipt, function (disable) {
                              if (!OB.MobileApp.model.hasPermission(me.permission, true) || disable) {
                                me.waterfall('onDisableButton', {
                                  disabled: true
                                });
                              } else {
                                me.waterfall('onDisableButton', {
                                  disabled: false
                                });
                              }
                            });
                          }
                        }, this);
                        this.command = 'screen:dto';
                      }
                    }
                  }
                }]
              }]
            }]
          }, {
            // empty right toolbar used in case the keyboard
            // shouldn't support these buttons
            name: 'sidedisabled',
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_disabled_A'
                }]
              }, {
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_disabled_B'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_disabled_C'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_disabled_D'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_disabled_E'
                }]
              }]
            }]
          }, {
            // right toolbar for ticket discounts
            name: 'ticketDiscountsToolbar',
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_discounts_A'
                }]
              }, {
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_discounts_B'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_discounts_C'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_discounts_D'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_discounts_E',
                  description: 'Discount button when discounts sidebar is used',
                  permission: 'OBPOS_retail.advDiscounts',
                  command: 'ticket:discount'
                }]
              }]
            }]
          }, {
            // rigth toolbar with plus and minus (Cash upr side toolbar)
            name: 'sidecashup',
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: '-',
                  classButton: 'btnkeyboard-num btnkeyboard-minus',
                  command: '-',
                  name: 'OBKEY_right_cashup_A',
                  description: '+ button used for cashup'
                }]
              }, {
                classes: 'span6',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  label: '+',
                  classButton: 'btnkeyboard-num btnkeyboard-plus',
                  command: '+',
                  name: 'OBKEY_right_cashup_B',
                  description: '- button used for cashup'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_cashup_C'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_cashup_D'
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                components: [{
                  kind: 'OB.UI.ButtonKey',
                  name: 'OBKEY_right_cashup_E'
                }]
              }]
            }]
          }, {
            classes: 'row-fluid',
            components: [{
              classes: 'span12',
              components: [{
                kind: 'OB.UI.ButtonKey',
                classButton: 'btn-icon btn-icon-enter',
                command: 'OK',
                name: 'btnEnter'
              }]
            }]
          }]
        }]
      }]
    }]
  }],

  events: {
    onCommandFired: '',
    onStatusChanged: '',
    onHoldActiveCmd: ''
  },

  handlers: {
    onGlobalKeypress: 'globalKeypressHandler',
    onCommandFired: 'commandHandler',
    onRegisterButton: 'registerButton',
    onKeyboardDisabled: 'keyboardDisabled',
    onClearEditBox: 'clearEditBox',
    onEnableQtyButton: 'enableQtyButton',
    onEnablePlusButton: 'enablePlusButton',
    onEnableMinusButton: 'enableMinusButton'
  },
  isEnabled: true,

  disableCommandKey: function (inSender, inEvent) {
    this.waterfall('onDisableButton', inEvent);
  },

  keyboardDisabled: function (inSender, inEvent) {
    if (inEvent.status) {
      _.each(this.buttons, function (btn) {
        if (!btn.hasClass('btnkeyboard-inactive')) {
          btn.setDisabled(true);
          btn.addClass('btnkeyboard-inactive');
        }
      });
      this.isEnabled = false;
    } else {
      _.each(this.buttons, function (btn) {
        if (btn.disabled) {
          btn.setDisabled(false);
          btn.removeClass('btnkeyboard-inactive');
        }
      });
      this.isEnabled = true;
    }
  },

  /**
   * Managing key up events. KeyDown or KeyPress is not properly
   * working in Android Chrome 26
   */
  globalKeypressHandler: function (inSender, inEvent) {
    var which = inEvent.keyboardEvent.which,
        actualStatus = null,
        actualChar, keeper;
    if (inEvent.keyboardEvent.originator && (inEvent.keyboardEvent.originator.hasClass('modal-dialog') || inEvent.keyboardEvent.originator.hasClass('modal-popup'))) {
      return;
    }

    //Issue 25013. This flag is checked by keypressHandler function in ob-terminal-component.js
    if (OB && OB.MobileApp && OB.MobileApp.model && OB.MobileApp.model.get('useBarcode')) {
      OB.MobileApp.keyPressProcessed = true;
    }

    if (which >= 96 && which <= 105) {
      which -= 48;
    }

    if (which === 46) { //Handle DELETE key
      this.doCommandFired({
        key: "line:delete"
      });
    }

    actualChar = String.fromCharCode(which);

    if (which === 107) {
      actualChar = '+';
    }

    if (which === 111 || which === 191) {
      actualChar = '/';
    }

    if (which === 186) {
      actualChar = ':';
    }

    if (which === 109 || which === 189) {
      actualChar = '-';
    }

    if (which === 8) { //Handle BACKSPACE key
      OB.MobileApp.view.scanningFocus();
      this.writeCharacter('del');
      return;
    } else if (which === 110) { //Handle numeric keypad dot (.)
      this.writeCharacter(OB.Format.defaultDecimalSymbol);
      return;
    } else if (which === 190) { //Handle keyboard dot (.) character
      this.writeCharacter(OB.Format.defaultDecimalSymbol);
      return;
    }

    if (actualChar && actualChar.match(this.keyMatcher)) {
      this.writeCharacter(actualChar);
    } else if ((which === 13 && !OB.UTIL.isIOS()) || (OB.UTIL.isIOS() && inEvent.enterEvent)) { //Handle ENTER key
      actualStatus = this.getStatus();
      keeper = document.getElementById('_focusKeeper');
      if (OB.MobileApp.view.scanMode && OB.MobileApp.model.get('useBarcode')) {
        // scanning using focusKeeper, take the value in the focusKeeper
        // some scanners do not send events for each character, that's why
        // the whole contents of focusKeeper are taken
        if (keeper) {
          if (keeper.value && keeper.value.length > 0) {
            this.$.editbox.setContent(keeper.value);
            keeper.value = '';
          } else {
            keeper.value = '';
            this.$.editbox.setContent(this.$.editbox.getContent());
          }
        }
      } else {
        this.clearInput();
        return;
      }
      if (this.$.editbox.getContent() === '0') {
        this.doCommandFired({
          key: "OK"
        });
      } else if (actualStatus) {
        this.execCommand(actualStatus, this.getString());
      } else {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_KeypadTargetMissing'));
      }
    } else if (which === 107 || which === 109) { //Handle + and - keys
      if (!OB.MobileApp.view.scanMode || !OB.MobileApp.scanning) {
        this.doCommandFired({
          key: actualChar
        });
      }

    } else if (OB.Format.defaultDecimalSymbol === '.') { //Handle any keypress except any kind of dot (.)
      if (which === 229) {
        var scan = document.getElementById('_focusKeeper').value;
        for (i = 0; i < scan.length; i++) {
          this.writeCharacter(scan.charAt(i));
        }
      } else {
        this.writeCharacter(actualChar);
      }
    }
  },

  virtualKeypressHandler: function (key, options) {
    if (options && options.fromPopup) {
      this.waterfall('onCloseAllPopups');
    }
    if (key.match(this.keyMatcher) || (key === 'del')) {
      this.writeCharacter(key);
    } else {
      this.doCommandFired({
        key: key
      });
    }
  },

  writeCharacter: function (character) {
    var content = this.$.editbox.getContent();
    var contentLength = content.length;
    if (contentLength >= 20 && character !== 'del') {
      OB.UTIL.showError(OB.I18N.getLabel('OBMOBC_ErrorMaxNumber'));
      return;
    }
    if (character.match(this.keyMatcher) && this.isEnabled) {
      this.$.editbox.setContent(content + character);
    } else if (character === 'del') {
      if (contentLength > 0) {
        this.$.editbox.setContent(content.substring(0, contentLength - 1));
      }
    }
    this.$.editbox.adjustFontSize();
    document.getElementById('_focusKeeper').value = this.$.editbox.getContent();
  },

  setStatus: function (newstatus) {
    var btn = this.buttons[this.status];

    if (btn && (btn.classButtonActive || (btn.owner && btn.owner.classButtonActive))) {
      btn.removeClass(btn.classButtonActive || btn.owner.classButtonActive);
    }
    this.status = newstatus;

    // sending the event to the components bellow this one
    this.waterfall('onStatusChanged', {
      status: newstatus
    });
    // sending the event to the components above this one
    this.doStatusChanged({
      payment: OB.MobileApp.model.paymentnames ? OB.MobileApp.model.paymentnames[this.status] : null,
      status: this.status
    });

    // set the right keypad by default
    if (this.namedkeypads[this.status]) {
      this.showKeypad(this.namedkeypads[this.status]);
    } else {
      this.showKeypad('basic');
    }

    btn = this.buttons[this.status];
    if (btn && (btn.classButtonActive || (btn.owner && btn.owner.classButtonActive))) {
      btn.addClass(btn.classButtonActive || btn.owner.classButtonActive);
    }
  },

  getStatus: function () {
    //returns the current status of the keyboard. If the keyboard doesn't have any status then
    //the function returns the default action for the keyboard.
    if (this.status) {
      if (this.status === "") {
        return this.defaultcommand;
      } else {
        return this.status;
      }
    }
    return this.defaultcommand;
  },

  execCommand: function (cmd, txt) {
    var cmddefinition = this.commands[cmd];
    //if (!cmddefinition.permission || OB.MobileApp.model.hasPermission(cmddefinition.permission)) {
    cmddefinition.action(this, txt);
    //}
  },

  execStatelessCommand: function (cmd, txt) {
    this.commands[cmd].action(this, txt);
  },

  getNumber: function () {
    return OB.I18N.parseNumber(this.getString());
  },

  getString: function () {
    var s = this.$.editbox.getContent();
    this.$.editbox.setContent('');
    return s;
  },

  clearInput: function () {
    this.$.editbox.setContent('');
    document.getElementById('_focusKeeper').value = '';
    this.setStatus('');
  },
  clearEditBox: function () {
    this.$.editbox.setContent('');
    document.getElementById('_focusKeeper').value = '';
  },
  enableQtyButton: function (inSender, inEvent) {
    if (inEvent.enable) {
      this.$.btnQty.disableButton(this.$.btnQty, {
        disabled: false
      });
    } else {
      this.$.btnQty.disableButton(this.$.btnQty, {
        disabled: true
      });
    }
  },
  enablePlusButton: function (inSender, inEvent) {
    if (inEvent.enable) {
      this.$.plusBtn.disableButton(this.$.plusBtn, {
        disabled: false
      });
    } else {
      this.$.plusBtn.disableButton(this.$.plusBtn, {
        disabled: true
      });
    }
  },
  enableMinusButton: function (inSender, inEvent) {
    if (inEvent.enable) {
      this.$.minusBtn.disableButton(this.$.minusBtn, {
        disabled: false
      });
    } else {
      this.$.minusBtn.disableButton(this.$.minusBtn, {
        disabled: true
      });
    }
  },
  commandHandler: function (inSender, inEvent) {
    var txt;
    var cmd = inEvent.key;

    if (cmd === 'OK') {
      txt = this.getString();

      // Shortcut to lock screen
      // Check for preference to disable lock screen, pass second parameter "true" to avoid false
      // positives on automatic roles.
      // hasPermission only loads preferences that belong to the mobile application in use. The
      // preference is defined using a posterminal preference property. Other applications that
      // want to disable the shortcut should deliver its own preference property and add it to
      // this if clause.
      if (txt === '0' && this.status === '' && !OB.MobileApp.model.hasPermission('OBPOS_DisableLockShortcut', true)) {
        OB.MobileApp.model.lock();
        return;
      }
      if (txt && this.status === '') {
        if (this.defaultcommand) {
          this.execCommand(this.defaultcommand, txt);
        } else {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_KeypadTargetMissing'));
        }
      } else if (txt && this.status !== '') {
        this.execCommand(this.status, txt);
        if (this.commands[this.status] && !this.commands[this.status].holdActive) {
          this.setStatus('');
        }
      }
    } else if (this.commands[cmd]) {
      txt = this.getString();
      if (this.commands[cmd].stateless) {
        // Stateless commands: add, subs, ...
        this.execStatelessCommand(cmd, txt);
      } else {
        // Statefull commands: quantity, price, discounts, payments ...
        if (txt && this.status === '') { // Short cut: type + action
          this.execCommand(cmd, txt);
        } else if (this.status === cmd && txt) {
          this.execCommand(cmd, txt);
        } else if (this.status === cmd) { // Reset status
          this.setStatus('');
        } else {
          this.setStatus(cmd);
        }
      }
      if (this.commands[cmd].holdActive) {
        this.doHoldActiveCmd({
          cmd: cmd
        });
      }
    } else {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_NoActionDefined'));
    }
  },

  registerButton: function (inSender, inEvent) {
    var me = this,
        button = inEvent.originator;
    if (button.command) {
      if (button.definition) {
        this.addCommand(button.command, button.definition);
      }
      if (button.command === '---') {
        // It is the null command
        button.command = false;
      } else if (!button.command.match(this.keyMatcher) && button.command !== 'OK' && button.command !== 'del' && button.command !== String.fromCharCode(13) && !this.commands[button.command]) {
        // is not a key and does not exists the command
        button.command = false;
      } else if (button.permission && !OB.MobileApp.model.hasPermission(button.permission)) {
        // does not have permissions.
        button.command = false;
      }
    }

    if (button.command) {
      button.$.button.tap = function () {
        if (button && button.definition && button.definition.includedInPopUp) {
          me.virtualKeypressHandler(button.command, {
            fromPopup: button.definition.includedInPopUp
          });
        } else {
          me.virtualKeypressHandler(button.command);
        }
      };

      this.addButton(button.command, button.$.button);
      button.$.button.removeClass('btnkeyboard-inactive');

    } else {
      button.disableButton(button, {
        disabled: true
      });
    }
  },

  initComponents: function () {
    var undef;
    this.buttons = {}; // must be intialized before calling super, not after.
    this.activekeypads = [];
    this.namedkeypads = {};

    this.inherited(arguments);
    // setting labels
    this.$.btnQty.$.button.setContent(OB.I18N.getLabel('OBMOBC_KbQuantity'));
    this.$.btnPrice.$.button.setContent(OB.I18N.getLabel('OBMOBC_KbPrice'));
    this.$.btnDiscount.$.button.setContent(OB.I18N.getLabel('OBMOBC_KbDiscount'));
    this.$.OBKEY_right_discounts_E.$.button.setContent(OB.I18N.getLabel('OBMOBC_KbDiscount'));

    if (this.buttonsDef) {
      if (this.buttonsDef.sideBar) {
        if (this.buttonsDef.sideBar.plusI18nLbl !== undef) {
          this.$.plusBtn.setLabel(OB.I18N.getLabel(this.buttonsDef.sideBar.plusI18nLbl));
        }
        if (this.buttonsDef.sideBar.minusI18nLbl !== undef) {
          this.$.minusBtn.setLabel(OB.I18N.getLabel(this.buttonsDef.sideBar.minusI18nLbl));
        }
        if (this.buttonsDef.sideBar.qtyI18nLbl !== undef) {
          this.$.btnQty.setLabel(OB.I18N.getLabel(this.buttonsDef.sideBar.qtyI18nLbl));
        }
        if (this.buttonsDef.sideBar.priceI18nLbl !== undef) {
          this.$.btnPrice.setLabel(OB.I18N.getLabel(this.buttonsDef.sideBar.priceI18nLbl));
        }
        if (this.buttonsDef.sideBar.discountI18nLbl !== undef) {
          this.$.btnDiscount.setLabel(OB.I18N.getLabel(this.buttonsDef.sideBar.discountI18nLbl));
        }
      }
    }

    this.state = new Backbone.Model();

    this.$.toolbarcontainer.destroyComponents();
    this.$.keypadcontainer.destroyComponents();

    this.showSidepad('sidedisabled');

    if (this.sideBarEnabled) {
      this.$.sideenabled.show();
      this.$.sidedisabled.hide();
      this.$.ticketDiscountsToolbar.hide();
      this.$.sidecashup.hide();
    } else {
      this.$.ticketDiscountsToolbar.hide();
      this.$.sidecashup.hide();
      this.$.sideenabled.hide();
      this.$.sidedisabled.show();
    }

    this.addKeypad('OB.UI.KeypadBasic');
    this.showKeypad('basic');
  },

  addToolbar: function (newToolbar) {
    var toolbar = this.$.toolbarcontainer.createComponent({
      toolbarName: newToolbar.name,
      shown: newToolbar.shown,
      keboard: this
    });
    var emptyBtn = {
      kind: 'OB.UI.BtnSide',
      btn: {}
    },
        i = 0,
        hasMore = newToolbar.buttons.length > this.maxButtons,
        displayedButtons = hasMore ? this.maxButtons - 1 : newToolbar.buttons.length,
        displayedEmptyButtons = hasMore ? 0 : this.maxButtons - newToolbar.buttons.length,
        btnDef, dialogButtons = {};

    for (i = 0; i < newToolbar.buttons.length; i++) {
      btnDef = newToolbar.buttons[i];
      if (i < displayedButtons) {
        // Send button to toolbar
        if (btnDef.command) {
          toolbar.createComponent({
            kind: 'OB.UI.BtnSide',
            btn: btnDef
          });
        } else {
          toolbar.createComponent(emptyBtn);
        }
      } else {
        // Send button to dialog.
        dialogButtons[btnDef.command] = btnDef;
        this.addCommand(btnDef.command, btnDef.definition); // It needs to register command before showing the button.
      }
      if (btnDef.initialize) {
        btnDef.initialize(this);
      }
    }

    if (hasMore) {
      // Add the button More..
      toolbar.createComponent({
        name: 'btnMore',
        keyboard: this,
        dialogButtons: dialogButtons,
        kind: 'OB.UI.ToolbarMore'
      });
    }

    // populate toolbar up to maxButtons with empty buttons
    for (i = 0; i < displayedEmptyButtons; i++) {
      toolbar.createComponent(emptyBtn);
    }

    //A toolbar could be create async, so we will hide it after creation.
    toolbar.hide();
  },

  addToolbarComponent: function (newToolbar) {
    this.$.toolbarcontainer.createComponent({
      kind: newToolbar,
      keyboard: this
    });
  },

  showToolbar: function (toolbarName) {
    this.show();
    enyo.forEach(this.$.toolbarcontainer.getComponents(), function (toolbar) {
      if (toolbar.toolbarName === toolbarName) {
        toolbar.render();
        toolbar.show();
        if (toolbar.shown) {
          toolbar.shown();
        }
      } else {
        toolbar.hide();
      }
    }, this);
  },

  addCommand: function (cmd, definition) {
    this.commands[cmd] = definition;
  },

  addButton: function (cmd, btn) {
    if (this.buttons[cmd]) {
      if (this.buttons[cmd].add) {
        this.buttons[cmd] = this.buttons[cmd].add(btn);
      }
    } else {
      this.buttons[cmd] = btn;
    }
  },

  addKeypad: function (keypad) {

    if (this.destroyed) {
      return;
    }
    var keypadconstructor = enyo.constructorForKind(keypad);
    this.activekeypads.push(keypadconstructor.prototype.padName);
    if (keypadconstructor.prototype.padPayment) {
      this.namedkeypads[keypadconstructor.prototype.padPayment] = keypadconstructor.prototype.padName;
    }

    this.$.keypadcontainer.createComponent({
      kind: keypad,
      keyboard: this
    }).render().hide();
  },

  showKeypad: function (keypadName) {
    var firstLabel = null,
        foundLabel = false,
        existsLabel = false;

    enyo.forEach(this.$.keypadcontainer.getComponents(), function (pad) {
      if (!firstLabel) {
        firstLabel = pad.label;
      } else if (foundLabel) {
        this.state.set('keypadNextLabel', this.state.get('keypadName') !== 'basic' ? this.$.keypadcontainer.children[0].label : this.searchLabelPad(this.namedkeypads[this.status]));
        foundLabel = false;
      }
      if (pad.padName === keypadName) {
        this.state.set('keypadName', keypadName);
        foundLabel = true;
        existsLabel = true;
        pad.show();
        // Set the right payment status. If needed.
        if (pad.padPayment && this.status !== pad.padPayment) {
          this.setStatus(pad.padPayment);
        }
      } else {
        pad.hide();
      }
    }, this);
    if (foundLabel) {
      this.state.set('keypadNextLabel', firstLabel);
    }

    // if keypadName does not exists show the 'basic' panel that always exists
    if (!existsLabel) {
      this.showKeypad('basic');
    }
  },

  showNextKeypad: function () {
    var i, max, current = this.state.get('keypadName');

    if (current !== 'basic') {
      this.showKeypad('basic');
    } else {
      for (i = 0, max = this.activekeypads.length; i < max; i++) {
        if (this.activekeypads[i] === current) {
          if (this.namedkeypads[this.status] === this.activekeypads[1] && this.activekeypads.length > 2) {
            this.showKeypad(i < this.activekeypads.length - 1 ? this.activekeypads[i + 2] : this.activekeypads[0]);
            break;
          } else {
            this.showKeypad(i < this.activekeypads.length - 1 ? this.activekeypads[i + 1] : this.activekeypads[0]);
            break;
          }

        }
      }
    }
  },

  searchLabelPad: function (status) {
    var i;
    if (!_.isUndefined(status)) {
      if (this.activekeypads.length > 2) {
        for (i = 0; i < this.activekeypads.length; i++) {
          if (this.activekeypads[i] === status) {
            return i < this.activekeypads.length - 1 ? this.$.keypadcontainer.children[i + 1].label : this.$.keypadcontainer.children[1].label;
          }
        }
      } else {
        return this.$.keypadcontainer.children[1].label;
      }
    } else {
      return this.$.keypadcontainer.children[1].label;
    }
  },

  showSidepad: function (sidepadname) {
    this.$.sideenabled.hide();
    this.$.sidedisabled.hide();
    this.$.ticketDiscountsToolbar.hide();
    this.$.sidecashup.hide();
    this.$[sidepadname].show();
  }
});

enyo.kind({
  name: 'OB.UI.BtnSide',
  style: 'display:table; width:100%',
  handlers: {
    onGlobalKeypress: 'changeScanningMode'
  },
  changeScanningMode: function () {
    if (this.btn.command === 'code') {
      OB.MobileApp.scanning = this.children[0].children[0].hasClass(this.btn.classButtonActive);
    }
  },
  initComponents: function () {
    this.createComponent({
      kind: 'OB.UI.ButtonKey',
      name: this.btn.command,
      label: this.btn.i18nLabel ? OB.I18N.getLabel(this.btn.i18nLabel) : this.btn.label,
      command: this.btn.command,
      permission: this.btn.permission,
      definition: this.btn.definition,
      classButtonActive: this.btn.classButtonActive || 'btnactive-green'
    });
  }
});

/**
 * Abstract class to handle barcode scan, findProductByBarcode and addProductToReceipt
 * methods need to be implemented
 */
enyo.kind({
  name: 'OB.UI.AbstractBarcodeActionHandler',
  kind: enyo.Object,
  action: function (keyboard, txt) {
    if (keyboard.receipt && keyboard.receipt.get('isEditable') === false) {
      keyboard.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    var me = this;
    OB.debug("AbstractBarcodeActionHandler - txt: " + txt);
    this.findProductByBarcode(txt, function (product) {
      me.addProductToReceipt(keyboard, product);
    }, keyboard);
  }
});

enyo.kind({
  name: 'OB.UI.ToolbarMore',
  style: 'display:table; width:100%;',
  handlers: {
    onStatusChanged: 'statusChanged'
  },
  components: [{
    style: 'margin: 5px;',
    components: [{
      kind: 'OB.UI.Button',
      classes: 'btnkeyboard',
      name: 'btn',
      label: ''
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.btn.setContent(OB.I18N.getLabel('OBMOBC_More'));
    this.activegreen = false;
  },
  tap: function () {
    if (this.activegreen) {
      this.keyboard.setStatus('');
    } else {
      this.keyboard.$.OB_UI_MoreButtons.showMoreButtons(this.dialogButtons);
    }
  },
  statusChanged: function (inSender, inEvent) {
    var status = inEvent.status;

    // Hide the More actions dialog if visible.
    if (this.keyboard.$.OB_UI_MoreButtons.hasNode()) {
      this.keyboard.$.OB_UI_MoreButtons.hide();
    }

    if (this.activegreen) {
      this.$.btn.setContent(OB.I18N.getLabel('OBMOBC_More'));
      this.$.btn.removeClass('btnactive-green');
      this.activegreen = false;
    }

    if (this.dialogButtons[status]) {
      this.$.btn.setContent(this.dialogButtons[status].label);
      this.$.btn.addClass('btnactive-green');
      this.activegreen = true;
    }
  }
});

enyo.kind({
  name: 'OB.UI.MoreButtons',
  kind: 'OB.UI.Modal',
  topPosition: '125px',
  i18nHeader: 'OBMOBC_MoreButtons',
  sideButtons: [],
  body: {
    classes: 'row-fluid',
    components: [{
      classes: 'span12',
      components: [{
        style: 'border-bottom: 1px solid #cccccc;',
        classes: 'row-fluid',
        components: [{
          name: 'morebuttonslist',
          classes: 'span12'
        }]
      }]
    }]
  },
  showMoreButtons: function (dialogButtons) {

    // Destroy previous buttons
    this.$.body.$.morebuttonslist.destroyComponents();

    // Create the new buttons.
    _.each(dialogButtons, function (btnDef) {
      this.$.body.$.morebuttonslist.createComponent({
        kind: 'OB.UI.BtnSide',
        btn: btnDef
      });
    }, this);

    // Render the new components created
    this.$.body.$.morebuttonslist.render();

    // Finally show the dialog.
    this.show();
  }
});