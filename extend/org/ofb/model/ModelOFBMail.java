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
 *****************************************************************************/
package org.ofb.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.EMail;

/**
 *	Validator default OFB
 *
 *  @author Italo Niñoles
 */
public class ModelOFBMail implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelOFBMail ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelOFBMail.class);
	/** Client			*/
	private int		m_AD_Client_ID = -1;
	
	private ArrayList<String> TNames = new ArrayList<String>();	
	private ArrayList<Integer>  TIDs = new ArrayList<Integer>();

	/**
	 *	Initialize Validation
	 *	@param engine validation engine
	 *	@param client client
	 */
	public void initialize (ModelValidationEngine engine, MClient client)
	{
		//client = null for global validator
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		}
		else  {
			log.info("Initializing global validator: "+this.toString());
		}
		//se cargan tablas a monitorear
		String sqlTable = "Select Value,Description from AD_SysConfig where name='OFB_TableMail'";
		try 
		{			
			PreparedStatement pstmtTable = DB.prepareStatement(sqlTable, null);
			ResultSet rsTable = pstmtTable.executeQuery();					
			while(rsTable.next())
			{
				TNames.add(rsTable.getString("Value"));
				TIDs.add(Integer.parseInt(rsTable.getString("Description")));
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		//	Tables to be monitored		
		//engine.addModelChange(MPayment.Table_Name, this);
		for(int a=0; a<TNames.size();a++)
		{
			engine.addModelChange(TNames.get(a), this);
		}		
		//	Documents to be monitored
		//engine.addDocValidate(MRequisition.Table_Name, this);
				

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);		
		
		if (type == TYPE_AFTER_CHANGE && TIDs.contains(po.get_Table_ID()))
		{
			String sqlMail ="";
			PreparedStatement pstmt = null;
			if(po.is_ValueChanged("DocStatus"))
			{
				if(po.get_ValueAsInt("C_Doctype_ID") > 0)// se busca si tiene tipo de documento
				{
					//se buscan correos configurados
					sqlMail = "SELECT * FROM R_MailText WHERE C_DocType_ID = "+po.get_ValueAsInt("C_Doctype_ID");
					try 
					{
						pstmt = DB.prepareStatement(sqlMail, po.get_TrxName());
						ResultSet rs = pstmt.executeQuery();					
						while(rs.next())
						{
							if(po.get_ValueOld("DocStatus").toString().compareTo(rs.getString("DocStatusOld")) ==0
								&& po.get_Value("DocStatus").toString().compareTo(rs.getString("DocStatus")) ==0)
							{
								try {
									
									
									String mailCC = "";
									String mailTO = "";
									boolean UsedTO = false;
									//seteo de correos con campos de texto plano
									if(rs.getString("EMail_To") != null && rs.getString("EMail_To").trim().length() > 0)
										mailTO = rs.getString("EMail_To");
									else
									{
										if(rs.getString("MailToField") != null && rs.getString("MailToField").trim().length() > 0)
										{
											mailTO =DB.getSQLValueString(po.get_TrxName(),"SELECT max(EMail) FROM AD_User WHERE AD_User_ID = "+po.get_ValueAsString(rs.getString("MailToField")));
											UsedTO=true;
										}
									}
									//se reemplazan campos de variables del registro	
									
									//String ln = System.getProperty("line.separator");
									StringBuilder str = new StringBuilder();
									str.append(rs.getString("MailText"));
									//reemplazo de variables en el mensaje
									str = new StringBuilder(str.toString().replace("@DocumentNo@", po.get_ValueAsString("DocumentNo")));
									//
									MClient client = new MClient(po.getCtx(),po.getAD_Client_ID(),po.get_TrxName());
									EMail mail = new EMail(client, client.getRequestEMail(), mailTO, rs.getString("MailHeader"),str.toString());
									mail.createAuthenticator(client.getRequestUser(), client.getRequestUserPW());
									if(rs.getString("EMail_CC") != null && rs.getString("EMail_CC").trim().length() > 0)
									{
										mailCC = rs.getString("EMail_CC");
										if(mailCC != null && mailCC.trim().length()>5)
											mail.addCc(mailCC);
									}
									if(rs.getString("MailCCField") != null && rs.getString("MailCCField").trim().length() > 0)
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
									}										
									mail.send();
								} catch (Exception e) {
									log.config(e.toString());
								}
							}
						}
					}
					catch (Exception e)
					{
						log.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		}		
		
		return null;
	}	//	modelChange

	/**
	 *	Validate Document.
	 *	Called as first step of DocAction.prepareIt
     *	when you called addDocValidate for the table.
     *	Note that totals, etc. may not be correct.
	 *	@param po persistent object
	 *	@param timing see TIMING_ constants
     *	@return error message or null
	 */
	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		
		return null;
	}	//	docValidate

	/**
	 *	User Login.
	 *	Called when preferences are set
	 *	@param AD_Org_ID org
	 *	@param AD_Role_ID role
	 *	@param AD_User_ID user
	 *	@return error message or null
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);

		return null;
	}	//	login


	/**
	 *	Get Client to be monitored
	 *	@return AD_Client_ID client
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID


	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("QSS_Validator");
		return sb.toString ();
	}	//	toString


	

}	