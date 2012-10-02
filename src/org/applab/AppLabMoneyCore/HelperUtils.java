/*
 * 
 * Copyright (c) 2010 AppLab, Grameen Foundation
 * 
 */

package org.applab.AppLabMoneyCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

public class HelperUtils {
    public static final String TARGET_DATABASE = "MYSQL";
    public static final String SMS_SENDER_NUM = "APPLABMONEY";
    public static final double REBATE_MIN_SAVED_PERCENT = 50.00;
    public static final double REBATE_MAX_ALLOWED_PERCENT_OF_BALANCE = 40.00;
    public static final String LIQUIDITY_OPTION_NAME_FULL = "FULL";
    public static final String LIQUIDITY_OPTION_NAME_PARTIAL = "PARTIAL";
    public static final String LIQUIDITY_OPTION_NAME_NONE = "NONE";
    
    //Account Type Bitmaps
    public static final int BITMAP_NONE = 0;
    public static final int BITMAP_TEMP = 1;
    public static final int BITMAP_MCOM = 2;
    public static final int BITMAP_AGNT = 4;
    public static final int BITMAP_DLER = 8;
    public static final int BITMAP_MERC = 16;
    public static final int BITMAP_CORP = 32;    

    /*
     * Procedure to send SMS Message
     */
    public static void sendSMS(String destMsisdn, String destMessage, String referenceId) {
        try {
            String finalDestMsisdn = destMsisdn;
            String deliveryStatus = "FAILED";
            
            //Log the SMS being sent out
            HelperUtils.writeToLogFile("OutBoundMessages", String.format("%s~%s~%s", referenceId, destMsisdn, destMessage));
                                    
            // Prepare the Dest MSISDN to have +
            if (!destMsisdn.startsWith("+")) {
                finalDestMsisdn = "+".concat(destMsisdn);
            }  
            
            try {
                sendSMS(HelperUtils.SMS_SENDER_NUM, finalDestMsisdn, destMessage, referenceId);
                
                //TODO: Kannel currently does not return delivery status
                deliveryStatus = "DLVD";
            }
            catch (Exception exMsg) {
                HelperUtils.writeToLogFile("Server", "ERR: " + exMsg.getMessage() + " TRACE: " + exMsg.getStackTrace());
            }
            
            //Log to Database
            DatabaseHelper.logOutBoundMessage(referenceId, destMsisdn, destMessage, deliveryStatus);
        }
        catch (Exception ex) {
            HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
        }
    }

    /*
     * Procedure to Send out SMS Messages via the Kannel
     */
    private static void sendSMS(String sender, String recipient, String content, String referenceId) {
        // This URL will be configurable via a config file
        Message message = new Message("http://ckwapps.applab.org:8888/services/sendSms");
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setBody(content);
        message.Send();
    }
    
    public static String maskPassword(String thePassword) {
        String maskedPwd = "*";        
        for(int i=0; i<thePassword.length(); i++){
            maskedPwd = maskedPwd.concat("*");
        }
        return maskedPwd;
    }
    
    /*
     * Procedure to get Stack Trace as String
     */
    public static String convertStackTraceToString(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
      }

    /*
     * Procedure to write out data to a log file
     */
    public static void writeToLogFile(String targetFolder, String theData) {
        File targetFile = null;
        File targetDirectory = null;
        String logDirectoryPath = null;
        String fileName = null;
        String timestamp = null;
        String finalRecordData = null;
        FileOutputStream strm = null;

        try {
            if (targetFolder.compareToIgnoreCase("console") == 0) {
                // Write on the Console Window
                System.out.println(theData);
            }
            else {
                // Write to the File and the specified targetFolder
                logDirectoryPath = new File("Log").getAbsolutePath();

                // Target Folder
                targetDirectory = new File(logDirectoryPath + File.separator + targetFolder);

                // If it doesn't exist create it
                if (!targetDirectory.exists()) {
                    boolean retVal = targetDirectory.mkdirs();
                    if (!retVal) {
                        // A problem occurred. For now just exit.
                        return;
                    }
                }

                // Now build the FileName based on the Date and Time
                long currentTime = System.currentTimeMillis();
                Date dateTime = new Date(currentTime);

                fileName = String.format("%1$td%1$tb%1$tY@%1$tH00.txt", dateTime);

                // Create the file
                targetFile = new File(targetDirectory.getAbsolutePath() + File.separator + fileName);

                // Append Time and Line Separator to the Data Record
                timestamp = String.format("[%1$tH:%1$tM:%1$tS]", dateTime);
                finalRecordData = String.format("%s %s%s", timestamp, theData.trim(), "\r\n");

                // Now Write the Record to the file
                strm = new FileOutputStream(targetFile, true);
                new PrintStream(strm).print(finalRecordData);

                // close the stream
                strm.close();
            }
        }
        catch (Exception ex) {
            System.err.println("ERR: " + ex.getMessage() + " TRACE: " + ex.getStackTrace());
        }
        finally {
            if (strm != null) {
                try {
                    strm.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getDefaultPinCode(int minPasswordLen, int maxPasswordLen) {
        String defaultPwd = "";
        for(int i=1; i<=maxPasswordLen; i++){
            defaultPwd = defaultPwd.concat(Integer.toString(i));
        }
        return defaultPwd;
    }    
}
