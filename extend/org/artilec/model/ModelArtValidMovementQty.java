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
package org.artilec.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.MProduct;

/**
 *	Validator for Artilec
 *  Valid qty on movement 
 *  @author mfrojas
 */
public class ModelArtValidMovementQty implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModelArtValidMovementQty ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModelArtValidMovementQty.class);
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
		//engine.addModelChange(MInvoice.Table_Name, this);

		engine.addDocValidate(MMovement.Table_Name, this);

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		return null;
	}	//	modelChange

	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		//Must validate if the movement of a specific line, is between warehouses, or
		//the same warehouses. 
		//If the warehouses are different, then we have to look for the "reserved qty", 
		//which consists of minout, issotrx=Y, Draft.
		
		if(timing == TIMING_BEFORE_COMPLETE && po.get_Table_ID()==MMovement.Table_ID)
		{
			MMovement mov = (MMovement)po;
			log.config("estado "+mov.getDocStatus());
			
			//obtener lineas, agrupadas por producto y por almacen.
			
			String sqllines = "SELECT movl.m_product_id as m_product_id, " +
					" ml.m_warehouse_id as m_warehouse_id, mlto.m_warehouse_id as m_warehouseto_id, " +
					" coalesce(sum(movl.movementqty),0) as movementqty" +
					" FROM m_movementline movl" +
					" INNER JOIN m_locator ml on ml.m_locator_id = movl.m_locator_id " +
					" INNER JOIN m_locator mlto on mlto.m_locator_id = movl.m_locatorto_id " +
					" INNER JOIN m_warehouse mw on mw.m_warehouse_id = ml.m_warehouse_id " +
					" INNER JOIN m_warehouse mwto on mwto.m_warehouse_id = mlto.m_warehouse_id " +
					" WHERE movl.m_movement_id = "+mov.get_ID()+" " +
					" GROUP BY movl.m_product_id, ml.m_warehouse_id, mlto.m_warehouse_id ";
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				
				log.config("sqllines "+sqllines);
				pstmt = DB.prepareStatement(sqllines, po.get_TrxName());
				rs = pstmt.executeQuery();
			
				BigDecimal stock = Env.ZERO;
				BigDecimal reserved = Env.ZERO;
				BigDecimal available = Env.ZERO;
			
				while(rs.next())
				{
					stock = Env.ZERO;
					reserved = Env.ZERO;
					//Only if warehouses are different
					log.config(" id w "+rs.getInt("m_warehouse_id"));
					log.config(" id wto "+rs.getInt("m_warehouseto_id"));
					if(rs.getInt("m_warehouse_id") != rs.getInt("m_warehouseto_id"))
					{
						//physical
						String sql = "SELECT coalesce(sum(qtyonhand),0) from m_storage where m_locator_id in " +
								"( select m_locator_id from m_locator where m_warehouse_id = ? ) " +
								" AND m_product_id = ? AND isactive='Y' ";
						stock = DB.getSQLValueBD(po.get_TrxName(), sql, rs.getInt("m_warehouse_id"), rs.getInt("m_product_id"));
					
					
						//reserved
						sql = "SELECT coalesce(sum(movementqty),0) from artilec_stock_reserva_v where m_warehouse_id = ? " +
								" AND m_product_id = ? ";
						reserved = DB.getSQLValueBD(po.get_TrxName(), sql, rs.getInt("m_warehouse_id"), rs.getInt("m_product_id"));
					
						//we have the movementqty, stock qty and reserved qty. 
					
						available = stock.subtract(reserved);
						if(rs.getBigDecimal("movementqty").compareTo(available) > 0)
						{
							//Obtain productname for given product
							MProduct prod = new MProduct(po.getCtx(), rs.getInt("m_product_id"), po.get_TrxName());
							return "La cantidad solicitada para "+prod.getName()+" no es suficiente: Solicitado "+rs.getBigDecimal("movementqty")+" Disponible "+available;
						}
					}
				}
				pstmt.close();
				rs.close();

			}
			catch(Exception e)
			{
				log.config("Exception "+e.toString());
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
		StringBuffer sb = new StringBuffer ("ModelPrice");
		return sb.toString ();
	}	//	toString


	

}	