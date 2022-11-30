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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MRequisition;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;

/**
 *	
 *	
 *  @author mfrojas
 *  @version $Id: ProcessMINSALSendMailReplenish.java $
 *  Enviar reabastecimientoL
 */

public class ProcessMINSALSendMailReplenish extends SvrProcess
{
	//private String			p_DocStatus = null;
	private int				p_Requisition_ID = 0; 
	private String				p_Action = "PR";
	private String 			p_Message = "";

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

		String sqlMail ="";
		PreparedStatement pstmt = null;

		//En este caso se trata de un correo configurable pero con parte del texto generado
		//en codigo
 
		sqlMail = "SELECT * FROM R_MailText WHERE Name like '%CORREO REABASTECIMIENTO%'";
		try 
		{
			pstmt = DB.prepareStatement(sqlMail, get_TrxName());
			ResultSet rs = pstmt.executeQuery();					
			while(rs.next())
			{
	
				String mailCC = "";
				String mailTO = "";
				boolean UsedTO = false;
				//seteo de correos con campos de texto plano
				if(rs.getString("EMail_To") != null && rs.getString("EMail_To").trim().length() > 0)
					mailTO = rs.getString("EMail_To");
				else
				{
					return "No hay destinatario";	
				}						
								
				//String ln = System.getProperty("line.separator");
				StringBuilder str = new StringBuilder();
				str.append(rs.getString("MailText"));
				str.append(System.getProperty("line.separator"));
				//Obtener los datos que se solicitan:
				
				String sql = " SELECT level_min, level_max, m_product_id, m_locator_id, name, qtyonhand from rvofb_replenishmail ";
				PreparedStatement pstmt2 = null;
				ResultSet rs2 = null;
				
				pstmt2 = DB.prepareStatement(sql, get_TrxName());
				rs2 = pstmt2.executeQuery();
				while(rs2.next())
				{
					str.append("Nombre producto: "+rs2.getString("name")+". Nivel minimo: "+rs2.getBigDecimal("level_min")+". Stock Actual: "+rs2.getBigDecimal("qtyonhand"));
					//str.append("\n");
					str.append(System.getProperty("line.separator"));

				}

				str.append("Saludos, ");
				
				MClient client = new MClient(getCtx(),getAD_Client_ID(),get_TrxName());
				EMail mail = new EMail(client, client.getRequestEMail(), mailTO, rs.getString("MailHeader"),str.toString());
				mail.createAuthenticator(client.getRequestUser(), client.getRequestUserPW());
				if(rs.getString("EMail_CC") != null && rs.getString("EMail_CC").trim().length() > 0)
				{
					mailCC = rs.getString("EMail_CC");
					if(mailCC != null && mailCC.trim().length()>5)
						mail.addCc(mailCC);
				}
				
						
						
				log.config("Envio correo");
				mail.send();
			}
			
		}
			
			
		catch (Exception e)
		{
				log.log(Level.SEVERE, e.getMessage(), e);
		}
		

	   return "Procesado";
	}	//	doIt
}
