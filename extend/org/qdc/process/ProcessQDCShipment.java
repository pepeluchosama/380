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
package org.qdc.process;

import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	
 *	
 *  @author mfrojas
 *  @version $Id: ProcessQDCShipment.java $
 */
public class ProcessQDCShipment extends SvrProcess
{
	private String			p_DocAction = null;
	private int				p_Shipment_ID = 0; 
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
			 else if (name.equals("DocAction"))
				 p_DocAction = para[i].getParameterAsString();
			 else
				 log.log(Level.SEVERE, "Unknown Parameter: " + name);
		 }
		 p_Shipment_ID=getRecord_ID();
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		if (p_Shipment_ID > 0)
		{
			MInOut ship = new MInOut(getCtx(), p_Shipment_ID, get_TrxName());
			if(ship.isSOTrx())
			{
				//seteo de nuevo estado
				/*String newStatus = "DR";
				String newAction = "DR";
				if(ship.getDocStatus().compareTo("DR") == 0)
				{
					newStatus = "IP";
					newAction = "PR";
				}
				else if(ship.getDocStatus().compareTo("IP") == 0 
						|| ship.getDocStatus().compareTo("IN")==0)
				{
					newStatus = "CO";
					newAction = "CO";
				}*/
/*				else if(ship.getDocStatus().compareTo("CO") == 0)
				{
					newStatus = "VO";
					newAction = "VO";
				}*/
				//validacion de rol
				int cant = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM AD_Document_Action_Access daa" +
						" INNER JOIN AD_Ref_List rl ON (daa.AD_Ref_List_ID = rl.AD_Ref_List_ID) " +
						" WHERE value = '"+p_DocAction+"' AND AD_Role_ID = "+Env.getAD_Role_ID(getCtx())+
						" AND C_DocType_ID = "+ship.getC_DocType_ID());
				//procesar
				if(cant > 0)
				{
					/*if(newStatus.compareTo("CO")==0 || newStatus.compareTo("VO")==0)
					{
						if(newStatus.compareTo("CO")==0)
						{
							ship.setDocStatus("DR");
							ship.processIt(newStatus);							
						}
						else if(newStatus.compareTo("VO")==0)
							ship.setDocStatus(newStatus);
					}
					else
						ship.setDocStatus(newStatus);		*/
					//se procesa segun accion
					ship.processIt(p_DocAction);
					ship.save();
				}
				//error de permisos
				else
					throw new AdempiereException("Error: Permisos de rol insuficientes");				
			}
		}
	   return "Procesado";
	}	//	doIt
}
