/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, */


// This definition overwrites the enyo component 'enyo.g11n.Fmts' used for example in onyx.DatePicker
// This way localization is defined in Openbravo
enyo.kind({
  name: 'enyo.g11n.Fmts',

  getMonthFields: function () {
    return OB.I18N.getMonthsShortList();
  },

  getDateFieldOrder: function () {
    return OB.I18N.getLabel('OBMOBC_Date_enyo_date_order');
  },

  getTimeFieldOrder: function () {
    return OB.I18N.getLabel('OBMOBC_Date_enyo_time_order');
  }
});