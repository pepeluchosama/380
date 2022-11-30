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
package org.minju.model;

import org.compiere.model.MClient;
import org.compiere.model.MCash;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.model.MPayment;

/**
 *	Validator for MINJU
 *
 *  @author mfrojas
 */
public class ModMINJUCashCreateOrUpdatePayment implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModMINJUCashCreateOrUpdatePayment ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModMINJUCashCreateOrUpdatePayment.class);
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
		engine.addModelChange(MCash.Table_Name, this);		
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if(type == TYPE_AFTER_CHANGE && po.get_Table_ID()==MCash.Table_ID
				&& po.is_ValueChanged("DocStatus")) 
		{	
			MCash cash = (MCash)po;
			//Si el documento cambio a estado completo, se debe revisar si
			//el diario tiene pago asociado. En caso de tenerlo, debe poner un correlativo.
			//Si no lo tiene, debe generarlo.
			
			if(cash.getDocStatus().compareTo("CO")==0)
			{
				//Primero, verificar si existe algun anticipo en las lineas.
				String sql = "SELECT count(1) from C_CashLine where CashType = 'T' AND C_Cash_ID = "+cash.get_ID();
				int counter = DB.getSQLValue(po.get_TrxName(), sql);
				
				if(counter == 0)
				{
					//Si contador == 0, entonces se debe realizar un pago asociado al diario
					MPayment pay = new MPayment(po.getCtx(),0,po.get_TrxName());
					pay.setAD_Org_ID(cash.getAD_Org_ID());
					String documentNo = cash.getDocumentNo();
					String documentNo2 = cash.get_Value("DocumentNo").toString();
					pay.setDocumentNo(documentNo2);
					pay.setR_PnRef(documentNo2);
					pay.set_ValueNoCheck("TrxType", "X");		//	Transfer
					pay.set_ValueNoCheck("TenderType", "X");
					//
					//Modification for cash payment - Posterita
					pay.setC_CashBook_ID(cash.getC_CashBook_ID());
					//End of modification - Posterita
					int bankaccount = DB.getSQLValue(po.get_TrxName(), "SELECT max(c_bankaccountref_id) from c_Cashline where c_Cash_id = "+cash.get_ID());
					pay.setC_BankAccount_ID(bankaccount);
					pay.setC_DocType_ID(true);	//	Receipt
					pay.setDateTrx(cash.getStatementDate());
					pay.setDateAcct(cash.getDateAcct());
					pay.setAmount(cash.getC_Currency_ID(),cash.getStatementDifference());	//	Transfer
					pay.setDescription(cash.getDescription());
					pay.setDocStatus(MPayment.DOCSTATUS_Closed);
					pay.setDocAction(MPayment.DOCACTION_None);
					pay.setPosted(true);
					pay.setIsAllocated(true);	//	Has No Allocation!
					pay.setProcessed(true);
					pay.setDocStatus("CO");
					//Obtener socio de negocio a partir de createdby
					int cbpartner = DB.getSQLValue(po.get_TrxName(), "SELECT coalesce(c_bpartner_id,0) from ad_user where ad_user_id = "+cash.getCreatedBy());
					
					if(cbpartner <= 0)
						cbpartner = DB.getSQLValue(po.get_TrxName(), "SELECT max(c_bpartner_id) from c_bpartner where lower(name) like '%ministerio%justicia%'");
					pay.setC_BPartner_ID(cbpartner);
					
					pay.setTrxType("S");
					pay.setTenderType("K");
					pay.set_CustomColumn("BudgetReference", cash.get_Value("BudgetReference").toString());
					pay.set_CustomColumn("CodeBudget", cash.get_Value("CodeBudget").toString());
					pay.save();

					
				}
				else
				{

					
				}
			}
		}		
		return null;
	}	//	modelChange

	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		
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