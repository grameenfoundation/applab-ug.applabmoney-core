package org.applab.AppLabMoneyCore;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInformation {

	private long userId;
	private long groupId;
	private String userName;
	private String lastName;
	private String otherNames;
	private String password;
	private String phonePinCode;
	private String authMsisdn;
	private boolean isEnabled;
	private boolean isLocked;
	private int invalidPinCount;
	private int accessLevel;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhonePinCode() {
		return phonePinCode;
	}

	public void setPhonePinCode(String phonePinCode) {
		this.phonePinCode = phonePinCode;
	}

	public String getAuthMsisdn() {
		return authMsisdn;
	}

	public void setAuthMsisdn(String authMsisdn) {
		this.authMsisdn = authMsisdn;
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

	public int getInvalidPinCount() {
		return invalidPinCount;
	}

	public void setInvalidPinCount(int invalidPinCount) {
		this.invalidPinCount = invalidPinCount;
	}

	public int getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
	}

	public static UserInformation getUserInfo(String targetMsisdn) {
		Connection cn = null;
		UserInformation userInfo = new UserInformation();
		StringBuffer sb = null;

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
			sb = new StringBuffer();
			sb.append("SELECT U.USER_ID, U.GROUP_ID, U.USERNAME, ");
			sb.append("U.LAST_NAME, U.OTHER_NAMES, U.PASSWORD, U.AUTHORIZED_MSISDN, ");
			sb.append("U.PHONE_PIN_CODE, U.ENABLED_FLG, U.LOCKED_FLG, G.MODULE_ACCESS_FLG");
			sb.append(" FROM USERS U INNER JOIN USER_GROUPS G ON U.GROUP_ID=G.GROUP_ID");
			sb.append(" WHERE U.AUTHORIZED_MSISDN='%s'");

			PreparedStatement stm = cn.prepareStatement(String.format(
					sb.toString(), targetMsisdn));

			ResultSet result = stm.executeQuery();
			while (result.next()) {

				userInfo.setUserId(result.getInt("USER_ID"));
				userInfo.setGroupId(result.getInt("GROUP_ID"));
				userInfo.setUserName((result.getString("USERNAME") == null) ? ""
						: result.getString("USERNAME").trim());
				userInfo.setLastName((result.getString("LAST_NAME") == null) ? ""
						: result.getString("LAST_NAME").trim());
				userInfo.setOtherNames((result.getString("OTHER_NAMES") == null) ? ""
						: result.getString("OTHER_NAMES").trim());
				if (result.getInt("ENABLED_FLG") == 1) {
					userInfo.setEnabled(true);
				} else {
					userInfo.setEnabled(false);
				}
				if (result.getInt("LOCKED_FLG") == 1) {
					userInfo.setLocked(true);
				} else {
					userInfo.setLocked(false);
				}
				userInfo.setAuthMsisdn((result.getString("AUTHORIZED_MSISDN") == null) ? ""
						: result.getString("AUTHORIZED_MSISDN").trim());
				userInfo.setPhonePinCode((result.getString("PHONE_PIN_CODE") == null) ? ""
						: result.getString("PHONE_PIN_CODE").trim());
				userInfo.setAccessLevel(result.getInt("MODULE_ACCESS_FLG"));
			}
			return userInfo;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static boolean resetInvalidPinCount(long userId) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE USERS SET INVALID_PIN_COUNT= 0 WHERE USER_ID = %s ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(userId)));

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

	public static boolean lockUser(long userId) throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {

			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("UPDATE USERS SET LOCKED_FLG= 1, LOCKED_REASON = 'INVALID PINCODE' WHERE USER_ID = %s ");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery,
					Long.toString(userId)));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();
			DatabaseHelper.writeToLogFile("console", "ERR: "
					+ "In lock customer3");

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
