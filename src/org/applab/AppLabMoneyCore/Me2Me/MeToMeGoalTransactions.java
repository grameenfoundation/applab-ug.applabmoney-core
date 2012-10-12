package org.applab.AppLabMoneyCore.Me2Me;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.applab.AppLabMoneyCore.HelperUtils;
import org.applab.AppLabMoneyCore.SystemConfigInfo;

public class MeToMeGoalTransactions extends ArrayList<MeToMeGoalTransaction> {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public String getTransactionsSms(int transactionCount) {
		StringBuilder sb = null;
		String transRecord = "";
		String transSms = "";
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String currencyCode = "";

		try {
			currencyCode = SystemConfigInfo.getCurrencyCode();

			sb = new StringBuilder();

			for (int i = 0; i < transactionCount && i < this.size(); i++) {

				if (i > 0) {
					sb.append("\r\n");
				}

				if (this.get(i).getGoalBalanceAfter() > this.get(i)
						.getGoalBalanceBefore()) {
					transRecord = String.format("%s DEP:%s%,.0f", df
							.format(this.get(i).getTransactionTimestamp()),
							currencyCode.toUpperCase(), this.get(i)
									.getTransactionAmount());
				} else if (this.get(i).getGoalBalanceAfter() < this.get(i)
						.getGoalBalanceBefore()) {
					transRecord = String.format("%s WTD:%s%,.0f", df
							.format(this.get(i).getTransactionTimestamp()),
							currencyCode.toUpperCase(), this.get(i)
									.getTransactionAmount());
				}

				// transRecord = String.format("DT:%s From:%s AMT:%,.2f%s",
				// df.format(this.get(i).getTransactionTimestamp()),
				// this.get(i).getSourceMsisdn(),
				// this.get(i).getTransactionAmount(),
				// currencyCode.toUpperCase());
				sb.append(transRecord);
			}

			transSms = sb.toString();
			return transSms;
		} catch (Exception ex) {
			HelperUtils.writeToLogFile("Server", "ERR: " + ex.getMessage()
					+ " TRACE: " + ex.getStackTrace());
			return transSms;
		}
	}

}
