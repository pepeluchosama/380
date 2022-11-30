/**
 * Rindegastos.com Java SDK
 * Includes calls to the following methods:
 * getExpenses https://www.rindegastos.com/api/expense#pos-getExpenses
 * getExpense https://www.rindegastos.com/api/expense#pos-getExpense
 * getExpenseReports https://www.rindegastos.com/api/expenseReport#pos-getExpenseReports
 * getExpenseReport https://www.rindegastos.com/api/expenseReport#pos-getExpenseReport
 * getFunds https://www.rindegastos.com/api/funds#pos-getFunds
 * getFund https://www.rindegastos.com/api/funds#pos-getFund
 * createFund https://www.rindegastos.com/api/funds#pos-createFund
 * updateFund https://www.rindegastos.com/api/funds#pos-updateFund
 * depositMoneyToFund https://www.rindegastos.com/api/funds#pos-depositMoneyToFund
 * withdrawMoneyFromFund https://www.rindegastos.com/api/funds#pos-withdrawMoneyFromFund
 * setFundStatus https://www.rindegastos.com/api/funds#pos-setFundStatus
 * getExpensePolicies https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicies
 * getExpensePolicy https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicy
 * setExpenseIntegration https://www.rindegastos.com/api/expense#pos-setExpenseIntegration
 * setExpenseReportIntegration https://www.rindegastos.com/api/expenseReport#pos-setExpenseReportIntegration
 * getExpensePolicyExpenseReportFields https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicyExpenseReportFields
 * getExpensePolicyExpenseFields https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicyExpenseFields
 * getExpensePolicyCategories https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicyCategories
 * getExpensePolicyWorkflow https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicyWorkFlow
 * getExpensePolicyTaxes https://www.rindegastos.com/api/expensePolicies#pos-getExpensePolicyTaxes
 * getUsers https://www.rindegastos.com/api/users#pos-getUsers
 * getUser https://www.rindegastos.com/api/users#pos-getUser
 * setExpenseReportCustomStatus https://www.rindegastos.com/api/expenseReport#pos-setExpenseReportCustomStatus
 */

package org.check.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.json.simple.JSONObject;

import com.sun.mail.iap.Protocol;
import com.sun.web.security.SSLSocketFactory;

public class CheckAPI {
    
    private String token = "";

    public CheckAPI() {
        //constructor
        //Definition of Token to access the different methods of the API Rindegatos
        //token = t;
    }
    public String check() throws MalformedURLException, IOException, NoSuchAlgorithmException 
    {
        String url = "https://checkapi-training-int-blumos.azurewebsites.net/api/ext/monitorEntrada";

        JSONObject objArray = new JSONObject();
        objArray.put("pre_uid_ins", "3fa85f64-5717-4562-b3fc-2c963f66afa6");
        objArray.put("pre_uid_erp", "3fa85f64-5717-4562-b3fc-2c963f66afa6");
        objArray.put("pre_id_erp", "string");
        objArray.put("pre_cod_tdo", "string");
        objArray.put("pre_eta_pre", "2021-12-20T05:48:10.855Z");
        objArray.put("pre_num_doc", "string");
        objArray.put("pre_lin_doc", 0);
        objArray.put("pre_cod_pro", "string");
        objArray.put("pre_des_pro", "string");
        objArray.put("pre_fec_emi", "2021-12-20T05:48:10.855Z");
        objArray.put("pre_cod_mat", "string");
        objArray.put("pre_des_mat", "string");
        objArray.put("pre_uni_mat", "string");
        objArray.put("pre_can_mat", 0);
        objArray.put("pre_ent_mat", 0);
        objArray.put("pre_pdt_mat", 0);
        objArray.put("pre_val_pre", 0);
        objArray.put("pre_val_pdt", 0);
        objArray.put("pre_obs_pre", "string");
        objArray.put("pre_ffa_mat", "2021-12-20T05:48:10.855Z");
        objArray.put("pre_fve_mat", "2021-12-20T05:48:10.855Z");
        objArray.put("pre_lot_mat", "string");
        objArray.put("pre_bod_cli", "string");
                
        org.apache.commons.httpclient.protocol.Protocol myhttps = new org.apache.commons.httpclient.protocol.Protocol("https", (ProtocolSocketFactory) new ProtocolSocketFactory() {
			
			@Override
			public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3, HttpConnectionParams arg4)
					throws IOException, UnknownHostException, ConnectTimeoutException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
					throws IOException, UnknownHostException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
				// TODO Auto-generated method stub
				return null;
			}
		}, 443);
        
        HttpClient httpclient= new HttpClient();
        httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        httpclient.getHostConfiguration().setHost(url, 443, myhttps);
        
        PostMethod httpPost = new PostMethod(url);
        httpPost.setRequestEntity(new StringRequestEntity(objArray.toJSONString()));
        String response = "";
       	httpclient.executeMethod(httpPost);
		if(httpPost.getStatusCode()== HttpStatus.SC_OK)
        {				
			response = httpPost.getResponseBodyAsString();
		}
		httpPost.releaseConnection();
        return response;       
    }
}
