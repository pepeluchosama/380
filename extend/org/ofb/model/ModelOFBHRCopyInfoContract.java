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

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.model.X_HR_Contract;
import org.eevolution.model.X_HR_Employee;


/**
 *	Validator for HR, copy info contract
 *
 *  @author mfrojas
 */
public class ModelOFBHRCopyInfoContract implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelOFBHRCopyInfoContract ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelOFBHRCopyInfoContract.class);
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
			log.info("Initializing global validator: "+this.toString());
		}

		//	Tables to be monitored		
		engine.addModelChange(X_HR_Contract.Table_Name, this);

		//	Documents to be monitored

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */

	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		if ((type == TYPE_AFTER_CHANGE || type == TYPE_AFTER_NEW) && po.get_Table_ID()==X_HR_Contract.Table_ID)
		{
			X_HR_Contract con = (X_HR_Contract)po;
			//mfrojas se revisa si el contract modificado es el ultimo de la persona 
			
			int lastcontract = DB.getSQLValue(po.get_TrxName(), "SELECT max(hr_contract_id) FROM HR_Contract " +
					" where isactive='Y' and c_bpartner_id = "+con.getC_BPartner_ID());
			if(lastcontract == con.get_ID())
			{
				//Se verifica cada campo a copiar. Si tiene datos, se copia en hr_employee
				int employee_ID = DB.getSQLValue(po.get_TrxName(), "SELECT coalesce(max(hr_employee_id),0) from " +
						" hr_employee where c_bpartner_id = "+con.getC_BPartner_ID());
				
				if(employee_ID > 0)
				{
					X_HR_Employee emp = new X_HR_Employee(po.getCtx(),employee_ID, po.get_TrxName());
					if(con.get_Value("HR_Job_ID") != null)
						emp.set_CustomColumn("HR_Job_ID", con.get_Value("HR_Job_ID"));
					if(con.get_Value("HR_JobRef_ID") != null)
						emp.set_CustomColumn("HR_JobRef_ID", con.get_Value("HR_JobRef_ID"));
					if(con.get_Value("Grade") != null)
						emp.set_CustomColumn("Grade", con.get_Value("Grade"));
					if(con.get_Value("GradeRef") != null)
						emp.set_CustomColumn("GradeRef", con.get_Value("GradeRef"));
					if(con.get_Value("Suplencia") != null)
						emp.set_CustomColumn("Suplencia", con.get_Value("Suplencia"));
					if(con.get_Value("AD_OrgRef_ID") != null)
						emp.set_CustomColumn("AD_OrgRef5_ID", con.get_Value("AD_OrgRef_ID"));
					
					emp.save();

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