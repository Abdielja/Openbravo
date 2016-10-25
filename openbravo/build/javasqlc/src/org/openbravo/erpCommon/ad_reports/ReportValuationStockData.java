//Sqlc generated V1.O00-1
package org.openbravo.erpCommon.ad_reports;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.service.db.QueryTimeOutUtil;
import org.openbravo.database.SessionInfo;
import java.util.*;

class ReportValuationStockData implements FieldProvider {
static Logger log4j = Logger.getLogger(ReportValuationStockData.class);
  private String InitRecordNumber="0";
  public String categoryName;
  public String mProductId;
  public String productName;
  public String qty;
  public String uomName;
  public String averageCost;
  public String totalCost;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("category_name") || fieldName.equals("categoryName"))
      return categoryName;
    else if (fieldName.equalsIgnoreCase("m_product_id") || fieldName.equals("mProductId"))
      return mProductId;
    else if (fieldName.equalsIgnoreCase("product_name") || fieldName.equals("productName"))
      return productName;
    else if (fieldName.equalsIgnoreCase("qty"))
      return qty;
    else if (fieldName.equalsIgnoreCase("uom_name") || fieldName.equals("uomName"))
      return uomName;
    else if (fieldName.equalsIgnoreCase("average_cost") || fieldName.equals("averageCost"))
      return averageCost;
    else if (fieldName.equalsIgnoreCase("total_cost") || fieldName.equals("totalCost"))
      return totalCost;
    else if (fieldName.equals("rownum"))
      return rownum;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static ReportValuationStockData[] select(ConnectionProvider connectionProvider, String adLanguage, String cCurrencyConv, String legalEntity, String datePlus, String dateFrom, String dateTimeFormat, String warehouse, String categoryProduct)    throws ServletException {
    return select(connectionProvider, adLanguage, cCurrencyConv, legalEntity, datePlus, dateFrom, dateTimeFormat, warehouse, categoryProduct, 0, 0);
  }

  public static ReportValuationStockData[] select(ConnectionProvider connectionProvider, String adLanguage, String cCurrencyConv, String legalEntity, String datePlus, String dateFrom, String dateTimeFormat, String warehouse, String categoryProduct, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT M_PRODUCT_CATEGORY.NAME AS CATEGORY_NAME, ZZ.M_PRODUCT_ID, AD_COLUMN_IDENTIFIER (to_char('M_Product'),to_char(ZZ.M_PRODUCT_ID),to_char(?)) AS PRODUCT_NAME, SUM(M.MOVEMENTQTY) AS QTY, UOM_NAME, " +
      "            CASE ZZ.ISCOSTCALCULATED" +
      "                       WHEN 'Y' THEN SUM(TOTAL_COST) / SUM(M.MOVEMENTQTY)" +
      "                       ELSE NULL" +
      "                     END AS AVERAGE_COST," +
      "               SUM(TOTAL_COST) AS TOTAL_COST" +
      "        FROM M_TRANSACTION M" +
      "        INNER JOIN (SELECT M_PRODUCT.M_PRODUCT_CATEGORY_ID, A.M_PRODUCT_ID, " +
      "                C_UOM.NAME AS UOM_NAME, SUM(A.SUMA) AS TOTAL_COST, A.ISCOSTCALCULATED, A.AD_CLIENT_ID, A.C_CURRENCY_ID, A.M_TRANSACTION_ID" +
      "             FROM M_TRANSACTION TR " +
      "             LEFT JOIN (SELECT TRX.M_TRANSACTION_ID, TRX.M_PRODUCT_ID, " +
      "                   CASE TRX.ISCOSTCALCULATED" +
      "                       WHEN 'Y' THEN C_CURRENCY_CONVERT_PRECISION (SUM(CASE WHEN TRX.MOVEMENTQTY < 0 THEN - TC.TRXCOST ELSE TC.TRXCOST END),TC.C_CURRENCY_ID,?,TC.MOVEMENTDATE,NULL,TRX.AD_CLIENT_ID,?,'C')" +
      "                       ELSE NULL" +
      "                     END AS SUMA," +
      "                   TRX.C_UOM_ID, TRX.AD_CLIENT_ID, TRX.ISCOSTCALCULATED, TC.C_CURRENCY_ID" +
      "                 FROM M_TRANSACTION TRX " +
      "                     JOIN M_LOCATOR L ON TRX.M_LOCATOR_ID = L.M_LOCATOR_ID" +
      "                     LEFT JOIN (SELECT SUM(COST) AS TRXCOST, M_TRANSACTION_ID, C_CURRENCY_ID, COALESCE(DATEACCT, COSTDATE) as MOVEMENTDATE" +
      "                                FROM M_TRANSACTION_COST" +
      "                                WHERE COALESCE(DATEACCT, COSTDATE) < to_date(?)" +
      "                                GROUP BY m_transaction_id, C_CURRENCY_ID, COALESCE(DATEACCT, COSTDATE)) TC ON TRX.M_TRANSACTION_ID = TC.M_TRANSACTION_ID" +
      "                 WHERE TRX.MOVEMENTDATE < to_date(?)" +
      "                 AND TRX.TRXPROCESSDATE >= to_timestamp(?, ?)" +
      "                 AND L.M_WAREHOUSE_ID = ?" +
      "                 GROUP BY TRX.M_TRANSACTION_ID, TRX.M_PRODUCT_ID, TRX.C_UOM_ID, TRX.AD_CLIENT_ID, TRX.ISCOSTCALCULATED, TC.C_CURRENCY_ID, TC.MOVEMENTDATE) A ON TR.M_TRANSACTION_ID = A.M_TRANSACTION_ID," +
      "                C_UOM," +
      "                M_PRODUCT" +
      "            WHERE A.M_PRODUCT_ID = M_PRODUCT.M_PRODUCT_ID" +
      "            AND   A.C_UOM_ID = C_UOM.C_UOM_ID            " +
      "            AND   1 = 1";
    strSql = strSql + ((categoryProduct==null || categoryProduct.equals(""))?"":"  AND M_PRODUCT.M_PRODUCT_CATEGORY_ID= ?  ");
    strSql = strSql + 
      "            AND  ( A.SUMA <> 0 OR TR.MOVEMENTQTY <> 0)" +
      "          GROUP BY M_PRODUCT.M_PRODUCT_CATEGORY_ID, A.M_PRODUCT_ID, C_UOM.NAME, A.ISCOSTCALCULATED, A.AD_CLIENT_ID, A.C_CURRENCY_ID, A.M_TRANSACTION_ID) ZZ" +
      "        ON M.M_TRANSACTION_ID = ZZ.M_TRANSACTION_ID, M_PRODUCT_CATEGORY" +
      "        where M_PRODUCT_CATEGORY.M_PRODUCT_CATEGORY_id = zz.M_PRODUCT_CATEGORY_id" +
      "        GROUP BY ZZ.M_PRODUCT_ID, M_PRODUCT_CATEGORY.NAME, UOM_NAME, ZZ.ISCOSTCALCULATED" +
      "        HAVING SUM(M.MOVEMENTQTY) <>0" +
      "        ORDER BY M_PRODUCT_CATEGORY.NAME, PRODUCT_NAME    ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, adLanguage);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cCurrencyConv);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, legalEntity);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, datePlus);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, datePlus);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, dateFrom);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, dateTimeFormat);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, warehouse);
      if (categoryProduct != null && !(categoryProduct.equals(""))) {
        iParameter++; UtilSql.setValue(st, iParameter, 12, null, categoryProduct);
      }

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
        ReportValuationStockData objectReportValuationStockData = new ReportValuationStockData();
        objectReportValuationStockData.categoryName = UtilSql.getValue(result, "category_name");
        objectReportValuationStockData.mProductId = UtilSql.getValue(result, "m_product_id");
        objectReportValuationStockData.productName = UtilSql.getValue(result, "product_name");
        objectReportValuationStockData.qty = UtilSql.getValue(result, "qty");
        objectReportValuationStockData.uomName = UtilSql.getValue(result, "uom_name");
        objectReportValuationStockData.averageCost = UtilSql.getValue(result, "average_cost");
        objectReportValuationStockData.totalCost = UtilSql.getValue(result, "total_cost");
        objectReportValuationStockData.rownum = Long.toString(countRecord);
        objectReportValuationStockData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectReportValuationStockData);
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
    ReportValuationStockData objectReportValuationStockData[] = new ReportValuationStockData[vector.size()];
    vector.copyInto(objectReportValuationStockData);
    return(objectReportValuationStockData);
  }

  public static ReportValuationStockData[] set()    throws ServletException {
    ReportValuationStockData objectReportValuationStockData[] = new ReportValuationStockData[1];
    objectReportValuationStockData[0] = new ReportValuationStockData();
    objectReportValuationStockData[0].categoryName = "";
    objectReportValuationStockData[0].mProductId = "";
    objectReportValuationStockData[0].productName = "";
    objectReportValuationStockData[0].qty = "";
    objectReportValuationStockData[0].uomName = "";
    objectReportValuationStockData[0].averageCost = "";
    objectReportValuationStockData[0].totalCost = "";
    return objectReportValuationStockData;
  }
}
