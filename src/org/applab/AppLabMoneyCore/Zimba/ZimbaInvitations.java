package org.applab.AppLabMoneyCore.Zimba;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class ZimbaInvitations extends ArrayList<ZimbaInvitation> {

    private static final long serialVersionUID = 1L;

    public static boolean createZimbaInvitation(ZimbaInvitation zimbaInvitation) {
        Connection cn = null;
        StringBuilder sb = null;
        String sqlQuery = "";
        PreparedStatement stm = null;
        int dbStatusCode = 0;

        try {
            if (zimbaInvitation == null) {
                return false;
            }

            if (zimbaInvitation.getCustomerId() <= 0) {
                return false;
            }

            if (zimbaInvitation.getFriendMsisdn().length() <= 0) {
                return false;
            }

            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

            sb = new StringBuilder();
            sb.append("INSERT INTO ZIMBA_INVITATIONS (CUSTOMER_ID, INVITATION_DATE, FRIEND_MSISDN) ");
            sb.append(" VALUES(%d,SYSDATE(),'%s')");
            sqlQuery = sb.toString();

            stm = cn.prepareStatement(String.format(sqlQuery, zimbaInvitation.getCustomerId(), zimbaInvitation.getFriendMsisdn()));

            // Execute the Query
            dbStatusCode = stm.executeUpdate();

            return ((dbStatusCode > 0) ? true : false);

        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }

    public static boolean respondToZimbaInvitation(ZimbaInvitation zimbaInvitation,  int responseFlg, long friendCustomerId,
                                                   String friendshipLevel) {
        Connection cn = null;
        StringBuilder sb = null;
        String sqlQuery = "";
        PreparedStatement stm = null;
        int dbStatusCode = 0;

        try {

            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

            cn.setAutoCommit(false);

            sb = new StringBuilder();
            sb.append("UPDATE ZIMBA_INVITATIONS SET INVITATION_STATUS = %d, RESPONSE_DATE = SYSDATE() WHERE INVITATION_ID = %d ");
            sqlQuery = sb.toString();

            stm = cn.prepareStatement(String.format(sqlQuery, responseFlg, zimbaInvitation.getInvitationId()));

            // Execute the Query
            dbStatusCode = stm.executeUpdate();

            if (dbStatusCode <= 0) {
                cn.rollback();
                return false;
            }

            // Create the Network, if the Invited Accepted the Invitation
            if (responseFlg == 1)
            {
                ZimbaNetwork zimbaNetwork = new ZimbaNetwork();
                zimbaNetwork.setCustomerId(zimbaInvitation.getCustomerId());
                zimbaNetwork.setFriendCustomerId(friendCustomerId);
                zimbaNetwork.setFriendshipLevel(friendshipLevel);

                boolean retValCreateNetwork = ZimbaNetworks.createZimbaNetwork(zimbaNetwork);

                if (!retValCreateNetwork)
                {
                    cn.rollback();
                    return false;
                }
            }
            
            // Otherwise, commit.
            cn.commit();

            return true;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }

    public static ZimbaInvitations getAllInvitations() {
        ZimbaInvitations zimbaInvitations = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT INVITATION_ID, INVITATION_DATE, CUSTOMER_ID, FRIEND_MSISDN, INVITATION_STATUS ");
            sb.append(" FROM ZIMBA_INVITATIONS ");

            String sqlQuery = sb.toString();
            zimbaInvitations = executeQueryToRetrieveZimbaInvitations(sqlQuery);
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    public static ZimbaInvitations getAllInvitationsFromCustomerId(long customerId) {
        ZimbaInvitations zimbaInvitations = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT INVITATION_ID, INVITATION_DATE, CUSTOMER_ID, FRIEND_MSISDN, INVITATION_STATUS ");
            sb.append(" FROM ZIMBA_INVITATIONS ");
            sb.append(" WHERE CUSTOMER_ID=%d");

            String sqlQuery = String.format(sb.toString(), customerId);
            zimbaInvitations = executeQueryToRetrieveZimbaInvitations(sqlQuery);
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    public static ZimbaInvitations getPendingInvitationsFromCustomerId(long customerId) {
        ZimbaInvitations zimbaInvitations = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT INVITATION_ID, INVITATION_DATE, CUSTOMER_ID, FRIEND_MSISDN, INVITATION_STATUS ");
            sb.append(" FROM ZIMBA_INVITATIONS ");
            sb.append(" WHERE CUSTOMER_ID=%d AND INVITATION_STATUS=0");

            String sqlQuery = String.format(sb.toString(), customerId);
            zimbaInvitations = executeQueryToRetrieveZimbaInvitations(sqlQuery);
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    public static ZimbaInvitations getPendingInvitationsFromCustomerIdToMsisdn(long customerId, String friendMsisdn) {
        ZimbaInvitations zimbaInvitations = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT INVITATION_ID, INVITATION_DATE, CUSTOMER_ID, FRIEND_MSISDN, INVITATION_STATUS ");
            sb.append(" FROM ZIMBA_INVITATIONS ");
            sb.append(" WHERE CUSTOMER_ID=%d AND FRIEND_MSISDN='%s' AND INVITATION_STATUS=0");

            String sqlQuery = String.format(sb.toString(), customerId, friendMsisdn);
            zimbaInvitations = executeQueryToRetrieveZimbaInvitations(sqlQuery);
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    public static ZimbaInvitations getAllInvitationsToFriendMsisdn(String friendMsisdn) {
        ZimbaInvitations zimbaInvitations = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT INVITATION_ID, INVITATION_DATE, CUSTOMER_ID, FRIEND_MSISDN, INVITATION_STATUS ");
            sb.append(" FROM ZIMBA_INVITATIONS ");
            sb.append(" WHERE FRIEND_CUSTOMER_ID=%s");

            String sqlQuery = String.format(sb.toString(), friendMsisdn);
            zimbaInvitations = executeQueryToRetrieveZimbaInvitations(sqlQuery);
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    public static ZimbaInvitations getPendingInvitationsToFriendMsisdn(String friendMsisdn) {
        ZimbaInvitations zimbaInvitations = null;
        StringBuffer sb = null;

        try {
            sb = new StringBuffer();
            sb.append("SELECT INVITATION_ID, INVITATION_DATE, CUSTOMER_ID, FRIEND_MSISDN, INVITATION_STATUS ");
            sb.append(" FROM ZIMBA_INVITATIONS ");
            sb.append(" WHERE FRIEND_CUSTOMER_ID=%s AND INVITATION_STATUS = 0");

            String sqlQuery = String.format(sb.toString(), friendMsisdn);
            zimbaInvitations = executeQueryToRetrieveZimbaInvitations(sqlQuery);
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    private static ZimbaInvitations executeQueryToRetrieveZimbaInvitations(String sqlQuery) {
        Connection cn = null;
        ZimbaInvitations zimbaInvitations = null;
        try {
            zimbaInvitations = new ZimbaInvitations();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

            PreparedStatement stm = cn.prepareStatement(sqlQuery);

            ResultSet result = stm.executeQuery();
            while (result.next()) {
                ZimbaInvitation zimbaInvitation = new ZimbaInvitation();
                zimbaInvitation.setInvitationId(result.getLong("INVITATION_ID"));
                zimbaInvitation.setInvitationDate((result.getDate("INVITATION_DATE") == null) ? null : result.getDate("INVITATION_DATE"));
                zimbaInvitation.setCustomerId(result.getLong("CUSTOMER_ID"));
                zimbaInvitation
                        .setFriendMsisdn((result.getString("FRIEND_MSISDN") == null) ? "" : result.getString("FRIEND_MSISDN").trim());
                zimbaInvitation.setInvitationStatus(result.getInt("INVITATION_STATUS"));

                zimbaInvitations.add(zimbaInvitation);
            }
            return zimbaInvitations;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

}
