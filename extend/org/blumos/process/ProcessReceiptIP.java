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
package org.blumos.process;

import org.compiere.model.MInOut;
import org.compiere.process.SvrProcess;
/**
 *  @author Italo Ni�oles
 */
public class ProcessReceiptIP extends SvrProcess
{	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	private int		p_InOut_ID;
	
	protected void prepare()
	{	
		p_InOut_ID = getRecord_ID();
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message 
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{	
		MInOut ship = new MInOut(getCtx(), p_InOut_ID, get_TrxName());
		//ship.processIt("IP");
		ship.setDocStatus("IP");
		ship.saveEx(get_TrxName());
		
		return "Procesado";
	}	//	doIt
}	