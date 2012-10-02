package org.applab.AppLabMoneyCore.Zimba;

import java.util.Date;

public class ZimbaNetwork {
    private long networkId;
    private long customerId;
    private long friendCustomerId;
    private Date dateConnected;
    private String friendshipLevel; //0 ordinary
    private boolean isDisconnected;
    private Date dateDisconnected;
    private int borrowedFromFriend;
    private int lendedToFriend;
    private int defaultsByFriend;
    private int defaultsToFriend;
    private double receivableFromFriend;
    private double payableToFriend;
    
    
    public long getNetworkId() {
        return networkId;
    }
    public void setNetworkId(long networkId) {
        this.networkId = networkId;
    }
    public long getCustomerId() {
        return customerId;
    }
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }
    public long getFriendCustomerId() {
        return friendCustomerId;
    }
    public void setFriendCustomerId(long friendCustomerId) {
        this.friendCustomerId = friendCustomerId;
    }
    public Date getDateConnected() {
        return dateConnected;
    }
    public void setDateConnected(Date dateConnected) {
        this.dateConnected = dateConnected;
    }
    public String getFriendshipLevel() {
        return friendshipLevel;
    }
    public void setFriendshipLevel(String friendshipLevel) {
        this.friendshipLevel = friendshipLevel;
    }
    public boolean isDisconnected() {
        return isDisconnected;
    }
    public void setDisconnected(boolean isDisconnected) {
        this.isDisconnected = isDisconnected;
    }
    public Date getDateDisconnected() {
        return dateDisconnected;
    }
    public void setDateDisconnected(Date dateDisconnected) {
        this.dateDisconnected = dateDisconnected;
    }
    public int getBorrowedFromFriend() {
        return borrowedFromFriend;
    }
    public void setBorrowedFromFriend(int borrowedFromFriend) {
        this.borrowedFromFriend = borrowedFromFriend;
    }
    public int getLendedToFriend() {
        return lendedToFriend;
    }
    public void setLendedToFriend(int lendedToFriend) {
        this.lendedToFriend = lendedToFriend;
    }
    public int getDefaultsByFriend() {
        return defaultsByFriend;
    }
    public void setDefaultsByFriend(int defaultsByFriend) {
        this.defaultsByFriend = defaultsByFriend;
    }
    public int getDefaultsToFriend() {
        return defaultsToFriend;
    }
    public void setDefaultsToFriend(int defaultsToFriend) {
        this.defaultsToFriend = defaultsToFriend;
    }
    public double getReceivableFromFriend() {
        return receivableFromFriend;
    }
    public void setReceivableFromFriend(double receivableFromFriend) {
        this.receivableFromFriend = receivableFromFriend;
    }
    public double getPayableToFriend() {
        return payableToFriend;
    }
    public void setPayableToFriend(double payableToFriend) {
        this.payableToFriend = payableToFriend;
    }
    
    
}
