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

import java.math.BigDecimal;
import java.util.Date;

import org.openbravo.base.structure.ActiveEnabled;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.structure.Traceable;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
/**
 * Entity class for entity OBPOS_Paymentmethodcashup (stored in table OBPOS_paymentmethodcashup).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class OBPOSPaymentMethodCashup extends BaseOBObject implements Traceable, ClientEnabled, OrganizationEnabled, ActiveEnabled {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "OBPOS_paymentmethodcashup";
    public static final String ENTITY_NAME = "OBPOS_Paymentmethodcashup";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    public static final String PROPERTY_CREATEDBY = "createdBy";
    public static final String PROPERTY_UPDATED = "updated";
    public static final String PROPERTY_UPDATEDBY = "updatedBy";
    public static final String PROPERTY_PAYMENTTYPE = "paymentType";
    public static final String PROPERTY_SEARCHKEY = "searchkey";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_STARTINGCASH = "startingcash";
    public static final String PROPERTY_TOTALSALES = "totalsales";
    public static final String PROPERTY_TOTALRETURNS = "totalreturns";
    public static final String PROPERTY_RATE = "rate";
    public static final String PROPERTY_CASHUP = "cashUp";
    public static final String PROPERTY_ISOCODE = "isocode";
    public static final String PROPERTY_AMOUNTTOKEEP = "amountToKeep";
    public static final String PROPERTY_CLIENT = "client";
    public static final String PROPERTY_ORGANIZATION = "organization";
    public static final String PROPERTY_TOTALDEPOSITS = "totalDeposits";
    public static final String PROPERTY_TOTALDROPS = "totalDrops";

    public OBPOSPaymentMethodCashup() {
        setDefaultValue(PROPERTY_ACTIVE, true);
        setDefaultValue(PROPERTY_TOTALDEPOSITS, new BigDecimal(0));
        setDefaultValue(PROPERTY_TOTALDROPS, new BigDecimal(0));
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

    public OBPOSAppPayment getPaymentType() {
        return (OBPOSAppPayment) get(PROPERTY_PAYMENTTYPE);
    }

    public void setPaymentType(OBPOSAppPayment paymentType) {
        set(PROPERTY_PAYMENTTYPE, paymentType);
    }

    public String getSearchkey() {
        return (String) get(PROPERTY_SEARCHKEY);
    }

    public void setSearchkey(String searchkey) {
        set(PROPERTY_SEARCHKEY, searchkey);
    }

    public String getName() {
        return (String) get(PROPERTY_NAME);
    }

    public void setName(String name) {
        set(PROPERTY_NAME, name);
    }

    public BigDecimal getStartingcash() {
        return (BigDecimal) get(PROPERTY_STARTINGCASH);
    }

    public void setStartingcash(BigDecimal startingcash) {
        set(PROPERTY_STARTINGCASH, startingcash);
    }

    public BigDecimal getTotalsales() {
        return (BigDecimal) get(PROPERTY_TOTALSALES);
    }

    public void setTotalsales(BigDecimal totalsales) {
        set(PROPERTY_TOTALSALES, totalsales);
    }

    public BigDecimal getTotalreturns() {
        return (BigDecimal) get(PROPERTY_TOTALRETURNS);
    }

    public void setTotalreturns(BigDecimal totalreturns) {
        set(PROPERTY_TOTALRETURNS, totalreturns);
    }

    public BigDecimal getRate() {
        return (BigDecimal) get(PROPERTY_RATE);
    }

    public void setRate(BigDecimal rate) {
        set(PROPERTY_RATE, rate);
    }

    public OBPOSAppCashup getCashUp() {
        return (OBPOSAppCashup) get(PROPERTY_CASHUP);
    }

    public void setCashUp(OBPOSAppCashup cashUp) {
        set(PROPERTY_CASHUP, cashUp);
    }

    public String getIsocode() {
        return (String) get(PROPERTY_ISOCODE);
    }

    public void setIsocode(String isocode) {
        set(PROPERTY_ISOCODE, isocode);
    }

    public BigDecimal getAmountToKeep() {
        return (BigDecimal) get(PROPERTY_AMOUNTTOKEEP);
    }

    public void setAmountToKeep(BigDecimal amountToKeep) {
        set(PROPERTY_AMOUNTTOKEEP, amountToKeep);
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

    public BigDecimal getTotalDeposits() {
        return (BigDecimal) get(PROPERTY_TOTALDEPOSITS);
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        set(PROPERTY_TOTALDEPOSITS, totalDeposits);
    }

    public BigDecimal getTotalDrops() {
        return (BigDecimal) get(PROPERTY_TOTALDROPS);
    }

    public void setTotalDrops(BigDecimal totalDrops) {
        set(PROPERTY_TOTALDROPS, totalDrops);
    }

}
