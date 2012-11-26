package org.applab.AppLabMoneyCore;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerInformation {
	private long customerId;
	private String msisdn;
	private String aliasName;
	private String lastName;
	private String otherNames;
	private String companyName;
	private int accountTypeBitmap;
	private String pinCode;
	private int kycStatusFlg;
	private boolean isEnabled;
	private boolean isLocked;
	private int registrarCustomerId;
	private String referenceTree;
	private String agentCode;
	private int invalidPinCount;
	private AccountInformation AccountInfo;

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getAccountTypeBitmap() {
		return accountTypeBitmap;
	}

	public void setAccountTypeBitmap(int accountTypeBitmap) {
		this.accountTypeBitmap = accountTypeBitmap;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	public int getKycStatusFlg() {
		return kycStatusFlg;
	}

	public void setKycStatusFlg(int kycStatusFlg) {
		this.kycStatusFlg = kycStatusFlg;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public int getRegistrarCustomerId() {
		return registrarCustomerId;
	}

	public void setRegistrarCustomerId(int registrarCustomerId) {
		this.registrarCustomerId = registrarCustomerId;
	}

	public String getReferenceTree() {
		return referenceTree;
	}

	public void setReferenceTree(String referenceTree) {
		this.referenceTree = referenceTree;
	}

	public String getAgentCode() {
		return agentCode;
	}

	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}

	public int getInvalidPinCount() {
		return invalidPinCount;
	}

	public void setInvalidPinCount(int invalidPinCount) {
		this.invalidPinCount = invalidPinCount;
	}

	public AccountInformation getAccountInfo() {
		return AccountInfo;
	}

	public void setAccountInfo(AccountInformation accountInfo) {
		AccountInfo = accountInfo;
	}

	public static CustomerInformation getNewCustomerInfo(String targetMsisdn) {
		Connection cn = null;
		CustomerInformation custInfo = null;
		StringBuffer sb = null;

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			sb = new StringBuffer();
			sb.append("SELECT C.CUSTOMER_ID,C.MSISDN,C.ALIAS_NAME,C.AGENT_CODE,C.LAST_NAME,C.OTHER_NAMES,C.ACCOUNT_TYPE_BITMAP,C.PIN_CODE,");
			sb.append("C.KYC_STATUS_FLG,C.ENABLED_FLG,C.LOCKED_FLG,C.REGISTRAR_CUSTOMER_ID,C.REFERENCE_TREE,C.INVALID_PIN_COUNT,C.COMPANY_NAME");
			sb.append(" FROM CUSTOMERS C ");
			sb.append(" WHERE C.MSISDN=%s");

			PreparedStatement stm = cn.prepareStatement(String.format(
					sb.toString(), targetMsisdn));

			ResultSet result = stm.executeQuery();
			while (result.next()) {
				custInfo = new CustomerInformation();
				custInfo.setAccountTypeBitmap(result
						.getInt("ACCOUNT_TYPE_BITMAP"));
				custInfo.setCustomerId(result.getInt("CUSTOMER_ID"));
				if (result.getInt("ENABLED_FLG") == 1) {
					custInfo.setEnabled(true);
				} else {
					custInfo.setEnabled(false);
				}
				custInfo.setMsisdn((result.getString("MSISDN") == null) ? ""
						: result.getString("MSISDN").trim());
				custInfo.setPinCode((result.getString("PIN_CODE") == null) ? ""
						: result.getString("PIN_CODE").trim());

			}
			return custInfo;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static CustomerInformation getCustomerAccountInfo(String targetMsisdn) {
		Connection cn = null;
		CustomerInformation custInfo = null;
		AccountInformation accountInfo = null;
		StringBuffer sb = null;

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			sb = new StringBuffer();
			sb.append("SELECT C.CUSTOMER_ID,C.MSISDN,C.ALIAS_NAME,C.AGENT_CODE,C.LAST_NAME,C.OTHER_NAMES,C.ACCOUNT_TYPE_BITMAP,C.PIN_CODE,");
			sb.append("C.KYC_STATUS_FLG,C.ENABLED_FLG,C.LOCKED_FLG,C.REGISTRAR_CUSTOMER_ID,C.REFERENCE_TREE,C.INVALID_PIN_COUNT,C.COMPANY_NAME,");
			sb.append(" A.ACCOUNT_ID,A.BOOK_BALANCE, A.BLOCKED_BALANCE ");
			sb.append(" FROM CUSTOMERS C INNER JOIN ACCOUNTS A ON C.CUSTOMER_ID=A.CUSTOMER_ID ");
			sb.append(" WHERE C.MSISDN='%s'");

			PreparedStatement stm = cn.prepareStatement(String.format(
					sb.toString(), targetMsisdn));

			ResultSet result = stm.executeQuery();
			while (result.next()) {
				custInfo = new CustomerInformation();
				custInfo.setAccountTypeBitmap(result
						.getInt("ACCOUNT_TYPE_BITMAP"));
				custInfo.setAgentCode((result.getString("AGENT_CODE") == null) ? ""
						: result.getString("AGENT_CODE").trim());
				custInfo.setAliasName((result.getString("ALIAS_NAME") == null) ? ""
						: result.getString("ALIAS_NAME").trim());
				custInfo.setCompanyName((result.getString("COMPANY_NAME") == null) ? ""
						: result.getString("COMPANY_NAME").trim());
				custInfo.setCustomerId(result.getInt("CUSTOMER_ID"));
				if (result.getInt("ENABLED_FLG") == 1) {
					custInfo.setEnabled(true);
				} else {
					custInfo.setEnabled(false);
				}
				custInfo.setInvalidPinCount(result.getInt("INVALID_PIN_COUNT"));
				custInfo.setKycStatusFlg(result.getInt("KYC_STATUS_FLG"));
				custInfo.setLastName((result.getString("LAST_NAME") == null) ? ""
						: result.getString("LAST_NAME").trim());
				custInfo.setOtherNames((result.getString("OTHER_NAMES") == null) ? ""
						: result.getString("OTHER_NAMES").trim());
				if (result.getInt("LOCKED_FLG") == 1) {
					custInfo.setLocked(true);
				} else {
					custInfo.setLocked(false);
				}
				custInfo.setMsisdn((result.getString("MSISDN") == null) ? ""
						: result.getString("MSISDN").trim());
				custInfo.setPinCode((result.getString("PIN_CODE") == null) ? ""
						: result.getString("PIN_CODE").trim());
				custInfo.setReferenceTree((result.getString("REFERENCE_TREE") == null) ? ""
						: result.getString("REFERENCE_TREE").trim());
				custInfo.setRegistrarCustomerId(result
						.getInt("REGISTRAR_CUSTOMER_ID"));

				accountInfo = new AccountInformation();
				accountInfo.setAccountId(result.getInt("ACCOUNT_ID"));
				accountInfo.setCustomerId(result.getInt("CUSTOMER_ID"));
				accountInfo.setBookBalance(result.getDouble("BOOK_BALANCE"));
				accountInfo.setBlockedBalance(result
						.getDouble("BLOCKED_BALANCE"));

				custInfo.setAccountInfo(accountInfo);

			}
			return custInfo;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static CustomerInformation getCustomerAccountInfoByAgentCode(
			String targetAgentCode) {
		Connection cn = null;
		CustomerInformation custInfo = null;
		AccountInformation accountInfo = null;
		StringBuffer sb = null;

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			sb = new StringBuffer();
			sb.append("SELECT C.CUSTOMER_ID,C.MSISDN,C.ALIAS_NAME,C.AGENT_CODE,C.LAST_NAME,C.OTHER_NAMES,C.ACCOUNT_TYPE_BITMAP,C.PIN_CODE,");
			sb.append("C.KYC_STATUS_FLG,C.ENABLED_FLG,C.LOCKED_FLG,C.REGISTRAR_CUSTOMER_ID,C.REFERENCE_TREE,C.INVALID_PIN_COUNT,C.COMPANY_NAME,");
			sb.append(" A.ACCOUNT_ID,A.BOOK_BALANCE, A.BLOCKED_BALANCE ");
			sb.append(" FROM CUSTOMERS C INNER JOIN ACCOUNTS A ON C.CUSTOMER_ID=A.CUSTOMER_ID ");
			sb.append(" WHERE C.AGENT_CODE='%s'");

			PreparedStatement stm = cn.prepareStatement(String.format(
					sb.toString(), targetAgentCode));

			ResultSet result = stm.executeQuery();
			while (result.next()) {
				custInfo = new CustomerInformation();
				custInfo.setAccountTypeBitmap(result
						.getInt("ACCOUNT_TYPE_BITMAP"));
				custInfo.setAgentCode((result.getString("AGENT_CODE") == null) ? ""
						: result.getString("AGENT_CODE").trim());
				custInfo.setAliasName((result.getString("ALIAS_NAME") == null) ? ""
						: result.getString("ALIAS_NAME").trim());
				custInfo.setCompanyName((result.getString("COMPANY_NAME") == null) ? ""
						: result.getString("COMPANY_NAME").trim());
				custInfo.setCustomerId(result.getInt("CUSTOMER_ID"));
				if (result.getInt("ENABLED_FLG") == 1) {
					custInfo.setEnabled(true);
				} else {
					custInfo.setEnabled(false);
				}
				custInfo.setInvalidPinCount(result.getInt("INVALID_PIN_COUNT"));
				custInfo.setKycStatusFlg(result.getInt("KYC_STATUS_FLG"));
				custInfo.setLastName((result.getString("LAST_NAME") == null) ? ""
						: result.getString("LAST_NAME").trim());
				custInfo.setOtherNames((result.getString("OTHER_NAMES") == null) ? ""
						: result.getString("OTHER_NAMES").trim());
				if (result.getInt("LOCKED_FLG") == 1) {
					custInfo.setLocked(true);
				} else {
					custInfo.setLocked(false);
				}
				custInfo.setMsisdn((result.getString("MSISDN") == null) ? ""
						: result.getString("MSISDN").trim());
				custInfo.setPinCode((result.getString("PIN_CODE") == null) ? ""
						: result.getString("PIN_CODE").trim());
				custInfo.setReferenceTree((result.getString("REFERENCE_TREE") == null) ? ""
						: result.getString("REFERENCE_TREE").trim());
				custInfo.setRegistrarCustomerId(result
						.getInt("REGISTRAR_CUSTOMER_ID"));

				accountInfo = new AccountInformation();
				accountInfo.setAccountId(result.getInt("ACCOUNT_ID"));
				accountInfo.setCustomerId(result.getInt("CUSTOMER_ID"));
				accountInfo.setBookBalance(result.getDouble("BOOK_BALANCE"));
				accountInfo.setBlockedBalance(result
						.getDouble("BLOCKED_BALANCE"));

				custInfo.setAccountInfo(accountInfo);

			}
			return custInfo;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static boolean createCustomerAccount(String phoneMsisdn,
			String lastName, String otherNames, String physicalAddress,
			String homeDistrict, String idType, String idNo,
			String nationality, Date dateOfBirth, String placeOfBirth,
			String defaultPinCode, long registrarCustomerId) {
		Connection cn = null;
		StringBuilder sb = null;
		String sqlQuery = "";
		PreparedStatement stm = null;
		int dbStatusCode = 0;
		try {

			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("INSERT INTO CUSTOMERS (CUSTOMER_ID, MSISDN, LAST_NAME, OTHER_NAMES, PHYSICAL_ADDRESS, TOWN, ID_TYPE,IDNO, NATIONALITY, ");
			sb.append(" DATE_OF_BIRTH, PLACE_OF_BIRTH, PIN_CODE, REGISTRAR_CUSTOMER_ID, ACCOUNT_TYPE_BITMAP, DATE_REGISTERED, ");
			sb.append(" REGISTERED_BY_USER, KYC_STATUS_FLG, ENABLED_FLG) ");
			sb.append(" VALUES(NULL, '%s','%s','%s','%s','%s','%s','%s','%s',SYSDATE(),'%s','%s','%s',1,SYSDATE(), 'SYSTEM', 1,1)");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, phoneMsisdn,
					lastName, otherNames, physicalAddress, homeDistrict,
					idType, idNo, nationality, placeOfBirth, defaultPinCode,
					Long.toString(registrarCustomerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			return ((dbStatusCode > 0) ? true : false);

		} catch (Exception ex) {
			HelperUtils.writeToLogFile("console", "ERR: " + ex.getMessage()
					+ " TRACE: " + HelperUtils.convertStackTraceToString(ex));
			return false;
		}
	}

	public static ValidationReturnInformation validateCustomerAccountInfo(
			CustomerInformation theCustAccountInfo,
			boolean targetCustAccountIsSource) {
		// Flag that it is Invalid
		boolean isValidAccount = false;
		String destMessage = "";

		ValidationReturnInformation validRetInfo = null;

		try {
			validRetInfo = new ValidationReturnInformation(false, "", null);
			if (targetCustAccountIsSource) {
				if (theCustAccountInfo == null) {
					destMessage = "You are not authorized to use this service. Please contact Customer Care.";
					isValidAccount = false;
				} else if (theCustAccountInfo.isLocked()
						|| !theCustAccountInfo.isEnabled()) {
					destMessage = "Your Account is Blocked from doing transactions. Please contact Customer Care.";
					isValidAccount = false;
				} else {
					isValidAccount = true;
				}
			} else {
				// Check Customer.AccountStatus and Account.EnabledFlg
				if (theCustAccountInfo == null) {
					destMessage = "The Destination Account does not exist.";
					isValidAccount = false;
				} else if (!theCustAccountInfo.isEnabled()) {
					destMessage = "The Destination Account is Blocked from doing transactions.";
					isValidAccount = false;
				} else {
					isValidAccount = true;
				}
			}

			validRetInfo.setPassedValidation(isValidAccount);
			validRetInfo.setValidationMessage(destMessage);
			return validRetInfo;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return validRetInfo;
		}
	}

	public static boolean createAccount(long customerId, int accountTypeBitmap) {
		Connection cn = null;
		StringBuilder sb = null;
		String sqlQuery = "";
		PreparedStatement stm = null;
		int dbStatusCode = 0;
		try {

			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("INSERT INTO ACCOUNTS (ACCOUNT_ID,CUSTOMER_ID, ACCOUNT_TYPE_BITMAP,DATE_CREATED, BOOK_BALANCE, BLOCKED_BALANCE, ENABLED_FLG)");
			sb.append(" VALUES(NULL, '%s','%s', SYSDATE(),0.00,0.00,1)");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId),
					Integer.toString(accountTypeBitmap)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			return ((dbStatusCode > 0) ? true : false);

		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return false;
		}
	}

	public static boolean changePinCode(long customerId, String newPinCode)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET PIN_CODE='%s' WHERE CUSTOMER_ID = %s ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, newPinCode,
					customerId));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			// Will work OK with SProcs
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

	public static boolean lockCustomer(long customerId) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET LOCKED_FLG= 1, LOCKED_REASON = 'INVALID PINCODE' WHERE CUSTOMER_ID = %s ");
			sqlQuery = sb.toString();

			
			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();
			DatabaseHelper.writeToLogFile("console", "ERR: " + "In lock customer3");
			// Will work OK with SProcs
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

	public static boolean resetInvalidPinCount(long customerId)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET INVALID_PIN_COUNT= 0 WHERE CUSTOMER_ID = %s ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			// Will work OK with SProcs
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

	public static boolean resetCustomerAccountPin(String phoneMsisdn,
			String defaultPinCode) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET INVALID_PIN_COUNT= 0, PIN_CODE = %s WHERE MSISDN = %s ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, defaultPinCode,
					phoneMsisdn));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			// Will work OK with SProcs
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

	public static boolean resetMe2Me(String phoneMsisdn, long customerId)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("DELETE FROM ME2ME_TRANSACTIONS WHERE SRC_MSISDN=%s");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, phoneMsisdn));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			sb.append("DELETE FROM ME2ME_GOALS WHERE CUSTOMER_ID= %s");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			sb.append("DELETE FROM ME2ME_CUSTOMERS WHERE CUSTOMER_ID= %s");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			// Will work OK with SProcs
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

	public static String getDisplayNames(CustomerInformation theCustomerInfo,
			boolean preferCompanyName) {
		String displayName = "";
		String otherNames = "";
		String lastName = "";
		String fullNames = "";

		try {
			if (theCustomerInfo == null) {
				return displayName;
			}
			displayName = theCustomerInfo.getMsisdn();
			otherNames = ((theCustomerInfo.getOtherNames() == null) ? ""
					: theCustomerInfo.getOtherNames());
			lastName = ((theCustomerInfo.getLastName() == null) ? ""
					: theCustomerInfo.getLastName());
			fullNames = lastName.concat(" ").concat(otherNames);

			if (preferCompanyName && theCustomerInfo.getCompanyName() != null) {
				displayName = theCustomerInfo.getCompanyName();
			} else {
				if (SystemConfigInfo.getSmsMsgSubDisplayFormat() == 2) {
					displayName = theCustomerInfo.getMsisdn().concat(" ")
							.concat(fullNames);
				} else if (SystemConfigInfo.getSmsMsgSubDisplayFormat() == 1) {
					displayName = fullNames;
				} else {
					displayName = theCustomerInfo.getMsisdn();
				}
			}

			return displayName;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return displayName;
		}
	}

	public static boolean unLockCustomer(long customerId) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET LOCKED_FLG = 0, LOCKED_REASON = ' ', INVALID_PIN_COUNT = 0 WHERE CUSTOMER_ID = %s ");
			sqlQuery = sb.toString();
			HelperUtils.writeToLogFile("Console", "ERR: " + "unlock");
			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();
			HelperUtils.writeToLogFile("Console", "ERR: " + "unlock2");
			// Will work OK with SProcs
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

	public static boolean deactivateAccount(long customerId)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET ENABLED_FLG= 0 WHERE CUSTOMER_ID = %s");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			// Will work OK with SProcs
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
	
	public static boolean activateAccount(long customerId)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE CUSTOMERS SET ENABLED_FLG= 1 WHERE CUSTOMER_ID = %s");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(customerId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			// Will work OK with SProcs
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

}