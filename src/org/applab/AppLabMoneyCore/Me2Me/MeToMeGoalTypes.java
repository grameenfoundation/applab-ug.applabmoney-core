package org.applab.AppLabMoneyCore.Me2Me;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class MeToMeGoalTypes extends ArrayList<MeToMeGoalType> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public boolean getGoalTypes() {
        Connection cn = null;
        MeToMeGoalType goalType = null;
        
        try {
            this.clear();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
            PreparedStatement stm = cn.prepareStatement("SELECT GOAL_TYPE_ID, GOAL_TYPE_CODE, GOAL_TYPE_NAME, GOAL_TYPE_DESC FROM ME2ME_GOAL_TYPES");
            
            ResultSet result = stm.executeQuery();
            while(result.next()){
                goalType = new MeToMeGoalType();
                goalType.setGoalTypeId(result.getInt("GOAL_TYPE_ID"));
                goalType.setGoalTypeCode((result.getString("GOAL_TYPE_CODE")==null) ? "" : result.getString("GOAL_TYPE_CODE").trim());                        
                goalType.setGoalTypeName((result.getString("GOAL_TYPE_NAME")==null) ? "" : result.getString("GOAL_TYPE_NAME").trim());
                goalType.setGoalTypeDesc((result.getString("GOAL_TYPE_DESC")==null) ? "" : result.getString("GOAL_TYPE_DESC").trim());
                this.add(goalType);
            }
            return true;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }
    

}
