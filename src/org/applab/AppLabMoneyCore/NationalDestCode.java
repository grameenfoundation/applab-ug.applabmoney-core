/*
 * 
 * Copyright (c) 2010 AppLab, Grameen Foundation
 * 
 */

package org.applab.AppLabMoneyCore;

public class NationalDestCode {
    private int nationalDestCodeId;
    private int countryId;
    private String ndcValue;
    private boolean isOffNetNdc;
    private boolean isEnabled;
    
    public NationalDestCode() {
        
    }
    
    public NationalDestCode(int ndcId, String ndcValue, boolean isOffNetNdc, boolean isEnabled){
        this.nationalDestCodeId = ndcId;
        this.ndcValue = ndcValue;
        this.isOffNetNdc = isOffNetNdc;
        this.isEnabled = isEnabled;
    }

    public int getNationalDestCodeId() {
        return nationalDestCodeId;
    }

    public void setNationalDestCodeId(int nationalDestCodeId) {
        this.nationalDestCodeId = nationalDestCodeId;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getNdcValue() {
        return ndcValue;
    }

    public void setNdcValue(String ndcValue) {
        this.ndcValue = ndcValue;
    }

    public boolean getIsOffNetNdc() {
        return isOffNetNdc;
    }

    public void setIsOffNetNdc(boolean isOffNetNdc) {
        this.isOffNetNdc = isOffNetNdc;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    
}
