package org.applab.AppLabMoneyCore;

public class AccountInformation {
    private int accountId;
    private int customerId;
    private int accountTypeBitmap;
    private boolean isMainAccount;
    private int memoGroupId;
    private String accountSubRef;
    private double bookBalance;
    private double blockedBalance;
    private boolean isEnabled;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAccountTypeBitmap() {
        return accountTypeBitmap;
    }

    public void setAccountTypeBitmap(int accountTypeBitmap) {
        this.accountTypeBitmap = accountTypeBitmap;
    }

    public boolean isMainAccount() {
        return isMainAccount;
    }

    public void setMainAccount(boolean isMainAccount) {
        this.isMainAccount = isMainAccount;
    }

    public int getMemoGroupId() {
        return memoGroupId;
    }

    public void setMemoGroupId(int memoGroupId) {
        this.memoGroupId = memoGroupId;
    }

    public String getAccountSubRef() {
        return accountSubRef;
    }

    public void setAccountSubRef(String accountSubRef) {
        this.accountSubRef = accountSubRef;
    }

    public double getBookBalance() {
        return bookBalance;
    }

    public void setBookBalance(double bookBalance) {
        this.bookBalance = bookBalance;
    }

    public double getBlockedBalance() {
        return blockedBalance;
    }

    public void setBlockedBalance(double blockedBalance) {
        this.blockedBalance = blockedBalance;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}