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
package org.clinicacolonial.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
/**
 *	
 *	
 */
public class GenerateUniqueInvoice extends SvrProcess
{	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	private int p_ID_BPartner ;
	private Timestamp p_DateInvoicedFrom;
	private Timestamp p_DateInvoicedTo;
	
	protected void prepare()
	{	
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			
			if(name.equals("C_BPartner_ID"))
				p_ID_BPartner = para[i].getParameterAsInt();
			else if(name.equals("DateInvoiced"))
			{
				p_DateInvoicedFrom = para[i].getParameterAsTimestamp();
				p_DateInvoicedTo = para[i].getParameterToAsTimestamp();
			}
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
		int cant = 0;
		MInvoice inv = null;
		String msg="No se ha podido generar la factura";
		int charge_ID = DB.getSQLValue(get_TrxName(), "SELECT C_Charge_ID FROM C_Charge "
				+ "WHERE IsActive='Y' AND description like 'atencion medica'");
		
		String sql = "select C_Invoice_ID FROM C_invoice "
				+ " WHERE DocStatus IN ('DR') AND Processed = 'Y'"
				+ " AND C_BPartner_ID = "+p_ID_BPartner
				+ " AND DateInvoiced BETWEEN ? AND ?"
				+ " AND C_Invoice_ID NOT IN"
				+ " (SELECT C_InvoiceRef_ID FROM C_InvoiceLine WHERE C_InvoiceRef_ID IS NOT NULL)";
		
		PreparedStatement pstmt = null;			
		ResultSet rs = null;
		
		try
		{			
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setTimestamp(1, p_DateInvoicedFrom);
			pstmt.setTimestamp(2, p_DateInvoicedTo);
			rs = pstmt.executeQuery();		
			
			while (rs.next())
			{
				MInvoice invChild = new MInvoice(getCtx(), rs.getInt("C_Invoice_ID"), get_TrxName());
				if(inv == null)//se crea factura
				{
					inv = new MInvoice(getCtx(), 0, get_TrxName());
					inv.setAD_Org_ID(invChild.getAD_Org_ID());
					inv.setC_DocTypeTarget_ID(invChild.getC_DocTypeTarget_ID());
					inv.setC_BPartner_ID(invChild.getC_BPartner_ID());
					inv.setC_BPartner_Location_ID(invChild.getC_BPartner_Location_ID());
					inv.setAD_User_ID(invChild.getAD_User_ID());
					inv.setM_PriceList_ID(invChild.getM_PriceList_ID());
					inv.setSalesRep_ID(invChild.getSalesRep_ID());
					inv.setC_PaymentTerm_ID(invChild.getC_PaymentTerm_ID());
					inv.saveEx(get_TrxName());	
				}
				if(inv != null)//se crea linea por cada factura
				{
					MInvoiceLine iLine = new MInvoiceLine(inv);
					iLine.setC_Charge_ID(charge_ID);
					iLine.setC_Tax_ID(2000002);
					iLine.setQty(Env.ONE);
					iLine.setPrice(invChild.getGrandTotal());
					iLine.set_CustomColumn("C_InvoiceRef_ID", invChild.get_ID());
					iLine.saveEx(get_TrxName());
					cant++;
					//se actualiza factura antigua
					DB.executeUpdate("UPDATE C_Invoice SET DocStatus='VO' WHERE C_Invoice_ID="+invChild.get_ID(), get_TrxName());
				}
			}
				
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		finally
		{
			pstmt.close(); rs.close();
			pstmt=null; rs=null;
		}
		if(inv != null)
			msg="Se ha generado la factura "+inv.getDocumentNo()+". Se han agregado "+cant+" lineas";
		return msg;
	}	//	doIt
}	//	

