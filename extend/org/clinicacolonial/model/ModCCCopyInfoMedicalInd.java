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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_CC_MedicalIndications;
import org.compiere.model.X_CC_MedicationPerDay;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
/**
 *	Validator for CC
 *
 *  @author ininoles
 */
public class ModCCCopyInfoMedicalInd implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModCCCopyInfoMedicalInd ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModCCCopyInfoMedicalInd.class);
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
		engine.addModelChange(X_CC_MedicationPerDay.Table_Name, this);
		//engine.addModelChange(X_CC_Epicrisis.Table_Name, this);		
		//engine.addModelChange(X_CC_MedicalIndications.Table_Name, this);
		//engine.addModelChange(X_CC_Evaluation_nursing.Table_Name, this);
		//engine.addModelChange(X_CC_CollectiveIntelligence.Table_Name, this);

		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		if(type == TYPE_AFTER_NEW && po.get_Table_ID()==X_CC_MedicationPerDay.Table_ID) 
		{	
			X_CC_MedicationPerDay eval = (X_CC_MedicationPerDay)po;
			int ID_Eval = DB.getSQLValue(po.get_TrxName(), "SELECT coalesce(MAX(CC_MedicationPerDay_ID),0) FROM CC_MedicationPerDay " +
					" WHERE IsActive = 'Y' AND CC_Hospitalization_ID = "+eval.getCC_Hospitalization_ID()+" AND CC_MedicationPerDay_ID <>"+eval.get_ID());
			if(ID_Eval > 0)
			{
				//se crean medicamentos
				String sql = "SELECT M_Product_ID, Qty, C_UOM_ID, Medicines_Status,DeliveryViaRule,Description, frecuency, medicine " +
						" FROM CC_MedicalIndications WHERE IsActive = 'Y' AND CC_MedicationPerDay_ID = ? ORDER BY CC_MedicalIndications_ID Asc";
				PreparedStatement ps = DB.prepareStatement(sql, po.get_TrxName());
				ps.setInt(1, ID_Eval);
				ResultSet rs = ps.executeQuery();				
				while (rs.next()) 
				{
					X_CC_MedicalIndications ind = new X_CC_MedicalIndications(po.getCtx(), 0, po.get_TrxName());
					//ind.setCC_Evaluation_ID(eval.get_ID());
					//ind.setC_BPartner_ID(eval.getC_BPartner_ID());
					ind.set_CustomColumn("CC_MedicationPerDay_ID", eval.get_ID());
					ind.setM_Product_ID(rs.getInt("M_Product_ID"));
					ind.setQty(rs.getBigDecimal("Qty"));
					ind.setC_UOM_ID(rs.getInt("C_UOM_ID"));
					ind.setMedicines_Status(rs.getString("Medicines_Status"));
					ind.setDeliveryViaRule(rs.getString("DeliveryViaRule"));
					ind.setDescription(rs.getString("Description"));
					//mfrojas
					ind.setIsActive(false);//ininoles se cambia tru por false a peticion de jorge 31-05-2021
					//end mfrojas
					//20191118
					ind.set_CustomColumn("Frecuency", rs.getString("Frecuency"));
					ind.set_CustomColumn("Medicine", rs.getString("Medicine"));
					ind.saveEx(po.get_TrxName());

				}
				rs.close();
				ps.close();				
			}			
			/**
			 * mfrojas 20190121
			 * si id_eval = 0, entonces es la primera evolucion. En este caso se debe traer medicalindications
			 * del ingreso (hospitalization), en caso de existir
			 */
			else
			{
				//se crean medicamentos
				String sql = "SELECT M_Product_ID, Qty, C_UOM_ID, Medicines_Status,DeliveryViaRule,Description, frecuency, medicine " +
						" FROM CC_MedicalIndications WHERE IsActive = 'Y' AND CC_Hospitalization_ID = ?";
				PreparedStatement ps = DB.prepareStatement(sql, po.get_TrxName());
				ps.setInt(1, eval.getCC_Hospitalization_ID());
				ResultSet rs = ps.executeQuery();				
				while (rs.next()) 
				{
					X_CC_MedicalIndications ind = new X_CC_MedicalIndications(po.getCtx(), 0, po.get_TrxName());
					//ind.setCC_Evaluation_ID(eval.get_ID());
					ind.set_CustomColumn("CC_MedicationPerDay_ID", eval.get_ID());
					//ind.setC_BPartner_ID(eval.getC_BPartner_ID());
					ind.setM_Product_ID(rs.getInt("M_Product_ID"));
					ind.setQty(rs.getBigDecimal("Qty"));
					ind.setC_UOM_ID(rs.getInt("C_UOM_ID"));
					ind.setMedicines_Status(rs.getString("Medicines_Status"));
					ind.setDeliveryViaRule(rs.getString("DeliveryViaRule"));
					ind.setDescription(rs.getString("Description"));
					//mfrojas
					ind.setIsActive(false);//ininoles se cambia a paticion de jorge 31-05-2021
					//end mfrojas
					//20191118
					ind.set_CustomColumn("Frecuency", rs.getString("Frecuency"));
					ind.set_CustomColumn("Medicine", rs.getString("Medicine"));
					ind.saveEx(po.get_TrxName());
				}
				rs.close();
				ps.close();				
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