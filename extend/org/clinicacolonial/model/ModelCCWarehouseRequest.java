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

import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MWarehouse;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MPPProductBOM;
import org.eevolution.model.MPPProductBOMLine;

/**
 *	Validator default OFB
 *
 *  @author Italo Niñoles
 */
public class ModelCCWarehouseRequest implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelCCWarehouseRequest ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelCCWarehouseRequest.class);
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
		engine.addModelChange(MRequisition.Table_Name, this);
		engine.addModelChange(MRequisitionLine.Table_Name, this);
		//	Documents to be monitored
		engine.addDocValidate(MRequisition.Table_Name, this);

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public static final String DOCSTATUS_Drafted = "DR";
	public static final String DOCSTATUS_Completed = "CO";
	public static final String DOCSTATUS_InProgress = "IP";
	public static final String DOCSTATUS_Voided = "VO";
	
	
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		if((type == TYPE_BEFORE_CHANGE || type == TYPE_BEFORE_NEW )&& po.get_Table_ID()==MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			//se trata de setear la bodega desde la cabecera
			MDocType docType = new MDocType(po.getCtx(), req.getC_DocType_ID(), po.get_TrxName());
			if(docType.get_ValueAsInt("M_Warehouse_ID") > 0)
			{
				if(docType.get_ValueAsInt("M_Warehouse_ID") != req.getM_Warehouse_ID())
					req.setM_Warehouse_ID(docType.get_ValueAsInt("M_Warehouse_ID"));
			}
		}		
		if((type == TYPE_BEFORE_CHANGE || type == TYPE_BEFORE_NEW )&& po.get_Table_ID()==MRequisitionLine.Table_ID)
		{
			MRequisitionLine rLine = (MRequisitionLine) po;
			//si producto tiene BOM se agrega descripcion
			if(rLine.getM_Product_ID() > 0)
			{
				//se busca bom 
				int ID_BOM = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(PP_Product_BOM_ID) FROM PP_Product_BOM "
						+" WHERE IsActive = 'Y' AND M_product_ID = "+rLine.getM_Product_ID());
				if(ID_BOM > 0)//tiene bom
				{
					if(rLine.getDescription()!= null && rLine.getDescription().trim().length()>0)
						rLine.setDescription(rLine.getDescription()+"*LDM*");
					else
						rLine.setDescription("*LDM*");
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
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MRequisition.Table_ID)
		{
			MRequisition req = (MRequisition) po;
			//se valida por tipo de documento CC
			if(req.getC_DocType_ID() == 2000176 
					|| req.getC_DocType_ID() == 2000149
					|| req.getC_DocType_ID() == 2000180
					|| req.getC_DocType_ID() == 2000140
					|| req.getC_DocType_ID() == 2000174
					|| req.getC_DocType_ID() == 2000175)//ininoles se agrega nuevo tipo de documento 30/08/2022
			{
				MRequisitionLine[] lines = req.getLines();
				MInventory inv = null;
				MWarehouse wh = MWarehouse.get(req.getCtx(), req.getM_Warehouse_ID(), req.get_TrxName());
				for (int i = 0; i < lines.length; i++)
				{
					MRequisitionLine line = lines[i];
					if (line.getM_Product_ID() >0)
					{
						if(inv == null)
						{  
							inv = new MInventory(req.getCtx(),0,req.get_TrxName());
							inv.setAD_Org_ID(req.getAD_Org_ID());
							inv.setM_Warehouse_ID(req.getM_Warehouse_ID());	
							inv.setC_DocType_ID(2000115);
							inv.setDescription("Generado automaticamente desde solicitud "+req.getDocumentNo());
							inv.set_CustomColumn("C_BPartner_ID", req.get_ValueAsInt("C_BPartner_ID"));
							//se guardan campos nuevos
							if(req.get_ValueAsInt("C_BPartner_ID") > 0)
							{
								MBPartner part = new MBPartner(po.getCtx(),req.get_ValueAsInt("C_BPartner_ID"),po.get_TrxName());
								inv.set_CustomColumn("DocumentNoBP", part.get_ValueAsString("DocumentNo"));
								//se setea nuevo campo
								String valueLoc = DB.getSQLValueString(po.get_TrxName(), "select (select max(ml.value)"
										+ " from a_locator_use alu"
										+ " left join m_locator ml on alu.m_locator_id=ml.m_locator_id"
										+ " where mr.datedoc between alu.datestart and"
										+ " coalesce(alu.dateend,current_timestamp+'1 day'::interval) and"
										+ " alu.c_bpartner_id = mr.c_bpartner_id) from M_Requisition mr"
										+ " where M_Requisition_ID=?", req.get_ID());
								if(valueLoc != null && valueLoc.trim().length()>0)
									inv.set_CustomColumn("ValueLoc",valueLoc);
							}
							inv.save();
							try{
								inv.set_CustomColumn("M_Requisition_ID", req.get_ID());								
								inv.set_CustomColumn("C_DocTypeRef_ID", req.getC_DocType_ID());
								inv.save();
							}
							catch (Exception e)
							{
								log.log(Level.SEVERE,"No se pudo asignar la variable M_Requisition_ID en M_Inventory",e);							
							}
							//end ininoles							
							//inv.set_CustomColumn("AD_User_ID", getAD_User_ID());						

						}
						//MInventoryLine il = new MInventoryLine(req.getCtx(),0,req.get_TrxName());
						MInventoryLine il = null;
						//primero se pregunta si tiene BOM
						if(line.getM_Product_ID() > 0)
						{
							//se busca bom 
							int ID_BOM = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(PP_Product_BOM_ID) FROM PP_Product_BOM "
									+" WHERE IsActive = 'Y' AND M_product_ID = "+line.getM_Product_ID());
							if(ID_BOM > 0)//tiene bom
							{
								MPPProductBOM bom = new MPPProductBOM(po.getCtx(), ID_BOM, po.get_TrxName());
								if(bom != null)
								{	
									MPPProductBOMLine[] bomlines = bom.getLines();
									for (int j = 0; j < bomlines.length; j++)
									{
										MPPProductBOMLine bomline = bomlines[j];
										il = new MInventoryLine(req.getCtx(),0,req.get_TrxName());
										il.setM_Inventory_ID(inv.getM_Inventory_ID() );
										il.setAD_Org_ID(line.getAD_Org_ID());
										il.setM_Product_ID(bomline.getM_Product_ID());
										//ininoles se setea nuevo campo de kit 16062022
										il.set_CustomColumn("M_ProductRef_ID",line.getM_Product_ID());
										il.setM_Locator_ID(wh.getDefaultLocator().getM_Locator_ID());
										if(req.getC_DocType_ID() == 2000174)// si es devolucion se genera en negativo
											il.setQtyInternalUse(line.getQty().multiply(bomline.getQtyBOM()).negate());
										else
											il.setQtyInternalUse(line.getQty().multiply(bomline.getQtyBOM()));
										int ID_Charge = -1;
										try{
											ID_Charge = req.get_ValueAsInt("C_Charge_ID");
										}
										catch (Exception e)
										{
											log.log(Level.SEVERE,"No se pudo asignar la variable M_Inventory_ID en M_Requisition",e);
											ID_Charge = DB.getSQLValue(po.get_TrxName(),"SELECT MAX(C_Charge_ID) " +
													" FROM C_Charge WHERE IsActive = 'Y' AND AD_Client_ID = "+Env.getAD_Client_ID(po.getCtx())+
													" AND upper(name) like '%CONSUMO%'");
										}	
										if(ID_Charge <= 0)
										{
											ID_Charge = DB.getSQLValue(po.get_TrxName(),"SELECT MAX(C_Charge_ID) " +
													" FROM C_Charge WHERE IsActive = 'Y' AND upper(name) like '%CONSUMO%'"+ 
													" AND AD_Client_ID = "+Env.getAD_Client_ID(po.getCtx()));
										}
										if(ID_Charge > 0)
											il.setC_Charge_ID(ID_Charge);
										else
											il.setC_Charge_ID(2000009);
										//
										il.setDescription(line.getDescription());
										il.saveEx(po.get_TrxName());
									}
								}
							}
							else//no tiene bom								
							{	
								il = new MInventoryLine(req.getCtx(),0,req.get_TrxName());
								il.setM_Inventory_ID(inv.getM_Inventory_ID() );
								il.setAD_Org_ID(line.getAD_Org_ID());
								il.setM_Product_ID(line.getM_Product_ID());
								il.setM_Locator_ID(wh.getDefaultLocator().getM_Locator_ID());
								if(req.getC_DocType_ID() == 2000174)
									il.setQtyInternalUse(line.getQty().negate());
								else
									il.setQtyInternalUse(line.getQty());
								int ID_Charge = -1;
								try{
									ID_Charge = req.get_ValueAsInt("C_Charge_ID");
								}
								catch (Exception e)
								{
									log.log(Level.SEVERE,"No se pudo asignar la variable M_Inventory_ID en M_Requisition",e);
									ID_Charge = DB.getSQLValue(po.get_TrxName(),"SELECT MAX(C_Charge_ID) " +
											" FROM C_Charge WHERE IsActive = 'Y' AND AD_Client_ID = "+Env.getAD_Client_ID(po.getCtx())+
											" AND upper(name) like '%CONSUMO%'");
								}	
								if(ID_Charge <= 0)
								{
									ID_Charge = DB.getSQLValue(po.get_TrxName(),"SELECT MAX(C_Charge_ID) " +
											" FROM C_Charge WHERE IsActive = 'Y' AND upper(name) like '%CONSUMO%'"+ 
											" AND AD_Client_ID = "+Env.getAD_Client_ID(po.getCtx()));
								}
								if(ID_Charge > 0)
									il.setC_Charge_ID(ID_Charge);
								else
									il.setC_Charge_ID(2000009);
								il.setDescription(line.getDescription());
								il.saveEx(po.get_TrxName());
							}
						}						
						//il.save();						
					}
				}				
				/*if(inv!=null)
				{
					try{
						req.set_CustomColumn("M_Inventory_ID", inv.getM_Inventory_ID());
					}
					catch (Exception e)
					{
						log.log(Level.SEVERE,"No se pudo asignar la variable M_Inventory_ID en M_Requisition",e);							
					}					
				}*/
			}
		}
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