package org.applab.AppLabMoneyCore.Me2Me;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.applab.AppLabMoneyCore.DatabaseHelper;
import org.applab.AppLabMoneyCore.HelperUtils;

public class MeToMeRewardTypes extends ArrayList<MeToMeRewardType> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public boolean getRewardTypes() {
        Connection cn = null;
        MeToMeRewardType rewardType = null;
        
        try {
            this.clear();
            cn = DatabaseHelper.getConnection(HelperUtils.TARGET_DATABASE);
            PreparedStatement stm = cn.prepareStatement("SELECT REWARD_TYPE_ID, REWARD_TYPE_CODE, REWARD_TYPE_NAME, REWARD_TYPE_DESC, IS_CASH_REWARD FROM ME2ME_REWARD_TYPES");
            
            ResultSet result = stm.executeQuery();
            while(result.next()){
                rewardType = new MeToMeRewardType();
                rewardType.setRewardTypeId(result.getInt("REWARD_TYPE_ID"));
                rewardType.setRewardTypeCode((result.getString("REWARD_TYPE_CODE")==null) ? "" : result.getString("REWARD_TYPE_CODE").trim());                        
                rewardType.setRewardTypeName((result.getString("REWARD_TYPE_NAME")==null) ? "" : result.getString("REWARD_TYPE_NAME").trim());
                rewardType.setRewardTypeDesc((result.getString("REWARD_TYPE_DESC")==null) ? "" : result.getString("REWARD_TYPE_DESC").trim());
                if (result.getInt("IS_CASH_REWARD") == 1){
                    rewardType.setIsCashReward(true);
                }
                else {
                    rewardType.setIsCashReward(false);
                }
                this.add(rewardType);
            }
            return true;
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
            return false;
        }
    }
}
