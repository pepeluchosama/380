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
package org.copesa.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_C_OrderPayCalendar;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
/**
 *	Validator for COPESA
 *
 *  @author Italo Ni�oles
 */
public class ModCOPESAGenOrderPayCal implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModCOPESAGenOrderPayCal ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModCOPESAGenOrderPayCal.class);
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
		engine.addDocValidate(MOrder.Table_Name, this);		
		
	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	OFB Consulting Ltda. By Julio Far�as
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);
		
		return null;
	}	//	modelChange

	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);
		
		if(timing == TIMING_AFTER_COMPLETE && po.get_Table_ID()==MOrder.Table_ID)
		{
			MOrder order = (MOrder)po;
			if(order.isSOTrx())
			{
				
				String sql = "select copesa_fillopc(?, ?, ?)";
				
				try
				{   
					int NPERIODS = 24;
				    PreparedStatement pstmt = DB.prepareStatement(sql, order.get_TrxName());
				    pstmt.setInt(1, order.getC_Order_ID() );
				    pstmt.setInt(2, Env.getAD_User_ID(order.getCtx()) );
				    pstmt.setInt(3, NPERIODS );
				    
				    pstmt.execute();
				    pstmt.close();
				}
				catch(Exception e)
				{
					log.severe(e.getMessage());
					return "No se pudo crear calendario de facturaci�n";
				}
				
/*				String sql = "SELECT * FROM fact_calendar("+order.get_ID()+") WHERE periodo < 25";
				try 
				{
					PreparedStatement ps = DB.prepareStatement(sql, po.get_TrxName());
					ResultSet rs = ps.executeQuery();				
					while (rs.next()) 
					{
						X_C_OrderPayCalendar pCal = new X_C_OrderPayCalendar(po.getCtx(), 0, po.get_TrxName());
						pCal.setC_Order_ID(order.get_ID());
						pCal.setAD_Org_ID(order.getAD_Org_ID());
						pCal.setIsActive(true);
						pCal.setPeriodNo(rs.getInt("periodo"));
						pCal.setDateStart(rs.getTimestamp("ini"));
						pCal.setDateEnd(rs.getTimestamp("fin"));
						pCal.setLineNetAmt(rs.getBigDecimal("neto"));				
						pCal.setLineTotalAmt(rs.getBigDecimal("total"));
						pCal.set_CustomColumn("C_DocType_ID", rs.getInt("doctype"));
						pCal.save();
					}					
				}
				catch (Exception e) 
				{
					log.config(e.toString());
				}
*/				
			}
		}
		if((timing == TIMING_AFTER_VOID || timing == TIMING_AFTER_REACTIVATE) && po.get_Table_ID()==MOrder.Table_ID)
		{	
			MOrder order = (MOrder)po;
			if(order.isSOTrx())
			{
				DB.executeUpdate("DELETE FROM C_OrderPayCalendar WHERE IsInvoiced <> 'Y' AND C_Order_ID = "+order.get_ID(), po.get_TrxName());
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

	public boolean reqApproval(MOrder order)
	{
		if(order.getPaymentRule().compareTo("D") == 0)
		{
			BigDecimal amt = DB.getSQLValueBD(null,"SELECT SUM(LineNetAmt) " +
					" FROM C_OrderLine WHERE C_Charge_ID > 0 AND C_Order_ID = ? ",order.get_ID());
			if(amt.compareTo(Env.ONEHUNDRED) < 0)
				return false;
		}	
		return true;
	}
	

}	