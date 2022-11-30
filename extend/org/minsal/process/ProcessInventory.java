/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
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
package org.minsal.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInventory;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
/**
 *	
 *	
 */

public class ProcessInventory extends SvrProcess
{
	private int				p_Inventory_ID = 0; 
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	 protected void prepare()
	{	
		 p_Inventory_ID=getRecord_ID();
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		if (p_Inventory_ID > 0)
		{
			MInventory req = new MInventory(getCtx(), p_Inventory_ID, get_TrxName());
			String docStatusNew = "CO";
			if(req.getDocStatus().compareTo("DR") == 0)
				docStatusNew = "RT";
			else if(req.getDocStatus().compareTo("RT") == 0)
				docStatusNew = "CO";
			
			//validacion de rol
			int cant = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM AD_Document_Action_Access daa" +
					" INNER JOIN AD_Ref_List rl ON (daa.AD_Ref_List_ID = rl.AD_Ref_List_ID) " +
					" WHERE value = '"+docStatusNew+"' AND AD_Role_ID = "+Env.getAD_Role_ID(getCtx())+
					" AND C_DocType_ID = "+req.getC_DocType_ID());
			//procesar
			log.config("cantidad"+cant);
			if(cant > 0)
			{
				if(req.getDocStatus().compareTo("DR") == 0)
					req.setDocStatus("RT");
				else if(req.getDocStatus().compareTo("RT") == 0)
				{
					req.setDocStatus("DR");
					req.processIt("CO");
				}
				req.saveEx(get_TrxName());
			}
			else
				throw new AdempiereException("Error: Permisos de rol insuficientes");
		}
	   return "Procesado";
	}	//	doIt
}
