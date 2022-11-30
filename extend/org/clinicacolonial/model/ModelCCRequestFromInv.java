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
package org.clinicacolonial.model;

import java.math.BigDecimal;

import org.compiere.model.MClient;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
/**
 *
 *  @author Italo Niñoles
 */
public class ModelCCRequestFromInv implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelCCRequestFromInv ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelCCRequestFromInv.class);
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
		engine.addModelChange(MInventoryLine.Table_Name, this);
		//	Documents to be monitored
		engine.addDocValidate(MInventory.Table_Name, this);

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
		log.info(po.get_TableName() + " Type: "+type);
		if(type == TYPE_BEFORE_DELETE && po.get_Table_ID()==MInventoryLine.Table_ID)
		{
			MInventoryLine iLine = (MInventoryLine) po;
			MInventory inv = new MInventory(po.getCtx(), iLine.getM_Inventory_ID(), po.get_TrxName());
			if(inv.get_ValueAsInt("M_Requisition_ID") > 0)
			{
				int ID_ReqLine = DB.getSQLValue(po.get_TrxName(), "SELECT M_RequisitionLine_ID FROM M_RequisitionLine"
						+" WHERE M_Requisition_ID="+inv.get_ValueAsInt("M_Requisition_ID")
						+" AND M_product_ID="+iLine.getM_Product_ID());
				if(ID_ReqLine > 0)
				{
					DB.executeUpdate("delete from pp_mrp"
							+ " where M_RequisitionLine_ID="+ID_ReqLine,po.get_TrxName());
					
					DB.executeUpdate("delete from M_RequisitionLine"
							+ " where M_RequisitionLine_ID="+ID_ReqLine,po.get_TrxName());
				}
			}
		}
		if(type == TYPE_BEFORE_CHANGE && po.get_Table_ID()==MInventoryLine.Table_ID
				&& po.is_ValueChanged("M_Product_ID"))
		{
			MInventoryLine iLine = (MInventoryLine) po;
			MInventory inv = new MInventory(po.getCtx(), iLine.getM_Inventory_ID(), po.get_TrxName());
			if(inv.get_ValueAsInt("M_Requisition_ID") > 0)
			{
				int ID_ReqLine = DB.getSQLValue(po.get_TrxName(), "SELECT M_RequisitionLine_ID FROM M_RequisitionLine"
						+" WHERE M_Requisition_ID="+inv.get_ValueAsInt("M_Requisition_ID")
						+" AND M_product_ID="+po.get_ValueOldAsInt("M_Product_ID"));
				if(ID_ReqLine > 0)
				{	
					DB.executeUpdate("UPDATE M_RequisitionLine SET M_product_ID = "+iLine.getM_Product_ID()+
							" WHERE M_RequisitionLine_ID="+ID_ReqLine,po.get_TrxName());
				}
			}
		}
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
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MInventory.Table_ID)
		{
			int ID_ReqLine = 0;
			MInventory inv = (MInventory) po;
			if(inv.get_ValueAsInt("M_Requisition_ID") > 0)
			{
				MInventoryLine[] lines = inv.getLines(true);
				for (int i = 0; i < lines.length; i++)
				{
					MInventoryLine line = lines[i];
					if (line.getM_Product_ID() >0)
					{
						//se busca linea de solicitud
						ID_ReqLine = DB.getSQLValue(po.get_TrxName(), "SELECT M_RequisitionLine_ID FROM M_RequisitionLine"
								+" WHERE M_Requisition_ID="+inv.get_ValueAsInt("M_Requisition_ID")
								+" AND M_product_ID="+line.getM_Product_ID());
						BigDecimal qty = line.getQtyInternalUse();
						if(ID_ReqLine > 0)
						{	
							DB.executeUpdate("UPDATE M_RequisitionLine SET qty="+qty+" WHERE M_RequisitionLine_ID="+ID_ReqLine, po.get_TrxName());
						}
						else//si no existe se crea linea
						{
							MRequisition req = new MRequisition(po.getCtx(), inv.get_ValueAsInt("M_Requisition_ID"), po.get_TrxName());
							//se descompleta solicitud por update
							DB.executeUpdate("UPDATE M_Requisition SET DocStatus='DR', Processed='N' "
									+ " WHERE M_Requisition_ID="+inv.get_ValueAsInt("M_Requisition_ID"), po.get_TrxName());
							req.setDocStatus("DR");
							req.saveEx(po.get_TrxName());
							
							//se crea linea 
							MRequisitionLine reqLine = new MRequisitionLine(req);
							reqLine.setAD_Org_ID(line.getAD_Org_ID());
							reqLine.setM_Product_ID(line.getM_Product_ID());
							reqLine.setQty(qty);
							reqLine.saveEx(po.get_TrxName());
							
							//se completa solicitud por update
							DB.executeUpdate("UPDATE M_Requisition SET DocStatus='CO', Processed='Y' "
									+ " WHERE M_Requisition_ID="+inv.get_ValueAsInt("M_Requisition_ID"), po.get_TrxName());
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