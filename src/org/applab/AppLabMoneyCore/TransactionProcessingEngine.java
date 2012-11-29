/*
 * 
 * Copyright (c) 2010 AppLab, Grameen Foundation
 * 
 */

package org.applab.AppLabMoneyCore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.applab.AppLabMoneyCore.Me2Me.*;
import org.applab.AppLabMoneyCore.Zimba.ZimbaCommon;
import org.applab.AppLabMoneyCore.Zimba.ZimbaInvitation;
import org.applab.AppLabMoneyCore.Zimba.ZimbaInvitations;
import org.applab.AppLabMoneyCore.Zimba.ZimbaNetwork;
import org.applab.AppLabMoneyCore.Zimba.ZimbaNetworks;
import org.applab.AppLabMoneyCore.HelperUtils;

public class TransactionProcessingEngine {

	private InetAddress hostInetAddress = null;
	private String hostIpAddress;
	private String sourceMsisdn;
	private String referenceId;
	private String requestKeyword;
	private String requestCommand;
	private NationalDestCodes nationalDestCodes = null;

	private CustomerInformation sourceCustInfo;
	private CustomerInformation destCustInfo;

	private String destMessage = "";
	private UserInformation systemUserInfo;

	public TransactionProcessingEngine() {

	}

	public TransactionProcessingEngine(String theReferenceId,
			String theSourceMsisdn, String theRequestKeyword,
			String theRequestCommand) {
		sourceMsisdn = theSourceMsisdn;
		referenceId = theReferenceId;
		requestKeyword = theRequestKeyword;
		requestCommand = theRequestCommand;

		if (nationalDestCodes == null) {
			nationalDestCodes = new NationalDestCodes();
		}

		// Get the HostIPAddress
		try {
			hostInetAddress = InetAddress.getLocalHost();
			hostIpAddress = (hostInetAddress != null) ? hostInetAddress
					.getHostAddress() : "127.0.0.1";
		} catch (UnknownHostException e) {
			hostIpAddress = "127.0.0.1";
		}
		if (hostIpAddress.trim().isEmpty()) {
			hostIpAddress = "127.0.0.1";
		}
	}

	/*
	 * Procedure that serves as entry-point for command processing
	 */
	public void processRequestCommand() {
		try {

			if (requestKeyword.toUpperCase().equalsIgnoreCase("KYCR")) {
				processKYCR();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("DPST")) {
				processDPST();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("MBAL")) {
				processMBAL();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("CASH")) {
				processCASH();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("WDRW")) {
				processWDRW();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("STMT")) {
				processSTMT();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("PINC")) {
				processPINC();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("ACTV")) {
				processACTV();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("CRGL")) {
				processCRGL();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("MTOM")) {
				processMTOM();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("MTOU")) {
				processMTOM();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("GBAL")) {
				processGBAL();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("TENQ")) {
				processTENQ();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("REDM")) {
				processREDM();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("STOP")) {
				processSTOP();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("REBT")) {
				processREBT();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("ALTG")) {
				processALTG();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("ALTGA")) {
				processALTGA();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("ALTGD")) {
				processALTGD();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("ADMN")) {
				processADMN();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase("INVT")) {
				processINVT();
			}

			// ZIMBA KEYWORDS
			else if (requestKeyword.toUpperCase().equalsIgnoreCase("NTACTV")) {
				processNTACTV();
			} else if (requestKeyword.toUpperCase()
					.equalsIgnoreCase("NTINVITE")) {
				processNTINVITE();
			} else if (requestKeyword.toUpperCase().equalsIgnoreCase(
					"NTRESPINV")) {
				processNTRESPINV();
			} else {
				destMessage = "The service failed to determine the kind of transaction requested.";
				HelperUtils.writeToLogFile("console", destMessage);
				HelperUtils.writeToLogFile("OutBoundMessages", destMessage);

				// send the SMS
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/*
	 * Procedure to log the received command request into the database
	 */
	private static boolean logInBoundMessage(String sourceMsisdn,
			String referenceId, String requestKeyword, String requestCommand)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {

			// If the data fields are empty, return false
			if (sourceMsisdn.trim().isEmpty() || referenceId.trim().isEmpty()
					|| requestCommand.trim().isEmpty()) {
				return false;
			}

			// Otherwise, get the Connection
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("INSERT INTO INBOUND_MESSAGES(INBOUND_MESSAGE_ID, REFERENCE_ID, CREATED_TIMESTAMP, SOURCE_MSISDN, REQUEST_KEY, REQUEST_MESSAGE) ");
			sb.append(" VALUES(NULL, '%s', SYSDATE(), '%s', '%s', '%s')");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, referenceId,
					sourceMsisdn, requestKeyword, requestCommand));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			return ((dbStatusCode > 0) ? true : false);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean logInBoundMessage(String theMessage) {
		try {
			int insertAttempts = 1;
			while (logInBoundMessage(sourceMsisdn, referenceId, requestKeyword,
					theMessage) == false) {
				if (insertAttempts >= 5) {
					HelperUtils
							.writeToLogFile("Server",
									"Failed Completely in all Attempts to Insert into SMPP_HITS.");
					return false;
				}

				insertAttempts++;

				// Get new Referenece_ID
				referenceId = referenceId.concat("1");
			}
			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		}
	}

	/*
	 * Procedure to Log OutBoundMessages to OUTBOUND_MESSAGES table
	 */
	public static boolean logOutBoundMessages(String destMessage) {
		try {
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static String convertMobileNoToMsisdn(String srcMobileNo) {
		String theMsisdn = "";
		String theMobileNo = srcMobileNo;

		try {

			if (theMobileNo.startsWith("+")) {
				theMobileNo = theMobileNo.substring(1);
			}

			if (theMobileNo.startsWith(SystemConfigInfo.getCountryCode())) {
				if (SystemConfigInfo.getMsisdnLeadZeroRequired()) {

					// The MobileLength + 1 to cater for the Leading Zero
					if (theMobileNo.trim().length() == (SystemConfigInfo
							.getMobileLength()
							+ SystemConfigInfo.getCountryCode().length() + 1)) {
						theMsisdn = theMobileNo;
					}
				} else {
					if (theMobileNo.trim().length() == (SystemConfigInfo
							.getMobileLength() + SystemConfigInfo
							.getCountryCode().length())) {
						theMsisdn = theMobileNo;
					}
				}
			} else {
				if (SystemConfigInfo.getMsisdnLeadZeroRequired()) {
					if (!theMobileNo.startsWith("0")) {
						theMobileNo = "0".concat(theMobileNo);
					}

					if (theMobileNo.trim().length() == (SystemConfigInfo
							.getMobileLength()
							+ SystemConfigInfo.getCountryCode().length() + 1)) {
						theMsisdn = SystemConfigInfo.getCountryCode().concat(
								theMobileNo);
					}
				} else {
					if (theMobileNo.startsWith("0")) {
						theMobileNo = theMobileNo.substring(1);
					}
					if (theMobileNo.trim().length() == SystemConfigInfo
							.getMobileLength()) {
						theMsisdn = SystemConfigInfo.getCountryCode().concat(
								theMobileNo);
					}
				}
			}

			return theMsisdn;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return theMsisdn;
		}
	}

	private boolean validatePinCode(String thePinCode) {
		boolean isValid = false;
		int failedPinAttempts = 0;

		try {
			failedPinAttempts = this.sourceCustInfo.getInvalidPinCount();
			if (this.sourceCustInfo.getPinCode() == null
					|| !this.sourceCustInfo.getPinCode().equalsIgnoreCase(
							thePinCode)) {
				failedPinAttempts++;

				if (failedPinAttempts >= SystemConfigInfo
						.getInvalidPasswordLock()) {
					CustomerInformation.lockCustomer(this.sourceCustInfo
							.getCustomerId());
				}
				isValid = false;
			} else {
				isValid = true;
				CustomerInformation.resetInvalidPinCount(this.sourceCustInfo
						.getCustomerId());
			}

			return isValid;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		}
	}

	private boolean validateAdminPinCode(String thePinCode) {
		boolean isValid = false;
		int failedPinAttempts = 0;

		try {
			failedPinAttempts = this.systemUserInfo.getInvalidPinCount();
			if (this.systemUserInfo.getPhonePinCode() == null
					|| !this.systemUserInfo.getPhonePinCode().equalsIgnoreCase(
							thePinCode)) {
				failedPinAttempts++;

				if (failedPinAttempts >= SystemConfigInfo
						.getInvalidPasswordLock()) {
					UserInformation.lockUser(this.systemUserInfo.getUserId());
				}
				isValid = false;
			} else {
				isValid = true;
				UserInformation.resetInvalidPinCount(this.systemUserInfo
						.getUserId());
			}

			return isValid;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		}
	}

	private boolean transferFundsP2P(long sourceAccountId, long destAccountId,
			double amount) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE= BOOK_BALANCE - %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount,
					sourceAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE = BOOK_BALANCE + %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount,
					destAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean transferFundsOwnMeToMeGoal(long sourceAccountId,
			int goalId, double amount, String referenceId, String sourceMsisdn)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Debit the Customer's Main Wallet and Credit the Customer's Me2Me
			// Wallet
			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE= IFNULL(BOOK_BALANCE,0.00) - %f, BLOCKED_BALANCE= IFNULL(BLOCKED_BALANCE,0.00) + %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, amount,
					sourceAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET ACCRUED_AMOUNT= IFNULL(ACCRUED_AMOUNT,0.00) + %f, LAST_CREDIT_DATE = SYSDATE() WHERE GOAL_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Log the Transaction
			sb = new StringBuilder();
			sb.append("INSERT INTO ME2ME_TRANSACTIONS (REFERENCE_ID,TRANSACTION_TIMESTAMP,SRC_MSISDN,TRANSACTION_AMOUNT,GOAL_ID,GOAL_BAL_BEFORE,GOAL_BAL_AFTER) ");
			sb.append(" VALUES('%s',SYSDATE(),'%s',%f,%d,(SELECT IFNULL(ACCRUED_AMOUNT,0.00) - %f FROM ME2ME_GOALS WHERE GOAL_ID=%d),(SELECT IFNULL(ACCRUED_AMOUNT,0.00) FROM ME2ME_GOALS WHERE GOAL_ID=%d))");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, referenceId,
					sourceMsisdn, amount, goalId, amount, goalId, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean transferFundsOthersMeToMeGoal(long sourceAccountId,
			long destAccountId, int goalId, double amount, String referenceId,
			String sourceMsisdn) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Debit the Sponsor's Main Wallet
			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE= IFNULL(BOOK_BALANCE,0.00) - %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount,
					sourceAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Credit the Beneficiary's Me2Me Wallet
			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BLOCKED_BALANCE= IFNULL(BLOCKED_BALANCE,0.00) + %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount,
					destAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Update the Balance for the Specific Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET ACCRUED_AMOUNT= IFNULL(ACCRUED_AMOUNT,0.00) + %f, LAST_CREDIT_DATE = SYSDATE() WHERE GOAL_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Log the Transaction
			sb = new StringBuilder();
			sb.append("INSERT INTO ME2ME_TRANSACTIONS (REFERENCE_ID,TRANSACTION_TIMESTAMP,SRC_MSISDN,TRANSACTION_AMOUNT,GOAL_ID,GOAL_BAL_BEFORE,GOAL_BAL_AFTER) ");
			sb.append(" VALUES('%s',SYSDATE(),'%s',%f,%d,(SELECT IFNULL(ACCRUED_AMOUNT,0.00) - %f FROM ME2ME_GOALS WHERE GOAL_ID=%d),(SELECT IFNULL(ACCRUED_AMOUNT,0.00) FROM ME2ME_GOALS WHERE GOAL_ID=%d))");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, referenceId,
					sourceMsisdn, amount, goalId, amount, goalId, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean transferFundsRedeemMeToMeGoal(long sourceAccountId,
			int goalId, double amount) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";
		double amountAfterRedeem = 0.0;

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Debit the Customer's Main Wallet and Credit the Customer's Me2Me
			// Wallet
			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE= IFNULL(BOOK_BALANCE,0.00) + %f, BLOCKED_BALANCE= IFNULL(BLOCKED_BALANCE,0.00) - %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, amount,
					sourceAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET IS_MATURED = 1, IS_STOPPED = 1, IS_REDEEMED = 1, DATE_REDEEMED = SYSDATE(), AMOUNT_REDEEMED=%f WHERE GOAL_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Log the Transaction
			sb = new StringBuilder();
			sb.append("INSERT INTO ME2ME_TRANSACTIONS (REFERENCE_ID,TRANSACTION_TIMESTAMP,SRC_MSISDN,TRANSACTION_AMOUNT,GOAL_ID,GOAL_BAL_BEFORE,GOAL_BAL_AFTER) ");
			sb.append(" VALUES('%s',SYSDATE(),'%s',%f,%d,%f,%f)");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, referenceId,
					sourceMsisdn, amount, goalId, amount, amountAfterRedeem));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean transferFundsStopMeToMeGoal(long sourceAccountId,
			int goalId, double amount, String reasonStopped)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Debit the Customer's Main Wallet and Credit the Customer's Me2Me
			// Wallet
			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE= IFNULL(BOOK_BALANCE,0.00) + %f, BLOCKED_BALANCE= IFNULL(BLOCKED_BALANCE,0.00) - %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, amount,
					sourceAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET IS_STOPPED = 1, DATE_STOPPED = SYSDATE(), REASON_STOPPED='%s' WHERE GOAL_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, reasonStopped,
					goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean transferFundsRebate(long sourceAccountId, int goalId,
			double amount, String withdrawReason) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Debit the Customer's Main Wallet and Credit the Customer's Me2Me
			// Wallet
			sb = new StringBuilder();
			sb.append("UPDATE ACCOUNTS SET BOOK_BALANCE= IFNULL(BOOK_BALANCE,0.00) + %f, BLOCKED_BALANCE= IFNULL(BLOCKED_BALANCE,0.00) - %f WHERE ACCOUNT_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, amount,
					sourceAccountId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET ACCRUED_AMOUNT = IFNULL(ACCRUED_AMOUNT,0.00) - %f, LAST_DEBIT_DATE = SYSDATE() WHERE GOAL_ID = %d ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, amount, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			// Log the Transaction
			sb = new StringBuilder();
			sb.append("INSERT INTO ME2ME_TRANSACTIONS (REFERENCE_ID,TRANSACTION_TIMESTAMP,SRC_MSISDN,TRANSACTION_AMOUNT,GOAL_ID,GOAL_BAL_BEFORE,GOAL_BAL_AFTER) ");
			sb.append(" VALUES('%s',SYSDATE(),'%s',%f,%d,(SELECT IFNULL(ACCRUED_AMOUNT,0.00) + %f FROM ME2ME_GOALS WHERE GOAL_ID=%d),(SELECT IFNULL(ACCRUED_AMOUNT,0.00) FROM ME2ME_GOALS WHERE GOAL_ID=%d))");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, referenceId,
					sourceMsisdn, amount, goalId, amount, goalId, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean alterMeToMeGoal(long sourceAccountId, int goalId,
			double targetAmount, String maturityDateStr, String referenceId,
			String sourceMsisdn) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET TARGET_AMOUNT = %f, MATURITY_DATE = '%s' WHERE GOAL_ID = %d");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, targetAmount,
					maturityDateStr, goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean alterMeToMeGoalAmount(long sourceAccountId, int goalId,
			double targetAmount, String referenceId, String sourceMsisdn)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET TARGET_AMOUNT = %f WHERE GOAL_ID = %d");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, targetAmount,
					goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private boolean alterMeToMeGoalDate(long sourceAccountId, int goalId,
			String maturityDateStr, String referenceId, String sourceMsisdn)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			cn.setAutoCommit(false);

			// Credit the Me2Me Goal
			sb = new StringBuilder();
			sb.append("UPDATE ME2ME_GOALS SET MATURITY_DATE = '%s' WHERE GOAL_ID = %d");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, maturityDateStr,
					goalId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			if (dbStatusCode <= 0) {
				cn.rollback();
				return false;
			}

			cn.commit();

			return true;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		} finally {
			if (cn != null) {
				cn.close();
			}

			if (stm != null) {
				stm.close();
			}
		}
	}

	private void processMBAL() {
		String[] transElements = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 2) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (sourceCustInfo.getAccountInfo() == null
					|| sourceCustInfo.getAccountInfo().getAccountId() == 0) {
				this.destMessage = "Your account information could not be verified.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			Date currDate = new Date(System.currentTimeMillis());
			String currency = SystemConfigInfo.getCurrencyCode();
			String currDateStr = String.format("%1$tH:%1$tM:%1$tS", currDate);

			this.destMessage = String.format(
					"REF: %s: Your available balance as at %s was %,.0f%s",
					referenceId, currDateStr, sourceCustInfo.getAccountInfo()
							.getBookBalance(), currency);
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processCASH() {
		String[] transElements = null;
		String phoneNumber = "";
		String destMsisdn = "";
		String amountStr = "";
		double amount = 0.00;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[3];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that "
						+ Integer.toString(SystemConfigInfo
								.getInvalidPasswordLock())
						+ " attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			int sourceAccountTypeBitmap = sourceCustInfo.getAccountTypeBitmap();
			if ((sourceAccountTypeBitmap <= HelperUtils.BITMAP_NONE)
					|| (SystemConfigInfo.getTempRestriction() && sourceAccountTypeBitmap <= HelperUtils.BITMAP_TEMP)) {
				this.destMessage = "Your account profile is not allowed to make this transaction. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			phoneNumber = transElements[1].trim();
			destMsisdn = convertMobileNoToMsisdn(phoneNumber);
			if (destMsisdn.isEmpty()) {
				this.destMessage = "The destination phone number is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			amountStr = transElements[2].trim();
			try {
				amount = Double.parseDouble(amountStr);
			} catch (Exception exAmount) {
				this.destMessage = "The amount you entered is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (amount < 0) {
				this.destMessage = "The amount to send must be positive.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (this.sourceCustInfo.getAccountInfo() == null
					|| this.sourceCustInfo.getAccountInfo().getBookBalance() < amount) {
				this.destMessage = "You do not have sufficient funds in your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			destCustInfo = CustomerInformation
					.getCustomerAccountInfo(destMsisdn);
			ValidationReturnInformation retValidDestCust = CustomerInformation
					.validateCustomerAccountInfo(destCustInfo, false);
			if (!retValidDestCust.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						retValidDestCust.getValidationMessage(), referenceId);
				return;
			}

			if (destCustInfo.getAccountInfo() == null
					|| destCustInfo.getAccountInfo().getAccountId() == 0) {
				this.destMessage = "The destination account information could not be verified.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retTransfer = transferFundsP2P(sourceCustInfo
					.getAccountInfo().getAccountId(), destCustInfo
					.getAccountInfo().getAccountId(), amount);
			if (!retTransfer) {
				this.destMessage = "Sorry, the transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();
			double sourceBalAfter = sourceCustInfo.getAccountInfo()
					.getBookBalance() - amount;
			double destBalAfter = destCustInfo.getAccountInfo()
					.getBookBalance() + amount;

			this.destMessage = String
					.format("REF: %s: You have sent %s%,.0f to %s. Your available balance is: %s%,.0f.",
							referenceId, currency, amount, CustomerInformation
									.getDisplayNames(destCustInfo, false),
							currency, sourceBalAfter);
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

			this.destMessage = String
					.format("REF: %s: You have received %,.0f%s from %s. Your available balance is: %,.0f%s.",
							referenceId, amount, currency, CustomerInformation
									.getDisplayNames(sourceCustInfo, false),
							destBalAfter, currency);
			HelperUtils.sendSMS(destMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processDPST() {
		String[] transElements = null;
		String phoneNumber = "";
		String destMsisdn = "";
		String amountStr = "";
		double amount = 0.00;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[3];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			int sourceAccountTypeBitmap = sourceCustInfo.getAccountTypeBitmap();
			if ((sourceAccountTypeBitmap & (HelperUtils.BITMAP_AGNT | HelperUtils.BITMAP_DLER)) == 0) {
				this.destMessage = "You are not allowed to use this feature. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			phoneNumber = transElements[1].trim();
			destMsisdn = convertMobileNoToMsisdn(phoneNumber);
			if (destMsisdn.isEmpty()) {
				this.destMessage = "The destination phone number is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			amountStr = transElements[2].trim();
			try {
				amount = Double.parseDouble(amountStr);
			} catch (Exception exAmount) {
				this.destMessage = "The amount you entered is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (amount < 0) {
				this.destMessage = "The deposit amount must be positive.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (this.sourceCustInfo.getAccountInfo() == null
					|| this.sourceCustInfo.getAccountInfo().getBookBalance() < amount) {
				this.destMessage = "You do not have sufficient funds to process the deposit.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			destCustInfo = CustomerInformation
					.getCustomerAccountInfo(destMsisdn);
			ValidationReturnInformation retValidDestCust = CustomerInformation
					.validateCustomerAccountInfo(destCustInfo, false);
			if (!retValidDestCust.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						retValidDestCust.getValidationMessage(), referenceId);
				return;
			}

			if (destCustInfo.getAccountInfo() == null
					|| destCustInfo.getAccountInfo().getAccountId() == 0) {
				this.destMessage = "The account information could not be verified.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retTransfer = transferFundsP2P(sourceCustInfo
					.getAccountInfo().getAccountId(), destCustInfo
					.getAccountInfo().getAccountId(), amount);
			if (!retTransfer) {
				this.destMessage = "Sorry, the transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();
			double sourceBalAfter = sourceCustInfo.getAccountInfo()
					.getBookBalance() - amount;
			double destBalAfter = destCustInfo.getAccountInfo()
					.getBookBalance() + amount;

			this.destMessage = String
					.format("REF: %s: You have accepted deposit of %,.0f%s from %s. Your available balance is: %,.0f%s.",
							referenceId, amount, currency, CustomerInformation
									.getDisplayNames(destCustInfo, false),
							sourceBalAfter, currency);
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

			this.destMessage = String
					.format("REF: %s: Please give %,.0f%s cash to %s as your deposit. Your available balance is: %,.0f%s.",
							referenceId, amount, currency, CustomerInformation
									.getDisplayNames(sourceCustInfo, true),
							destBalAfter, currency);
			HelperUtils.sendSMS(destMsisdn, destMessage, referenceId);

		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processPINC() {
		String[] transElements = null;
		String oldPassword = "";
		String newPassword = "";
		String maskedOldPassword = "";
		String maskedNewPassword = "";
		String separatorChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			oldPassword = transElements[1];
			maskedOldPassword = HelperUtils.maskPassword(oldPassword);

			newPassword = transElements[2];
			maskedNewPassword = HelperUtils.maskPassword(newPassword);

			// Swap the originalPassword with the maskedPassword
			transElements[2] = maskedNewPassword;
			transElements[1] = maskedOldPassword;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(oldPassword)) {
				this.destMessage = "You have entered an invalid old PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (newPassword.equalsIgnoreCase(sourceCustInfo.getPinCode())) {
				this.destMessage = "Your new PIN must be different from your old PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (newPassword.length() < SystemConfigInfo.getMinPasswordLen()
					&& SystemConfigInfo.getMinPasswordLen() != 0) {
				this.destMessage = "Your PIN is shorter than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (newPassword.length() > SystemConfigInfo.getMaxPasswordLen()
					&& SystemConfigInfo.getMaxPasswordLen() != 0) {
				this.destMessage = "Your PIN is longer than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			if (newPassword.startsWith(defaultPinCode)) {
				this.destMessage = "You must use a more secure PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retUpdate = CustomerInformation.changePinCode(
					this.sourceCustInfo.getCustomerId(), newPassword);

			if (!retUpdate) {
				this.destMessage = "The system encountered a problem while processing your request. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.destMessage = "Your PIN has been changed successfully.";
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processKYCR() {
		String[] transElements = null;
		String phoneNumber = "";
		String phoneMsisdn = "";
		String lastName = "";
		String otherNames = "";
		String physicalAddress = "";
		String homeDistrict = "";
		String idType = "";
		String idNo = "";
		String nationality = "";
		String dateOfBirthStr = "";
		Date dateOfBirth = null;
		String placeOfBirth = "";
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String asteriskChar = "*";
		String spaceChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 12) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[11];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[11] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			int sourceAccountTypeBitmap = sourceCustInfo.getAccountTypeBitmap();
			if ((sourceAccountTypeBitmap & (HelperUtils.BITMAP_AGNT | HelperUtils.BITMAP_DLER)) == 0) {
				this.destMessage = "You are not allowed to use this feature. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			phoneNumber = transElements[1].trim();
			phoneMsisdn = convertMobileNoToMsisdn(phoneNumber);
			if (phoneMsisdn.isEmpty()) {
				this.destMessage = "The phone number for the new customer is not valid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			CustomerInformation existingCustInfo = CustomerInformation
					.getCustomerAccountInfo(phoneMsisdn);
			if (existingCustInfo != null) {
				this.destMessage = "Another customer is already registered with the specified phone number.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			lastName = transElements[2].trim().replace(asteriskChar, spaceChar);
			otherNames = transElements[3].trim().replace(asteriskChar,
					spaceChar);
			physicalAddress = transElements[4].trim().replace(asteriskChar,
					spaceChar);
			homeDistrict = transElements[5].trim().replace(asteriskChar,
					spaceChar);
			idType = transElements[6].trim().replace(asteriskChar, spaceChar);
			idNo = transElements[7].trim().replace(asteriskChar, spaceChar);
			nationality = transElements[8].trim().replace(asteriskChar,
					spaceChar);
			dateOfBirthStr = transElements[9].trim().replace(asteriskChar,
					spaceChar);
			dateOfBirth = new SimpleDateFormat("ddMMyyyy")
					.parse(dateOfBirthStr);
			placeOfBirth = transElements[10].trim().replace(asteriskChar,
					spaceChar);

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			boolean retCreateCust = CustomerInformation.createCustomerAccount(
					phoneMsisdn, lastName, otherNames, physicalAddress,
					homeDistrict, idType, idNo, nationality, dateOfBirth,
					placeOfBirth, defaultPinCode,
					sourceCustInfo.getCustomerId());

			if (!retCreateCust) {
				this.destMessage = "The system encountered problems while registering the new customer.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			CustomerInformation newCustInfo = CustomerInformation
					.getNewCustomerInfo(phoneMsisdn);
			if (newCustInfo == null) {
				this.destMessage = "The system encountered problems while registering the new customer.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retCreateAccount = CustomerInformation.createAccount(
					newCustInfo.getCustomerId(),
					newCustInfo.getAccountTypeBitmap());
			if (!retCreateAccount) {
				this.destMessage = "The system encountered problems while registering the new customer.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String fullNames = lastName.concat(" ").concat(otherNames)
					.toUpperCase();

			this.destMessage = "The new customer: " + fullNames + " of phone: "
					+ phoneMsisdn + " has been registered successfully.";
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

			this.destMessage = "Welcome to AppLab Money. Your PIN is "
					+ defaultPinCode
					+ ". For your own security change your PIN before transacting.";
			HelperUtils.sendSMS(phoneMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processWDRW() {
		String[] transElements = null;
		String agentCode = "";
		String destMsisdn = "";
		String amountStr = "";
		double amount = 0.00;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[3];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that "
						+ Integer.toString(SystemConfigInfo
								.getInvalidPasswordLock())
						+ " attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			int sourceAccountTypeBitmap = sourceCustInfo.getAccountTypeBitmap();
			if ((sourceAccountTypeBitmap <= HelperUtils.BITMAP_NONE)
					|| (SystemConfigInfo.getTempRestriction() && sourceAccountTypeBitmap <= HelperUtils.BITMAP_TEMP)) {
				this.destMessage = "Your account profile is not allowed to make this transaction. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			agentCode = transElements[1].trim();
			amountStr = transElements[2].trim();
			try {
				amount = Double.parseDouble(amountStr);
			} catch (Exception exAmount) {
				this.destMessage = "The amount you entered is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (amount < 0) {
				this.destMessage = "The amount to withdraw must be positive.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (this.sourceCustInfo.getAccountInfo() == null
					|| this.sourceCustInfo.getAccountInfo().getBookBalance() < amount) {
				this.destMessage = "You do not have sufficient funds in your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			destCustInfo = CustomerInformation
					.getCustomerAccountInfoByAgentCode(agentCode);
			ValidationReturnInformation retValidDestCust = CustomerInformation
					.validateCustomerAccountInfo(destCustInfo, false);
			if (!retValidDestCust.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						retValidDestCust.getValidationMessage(), referenceId);
				return;
			}

			if (destCustInfo.getAccountInfo() == null
					|| destCustInfo.getAccountInfo().getAccountId() == 0) {
				this.destMessage = "The account information could not be verified.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			int destAccountTypeBitmap = destCustInfo.getAccountTypeBitmap();
			if ((destAccountTypeBitmap & (HelperUtils.BITMAP_AGNT | HelperUtils.BITMAP_DLER)) == 0) {
				this.destMessage = "The agent code you have specified is barred from processing withdrawals. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retTransfer = transferFundsP2P(sourceCustInfo
					.getAccountInfo().getAccountId(), destCustInfo
					.getAccountInfo().getAccountId(), amount);
			if (!retTransfer) {
				this.destMessage = "Sorry, the transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();
			double sourceBalAfter = sourceCustInfo.getAccountInfo()
					.getBookBalance() - amount;
			double destBalAfter = destCustInfo.getAccountInfo()
					.getBookBalance() + amount;

			this.destMessage = String
					.format("REF: %s: You have withdrawn %,.0f%s at %s. Your available balance is: %,.0f%s.",
							referenceId, amount, currency, CustomerInformation
									.getDisplayNames(destCustInfo, true)
									.toUpperCase(), sourceBalAfter, currency);
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

			this.destMessage = String
					.format("REF: %s: Please give %,.0f%s to %s as withdrawal. Your available balance is: %,.0f%s.",
							referenceId, amount, currency, CustomerInformation
									.getDisplayNames(sourceCustInfo, false)
									.toUpperCase(), destBalAfter, currency);
			HelperUtils.sendSMS(destMsisdn, destMessage, referenceId);

		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processSTMT() {
		try {
			if (!logInBoundMessage(requestCommand)) {
				return;
			}
			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, requestCommand));
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processCRGL() {

		// CRGL SCHOOLFEES 1000000 31122012 WEEKLY NONE 8876
		String[] transElements = null;
		String goalType = "";
		String targetAmountStr = "";
		double targetAmount = 0.00;
		String maturityDateStr = "";
		Date maturityDate = null;
		String alertOption = "";
		String liquidityOption = "";
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 7) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[6];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// swap the originalPassword with the maskedPassword
			transElements[6] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			SimpleDateFormat inputDateFormatter = new SimpleDateFormat(
					"ddMMyyyy", Locale.ENGLISH);
			SimpleDateFormat outputDateFormatter = new SimpleDateFormat(
					"dd MM yyyy");

			goalType = transElements[1];
			targetAmountStr = transElements[2];
			targetAmount = Double.parseDouble(targetAmountStr);
			String[] dateParts = transElements[3].trim().split(":");
			maturityDateStr = dateParts[1].replaceAll("\\W", "");
			maturityDate = inputDateFormatter.parse(maturityDateStr);
			alertOption = transElements[4];
			liquidityOption = transElements[5];

			// Validate the Amount: Enforce 50000 thresh-hold amount
			if ((targetAmount < 500) || (targetAmount > 10000000)) {
				this.destMessage = "Your goal creation was unsuccessful. Your target amount must be above UGX500. Please create a new goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Validate Maturity Date: Enforce 1 Month savings threshold
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 1);

			if (maturityDate.before(cal.getTime())) {
				this.destMessage = "The goal end date is invalid. It has to be at least 1 month in the future. Please create a new goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Check whether the guy is activated
			boolean retActivatedMe2Me = MeToMeCommon
					.checkMeToMeActivation(sourceCustInfo.getCustomerId());
			if (!retActivatedMe2Me) {
				this.destMessage = "You must first activate your me2me wallet in order to access the service features. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Check whether another Goal is Active for this Type
			MeToMeGoals activeGoals = MeToMeGoals
					.getActiveGoalsByCustomerAndGoalType(
							sourceCustInfo.getCustomerId(), goalType);
			if (activeGoals.size() > 0) {
				this.destMessage = String
						.format("You have already created the goal %s.You can only have one me2me goal at a time",
								goalType.toUpperCase());
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Pick all active goals regardless of type
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());
			if (allActiveGoals.size() >= 1) {
				this.destMessage = String
						.format("You already created the goal: %s. You can only have one me2me goal at a time ",
								allActiveGoals.get(0).getGoalName()
										.toUpperCase());
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retCreateGoal = MeToMeGoal.createMeToMeGoal(sourceCustInfo,
					goalType, targetAmount, maturityDate, alertOption,
					liquidityOption);

			if (!retCreateGoal) {
				this.destMessage = "The system encountered a problem while creating your goal. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.destMessage = String
					.format("You successfully created your me2me goal: %s\r\nTarget Amount: %s%,.0f\r\nGoal End Date: %s",
							goalType.toUpperCase(), SystemConfigInfo
									.getCurrencyCode(), targetAmount,
							outputDateFormatter.format(maturityDate)
									.toUpperCase());
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	// MTOM OWN SCHOOLFEES 45000 *****
	// MTOM <dest_msisdn> SCHOOLFEES 45000 *****
	private void processMTOM() {
		String[] transElements = null;
		String goalType = "";
		String amountStr = "";
		double amount = 0.00;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;
		boolean isOwnPayment = false;
		String phoneNumber = "";
		String destMsisdn = "";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Check whether the size is 5: Pay To Other Person Goal
			if (transElements.length == 4) {
				isOwnPayment = true;
			}

			pinCode = transElements[transElements.length - 1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[transElements.length - 1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {

				// Take care of OWN: Fix in the word OWN
				if (isOwnPayment && i == 1) {
					originalRequest += "OWN" + separatorChar;
				}
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			// Get Destination Customer Info
			if (isOwnPayment) {
				destCustInfo = sourceCustInfo;
			} else {
				phoneNumber = transElements[1].trim();
				destMsisdn = convertMobileNoToMsisdn(phoneNumber);
				if (destMsisdn.isEmpty()) {
					this.destMessage = "The destination phone number is invalid.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				destCustInfo = CustomerInformation
						.getCustomerAccountInfo(destMsisdn);
				ValidationReturnInformation retValidDestCust = CustomerInformation
						.validateCustomerAccountInfo(destCustInfo, false);
				if (!retValidDestCust.isPassedValidation()) {
					HelperUtils.sendSMS(sourceMsisdn,
							retValidDestCust.getValidationMessage(),
							referenceId);
					return;
				}

				if (destCustInfo.getAccountInfo() == null
						|| destCustInfo.getAccountInfo().getAccountId() == 0) {
					this.destMessage = "The account information could not be verified.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			}

			// Process the Amount
			amountStr = transElements[transElements.length - 2].trim();
			try {
				amount = Double.parseDouble(amountStr);
			} catch (Exception exAmount) {
				this.destMessage = "The amount you entered is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (amount < 0) {
				this.destMessage = "The payment amount must be positive.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Validate the GoalType or Name: Use Index from behind
			goalType = transElements[transElements.length - 3].trim();

			// Pick all active goals regardless of type
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(destCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				if (isOwnPayment) {
					this.destMessage = "The goal selected does not exist. Please go to the main menu to create your goal.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String
							.format("The beneficiary %s does not have an active %s goal. Please ask the beneficiary to first create a goal.",
									CustomerInformation.getDisplayNames(
											destCustInfo, false).toUpperCase(),
									goalType.toUpperCase());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

					// Notify the Beneficiary Customer
					if (allActiveGoals.size() < 1) {
						this.destMessage = String
								.format("The sponsor %s attempted to pay %,.0f%s into your %s goal, but you do not have any active goals. Please first create the goal then notify the sponsor.",
										CustomerInformation.getDisplayNames(
												sourceCustInfo, false)
												.toUpperCase(), amount,
										SystemConfigInfo.getCurrencyCode(),
										goalType.toUpperCase());
						HelperUtils.sendSMS(destMsisdn, destMessage,
								referenceId);
					} else {
						// Get the Active Goals
						String activeGoalsList = "";
						for (int goalsIndex = 0; goalsIndex < allActiveGoals
								.size(); goalsIndex++) {
							activeGoalsList = activeGoalsList
									.concat(allActiveGoals.get(goalsIndex)
											.getGoalName());

							if (goalsIndex < allActiveGoals.size() - 1) {
								activeGoalsList = activeGoalsList.concat(";");
							}
						}
						this.destMessage = String
								.format("The sponsor %s attempted to pay %,.0f%s into your %s goal, but your active goals are: %s. Please notify the sponsor.",
										CustomerInformation.getDisplayNames(
												sourceCustInfo, false)
												.toUpperCase(), amount,
										SystemConfigInfo.getCurrencyCode(),
										goalType.toUpperCase(), activeGoalsList
												.toUpperCase());
						HelperUtils.sendSMS(destMsisdn, destMessage,
								referenceId);
					}
				}
				return;
			}

			// Otherwise, Pick the First Goal Type: There will always be 1 goal
			// per category for now
			targetGoal = allActiveGoals.get(0);

			if (this.sourceCustInfo.getAccountInfo() == null
					|| this.sourceCustInfo.getAccountInfo().getBookBalance() < amount) {
				this.destMessage = "You do not have sufficient funds to process the payment.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Make the Transfer
			boolean retTransfer = false;
			if (isOwnPayment) {
				retTransfer = transferFundsOwnMeToMeGoal(sourceCustInfo
						.getAccountInfo().getAccountId(),
						targetGoal.getGoalId(), amount, referenceId,
						sourceCustInfo.getMsisdn());
			} else {
				retTransfer = transferFundsOthersMeToMeGoal(sourceCustInfo
						.getAccountInfo().getAccountId(), destCustInfo
						.getAccountInfo().getAccountId(),
						targetGoal.getGoalId(), amount, referenceId,
						sourceCustInfo.getMsisdn());
			}

			if (!retTransfer) {
				this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();
			double sourceBalAfter = sourceCustInfo.getAccountInfo()
					.getBookBalance() - amount;
			double accruedGoalBalance = targetGoal.getAccruedAmount() + amount;
			double remainingTarget = targetGoal.getTargetAmount()
					- accruedGoalBalance;

			if (isOwnPayment) {
				this.destMessage = String
						.format("REF: %s: You have sent %s%,.0f to your goal, %s.\r\nAmount Raised: %s%,.0f\r\nAmount To Goal End: %s%,.0f\r\nMain Account Balance: %s%,.0f",
								referenceId, currency, amount,
								targetGoal.getGoalName(), currency,
								accruedGoalBalance, currency, remainingTarget,
								currency, sourceBalAfter);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
			} else {

				this.destMessage = String
						.format("REF: %s: You have sent %,.0f%s to the %s goal for %s. Your available balance is: %,.0f%s. ",
								referenceId,
								amount,
								currency,
								targetGoal.getGoalName().toUpperCase(),
								CustomerInformation.getDisplayNames(
										destCustInfo, false).toUpperCase(),
								sourceBalAfter, currency);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

				this.destMessage = String
						.format("REF: %s: The sponsor %s has sent %,.0f%s to your %s goal. The amount raised for the goal is now: %,.0f%s. ",
								referenceId, CustomerInformation
										.getDisplayNames(sourceCustInfo, false)
										.toUpperCase(), amount, currency,
								targetGoal.getGoalName().toUpperCase(),
								accruedGoalBalance, currency);
				HelperUtils.sendSMS(destMsisdn, destMessage, referenceId);
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processGBAL() {
		String[] transElements = null;
		String goalType = "";
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[2];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// swap the originalPassword with the maskedPassword
			transElements[2] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			// Validate the GoalType or Name
			goalType = transElements[1].trim();

			// Pick all active goals regardless of type
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Pick the First Goal Type: There will always be 1 goal per
			// category for now
			targetGoal = allActiveGoals.get(0);

			// Check again
			if (null == targetGoal) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if ((targetGoal.isStopped()) || (targetGoal.isMatured())) {
				this.destMessage = String
						.format("This goal was closed and therefore does not exist. Please go to the main menu to create another goal.",
								goalType.toUpperCase());
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}
			String currency = SystemConfigInfo.getCurrencyCode();
			double accruedGoalBalance = targetGoal.getAccruedAmount();
			double remainingTarget = targetGoal.getTargetAmount()
					- accruedGoalBalance;
			double bookBalance = sourceCustInfo.getAccountInfo()
					.getBookBalance() + accruedGoalBalance;

			this.destMessage = String
					.format("%s Goal \r\n\r\nAmount Raised: %s%,.0f \r\nAmount To Goal End: %s%,.0f \r\nGoal End Date: %s \r\nMain Account Balance: %s%,.0f",
							targetGoal.getGoalName(),
							currency,
							accruedGoalBalance,
							currency,
							remainingTarget,
							new SimpleDateFormat("dd MM yyyy").format(
									targetGoal.getMaturityDate()).toUpperCase(),
							currency, sourceCustInfo.getAccountInfo()
									.getBookBalance());
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processTENQ() {
		String[] transElements = null;
		String goalType = "";
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[2];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[2] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			// Validate the GoalType or Name: get Unredeemed Goals for the
			// Customer
			MeToMeGoals unRedeemedGoals = MeToMeGoals
					.getUnRedeemedGoalsByCustomerAndGoalType(
							sourceCustInfo.getCustomerId(), goalType);

			// Validate the GoalType or Name
			goalType = transElements[1].trim();

			// Pick all active goals regardless of type
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = String
						.format("You do not have an active goal for: %s. Please go to the main menu to create your goal.",
								goalType.toUpperCase());
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;

			} else {
				targetGoal = allActiveGoals.get(0);

				// Get the Transactions for the selected Goal
				MeToMeGoalTransactions meToMeGoalTrans = targetGoal
						.getGoalTransactions();

				if ((targetGoal == null) || (allActiveGoals.size() < 1)
						&& (unRedeemedGoals.size() < 1)) {
					this.destMessage = "You do not have any active goals. Please go to the main menu to create your goal.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else if (meToMeGoalTrans == null) {
					this.destMessage = String
							.format("You do not have any transactions for the %s goal.",
									goalType.toUpperCase());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {

					// Otherwise display the transactions
					String smsMsg = meToMeGoalTrans.getTransactionsSms(4);
					String smsMsgHeader = String.format(
							" %s Goal Mini-Statement", targetGoal.getGoalName()
									.toUpperCase());

					this.destMessage = smsMsgHeader + "\r\n" + smsMsg;
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				}
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	// REDM SCHOOLFEES 8876
	private void processREDM() {
		String[] transElements = null;
		String goalType = "";
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[2];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[2] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			// Validate the GoalType or Name: get Unredeemed Goals for the
			// Customer
			goalType = transElements[1].trim();

			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			} else {
				targetGoal = allActiveGoals.get(0);

				MeToMeGoals unRedeemedGoals = MeToMeGoals
						.getUnRedeemedGoalsByCustomer(sourceCustInfo
								.getCustomerId());
				if (unRedeemedGoals.size() < 1) {
					this.destMessage = "You do not have an active or matured goal. Please go to the main menu to create your goal.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				// Pick the First Goal Type: There will always be 1 goal per
				// category for now
				targetGoal = unRedeemedGoals.get(0);

				// Check again
				if (null == targetGoal) {
					this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				Date currDate = java.util.Calendar.getInstance().getTime();
				SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
				String currency = SystemConfigInfo.getCurrencyCode();

				// //If Goal End Date is not reached
				// if (targetGoal.getMaturityDate().after(currDate)) {
				// if (!(targetGoal.getAccruedAmount() >= targetGoal
				// .getTargetAmount())) {
				// this.destMessage = String
				// .format("Your %s goal can only be cashed out on %s or earlier if you reach %s%,.0f. You can make an early withdrawal from the me2me menu.",
				// targetGoal.getGoalName().toUpperCase(),
				// df.format(targetGoal.getMaturityDate()),
				// currency, targetGoal.getTargetAmount());
				// HelperUtils.sendSMS(sourceMsisdn, destMessage,
				// referenceId);
				// } else {

				double accruedGoalBalance = targetGoal.getAccruedAmount();

				// Compute Rewards
				// double cashReward = 0.00;
				// Assume a Credit Point = 10000.00UGX
				double creditPoints = accruedGoalBalance / 10000.00;

				// Verify Amounts
				if (sourceCustInfo.getAccountInfo().getBlockedBalance() < accruedGoalBalance) {
					this.destMessage = "There was a problem reconciling your balance in the savings wallet with the goal amount to be redeemed. Please contact Customer Care for assistance.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				// Verify again that Goal is not Redeemed
				if (targetGoal.isRedeemed()) {
					this.destMessage = String
							.format("The %s goal you are trying to redeem is already redeemed. The redemption was on %s. Please contact Customer Care for assistance.",
									targetGoal.getGoalName().toUpperCase(),
									df.format(targetGoal.getDateRedeemed())
											.toUpperCase());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				// Process Redemption
				boolean retRedeemGoal = transferFundsRedeemMeToMeGoal(
						sourceCustInfo.getAccountInfo().getAccountId(),
						targetGoal.getGoalId(), accruedGoalBalance);

				if (!retRedeemGoal) {
					this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				// Get the final expected Book Balance. A second trip to
				// the database would be more accurate
				double bookBalance = sourceCustInfo.getAccountInfo()
						.getBookBalance() + accruedGoalBalance;
				double achievedPercentage = (accruedGoalBalance / targetGoal
						.getTargetAmount()) * 100;

				// If Goal End Date is not reached
				if (targetGoal.getMaturityDate().after(currDate)) {
					if (!(targetGoal.getAccruedAmount() >= targetGoal
							.getTargetAmount())) {

						// All money accrued can be redeemed
						this.destMessage = String
								.format("You have chosen to stop your %s goal of target amount %,.0f%s. The amount raised will be ready for cash out within 24 hours.",
										targetGoal.getGoalName().toUpperCase(),
										targetGoal.getAccruedAmount(),
										currency,
										df.format(targetGoal.getMaturityDate()));
						HelperUtils.sendSMS(sourceMsisdn, destMessage,
								referenceId);
					} else {
						// All money accrued can be redeemed
						this.destMessage = String
								.format("Your %s goal of target amount: %,.0f%s will be ready for cash out within 24 hours.",
										goalType.toUpperCase(),
										targetGoal.getAccruedAmount(),
										currency,
										df.format(targetGoal.getMaturityDate()));
						HelperUtils.sendSMS(sourceMsisdn, destMessage,
								referenceId);
					}
					// }
					return;
				}
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processACTV() {
		String[] transElements = null;
		String pinCode = "";
		String confirmPinCode = "";
		String maskedPinCode = "";
		String maskedConfirmPinCode = "";
		String separatorChar = " ";

		try {
			HelperUtils.writeToLogFile("console", "RCVD:" + requestCommand);
			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			confirmPinCode = transElements[2];
			maskedConfirmPinCode = HelperUtils.maskPassword(confirmPinCode);

			// swap the originalPassword with the maskedPassword
			transElements[2] = maskedConfirmPinCode;
			transElements[1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!pinCode.equalsIgnoreCase(confirmPinCode)) {
				this.destMessage = "The PIN and the confirmation do not match.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (confirmPinCode.length() < SystemConfigInfo.getMinPasswordLen()
					&& SystemConfigInfo.getMinPasswordLen() != 0) {
				this.destMessage = "Your PIN is shorter than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (confirmPinCode.length() > SystemConfigInfo.getMaxPasswordLen()
					&& SystemConfigInfo.getMaxPasswordLen() != 0) {
				this.destMessage = "Your PIN is longer than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			if (confirmPinCode.startsWith(defaultPinCode)) {
				this.destMessage = "You must use a more secure PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			HelperUtils.writeToLogFile("console",
					"Calling function activateMe2Me");
			boolean retActivateMe2Me = MeToMeCommon
					.activateMe2Me(sourceCustInfo);

			if (!retActivateMe2Me) {
				this.destMessage = "The System encountered a problem while activating your me2me wallet. Please contact Customer Service for more Information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			HelperUtils.writeToLogFile("console",
					"Sending Notification for Successful ACTV");
			this.destMessage = "You have successfully activated your me2me wallet. You can now create goals and make payments towards them.";
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	// STOP SCHOOLFEES REASON 8876
	private void processSTOP() {
		String[] transElements = null;
		String goalType = "";
		String reasonStopped = "";
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String asteriskChar = "*";
		String spaceChar = " ";
		MeToMeGoal targetGoal = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[3];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			// Validate the GoalType or Name: get Active Goals for the Customer
			goalType = transElements[1].trim();

			// Pick the First Goal Type: There will always be 1 goal per
			// category for now
			// targetGoal = activeGoals.get(0);

			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			targetGoal = allActiveGoals.get(0);

			// Check again
			if (null == targetGoal) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the Reason why the Saving was stopped
			reasonStopped = transElements[2].trim().replace(asteriskChar,
					spaceChar);

			String currency = SystemConfigInfo.getCurrencyCode();
			double accruedGoalBalance = targetGoal.getAccruedAmount();

			// Verify Amounts
			if (sourceCustInfo.getAccountInfo().getBlockedBalance() < accruedGoalBalance) {
				this.destMessage = "There was a problem reconciling your balance in the savings wallet with the goal amount to be redeemed. Please contact Customer Care for assistance.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Process Stoppage
			boolean retStopGoal = transferFundsStopMeToMeGoal(sourceCustInfo
					.getAccountInfo().getAccountId(), targetGoal.getGoalId(),
					accruedGoalBalance, reasonStopped);

			if (!retStopGoal) {
				this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the final expected Book Balance. A second trip to the
			// database would be more accurate
			double bookBalance = sourceCustInfo.getAccountInfo()
					.getBookBalance() + accruedGoalBalance;
			double achievedPercentage = (accruedGoalBalance / targetGoal
					.getTargetAmount()) * 100;

			this.destMessage = String
					.format("REF: %s: The accumulated amount of %s%,.0f from your stopped %s goal was credited back into your main account. Your main account balance is %,.0f%s.",
							referenceId, currency, accruedGoalBalance,
							targetGoal.getGoalName().toUpperCase(),
							bookBalance, currency);
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	// REBT SCHOOLFEES <Reason> <amount> 8876
	private void processREBT() {
		String[] transElements = null;
		String goalType = "";
		String withdrawReason = "";
		String amountStr = "";
		double amount = 0.00;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String asteriskChar = "*";
		String spaceChar = " ";
		MeToMeGoal targetGoal = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 5) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[4];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[4] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			// Validate the GoalType or Name: get Active Goals for the Customer
			goalType = transElements[1].trim();

			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Pick the First Goal Type: There will always be 1 goal per
			// category for now
			targetGoal = allActiveGoals.get(0);

			// Process the Amount
			amountStr = transElements[3].trim();
			try {
				amount = Double.parseDouble(amountStr);
			} catch (Exception exAmount) {
				this.destMessage = "The amount you entered is invalid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (amount < 0) {
				this.destMessage = "The amount must be positive.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();
			double accruedGoalBalance = targetGoal.getAccruedAmount();
			double percentageSaved = (accruedGoalBalance / targetGoal
					.getTargetAmount()) * 100;

			// Verify Amounts: Still consider the total accrued Balance during
			// Reconciliation
			if (sourceCustInfo.getAccountInfo().getBlockedBalance() < accruedGoalBalance) {
				this.destMessage = "There was a problem reconciling your balance in the savings wallet with the liquidity amount. Please contact Customer Care for assistance.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			} else if (accruedGoalBalance < amount) {
				this.destMessage = String
						.format("REF: %s: You cannot withdraw %s%,.0f because your %s Amount Raised is %s%,.0f. Please withdraw a lower amount.",
								referenceId, currency, amount, targetGoal
										.getGoalName().toUpperCase(), currency,
								accruedGoalBalance);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
			} else if (accruedGoalBalance >= amount) {

				if (accruedGoalBalance > amount) {

					// Process Rebate
					boolean retRebate = transferFundsRebate(sourceCustInfo
							.getAccountInfo().getAccountId(),
							targetGoal.getGoalId(), amount, withdrawReason);

					if (!retRebate) {
						this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
						HelperUtils.sendSMS(sourceMsisdn, destMessage,
								referenceId);
						return;
					}
				} else if (accruedGoalBalance == amount) {
					String reasonStopped = "zero balance";
					boolean retStopGoal = transferFundsStopMeToMeGoal(
							sourceCustInfo.getAccountInfo().getAccountId(),
							targetGoal.getGoalId(), accruedGoalBalance,
							reasonStopped);

					if (!retStopGoal) {
						this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
						HelperUtils.sendSMS(sourceMsisdn, destMessage,
								referenceId);
						return;
					}
				}

				// Get the final expected Book Balance. A second trip to the
				// database would be more accurate
				double bookBalance = sourceCustInfo.getAccountInfo()
						.getBookBalance() + amount;
				double newAccruedGoalBalance = accruedGoalBalance - amount;

				if (newAccruedGoalBalance == 0) {

					this.destMessage = String
							.format("REF: %s: You have transferred all your funds, %s%,.0f from %s goal into main account.The goal has been CLOSED.\r\nMain Account Balance: %s%,.0f",
									referenceId, currency, amount, targetGoal
											.getGoalName().toUpperCase(),
									currency, bookBalance);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				} else {
					this.destMessage = String
							.format("REF: %s: You have transferred %s%,.0f from %s goal into main account\r\nAmount Raised: %s%,.0f\r\nGoal End Date: %s\r\nMain Account Balance: %s%,.0f",
									referenceId,
									currency,
									amount,
									targetGoal.getGoalName().toUpperCase(),
									currency,
									newAccruedGoalBalance,
									new SimpleDateFormat("dd MM yyyy").format(
											targetGoal.getMaturityDate())
											.toUpperCase(), currency,
									bookBalance);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				}
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/**
	 * Method to Edit existing Goal
	 * 
	 */
	private void processALTG() {

		// ALTG SCHOOLFEES 1000000 31122012 8876
		String[] transElements = null;
		String goalType = "";
		String targetAmountStr = "";
		double targetAmount = 0.00;
		String maturityDateStr = "";
		Date maturityDate = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;
		Date oldCreatedDate = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 5) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[4];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[4] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			SimpleDateFormat inputDateFormatter = new SimpleDateFormat(
					"ddMMyyyy");
			SimpleDateFormat outputDateFormatter = new SimpleDateFormat(
					"dd MM yyyy");

			goalType = transElements[1];

			// Check if goal exists
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			targetGoal = allActiveGoals.get(0);

			if ((transElements[2] == "0")
					|| (Integer.parseInt(transElements[2]) == 0)) {
				targetAmount = targetGoal.getTargetAmount();
			} else {
				targetAmountStr = transElements[2];
				targetAmount = Double.parseDouble(targetAmountStr);

				// Validate the Amount: Enforce 500 thresh-hold amount and 10M
				// ceiling
				if ((targetAmount < 500) || (targetAmount > 10000000)
						&& (targetAmount != targetGoal.getTargetAmount())) {
					this.destMessage = String
							.format("The target amount is invalid. Your target amount must be above UGX500 and not exceed UGX10,000,000. Your %s goal is unchanged.",
									goalType.toUpperCase());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			}
			if ((transElements[3] == "0")
					|| (Integer.parseInt(transElements[3]) == 0)) {
				maturityDate = targetGoal.getMaturityDate();
			} else {
				maturityDateStr = transElements[3];
				maturityDate = inputDateFormatter.parse(maturityDateStr);

				// Validate Maturity Date: Enforce 1 Months savings
				// Check that the new date is not less than a month from the
				// date the goal was created
				Calendar cal1 = Calendar.getInstance();
				oldCreatedDate = targetGoal.getCreatedTimestamp();
				cal1.setTime(oldCreatedDate);
				cal1.add(Calendar.MONTH, 1);

				if (maturityDate.before(cal1.getTime())) {
					this.destMessage = String
							.format("The goal end date is invalid. It has to be at least 1 month from the date of creation. Your %s goal is unchanged.",
									targetGoal.getGoalName());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			}

			// Make the Transfer
			boolean retAlter = false;

			retAlter = alterMeToMeGoal(sourceCustInfo.getAccountInfo()
					.getAccountId(), targetGoal.getGoalId(), targetAmount,
					new SimpleDateFormat("yyyy-MM-dd").format(maturityDate)
							.toUpperCase(), referenceId,
					sourceCustInfo.getMsisdn());

			if (!retAlter) {
				this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();

			this.destMessage = String
					.format("REF: %s: You have successfully changed your %s goal.\r\nTarget Amount: %s%,.0f\r\nGoal End Date: %s",
							referenceId, targetGoal.getGoalName(), currency,
							targetAmount,
							outputDateFormatter.format(maturityDate)
									.toUpperCase());
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/**
	 * Method to Edit existing Goal target Amount
	 * 
	 */
	private void processALTGA() {

		// ALTG SCHOOLFEES 1000000 8876
		String[] transElements = null;
		String goalType = "";
		String targetAmountStr = "";
		double targetAmount = 0.00;
		String maturityDateStr = "";
		Date maturityDate = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;
		Date oldCreatedDate = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[3];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			goalType = transElements[1];

			// Check if goal exists
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}
			targetGoal = allActiveGoals.get(0);

			if ((transElements[2] == "0")
					|| (Integer.parseInt(transElements[2]) == 0)) {
				targetAmount = targetGoal.getTargetAmount();
			} else {
				targetAmountStr = transElements[2];
				targetAmount = Double.parseDouble(targetAmountStr);

				// Validate the Amount: Enforce 500 thresh-hold amount and 10M
				// ceiling
				if ((targetAmount < 500) || (targetAmount > 10000000)
						&& (targetAmount != targetGoal.getTargetAmount())) {
					this.destMessage = String
							.format("The target amount is invalid. Your target amount must be above UGX500 and not exceed UGX10,000,000. Your %s goal is unchanged.",
									goalType.toUpperCase());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			}

			// Make the Transfer
			boolean retAlter = false;

			retAlter = alterMeToMeGoalAmount(sourceCustInfo.getAccountInfo()
					.getAccountId(), targetGoal.getGoalId(), targetAmount,
					referenceId, sourceCustInfo.getMsisdn());

			if (!retAlter) {
				this.destMessage = "Sorry, the me2me transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();

			this.destMessage = String
					.format("REF: %s: You have successfully changed your %s goal.\r\nTarget Amount: %s%,.0f\r\nGoal End Date: %s",
							referenceId, targetGoal.getGoalName(), currency,
							targetAmount, targetGoal.getMaturityDate());
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/**
	 * Method to Edit existing Goal target Amount
	 * 
	 */
	private void processALTGD() {

		// ALTG SCHOOLFEES 1000000 20122012 54321
		String[] transElements = null;
		String goalType = "";
		String targetAmountStr = "";
		double targetAmount = 0.00;
		String maturityDateStr = "";
		Date maturityDate = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		MeToMeGoal targetGoal = null;
		Date oldCreatedDate = null;
		Date currentTargetDate = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[3];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			SimpleDateFormat inputDateFormatter = new SimpleDateFormat(
					"ddMMyyyy");
			SimpleDateFormat outputDateFormatter = new SimpleDateFormat(
					"dd MM yyyy");

			goalType = transElements[1];

			// Check if goal exists
			MeToMeGoals allActiveGoals = MeToMeGoals
					.getActiveGoalsByCustomerId(sourceCustInfo.getCustomerId());

			if ((allActiveGoals.isEmpty()) && (allActiveGoals.size() < 1)) {
				this.destMessage = "You do not have any active goal. Please go to the main menu to create your goal.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			targetGoal = allActiveGoals.get(0);

			if ((transElements[2] == "0")
					|| (Integer.parseInt(transElements[2]) == 0)) {
				maturityDate = targetGoal.getMaturityDate();
			} else {
				maturityDateStr = transElements[2];
				maturityDate = inputDateFormatter.parse(maturityDateStr);

				// Validate Maturity Date: Enforce 1 Months savings
				// Check that the new date is not less than a month from the
				// date the goal was created
				Calendar cal1 = Calendar.getInstance();
				oldCreatedDate = targetGoal.getCreatedTimestamp();
				cal1.setTime(oldCreatedDate);
				cal1.add(Calendar.MONTH, 1);

				Calendar calCurrentTargetDate = Calendar.getInstance();
				currentTargetDate = targetGoal.getMaturityDate();
				calCurrentTargetDate.setTime(currentTargetDate);

				if (maturityDate.before(cal1.getTime())) {
					this.destMessage = String
							.format("The Goal End Date is invalid. It has to be at least 1 month from the date of creation. Your %s goal is unchanged.",
									targetGoal.getGoalName());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}

				if (maturityDate.before(calCurrentTargetDate.getTime())) {
					this.destMessage = String
							.format("The goal end date is invalid. You can only have the goal end date pushed forward. Your %s goal is unchanged.",
									targetGoal.getGoalName());
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			}

			// Make the Transfer
			boolean retAlter = false;

			retAlter = alterMeToMeGoalDate(sourceCustInfo.getAccountInfo()
					.getAccountId(), targetGoal.getGoalId(),
					new SimpleDateFormat("yyyy-MM-dd").format(maturityDate)
							.toUpperCase(), referenceId,
					sourceCustInfo.getMsisdn());

			if (!retAlter) {
				this.destMessage = "Sorry, the Me2Me transaction was not completed due to processing problems.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String currency = SystemConfigInfo.getCurrencyCode();

			this.destMessage = String
					.format("REF: %s: You have successfully changed your %s goal.\r\nTarget Amount: %s%,.0f\r\nGoal End Date: %s",
							referenceId, targetGoal.getGoalName(), currency,
							targetGoal.getTargetAmount(), outputDateFormatter
									.format(maturityDate).toUpperCase());
			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	private void processADMN() {

		// ADMN ACTION 0781058390 55555
		String[] transElements = null;
		String phoneNumber = "";
		String phoneMsisdn = "";
		String phonePinCode = "";
		String maskedPinCode = "";
		String adminAction = "";
		String separatorChar = " ";
		String asteriskChar = "*";
		String spaceChar = " ";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length < 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			phonePinCode = transElements[3].trim();
			maskedPinCode = HelperUtils.maskPassword(phonePinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[3] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.systemUserInfo = UserInformation.getUserInfo(sourceMsisdn);

			//check if user exists and has admin rights
			if (systemUserInfo == null){ 
				this.destMessage = "You are not authorised to use this service";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			} else if (systemUserInfo.getAccessLevel() < 2){
				this.destMessage = "You are not authorised to use this service";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validateAdminPinCode(phonePinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account PIN";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}
			
			if (phonePinCode.equalsIgnoreCase(HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen()))) {
				this.destMessage = "For your own security please change your PIN before doing any transactions.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			phoneNumber = transElements[2].trim();
			phoneMsisdn = convertMobileNoToMsisdn(phoneNumber);
			if (phoneMsisdn.isEmpty()) {
				this.destMessage = "The phone number for the target customer is not valid.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get target customer's info from msisdn
			this.destCustInfo = CustomerInformation
					.getCustomerAccountInfo(phoneMsisdn);
			if (destCustInfo == null) {
				this.destMessage = String
						.format("This number - %s is not registered with the system. Please contact Customer Service for more information.",
								phoneMsisdn);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			adminAction = transElements[1].trim().replace(asteriskChar,
					spaceChar);
			boolean retUpdateCust = false;
			if (adminAction.equalsIgnoreCase("RESET PIN")) {
				String defaultPinCode = HelperUtils.getDefaultPinCode(
						SystemConfigInfo.getMinPasswordLen(),
						SystemConfigInfo.getMaxPasswordLen());
				retUpdateCust = CustomerInformation.resetCustomerAccountPin(
						phoneMsisdn, defaultPinCode);

				if (!retUpdateCust) {
					this.destMessage = String
							.format("The system encountered problems while reseting the PIN code for the account associated with this number %s.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String
							.format("PIN code reset for the account associated with this number %s successful.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			} else if (adminAction.equalsIgnoreCase("RESET ME2ME")) {
				retUpdateCust = CustomerInformation.resetMe2Me(phoneMsisdn,
						destCustInfo.getCustomerId());
				if (!retUpdateCust) {
					this.destMessage = String
							.format("The system encountered problems while reseting me2me for this number %s.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String
							.format("Me2me reset for the account associated with this number %s successful.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			} else if (adminAction.equalsIgnoreCase("LOCK ACCOUNT")) {
				retUpdateCust = CustomerInformation.lockCustomer(destCustInfo
						.getCustomerId());
				DatabaseHelper.writeToLogFile("console", "ERR: "
						+ retUpdateCust);
				if (!retUpdateCust) {
					this.destMessage = String
							.format("The system encountered problems while locking the account associated with this number %s.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String.format(
							"Account lock for %s successful.", phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			} else if (adminAction.equalsIgnoreCase("UNLOCK ACCOUNT")) {
				retUpdateCust = CustomerInformation.unLockCustomer(destCustInfo
						.getCustomerId());
				if (!retUpdateCust) {
					this.destMessage = String
							.format("The system encountered problems while unlocking the account associated with this number %s.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String.format(
							"Account unlock for %s successful.", phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			} else if (adminAction.equalsIgnoreCase("DEACTIVATE ACCOUNT")) {
				retUpdateCust = CustomerInformation
						.deactivateAccount(destCustInfo.getCustomerId());
				if (!retUpdateCust) {
					this.destMessage = String
							.format("The system encountered problems while deactivating this number %s.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String.format(
							"Account deactivate for %s successful.",
							phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			} else if (adminAction.equalsIgnoreCase("ACTIVATE ACCOUNT")) {
				retUpdateCust = CustomerInformation
						.deactivateAccount(destCustInfo.getCustomerId());
				if (!retUpdateCust) {
					this.destMessage = String
							.format("The system encountered problems while activating this number %s.",
									phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				} else {
					this.destMessage = String.format(
							"Account activate for %s successful.", phoneMsisdn);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					return;
				}
			}

		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/**
	 * Method to Invite Friends to my Network ZIMBA: NTINVITE
	 */
	private void processINVT() {

		// INVT SCHOOLFEES 0779661906,0781058390 4321
		String[] transElements = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String friendsList = "";
		String[] phoneNumbers = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length != 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[transElements.length - 1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[transElements.length - 1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.length() < SystemConfigInfo.getMinPasswordLen()
					&& SystemConfigInfo.getMinPasswordLen() != 0) {
				this.destMessage = "Your PIN is shorter than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.length() > SystemConfigInfo.getMaxPasswordLen()
					&& SystemConfigInfo.getMaxPasswordLen() != 0) {
				this.destMessage = "Your PIN is longer than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			if (pinCode.startsWith(defaultPinCode)) {
				this.destMessage = "You must use a more secure PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the Phone Numbers
			friendsList = transElements[1];
			phoneNumbers = friendsList.split(",");

			String friendMsisdn = "";
			CustomerInformation friendCustInfo = null;

			// Send Invitation SMS for Each Phone Number
			for (int i = 0; i < phoneNumbers.length; i++) {

				// Confirm that the number is valid
				friendMsisdn = convertMobileNoToMsisdn(phoneNumbers[i]);
				if (friendMsisdn.isEmpty()) {
					this.destMessage = String
							.format("The phone number that you specified: %s is invalid. The system could not send an invitation message to your friend.",
									phoneNumbers[i]);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					continue;
				}

				// Check whether the Friend exists as a customer
				friendCustInfo = CustomerInformation
						.getCustomerAccountInfo(friendMsisdn);

				// Confirm that the Friend exists
				if (friendCustInfo == null) {
					this.destMessage = String
							.format("Please support me MTN Mobile Money and join my Zimba network so that we can borrow and lend each other cash using the mobile phone. Thanks. %s.",
									CustomerInformation.getDisplayNames(
											sourceCustInfo, false));
					HelperUtils.sendSMS(friendMsisdn, destMessage, referenceId);
				} else {
					this.destMessage = String
							.format("Please join my Zimba network so that we can borrow and lend each other cash using Mobile Money. Thank you. %s.",
									CustomerInformation.getDisplayNames(
											sourceCustInfo, false));
					HelperUtils.sendSMS(friendMsisdn, destMessage, referenceId);
				}

				// Inform the sender that the Invitation request has been sent
				if (friendCustInfo == null) {
					this.destMessage = String
							.format("Your invitation to %s to join your Zimba network has been sent successfully.",
									phoneNumbers[i]);
				} else {
					this.destMessage = String
							.format("Your invitation to %s to join your Zimba network has been sent successfully.",
									CustomerInformation.getDisplayNames(
											friendCustInfo, false));
				}
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	// ZIMBA METHODS
	/**
	 * Method to Activate ZIMBA: NTACTV
	 */
	private void processNTACTV() {

		// NTACTV SMALL*(5*TO*10*FRIENDS) BOTH 4321
		String[] transElements = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String networkSize = "";
		String networkRole = "";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length != 4) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[transElements.length - 1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[transElements.length - 1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.length() < SystemConfigInfo.getMinPasswordLen()
					&& SystemConfigInfo.getMinPasswordLen() != 0) {
				this.destMessage = "Your PIN is shorter than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.length() > SystemConfigInfo.getMaxPasswordLen()
					&& SystemConfigInfo.getMaxPasswordLen() != 0) {
				this.destMessage = "Your PIN is longer than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			if (pinCode.startsWith(defaultPinCode)) {
				this.destMessage = "You must use a more secure PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			boolean retValCheckZimbaActivation = ZimbaCommon
					.checkZimbaActivation(sourceCustInfo.getCustomerId());
			if (retValCheckZimbaActivation) {
				this.destMessage = "You are already activated on Zimba.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the Network Size
			networkSize = transElements[1];
			networkRole = transElements[2];

			boolean retActivateZimba = ZimbaCommon.activateZimba(
					sourceCustInfo, networkSize, networkRole);

			if (!retActivateZimba) {
				this.destMessage = "The system encountered a problem while activating your Zimba network. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (networkRole.toUpperCase().startsWith("BORROW")) {
				this.destMessage = "You have successfully activated your Zimba network. You can now invite people to join your network and start Borrowing from your friends right from your handset!";
			} else if (networkRole.toUpperCase().startsWith("LEND")) {
				this.destMessage = "You have successfully activated your Zimba network. You can now invite people to join your network and start Lending to your friends right from your handset!";
			} else {
				this.destMessage = "You have successfully activated your Zimba network. You can now invite people to join your network and start Borrowing and Lending right from your handset!";
			}

			HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/**
	 * Method to Invite Friends to my Network ZIMBA: NTINVITE
	 */
	private void processNTINVITE() {

		// NTINVITE 0779661906,0781058390 4321
		String[] transElements = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String friendsList = "";
		String[] phoneNumbers = null;

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length != 3) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[transElements.length - 1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[transElements.length - 1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.length() < SystemConfigInfo.getMinPasswordLen()
					&& SystemConfigInfo.getMinPasswordLen() != 0) {
				this.destMessage = "Your PIN is shorter than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.length() > SystemConfigInfo.getMaxPasswordLen()
					&& SystemConfigInfo.getMaxPasswordLen() != 0) {
				this.destMessage = "Your PIN is longer than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			if (pinCode.startsWith(defaultPinCode)) {
				this.destMessage = "You must use a more secure PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the Phone Numbers
			friendsList = transElements[1];
			phoneNumbers = friendsList.split(",");

			String friendMsisdn = "";
			CustomerInformation friendCustInfo = null;

			// Send Invitation SMS for Each Phone Number
			for (int i = 0; i < phoneNumbers.length; i++) {

				// Confirm that the number is valid
				friendMsisdn = convertMobileNoToMsisdn(phoneNumbers[i]);
				if (friendMsisdn.isEmpty()) {
					this.destMessage = String
							.format("The phone number that you specified: %s is invalid. The system could not send an invitation message to your friend.",
									phoneNumbers[i]);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
					continue;
				}

				// Check whether the Friend exists as a customer
				friendCustInfo = CustomerInformation
						.getCustomerAccountInfo(friendMsisdn);

				// Check whether they are already connected
				if (friendCustInfo != null) {

					ZimbaNetwork existingNetwork = ZimbaNetworks
							.getNetworkForCustomerToFriend(
									sourceCustInfo.getCustomerId(),
									friendCustInfo.getCustomerId());

					if (existingNetwork != null) {

						this.destMessage = String.format(
								"You are already connected to: %s since %s.",
								CustomerInformation.getDisplayNames(
										friendCustInfo, false),
								new SimpleDateFormat("dd MM yyyy")
										.format(existingNetwork
												.getDateConnected()));
						HelperUtils.sendSMS(sourceMsisdn, destMessage,
								referenceId);
						return;
					}
				}

				// Also check whether there is a pending invitation to the
				// friend
				boolean isReminderInvitation = false;
				ZimbaInvitations pendingInvitations = ZimbaInvitations
						.getPendingInvitationsFromCustomerIdToMsisdn(
								sourceCustInfo.getCustomerId(), friendMsisdn);
				if (pendingInvitations.size() > 0) {

					isReminderInvitation = true;
					this.destMessage = String
							.format("You already have a pending invitation to your friend of phone number: %s. A reminder will be sent to your friend.",
									phoneNumbers[i]);
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

					// Send reminder to friend
					this.destMessage = String
							.format("I kindly remind you to respond to my invitation to join my zimba network so that we can borrow and lend each other cash using the mobile phone. Thank you. %s.",
									CustomerInformation.getDisplayNames(
											sourceCustInfo, false));
					HelperUtils.sendSMS(friendMsisdn, destMessage, referenceId);
					continue;
				}

				if (!isReminderInvitation) {

					// Confirm that the Friend exists
					if (friendCustInfo == null) {
						this.destMessage = String
								.format("Please join MTN Mobile Money and join my Zimba network so that we can borrow and lend each other cash using the mobile phone. Thank you. %s.",
										CustomerInformation.getDisplayNames(
												sourceCustInfo, false));
						HelperUtils.sendSMS(friendMsisdn, destMessage,
								referenceId);
					} else {
						this.destMessage = String
								.format("Please join my Zimba network so that we can borrow and lend each other cash using Mobile Money. Thank you. %s.",
										CustomerInformation.getDisplayNames(
												sourceCustInfo, false));
						HelperUtils.sendSMS(friendMsisdn, destMessage,
								referenceId);
					}
				}

				// Inform the sender that the Invitation request has been sent
				if (!isReminderInvitation) {
					if (friendCustInfo == null) {
						this.destMessage = String
								.format("Your invitation to %s to join your Zimba network has been sent successfully.",
										phoneNumbers[i]);
					} else {
						this.destMessage = String
								.format("Your invitation to %s to join your Zimba network has been sent successfully.",
										CustomerInformation.getDisplayNames(
												friendCustInfo, false));
					}
					HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				}

				if (!isReminderInvitation) {

					// Log the Invitation in the Database
					ZimbaInvitation zimbaInvitation = new ZimbaInvitation();
					zimbaInvitation.setCustomerId(sourceCustInfo
							.getCustomerId());
					zimbaInvitation.setFriendMsisdn(friendMsisdn);

					boolean retVal = ZimbaInvitations
							.createZimbaInvitation(zimbaInvitation);
					if (retVal) {

						// Do Notify: But I have already told Customer that
						// invite was sent???? -:)
					}
				}
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}

	/**
	 * Method to Respond to a ZIMBA Network Invitation: NTRESPINV
	 */
	private void processNTRESPINV() {

		// NTRESPINV 0779661904 ACCEPT*INVITATION FAMILY-MEMBER 4321

		String[] transElements = null;
		String pinCode = "";
		String maskedPinCode = "";
		String separatorChar = " ";
		String phoneNumber = "";
		String response = "";
		String friendshipLevel = "";

		try {

			String formedSrcMsisdn = convertMobileNoToMsisdn(this.sourceMsisdn);
			if (formedSrcMsisdn.isEmpty()) {
				this.destMessage = "You are not authorized to use this service.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			this.sourceMsisdn = formedSrcMsisdn;
			transElements = requestCommand.split(separatorChar);
			if (transElements.length != 5) {
				this.destMessage = "Invalid command format. Some parameters are missing.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			pinCode = transElements[transElements.length - 1];
			maskedPinCode = HelperUtils.maskPassword(pinCode);

			// Swap the originalPassword with the maskedPassword
			transElements[transElements.length - 1] = maskedPinCode;

			// Reconstruct the Original string
			String originalRequest = "";

			for (int i = 0; i < transElements.length; i++) {
				originalRequest += transElements[i] + separatorChar;
			}

			if (!logInBoundMessage(originalRequest)) {
				return;
			}

			HelperUtils.writeToLogFile("InBoundMessages", String.format(
					"%s~%s~%s", referenceId, sourceMsisdn, originalRequest));

			this.sourceCustInfo = CustomerInformation
					.getCustomerAccountInfo(sourceMsisdn);
			if (sourceCustInfo == null) {
				this.destMessage = "You are not authorized to use this service. Please contact Customer Service for more information.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (!validatePinCode(pinCode)) {
				this.destMessage = "You have entered an invalid PIN. Note that 3 attempts with invalid PIN will block your account.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			ValidationReturnInformation validSrcCustInfo = CustomerInformation
					.validateCustomerAccountInfo(sourceCustInfo, true);
			if (!validSrcCustInfo.isPassedValidation()) {
				HelperUtils.sendSMS(sourceMsisdn,
						validSrcCustInfo.getValidationMessage(), referenceId);
				return;
			}

			if (pinCode.length() < SystemConfigInfo.getMinPasswordLen()
					&& SystemConfigInfo.getMinPasswordLen() != 0) {
				this.destMessage = "Your PIN is shorter than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			if (pinCode.length() > SystemConfigInfo.getMaxPasswordLen()
					&& SystemConfigInfo.getMaxPasswordLen() != 0) {
				this.destMessage = "Your PIN is longer than the recommended length.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			String defaultPinCode = HelperUtils.getDefaultPinCode(
					SystemConfigInfo.getMinPasswordLen(),
					SystemConfigInfo.getMaxPasswordLen());

			if (pinCode.startsWith(defaultPinCode)) {
				this.destMessage = "You must use a more secure PIN.";
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the Phone Number
			phoneNumber = transElements[1];
			response = transElements[2];
			friendshipLevel = transElements[3];

			// Verify the phoneNumber
			String invitorMsisdn = convertMobileNoToMsisdn(phoneNumber);
			if (invitorMsisdn.isEmpty()) {
				this.destMessage = String
						.format("You have specified an invalid phone number: %s. Kindly confirm the correct number from the invitation Message and try again.",
								phoneNumber);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the details of the Person that made the Invitation
			CustomerInformation invitorCustInfo = CustomerInformation
					.getCustomerAccountInfo(invitorMsisdn);
			if (invitorCustInfo == null) {
				this.destMessage = String
						.format("The system failed to find details for your friend of phone number: %s. Kindly confirm the correct number from the invitation message and try again.",
								phoneNumber);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Check whether they are already connected
			ZimbaNetwork existingNetwork = ZimbaNetworks
					.getNetworkForCustomerToFriend(
							invitorCustInfo.getCustomerId(),
							sourceCustInfo.getCustomerId());
			if (existingNetwork != null) {
				this.destMessage = String.format(
						"You are already connected to: %s since %s.",
						CustomerInformation.getDisplayNames(invitorCustInfo,
								false), new SimpleDateFormat("dd MM yyyy")
								.format(existingNetwork.getDateConnected()));
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Confirm that the Invitation being responded to actually exists
			ZimbaInvitations zimbaInvitations = ZimbaInvitations
					.getPendingInvitationsFromCustomerIdToMsisdn(
							invitorCustInfo.getCustomerId(),
							sourceCustInfo.getMsisdn());
			if (zimbaInvitations == null || zimbaInvitations.size() < 1) {
				this.destMessage = String
						.format("You do not have any pending invitation from the owner of the phone number: %s. If the person is your friend, you can send a Zimba invitation to the person.",
								phoneNumber);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Get the actual invitation: Assume only 1 exists
			ZimbaInvitation zimbaInvitation = zimbaInvitations.get(0);

			boolean respInvite = false;
			if (response.toUpperCase().startsWith("ACCEPT")) {
				respInvite = ZimbaInvitations.respondToZimbaInvitation(
						zimbaInvitation, 1, sourceCustInfo.getCustomerId(),
						friendshipLevel);
			} else {
				respInvite = ZimbaInvitations.respondToZimbaInvitation(
						zimbaInvitation, 2, sourceCustInfo.getCustomerId(),
						friendshipLevel);
			}

			if (!respInvite) {
				this.destMessage = String
						.format("The system encountered a problem while processing your response to the Zimba invitation by: %s. Please try again later or contact Customer Care.",
								CustomerInformation.getDisplayNames(
										invitorCustInfo, false));
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);
				return;
			}

			// Notify the Invitor and Invited that they are now connected or not
			if (response.toUpperCase().startsWith("ACCEPT")) {
				// Message to the Invited
				this.destMessage = String
						.format("Congratulations! You are now connected to: %s. Your %s Zimba Group is growing!.",
								CustomerInformation.getDisplayNames(
										invitorCustInfo, false),
								friendshipLevel);
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

				// Message to the Invitor:
				// TODO: I need to be able to set friendship levels for the
				// Invitor also
				this.destMessage = String
						.format("Congratulations! %s has just accepted your Zimba invitation and you are now connected.",
								CustomerInformation.getDisplayNames(
										sourceCustInfo, false));
				HelperUtils.sendSMS(invitorMsisdn, destMessage, referenceId);

			} else {

				// Message to the Invited
				this.destMessage = String
						.format("Your decline response to the Zimba invitation by %s has been processed successfully. You can still connect later by sending the person an invitation.",
								CustomerInformation.getDisplayNames(
										invitorCustInfo, false));
				HelperUtils.sendSMS(sourceMsisdn, destMessage, referenceId);

				// Message to the Inviter
				this.destMessage = String
						.format("Sorry, %s has declined your Zimba invitation. You can still connect later.",
								CustomerInformation.getDisplayNames(
										sourceCustInfo, false));
				HelperUtils.sendSMS(invitorMsisdn, destMessage, referenceId);
			}
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
		}
	}
}