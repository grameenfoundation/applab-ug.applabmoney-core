package org.applab.AppLabMoneyCore.Zimba;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class ZimbaNetworks extends ArrayList<ZimbaNetwork> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static boolean createZimbaNetwork(ZimbaNetwork zimbaNetwork) {
        Connection cn = null;
        StringBuilder sb = null;
        String sqlQuery = "";
        PreparedStatement stm = null;
        int dbStatusCode = 0;

        try {
            if (zimbaNetwork == null) {
                return false;
            }
            
            if (zimbaNetwork.getCustomerId() <= 0) {
                return false;
            }
            
            if (zimbaNetwork.getFriendCustomerId() <= 0) {
                return false;
            }

            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

            sb = new StringBuilder();
            sb.append("INSERT INTO ZIMBA_NETWORKS (CUSTOMER_ID, FRIEND_CUSTOMER_ID, DATE_CONNECTED, FRIENDSHIP_LEVEL) ");
            sb.append(" VALUES(%d, %d, SYSDATE(),'%s')");
            sqlQuery = sb.toString();

            stm = cn.prepareStatement(String.format(sqlQuery, zimbaNetwork.getCustomerId(), zimbaNetwork.getFriendCustomerId(), zimbaNetwork.getFriendshipLevel()));

            // Execute the Query
            dbStatusCode = stm.executeUpdate();

            return ((dbStatusCode > 0) ? true : false);

        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }
    
    public static ZimbaNetworks getNetworksByCustomerId(long customerId) {
        ZimbaNetworks zimbaNetworks = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT NETWORK_ID, DATE_CONNECTED, CUSTOMER_ID, FRIEND_CUSTOMER_ID, FRIENDSHIP_LEVEL, BORROWED_FROM_FRIEND, LENDED_TO_FRIEND, ");
            sb.append(" DEFAULTS_BY_FRIEND, DEFAULTS_TO_FRIEND, RECEIVABLE_FROM_FRIEND, PAYABLE_TO_FRIEND ");
            sb.append(" FROM ZIMBA_NETWORKS ");
            sb.append(" WHERE CUSTOMER_ID=%d");

            String sqlQuery = String.format(sb.toString(), customerId);
            zimbaNetworks = executeQueryToRetrieveZimbaNetworks(sqlQuery);
            return zimbaNetworks;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }
    
    public static ZimbaNetworks getActiveNetworksByCustomerId(long customerId) {
        ZimbaNetworks zimbaNetworks = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT NETWORK_ID, DATE_CONNECTED, CUSTOMER_ID, FRIEND_CUSTOMER_ID, FRIENDSHIP_LEVEL, BORROWED_FROM_FRIEND, LENDED_TO_FRIEND, ");
            sb.append(" DEFAULTS_BY_FRIEND, DEFAULTS_TO_FRIEND, RECEIVABLE_FROM_FRIEND, PAYABLE_TO_FRIEND ");
            sb.append(" FROM ZIMBA_NETWORKS ");
            sb.append(" WHERE CUSTOMER_ID=%d AND IS_DISCONNECTED = 0");

            String sqlQuery = String.format(sb.toString(), customerId);
            zimbaNetworks = executeQueryToRetrieveZimbaNetworks(sqlQuery);
            return zimbaNetworks;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }
    
    public static ZimbaNetworks getNetworksByCustomerIdToFriend(long customerId, long friendCustomerId) {
        ZimbaNetworks zimbaNetworks = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT NETWORK_ID, DATE_CONNECTED, CUSTOMER_ID, FRIEND_CUSTOMER_ID, FRIENDSHIP_LEVEL, BORROWED_FROM_FRIEND, LENDED_TO_FRIEND, ");
            sb.append(" DEFAULTS_BY_FRIEND, DEFAULTS_TO_FRIEND, RECEIVABLE_FROM_FRIEND, PAYABLE_TO_FRIEND ");
            sb.append(" FROM ZIMBA_NETWORKS ");
            sb.append(" WHERE CUSTOMER_ID=%d AND FRIEND_CUSTOMER_ID = %d");

            String sqlQuery = String.format(sb.toString(), customerId, friendCustomerId);
            zimbaNetworks = executeQueryToRetrieveZimbaNetworks(sqlQuery);
            return zimbaNetworks;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }
    
    private static ZimbaNetworks executeQueryToRetrieveZimbaNetworks(String sqlQuery) {
        Connection cn = null;
        ZimbaNetworks zimbaNetworks = null;
        try {
            zimbaNetworks = new ZimbaNetworks();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

            PreparedStatement stm = cn.prepareStatement(sqlQuery);

            ResultSet result = stm.executeQuery();
            while (result.next()) {
                ZimbaNetwork zimbaNetwork = new ZimbaNetwork();
                zimbaNetwork.setNetworkId(result.getLong("NETWORK_ID"));
                zimbaNetwork.setDateConnected((result.getDate("DATE_CONNECTED") == null) ? null : result.getDate("DATE_CONNECTED"));
                zimbaNetwork.setCustomerId(result.getLong("CUSTOMER_ID"));
                zimbaNetwork.setFriendCustomerId(result.getLong("FRIEND_CUSTOMER_ID"));
                zimbaNetwork
                        .setFriendshipLevel((result.getString("FRIENDSHIP_LEVEL") == null) ? "" : result.getString("FRIENDSHIP_LEVEL").trim());
                zimbaNetwork.setBorrowedFromFriend(result.getInt("BORROWED_FROM_FRIEND"));
                zimbaNetwork.setLendedToFriend(result.getInt("LENDED_TO_FRIEND"));
                zimbaNetwork.setDefaultsByFriend(result.getInt("DEFAULTS_BY_FRIEND"));
                zimbaNetwork.setDefaultsToFriend(result.getInt("DEFAULTS_TO_FRIEND"));
                zimbaNetwork.setReceivableFromFriend(result.getDouble("RECEIVABLE_FROM_FRIEND"));
                zimbaNetwork.setPayableToFriend(result.getDouble("PAYABLE_TO_FRIEND"));

                zimbaNetworks.add(zimbaNetwork);
            }
            return zimbaNetworks;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }
    
    public static boolean isCustomerConnectedToFriend(long customerId, long friendCustomerId) {
        try {
            ZimbaNetworks networks = getNetworksByCustomerIdToFriend(customerId, friendCustomerId);
            
            if(networks == null || networks.size() < 1) {
                return false;
            }
            else {
                return true;
            }            
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }
    
    public static ZimbaNetwork getNetworkForCustomerToFriend(long customerId, long friendCustomerId) {
        try {
            ZimbaNetworks networks = getNetworksByCustomerIdToFriend(customerId, friendCustomerId);
            
            if(networks == null || networks.size() < 1) {
                return null;
            }
            else {
                return networks.get(0);
            }            
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }
}
