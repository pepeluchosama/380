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
package org.ofb.model;

import java.math.BigDecimal;

import org.compiere.model.MClient;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_M_Product_Category_Budget;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	Validator default OFB
 *
 *  @author Italo Ni�oles
 */
public class ModelOFBBudget implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelOFBBudget ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelOFBBudget.class);
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
			log.info("Initializing global validator: "+this.toString());
		}

		//	Tables to be monitored
		//engine.addModelChange(MRequisition.Table_Name, this);
		//
		engine.addDocValidate(MRequisition.Table_Name, this);
		

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public static final String DOCSTATUS_Drafted = "DR";
	public static final String DOCSTATUS_Completed = "CO";
	public static final String DOCSTATUS_InProgress = "IP";
	public static final String DOCSTATUS_Voided = "VO";
	
	
	public String modelChange (PO po, int type) throws Exception
	{
		return null;
	}	//	modelChange

	/**
	 *	Validate Document.
	 *	Called as first step of DocAction.prepareIt
     *	when you called addDocValidate for the table.
     *	Note that totals, etc. may not be correct.
	 *	@param po persistent object
	 *	@param timing see TIMING_ constants
     *	@return error message or null
	 */
	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		if(timing == TIMING_BEFORE_COMPLETE && po.get_Table_ID()==MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			if(!req.get_ValueAsBoolean("Overwrite"))
			{
				MRequisitionLine[] lines = req.getLines();
				for (int lineIndex = 0; lineIndex < lines.length; lineIndex++)
				{
					MRequisitionLine rLine = lines[lineIndex];
					if(rLine.getM_Product_ID() > 0)
					{
						int yearReq = req.getDateDoc().getYear()+1900;
						//se busca a�o
						int yearAD = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(C_Year_ID) FROM C_Year WHERE FiscalYear = '"+yearReq+"'");
						if(yearAD > 0)
						{
							//se busca presupuestp
							int ID_Budget = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(M_Product_Category_Budget_ID) FROM M_Product_Category_Budget " +
									" WHERE C_Year_ID = "+yearAD+" AND M_Product_Category_ID ="+rLine.getM_Product().getM_Product_Category_ID());
							if(ID_Budget > 0)
							{
								//revisamos 
								X_M_Product_Category_Budget bud = new X_M_Product_Category_Budget(po.getCtx(), ID_Budget, po.get_TrxName());
								BigDecimal AmtAvai = bud.getAmt().subtract(bud.getAmtUsed());
								BigDecimal amtTLines = DB.getSQLValueBD(po.get_TrxName(), "SELECT SUM(LineNetAmt) FROM M_requisitionLine rl " +
										" INNER JOIN M_Product mp ON (rl.M_Product_ID = mp.M_Product_ID)" +
										" WHERE mp.M_Product_Category_ID = "+rLine.getM_Product().getM_Product_Category_ID()+
										" AND rl.M_requisition_ID = "+rLine.getM_Requisition_ID()+
										" AND M_requisitionLine_ID <> "+rLine.get_ID());
								if(amtTLines == null)
									amtTLines = Env.ZERO;
								amtTLines = amtTLines.add(rLine.getLineNetAmt());
								if(amtTLines.compareTo(AmtAvai) > 0)
									return "ERROR: Presupuesto Insuficiente linea "+rLine.getLine();
							}
						}
					}
				}
			}
		}
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			MRequisitionLine[] lines = req.getLines();
			for (int lineIndex = 0; lineIndex < lines.length; lineIndex++)
			{
				MRequisitionLine rLine = lines[lineIndex];
				if(rLine.getM_Product_ID() > 0)
				{
					int yearReq = req.getDateDoc().getYear()+1900;
					//se busca a�o
					int yearAD = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(C_Year_ID) FROM C_Year WHERE FiscalYear = '"+yearReq+"'");
					if(yearAD > 0)
					{
						//se busca presupuestp
						int ID_Budget = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(M_Product_Category_Budget_ID) FROM M_Product_Category_Budget " +
								" WHERE C_Year_ID = "+yearAD+" AND M_Product_Category_ID ="+rLine.getM_Product().getM_Product_Category_ID());
						if(ID_Budget > 0)
						{
							X_M_Product_Category_Budget bud = new X_M_Product_Category_Budget(po.getCtx(), ID_Budget, po.get_TrxName());
							bud.setAmtUsed(bud.getAmtUsed().add(rLine.getLineNetAmt()));
							bud.saveEx(po.get_TrxName());
						}
					}
				}
			}	
		}
		if(timing == TIMING_AFTER_VOID && po.get_Table_ID()==MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			MRequisitionLine[] lines = req.getLines();
			for (int lineIndex = 0; lineIndex < lines.length; lineIndex++)
			{
				MRequisitionLine rLine = lines[lineIndex];
				if(rLine.getM_Product_ID() > 0)
				{
					int yearReq = req.getDateDoc().getYear()+1900;
					//se busca a�o
					int yearAD = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(C_Year_ID) FROM C_Year WHERE FiscalYear = '"+yearReq+"'");
					if(yearAD > 0)
					{
						//se busca presupuestp
						int ID_Budget = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(M_Product_Category_Budget_ID) FROM M_Product_Category_Budget " +
								" WHERE C_Year_ID = "+yearAD+" AND M_Product_Category_ID ="+rLine.getM_Product().getM_Product_Category_ID());
						if(ID_Budget > 0)
						{
							X_M_Product_Category_Budget bud = new X_M_Product_Category_Budget(po.getCtx(), ID_Budget, po.get_TrxName());
							bud.setAmtUsed(bud.getAmtUsed().subtract(rLine.getLineNetAmt()));
							bud.saveEx(po.get_TrxName());
						}
					}
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
		StringBuffer sb = new StringBuffer ("QSS_Validator");
		return sb.toString ();
	}	//	toString	
}	