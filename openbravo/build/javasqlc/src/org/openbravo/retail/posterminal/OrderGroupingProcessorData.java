//Sqlc generated V1.O00-1
package org.openbravo.retail.posterminal;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.service.db.QueryTimeOutUtil;
import org.openbravo.database.SessionInfo;
import java.util.*;

class OrderGroupingProcessorData implements FieldProvider {
static Logger log4j = Logger.getLogger(OrderGroupingProcessorData.class);
  private String InitRecordNumber="0";
  public String cOrderlineId;
  public String cOrderId;
  public String cInvoiceId;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("c_orderline_id") || fieldName.equals("cOrderlineId"))
      return cOrderlineId;
    else if (fieldName.equalsIgnoreCase("c_order_id") || fieldName.equals("cOrderId"))
      return cOrderId;
    else if (fieldName.equalsIgnoreCase("c_invoice_id") || fieldName.equals("cInvoiceId"))
      return cInvoiceId;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

/**
select orderlines with multiple shipmentlines
 */
  public static OrderGroupingProcessorData[] selectSplitOrderLines(ConnectionProvider connectionProvider, String cashupId)    throws ServletException {
    return selectSplitOrderLines(connectionProvider, cashupId, 0, 0);
  }

/**
select orderlines with multiple shipmentlines
 */
  public static OrderGroupingProcessorData[] selectSplitOrderLines(ConnectionProvider connectionProvider, String cashupId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        select ol.c_orderline_id, o.c_order_id, '' as c_invoice_id" +
      "        from c_orderline ol, c_order o" +
      "        where ol.c_order_id = o.c_order_id" +
      "        and (select count(1) from m_inoutline il where il.c_orderline_id = ol.c_orderline_id) > 1" +
      "        and o.em_obpos_app_cashup_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cashupId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        OrderGroupingProcessorData objectOrderGroupingProcessorData = new OrderGroupingProcessorData();
        objectOrderGroupingProcessorData.cOrderlineId = UtilSql.getValue(result, "c_orderline_id");
        objectOrderGroupingProcessorData.cOrderId = UtilSql.getValue(result, "c_order_id");
        objectOrderGroupingProcessorData.cInvoiceId = UtilSql.getValue(result, "c_invoice_id");
        objectOrderGroupingProcessorData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectOrderGroupingProcessorData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    OrderGroupingProcessorData objectOrderGroupingProcessorData[] = new OrderGroupingProcessorData[vector.size()];
    vector.copyInto(objectOrderGroupingProcessorData);
    return(objectOrderGroupingProcessorData);
  }

/**
Insert invoice headers grouping orders
 */
  public static int insertHeaderGrouping(ConnectionProvider connectionProvider, String userId, String executionId, String currentDate, String cashupId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoice" +
      "        (c_invoice_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, issotrx," +
      "         documentno, docstatus, docaction, processing, processed, posted, c_doctype_id, c_doctypetarget_id, c_order_id, description, dateordered," +
      "         salesrep_id, dateinvoiced, dateacct, c_bpartner_id, c_bpartner_location_id,  c_currency_id, paymentrule, c_paymentterm_id, totallines," +
      "         grandtotal, m_pricelist_id, ispaid, totalpaid, outstandingamt, fin_paymentmethod_id, em_aprm_processinvoice)" +
      "        select get_uuid(), o.ad_client_id, o.ad_org_id, 'Y', now(), ?, now(), ?, 'Y', ?," +
      "        'CO', 'RE', 'N', 'Y', 'N', dt.c_doctypeinvoice_id, dt.c_doctypeinvoice_id," +
      "        null, null, null, max(o.salesrep_id), to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS'))," +
      "        to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS')), o.c_bpartner_id, o.c_bpartner_location_id," +
      "        o.c_currency_id, bp.paymentrule, bp.c_paymentterm_id, sum(o.totallines), sum(o.grandtotal), max(o.m_pricelist_id)," +
      "        'Y', sum(o.grandtotal),  0, bp.fin_paymentmethod_id, 'RE'" +
      "        from  c_order o, obpos_applications ap, obpos_terminaltype tt, c_doctype dt, c_bpartner bp" +
      "        where o.em_obpos_applications_id = ap.obpos_applications_id" +
      "        and ap.obpos_terminaltype_id = tt.obpos_terminaltype_id" +
      "        and tt.c_doctype_id = dt.c_doctype_id" +
      "        and o.c_bpartner_id = bp.c_bpartner_id" +
      "        and o.em_obpos_app_cashup_id = ?" +
      "        and o.c_doctype_id in (tt.c_doctype_id, tt.c_doctyperet_id)" +
      "        and not exists (select 1 from c_orderline ol2 where (ol2.qtyinvoiced <> 0 or ol2.qtydelivered <> ol2.qtyordered) and ol2.c_order_id = o.c_order_id)" +
      "        and o.em_obpos_notinvoiceoncashup = 'N'" +
      "        group by  o.ad_client_id, o.ad_org_id, dt.c_doctypeinvoice_id, dt.c_doctypeinvoice_id," +
      "        o.c_bpartner_id, o.c_bpartner_location_id, o.c_currency_id, bp.paymentrule," +
      "        bp.c_paymentterm_id, o.m_pricelist_id, bp.fin_paymentmethod_id";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, userId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, userId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cashupId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice headers no grouping orders
 */
  public static int insertHeaderNoGrouping(ConnectionProvider connectionProvider, String userId, String executionId, String lang, String currentDate, String cashupId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoice" +
      "        (c_invoice_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, issotrx," +
      "         documentno, docstatus, docaction, processing, processed, posted, c_doctype_id, c_doctypetarget_id, c_order_id, description, dateordered," +
      "         salesrep_id, dateinvoiced, dateacct, c_bpartner_id, c_bpartner_location_id,  c_currency_id, paymentrule, c_paymentterm_id, totallines," +
      "         grandtotal, m_pricelist_id, ispaid, totalpaid, outstandingamt, fin_paymentmethod_id, em_aprm_processinvoice)" +
      "        select get_uuid(), o.ad_client_id, o.ad_org_id, 'Y', now(), ?, now(), ?, 'Y', ?, 'CO', 'RE', 'N', 'Y', 'N'," +
      "        dt.c_doctypeinvoice_id, dt.c_doctypeinvoice_id," +
      "        o.c_order_id, max(ad_message_get2('OrderDocumentno', ?)) || ' ' || o.documentno, o.dateordered," +
      "        max(o.salesrep_id), to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS'))," +
      "        to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS'))," +
      "        o.c_bpartner_id, o.c_bpartner_location_id, o.c_currency_id, bp.paymentrule," +
      "        bp.c_paymentterm_id, sum(o.totallines), sum(o.grandtotal), max(o.m_pricelist_id), 'Y', sum(o.grandtotal),  0, bp.fin_paymentmethod_id, 'RE'" +
      "        from  c_order o, obpos_applications ap, obpos_terminaltype tt, c_doctype dt, c_bpartner bp" +
      "        where o.em_obpos_applications_id = ap.obpos_applications_id" +
      "        and ap.obpos_terminaltype_id = tt.obpos_terminaltype_id" +
      "        and tt.c_doctype_id = dt.c_doctype_id" +
      "        and o.c_bpartner_id = bp.c_bpartner_id" +
      "        and o.em_obpos_app_cashup_id = ?" +
      "        and o.c_doctype_id in (tt.c_doctype_id, tt.c_doctyperet_id)" +
      "        and not exists (select 1 from c_orderline ol2 where (ol2.qtyinvoiced <> 0 or ol2.qtydelivered <> ol2.qtyordered) and ol2.c_order_id = o.c_order_id)" +
      "        and o.em_obpos_notinvoiceoncashup = 'N'" +
      "        group by  o.ad_client_id, o.ad_org_id, dt.c_doctypeinvoice_id, dt.c_doctypeinvoice_id," +
      "        o.c_bpartner_id, o.c_bpartner_location_id, o.c_currency_id, bp.paymentrule," +
      "        bp.c_paymentterm_id, o.m_pricelist_id, bp.fin_paymentmethod_id," +
      "        o.c_order_id, o.dateOrdered, o.documentno" +
      "        order by o.dateOrdered, o.documentno";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, userId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, userId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, lang);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cashupId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice lines grouping orders
 */
  public static int insertLinesGrouping(ConnectionProvider connectionProvider, String cashupId, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoiceline" +
      "        (c_invoiceline_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "         c_invoice_id, c_orderline_id, m_inoutline_id, line," +
      "         description, m_product_id, qtyinvoiced, pricelist, priceactual, pricelimit, linenetamt," +
      "         c_uom_id, c_tax_id, taxamt, m_attributesetinstance_id, pricestd," +
      "         taxbaseamt, line_gross_amount, gross_unit_price," +
      "         c_bpartner_id, grosspricestd, grosspricelist," +
      "         a_asset_id, c_project_id, user1_id, user2_id, c_costcenter_id)" +
      "        select get_uuid(), i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "        i.c_invoice_id, ol.c_orderline_id, max(iol.m_inoutline_id), row_number() over (partition by i.c_invoice_id order by o.documentno, ol.line) * 10 as line," +
      "        ol.description, ol.m_product_id, ol.qtyordered, ol.pricelist, ol.priceactual, ol.pricelimit, ol.linenetamt," +
      "        ol.c_uom_id, ol.c_tax_id, 0, ol.m_attributesetinstance_id, ol.pricestd," +
      "        ol.linenetamt , (case when pl.istaxincluded = 'Y' then ol.line_gross_amount else 0 end), ol.gross_unit_price," +
      "        ol.c_bpartner_id, ol.grosspricestd, ol.grosspricelist," +
      "        ol.a_asset_id, ol.c_project_id, ol.user1_id, ol.user2_id, ol.c_costcenter_id" +
      "        from c_order o, m_pricelist pl, obpos_applications ap, obpos_terminaltype tt, c_doctype dt, c_invoice i," +
      "        c_orderline ol left join m_inoutline iol on ol.c_orderline_id = iol.c_orderline_id" +
      "        where ol.c_order_id = o.c_order_id" +
      "        and o.m_pricelist_id = pl.m_pricelist_id" +
      "        and o.em_obpos_applications_id = ap.obpos_applications_id" +
      "        and ap.obpos_terminaltype_id = tt.obpos_terminaltype_id" +
      "        and tt.c_doctype_id = dt.c_doctype_id" +
      "        and o.em_obpos_app_cashup_id = ?" +
      "        and ol.qtydelivered = ol.qtyordered" +
      "        and ol.qtyordered <> 0" +
      "        and o.c_doctype_id in (tt.c_doctype_id, tt.c_doctyperet_id)" +
      "        and not exists (select 1 from c_orderline ol2 where ol2.qtyinvoiced <> 0 and ol2.c_order_id = o.c_order_id)" +
      "        and o.em_obpos_notinvoiceoncashup = 'N'" +
      "        and i.documentno = ?" +
      "        and i.c_bpartner_id = o.c_bpartner_id and i.c_bpartner_location_id = o.billto_id" +
      "        group by i.ad_client_id, i.ad_org_id, i.createdby, i.updatedby," +
      "        i.c_invoice_id, ol.c_orderline_id, ol.description, ol.m_product_id," +
      "        ol.qtyordered, ol.pricelist, ol.priceactual, ol.pricelimit, ol.linenetamt," +
      "        ol.c_uom_id, ol.c_tax_id, ol.m_attributesetinstance_id, ol.pricestd," +
      "        ol.linenetamt , ol.line_gross_amount, ol.gross_unit_price," +
      "        ol.c_bpartner_id, ol.grosspricestd, ol.grosspricelist," +
      "        ol.a_asset_id, ol.c_project_id, ol.user1_id, ol.user2_id, ol.c_costcenter_id, pl.istaxincluded," +
      "        o.documentno, ol.line, o.c_order_id" +
      "        order by o.documentno, ol.line";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cashupId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice lines no grouping orders
 */
  public static int insertLinesNoGrouping(ConnectionProvider connectionProvider, String cashupId, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoiceline" +
      "        (c_invoiceline_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "         c_invoice_id, c_orderline_id, m_inoutline_id, line," +
      "         description, m_product_id, qtyinvoiced, pricelist, priceactual, pricelimit, linenetamt," +
      "         c_uom_id, c_tax_id, taxamt, m_attributesetinstance_id, pricestd," +
      "         taxbaseamt, line_gross_amount, gross_unit_price," +
      "         c_bpartner_id, grosspricestd, grosspricelist," +
      "         a_asset_id, c_project_id, user1_id, user2_id, c_costcenter_id)" +
      "        select get_uuid(), i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "        i.c_invoice_id, ol.c_orderline_id, max(iol.m_inoutline_id), row_number() over (partition by i.c_invoice_id order by o.documentno, ol.line asc) * 10 as line," +
      "        ol.description, ol.m_product_id, ol.qtyordered, ol.pricelist, ol.priceactual, ol.pricelimit, ol.linenetamt," +
      "        ol.c_uom_id, ol.c_tax_id, 0, ol.m_attributesetinstance_id, ol.pricestd," +
      "        ol.linenetamt , (case when pl.istaxincluded = 'Y' then ol.line_gross_amount else 0 end), ol.gross_unit_price," +
      "        ol.c_bpartner_id, ol.grosspricestd, ol.grosspricelist," +
      "        ol.a_asset_id, ol.c_project_id, ol.user1_id, ol.user2_id, ol.c_costcenter_id" +
      "        from c_order o, m_pricelist pl, obpos_applications ap, obpos_terminaltype tt, c_doctype dt, c_invoice i," +
      "        c_orderline ol left join m_inoutline iol on ol.c_orderline_id = iol.c_orderline_id" +
      "        where ol.c_order_id = o.c_order_id" +
      "        and o.m_pricelist_id = pl.m_pricelist_id" +
      "        and o.em_obpos_applications_id = ap.obpos_applications_id" +
      "        and ap.obpos_terminaltype_id = tt.obpos_terminaltype_id" +
      "        and tt.c_doctype_id = dt.c_doctype_id" +
      "        and o.em_obpos_app_cashup_id = ?" +
      "        and ol.qtydelivered = ol.qtyordered" +
      "        and ol.qtyordered <> 0" +
      "        and o.c_doctype_id in (tt.c_doctype_id, tt.c_doctyperet_id)" +
      "        and not exists (select 1 from c_orderline ol2 where ol2.qtyinvoiced <> 0 and ol2.c_order_id = o.c_order_id)" +
      "        and o.em_obpos_notinvoiceoncashup = 'N'" +
      "        and i.documentno = ?" +
      "        and i.c_order_id = o.c_order_id" +
      "        group by i.ad_client_id, i.ad_org_id, i.createdby, i.updatedby," +
      "        i.c_invoice_id, ol.c_orderline_id, ol.description, ol.m_product_id," +
      "        ol.qtyordered, ol.pricelist, ol.priceactual, ol.pricelimit, ol.linenetamt," +
      "        ol.c_uom_id, ol.c_tax_id, ol.m_attributesetinstance_id, ol.pricestd," +
      "        ol.linenetamt , ol.line_gross_amount, ol.gross_unit_price," +
      "        ol.c_bpartner_id, ol.grosspricestd, ol.grosspricelist," +
      "        ol.a_asset_id, ol.c_project_id, ol.user1_id, ol.user2_id, ol.c_costcenter_id, pl.istaxincluded," +
      "        o.documentno, ol.line, o.c_order_id" +
      "        order by o.documentno, ol.line";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cashupId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Update qty invoiced of order lines grouping orders
 */
  public static int updateQtyOrderLinesGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        update c_orderline set qtyinvoiced=qtyordered" +
      "        where c_orderline_id in (" +
      "          select ol.c_orderline_id" +
      "          from c_order o, c_orderline ol, c_orderlinetax olt, c_invoice i, c_invoiceline il" +
      "          where o.c_order_id = ol.c_order_id" +
      "          and ol.c_orderline_id = olt.c_orderline_id" +
      "          and ol.c_orderline_id = il.c_orderline_id" +
      "          and il.c_invoice_id = i.c_invoice_id" +
      "          and i.documentno = ?" +
      "          and i.c_bpartner_id = o.c_bpartner_id" +
      "          and i.c_bpartner_location_id = o.billto_id)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Update qty invoiced of order lines no grouping orders
 */
  public static int updateQtyOrderLinesNoGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        update c_orderline set qtyinvoiced=qtyordered" +
      "        where c_orderline_id in (" +
      "          select ol.c_orderline_id" +
      "          from c_order o, c_orderline ol, c_orderlinetax olt, c_invoice i, c_invoiceline il" +
      "          where o.c_order_id = ol.c_order_id" +
      "          and ol.c_orderline_id = olt.c_orderline_id" +
      "          and ol.c_orderline_id = il.c_orderline_id" +
      "          and il.c_invoice_id = i.c_invoice_id" +
      "          and i.documentno = ?" +
      "          and i.c_order_id = o.c_order_id)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice tax lines grouping orders
 */
  public static int insertTaxLinesGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoicelinetax (c_invoicelinetax_id, c_tax_id, c_invoice_id, c_invoiceline_id," +
      "        ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "        taxbaseamt, taxamt, line, recalculate)" +
      "        select get_uuid(), olt.c_tax_id, i.c_invoice_id, il.c_invoiceline_id," +
      "        i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "        olt.taxbaseamt, olt.taxamt, olt.line, 'Y'" +
      "        from c_order o, c_orderline ol, c_orderlinetax olt, c_invoice i, c_invoiceline il" +
      "        where o.c_order_id = ol.c_order_id" +
      "        and ol.c_orderline_id = olt.c_orderline_id" +
      "        and ol.c_orderline_id = il.c_orderline_id" +
      "        and il.c_invoice_id = i.c_invoice_id" +
      "        and i.documentno = ?" +
      "        and i.c_bpartner_id = o.c_bpartner_id" +
      "        and i.c_bpartner_location_id = o.billto_id";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice tax lines no grouping orders
 */
  public static int insertTaxLinesNoGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoicelinetax (c_invoicelinetax_id, c_tax_id, c_invoice_id, c_invoiceline_id," +
      "        ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "        taxbaseamt, taxamt, line, recalculate)" +
      "        select get_uuid(), olt.c_tax_id, i.c_invoice_id, il.c_invoiceline_id," +
      "        i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "        olt.taxbaseamt, olt.taxamt, olt.line, 'Y'" +
      "        from c_order o, c_orderline ol, c_orderlinetax olt, c_invoice i, c_invoiceline il" +
      "        where o.c_order_id = ol.c_order_id" +
      "        and ol.c_orderline_id = olt.c_orderline_id" +
      "        and ol.c_orderline_id = il.c_orderline_id" +
      "        and il.c_invoice_id = i.c_invoice_id" +
      "        and i.documentno = ?" +
      "        and i.c_order_id = o.c_order_id";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice offer lines grouping orders
 */
  public static int insertOfferLinesGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoiceline_offer" +
      "        (c_invoiceline_offer_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "         c_invoiceline_id, line, m_offer_id, priceoffer, amtoffer, priceoffergross, totalamt, displayedtotalamt, em_obdisc_qtyoffer)" +
      "        select get_uuid(), i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "        il.c_invoiceline_id, olo.line, olo.m_offer_id, olo.priceoffer , olo.amtoffer," +
      "        olo.priceoffergross , olo.totalamt," +
      "        olo.displayedtotalamt, olo.em_obdisc_qtyoffer" +
      "        from c_order o, c_orderline ol, c_orderline_offer olo, c_invoice i, c_invoiceline il" +
      "        where o.c_order_id = ol.c_order_id" +
      "        and ol.c_orderline_id = olo.c_orderline_id" +
      "        and ol.c_orderline_id = il.c_orderline_id" +
      "        and il.c_invoice_id = i.c_invoice_id" +
      "        and i.documentno = ?" +
      "        and i.c_bpartner_id = o.c_bpartner_id" +
      "        and i.c_bpartner_location_id = o.billto_id";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice offer lines no grouping orders
 */
  public static int insertOfferLinesNoGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoiceline_offer" +
      "        (c_invoiceline_offer_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "         c_invoiceline_id, line, m_offer_id, priceoffer, amtoffer, priceoffergross, totalamt, displayedtotalamt, em_obdisc_qtyoffer)" +
      "        select get_uuid(), i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "        il.c_invoiceline_id, olo.line, olo.m_offer_id, olo.priceoffer , olo.amtoffer," +
      "        olo.priceoffergross , olo.totalamt," +
      "        olo.displayedtotalamt, olo.em_obdisc_qtyoffer" +
      "        from c_order o, c_orderline ol, c_orderline_offer olo, c_invoice i, c_invoiceline il" +
      "        where o.c_order_id = ol.c_order_id" +
      "        and ol.c_orderline_id = olo.c_orderline_id" +
      "        and ol.c_orderline_id = il.c_orderline_id" +
      "        and il.c_invoice_id = i.c_invoice_id" +
      "        and i.documentno = ?" +
      "        and i.c_order_id = o.c_order_id";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice tax grouping orders
 */
  public static int insertInvoiceTaxGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoicetax" +
      "        (c_tax_id, c_invoice_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "         taxbaseamt, taxamt, line, c_invoicetax_id, recalculate)" +
      "        select c_tax_id, c_invoice_id, ad_client_id, ad_org_id, 'Y', now(), createdby, now(), updatedby," +
      "        sum(taxbaseamt), sum(taxamt)," +
      "        row_number() over (partition by c_invoice_id order by c_tax_id asc) * 10 as line, get_uuid(), 'Y'" +
      "        from (" +
      "           select ot.c_tax_id, i.c_invoice_id, i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "           ot.taxbaseamt, ot.taxamt" +
      "           from c_ordertax ot inner join c_order o on ot.c_order_id=o.c_order_id" +
      "           inner join c_orderline ol on o.c_order_id=ol.c_order_id" +
      "           inner join c_invoiceline il on il.c_orderline_id = ol.c_orderline_id" +
      "           inner join c_invoice i on i.c_invoice_id=il.c_invoice_id" +
      "           where i.documentno = ?" +
      "           and i.c_bpartner_id = o.c_bpartner_id and i.c_bpartner_location_id = o.billto_id" +
      "           group by ot.c_ordertax_id, ot.c_tax_id , i.c_invoice_id, i.ad_client_id, i.ad_org_id,now(), i.createdby, now(), i.updatedby,        ot.taxbaseamt, ot.taxamt" +
      "        ) d" +
      "        group by c_tax_id, c_invoice_id,ad_client_id, ad_org_id, createdby, updatedby";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice tax no grouping orders
 */
  public static int insertInvoiceTaxNoGrouping(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into c_invoicetax" +
      "        (c_tax_id, c_invoice_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby," +
      "         taxbaseamt, taxamt, line, c_invoicetax_id, recalculate)" +
      "        select c_tax_id, c_invoice_id, ad_client_id, ad_org_id, 'Y', now(), createdby, now(), updatedby," +
      "        sum(taxbaseamt), sum(taxamt)," +
      "        row_number() over (partition by c_invoice_id order by c_tax_id asc) * 10 as line, get_uuid(), 'Y'" +
      "        from (" +
      "           select ot.c_tax_id, i.c_invoice_id, i.ad_client_id, i.ad_org_id, 'Y', now(), i.createdby, now(), i.updatedby," +
      "           ot.taxbaseamt, ot.taxamt" +
      "           from c_ordertax ot inner join c_order o on ot.c_order_id=o.c_order_id" +
      "           inner join c_orderline ol on o.c_order_id=ol.c_order_id" +
      "           inner join c_invoiceline il on il.c_orderline_id = ol.c_orderline_id" +
      "           inner join c_invoice i on i.c_invoice_id=il.c_invoice_id" +
      "           where i.documentno = ?" +
      "           and i.c_order_id = o.c_order_id" +
      "           group by ot.c_ordertax_id, ot.c_tax_id , i.c_invoice_id, i.ad_client_id, i.ad_org_id,now(), i.createdby, now(), i.updatedby,        ot.taxbaseamt, ot.taxamt" +
      "        ) d" +
      "        group by c_tax_id, c_invoice_id,ad_client_id, ad_org_id, createdby, updatedby";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
Insert invoice payment schedule
 */
  public static int insertPaymentSchedule(ConnectionProvider connectionProvider, String currentDate, String executionId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        insert into fin_payment_schedule" +
      "        (fin_payment_schedule_id, ad_client_id, ad_org_id, created, createdby, updated, updatedby," +
      "         c_invoice_id, duedate, fin_paymentmethod_id, c_currency_id," +
      "         amount, paidamt, outstandingamt, isactive," +
      "         fin_payment_priority_id, origduedate, expecteddate)" +
      "        select get_uuid(), i.ad_client_id, i.ad_org_id, now(), i.createdby, now(), i.updatedby," +
      "        i.c_invoice_id, to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS')), i.fin_paymentmethod_id, i.c_currency_id," +
      "        0, 0, 0, 'Y', i.fin_payment_priority_id, to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS'))," +
      "        to_timestamp(to_char(?), to_char('DD-MM-YYYY HH24:MI:SS'))" +
      "        from c_invoice i" +
      "        where i.documentno = ?";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currentDate);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

/**
select orderlines with multiple shipmentlines
 */
  public static OrderGroupingProcessorData[] selectOrderAndInvoiceId(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    return selectOrderAndInvoiceId(connectionProvider, executionId, 0, 0);
  }

/**
select orderlines with multiple shipmentlines
 */
  public static OrderGroupingProcessorData[] selectOrderAndInvoiceId(ConnectionProvider connectionProvider, String executionId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        select distinct ol.c_order_id, i.c_invoice_id" +
      "        from c_orderline ol, c_invoiceline il, c_invoice i" +
      "        where ol.c_orderline_id = il.c_orderline_id" +
      "        and il.c_invoice_id = i.c_invoice_id" +
      "        and i.documentno = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        OrderGroupingProcessorData objectOrderGroupingProcessorData = new OrderGroupingProcessorData();
        objectOrderGroupingProcessorData.cOrderId = UtilSql.getValue(result, "c_order_id");
        objectOrderGroupingProcessorData.cInvoiceId = UtilSql.getValue(result, "c_invoice_id");
        objectOrderGroupingProcessorData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectOrderGroupingProcessorData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    OrderGroupingProcessorData objectOrderGroupingProcessorData[] = new OrderGroupingProcessorData[vector.size()];
    vector.copyInto(objectOrderGroupingProcessorData);
    return(objectOrderGroupingProcessorData);
  }

  public static OrderGroupingProcessorData[] selectInvoiceId(ConnectionProvider connectionProvider, String executionId)    throws ServletException {
    return selectInvoiceId(connectionProvider, executionId, 0, 0);
  }

  public static OrderGroupingProcessorData[] selectInvoiceId(ConnectionProvider connectionProvider, String executionId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        select i.c_invoice_id" +
      "        from c_invoice i" +
      "        where i.documentno = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, executionId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        OrderGroupingProcessorData objectOrderGroupingProcessorData = new OrderGroupingProcessorData();
        objectOrderGroupingProcessorData.cInvoiceId = UtilSql.getValue(result, "c_invoice_id");
        objectOrderGroupingProcessorData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectOrderGroupingProcessorData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    OrderGroupingProcessorData objectOrderGroupingProcessorData[] = new OrderGroupingProcessorData[vector.size()];
    vector.copyInto(objectOrderGroupingProcessorData);
    return(objectOrderGroupingProcessorData);
  }
}
