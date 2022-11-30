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
package org.ofb.process;

import java.sql.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.*;

/**
 *	
 *	
 *  @author italo niñoles
 *  @version $Id: ProcessOT.java,v 1.2 2008/06/12 00:51:01  $
 */
public class CompleteInvoiceMassive extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	private int docType_ID =0;
	Timestamp dateAcct = null;
	Timestamp dateAcctTo = null;
	
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_DocType_ID"))
				docType_ID = para[i].getParameterAsInt();
			else if (name.equals("DateAcct"))
			{
				dateAcct = para[i].getParameterAsTimestamp();
				dateAcctTo = para[i].getParameterToAsTimestamp();
			}
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		int cant = 0;
		String mysql = "Select C_Invoice_ID FROM C_invoice "
				+ " WHERE DocStatus IN ('DR','IP') AND Issotrx = 'N'"
				+ " AND dateacct BETWEEN ? AND ? ";
		
		if(docType_ID > 0)
			mysql = mysql+" AND C_DocType_ID ="+docType_ID;
			
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(mysql, get_TrxName());
			pstmt.setTimestamp(1,dateAcct);
			pstmt.setTimestamp(2,dateAcctTo);
			rs = pstmt.executeQuery();
			while(rs.next())
			{	
				MInvoice inv = new MInvoice(getCtx(), rs.getInt("C_Invoice_ID"), get_TrxName());
				//se completa factura
				if(inv.getDocStatus().compareTo("CO")!= 0
						&& inv.getDocStatus().compareTo("CL")!= 0)
				{
					inv.processIt("CO");
					inv.saveEx(get_TrxName());
					cant++;
				}
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, mysql, e);
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}		
		return "Se han completado "+cant+ " documentos";
	}	//	doIt


	
}	//	CopyOrder
