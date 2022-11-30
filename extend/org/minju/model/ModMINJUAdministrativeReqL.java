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
package org.minju.model;

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_HR_AdministrativeRequests;
import org.compiere.model.X_HR_AdministrativeRequestsL;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

/**
 *	Validator for MINJU
 *
 *  @author Italo Ni�oles
 */
public class ModMINJUAdministrativeReqL implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModMINJUAdministrativeReqL ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModMINJUAdministrativeReqL.class);
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
		engine.addModelChange(X_HR_AdministrativeRequestsL.Table_Name, this);		
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if((type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE)&& po.get_Table_ID()==X_HR_AdministrativeRequestsL.Table_ID) 
		{	
			X_HR_AdministrativeRequestsL reqL = (X_HR_AdministrativeRequestsL)po;
			X_HR_AdministrativeRequests req = new X_HR_AdministrativeRequests(po.getCtx(), reqL.getHR_AdministrativeRequests_ID(), po.get_TrxName()) ;
			if(req.getRequestType().compareTo("PPM")==0)
			{
				if(reqL.getDays().compareTo(new BigDecimal("5.0")) > 0)
				{
		        	throw new AdempiereException("Solo se pueden solicitar m�ximo 5 dias.");
				}
			}
			if(req.getRequestType().compareTo("PAT")==0)
			{
				if(reqL.getDays().compareTo(Env.ZERO) <= 0)
				{
					throw new AdempiereException("Debe ingresar minimo 1 dia");
				}
			}
			if(req.getRequestType().compareTo("FAL")==0)
			{
				if(req.get_ValueAsString("RequestType2").compareTo("01") == 0)
				{
					if(reqL.getDays().compareTo(new BigDecimal("7.0")) > 0)
					{
						throw new AdempiereException("Solo se pueden solicitar m�ximo 7 dias.");
					}
				}
				if(req.get_ValueAsString("RequestType2").compareTo("02") == 0)
				{
					if(reqL.getDays().compareTo(new BigDecimal("3.0")) > 0)
					{
						throw new AdempiereException("Solo se pueden solicitar m�ximo 3 dias.");
					}
				}
			}
			if(req.getRequestType().compareTo("SVC")==0)
			{
				if(reqL.getDays().compareTo(Env.ZERO) <= 0)
					throw new AdempiereException("En solicitud de vacaciones, los d�as deben ser mayor a 0");
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