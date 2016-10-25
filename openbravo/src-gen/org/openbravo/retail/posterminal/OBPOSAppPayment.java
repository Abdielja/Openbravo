/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2008-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.base.structure.ActiveEnabled;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.structure.Traceable;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.retail.config.CashManagementEvents;
/**
 * Entity class for entity OBPOS_App_Payment (stored in table OBPOS_APP_PAYMENT).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class OBPOSAppPayment extends BaseOBObject implements Traceable, ClientEnabled, OrganizationEnabled, ActiveEnabled {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "OBPOS_APP_PAYMENT";
    public static final String ENTITY_NAME = "OBPOS_App_Payment";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CLIENT = "client";
    public static final String PROPERTY_ORGANIZATION = "organization";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    public static final String PROPERTY_CREATEDBY = "createdBy";
    public static final String PROPERTY_UPDATED = "updated";
    public static final String PROPERTY_UPDATEDBY = "updatedBy";
    public static final String PROPERTY_SEARCHKEY = "searchKey";
    public static final String PROPERTY_FINANCIALACCOUNT = "financialAccount";
    public static final String PROPERTY_OBPOSAPPLICATIONS = "obposApplications";
    public static final String PROPERTY_PAYMENTMETHOD = "paymentMethod";
    public static final String PROPERTY_OBRETCOCMEVENTS = "obretcoCmevents";
    public static final String PROPERTY_COMMERCIALNAME = "commercialName";
    public static final String PROPERTY_LINE = "line";
    public static final String PROPERTY_OVERRIDECONFIGURATION = "overrideconfiguration";
    public static final String PROPERTY_CASHDIFFERENCES = "cashDifferences";
    public static final String PROPERTY_GLITEMFORCASHDROPDEPOSIT = "gLItemForCashDropDeposit";
    public static final String PROPERTY_AUTOMATEMOVEMENTTOOTHERACCOUNT = "automateMovementToOtherAccount";
    public static final String PROPERTY_KEEPFIXEDAMOUNT = "keepFixedAmount";
    public static final String PROPERTY_AMOUNT = "amount";
    public static final String PROPERTY_ALLOWVARIABLEAMOUNT = "allowVariableAmount";
    public static final String PROPERTY_ALLOWNOTTOMOVE = "allowNotToMove";
    public static final String PROPERTY_ALLOWMOVEEVERYTHING = "allowMoveEverything";
    public static final String PROPERTY_COUNTCASH = "countCash";
    public static final String PROPERTY_OBPOSAPPCASHRECONCILLIST = "oBPOSAppCashReconcilList";
    public static final String PROPERTY_OBPOSPAYMENTMETHODCASHUPLIST = "oBPOSPaymentmethodcashupList";

    public OBPOSAppPayment() {
        setDefaultValue(PROPERTY_ACTIVE, true);
        setDefaultValue(PROPERTY_OVERRIDECONFIGURATION, false);
        setDefaultValue(PROPERTY_AUTOMATEMOVEMENTTOOTHERACCOUNT, false);
        setDefaultValue(PROPERTY_KEEPFIXEDAMOUNT, false);
        setDefaultValue(PROPERTY_ALLOWVARIABLEAMOUNT, false);
        setDefaultValue(PROPERTY_ALLOWNOTTOMOVE, false);
        setDefaultValue(PROPERTY_ALLOWMOVEEVERYTHING, false);
        setDefaultValue(PROPERTY_COUNTCASH, false);
        setDefaultValue(PROPERTY_OBPOSAPPCASHRECONCILLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSPAYMENTMETHODCASHUPLIST, new ArrayList<Object>());
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    public String getId() {
        return (String) get(PROPERTY_ID);
    }

    public void setId(String id) {
        set(PROPERTY_ID, id);
    }

    public Client getClient() {
        return (Client) get(PROPERTY_CLIENT);
    }

    public void setClient(Client client) {
        set(PROPERTY_CLIENT, client);
    }

    public Organization getOrganization() {
        return (Organization) get(PROPERTY_ORGANIZATION);
    }

    public void setOrganization(Organization organization) {
        set(PROPERTY_ORGANIZATION, organization);
    }

    public Boolean isActive() {
        return (Boolean) get(PROPERTY_ACTIVE);
    }

    public void setActive(Boolean active) {
        set(PROPERTY_ACTIVE, active);
    }

    public Date getCreationDate() {
        return (Date) get(PROPERTY_CREATIONDATE);
    }

    public void setCreationDate(Date creationDate) {
        set(PROPERTY_CREATIONDATE, creationDate);
    }

    public User getCreatedBy() {
        return (User) get(PROPERTY_CREATEDBY);
    }

    public void setCreatedBy(User createdBy) {
        set(PROPERTY_CREATEDBY, createdBy);
    }

    public Date getUpdated() {
        return (Date) get(PROPERTY_UPDATED);
    }

    public void setUpdated(Date updated) {
        set(PROPERTY_UPDATED, updated);
    }

    public User getUpdatedBy() {
        return (User) get(PROPERTY_UPDATEDBY);
    }

    public void setUpdatedBy(User updatedBy) {
        set(PROPERTY_UPDATEDBY, updatedBy);
    }

    public String getSearchKey() {
        return (String) get(PROPERTY_SEARCHKEY);
    }

    public void setSearchKey(String searchKey) {
        set(PROPERTY_SEARCHKEY, searchKey);
    }

    public FIN_FinancialAccount getFinancialAccount() {
        return (FIN_FinancialAccount) get(PROPERTY_FINANCIALACCOUNT);
    }

    public void setFinancialAccount(FIN_FinancialAccount financialAccount) {
        set(PROPERTY_FINANCIALACCOUNT, financialAccount);
    }

    public OBPOSApplications getObposApplications() {
        return (OBPOSApplications) get(PROPERTY_OBPOSAPPLICATIONS);
    }

    public void setObposApplications(OBPOSApplications obposApplications) {
        set(PROPERTY_OBPOSAPPLICATIONS, obposApplications);
    }

    public TerminalTypePaymentMethod getPaymentMethod() {
        return (TerminalTypePaymentMethod) get(PROPERTY_PAYMENTMETHOD);
    }

    public void setPaymentMethod(TerminalTypePaymentMethod paymentMethod) {
        set(PROPERTY_PAYMENTMETHOD, paymentMethod);
    }

    public CashManagementEvents getObretcoCmevents() {
        return (CashManagementEvents) get(PROPERTY_OBRETCOCMEVENTS);
    }

    public void setObretcoCmevents(CashManagementEvents obretcoCmevents) {
        set(PROPERTY_OBRETCOCMEVENTS, obretcoCmevents);
    }

    public String getCommercialName() {
        return (String) get(PROPERTY_COMMERCIALNAME);
    }

    public void setCommercialName(String commercialName) {
        set(PROPERTY_COMMERCIALNAME, commercialName);
    }

    public Long getLine() {
        return (Long) get(PROPERTY_LINE);
    }

    public void setLine(Long line) {
        set(PROPERTY_LINE, line);
    }

    public Boolean isOverrideconfiguration() {
        return (Boolean) get(PROPERTY_OVERRIDECONFIGURATION);
    }

    public void setOverrideconfiguration(Boolean overrideconfiguration) {
        set(PROPERTY_OVERRIDECONFIGURATION, overrideconfiguration);
    }

    public GLItem getCashDifferences() {
        return (GLItem) get(PROPERTY_CASHDIFFERENCES);
    }

    public void setCashDifferences(GLItem cashDifferences) {
        set(PROPERTY_CASHDIFFERENCES, cashDifferences);
    }

    public GLItem getGLItemForCashDropDeposit() {
        return (GLItem) get(PROPERTY_GLITEMFORCASHDROPDEPOSIT);
    }

    public void setGLItemForCashDropDeposit(GLItem gLItemForCashDropDeposit) {
        set(PROPERTY_GLITEMFORCASHDROPDEPOSIT, gLItemForCashDropDeposit);
    }

    public Boolean isAutomateMovementToOtherAccount() {
        return (Boolean) get(PROPERTY_AUTOMATEMOVEMENTTOOTHERACCOUNT);
    }

    public void setAutomateMovementToOtherAccount(Boolean automateMovementToOtherAccount) {
        set(PROPERTY_AUTOMATEMOVEMENTTOOTHERACCOUNT, automateMovementToOtherAccount);
    }

    public Boolean isKeepFixedAmount() {
        return (Boolean) get(PROPERTY_KEEPFIXEDAMOUNT);
    }

    public void setKeepFixedAmount(Boolean keepFixedAmount) {
        set(PROPERTY_KEEPFIXEDAMOUNT, keepFixedAmount);
    }

    public Long getAmount() {
        return (Long) get(PROPERTY_AMOUNT);
    }

    public void setAmount(Long amount) {
        set(PROPERTY_AMOUNT, amount);
    }

    public Boolean isAllowVariableAmount() {
        return (Boolean) get(PROPERTY_ALLOWVARIABLEAMOUNT);
    }

    public void setAllowVariableAmount(Boolean allowVariableAmount) {
        set(PROPERTY_ALLOWVARIABLEAMOUNT, allowVariableAmount);
    }

    public Boolean isAllowNotToMove() {
        return (Boolean) get(PROPERTY_ALLOWNOTTOMOVE);
    }

    public void setAllowNotToMove(Boolean allowNotToMove) {
        set(PROPERTY_ALLOWNOTTOMOVE, allowNotToMove);
    }

    public Boolean isAllowMoveEverything() {
        return (Boolean) get(PROPERTY_ALLOWMOVEEVERYTHING);
    }

    public void setAllowMoveEverything(Boolean allowMoveEverything) {
        set(PROPERTY_ALLOWMOVEEVERYTHING, allowMoveEverything);
    }

    public Boolean isCountCash() {
        return (Boolean) get(PROPERTY_COUNTCASH);
    }

    public void setCountCash(Boolean countCash) {
        set(PROPERTY_COUNTCASH, countCash);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSAppCashReconcil> getOBPOSAppCashReconcilList() {
      return (List<OBPOSAppCashReconcil>) get(PROPERTY_OBPOSAPPCASHRECONCILLIST);
    }

    public void setOBPOSAppCashReconcilList(List<OBPOSAppCashReconcil> oBPOSAppCashReconcilList) {
        set(PROPERTY_OBPOSAPPCASHRECONCILLIST, oBPOSAppCashReconcilList);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSPaymentMethodCashup> getOBPOSPaymentmethodcashupList() {
      return (List<OBPOSPaymentMethodCashup>) get(PROPERTY_OBPOSPAYMENTMETHODCASHUPLIST);
    }

    public void setOBPOSPaymentmethodcashupList(List<OBPOSPaymentMethodCashup> oBPOSPaymentmethodcashupList) {
        set(PROPERTY_OBPOSPAYMENTMETHODCASHUPLIST, oBPOSPaymentmethodcashupList);
    }

}
