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
package org.qdc.model;

import java.math.BigDecimal;

import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
/**
 *	Validator for company QDC
 *
 *  @author ininoles
 */
public class ModQDCUpdateQtyDelivered implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModQDCUpdateQtyDelivered ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModQDCUpdateQtyDelivered.class);
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
		engine.addDocValidate(MInOut.Table_Name, this);		
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	OFB Consulting Ltda. By mfrojas
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
			MInOut ship = (MInOut)po;
			if(ship.getC_Order_ID() > 0)
			{
				MOrder order = new MOrder(po.getCtx(), ship.getC_Order_ID(),po.get_TrxName());
				if(order.getC_DocType_ID() ==1000078
						|| order.getC_DocType_ID() ==1000085
						|| order.getC_DocType_ID() ==1000029)
				{
					MOrderLine[] lines = order.getLines(true, null);	//	Line is default
					for (int i = 0; i < lines.length; i++)
					{
						MOrderLine line = lines[i];
						/*BigDecimal qtyDelivered = DB.getSQLValueBD(po.get_TrxName(), "select SUM(movementqty) FROM M_InOutLine iol"
								+ " INNER JOIN M_InOut io ON (iol.M_InOut_ID = io.M_InOut_ID)"
								+ " WHERE iol.C_OrderLine_ID = "+line.get_ID()+" AND io.DocStatus IN ('CO')");
								*/
						//mfrojas 20220825 Se cambia query. La original no entregaba resultados. Esto se ejecuta after complete, 
						//así que no es necesario revisar el estado del documento, que aún no existe en la db.
						
						BigDecimal qtyDelivered = DB.getSQLValueBD(po.get_TrxName(), "SELECT coalesce(sum(movementqty),0) from " +
								" m_inoutline iol inner join m_inout io on (iol.m_inout_id = io.m_inout_id) " +
								" where iol.c_orderline_id = ? ", line.get_ID());
						log.config("qty delivered = "+qtyDelivered);
						if(qtyDelivered != null && qtyDelivered.compareTo(Env.ZERO) > 0)
						{
							line.setQtyDelivered(qtyDelivered);
							line.setQtyReserved(line.getQtyOrdered().subtract(qtyDelivered));
							line.saveEx(po.get_TrxName());
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
		StringBuffer sb = new StringBuffer ("ModelPrice");
		return sb.toString ();
	}	//	toString


	

}	