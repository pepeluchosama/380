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
package org.tsm.model;

import org.compiere.model.MAsset;
import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;


/**
 *	Validator for TSM
 *
 *  @author Italo Ni�oles
 */
public class ModTSMUpAssetCustodio implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModTSMUpAssetCustodio ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModTSMUpAssetCustodio.class);
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

		//	Document to be monitored
		engine.addDocValidate(MInOut.Table_Name, this);
		engine.addDocValidate(MOrder.Table_Name, this);
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	OFB Consulting Ltda. By Julio Far�as
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
					
		return null;
	}	//	modelChange

	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MInOut.Table_ID)
		{	
			MInOut receipt = (MInOut) po;
			MInOutLine[] rlines = receipt.getLines(false);
			
			for (int i = 0; i < rlines.length; i++)
			{	
				MInOutLine rline = rlines[i];
				if(rline.get_ValueAsInt("A_Asset_ID") > 0 && rline.getC_Charge().getC_ChargeType().getValue().compareToIgnoreCase("TCRC") == 0)					
				{
					MAsset asset = new MAsset(po.getCtx(), rline.get_ValueAsInt("A_Asset_ID"), po.get_TrxName());
					asset.set_CustomColumn("custodio", "TN");
					asset.save();
				}	
				if(rline.get_ValueAsInt("A_Asset_ID") > 0 && rline.getC_Charge().getC_ChargeType().getValue().compareToIgnoreCase("TCBN") == 0)					
				{
					MAsset asset = new MAsset(po.getCtx(), rline.get_ValueAsInt("A_Asset_ID"), po.get_TrxName());
					asset.set_CustomColumn("custodio", "DB");
					asset.setIsActive(false);
					asset.save();
					//se deja cantidad en 0 para que no aparesca en crear desde en factura
					DB.executeUpdate("UPDATE M_InOutLine SET MovementQty = 0 WHERE M_InOutLine_ID = "+rline.get_ID(), po.get_TrxName());
				}
				if(rline.get_ValueAsInt("A_Asset_ID") > 0 && rline.getC_Charge().getC_ChargeType().getValue().compareToIgnoreCase("TCAF") == 0)					
				{
					MAsset asset = new MAsset(po.getCtx(), rline.get_ValueAsInt("A_Asset_ID"), po.get_TrxName());
					asset.set_CustomColumn("custodio", "AN");
					asset.save();
				}
			}
		}
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MOrder.Table_ID)
		{	
			MOrder order = (MOrder) po;
			MOrderLine[] olines = order.getLines();
			
			for (int i = 0; i < olines.length; i++)
			{	
				MOrderLine oline = olines[i];
				if(oline.get_ValueAsInt("A_Asset_ID") > 0 && oline.getC_Charge().getC_ChargeType().getValue().compareToIgnoreCase("TCRC") == 0)					
				{
					MAsset asset = new MAsset(po.getCtx(), oline.get_ValueAsInt("A_Asset_ID"), po.get_TrxName());
					asset.set_CustomColumn("custodio", "ER");
					asset.save();
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

	public static boolean isNumeric(String str) {
        return (str.matches("[+-]?\\d*(\\.\\d+)?") && str.equals("")==false);
    }
	

}	
