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

import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
/**
 *	
 *	
 */

public class UpdateRequisitionSig1 extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	 protected void prepare()
	{	
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		//se ejecuta update		
		/*DB.executeUpdate("UPDATE M_Requisition SET Signature1='Y'"
				+ " WHERE M_Requisition_ID IN ("
				+ " select M_Requisition_ID from M_Inventory"
				+ " WHERE M_Requisition_ID > 0 AND DocStatus IN ('CO')"
				+ " AND Updated < (now()-'2 day'::interval))", get_TrxName());*/
		//ininoles nuevo update con campos nuevos 
		DB.executeUpdate("UPDATE M_Requisition SET Signature1='Y',UpdatedBySignature1=2097628,UpdatedSignature1=now()"
				+ " WHERE M_Requisition_ID IN ("
				+ " select M_Requisition_ID from M_Inventory"
				+ " WHERE M_Requisition_ID > 0 AND DocStatus IN ('CO')"
				+ " AND Updated < (now()-'2 day'::interval))", get_TrxName());
		
	   return "Procesado";
	}	//	doIt
}
