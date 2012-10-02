/*
 * 
 * Copyright (c) 2010 AppLab, Grameen Foundation
 * 
 */

package org.applab.AppLabMoneyCore;

public class SystemConfigInfo {
    private static int maxPasswordLen;
    private static int minPasswordLen;
    private static int mobileLength;
    private static boolean msisdnLeadZeroRequired;
    private static String operation;
    private static String senderNumber;
    private static double smsCost;
    private static int invalidPasswordLock;
    private static int failedTransferLock;
    private static String countryCode;
    private static String countryDomain;
    private static String currencyCode;
    private static int currencyIsoNumber;
    private static int eMailCount;
    private static boolean autoCreateTempAccount;
    private static boolean tempRestriction;
    private static int smsMsgSubDisplayFormat;
    private static int subDisplayMaxChars;
    private static boolean keyCostCollectImmediate;
    private static boolean notifyOnDelayedTrans;
    private static int notifyDelaySeconds;
    private static int notifyDelayMsgId;
    private static boolean maintenanceModeFlag = false;
    private static int maintenanceModeMsgId;   
    

    public static int getMaxPasswordLen() {
        return maxPasswordLen;
    }

    public static void setMaxPasswordLen(int maxPassword) {
        SystemConfigInfo.maxPasswordLen = maxPassword;
    }

    public static int getMinPasswordLen() {
        return minPasswordLen;
    }

    public static void setMinPasswordLen(int minPassword) {
        SystemConfigInfo.minPasswordLen = minPassword;
    }

    public static int getMobileLength() {
        return mobileLength;
    }

    public static void setMobileLength(int mobileLength) {
        SystemConfigInfo.mobileLength = mobileLength;
    }

    public static Boolean getMsisdnLeadZeroRequired() {
        return msisdnLeadZeroRequired;
    }

    public static void setMsisdnLeadZeroRequired(Boolean msisdnLeadZeroRequired) {
        SystemConfigInfo.msisdnLeadZeroRequired = msisdnLeadZeroRequired;
    }

    public static String getOperation() {
        return operation;
    }

    public static void setOperation(String operation) {
        SystemConfigInfo.operation = operation;
    }

    public static String getSenderNumber() {
        return senderNumber;
    }

    public static void setSenderNumber(String senderNumber) {
        SystemConfigInfo.senderNumber = senderNumber;
    }

    public static double getSmsCost() {
        return smsCost;
    }

    public static void setSmsCost(double smsCost) {
        SystemConfigInfo.smsCost = smsCost;
    }

    public static int getInvalidPasswordLock() {
        return invalidPasswordLock;
    }

    public static void setInvalidPasswordLock(int invalidPasswordLock) {
        SystemConfigInfo.invalidPasswordLock = invalidPasswordLock;
    }

    public static int getFailedTransferLock() {
        return failedTransferLock;
    }

    public static void setFailedTransferLock(int failedTransferLock) {
        SystemConfigInfo.failedTransferLock = failedTransferLock;
    }

    public static String getCountryCode() {
        return countryCode;
    }

    public static void setCountryCode(String countryCode) {
        SystemConfigInfo.countryCode = countryCode;
    }

    public static String getCountryDomain() {
        return countryDomain;
    }

    public static void setCountryDomain(String countryDomain) {
        SystemConfigInfo.countryDomain = countryDomain;
    }

    public static String getCurrencyCode() {
        return currencyCode;
    }

    public static void setCurrencyCode(String currencyCode) {
        SystemConfigInfo.currencyCode = currencyCode;
    }

    public static int getCurrencyIsoNumber() {
        return currencyIsoNumber;
    }

    public static void setCurrencyIsoNumber(int currencyIsoNumber) {
        SystemConfigInfo.currencyIsoNumber = currencyIsoNumber;
    }

    public static int geteMailCount() {
        return eMailCount;
    }

    public static void seteMailCount(int eMailCount) {
        SystemConfigInfo.eMailCount = eMailCount;
    }

    public static Boolean getAutoCreateTempAccount() {
        return autoCreateTempAccount;
    }

    public static void setAutoCreateTempAccount(Boolean autoCreateTempAccount) {
        SystemConfigInfo.autoCreateTempAccount = autoCreateTempAccount;
    }

    public static Boolean getTempRestriction() {
        return tempRestriction;
    }

    public static void setTempRestriction(Boolean tempRestriction) {
        SystemConfigInfo.tempRestriction = tempRestriction;
    }

    public static int getSmsMsgSubDisplayFormat() {
        return smsMsgSubDisplayFormat;
    }

    public static void setSmsMsgSubDisplayFormat(int smsMsgSubDisplayFormat) {
        SystemConfigInfo.smsMsgSubDisplayFormat = smsMsgSubDisplayFormat;
    }

    public static int getSubDisplayMaxChars() {
        return subDisplayMaxChars;
    }

    public static void setSubDisplayMaxChars(int subDisplayMaxChars) {
        SystemConfigInfo.subDisplayMaxChars = subDisplayMaxChars;
    }

    public static Boolean getKeyCostCollectImmediate() {
        return keyCostCollectImmediate;
    }

    public static void setKeyCostCollectImmediate(Boolean keyCostCollectImmediate) {
        SystemConfigInfo.keyCostCollectImmediate = keyCostCollectImmediate;
    }

    public static Boolean getNotifyOnDelayedTrans() {
        return notifyOnDelayedTrans;
    }

    public static void setNotifyOnDelayedTrans(Boolean notifyOnDelayedTrans) {
        SystemConfigInfo.notifyOnDelayedTrans = notifyOnDelayedTrans;
    }

    public static int getNotifyDelaySeconds() {
        return notifyDelaySeconds;
    }

    public static void setNotifyDelaySeconds(int notifyDelaySeconds) {
        SystemConfigInfo.notifyDelaySeconds = notifyDelaySeconds;
    }

    public static int getNotifyDelayMsgId() {
        return notifyDelayMsgId;
    }

    public static void setNotifyDelayMsgId(int notifyDelayMsgId) {
        SystemConfigInfo.notifyDelayMsgId = notifyDelayMsgId;
    }

    public static Boolean getMaintenanceModeFlag() {
        return maintenanceModeFlag;
    }

    public static void setMaintenanceModeFlag(Boolean maintenanceModeFlag) {
        SystemConfigInfo.maintenanceModeFlag = maintenanceModeFlag;
    }

    public static int getMaintenanceModeMsgId() {
        return maintenanceModeMsgId;
    }

    public static void setMaintenanceModeMsgId(int maintenanceModeMsgId) {
        SystemConfigInfo.maintenanceModeMsgId = maintenanceModeMsgId;
    }
    
    //Static Constructor
    static {
        //Read from database
        setAutoCreateTempAccount(true);
        setCountryCode("256");
        setCountryDomain("UG");
        setCurrencyCode("UGX");
        setCurrencyIsoNumber(476);
        seteMailCount(3);
        setFailedTransferLock(3);
        setInvalidPasswordLock(3);
        setKeyCostCollectImmediate(false);
        setMaintenanceModeFlag(false);
        setMaintenanceModeMsgId(60001);
        setMaxPasswordLen(4);
        setMinPasswordLen(4);
        setMobileLength(9);
        setMsisdnLeadZeroRequired(false);
        setOperation("UGANDA");
        setSenderNumber("APPLABMONEY");
        setSmsMsgSubDisplayFormat(2);
        setSubDisplayMaxChars(30);
        setTempRestriction(false);
    }
}
