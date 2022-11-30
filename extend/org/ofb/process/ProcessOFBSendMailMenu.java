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
 *  @author mfrojas
 *  @version $Id: ProcessOT.java,v 1.2 2008/06/12 00:51:01  $
 */
public class ProcessOFBSendMailMenu extends SvrProcess
{
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	private String P_DocAction;
	private Timestamp p_DateTrx;
	/*OT ID*/
	private int 			Record_ID;
	
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("DocAction"))
				P_DocAction = (String)para[i].getParameter();
			else if (name.equals("DateTrx"))
				p_DateTrx = para[i].getParameterAsTimestamp();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
		
		Record_ID=getRecord_ID();
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		MClient client = new MClient(getCtx(),getAD_Client_ID(),get_TrxName());

		String mailTO = "mfrojas@gmail.com";
		EMail mail = new EMail(client, client.getRequestEMail(), mailTO, "Encabezado ","Correo ");
		mail.createAuthenticator(client.getRequestUser(), client.getRequestUserPW());
	/*	if(rs.getString("EMail_CC") != null && rs.getString("EMail_CC").trim().length() > 0)
		{
			mailCC = rs.getString("EMail_CC");
			if(mailCC != null && mailCC.trim().length()>5)
				mail.addCc(mailCC);
		}*/
		mail.addCc("andrea.ponce@gmail.com");
	/*	if(rs.getString("MailCCField") != null && rs.getString("MailCCField").trim().length() > 0)
		{
			mailCC = DB.getSQLValueString(po.get_TrxName(),"SELECT max(EMail) FROM AD_User WHERE AD_User_ID = "+po.get_ValueAsString(rs.getString("MailCCField")));
			if(mailCC != null && mailCC.trim().length()>5)
				mail.addCc(mailCC);
		}
		if(UsedTO == false)
		{
			if(rs.getString("MailToField") != null && rs.getString("MailToField").trim().length() > 0)
			{
				mailTO =DB.getSQLValueString(po.get_TrxName(),"SELECT max(EMail) FROM AD_User WHERE AD_User_ID = "+po.get_ValueAsString(rs.getString("MailToField")));
				if(mailTO != null && mailTO.trim().length()>5)
					mail.addTo(mailTO);
			}										
		}	*/									
		mail.send();
		
		return "";
		
	}	//	doIt


	
}	//	CopyOrder
