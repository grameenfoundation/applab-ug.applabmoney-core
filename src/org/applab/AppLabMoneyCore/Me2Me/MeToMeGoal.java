package org.applab.AppLabMoneyCore.Me2Me;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.applab.AppLabMoneyCore.CustomerInformation;
import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class MeToMeGoal {
    private int goalId = 0;
    private long customerId = 0;
    private Date createdTimestamp = null;
    private int goalTypeId = 0;
    private String goalName = "";
    private String goalCode = "";
    private double targetAmount = 0.00;
    private double accruedAmount = 0.00;
    private Date maturityDate = null;
    private String alertOption = "";
    private String liquidityOption = "";
    private boolean isMatured = false;
    private boolean isStopped = false;
    private boolean isRedeemed = false;
    private double redeemedAmount = 0.00;
    private Date dateRedeemed = null;
    private Date dateStopped = null;
    private Date lastCreditDate = null;
    private Date lastDebitDate = null;

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public int getGoalTypeId() {
        return goalTypeId;
    }

    public void setGoalTypeId(int goalTypeId) {
        this.goalTypeId = goalTypeId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public String getGoalCode() {
        return goalCode;
    }

    public void setGoalCode(String goalCode) {
        this.goalCode = goalCode;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(Date maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getAlertOption() {
        return alertOption;
    }

    public void setAlertOption(String alertOption) {
        this.alertOption = alertOption;
    }

    public String getLiquidityOption() {
        return liquidityOption;
    }

    public void setLiquidityOption(String liquidityOption) {
        this.liquidityOption = liquidityOption;
    }

    public boolean isMatured() {
        return isMatured;
    }

    public void setIsMatured(boolean isMatured) {
        this.isMatured = isMatured;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setIsStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public boolean isRedeemed() {
        return isRedeemed;
    }

    public void setIsRedeemed(boolean isRedeemed) {
        this.isRedeemed = isRedeemed;
    }

    public double getRedeemedAmount() {
        return redeemedAmount;
    }

    public void setRedeemedAmount(double redeemedAmount) {
        this.redeemedAmount = redeemedAmount;
    }

    public Date getDateRedeemed() {
        return dateRedeemed;
    }

    public void setDateRedeemed(Date dateRedeemed) {
        this.dateRedeemed = dateRedeemed;
    }

    public Date getDateStopped() {
        return dateStopped;
    }

    public void setDateStopped(Date dateStopped) {
        this.dateStopped = dateStopped;
    }
    
    public static boolean createMeToMeGoal(CustomerInformation sourceCustInfo, String goalType, double targetAmount, Date maturityDate, String alertOption, String liquidityOption) {
        MeToMeGoal meToMeGoal = new MeToMeGoal();
        meToMeGoal.alertOption = alertOption;
        meToMeGoal.createdTimestamp = Calendar.getInstance().getTime();
        meToMeGoal.customerId = sourceCustInfo.getCustomerId();
        meToMeGoal.goalCode = goalType;
        meToMeGoal.goalName = goalType;
        meToMeGoal.liquidityOption = liquidityOption;
        meToMeGoal.maturityDate = maturityDate;
        meToMeGoal.targetAmount = targetAmount;
        
        return meToMeGoal.createGoal();
    } 

    public boolean createGoal() {
        Connection cn = null;
        StringBuilder sb = null;
        String sqlQuery = "";
        PreparedStatement stm = null;
        int dbStatusCode = 0;
        
        try {
            if(customerId <= 0){
                return false;
            }
                
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
    
            sb = new StringBuilder();
            sb.append("INSERT INTO ME2ME_GOALS (CUSTOMER_ID, CREATED_TIMESTAMP, GOAL_NAME, TARGET_AMOUNT, MATURITY_DATE, ALERT_OPTION, LIQUIDITY_OPTION) ");
            sb.append(" VALUES(%d,SYSDATE(),'%s',%.2f,'%s','%s','%s')");
            sqlQuery = sb.toString();
    
            stm = cn.prepareStatement(String.format(sqlQuery, this.customerId,this.goalName,this.targetAmount,new SimpleDateFormat("yyyy-MM-dd").format(maturityDate),this.alertOption,this.liquidityOption));
                
            // Execute the Query
            dbStatusCode = stm.executeUpdate();
    
            return ((dbStatusCode > 0) ? true : false);   

        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }
    
    public MeToMeGoalTransactions getGoalTransactions() {
        Connection cn = null;
        MeToMeGoalTransactions meToMeGoalTransactions = null;  
        StringBuilder sb = null;
        String sqlQuery = null;
        
        try {  
            meToMeGoalTransactions = new MeToMeGoalTransactions();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
            
            sb = new StringBuilder();
            sb.append("SELECT TRANSACTION_ID,REFERENCE_ID,TRANSACTION_TIMESTAMP,SRC_MSISDN,TRANSACTION_AMOUNT,GOAL_ID,GOAL_BAL_BEFORE,GOAL_BAL_AFTER");
            sb.append(" FROM ME2ME_TRANSACTIONS ");
            sb.append(" WHERE GOAL_ID = %d");
            sb.append(" ORDER BY TRANSACTION_ID DESC LIMIT 4");
            sqlQuery = sb.toString();
                        
            PreparedStatement stm = cn.prepareStatement(String.format(sqlQuery,this.goalId));
            
            ResultSet result = stm.executeQuery();
            while(result.next()){
                MeToMeGoalTransaction meToMeGoalTransaction = new MeToMeGoalTransaction();                
                meToMeGoalTransaction.setReferenceId((result.getString("REFERENCE_ID")==null) ? "" : result.getString("REFERENCE_ID").trim());
                meToMeGoalTransaction.setTransactionTimestamp((result.getDate("TRANSACTION_TIMESTAMP")==null) ? null : result.getDate("TRANSACTION_TIMESTAMP"));
                meToMeGoalTransaction.setSourceMsisdn((result.getString("SRC_MSISDN")==null) ? "" : result.getString("SRC_MSISDN").trim());
                meToMeGoalTransaction.setGoalId(result.getInt("GOAL_ID"));
                meToMeGoalTransaction.setTransactionId(result.getLong("TRANSACTION_ID"));
                meToMeGoalTransaction.setTransactionAmount(result.getDouble("TRANSACTION_AMOUNT"));
                meToMeGoalTransaction.setGoalBalanceBefore(result.getDouble("GOAL_BAL_BEFORE"));
                meToMeGoalTransaction.setGoalBalanceAfter(result.getDouble("GOAL_BAL_AFTER"));           
                meToMeGoalTransactions.add(meToMeGoalTransaction);                
            }
            return meToMeGoalTransactions;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return null;
        }
    }

    public double getAccruedAmount() {
        return accruedAmount;
    }

    public void setAccruedAmount(double accruedAmount) {
        this.accruedAmount = accruedAmount;
    }

    public Date getLastCreditDate() {
        return lastCreditDate;
    }

    public void setLastCreditDate(Date lastCreditDate) {
        this.lastCreditDate = lastCreditDate;
    }

    public Date getLastDebitDate() {
        return lastDebitDate;
    }

    public void setLastDebitDate(Date lastDebitDate) {
        this.lastDebitDate = lastDebitDate;
    }
}