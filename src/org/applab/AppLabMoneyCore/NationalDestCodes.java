/*
 * 
 * Copyright (c) 2010 AppLab, Grameen Foundation
 * 
 */

package org.applab.AppLabMoneyCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class NationalDestCodes extends ArrayList<NationalDestCode> {
    
    private static final long serialVersionUID = -5528575517977919779L;
    
    public boolean getPeerToPeerNdcList() {
        Connection cn = null;
        NationalDestCode ndc = null;
        
        try {
            this.clear();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
            PreparedStatement stm = cn.prepareStatement("SELECT NDC_LIST_ID, NDC, ENABLED_FLG FROM NDC_LIST_OFFNET WHERE ENABLED_FLG=1");
            
            ResultSet result = stm.executeQuery();
            while(result.next()){
                ndc = new NationalDestCode();
                ndc.setNationalDestCodeId(result.getInt("NDC_LIST_ID"));
                ndc.setNdcValue(result.getString("NDC"));
                int enabledFlg = result.getInt("ENABLED_FLG");
                ndc.setEnabled(((enabledFlg == 1) ? true : false));               
                this.add(ndc);
            }
            return true;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }
    
    public boolean getOffNetNdcList() {
        Connection cn = null;
        NationalDestCode ndc = null;
        
        try {
            this.clear();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
            PreparedStatement stm = cn.prepareStatement("SELECT NDC_LIST_ID, NDC, ENABLED_FLG FROM NDC_LIST_P2P WHERE ENABLED_FLG=1");
            
            ResultSet result = stm.executeQuery();
            while(result.next()){
                ndc = new NationalDestCode();
                ndc.setNationalDestCodeId(result.getInt("NDC_LIST_ID"));
                ndc.setNdcValue(result.getString("NDC"));
                int enabledFlg = result.getInt("ENABLED_FLG");
                ndc.setEnabled(((enabledFlg == 1) ? true : false));               
                this.add(ndc);
            }
            return true;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }

}
