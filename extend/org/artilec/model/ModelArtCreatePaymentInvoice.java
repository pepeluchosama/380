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
package org.artilec.model;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import org.compiere.model.MInvoice;
import org.compiere.model.MPayment;

/**
 *	Validator for Artilec
 *  Create Payment or Pagare 
 *  @author mfrojas
 */
public class ModelArtCreatePaymentInvoice implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelArtCreatePaymentInvoice ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelArtCreatePaymentInvoice.class);
	/** Client			*/
	private int		m_AD_Client_ID = -1;
	

	/**
	 *	Initialize Validation
	 *	@param engine validation engine
	 *	@param client client
	 */
	public void initialize (ModelValidationEngine engine, MClient client)
	{
		//client = null for global validator
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		}
		else  {
			log.info("Initializing Model Price Validator: "+this.toString());
		}

		//	Tables to be monitored
		//engine.addModelChange(MInvoice.Table_Name, this);

		engine.addDocValidate(MInvoice.Table_Name, this);

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		return null;
	}	//	modelChange

	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MInvoice.Table_ID)
		{
			MInvoice inv = (MInvoice)po;
			log.config("estado "+inv.getDocStatus());
			if(inv.isSOTrx() && inv.getC_DocType().getDocBaseType().compareTo("CDC") != 0 && inv.getC_DocType_ID() != 2000143
					 && inv.getC_DocType_ID() != 2000096)
			{

					//Si se completa la Factura, se debe ver si tiene una orden asociada.
					

						
						//Se debe validar cual es la regla de pago. 
						//Si es distinto de efectivo o pagare, entonces se crea directamente el pago.
						
						if(inv.getPaymentRule().compareTo("B") != 0 && inv.getPaymentRule().compareTo("G") != 0)
						{
							//Distinto de efectivo (CASH), así que se debe crear el pago.
							int bankaccount = DB.getSQLValue(po.get_TrxName(), "SELECT coalesce(max(c_bankaccount_id),0) "
									+ " FROM c_bankaccount where ad_Client_id = 2000006 and isactive='Y'");
							
							MPayment pay = new MPayment(po.getCtx(), 0, po.get_TrxName());
							pay.setC_BPartner_ID(inv.getC_BPartner_ID());
							pay.setDescription(inv.getDescription());
							pay.setC_DocType_ID(2000100);
							pay.setDateTrx(inv.getDateInvoiced());
							pay.setDateAcct(inv.getDateAcct());
							pay.setC_Invoice_ID(inv.get_ID());
							pay.setPayAmt(inv.getGrandTotal());
							//if de tendertype con paymentrule
							if(inv.getPaymentRule().compareTo("S") == 0)
								pay.setTenderType("K");
							else if(inv.getPaymentRule().compareTo("P") == 0)
								pay.setTenderType("K");
							else if(inv.getPaymentRule().compareTo("M") == 0)
							{
								//Si es tipo de pago M (contado) hay que mirar el paymentterm
								log.config("payment term "+inv.getC_PaymentTerm().getName());
								if(inv.getC_PaymentTerm().getName().compareTo("Efectivo") == 0)
									pay.setTenderType("T");
								if(inv.getC_PaymentTerm().getName().compareTo("Tarjeta de credito") == 0)
									pay.setTenderType("C");
								if(inv.getC_PaymentTerm().getName().compareTo("Tarjeta de debito") == 0)
									pay.setTenderType("D");
								if(inv.getC_PaymentTerm().getName().compareTo("Transferencia") == 0)
									pay.setTenderType("A");
								if(inv.getC_PaymentTerm().getName().compareTo("Webpay Plus") == 0)
									pay.setTenderType("A");
							}
							else
								pay.setTenderType("D");
							log.config(inv.getPaymentRule());
							pay.setC_Currency_ID(inv.getC_Currency_ID());
							//Cuenta por defecto a la espera de definicion
							pay.setC_BankAccount_ID(2000006);
							//pay.setAD_Org_ID(ord.getAD_Org_ID());
							//mfrojas 20210507
							pay.setIsOverUnderPayment(true);
							pay.setAD_Org_ID(inv.getAD_Org_ID());
							pay.save();
							pay.setAD_Org_ID(inv.getAD_Org_ID());
							pay.save();
						}
						

					}

										
					
				

				
			}

		
		
		return null;
	}	//	docValidate
	
	/**
	 *	User Login.
	 *	Called when preferences are set
	 *	@param AD_Org_ID org
	 *	@param AD_Role_ID role
	 *	@param AD_User_ID user
	 *	@return error message or null
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);

		return null;
	}	//	login


	/**
	 *	Get Client to be monitored
	 *	@return AD_Client_ID client
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID


	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("ModelPrice");
		return sb.toString ();
	}	//	toString


	

}	