package org.applab.AppLabMoneyCore.Zimba;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.applab.AppLabMoneyCore.CustomerInformation;
import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class ZimbaCommon {
    public static boolean activateZimba(CustomerInformation sourceCustInfo, String networkSizeRange, String networkRole) {
        Connection cn = null;
        StringBuilder sb = null;
        String sqlQuery = "";
        PreparedStatement stm = null;
        int dbStatusCode = 0;
        
        try {
            if (sourceCustInfo == null) {
                return false;
            }
                
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
    
            sb = new StringBuilder();
            sb.append("INSERT INTO ZIMBA_CUSTOMERS (CUSTOMER_ID, ACTIVATION_DATE,PREF_NT_SIZE_RANGE, PREF_NT_ROLE) ");
            sb.append(" VALUES(%s,SYSDATE(),'%s','%s')");
            sqlQuery = sb.toString();
    
            stm = cn.prepareStatement(String.format(sqlQuery, Long.toString(sourceCustInfo.getCustomerId()), networkSizeRange, networkRole));
    
            // Execute the Query
            dbStatusCode = stm.executeUpdate();
    
            return ((dbStatusCode > 0) ? true : false);   

        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    } 
    
    public static boolean checkZimbaActivation(long customerId) {
        Connection cn = null;
        int count = 0;

        try {
            // Otherwise, get the Connection
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

            PreparedStatement stm = cn.prepareStatement(String.format(
                    "SELECT COUNT(*) ACIVATION FROM ZIMBA_CUSTOMERS WHERE CUSTOMER_ID = '%s'", Long.toString(customerId)));

            // Execute the Query
            ResultSet result = stm.executeQuery();

            while (result.next()) {
                count = result.getInt(1);
            }

            if (count > 0) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
        finally {
            try {
                if (cn != null) {
                    cn.close();
                }
            }
            catch (Exception ex2) {
                HelperUtils.writeToLogFile("Server", "ERR: " + ex2.getMessage() + " TRACE: " + ex2.getStackTrace());
            }
        }
    }
}
