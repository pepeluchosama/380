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

import java.math.BigDecimal;

import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;


/**
 *	Validator for PDV Colegios
 *
 *  @author Italo Ni�oles
 */
public class ModTSMValidQtyAmtOC implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModTSMValidQtyAmtOC ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModTSMValidQtyAmtOC.class);
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
		engine.addModelChange(MOrderLine.Table_Name, this);		
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	OFB Consulting Ltda. By Julio Far�as
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if((type == TYPE_BEFORE_CHANGE || type == TYPE_BEFORE_NEW) && po.get_Table_ID()==MOrderLine.Table_ID) 
		{	
			MOrderLine oLine = (MOrderLine)po;
			MOrder order = new MOrder(po.getCtx(), oLine.getC_Order_ID(), po.get_TrxName());
			if(order.isSOTrx() == false)
			{
				if (oLine.get_ValueAsInt("M_RequisitionLine_ID") > 0 && order.getC_DocTypeTarget().getName().toLowerCase().contains("compra"))
				{
					MRequisitionLine rLine = new MRequisitionLine(po.getCtx(), oLine.get_ValueAsInt("M_RequisitionLine_ID"), po.get_TrxName());
					//validacion de cantidad
					String sqlVal= "SELECT SUM(qtyEntered) FROM C_OrderLine col " +
					" INNER JOIN C_Order co on (col.C_Order_ID = co.C_Order_ID)" +
					" WHERE co.DocStatus IN ('CO','CL','DR','IP') AND " +
					" col.M_requisitionLine_ID = "+oLine.get_ValueAsInt("M_RequisitionLine_ID")+
					" AND C_OrderLine_ID <> "+oLine.get_ID();
					
					BigDecimal qtyTotal = DB.getSQLValueBD(po.get_TrxName(), sqlVal);
					if (qtyTotal == null)
						qtyTotal = Env.ZERO;
					qtyTotal = qtyTotal.add(oLine.getQtyEntered());					
					if (qtyTotal.compareTo(rLine.getQty()) > 0)
					{
						return "Cantidad Supera Cantidad de Solicitud";
					}
					//validacion de monto
					String sqlValAmt= "SELECT SUM(LineNetAmt) FROM C_OrderLine col " +
					" INNER JOIN C_Order co on (col.C_Order_ID = co.C_Order_ID)" +
					" WHERE co.DocStatus IN ('CO','CL','DR','IP') AND " +
					" col.M_requisitionLine_ID = "+oLine.get_ValueAsInt("M_RequisitionLine_ID")+
					" AND C_OrderLine_ID <> "+oLine.get_ID();
					
					BigDecimal amtTotal = DB.getSQLValueBD(po.get_TrxName(), sqlValAmt);
					if (amtTotal == null)
						amtTotal = Env.ZERO;
					amtTotal = amtTotal.add(oLine.getLineNetAmt());					
					if (amtTotal.compareTo(rLine.getLineNetAmt().setScale(order.getC_Currency().getStdPrecision(),BigDecimal.ROUND_HALF_EVEN)) > 0)
					{
						return "Monto Supera Monto de la Solicitud";
					}
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