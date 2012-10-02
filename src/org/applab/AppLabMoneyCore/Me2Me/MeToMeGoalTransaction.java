package org.applab.AppLabMoneyCore.Me2Me;

import java.util.Date;

public class MeToMeGoalTransaction {
    
    private long transactionId;
    private String referenceId;
    private Date transactionTimestamp;
    private String sourceMsisdn;
    private double transactionAmount;
    private int goalId;
    private double goalBalanceBefore;
    private double goalBalanceAfter;
    
    
    public long getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }
    public String getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    public Date getTransactionTimestamp() {
        return transactionTimestamp;
    }
    public void setTransactionTimestamp(Date transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }
    public String getSourceMsisdn() {
        return sourceMsisdn;
    }
    public void setSourceMsisdn(String sourceMsisdn) {
        this.sourceMsisdn = sourceMsisdn;
    }
    public double getTransactionAmount() {
        return transactionAmount;
    }
    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    public int getGoalId() {
        return goalId;
    }
    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }
    public double getGoalBalanceBefore() {
        return goalBalanceBefore;
    }
    public void setGoalBalanceBefore(double goalBalanceBefore) {
        this.goalBalanceBefore = goalBalanceBefore;
    }
    public double getGoalBalanceAfter() {
        return goalBalanceAfter;
    }
    public void setGoalBalanceAfter(double goalBalanceAfter) {
        this.goalBalanceAfter = goalBalanceAfter;
    }
    
    

}
