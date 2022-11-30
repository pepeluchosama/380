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
package org.blumos.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.process.SvrProcess;

/**
 *	Generate XML Consolidated from Invoice (Generic) 
 *	
 *  @author Italo Ni�oles ininoles
 *  @version $Id: FSCreateSeguimiento.java,v 1.2 19/05/2011 $
 */
public class FSCreateSeguimiento extends SvrProcess
{	
	/** Properties						*/
	private Properties 		m_ctx;	
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		log.config(m_ctx.toString());
		String sql = "SELECT * FROM T_SeguimientoXML WHERE I_IsImported <> 'Y'";
		PreparedStatement pstmt = null;
		pstmt = DB.prepareStatement (sql, get_TrxName());
		ResultSet rs = pstmt.executeQuery ();
		int ID_project = 0;
		int ID_User = 0;
		while (rs.next())
		{
			ID_project = 0;
			ID_User = 0;
			//se busca ID de proyecto
			ID_project = DB.getSQLValue(get_TrxName(), "SELECT MAX(C_ProjectOFB_ID) FROM C_ProjectOFB"
					+ " WHERE C_ProjectOFB_ID = "+rs.getInt("C_ProjectOFB_ID"));
			if(ID_project > 0)//existe projecto
			{
				X_C_ProjectOFB pro = new X_C_ProjectOFB(getCtx(), ID_project, get_TrxName());
				//se crea seguimiento
				X_T_SEGUIMIENTO seg = new X_T_SEGUIMIENTO(getCtx(), 0, get_TrxName());
				seg.setAD_Org_ID(pro.getAD_Org_ID());
				seg.setDescription(rs.getString("description")+" - "+rs.getString("UserName"));
				seg.setC_ProjectOFB_ID(pro.get_ID());
				seg.setLLAMADA(true);
				seg.setEMail(true);
				seg.setVISITA(true);
				//se busca susuario
				if(rs.getString("UserName") != null)
				{
					ID_User = DB.getSQLValue(get_TrxName(),"SELECT AD_User_ID FROM AD_User "
						+ " WHERE AD_Client_ID="+pro.getAD_Client_ID()+" AND upper(name) like '"+rs.getString("UserName").toUpperCase()+"'");
					if(ID_User > 0)
						seg.setAD_User_ID(ID_User);
				}
				seg.set_CustomColumn("AD_USER_ID_COMENTARIO", null);
				if(seg.save(get_TrxName()))
					DB.executeUpdate("UPDATE T_SeguimientoXML SET I_IsImported='Y' WHERE T_SeguimientoXML_ID ="+rs.getInt("T_SeguimientoXML_ID"), get_TrxName());
			}
			
		}		
		return "OK";
	}	//	doIt	
}	//	InvoiceCreateInOut
