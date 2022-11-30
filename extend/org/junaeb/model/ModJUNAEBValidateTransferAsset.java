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
package org.junaeb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

import org.compiere.model.MAsset;
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.ofb.utils.DateUtils;

/**
 *	Validator for JUNAEB
 *
 *  @author mfrojas
 */
public class ModJUNAEBValidateTransferAsset implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModJUNAEBValidateTransferAsset ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModJUNAEBValidateTransferAsset.class);
	/** Client			*/
	private int		m_AD_Client_ID = -1;
	

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
			log.info("Initializing Model Price Validator: "+this.toString());
		}

		//	Tables to be monitored
		engine.addModelChange(MInvoice.Table_Name, this);		
		engine.addModelChange(MInvoiceLine.Table_Name, this);
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		//Se validara que, para los traspasos, se cumpla cierta cantidad de condiciones. 
		
		if((type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE) && po.get_Table_ID()==MInvoiceLine.Table_ID) 
		{	
			MInvoiceLine invl = (MInvoiceLine)po;
			//Primero se obtiene el tipo de documento. 
			int doctype_id = invl.getC_Invoice().getC_DocTypeTarget_ID();
			//Primero se validara para TRASPASO CAMBIO DE NIVEL (INT)
			if(doctype_id == 1000011)
			{
				int oldprovince = invl.get_ValueAsInt("C_Province_ID");
				log.config("old province "+oldprovince);
				
				int newprovince = invl.get_ValueAsInt("C_NewProvince_ID");
				log.config("new province "+newprovince);
				
				//mfrojas 20180413 Se agrega modificaci�n. en INT, al menos se debe cambiar el servicio o provincia
				
				int oldorg = invl.get_ValueAsInt("AD_Org_ID");
				log.config("old org "+oldorg);
				
				int neworg = invl.get_ValueAsInt("AD_NewOrg_ID");
				log.config("new org "+neworg);
				
				if(oldprovince == newprovince && neworg == oldorg)
					return "Para el tipo INT, al menos debe cambiar la provincia o el servicio";
			}
			//traspaso interno
			else if(doctype_id == 1000010)
			{
				int oldoffice = invl.get_ValueAsInt("S_ResourceRef_ID");
				log.config("old office "+oldoffice);
				
				int newoffice = invl.get_ValueAsInt("S_NewResourceRef_ID");
				log.config("new office "+newoffice);
				
				if(oldoffice == newoffice)
					return "Para el tipo CNN, al menos debe cambiar la oficina";

			}

		}
		
		return null;
	}	//	modelChange

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
		StringBuffer sb = new StringBuffer ("ModelPrice");
		return sb.toString ();
	}	//	toString


	

}	