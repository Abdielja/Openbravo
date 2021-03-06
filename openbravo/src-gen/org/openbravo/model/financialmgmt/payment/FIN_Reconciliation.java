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
package org.openbravo.model.financialmgmt.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openbravo.advpaymentmngt.APRM_FinaccTransactionV;
import org.openbravo.advpaymentmngt.APRM_Finacc_Trx_Full_Acct_V;
import org.openbravo.advpaymentmngt.APRM_Reconciliation_v;
import org.openbravo.base.structure.ActiveEnabled;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.structure.Traceable;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.OBPOSAppCashReconcil;
/**
 * Entity class for entity FIN_Reconciliation (stored in table FIN_Reconciliation).
 *
 * NOTE: This class should not be instantiated directly. To instantiate this
 * class the {@link org.openbravo.base.provider.OBProvider} should be used.
 */
public class FIN_Reconciliation extends BaseOBObject implements Traceable, ClientEnabled, OrganizationEnabled, ActiveEnabled {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "FIN_Reconciliation";
    public static final String ENTITY_NAME = "FIN_Reconciliation";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CLIENT = "client";
    public static final String PROPERTY_ORGANIZATION = "organization";
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    public static final String PROPERTY_CREATEDBY = "createdBy";
    public static final String PROPERTY_UPDATED = "updated";
    public static final String PROPERTY_UPDATEDBY = "updatedBy";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_ACCOUNT = "account";
    public static final String PROPERTY_DOCUMENTNO = "documentNo";
    public static final String PROPERTY_DOCUMENTTYPE = "documentType";
    public static final String PROPERTY_ENDINGDATE = "endingDate";
    public static final String PROPERTY_TRANSACTIONDATE = "transactionDate";
    public static final String PROPERTY_ENDINGBALANCE = "endingBalance";
    public static final String PROPERTY_STARTINGBALANCE = "startingbalance";
    public static final String PROPERTY_DOCUMENTSTATUS = "documentStatus";
    public static final String PROPERTY_PROCESSNOW = "processNow";
    public static final String PROPERTY_PROCESSED = "processed";
    public static final String PROPERTY_POSTED = "posted";
    public static final String PROPERTY_PRINTDETAILED = "printdetailed";
    public static final String PROPERTY_PRINTSUMMARY = "printsummary";
    public static final String PROPERTY_APRMPROCESSRECONCILIATION = "aPRMProcessReconciliation";
    public static final String PROPERTY_APRMPRINTDETAILED = "aPRMPrintDetailed";
    public static final String PROPERTY_APRMPRINTSUMMARY = "aPRMPrintSummary";
    public static final String PROPERTY_APRMPROCESSREC = "aprmProcessRec";
    public static final String PROPERTY_APRMPROCESSRECONCILIATIONFORCE = "aPRMProcessReconciliationForce";
    public static final String PROPERTY__COMPUTEDCOLUMNS = "_computedColumns";
    public static final String PROPERTY_APRMFINACCTRANSACTIONVLIST = "aPRMFinaccTransactionVList";
    public static final String PROPERTY_APRMFINACCTRXFULLACCTVLIST = "aPRMFinaccTrxFullAcctVList";
    public static final String PROPERTY_APRMRECONCILIATIONLIST = "aPRMReconciliationList";
    public static final String PROPERTY_FINBANKSTATEMENTLIST = "fINBankStatementList";
    public static final String PROPERTY_FINFINACCTRANSACTIONLIST = "fINFinaccTransactionList";
    public static final String PROPERTY_FINRECONCILIATIONLINETEMPLIST = "fINReconciliationLineTempList";
    public static final String PROPERTY_FINRECONCILIATIONLINEVLIST = "fINReconciliationLineVList";
    public static final String PROPERTY_OBPOSAPPCASHRECONCILLIST = "oBPOSAppCashReconcilList";


    // Computed columns properties, these properties cannot be directly accessed, they need
    // to be read through _commputedColumns proxy. They cannot be directly used in HQL, OBQuery
    // nor OBCriteria. 
    public static final String COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITITEMSAMOUNT = "aPRMOutstandingDepositItemsAmount";
    public static final String COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITSITEMNO = "aPRMOutstandingDepositsItemNo";
    public static final String COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMNO = "aPRMOutstandingPaymentsItemNo";
    public static final String COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMSAMOUNT = "aPRMOutstandingPaymentsItemsAmount";
    public static final String COMPUTED_COLUMN_APRMRECONCILEDITEMAMOUNT = "aPRMReconciledItemAmount";
    public static final String COMPUTED_COLUMN_APRMRECONCILEDITEMNO = "aPRMReconciledItemNo";
    public static final String COMPUTED_COLUMN_APRMUNRECONCILEDITEMAMOUNT = "aPRMUnReconciledItemAmount";
    public static final String COMPUTED_COLUMN_APRMUNRECONCILEDITEMNO = "aPRMUnReconciledItemNo";

    public FIN_Reconciliation() {
        setDefaultValue(PROPERTY_CREATIONDATE, new Date());
        setDefaultValue(PROPERTY_UPDATED, new Date());
        setDefaultValue(PROPERTY_ACTIVE, true);
        setDefaultValue(PROPERTY_ENDINGBALANCE, new BigDecimal(0));
        setDefaultValue(PROPERTY_STARTINGBALANCE, new BigDecimal(0));
        setDefaultValue(PROPERTY_PROCESSNOW, false);
        setDefaultValue(PROPERTY_PROCESSED, false);
        setDefaultValue(PROPERTY_POSTED, "N");
        setDefaultValue(PROPERTY_PRINTDETAILED, false);
        setDefaultValue(PROPERTY_PRINTSUMMARY, false);
        setDefaultValue(PROPERTY_APRMPROCESSRECONCILIATION, "P");
        setDefaultValue(PROPERTY_APRMPRINTDETAILED, false);
        setDefaultValue(PROPERTY_APRMPRINTSUMMARY, false);
        setDefaultValue(PROPERTY_APRMPROCESSREC, "P");
        setDefaultValue(PROPERTY_APRMPROCESSRECONCILIATIONFORCE, "P");
        setDefaultValue(PROPERTY_APRMFINACCTRANSACTIONVLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_APRMFINACCTRXFULLACCTVLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_APRMRECONCILIATIONLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_FINBANKSTATEMENTLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_FINFINACCTRANSACTIONLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_FINRECONCILIATIONLINETEMPLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_FINRECONCILIATIONLINEVLIST, new ArrayList<Object>());
        setDefaultValue(PROPERTY_OBPOSAPPCASHRECONCILLIST, new ArrayList<Object>());
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

    public Boolean isActive() {
        return (Boolean) get(PROPERTY_ACTIVE);
    }

    public void setActive(Boolean active) {
        set(PROPERTY_ACTIVE, active);
    }

    public FIN_FinancialAccount getAccount() {
        return (FIN_FinancialAccount) get(PROPERTY_ACCOUNT);
    }

    public void setAccount(FIN_FinancialAccount account) {
        set(PROPERTY_ACCOUNT, account);
    }

    public String getDocumentNo() {
        return (String) get(PROPERTY_DOCUMENTNO);
    }

    public void setDocumentNo(String documentNo) {
        set(PROPERTY_DOCUMENTNO, documentNo);
    }

    public DocumentType getDocumentType() {
        return (DocumentType) get(PROPERTY_DOCUMENTTYPE);
    }

    public void setDocumentType(DocumentType documentType) {
        set(PROPERTY_DOCUMENTTYPE, documentType);
    }

    public Date getEndingDate() {
        return (Date) get(PROPERTY_ENDINGDATE);
    }

    public void setEndingDate(Date endingDate) {
        set(PROPERTY_ENDINGDATE, endingDate);
    }

    public Date getTransactionDate() {
        return (Date) get(PROPERTY_TRANSACTIONDATE);
    }

    public void setTransactionDate(Date transactionDate) {
        set(PROPERTY_TRANSACTIONDATE, transactionDate);
    }

    public BigDecimal getEndingBalance() {
        return (BigDecimal) get(PROPERTY_ENDINGBALANCE);
    }

    public void setEndingBalance(BigDecimal endingBalance) {
        set(PROPERTY_ENDINGBALANCE, endingBalance);
    }

    public BigDecimal getStartingbalance() {
        return (BigDecimal) get(PROPERTY_STARTINGBALANCE);
    }

    public void setStartingbalance(BigDecimal startingbalance) {
        set(PROPERTY_STARTINGBALANCE, startingbalance);
    }

    public String getDocumentStatus() {
        return (String) get(PROPERTY_DOCUMENTSTATUS);
    }

    public void setDocumentStatus(String documentStatus) {
        set(PROPERTY_DOCUMENTSTATUS, documentStatus);
    }

    public Boolean isProcessNow() {
        return (Boolean) get(PROPERTY_PROCESSNOW);
    }

    public void setProcessNow(Boolean processNow) {
        set(PROPERTY_PROCESSNOW, processNow);
    }

    public Boolean isProcessed() {
        return (Boolean) get(PROPERTY_PROCESSED);
    }

    public void setProcessed(Boolean processed) {
        set(PROPERTY_PROCESSED, processed);
    }

    public String getPosted() {
        return (String) get(PROPERTY_POSTED);
    }

    public void setPosted(String posted) {
        set(PROPERTY_POSTED, posted);
    }

    public Boolean isPrintdetailed() {
        return (Boolean) get(PROPERTY_PRINTDETAILED);
    }

    public void setPrintdetailed(Boolean printdetailed) {
        set(PROPERTY_PRINTDETAILED, printdetailed);
    }

    public Boolean isPrintsummary() {
        return (Boolean) get(PROPERTY_PRINTSUMMARY);
    }

    public void setPrintsummary(Boolean printsummary) {
        set(PROPERTY_PRINTSUMMARY, printsummary);
    }

    public String getAPRMProcessReconciliation() {
        return (String) get(PROPERTY_APRMPROCESSRECONCILIATION);
    }

    public void setAPRMProcessReconciliation(String aPRMProcessReconciliation) {
        set(PROPERTY_APRMPROCESSRECONCILIATION, aPRMProcessReconciliation);
    }

    public Boolean isAPRMPrintDetailed() {
        return (Boolean) get(PROPERTY_APRMPRINTDETAILED);
    }

    public void setAPRMPrintDetailed(Boolean aPRMPrintDetailed) {
        set(PROPERTY_APRMPRINTDETAILED, aPRMPrintDetailed);
    }

    public Boolean isAPRMPrintSummary() {
        return (Boolean) get(PROPERTY_APRMPRINTSUMMARY);
    }

    public void setAPRMPrintSummary(Boolean aPRMPrintSummary) {
        set(PROPERTY_APRMPRINTSUMMARY, aPRMPrintSummary);
    }

    public String getAprmProcessRec() {
        return (String) get(PROPERTY_APRMPROCESSREC);
    }

    public void setAprmProcessRec(String aprmProcessRec) {
        set(PROPERTY_APRMPROCESSREC, aprmProcessRec);
    }

    public String getAPRMProcessReconciliationForce() {
        return (String) get(PROPERTY_APRMPROCESSRECONCILIATIONFORCE);
    }

    public void setAPRMProcessReconciliationForce(String aPRMProcessReconciliationForce) {
        set(PROPERTY_APRMPROCESSRECONCILIATIONFORCE, aPRMProcessReconciliationForce);
    }

    public BigDecimal getAPRMOutstandingDepositItemsAmount() {
        return (BigDecimal) get(COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITITEMSAMOUNT);
    }

    public void setAPRMOutstandingDepositItemsAmount(BigDecimal aPRMOutstandingDepositItemsAmount) {
        set(COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITITEMSAMOUNT, aPRMOutstandingDepositItemsAmount);
    }

    public Long getAPRMOutstandingDepositsItemNo() {
        return (Long) get(COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITSITEMNO);
    }

    public void setAPRMOutstandingDepositsItemNo(Long aPRMOutstandingDepositsItemNo) {
        set(COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITSITEMNO, aPRMOutstandingDepositsItemNo);
    }

    public Long getAPRMOutstandingPaymentsItemNo() {
        return (Long) get(COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMNO);
    }

    public void setAPRMOutstandingPaymentsItemNo(Long aPRMOutstandingPaymentsItemNo) {
        set(COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMNO, aPRMOutstandingPaymentsItemNo);
    }

    public BigDecimal getAPRMOutstandingPaymentsItemsAmount() {
        return (BigDecimal) get(COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMSAMOUNT);
    }

    public void setAPRMOutstandingPaymentsItemsAmount(BigDecimal aPRMOutstandingPaymentsItemsAmount) {
        set(COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMSAMOUNT, aPRMOutstandingPaymentsItemsAmount);
    }

    public BigDecimal getAPRMReconciledItemAmount() {
        return (BigDecimal) get(COMPUTED_COLUMN_APRMRECONCILEDITEMAMOUNT);
    }

    public void setAPRMReconciledItemAmount(BigDecimal aPRMReconciledItemAmount) {
        set(COMPUTED_COLUMN_APRMRECONCILEDITEMAMOUNT, aPRMReconciledItemAmount);
    }

    public Long getAPRMReconciledItemNo() {
        return (Long) get(COMPUTED_COLUMN_APRMRECONCILEDITEMNO);
    }

    public void setAPRMReconciledItemNo(Long aPRMReconciledItemNo) {
        set(COMPUTED_COLUMN_APRMRECONCILEDITEMNO, aPRMReconciledItemNo);
    }

    public BigDecimal getAPRMUnReconciledItemAmount() {
        return (BigDecimal) get(COMPUTED_COLUMN_APRMUNRECONCILEDITEMAMOUNT);
    }

    public void setAPRMUnReconciledItemAmount(BigDecimal aPRMUnReconciledItemAmount) {
        set(COMPUTED_COLUMN_APRMUNRECONCILEDITEMAMOUNT, aPRMUnReconciledItemAmount);
    }

    public Long getAPRMUnReconciledItemNo() {
        return (Long) get(COMPUTED_COLUMN_APRMUNRECONCILEDITEMNO);
    }

    public void setAPRMUnReconciledItemNo(Long aPRMUnReconciledItemNo) {
        set(COMPUTED_COLUMN_APRMUNRECONCILEDITEMNO, aPRMUnReconciledItemNo);
    }

    public FIN_Reconciliation_ComputedColumns get_computedColumns() {
        return (FIN_Reconciliation_ComputedColumns) get(PROPERTY__COMPUTEDCOLUMNS);
    }

    public void set_computedColumns(FIN_Reconciliation_ComputedColumns _computedColumns) {
        set(PROPERTY__COMPUTEDCOLUMNS, _computedColumns);
    }

    @SuppressWarnings("unchecked")
    public List<APRM_FinaccTransactionV> getAPRMFinaccTransactionVList() {
      return (List<APRM_FinaccTransactionV>) get(PROPERTY_APRMFINACCTRANSACTIONVLIST);
    }

    public void setAPRMFinaccTransactionVList(List<APRM_FinaccTransactionV> aPRMFinaccTransactionVList) {
        set(PROPERTY_APRMFINACCTRANSACTIONVLIST, aPRMFinaccTransactionVList);
    }

    @SuppressWarnings("unchecked")
    public List<APRM_Finacc_Trx_Full_Acct_V> getAPRMFinaccTrxFullAcctVList() {
      return (List<APRM_Finacc_Trx_Full_Acct_V>) get(PROPERTY_APRMFINACCTRXFULLACCTVLIST);
    }

    public void setAPRMFinaccTrxFullAcctVList(List<APRM_Finacc_Trx_Full_Acct_V> aPRMFinaccTrxFullAcctVList) {
        set(PROPERTY_APRMFINACCTRXFULLACCTVLIST, aPRMFinaccTrxFullAcctVList);
    }

    @SuppressWarnings("unchecked")
    public List<APRM_Reconciliation_v> getAPRMReconciliationList() {
      return (List<APRM_Reconciliation_v>) get(PROPERTY_APRMRECONCILIATIONLIST);
    }

    public void setAPRMReconciliationList(List<APRM_Reconciliation_v> aPRMReconciliationList) {
        set(PROPERTY_APRMRECONCILIATIONLIST, aPRMReconciliationList);
    }

    @SuppressWarnings("unchecked")
    public List<FIN_BankStatement> getFINBankStatementList() {
      return (List<FIN_BankStatement>) get(PROPERTY_FINBANKSTATEMENTLIST);
    }

    public void setFINBankStatementList(List<FIN_BankStatement> fINBankStatementList) {
        set(PROPERTY_FINBANKSTATEMENTLIST, fINBankStatementList);
    }

    @SuppressWarnings("unchecked")
    public List<FIN_FinaccTransaction> getFINFinaccTransactionList() {
      return (List<FIN_FinaccTransaction>) get(PROPERTY_FINFINACCTRANSACTIONLIST);
    }

    public void setFINFinaccTransactionList(List<FIN_FinaccTransaction> fINFinaccTransactionList) {
        set(PROPERTY_FINFINACCTRANSACTIONLIST, fINFinaccTransactionList);
    }

    @SuppressWarnings("unchecked")
    public List<FIN_ReconciliationLineTemp> getFINReconciliationLineTempList() {
      return (List<FIN_ReconciliationLineTemp>) get(PROPERTY_FINRECONCILIATIONLINETEMPLIST);
    }

    public void setFINReconciliationLineTempList(List<FIN_ReconciliationLineTemp> fINReconciliationLineTempList) {
        set(PROPERTY_FINRECONCILIATIONLINETEMPLIST, fINReconciliationLineTempList);
    }

    @SuppressWarnings("unchecked")
    public List<FIN_ReconciliationLine_v> getFINReconciliationLineVList() {
      return (List<FIN_ReconciliationLine_v>) get(PROPERTY_FINRECONCILIATIONLINEVLIST);
    }

    public void setFINReconciliationLineVList(List<FIN_ReconciliationLine_v> fINReconciliationLineVList) {
        set(PROPERTY_FINRECONCILIATIONLINEVLIST, fINReconciliationLineVList);
    }

    @SuppressWarnings("unchecked")
    public List<OBPOSAppCashReconcil> getOBPOSAppCashReconcilList() {
      return (List<OBPOSAppCashReconcil>) get(PROPERTY_OBPOSAPPCASHRECONCILLIST);
    }

    public void setOBPOSAppCashReconcilList(List<OBPOSAppCashReconcil> oBPOSAppCashReconcilList) {
        set(PROPERTY_OBPOSAPPCASHRECONCILLIST, oBPOSAppCashReconcilList);
    }


    @Override
    public Object get(String propName) {
      if (COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITITEMSAMOUNT.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMOutstandingDepositItemsAmount();
      }
      if (COMPUTED_COLUMN_APRMOUTSTANDINGDEPOSITSITEMNO.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMOutstandingDepositsItemNo();
      }
      if (COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMNO.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMOutstandingPaymentsItemNo();
      }
      if (COMPUTED_COLUMN_APRMOUTSTANDINGPAYMENTSITEMSAMOUNT.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMOutstandingPaymentsItemsAmount();
      }
      if (COMPUTED_COLUMN_APRMRECONCILEDITEMAMOUNT.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMReconciledItemAmount();
      }
      if (COMPUTED_COLUMN_APRMRECONCILEDITEMNO.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMReconciledItemNo();
      }
      if (COMPUTED_COLUMN_APRMUNRECONCILEDITEMAMOUNT.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMUnReconciledItemAmount();
      }
      if (COMPUTED_COLUMN_APRMUNRECONCILEDITEMNO.equals(propName)) {
        if (get_computedColumns() == null) {
          return null;
        }
        return get_computedColumns().getAPRMUnReconciledItemNo();
      }
    
      return super.get(propName);
    }
}
