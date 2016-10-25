/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

(function () {

  enyo.kind({
    name: 'OBRETUR.UI.MenuReturn',
    kind: 'OB.UI.MenuAction',
    permission: 'OBRETUR_Return',
    i18nLabel: 'OBRETUR_LblReturn',
    events: {
      onPaidReceipts: ''
    },
    tap: function () {
      if (this.disabled) {
        return true;
      }
      if (this.model.get('order').get('isEditable') === false) {
        this.setDisabled(true);
        return;
      }
      this.inherited(arguments); // Manual dropdown menu closure
      if (!OB.MobileApp.model.get('connectedToERP')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
        return;
      }
      if (OB.MobileApp.model.hasPermission(this.permission)) {
        this.doPaidReceipts({
          isQuotation: false,
          isReturn: true
        });
      }
    },
    updateVisibility: function () {
      var me = this;
      if (this.receipt.get('isEditable') === false) {
        me.setDisabled(true);
        return;
      } else {
        me.setDisabled(false);
        return;
      }
    },
    init: function (model) {
      var me = this;
      this.model = model;
      this.receipt = model.get('order');
      this.receipt.on('change:isEditable', function () {
        this.updateVisibility();
      }, this);
    }
  });

  // Register the menu...
  OB.OBPOSPointOfSale.UI.LeftToolbarImpl.prototype.menuEntries.push({
    kind: 'OBRETUR.UI.MenuReturn'
  });
}());