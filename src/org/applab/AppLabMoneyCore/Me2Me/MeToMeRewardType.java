package org.applab.AppLabMoneyCore.Me2Me;

public class MeToMeRewardType {
    private int rewardTypeId = 0;
    private String rewardTypeCode = "";
    private String rewardTypeName = "";
    private String rewardTypeDesc = "";
    private boolean isCashReward = false;
    
    public int getRewardTypeId() {
        return rewardTypeId;
    }
    public void setRewardTypeId(int rewardTypeId) {
        this.rewardTypeId = rewardTypeId;
    }
    public String getRewardTypeCode() {
        return rewardTypeCode;
    }
    public void setRewardTypeCode(String rewardTypeCode) {
        this.rewardTypeCode = rewardTypeCode;
    }
    public String getRewardTypeName() {
        return rewardTypeName;
    }
    public void setRewardTypeName(String rewardTypeName) {
        this.rewardTypeName = rewardTypeName;
    }
    public String getRewardTypeDesc() {
        return rewardTypeDesc;
    }
    public void setRewardTypeDesc(String rewardTypeDesc) {
        this.rewardTypeDesc = rewardTypeDesc;
    }
    public boolean isCashReward() {
        return isCashReward;
    }
    public void setIsCashReward(boolean isCashReward) {
        this.isCashReward = isCashReward;
    }
}
