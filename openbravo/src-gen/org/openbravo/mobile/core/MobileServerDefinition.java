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
package org.openbravo.mobile.core;

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
/**
 * Entity class for entity OBMOBC_SERVER_DEFINITION (stored in table OBMOBC_SERVER_DEFINITION).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class MobileServerDefinition extends BaseOBObject implements Traceable, ClientEnabled, OrganizationEnabled, ActiveEnabled {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "OBMOBC_SERVER_DEFINITION";
    public static final String ENTITY_NAME = "OBMOBC_SERVER_DEFINITION";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CLIENT = "client";
    public static final String PROPERTY_ORGANIZATION = "organization";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    public static final String PROPERTY_CREATEDBY = "createdBy";
    public static final String PROPERTY_UPDATED = "updated";
    public static final String PROPERTY_UPDATEDBY = "updatedBy";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_SERVERTYPE = "serverType";
    public static final String PROPERTY_URL = "uRL";
    public static final String PROPERTY_PRIORITY = "priority";
    public static final String PROPERTY_ALLORGS = "allorgs";
    public static final String PROPERTY_ALLSERVICES = "allservices";
    public static final String PROPERTY_MOBILESERVERKEY = "mobileServerKey";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_PINGSERVICE = "pingservice";
    public static final String PROPERTY_TRIGGERSTATE = "triggerstate";
    public static final String PROPERTY_SERVICESELECTION = "serviceSelection";
    public static final String PROPERTY_OBMOBCSERVERORGSLIST = "oBMOBCSERVERORGSList";
    public static final String PROPERTY_OBMOBCSERVERSERVICESLIST = "oBMOBCSERVERSERVICESList";

    public MobileServerDefinition() {
        setDefaultValue(PROPERTY_ACTIVE, true);
        setDefaultValue(PROPERTY_PRIORITY, (long) 10);
        setDefaultValue(PROPERTY_ALLORGS, true);
        setDefaultValue(PROPERTY_ALLSERVICES, true);
        setDefaultValue(PROPERTY_STATUS, "ON");
        setDefaultValue(PROPERTY_TRIGGERSTATE, true);
        setDefaultValue(PROPERTY_SERVICESELECTION, "Y");
        setDefaultValue(PROPERTY_OBMOBCSERVERORGSLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBMOBCSERVERSERVICESLIST, new ArrayList<Object>());
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

    public String getName() {
        return (String) get(PROPERTY_NAME);
    }

    public void setName(String name) {
        set(PROPERTY_NAME, name);
    }

    public String getDescription() {
        return (String) get(PROPERTY_DESCRIPTION);
    }

    public void setDescription(String description) {
        set(PROPERTY_DESCRIPTION, description);
    }

    public String getServerType() {
        return (String) get(PROPERTY_SERVERTYPE);
    }

    public void setServerType(String serverType) {
        set(PROPERTY_SERVERTYPE, serverType);
    }

    public String getURL() {
        return (String) get(PROPERTY_URL);
    }

    public void setURL(String uRL) {
        set(PROPERTY_URL, uRL);
    }

    public Long getPriority() {
        return (Long) get(PROPERTY_PRIORITY);
    }

    public void setPriority(Long priority) {
        set(PROPERTY_PRIORITY, priority);
    }

    public Boolean isAllorgs() {
        return (Boolean) get(PROPERTY_ALLORGS);
    }

    public void setAllorgs(Boolean allorgs) {
        set(PROPERTY_ALLORGS, allorgs);
    }

    public Boolean isAllservices() {
        return (Boolean) get(PROPERTY_ALLSERVICES);
    }

    public void setAllservices(Boolean allservices) {
        set(PROPERTY_ALLSERVICES, allservices);
    }

    public String getMobileServerKey() {
        return (String) get(PROPERTY_MOBILESERVERKEY);
    }

    public void setMobileServerKey(String mobileServerKey) {
        set(PROPERTY_MOBILESERVERKEY, mobileServerKey);
    }

    public String getStatus() {
        return (String) get(PROPERTY_STATUS);
    }

    public void setStatus(String status) {
        set(PROPERTY_STATUS, status);
    }

    public String getPingservice() {
        return (String) get(PROPERTY_PINGSERVICE);
    }

    public void setPingservice(String pingservice) {
        set(PROPERTY_PINGSERVICE, pingservice);
    }

    public Boolean isTriggerstate() {
        return (Boolean) get(PROPERTY_TRIGGERSTATE);
    }

    public void setTriggerstate(Boolean triggerstate) {
        set(PROPERTY_TRIGGERSTATE, triggerstate);
    }

    public String getServiceSelection() {
        return (String) get(PROPERTY_SERVICESELECTION);
    }

    public void setServiceSelection(String serviceSelection) {
        set(PROPERTY_SERVICESELECTION, serviceSelection);
    }

    @SuppressWarnings("unchecked")
    public List<MobileServerOrganization> getOBMOBCSERVERORGSList() {
      return (List<MobileServerOrganization>) get(PROPERTY_OBMOBCSERVERORGSLIST);
    }

    public void setOBMOBCSERVERORGSList(List<MobileServerOrganization> oBMOBCSERVERORGSList) {
        set(PROPERTY_OBMOBCSERVERORGSLIST, oBMOBCSERVERORGSList);
    }

    @SuppressWarnings("unchecked")
    public List<MobileServerService> getOBMOBCSERVERSERVICESList() {
      return (List<MobileServerService>) get(PROPERTY_OBMOBCSERVERSERVICESLIST);
    }

    public void setOBMOBCSERVERSERVICESList(List<MobileServerService> oBMOBCSERVERSERVICESList) {
        set(PROPERTY_OBMOBCSERVERSERVICESLIST, oBMOBCSERVERSERVICESList);
    }

}
