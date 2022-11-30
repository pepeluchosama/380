/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.blumos.process;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.logging.Level;

import org.json.JSONObject;
import org.ofb.utils.DateUtils;
//import org.rindegastos.api.SdkRindegastos;
import org.rindegastos.api.SdkRindegastosReturn;

//import rindegastosapi.SdkRindegastos;

import org.json.JSONArray;

import org.compiere.process.*;
import org.compiere.util.DB;
import org.compiere.util.Env;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.compiere.model.MCash;
import org.compiere.model.MCashBook;
import org.compiere.model.MCashLine;
import org.compiere.model.MClient;
import org.compiere.model.MInvoice;

/**
 * 
 *
 *	@author ininoles
 *	@version $Id: ProcessRindeGastos.java,v 1.2 2020/11/05 
 */
public class ProcessRindeGastos extends SvrProcess
{
	/**	The Order				*/
	private Timestamp dateFrom;
	private Timestamp dateTo;
	private String TypeDF = "1";
	private int currency = 0;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("DateTrx"))
			{
				dateFrom = para[i].getParameterAsTimestamp();
				dateTo = para[i].getParameterToAsTimestamp();
			}
			else if (name.equals("TypeDateFilter"))
				TypeDF = para[i].getParameterAsString();
			//mfrojas se agrega parametro de moneda
			else if(name.equals("C_Currency_ID"))
				currency = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		int cant=0;
		int cantHead=0;
		
		if(TypeDF == null)
			TypeDF = "1";
		int ID_ChargeBase = DB.getSQLValue(get_TrxName(), "select MAX(C_Charge_ID) from C_Charge where name like '212090%' AND AD_Client_ID = "+Env.getAD_Client_ID(getCtx()));
		//String result ="";
		if(dateFrom == null)
			dateFrom = DateUtils.today();
		if(dateTo == null)
			dateTo = DateUtils.today();
		String dateFromTxt=(dateFrom.getYear()+1900)+"-";
		if((dateFrom.getMonth()+1) < 10)
			dateFromTxt = dateFromTxt+"0"+(dateFrom.getMonth()+1)+"-";
		else
			dateFromTxt = dateFromTxt+(dateFrom.getMonth()+1)+"-";
		if(dateFrom.getDate()< 10)
			dateFromTxt = dateFromTxt+"0"+dateFrom.getDate();
		else
			dateFromTxt = dateFromTxt+dateFrom.getDate();			
			
		String dateToTxt =(dateTo.getYear()+1900)+"-";
		if((dateTo.getMonth()+1) < 10)
			dateToTxt = dateToTxt+"0"+(dateTo.getMonth()+1)+"-";
		else
			dateToTxt = dateToTxt+(dateTo.getMonth()+1)+"-";
		if(dateTo.getDate()< 10)
			dateToTxt = dateToTxt+"0"+dateTo.getDate();
		else
			dateToTxt = dateToTxt+dateTo.getDate();
		String TokenPass = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiODYwNTQiLCJjb21wYW55X2lkIjoiMTk2OTAiLCJyYW5kb20iOiJyYW5kQVBJNWY2MjM3YjE5OGQ3YzUuNzYwNzkzMjgifQ.y5QUA5yVZIn3fxs5QsrZAFU4ZZoQqeFjgwMJ7mTYUKs";
		//mfrojas 20221019
		//Si el adclient es sonutra (1000022) el token debe cambiar.
		if(Env.getAD_Client_ID(getCtx()) == 1000022)
			TokenPass = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNTcyNjUiLCJjb21wYW55X2lkIjoiMjg2MjQiLCJyYW5kb20iOiJyYW5kQVBJNjMzNDlmYzg3Yjk4NDMuMzI2MDk5OTUifQ.dPaexp6oigIKNOKEPfMIkVmQW9GSessG5evTJduMhC4";
	    SdkRindegastosReturn x = new SdkRindegastosReturn(TokenPass);
		
	    
	    //mfrojas 20221116
	    //Si el parametro moneda existe, se debe utilizar. Si no, sera CLP
	    String stcurrency = "CLP";
	    if(currency > 0)
	    {
	    	String staux = null;
	    	staux = DB.getSQLValueString(get_TrxName(), "SELECT ISO_Code from c_currency WHERE c_currency_id = ?", currency);
	    	if(staux != null)
	    		stcurrency = staux;
	    }
	    log.config("stcurrency "+stcurrency);
	    //se busca id de informe de gastos.
	    HashMap<String,String> paramsGetExpenseReports = new HashMap<>();
	    paramsGetExpenseReports.put("Since",dateFromTxt);
	    paramsGetExpenseReports.put("Until",dateToTxt);
	    //paramsGetExpenseReports.put("TypeDateFilter", "1");
	    paramsGetExpenseReports.put("TypeDateFilter", TypeDF);
	    //paramsGetExpenseReports.put("Currency", "CLP");
	    paramsGetExpenseReports.put("Currency", stcurrency);
	    paramsGetExpenseReports.put("Status","1");
	    paramsGetExpenseReports.put("ExpensePolicyId","");
	    paramsGetExpenseReports.put("IntegrationStatus", "");
	    paramsGetExpenseReports.put("IntegrationCode", "");
	    paramsGetExpenseReports.put("IntegrationDate", "");
	    paramsGetExpenseReports.put("UserId","");
	    paramsGetExpenseReports.put("OrderBy","");
	    paramsGetExpenseReports.put("Order","");
	    paramsGetExpenseReports.put("ResultsPerPage","");
	    paramsGetExpenseReports.put("Page","");
        //example calling method getExpenses
        String jsonTextEReport = x.getExpenseReports(paramsGetExpenseReports);
		log.config("jsonText = "+jsonTextEReport);
		JSONObject jsonEReport = new JSONObject(jsonTextEReport);
		JSONArray jsonarrEReport = jsonEReport.getJSONArray("ExpenseReports");
		int qtyRegistrosEReport = jsonarrEReport.length();
	    String IDEReport = "0";
	    String ReportNumber = "-";
	    String Title = "-";
	    int IDEnteroEreport = 0;
	    int IDUsadoEReport = 0;
		String policyID = "";
		String UserId = "";
		int ID_CashBook = 0;
		int existPoli =0;
		MClient client = new MClient(getCtx(), get_TrxName());
		int existJCash = 0;
		MCash cash = null;
		
		//variables usadas en el detalle
		String ID = "0";
		int IDEntero = 0;
		int IDUsado = 0;
		String total = "";
		BigDecimal totalAmt = Env.ZERO;
		String supplier = "";
		String rutProveedor = "";
		String numDoc = "";
		String tipoDoc = "";
		String kilometraje = "";
		BigDecimal kilometrajeNum = Env.ZERO;
		String combustible = "";
		BigDecimal combustibleNum = Env.ZERO;
		String centroCosto = "";
		int ID_Vendor=0;
		int ID_invoice=0;
		int ID_Cargo;
		int ID_Asset;
		MInvoice inv = null;
		
		int ID_Org;
		String group = "";
		String groupCode = "";
		String note = "";
		
		//campos para comparar monto de factura
		BigDecimal totalAmtMax = Env.ZERO;
		BigDecimal totalAmtMin = Env.ZERO;
		
		//IDs de diarios creados o usados
		String ID_Cash = "0";
		
		if(qtyRegistrosEReport > 0)
		{
			for(int c=0; c<qtyRegistrosEReport;c++)
			{
				IDEReport = jsonarrEReport.getJSONObject(c).get("Id").toString();
				policyID = jsonarrEReport.getJSONObject(c).get("PolicyId").toString();
				UserId = jsonarrEReport.getJSONObject(c).get("EmployeeId").toString();
				ReportNumber = jsonarrEReport.getJSONObject(c).get("ReportNumber").toString();
				Title = jsonarrEReport.getJSONObject(c).get("Title").toString();
				
				if(IDEReport != null && IDEReport.trim().length()>0)
					IDEnteroEreport = Integer.parseInt(IDEReport);
				else
				{
					IDEnteroEreport = 0;
					IDUsadoEReport = 0;
				}
				//if(IDEnteroEreport > 0)
				//	IDUsadoEReport = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM C_Cash WHERE IDRindegastos="+IDEnteroEreport);

				if(IDUsadoEReport <=0)
				{
					//se valida que policyID pertenesca al cliente.
					if(policyID == null)
						policyID="0";
					if(UserId == null)
						UserId="-1";
					
					existPoli = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM RD_ExpensePolicies WHERE AD_Client_ID="+client.get_ID()+
							" AND expensepolicyid = "+policyID);				
					if(existPoli > 0)
					{	
						//se busca si existe diario abierto
						existJCash = DB.getSQLValue(get_TrxName(), "SELECT MAX(C_Cash_ID) "
								+ " FROM C_Cash cc "
								+ " INNER JOIN C_CashBook cb ON (cc.C_CashBook_ID = cb.C_CashBook_ID)"
								+ " WHERE cc.docStatus IN ('DR','IP') AND cb.IDRindegastos="+UserId
								+ " AND cc.AD_Client_ID="+Env.getAD_Client_ID(getCtx()));
						if(existJCash > 0)//existe diario abierto
						{	
							cash = new MCash(getCtx(),existJCash, get_TrxName());
							//si ya existe se agrega numero de reporte
							cash.setName(cash.getName()+"-"+ReportNumber);
							cash.setDescription(cash.getDescription()+"-"+Title);
							cash.save();
							ID_Cash = ID_Cash+","+cash.get_ID();
						}
						else//sino se crea diario
						{
							//se busca cashbook
							ID_CashBook = DB.getSQLValue(get_TrxName(), "SELECT MAX(C_CashBook_ID) FROM C_CashBook "
									+ " WHERE IDRindegastos ="+UserId+" AND IsActive = 'Y' AND AD_Client_ID="+Env.getAD_Client_ID(getCtx()));
							
							if(ID_CashBook > 0)//si existe se genera diario
							{
								MCashBook book = new MCashBook(getCtx(), ID_CashBook, get_TrxName());
								cash = new MCash(getCtx(), 0, get_TrxName());
								cash.setAD_Org_ID(book.getAD_Org_ID());
								cash.setName(ReportNumber);
								cash.setC_CashBook_ID(ID_CashBook);
								cash.setDescription(Title);
								cash.setStatementDate(DateUtils.today());
								cash.setDateAcct(DateUtils.today());
								cash.set_CustomColumn("IDRindegastos", IDEReport);
								cash.save(get_TrxName());
								cantHead++;
								ID_Cash = ID_Cash+","+cash.get_ID();
							}
							else
								cash=null;
						}					
					
						if(cash != null)
						{
								
							HashMap<String,String> paramsGetExpenses = new HashMap<>();
					        //paramsGetExpenses.put("Since", "2020-01-01");
					        paramsGetExpenses.put("Since","");
					        //paramsGetExpenses.put("Until", "2020-02-26");
					        paramsGetExpenses.put("Until","");
					        //paramsGetExpenses.put("Currency", "CLP");
					        paramsGetExpenses.put("Currency", stcurrency);
					        paramsGetExpenses.put("Status", "1");
					        paramsGetExpenses.put("Category","");
					        paramsGetExpenses.put("ReportId",IDEReport);
					        paramsGetExpenses.put("ExpensePolicyId", "");
					        paramsGetExpenses.put("IntegrationStatus", "");
					        paramsGetExpenses.put("IntegrationCode", "");
					        paramsGetExpenses.put("IntegrationDate", "");
					        paramsGetExpenses.put("UserId","");
					        paramsGetExpenses.put("OrderBy","");
					        paramsGetExpenses.put("Order","");
					        paramsGetExpenses.put("ResultsPerPage","");
					        paramsGetExpenses.put("Page","");
					        //example calling method getExpenses
					        String jsonText = x.getExpenses(paramsGetExpenses);
							log.config("jsonText = "+jsonText);
							JSONObject json = new JSONObject(jsonText);
							//se busca cantidad de registros
							//int qtyRegistros = Integer.parseInt((String)json.get("TotalRecords"));
							JSONArray jsonarr = json.getJSONArray("Expenses");
							
							ID = "0";
							IDEntero = 0;
							IDUsado = 0;
							total = "";
							totalAmt = Env.ZERO;
							totalAmtMin = Env.ZERO;
							totalAmtMax = Env.ZERO;
							supplier = "";
							rutProveedor = "";
							numDoc = "";
							tipoDoc = "";
							kilometraje = "";
							kilometrajeNum = Env.ZERO;
							combustible = "";
							combustibleNum = Env.ZERO;
							centroCosto = "";
							ID_Vendor=0;
							ID_invoice=0;
							inv = null;
							ID_Cargo =0;
							ID_Asset =0;
							
							ID_Org=0;
							group = "";
							groupCode = "";
							note = "";
							
							String salida = "";					
							BigDecimal openAmt = Env.ZERO;
							int qtyRegistros = jsonarr.length();
							if(qtyRegistros > 0)
							{
								for(int a=0; a<qtyRegistros;a++)
								{	
									salida = "";
									ID_invoice=0;
									inv=null;
									ID_Cargo =0;
									ID_Asset =0;
									ID_Org =0;
									IDEntero = 0;
									IDUsado = 0;
									existJCash = 0;
									rutProveedor = "";
									totalAmt = Env.ZERO;
									totalAmtMin = Env.ZERO;
									totalAmtMax = Env.ZERO;
									numDoc = "";
									tipoDoc = "";
									kilometraje = "";
									combustible = "";
									combustibleNum = Env.ZERO;
									
									centroCosto = "";
									ID = jsonarr.getJSONObject(a).get("Id").toString();
									total = jsonarr.getJSONObject(a).get("Total").toString();
									supplier = jsonarr.getJSONObject(a).get("Supplier").toString();
									//policyID = jsonarr.getJSONObject(a).get("ExpensePolicyId").toString();
									//UserId = jsonarr.getJSONObject(a).get("UserId").toString();
									group = jsonarr.getJSONObject(a).get("CategoryGroup").toString();
									groupCode = jsonarr.getJSONObject(a).get("CategoryGroupCode").toString();
									note = jsonarr.getJSONObject(a).get("Note").toString();
									
									//if(policyID == null)
									//	policyID="0";									
									//existPoli = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM RD_ExpensePolicies WHERE AD_Client_ID="+client.get_ID()+
									//		" AND expensepolicyid = "+policyID);				
									//if(existPoli > 0)
									//{
										if(cash != null)
										{
											//se lee detalle
											if(total != null && total.trim().length() > 0)
												totalAmt = new BigDecimal(total);
											if(totalAmt == null)
												totalAmt=Env.ZERO;
											JSONArray jsonArrDet = jsonarr.getJSONObject(a).getJSONArray("ExtraFields");
											int lenght = jsonArrDet.length();
											for(int b=0; b<lenght; b++)
											{
												if(jsonArrDet.getJSONObject(b).get("Name").toString() != null
														&& jsonArrDet.getJSONObject(b).get("Name").toString().compareTo("N\u00b0 Documento")==0)
												{
													numDoc=jsonArrDet.getJSONObject(b).get("Value").toString();
												}
												else if(jsonArrDet.getJSONObject(b).get("Name").toString() != null
														&& jsonArrDet.getJSONObject(b).get("Name").toString().compareTo("RUT Proveedor")==0)
												{
													rutProveedor=jsonArrDet.getJSONObject(b).get("Value").toString();
												}
												else if(jsonArrDet.getJSONObject(b).get("Name").toString() != null
														&& jsonArrDet.getJSONObject(b).get("Name").toString().compareTo("Kilometraje")==0)
												{
													kilometraje=jsonArrDet.getJSONObject(b).get("Value").toString();
												}
												else if(jsonArrDet.getJSONObject(b).get("Name").toString() != null
														&& jsonArrDet.getJSONObject(b).get("Name").toString().compareTo("Centro de Costo")==0)
												{
													centroCosto=jsonArrDet.getJSONObject(b).get("Value").toString();
												}
												else if(jsonArrDet.getJSONObject(b).get("Name").toString() != null
														&& jsonArrDet.getJSONObject(b).get("Name").toString().compareTo("Tipo de Documento")==0)
												{
													tipoDoc=jsonArrDet.getJSONObject(b).get("Value").toString();
												}
												else if(jsonArrDet.getJSONObject(b).get("Name").toString() != null
														&& jsonArrDet.getJSONObject(b).get("Name").toString().compareTo("Cantidad de Litros")==0)
												{
													combustible=jsonArrDet.getJSONObject(b).get("Value").toString();
												}
											}
											//se busca proveedor
											ID_Vendor = DB.getSQLValue(get_TrxName(), "SELECT C_BPartner_ID FROM C_BPartner "
													+ " WHERE value||'-'||digito like '"+rutProveedor+"' AND AD_Client_ID="+Env.getAD_Client_ID(getCtx()));
											if(ID_Vendor > 0)
											{
												//se busca factura
												ID_invoice = DB.getSQLValue(get_TrxName(), "SELECT C_Invoice_ID FROM C_Invoice "
														+ " WHERE IssoTrx='N' AND IsActive='Y' AND C_Bpartner_ID = "+ID_Vendor
														+ " AND DocumentNo = '"+numDoc.trim()+"' AND AD_Client_ID="+Env.getAD_Client_ID(getCtx()));
											}
											else
												salida = salida+" NO Existe Proveedor rut:"+rutProveedor;
												
											//se crea linea
											if(cash != null)
											{
												if(ID != null && ID.trim().length()>0)
													IDEntero = Integer.parseInt(ID);
												else
												{
													IDEntero = 0;
													IDUsado = 0;
												}
												if(IDEntero > 0)
													IDUsado = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM C_CashLine WHERE IDRindegastos="+IDEntero);
							
												if(IDUsado <=0)
												{
													MCashLine cLine = new MCashLine(cash);
													cLine.set_CustomColumn("C_Bpartner_ID",ID_Vendor);
													cLine.setC_Cash_ID(cash.get_ID());
													if(ID_invoice > 0)
													{
														//se crea objeto factura
														inv = new MInvoice(getCtx(), ID_invoice, get_TrxName());
														//se valida que factura tenga monto abierto
														openAmt = DB.getSQLValueBD(get_TrxName(), "SELECT invoiceopen(C_invoice_ID,0) FROM C_Invoice WHERE C_Invoice_ID = "+ID_invoice);
														if(openAmt == null)
															openAmt = Env.ZERO;
														if(openAmt.compareTo(Env.ZERO) > 0)
														{
															cLine.setCashType("I");
															cLine.setC_Invoice_ID(ID_invoice);
														}
														else
														{
															salida = salida+" Factura YA Pagada.";
															cLine.setCashType("C");
															cLine.setC_Charge_ID(ID_ChargeBase);//
														}													
														//se agregan nuevos mensajes de validacion
														//validacion con monto es por un rango de 5 pesos
														totalAmtMin = totalAmt.subtract(new BigDecimal("5.0"));
														totalAmtMax = totalAmt.add(new BigDecimal("5.0"));
														if(inv.getGrandTotal().compareTo(totalAmtMin) < 0 || 
																inv.getGrandTotal().compareTo(totalAmtMax) > 0)
															salida = salida+" Monto no coincide.";
														if(inv.getDocStatus().compareTo("CO") != 0)
															salida = salida+" Factura NO Completa.";
													}
													else
													{
														//si existe numero de documento y no existe factura
														if(numDoc != null && numDoc.trim().length()>0)
															salida = salida+" No existe factura asociada.";
																
														cLine.setCashType("C");
														//se busca cargo
														ID_Cargo = DB.getSQLValue(get_TrxName(), "SELECT MAX(C_Charge_ID) FROM C_Charge "
																+ " WHERE name like '"+group+"' AND IsActive='Y' AND AD_Client_ID = "+client.get_ID());
														if(ID_Cargo<=0)
														{
															ID_Cargo=ID_ChargeBase;
															salida = salida+" No existe cargo asociado:"+group;
														}										
														cLine.setC_Charge_ID(ID_Cargo);//
													}
													//se busca recurso
													ID_Asset = DB.getSQLValue(get_TrxName(), "SELECT MAX(A_Asset_ID) FROM A_Asset "
															+ " WHERE name like '"+groupCode+"' AND IsActive='Y' AND AD_Client_ID = "+client.get_ID());
													if(ID_Asset > 0)
														cLine.set_CustomColumn("A_AssetRef_ID", ID_Asset);	
													else
														salida=salida+" No se encontro Recurso:"+groupCode;
													
													//se busca centro de costo
													if(centroCosto != null)
													{
														ID_Org = DB.getSQLValue(get_TrxName(), "SELECT MAX(AD_Org_ID) FROM AD_Org "
																+ " WHERE upper(name) like '"+centroCosto.toUpperCase()+"' AND IsActive='Y' AND AD_Client_ID = "+client.get_ID());
														if(ID_Org > 0)
															cLine.setAD_Org_ID(ID_Org);	
														else
															salida=salida+" No se encontro Centro de costo:"+centroCosto;
													}									
													//se revisar� si posee notas sino se setea como hasta ahora
													if(note != null && note.trim().length() > 1)
														cLine.setDescription(note);
													else
														cLine.setDescription("ID:"+ID+" total:"+total+" rut proveedor:"+rutProveedor+" numero doc:"+numDoc+salida);
													cLine.setC_Currency_ID(228);
													if(tipoDoc.toUpperCase().contains("NOTA DE CREDITO"))
														cLine.setAmount(totalAmt);
													else
														cLine.setAmount(totalAmt.negate());
													if(IDEntero > 0)
														cLine.set_CustomColumn("IDRindegastos",IDEntero);
													//kilometraje
													if(kilometraje != null && kilometraje.trim().length()>0)
													{
														try
														{
															kilometrajeNum = new BigDecimal(kilometraje);
														}catch (Exception e)
														{
															kilometrajeNum = Env.ZERO;
														}
														cLine.set_CustomColumn("KILOMETRAJE",kilometrajeNum);
													}
													//combustible
													if(combustible != null && combustible.trim().length()>0)
													{
														try
														{
															combustible = combustible.replace(",", ".");
															combustibleNum = new BigDecimal(combustible);
														}catch (Exception e)
														{
															combustibleNum = Env.ZERO;
														}
														cLine.set_CustomColumn("COMBUSTIBLE",combustibleNum);
													}
													cLine.saveEx(get_TrxName());
													cant++;
												}
											}
										}
										log.config("json : "+json.toString());
										log.config("ID "+ID);
										log.config("total "+total);
										log.config("supplier "+supplier);
										log.config("rutProveedor "+rutProveedor);
										log.config("numDoc "+numDoc);
									//}
								}
							}
						}
					}
				}
			}
			log.config("ININOLES id de diarios a netear:"+ID_Cash);
			//se revisan todos los diarios y se crean lineas de neteo
			int ID_ChargeNeteo = DB.getSQLValue(get_TrxName(), "select MAX(C_Charge_ID) from C_Charge where name like '112010%' AND AD_Client_ID = "+Env.getAD_Client_ID(getCtx()));
			BigDecimal amtNeteo = Env.ZERO;
			int ID_BPNeteo = 0;
			if(ID_ChargeNeteo > 0)
			{
				String sqlIds = "SELECT C_Cash_ID FROM C_Cash WHERE C_Cash_ID IN ("+ID_Cash+")";
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				//try
				//{
					pstmt = DB.prepareStatement(sqlIds, get_TrxName());
					rs = pstmt.executeQuery();
					while(rs.next())
					{	
						if(rs.getInt("C_Cash_ID") > 0)
						{
							MCash cashNet = new MCash(getCtx(), rs.getInt("C_Cash_ID"), get_TrxName());
							//se calcula monto de sumatoria 
							amtNeteo = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(amount) FROM C_CashLine"
									+ " WHERE C_Cash_ID="+rs.getInt("C_Cash_ID"));
							if(amtNeteo != null && amtNeteo.compareTo(Env.ZERO) != 0)
							{
								amtNeteo = amtNeteo.negate();
								if(cashNet.getCashBook().getDescription() != null)
								{
									ID_BPNeteo = DB.getSQLValue(get_TrxName(), "SELECT MAX(C_BPartner_ID) FROM C_BPartner "
											+ " WHERE UPPER(trim(name)) like '"+cashNet.getCashBook().getDescription().trim().toUpperCase()+"' AND AD_Client_ID="+cashNet.getAD_Client_ID());
									if(ID_BPNeteo > 0)
									{
									//se crea linea 
										MCashLine nLineNet = new MCashLine(cashNet);
										nLineNet.setCashType("C");
										nLineNet.setAmount(amtNeteo);
										nLineNet.setC_Charge_ID(ID_ChargeNeteo);
										nLineNet.setDescription("Linea de neteo automatico");
										nLineNet.set_CustomColumn("C_BPartner_ID", ID_BPNeteo);
										nLineNet.saveEx(get_TrxName());
									}
								}
							}
						}
					}
				/*}
				catch (Exception e)
				{
					log.config(e.toString());
				}
				finally
				{
					DB.close(rs, pstmt);
					rs = null; pstmt = null;
				}*/
			}
		}
		
		return "Se han creado "+cantHead+" cabeceras. Se han agregado "+cant+" lineas de rendici�n";
	}	//	doIt
	
	
}	//	CopyFromOrder
