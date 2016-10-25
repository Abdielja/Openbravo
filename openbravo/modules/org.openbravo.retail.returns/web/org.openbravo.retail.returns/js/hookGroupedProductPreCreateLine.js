/*
 ************************************************************************************
 * Copyright (C) 2013-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $, _ */


(function () {

  OB.UTIL.HookManager.registerHook('OBPOS_GroupedProductPreCreateLine', function (args, callbacks) {

    if (!OB.UTIL.isNullOrUndefined(args.line) && !OB.UTIL.isNullOrUndefined(args.line.get('originalOrderLineId'))) {
      args.cancelOperation = true;
      args.receipt.createLine(args.p, args.qty, args.options, args.attrs);
      args.receipt.save();
    }
    OB.UTIL.HookManager.callbackExecutor(args, callbacks);

    return;
  });

}());