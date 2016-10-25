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
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
/**
 * Entity class for entity OBPOS_Print_Template (stored in table OBPOS_Print_Template).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class PrintTemplate extends BaseOBObject implements Traceable, ClientEnabled, OrganizationEnabled, ActiveEnabled {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "OBPOS_Print_Template";
    public static final String ENTITY_NAME = "OBPOS_Print_Template";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CLIENT = "client";
    public static final String PROPERTY_ORGANIZATION = "organization";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    public static final String PROPERTY_CREATEDBY = "createdBy";
    public static final String PROPERTY_UPDATED = "updated";
    public static final String PROPERTY_UPDATEDBY = "updatedBy";
    public static final String PROPERTY_MODULE = "module";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_TEMPLATETYPE = "templateType";
    public static final String PROPERTY_TEMPLATEPATH = "templatePath";
    public static final String PROPERTY_ISPDF = "ispdf";
    public static final String PROPERTY_PRINTER = "printer";
    public static final String PROPERTY_OBPOSPRINTTEMPLATESUBREPLIST = "oBPOSPrintTemplateSubrepList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSTICKETTEMPLATEIDLIST = "organizationEMObposTicketTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSCASHUPTEMPLATEIDLIST = "organizationEMObposCashupTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSCLOSEDRECEIPTTEMPLATELIST = "organizationEMOBPOSClosedReceiptTemplateList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSINVOICETEMPLATEIDLIST = "organizationEMObposInvoiceTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSRETINVTEMPLATEIDLIST = "organizationEMObposRetInvTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSLAYAWAYTEMPLATEIDLIST = "organizationEMObposLayawayTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSRETURNTEMPLATEIDLIST = "organizationEMObposReturnTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSQUOTTEMPLATEIDLIST = "organizationEMObposQuotTemplateIDList";
    public static final String PROPERTY_ORGANIZATIONEMOBPOSCLOINVTEMPLATEIDLIST = "organizationEMObposCloInvTemplateIDList";

    public PrintTemplate() {
        setDefaultValue(PROPERTY_ACTIVE, true);
        setDefaultValue(PROPERTY_ISPDF, false);
        setDefaultValue(PROPERTY_OBPOSPRINTTEMPLATESUBREPLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSTICKETTEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSCASHUPTEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSCLOSEDRECEIPTTEMPLATELIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSINVOICETEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSRETINVTEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSLAYAWAYTEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSRETURNTEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSQUOTTEMPLATEIDLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_ORGANIZATIONEMOBPOSCLOINVTEMPLATEIDLIST, new ArrayList<Object>());
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

    public Module getModule() {
        return (Module) get(PROPERTY_MODULE);
    }

    public void setModule(Module module) {
        set(PROPERTY_MODULE, module);
    }

    public String getName() {
        return (String) get(PROPERTY_NAME);
    }

    public void setName(String name) {
        set(PROPERTY_NAME, name);
    }

    public String getTemplateType() {
        return (String) get(PROPERTY_TEMPLATETYPE);
    }

    public void setTemplateType(String templateType) {
        set(PROPERTY_TEMPLATETYPE, templateType);
    }

    public String getTemplatePath() {
        return (String) get(PROPERTY_TEMPLATEPATH);
    }

    public void setTemplatePath(String templatePath) {
        set(PROPERTY_TEMPLATEPATH, templatePath);
    }

    public Boolean isPdf() {
        return (Boolean) get(PROPERTY_ISPDF);
    }

    public void setPdf(Boolean ispdf) {
        set(PROPERTY_ISPDF, ispdf);
    }

    public Long getPrinter() {
        return (Long) get(PROPERTY_PRINTER);
    }

    public void setPrinter(Long printer) {
        set(PROPERTY_PRINTER, printer);
    }

    @SuppressWarnings("unchecked")
    public List<PrintTemplateSubrep> getOBPOSPrintTemplateSubrepList() {
      return (List<PrintTemplateSubrep>) get(PROPERTY_OBPOSPRINTTEMPLATESUBREPLIST);
    }

    public void setOBPOSPrintTemplateSubrepList(List<PrintTemplateSubrep> oBPOSPrintTemplateSubrepList) {
        set(PROPERTY_OBPOSPRINTTEMPLATESUBREPLIST, oBPOSPrintTemplateSubrepList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposTicketTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSTICKETTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposTicketTemplateIDList(List<Organization> organizationEMObposTicketTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSTICKETTEMPLATEIDLIST, organizationEMObposTicketTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposCashupTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSCASHUPTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposCashupTemplateIDList(List<Organization> organizationEMObposCashupTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSCASHUPTEMPLATEIDLIST, organizationEMObposCashupTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMOBPOSClosedReceiptTemplateList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSCLOSEDRECEIPTTEMPLATELIST);
    }

    public void setOrganizationEMOBPOSClosedReceiptTemplateList(List<Organization> organizationEMOBPOSClosedReceiptTemplateList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSCLOSEDRECEIPTTEMPLATELIST, organizationEMOBPOSClosedReceiptTemplateList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposInvoiceTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSINVOICETEMPLATEIDLIST);
    }

    public void setOrganizationEMObposInvoiceTemplateIDList(List<Organization> organizationEMObposInvoiceTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSINVOICETEMPLATEIDLIST, organizationEMObposInvoiceTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposRetInvTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSRETINVTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposRetInvTemplateIDList(List<Organization> organizationEMObposRetInvTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSRETINVTEMPLATEIDLIST, organizationEMObposRetInvTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposLayawayTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSLAYAWAYTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposLayawayTemplateIDList(List<Organization> organizationEMObposLayawayTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSLAYAWAYTEMPLATEIDLIST, organizationEMObposLayawayTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposReturnTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSRETURNTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposReturnTemplateIDList(List<Organization> organizationEMObposReturnTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSRETURNTEMPLATEIDLIST, organizationEMObposReturnTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposQuotTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSQUOTTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposQuotTemplateIDList(List<Organization> organizationEMObposQuotTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSQUOTTEMPLATEIDLIST, organizationEMObposQuotTemplateIDList);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationEMObposCloInvTemplateIDList() {
      return (List<Organization>) get(PROPERTY_ORGANIZATIONEMOBPOSCLOINVTEMPLATEIDLIST);
    }

    public void setOrganizationEMObposCloInvTemplateIDList(List<Organization> organizationEMObposCloInvTemplateIDList) {
        set(PROPERTY_ORGANIZATIONEMOBPOSCLOINVTEMPLATEIDLIST, organizationEMObposCloInvTemplateIDList);
    }

}
