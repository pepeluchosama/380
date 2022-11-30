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
 * Contributor(s): Chris Farley - northernbrewer                              *
 *****************************************************************************/
package org.minsal.process;

import java.util.logging.Level;

import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.ofb.utils.DateUtils;
/**
 *	
 *	
 */
public class CreateInvFromInOut extends SvrProcess
{	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	private int p_OrgID;
	protected void prepare()
	{	

		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			
			if(name.equals("AD_Org_ID"))
				p_OrgID = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message 
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{	
		MInOut iout = new MInOut(getCtx(), getRecord_ID(), get_TrxName());
		//se crea consumo
		MInventory inv = new MInventory(getCtx(), 0, get_TrxName());
		inv.setAD_Org_ID(p_OrgID);
		inv.setM_Warehouse_ID(iout.getM_Warehouse_ID());
		inv.setC_DocType_ID(2000188);
		inv.setMovementDate(DateUtils.today());
		//se setean relaciones
		inv.set_CustomColumn("M_InOut_ID", iout.get_ID());
		inv.saveEx(get_TrxName());
		DB.executeUpdate("UPDATE M_InOut SET M_Inventory_ID="+inv.get_ID()
		+" WHERE M_InOut_ID="+iout.get_ID(), get_TrxName());
		
		//se crean lineas
		MInOutLine[] ioLines = iout.getLines(true);
		for (int i = 0; i < ioLines.length; i++)
		{
			MInOutLine ioLine = ioLines[i];
			MInventoryLine iLine = new MInventoryLine(getCtx(), 0, get_TrxName());
			iLine.setM_Inventory_ID(inv.get_ID());
			iLine.setM_Product_ID(ioLine.getM_Product_ID());
			if(ioLine.getM_AttributeSetInstance_ID() > 0)
				iLine.setM_AttributeSetInstance_ID(ioLine.getM_AttributeSetInstance_ID());
			iLine.setQtyInternalUse(ioLine.getQtyEntered());
			iLine.setC_Charge_ID(2000003);
			iLine.setM_Locator_ID(ioLine.getM_Locator_ID());
			//precio se setea desde OC
			if(ioLine.getC_OrderLine_ID() > 0)
			{
				iLine.set_CustomColumn("PriceList",ioLine.getC_OrderLine().getPriceEntered());
				iLine.set_CustomColumn("LineNetAmt", ioLine.getC_OrderLine().getPriceEntered().multiply(ioLine.getQtyEntered()));
			}
			iLine.saveEx(get_TrxName());
		}
		return "Se ha generado despacho N°"+inv.getDocumentNo();
	}	//	doIt
	
	
}	

