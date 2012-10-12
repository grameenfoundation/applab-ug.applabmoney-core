package org.applab.AppLabMoneyCore.Me2Me;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class MeToMeGoals extends ArrayList<MeToMeGoal> {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public static MeToMeGoals getAllGoals() {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");

			String sqlQuery = sb.toString();
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getAllGoalsByGoalTypeId(int goalTypeId) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE GOAL_TYPE_ID=%s ");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(goalTypeId));
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getAllGoalsByCustomerId(long customerId) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE CUSTOMER_ID=%s ");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(customerId));
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getAllActiveGoals() {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE IS_MATURED=0 AND IS_STOPPED=0 ");

			String sqlQuery = sb.toString();
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getActiveGoalsByGoalTypeId(int goalTypeId) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE GOAL_TYPE_ID = %s AND IS_MATURED=0 AND IS_STOPPED=0 ");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(goalTypeId));
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getActiveGoalsByCustomerAndGoalType(
			long customerId, String goalType) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE CUSTOMER_ID=%s AND IS_MATURED=0 AND IS_STOPPED=0 ");
			sb.append(" AND GOAL_TYPE_ID = (SELECT GOAL_TYPE_ID FROM ME2ME_GOAL_TYPES WHERE UPPER(GOAL_TYPE_CODE) = UPPER('%s') OR UPPER(GOAL_TYPE_NAME)=UPPER('%s') LIMIT 1)");

			// TODO: I may have to use GOAL_NAME instead of GOAL_TYPE_ID to
			// avoid trouble
			String sqlQuery = String.format(sb.toString(),
					Long.toString(customerId), goalType, goalType);
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getActiveGoalsByCustomerAndGoalCode(
			long customerId, String goalCode) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE CUSTOMER_ID=%s AND UPPER(GOAL_CODE) = UPPER('%s')");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(customerId), goalCode);
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getActiveGoalsByCustomerId(long customerId) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE CUSTOMER_ID=%s AND IS_MATURED=0 AND IS_STOPPED=0");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(customerId));
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	public static MeToMeGoals getUnRedeemedGoalsByCustomerAndGoalType(
			long customerId, String goalType) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE CUSTOMER_ID=%s AND IS_REDEEMED=0");
			sb.append(" AND GOAL_TYPE_ID = (SELECT GOAL_TYPE_ID FROM ME2ME_GOAL_TYPES WHERE UPPER(GOAL_TYPE_CODE) = UPPER('%s') OR UPPER(GOAL_TYPE_NAME)=UPPER('%s') LIMIT 1)");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(customerId), goalType, goalType);
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	/**
	 * @param customerId
	 * @return
	 */
	public static MeToMeGoals getUnRedeemedGoalsByCustomer(long customerId) {
		MeToMeGoals meToMeGoals = null;
		StringBuffer sb = null;

		try {
			sb = new StringBuffer();
			sb.append("SELECT GOAL_ID, CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_TYPE_ID,GOAL_NAME, GOAL_CODE, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION, ACCRUED_AMOUNT, ");
			sb.append(" IS_MATURED, IS_REDEEMED, DATE_REDEEMED, AMOUNT_REDEEMED, IS_STOPPED, DATE_STOPPED ");
			sb.append(" FROM ME2ME_GOALS ");
			sb.append(" WHERE CUSTOMER_ID=%s AND IS_REDEEMED=0");

			String sqlQuery = String.format(sb.toString(),
					Long.toString(customerId));
			meToMeGoals = executeQueryToRetrieveMeToMeGoals(sqlQuery);
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}

	private static MeToMeGoals executeQueryToRetrieveMeToMeGoals(String sqlQuery) {
		Connection cn = null;
		MeToMeGoals meToMeGoals = null;
		try {
			meToMeGoals = new MeToMeGoals();
			cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);

			PreparedStatement stm = cn.prepareStatement(sqlQuery);

			ResultSet result = stm.executeQuery();
			while (result.next()) {
				MeToMeGoal meToMeGoal = new MeToMeGoal();
				meToMeGoal
						.setAlertOption((result.getString("ALERT_OPTION") == null) ? ""
								: result.getString("ALERT_OPTION").trim());
				meToMeGoal.setCreatedTimestamp((result
						.getDate("CREATED_TIMESTAMP") == null) ? null : result
						.getDate("CREATED_TIMESTAMP"));
				meToMeGoal.setCustomerId(result.getLong("CUSTOMER_ID"));
				meToMeGoal
						.setGoalCode((result.getString("GOAL_CODE") == null) ? ""
								: result.getString("GOAL_CODE").trim());
				meToMeGoal.setGoalId(result.getInt("GOAL_ID"));
				meToMeGoal
						.setGoalName((result.getString("GOAL_NAME") == null) ? ""
								: result.getString("GOAL_NAME").trim());
				meToMeGoal.setGoalTypeId(result.getInt("GOAL_TYPE_ID"));
				meToMeGoal.setTargetAmount(result.getDouble("TARGET_AMOUNT"));
				meToMeGoal.setAccruedAmount(result.getDouble("ACCRUED_AMOUNT"));
				meToMeGoal.setLiquidityOption((result
						.getString("LIQUIDITY_OPTION") == null) ? "" : result
						.getString("LIQUIDITY_OPTION").trim());
				meToMeGoal
						.setMaturityDate((result.getDate("MATURITY_DATE") == null) ? null
								: result.getDate("MATURITY_DATE"));
				if (result.getInt("IS_MATURED") == 1) {
					meToMeGoal.setIsMatured(true);
				}
				if (result.getInt("IS_REDEEMED") == 1) {
					meToMeGoal.setIsRedeemed(true);
					meToMeGoal
							.setDateRedeemed((result.getDate("DATE_REDEEMED") == null) ? null
									: result.getDate("DATE_REDEEMED"));
				}
				meToMeGoals.add(meToMeGoal);
			}
			return meToMeGoals;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return null;
		}
	}
}
