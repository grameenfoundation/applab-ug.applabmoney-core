package org.applab.AppLabMoneyCore.Zimba;

import java.util.Date;

public class ZimbaInvitation {

    private long invitationId;
    private Date invitationDate;
    private long customerId;
    private String friendMsisdn;    //If friend is not a customer
    private int invitationStatus; //0 PENDING | 1 ACCEPTED | 2 DECLINED
    
    public long getInvitationId() {
        return invitationId;
    }
    public void setInvitationId(long invitationId) {
        this.invitationId = invitationId;
    }
    public Date getInvitationDate() {
        return invitationDate;
    }
    public void setInvitationDate(Date invitationDate) {
        this.invitationDate = invitationDate;
    }    
    public String getFriendMsisdn() {
        return friendMsisdn;
    }
    public void setFriendMsisdn(String friendMsisdn) {
        this.friendMsisdn = friendMsisdn;
    }
    public int getInvitationStatus() {
        return invitationStatus;
    }
    public void setInvitationStatus(int invitationStatus) {
        this.invitationStatus = invitationStatus;
    }
    public long getCustomerId() {
        return customerId;
    }
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }
    
       

}
