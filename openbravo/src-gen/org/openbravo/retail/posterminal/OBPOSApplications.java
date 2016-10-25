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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryArchive;
/**
 * Entity class for entity OBPOS_Applications (stored in table OBPOS_APPLICATIONS).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class OBPOSApplications extends BaseOBObject implements Traceable, ClientEnabled, OrganizationEnabled, ActiveEnabled {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "OBPOS_APPLICATIONS";
    public static final String ENTITY_NAME = "OBPOS_Applications";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CLIENT = "client";
    public static final String PROPERTY_ORGANIZATION = "organization";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    public static final String PROPERTY_CREATEDBY = "createdBy";
    public static final String PROPERTY_UPDATED = "updated";
    public static final String PROPERTY_UPDATEDBY = "updatedBy";
    public static final String PROPERTY_SEARCHKEY = "searchKey";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_HARDWAREURL = "hardwareurl";
    public static final String PROPERTY_SCALEURL = "scaleurl";
    public static final String PROPERTY_ORDERDOCNOPREFIX = "orderdocnoPrefix";
    public static final String PROPERTY_OBPOSTERMINALTYPE = "obposTerminaltype";
    public static final String PROPERTY_QUOTATIONDOCNOPREFIX = "quotationdocnoPrefix";
    public static final String PROPERTY_DEFAULTWEBPOSTAB = "defaultwebpostab";
    public static final String PROPERTY_DEFAULTCUSTOMER = "defaultCustomer";
    public static final String PROPERTY_OBPOSCBPARTNERLOC = "obposCBpartnerLoc";
    public static final String PROPERTY_PRINTOFFLINE = "printoffline";
    public static final String PROPERTY_TERMINALKEY = "terminalKey";
    public static final String PROPERTY_ISLINKED = "islinked";
    public static final String PROPERTY_UNLINKDEVICE = "unlinkdevice";
    public static final String PROPERTY_LASTASSIGNEDNUM = "lastassignednum";
    public static final String PROPERTY_ISMASTER = "ismaster";
    public static final String PROPERTY_MASTERTERMINAL = "masterterminal";
    public static final String PROPERTY_QUOTATIONSLASTASSIGNEDNUM = "quotationslastassignednum";
    public static final String PROPERTY_CURRENTCACHESESSION = "currentCacheSession";
    public static final String PROPERTY_CIMPORTENTRYEMOBPOSPOSTERMINALLIST = "cIMPORTENTRYEMOBPOSPOSTerminalList";
    public static final String PROPERTY_CIMPORTENTRYARCHIVEEMOBPOSPOSTERMINALLIST = "cImportEntryArchiveEMOBPOSPOSTerminalList";
    public static final String PROPERTY_OBPOSAPPCASHUPLIST = "oBPOSAppCashupList";
    public static final String PROPERTY_OBPOSAPPPAYMENTLIST = "oBPOSAppPaymentList";
    public static final String PROPERTY_OBPOSAPPLICATIONSMASTERTERMINALIDLIST = "oBPOSApplicationsMasterterminalIDList";
    public static final String PROPERTY_OBPOSERRORSLIST = "oBPOSErrorsList";
    public static final String PROPERTY_OBPOSTERMINALACCESSLIST = "oBPOSTerminalAccessList";
    public static final String PROPERTY_ORDEREMOBPOSAPPLICATIONSIDLIST = "orderEMObposApplicationsIDList";

    public OBPOSApplications() {
        setDefaultValue(PROPERTY_ACTIVE, true);
        setDefaultValue(PROPERTY_DEFAULTWEBPOSTAB, "scan");
        setDefaultValue(PROPERTY_PRINTOFFLINE, false);
        setDefaultValue(PROPERTY_ISLINKED, false);
        setDefaultValue(PROPERTY_UNLINKDEVICE, false);
        setDefaultValue(PROPERTY_ISMASTER, false);
        setDefaultValue(PROPERTY_CIMPORTENTRYEMOBPOSPOSTERMINALLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_CIMPORTENTRYARCHIVEEMOBPOSPOSTERMINALLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSAPPCASHUPLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSAPPPAYMENTLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSAPPLICATIONSMASTERTERMINALIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSERRORSLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSTERMINALACCESSLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORDEREMOBPOSAPPLICATIONSIDLIST, new ArrayList<Object>());
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

    public String getName() {
        return (String) get(PROPERTY_NAME);
    }

    public void setName(String name) {
        set(PROPERTY_NAME, name);
    }

    public String getHardwareurl() {
        return (String) get(PROPERTY_HARDWAREURL);
    }

    public void setHardwareurl(String hardwareurl) {
        set(PROPERTY_HARDWAREURL, hardwareurl);
    }

    public String getScaleurl() {
        return (String) get(PROPERTY_SCALEURL);
    }

    public void setScaleurl(String scaleurl) {
        set(PROPERTY_SCALEURL, scaleurl);
    }

    public String getOrderdocnoPrefix() {
        return (String) get(PROPERTY_ORDERDOCNOPREFIX);
    }

    public void setOrderdocnoPrefix(String orderdocnoPrefix) {
        set(PROPERTY_ORDERDOCNOPREFIX, orderdocnoPrefix);
    }

    public TerminalType getObposTerminaltype() {
        return (TerminalType) get(PROPERTY_OBPOSTERMINALTYPE);
    }

    public void setObposTerminaltype(TerminalType obposTerminaltype) {
        set(PROPERTY_OBPOSTERMINALTYPE, obposTerminaltype);
    }

    public String getQuotationdocnoPrefix() {
        return (String) get(PROPERTY_QUOTATIONDOCNOPREFIX);
    }

    public void setQuotationdocnoPrefix(String quotationdocnoPrefix) {
        set(PROPERTY_QUOTATIONDOCNOPREFIX, quotationdocnoPrefix);
    }

    public String getDefaultwebpostab() {
        return (String) get(PROPERTY_DEFAULTWEBPOSTAB);
    }

    public void setDefaultwebpostab(String defaultwebpostab) {
        set(PROPERTY_DEFAULTWEBPOSTAB, defaultwebpostab);
    }

    public BusinessPartner getDefaultCustomer() {
        return (BusinessPartner) get(PROPERTY_DEFAULTCUSTOMER);
    }

    public void setDefaultCustomer(BusinessPartner defaultCustomer) {
        set(PROPERTY_DEFAULTCUSTOMER, defaultCustomer);
    }

    public Location getObposCBpartnerLoc() {
        return (Location) get(PROPERTY_OBPOSCBPARTNERLOC);
    }

    public void setObposCBpartnerLoc(Location obposCBpartnerLoc) {
        set(PROPERTY_OBPOSCBPARTNERLOC, obposCBpartnerLoc);
    }

    public Boolean isPrintoffline() {
        return (Boolean) get(PROPERTY_PRINTOFFLINE);
    }

    public void setPrintoffline(Boolean printoffline) {
        set(PROPERTY_PRINTOFFLINE, printoffline);
    }

    public String getTerminalKey() {
        return (String) get(PROPERTY_TERMINALKEY);
    }

    public void setTerminalKey(String terminalKey) {
        set(PROPERTY_TERMINALKEY, terminalKey);
    }

    public Boolean isLinked() {
        return (Boolean) get(PROPERTY_ISLINKED);
    }

    public void setLinked(Boolean islinked) {
        set(PROPERTY_ISLINKED, islinked);
    }

    public Boolean isUnlinkdevice() {
        return (Boolean) get(PROPERTY_UNLINKDEVICE);
    }

    public void setUnlinkdevice(Boolean unlinkdevice) {
        set(PROPERTY_UNLINKDEVICE, unlinkdevice);
    }

    public Long getLastassignednum() {
        return (Long) get(PROPERTY_LASTASSIGNEDNUM);
    }

    public void setLastassignednum(Long lastassignednum) {
        set(PROPERTY_LASTASSIGNEDNUM, lastassignednum);
    }

    public Boolean isMaster() {
        return (Boolean) get(PROPERTY_ISMASTER);
    }

    public void setMaster(Boolean ismaster) {
        set(PROPERTY_ISMASTER, ismaster);
    }

    public OBPOSApplications getMasterterminal() {
        return (OBPOSApplications) get(PROPERTY_MASTERTERMINAL);
    }

    public void setMasterterminal(OBPOSApplications masterterminal) {
        set(PROPERTY_MASTERTERMINAL, masterterminal);
    }

    public Long getQuotationslastassignednum() {
        return (Long) get(PROPERTY_QUOTATIONSLASTASSIGNEDNUM);
    }

    public void setQuotationslastassignednum(Long quotationslastassignednum) {
        set(PROPERTY_QUOTATIONSLASTASSIGNEDNUM, quotationslastassignednum);
    }

    public String getCurrentCacheSession() {
        return (String) get(PROPERTY_CURRENTCACHESESSION);
    }

    public void setCurrentCacheSession(String currentCacheSession) {
        set(PROPERTY_CURRENTCACHESESSION, currentCacheSession);
    }

    @SuppressWarnings("unchecked")
    public List<ImportEntry> getCIMPORTENTRYEMOBPOSPOSTerminalList() {
      return (List<ImportEntry>) get(PROPERTY_CIMPORTENTRYEMOBPOSPOSTERMINALLIST);
    }

    public void setCIMPORTENTRYEMOBPOSPOSTerminalList(List<ImportEntry> cIMPORTENTRYEMOBPOSPOSTerminalList) {
        set(PROPERTY_CIMPORTENTRYEMOBPOSPOSTERMINALLIST, cIMPORTENTRYEMOBPOSPOSTerminalList);
    }

    @SuppressWarnings("unchecked")
    public List<ImportEntryArchive> getCImportEntryArchiveEMOBPOSPOSTerminalList() {
      return (List<ImportEntryArchive>) get(PROPERTY_CIMPORTENTRYARCHIVEEMOBPOSPOSTERMINALLIST);
    }

    public void setCImportEntryArchiveEMOBPOSPOSTerminalList(List<ImportEntryArchive> cImportEntryArchiveEMOBPOSPOSTerminalList) {
        set(PROPERTY_CIMPORTENTRYARCHIVEEMOBPOSPOSTERMINALLIST, cImportEntryArchiveEMOBPOSPOSTerminalList);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSAppCashup> getOBPOSAppCashupList() {
      return (List<OBPOSAppCashup>) get(PROPERTY_OBPOSAPPCASHUPLIST);
    }

    public void setOBPOSAppCashupList(List<OBPOSAppCashup> oBPOSAppCashupList) {
        set(PROPERTY_OBPOSAPPCASHUPLIST, oBPOSAppCashupList);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSAppPayment> getOBPOSAppPaymentList() {
      return (List<OBPOSAppPayment>) get(PROPERTY_OBPOSAPPPAYMENTLIST);
    }

    public void setOBPOSAppPaymentList(List<OBPOSAppPayment> oBPOSAppPaymentList) {
        set(PROPERTY_OBPOSAPPPAYMENTLIST, oBPOSAppPaymentList);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSApplications> getOBPOSApplicationsMasterterminalIDList() {
      return (List<OBPOSApplications>) get(PROPERTY_OBPOSAPPLICATIONSMASTERTERMINALIDLIST);
    }

    public void setOBPOSApplicationsMasterterminalIDList(List<OBPOSApplications> oBPOSApplicationsMasterterminalIDList) {
        set(PROPERTY_OBPOSAPPLICATIONSMASTERTERMINALIDLIST, oBPOSApplicationsMasterterminalIDList);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSErrors> getOBPOSErrorsList() {
      return (List<OBPOSErrors>) get(PROPERTY_OBPOSERRORSLIST);
    }

    public void setOBPOSErrorsList(List<OBPOSErrors> oBPOSErrorsList) {
        set(PROPERTY_OBPOSERRORSLIST, oBPOSErrorsList);
    }

    @SuppressWarnings("unchecked")
    public List<TerminalAccess> getOBPOSTerminalAccessList() {
      return (List<TerminalAccess>) get(PROPERTY_OBPOSTERMINALACCESSLIST);
    }

    public void setOBPOSTerminalAccessList(List<TerminalAccess> oBPOSTerminalAccessList) {
        set(PROPERTY_OBPOSTERMINALACCESSLIST, oBPOSTerminalAccessList);
    }

    @SuppressWarnings("unchecked")
    public List<Order> getOrderEMObposApplicationsIDList() {
      return (List<Order>) get(PROPERTY_ORDEREMOBPOSAPPLICATIONSIDLIST);
    }

    public void setOrderEMObposApplicationsIDList(List<Order> orderEMObposApplicationsIDList) {
        set(PROPERTY_ORDEREMOBPOSAPPLICATIONSIDLIST, orderEMObposApplicationsIDList);
    }

}
