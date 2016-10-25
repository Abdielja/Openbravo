/*
 ************************************************************************************
 * Copyright (C) 2014-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */


(function () {

  enyo.kind({
    name: 'OB.UI.CheckboxButtonAll',
    kind: 'OB.UI.CheckboxButton',
    classes: 'modal-dialog-btn-check span1',
    style: 'width: 8%',
    events: {
      onCheckedAll: ''
    },
    handlers: {
      onAllSelected: 'allSelected'
    },
    allSelected: function (inSender, inEvent) {
      if (inEvent.allSelected) {
        this.check();
      } else {
        this.unCheck();
      }
      return true;
    },
    tap: function () {
      this.inherited(arguments);
      this.doCheckedAll({
        checked: this.checked
      });
    }
  });

  enyo.kind({
    name: 'OB.UI.CheckboxButtonReturn',
    kind: 'OB.UI.CheckboxButton',
    classes: 'modal-dialog-btn-check span1',
    style: 'width: 8%',
    handlers: {
      onCheckAll: 'checkAll'
    },
    events: {
      onLineSelected: ''
    },
    isGiftCard: false,
    checkAll: function (inSender, inEvent) {
      if (this.isGiftCard) {
        return;
      }
      if (inEvent.checked && this.parent.$.quantity.value > 0 && !this.parent.newAttribute.notReturnable) {
        this.check();
      } else {
        this.unCheck();
      }
      if (this.parent.$.quantity.value !== 0) {
        this.parent.$.quantity.setDisabled(!inEvent.checked);
        this.parent.$.qtyplus.setDisabled(!inEvent.checked);
        this.parent.$.qtyminus.setDisabled(!inEvent.checked);
      }
      if (this.parent.newAttribute.productType === 'S') {
        this.parent.$.quantity.setDisabled(true);
        this.parent.$.qtyplus.setDisabled(true);
        this.parent.$.qtyminus.setDisabled(true);
      }
    },
    tap: function () {
      if (this.isGiftCard || this.parent.$.quantity.value === 0) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_LineCanNotBeSelected'));
        return;
      }
      this.inherited(arguments);
      this.parent.$.quantity.setDisabled(!this.checked);
      this.parent.$.qtyplus.setDisabled(!this.checked);
      this.parent.$.qtyminus.setDisabled(!this.checked);

      if (this.checked) {
        this.parent.$.quantity.focus();
      }
      this.doLineSelected({
        selected: this.checked
      });
      if (this.parent.newAttribute.productType === 'S') {
        this.parent.$.quantity.setDisabled(true);
        this.parent.$.qtyplus.setDisabled(true);
        this.parent.$.qtyminus.setDisabled(true);
      }
    }
  });

  enyo.kind({
    name: 'OB.UI.EditOrderLine',
    style: 'border-bottom: 1px solid #cccccc; text-align: center; color: black; padding-top: 9px;',
    handlers: {
      onApplyChange: 'applyChange'
    },
    events: {
      onCorrectQty: ''
    },
    isGiftCard: false,
    applyChange: function (inSender, inEvent) {
      var me = this;
      var model = inEvent.model;
      var index = inEvent.lines.indexOf(this.newAttribute);
      var line, promotionsfactor;
      if (index !== -1) {
        if (this.$.checkboxButtonReturn.checked) {
          var initialQty = inEvent.lines[index].quantity;
          var qty = this.$.quantity.getValue();
          var orderList = OB.MobileApp.model.orderList;
          enyo.forEach(orderList.models, function (order) {
            enyo.forEach(order.get('lines').models, function (l) {
              if (l.get('originalOrderLineId')) {
                if (l.get('product').id === me.newAttribute.id && l.get('originalOrderLineId') === me.newAttribute.lineId) {
                  qty = qty - l.get('qty');
                }
              }
            });
          });
          if (qty > inEvent.lines[index].remainingQuantity) {
            OB.UTIL.showWarning(OB.I18N.getLabel('OBRETUR_ExceedsQuantity') + ' ' + me.newAttribute.name);
            inEvent.lines[index].exceedsQuantity = true;
          }
          inEvent.lines[index].remainingQuantity = inEvent.lines[index].remainingQuantity - this.$.quantity.getValue();
          inEvent.lines[index].selectedQuantity = this.$.quantity.getValue();
          // update promotions amount to the quantity returned
          enyo.forEach(this.newAttribute.promotions, function (p) {
            if (!OB.UTIL.isNullOrUndefined(p)) {
              p.amt = OB.DEC.mul(p.amt, (me.$.quantity.getValue() / initialQty));
              p.actualAmt = OB.DEC.mul(p.actualAmt, (me.$.quantity.getValue() / initialQty));
            }
          });
        } else {
          inEvent.lines.splice(index, 1);
        }
      }
    },
    components: [{
      kind: 'OB.UI.CheckboxButtonReturn',
      name: 'checkboxButtonReturn'
    }, {
      name: 'product',
      classes: 'span4',
      style: 'line-height: 40px; font-size: 17px; width: 180px;'
    }, {
      name: 'totalQuantity',
      classes: 'span2',
      style: 'line-height: 40px; font-size: 17px; width: 85px;'
    }, {
      name: 'remainingQuantity',
      classes: 'span2',
      style: 'line-height: 40px; font-size: 17px; width:85px;'
    }, {
      name: 'fullyReturned',
      classes: 'btn-returned span2',
      showing: false
    }, {
      name: 'qtyminus',
      kind: 'OB.UI.SmallButton',
      style: 'width: 40px',
      classes: 'btnlink-gray btnlink-cashup-edit span1',
      content: '-',
      ontap: 'subUnit'
    }, {
      kind: 'enyo.Input',
      type: 'text',
      classes: 'input span1',
      style: 'margin-right: 2px; text-align: center; width: 50px;',
      name: 'quantity',
      isFirstFocus: true,
      selectOnFocus: true,
      autoKeyModifier: 'num-lock',
      onchange: 'validate'
    }, {
      name: 'qtyplus',
      kind: 'OB.UI.SmallButton',
      style: 'width: 40px',
      classes: 'btnlink-gray btnlink-cashup-edit span1',
      content: '+',
      ontap: 'addUnit'
    }, {
      name: 'price',
      classes: 'span2',
      style: 'line-height: 40px; font-size: 17px; width: 100px;'
    }, {
      style: 'clear: both;'
    }],
    addUnit: function (inSender, inEvent) {
      var units = parseInt(this.$.quantity.getValue(), 10);
      if (!isNaN(units) && units < this.$.quantity.getAttribute('max')) {
        this.$.quantity.setValue(units + 1);
        this.validate();
      }

    },
    subUnit: function (inSender, inEvent) {
      var units = parseInt(this.$.quantity.getValue(), 10);
      if (!isNaN(units) && units > this.$.quantity.getAttribute('min')) {
        this.$.quantity.setValue(units - 1);
        this.validate();
      }
    },
    validate: function () {
      var value, maxValue;
      value = this.$.quantity.getValue();
      try {
        value = parseFloat(this.$.quantity.getValue());
      } catch (e) {
        this.addStyles('background-color: red');
        this.doCorrectQty({
          correctQty: false
        });
        return true;
      }
      maxValue = OB.DEC.toNumber(OB.DEC.toBigDecimal(this.$.quantity.getAttribute('max')));

      if (!_.isNumber(value) || _.isNaN(value)) {
        this.addStyles('background-color: red');
        this.doCorrectQty({
          correctQty: false
        });
        return true;
      }

      value = OB.DEC.toNumber(OB.DEC.toBigDecimal(value));
      this.$.quantity.setValue(value);


      if (value > maxValue || value <= 0) {
        this.addStyles('background-color: red');
        this.doCorrectQty({
          correctQty: false
        });
      } else {
        this.addStyles('background-color: white');
        this.doCorrectQty({
          correctQty: true
        });
        return true;
      }
    },
    markAsGiftCard: function () {
      this.isGiftCard = true;
      this.$.checkboxButtonReturn.isGiftCard = this.isGiftCard;
    },
    tap: function () {
      if (this.isGiftCard === true) {
        OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_LineCanNotBeSelected'));
      }
    },
    initComponents: function () {
      this.inherited(arguments);

      if (!this.newAttribute.returnable && this.newAttribute.productType === 'S') {
        this.$.checkboxButtonReturn.setDisabled(true);
        this.$.checkboxButtonReturn.removeClass('btn-icon-check');
        this.$.checkboxButtonReturn.addClass('btn-icon-notreturnable');
        this.$.quantity.hide();
        this.$.qtyplus.hide();
        this.$.qtyminus.hide();
        this.$.fullyReturned.setContent(OB.I18N.getLabel('OBRETUR_notreturnable'));
        this.$.fullyReturned.addStyles('color: #C80000');
        this.$.fullyReturned.show();
      } else if (this.newAttribute.remainingQuantity <= 0) {
        this.$.checkboxButtonReturn.setDisabled(true);
        this.$.checkboxButtonReturn.removeClass('btn-icon-check');
        this.$.checkboxButtonReturn.addClass('btn-icon-returned');
        this.$.quantity.hide();
        this.$.qtyplus.hide();
        this.$.qtyminus.hide();
        this.$.fullyReturned.setContent(OB.I18N.getLabel('OBRETUR_FullyReturned'));
        this.$.fullyReturned.show();
      }

      this.$.product.setContent(this.newAttribute.name);
      this.$.remainingQuantity.setContent(this.newAttribute.remainingQuantity);
      this.$.totalQuantity.setContent(this.newAttribute.quantity);
      this.$.quantity.setDisabled(true);
      this.$.qtyplus.setDisabled(true);
      this.$.qtyminus.setDisabled(true);
      this.$.quantity.setValue(this.newAttribute.remainingQuantity);
      this.$.quantity.setAttribute('max', this.newAttribute.remainingQuantity);
      this.$.quantity.setAttribute('min', OB.DEC.One);
      this.$.price.setContent(this.newAttribute.priceIncludesTax ? this.newAttribute.unitPrice : this.newAttribute.baseNetUnitPrice);
      if (this.newAttribute.promotions.length > 0) {
        this.$.quantity.addStyles('margin-bottom:0px');
        enyo.forEach(this.newAttribute.promotions, function (d) {
          if (d.hidden) {
            // continue
            return;
          }
          this.createComponent({
            style: 'display: block; color:gray; font-size:13px; line-height: 20px;  width: 150px;',
            components: [{
              content: '-- ' + d.name,
              attributes: {
                style: 'float: left; width: 60%;'
              }
            }, {
              content: OB.I18N.formatCurrency(-d.amt),
              attributes: {
                style: 'float: left; width: 31%; text-align: right;'
              }
            }, {
              style: 'clear: both;'
            }]
          });
        }, this);

      }
      // shipment info
      if (!OB.UTIL.isNullOrUndefined(this.newAttribute.shiplineNo)) {
        this.createComponent({
          style: 'display: block; color:gray; font-size:13px; line-height: 20px;',
          components: [{
            content: this.newAttribute.shipment + ' - ' + this.newAttribute.shiplineNo,
            attributes: {
              style: 'float: left; width: 60%;'
            }
          }, {
            style: 'clear: both;'
          }]
        });
      }
    }
  });

  enyo.kind({
    kind: 'OB.UI.ModalDialogButton',
    name: 'OB.UI.ReturnReceiptDialogApply',
    events: {
      onApplyChanges: '',
      onCallbackExecutor: '',
      onCheckQty: ''
    },
    tap: function () {
      if (this.doCheckQty()) {
        return true;
      }
      if (this.doApplyChanges()) {
        OB.UTIL.showLoading(true);
        this.doCallbackExecutor();
        this.doHideThisPopup();
      }
    },
    initComponents: function () {
      this.inherited(arguments);
      this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
    }
  });

  enyo.kind({
    kind: 'OB.UI.ModalDialogButton',
    name: 'OB.UI.ReturnReceiptDialogCancel',
    tap: function () {
      this.doHideThisPopup();
    },
    initComponents: function () {
      this.inherited(arguments);
      this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
    }
  });

  enyo.kind({
    name: 'OB.UI.ModalReturnReceipt',
    kind: 'OB.UI.ModalAction',
    classes: 'modal-dialog',
    style: 'width: 700px;',
    correctQty: true,
    handlers: {
      onApplyChanges: 'applyChanges',
      onCallbackExecutor: 'callbackExecutor',
      onCheckedAll: 'checkedAll',
      onCheckQty: 'checkQty',
      onCorrectQty: 'changeCorrectQty',
      onLineSelected: 'lineSelected'
    },
    lineShouldBeIncludedFunctions: [],
    bodyContent: {
      kind: 'Scroller',
      maxHeight: '225px',
      style: 'background-color: #ffffff;',
      thumb: true,
      horizontal: 'hidden',
      components: [{
        name: 'attributes'
      }]
    },
    bodyButtons: {
      components: [{
        kind: 'OB.UI.ReturnReceiptDialogApply'
      }, {
        kind: 'OB.UI.ReturnReceiptDialogCancel'
      }]
    },
    applyChanges: function (inSender, inEvent) {
      this.waterfall('onApplyChange', {
        lines: this.args.args.order.receiptLines,
        model: this.args.args.context.model
      });
      return true;
    },
    callbackExecutor: function (inSender, inEvent) {
      var me = this,
          i;
      var nameLocation = "";
      var oldbp = me.args.args.context.model.get('order').get('bp');
      var bpLoc = me.args.args.order.bpLocId;
      var noFoundProduct = true,
          NoFoundCustomer = true,
          isLoadedPartiallyFromBackend = false;

      var findBusinessPartner = function (bp) {
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.discount.bp', true)) {

            var oldbpfilter = {
              columns: ['businessPartner'],
              operator: 'equals',
              value: oldbp.id
            };
            var oldRemoteCriteria = [oldbpfilter];
            var criteria = {};
            criteria.remoteFilters = oldRemoteCriteria;
            OB.Dal.find(OB.Model.DiscountFilterBusinessPartner, criteria, function (discountsBP) {
              _.each(discountsBP.models, function (dsc) {
                OB.Dal.removeTemporally(dsc, function () {}, function () {
                  OB.error(arguments);
                }, true);
              });
            }, function () {
              OB.error(arguments);
            });
            var bpFilter = {
              columns: ['businessPartner'],
              operator: 'equals',
              value: OB.MobileApp.model.get('businessPartner').id
            };
            var remoteCriteria = [bpFilter];
            var criteriaFilter = {};
            criteriaFilter.remoteFilters = remoteCriteria;
            OB.Dal.find(OB.Model.DiscountFilterBusinessPartner, criteriaFilter, function (discountsBP) {
              _.each(discountsBP.models, function (dsc) {
                OB.Dal.saveTemporally(dsc, function () {}, function () {
                  OB.error(arguments);
                }, true);
              });
            }, function () {
              OB.error(arguments);
            });
          }
          var locationForBpartner = function (bpLoc) {
              var finishOrderCreation = function () {
                  me.args.args.context.model.get('order').calculateReceipt(function () {
                    me.args.args.context.model.get('order').save(function () {
                      me.args.args.context.model.get('orderList').saveCurrent();
                      OB.UTIL.showLoading(false);
                    });
                  });
                  },
                  finishOrder = _.after(me.args.args.order.receiptLines.length, finishOrderCreation),
                  createOrderLines = function () {
                  _.each(me.args.args.order.receiptLines, function (line) {
                    if (!line.exceedsQuantity) {
                      var createLineFunction = function (prod) {
                          var order = me.args.args.context.model.get('order');
                          var qty = line.selectedQuantity;
                          if (order.get('orderType') !== 1) {
                            qty = qty ? -qty : -1;
                          }
                          prod.set('ignorePromotions', true);
                          prod.set('standardPrice', line.priceIncludesTax ? line.unitPrice : line.baseNetUnitPrice);
                          order.addProductToOrder(prod, qty, null, {
                            'originalOrderLineId': line.lineId,
                            'originalDocumentNo': me.args.args.order.documentNo,
                            'skipApplyPromotions': true,
                            'promotions': line.promotions,
                            'shipmentlineId': line.shipmentlineId,
                            'relatedLines': line.relatedLines
                          });
                          me.args.args.cancelOperation = true;
                          OB.UTIL.HookManager.callbackExecutor(me.args.args, me.args.callbacks);
                          finishOrder();
                          };

                      _.each(line.promotions, function (promotion) {
                        promotion.amt = -promotion.amt;
                        promotion.actualAmt = -promotion.actualAmt;
                      });
                      OB.Dal.get(OB.Model.Product, line.id, function (product) {
                        createLineFunction(product);
                      }, null, function () {
                        new OB.DS.Request('org.openbravo.retail.posterminal.master.LoadedProduct').exec({
                          productId: line.id
                        }, function (data) {
                          createLineFunction(OB.Dal.transform(OB.Model.Product, data[0]));
                        }, function () {
                          if (noFoundProduct) {
                            noFoundProduct = false;
                            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBRETUR_InformationTitle'), OB.I18N.getLabel('OBRETUR_NoReturnLoadedText'), [{
                              label: OB.I18N.getLabel('OBPOS_LblOk'),
                              isConfirmButton: true
                            }]);
                          }
                        });
                      });
                    } else {
                      me.args.args.cancelOperation = true;
                      OB.UTIL.HookManager.callbackExecutor(me.args.args, me.args.callbacks);
                      finishOrder();
                    }
                  });
                  },
                  approvalNeeded = false,
                  currentDate = new Date(),
                  servicesToApprove = '',
                  servicesToApproveArr = [];

              me.nameLocation = bpLoc.get('name');
              bp.set('locationModel', bpLoc);
              bp.set('locId', me.args.args.order.bpLocId);
              bp.set('locName', me.nameLocation);
              me.args.args.context.model.get('order').setBPandBPLoc(bp, false, true);
              //If we do not let user to select qty to return of shipment lines, we do it automatically
              if (!OB.MobileApp.model.hasPermission("OBPOS_SplitLinesInShipments", true)) {
                me.args.args.order.receiptLines = me.autoSplitShipmentLines(me.args.args.order.receiptLines);
              }

              currentDate.setHours(0);
              currentDate.setMinutes(0);
              currentDate.setSeconds(0);
              currentDate.setMilliseconds(0);
              for (i = 0; i < me.args.args.order.receiptLines.length; i++) {
                var line = me.args.args.order.receiptLines[i];
                if (!line.notReturnable) {
                  if (line.productType === 'S' && (line.overdueReturnDays < 0 || ((currentDate.getTime() - line.overdueReturnDays * 86400000) > (new Date(me.args.args.order.orderDate)).getTime()))) {
                    servicesToApprove += '<br>· ' + line.name;
                    servicesToApproveArr.push(' * ' + line.name);
                    if (!approvalNeeded) {
                      approvalNeeded = true;
                    }
                  }
                } else {
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableProduct'), OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [line.name]));
                  return;
                }
              }
              if (approvalNeeded) {
                OB.UTIL.showLoading(false);
                OB.UTIL.Approval.requestApproval(
                OB.MobileApp.view.$.containerWindow.getRoot().model, [{
                  approval: 'OBPOS_approval.returnService',
                  message: 'OBRETUR_approval.returnService_OutOfDate',
                  params: [servicesToApprove]
                }], function (approved, supervisor, approvalType) {
                  if (approved) {
                    if (supervisor.id === OB.POS.terminal.terminal.usermodel.id) {
                      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_LblWarning'), OB.I18N.getLabel('OBRETUR_returnService_OutOfDate_warning', [servicesToApproveArr]));
                    }
                    OB.UTIL.showLoading(true);
                    createOrderLines();
                  }
                });
              } else {
                createOrderLines();
              }

              };
          if (isLoadedPartiallyFromBackend) {
            locationForBpartner(bpLoc);
          } else {
            OB.Dal.get(OB.Model.BPLocation, me.args.args.order.bpLocId, function (bpLoc) {
              locationForBpartner(bpLoc);
            }, function () {
              // TODO: Report errors properly
            });
          }
          };
      //Check businesspartner
      OB.Dal.get(OB.Model.BusinessPartner, this.args.args.order.bp, function (bp) {
        findBusinessPartner(bp);
      }, null, function () {
        new OB.DS.Request('org.openbravo.retail.posterminal.master.LoadedCustomer').exec({
          bpartnerId: me.args.args.order.bp,
          bpLocationId: me.args.args.order.bpLocId
        }, function (data) {
          isLoadedPartiallyFromBackend = true;
          bpLoc = OB.Dal.transform(OB.Model.BPLocation, data[1]);
          findBusinessPartner(OB.Dal.transform(OB.Model.BusinessPartner, data[0]));
        }, function () {
          if (NoFoundCustomer) {
            NoFoundCustomer = false;
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_InformationTitle'), OB.I18N.getLabel('OBPOS_NoReceiptLoadedText'), [{
              label: OB.I18N.getLabel('OBPOS_LblOk'),
              isConfirmButton: true
            }]);
          }
        });
      });
    },
    checkedAll: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.selectedLines = this.numberOfLines;
        this.allSelected = true;
      } else {
        this.selectedLines = 0;
        this.allSelected = false;
      }
      this.waterfall('onCheckAll', {
        checked: inEvent.checked
      });

      return true;
    },
    checkQty: function (inSender, inEvent) {
      if (!this.correctQty) {
        return true;
      }
    },
    changeCorrectQty: function (inSender, inEvent) {
      this.correctQty = inEvent.correctQty;
    },
    lineSelected: function (inSender, inEvent) {
      if (inEvent.selected) {
        this.selectedLines += 1;
      } else {
        this.selectedLines -= 1;
      }
      if (this.selectedLines === this.numberOfLines) {
        this.allSelected = true;
        this.waterfall('onAllSelected', {
          allSelected: this.allSelected
        });
        return true;
      } else {
        if (this.allSelected) {
          this.allSelected = false;
          this.waterfall('onAllSelected', {
            allSelected: this.allSelected
          });
        }
      }
    },
    splitShipmentLines: function (lines) {
      var newlines = [];

      enyo.forEach(lines, function (line) {
        if (line.shipmentlines && line.shipmentlines.length > 1) {
          enyo.forEach(line.shipmentlines, function (sline) {
            var attr, splitline = {};
            for (attr in line) {
              if (line.hasOwnProperty(attr) && !OB.UTIL.isNullOrUndefined(line[attr]) && typeof line[attr] !== 'object') {
                splitline[attr] = line[attr];
              }
            }
            splitline.compname = line.lineId + sline.shipLineId;
            splitline.quantity = sline.qty;
            splitline.shiplineNo = sline.shipmentlineNo;
            splitline.shipment = sline.shipment;
            splitline.shipmentlineId = sline.shipLineId;
            splitline.remainingQuantity = sline.remainingQty;
            // delete confusing properties
            delete splitline.linegrossamount;
            delete splitline.warehouse;
            delete splitline.warehousename;
            // split promotions
            splitline.promotions = [];
            if (line.promotions.length > 0) {
              enyo.forEach(line.promotions, function (p) {
                if (!OB.UTIL.isNullOrUndefined(p)) {
                  var attr, splitpromo = {};
                  for (attr in p) {
                    if (p.hasOwnProperty(attr) && !OB.UTIL.isNullOrUndefined(p[attr]) && typeof p[attr] !== 'object') {
                      splitpromo[attr] = p[attr];
                    }
                  }
                  splitpromo.amt = OB.DEC.mul(p.amt, (splitline.remainingQuantity / line.quantity));
                  splitpromo.actualAmt = OB.DEC.mul(p.actualAmt, (splitline.remainingQuantity / line.quantity));
                  splitline.promotions.push(splitpromo);
                }
              });
            }

            newlines.push(splitline);
          }, this);
        } else {
          line.compname = line.lineId;
          if (line.shipmentlines && line.shipmentlines.length === 1) {
            line.shipmentlineId = line.shipmentlines[0].shipLineId;
          }
          // delete confusing properties
          delete line.linegrossamount;
          delete line.warehouse;
          delete line.warehousename;
          newlines.push(line);
        }
      }, this);
      return newlines;
    },
    autoSplitShipmentLines: function (lines) {
      var newlines = [];

      enyo.forEach(lines, function (line) {
        if (line.shipmentlines && line.shipmentlines.length > 1) {
          enyo.forEach(line.shipmentlines, function (sline) {
            var attr, splitline = {};
            if (line.selectedQuantity > 0 && sline.remainingQty > 0) {
              for (attr in line) {
                if (line.hasOwnProperty(attr) && !OB.UTIL.isNullOrUndefined(line[attr]) && typeof line[attr] !== 'object') {
                  splitline[attr] = line[attr];
                }
              }
              splitline.compname = line.lineId + sline.shipLineId;
              splitline.quantity = sline.qty;
              splitline.shiplineNo = sline.shipmentlineNo;
              splitline.shipment = sline.shipment;
              splitline.shipmentlineId = sline.shipLineId;
              splitline.selectedQuantity = parseInt(splitline.selectedQuantity, 10);
              if (sline.remainingQty < splitline.selectedQuantity) {
                splitline.selectedQuantity = sline.remainingQty;
              }
              line.selectedQuantity = OB.DEC.sub(line.selectedQuantity, splitline.selectedQuantity);
              // delete confusing properties
              delete splitline.linegrossamount;
              delete splitline.warehouse;
              delete splitline.warehousename;
              // split promotions
              splitline.promotions = [];
              if (line.promotions.length > 0) {
                enyo.forEach(line.promotions, function (p) {
                  if (!OB.UTIL.isNullOrUndefined(p)) {
                    var attr, splitpromo = {};
                    for (attr in p) {
                      if (p.hasOwnProperty(attr) && !OB.UTIL.isNullOrUndefined(p[attr]) && typeof p[attr] !== 'object') {
                        splitpromo[attr] = p[attr];
                      }
                    }
                    splitpromo.amt = OB.DEC.mul(p.amt, (splitline.selectedQuantity / line.quantity));
                    splitpromo.actualAmt = OB.DEC.mul(p.actualAmt, (splitline.selectedQuantity / line.quantity));
                    splitline.promotions.push(splitpromo);
                  }
                });
              }

              newlines.push(splitline);
            }
          }, this);
        } else {
          line.compname = line.lineId;
          if (line.shipmentlines && line.shipmentlines.length === 1) {
            line.shipmentlineId = line.shipmentlines[0].shipLineId;
          }
          // delete confusing properties
          delete line.linegrossamount;
          delete line.warehouse;
          delete line.warehousename;
          newlines.push(line);
        }
      }, this);
      return newlines;
    },
    executeOnShow: function () {
      var me = this,
          lineNum = 0;
      OB.UTIL.showLoading(false);
      this.$.bodyContent.$.attributes.destroyComponents();
      this.$.header.destroyComponents();
      this.$.header.createComponent({
        name: 'CheckAllHeaderDocNum',
        style: 'text-align: center; color: white;',
        components: [{
          content: me.args.args.order.documentNo,
          name: 'documentNo',
          classes: 'span12',
          style: 'line-height: 40px; font-size: 24px;'
        }, {
          style: 'clear: both;'
        }]
      });
      if (!this.$.header.$.checkboxButtonAll) {
        this.$.header.addStyles('padding-bottom: 0px; margin: 0px; height: 103px;');

        this.$.header.createComponent({
          name: 'CheckAllHeader',
          style: 'padding-top: 5px; border-bottom: 3px solid #cccccc; text-align: center; color: black; margin-top: 10px; padding-bottom: 8px;  font-weight: bold; background-color: white',
          components: [{
            kind: 'OB.UI.CheckboxButtonAll',
            name: 'checkboxButtonAll'
          }, {
            content: OB.I18N.getLabel('OBRETUR_LblProductName'),
            name: 'productNameLbl',
            classes: 'span4',
            style: 'line-height: 40px; font-size: 17px;  width: 180px;'
          }, {
            name: 'totalQtyLbl',
            content: OB.I18N.getLabel('OBRETUR_LblTotalQty'),
            classes: 'span2',
            style: 'line-height: 40px; font-size: 17px; width: 85px;'
          }, {
            name: 'remainingQtyLbl',
            content: OB.I18N.getLabel('OBRETUR_LblRemainingQty'),
            classes: 'span2',
            style: 'line-height: 40px; font-size: 17px; width: 85px;'
          }, {
            content: OB.I18N.getLabel('OBRETUR_LblQty'),
            name: 'qtyLbl',
            classes: 'span3',
            style: 'line-height: 40px; font-size: 17px; width: 155px;'
          }, {
            content: OB.I18N.getLabel('OBRETUR_LblPrice'),
            name: 'priceLbl',
            classes: 'span2',
            style: 'line-height: 40px; font-size: 17px; width: 110px;'
          }, {
            style: 'clear: both;'
          }]
        });
      } else {
        this.$.header.$.checkboxButtonAll.unCheck();
      }
      if (OB.MobileApp.model.hasPermission("OBPOS_SplitLinesInShipments", true)) {
        this.args.args.order.receiptLines = this.splitShipmentLines(this.args.args.order.receiptLines);
      }
      this.numberOfLines = 0;
      this.selectedLines = 0;
      this.allSelected = false;

      enyo.forEach(this.args.args.order.receiptLines, function (line) {
        lineNum++;
        var isSelectableLine = true;
        _.each(this.lineShouldBeIncludedFunctions, function (f) {
          isSelectableLine = isSelectableLine && f.isSelectableLine(line);
        });
        var lineEnyoObject = this.$.bodyContent.$.attributes.createComponent({
          kind: 'OB.UI.EditOrderLine',
          name: 'line' + lineNum,
          newAttribute: line
        });
        if (!isSelectableLine) {
          lineEnyoObject.markAsGiftCard();
        }
        this.numberOfLines += 1;
        if (line.productType === 'S' && !line.returnable) {
          line.notReturnable = true;
        }
      }, this);

      this.$.bodyContent.$.attributes.render();
      this.$.header.render();
      // Set correctQty to true onShow the popup.
      this.waterfall('onCorrectQty', {
        correctQty: true
      });
    },
    initComponents: function () {
      this.inherited(arguments);
      this.attributeContainer = this.$.bodyContent.$.attributes;
    }
  });
  OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
    kind: 'OB.UI.ModalReturnReceipt',
    name: 'modalReturnReceipt'
  });
}());