/*
 * 
 * Copyright (c) 2010 AppLab, Grameen Foundation
 * 
 */

package org.applab.AppLabMoneyCore;

import java.sql.*;
import java.util.Date;
import java.io.*;
import javax.naming.*;

import oracle.jdbc.pool.*;

public class DatabaseHelper {

	private static OracleDataSource ods = null;
	private static Connection conn = null;

	private static String mySqlConnectionString = null;

	public static void setMySqlConnectionString(String value) {
		mySqlConnectionString = value;
	}

	public static Connection getConnection(String targetDatabase)
			throws NamingException, SQLException {

		if (targetDatabase.toUpperCase().equalsIgnoreCase("ORACLE")) {
			return getOracleConnection();
		} else if (targetDatabase.toUpperCase().equalsIgnoreCase("MYSQL")) {
			return getMySqlConnection();
		} else if (targetDatabase.toUpperCase().equalsIgnoreCase("MSSQL")) {
			return getMsSqlConnection();
		} else {
			return getMySqlConnection();
		}
	}

	private static Connection getOracleConnection() throws NamingException,
			SQLException {
		try {
			if (conn != null && !conn.isClosed()) {
				return conn;
			}
			// Otherwise create the connection
			ods = new OracleDataSource();
			ods.setUser("APPLABMONEY");
			ods.setPassword("APPLABMONEY");
			ods.setDriverType("thin");
			ods.setDatabaseName("PBSLDB");
			ods.setServerName("Moses-PC");
			ods.setPortNumber(1521);
			ods.setDescription("AppLab Money Database Schema");
			conn = ods.getConnection();

			return conn;
		} catch (SQLException ex) {
			DatabaseHelper.writeToLogFile("console", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return conn;
		}
	}

	private static Connection getMySqlConnection() throws NamingException,
			SQLException {
		try {
			if (conn != null && !conn.isClosed()) {
				return conn;
			}

			// Otherwise create the connection
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			conn = DriverManager.getConnection(mySqlConnectionString);
			// conn = DriverManager
			// .getConnection("jdbc:mysql://localhost/applabmoney?"
			// + "user=root&password=g5*vTys-D2");

			// DatabaseHelper.writeToLogFile("console", "DB CONNECTION STR->" +
			// mySqlConnectionString);
			return conn;
		} catch (Exception ex) {
			DatabaseHelper
					.writeToLogFile(
							"console",
							"DatabaseHelper::getMySqlConnection->CONNSTR="
									+ mySqlConnectionString + ": ERR->"
									+ ex.getMessage() + " TRACE: "
									+ ex.getStackTrace());
			return conn;
		}
	}

	private static Connection getMsSqlConnection() throws NamingException,
			SQLException {
		try {
			if (conn != null && !conn.isClosed()) {
				return conn;
			}
			// Otherwise create the connection
			ods = new OracleDataSource();
			ods.setUser("APPLABMONEY");
			ods.setPassword("APPLABMONEY");
			ods.setDriverType("thin");
			ods.setDatabaseName("PBSLDB");
			ods.setServerName("Moses-PC");
			ods.setPortNumber(1521);
			ods.setDescription("AppLab Money Database Schema");
			conn = ods.getConnection();

			return conn;
		} catch (SQLException ex) {
			DatabaseHelper.writeToLogFile("console", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return conn;
		}
	}

	public static void writeToLogFile(String targetFolder, String theData) {
		File targetFile = null;
		File targetDirectory = null;
		String logDirectoryPath = null;
		String fileName = null;
		String timestamp = null;
		String finalRecordData = null;
		FileOutputStream strm = null;

		try {
			if (targetFolder.compareToIgnoreCase("console") == 0) {

				// Write on the Console Window
				System.out.println(theData);
			} else {

				// Write to the File and the specified targetFolder
				logDirectoryPath = new File("Log").getAbsolutePath();

				// Now build the FileName based on the time
				long currentTime = System.currentTimeMillis();
				Date dateTime = new Date(currentTime);
				fileName = String.format("%1$td%1$tb%1$tY@%1$tH00.txt",
						dateTime);

				// Target Folder
				targetDirectory = new File(logDirectoryPath + File.separator
						+ targetFolder);

				// If it doesn't exist create it
				if (!targetDirectory.exists()) {
					boolean retVal = targetDirectory.mkdirs();
					if (!retVal) {
						// A problem occurred. For now just exit.
						return;
					}
				}

				// Create the file
				targetFile = new File(targetDirectory.getAbsolutePath()
						+ File.separator + fileName);

				// Append Time and Line Separator to the Data Record
				timestamp = String.format("[%1$tH:%1$tM:%1$tS]", dateTime);
				finalRecordData = String.format("%s %s%s", timestamp,
						theData.trim(), "\r\n");

				// Now Write the Record to the file
				strm = new FileOutputStream(targetFile, true);
				new PrintStream(strm).print(finalRecordData);

				// close the stream
				strm.close();
			}
		} catch (Exception ex) {
			System.err.println("ERR: " + ex.getMessage() + " TRACE: "
					+ ex.getStackTrace());
		} finally {
			if (strm != null) {
				try {
					strm.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * Procedure to log the outbound messages into the database
	 */
	public static boolean logOutBoundMessage(String referenceId,
			String destMsisdn, String messageText, String deliveryStatus)
			throws SQLException {
		Connection cn = null;
		PreparedStatement stm = null;
		StringBuilder sb = null;
		int dbStatusCode = 0;
		String sqlQuery = "";

		try {
			// If the data fields are empty, return false
			if (destMsisdn.trim().isEmpty() || referenceId.trim().isEmpty()
					|| messageText.trim().isEmpty()) {
				return false;
			}

			if (deliveryStatus.trim().isEmpty()) {
				deliveryStatus = "FAILED";
			}

			// Otherwise, get the Connection
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			sb = new StringBuilder();
			sb.append("INSERT INTO OUTBOUND_MESSAGES(OUTBOUND_MESSAGE_ID, REFERENCE_ID, CREATED_TIMESTAMP, DEST_MSISDN, MESSAGE, DELIVERY_STATUS) ");
			sb.append(" VALUES(NULL, '%s', SYSDATE(), '%s', '%s', '%s')");
			sqlQuery = sb.toString();

			stm = cn.prepareStatement(String.format(sqlQuery, referenceId,
					destMsisdn, messageText, deliveryStatus));

			// Execute the Query
			dbStatusCode = stm.executeUpdate();

			cn.commit();

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
