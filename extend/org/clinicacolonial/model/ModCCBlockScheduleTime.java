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
package org.clinicacolonial.model;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_MED_Appointment;
import org.compiere.model.X_MED_ScheduleTime;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import java.sql.PreparedStatement;

/**
 *	Validator for CC
 *
 *  @author ianinoles
 */
public class ModCCBlockScheduleTime implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModCCBlockScheduleTime ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModCCBlockScheduleTime.class);
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
		engine.addModelChange(X_MED_Appointment.Table_Name, this);
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if((type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE )&& po.get_Table_ID()==X_MED_Appointment.Table_ID) 
		{	
			X_MED_Appointment appo = (X_MED_Appointment)po;
			if (appo.get_ValueAsInt("MED_ScheduleTime_ID") > 0)
			{
				X_MED_ScheduleTime sTime = new X_MED_ScheduleTime(po.getCtx(), appo.get_ValueAsInt("MED_ScheduleTime_ID"), po.get_TrxName());
				if(sTime.getTimeRequested() != null)
				{
					//se buscan horas en el mismo horario
					String stringUpdate = "UPDATE MED_ScheduleTime SET State='BL' WHERE MED_ScheduleTime_ID IN "
							+ " (SELECT MED_ScheduleTime_ID FROM MED_ScheduleTime st"
							+ " INNER JOIN MED_ScheduleDay sd ON (st.MED_ScheduleDay_ID = sd.MED_ScheduleDay_ID)"
							+ " INNER JOIN MED_Schedule s ON (s.MED_Schedule_ID = sd.MED_Schedule_ID) "
							+ " WHERE s.C_BPartnerMed_ID="+appo.get_ValueAsInt("C_BPartnerMed_ID")+" AND st.MED_ScheduleTime_ID <> "+appo.get_ValueAsInt("MED_ScheduleTime") 
							+ " AND timerequested = ?)";
					
					PreparedStatement pstmt = DB.prepareStatement(stringUpdate, po.get_TrxName());
					pstmt.setTimestamp(1, sTime.getTimeRequested());
					pstmt.executeUpdate();
				}
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