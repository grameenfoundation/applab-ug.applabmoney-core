package org.applab.AppLabMoneyCore;

public class ValidationReturnInformation {
    private boolean passedValidation = false;
    private String validationMessage = "";
    private Object extData = null;
    
    public ValidationReturnInformation(){
        
    }
    
    public ValidationReturnInformation(boolean passedValidation, String validationMessage, Object extData){
        this.setExtData(extData);
        this.setPassedValidation(false);
        this.setValidationMessage("");
    }

    public boolean isPassedValidation() {
        return passedValidation;
    }

    public void setPassedValidation(boolean passedValidation) {
        this.passedValidation = passedValidation;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public Object getExtData() {
        return extData;
    }

    public void setExtData(Object extData) {
        this.extData = extData;
    }
}
