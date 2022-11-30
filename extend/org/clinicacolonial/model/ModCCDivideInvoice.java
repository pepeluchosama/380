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
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.adempiere.exceptions.AdempiereException;

/**
 *	Validator for HFBC
 *  Cant complete order with paymentrule cash
 *  @author ininoles
 */
public class ModCCDivideInvoice implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModCCDivideInvoice ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModCCDivideInvoice.class);
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

		engine.addDocValidate(MInvoice.Table_Name, this);

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
		if(timing == TIMING_AFTER_PREPARE && po.get_Table_ID()==MInvoice.Table_ID)
		{
			MInvoice inv = (MInvoice)po;
			if(inv.getC_Order_ID() > 0)
			{
				MOrder ord = new MOrder(po.getCtx(), inv.getC_Order_ID(), po.get_TrxName());
				BigDecimal amtSplit = (BigDecimal)ord.get_Value("AmountSplit");
				if(amtSplit == null)
					amtSplit = Env.ZERO;
				if(ord.isSOTrx() && amtSplit.compareTo(Env.ZERO) > 0)
				{					
					//se actualiza factura de paciente
					//se busca cargo generico
					int charge_ID = DB.getSQLValue(po.get_TrxName(), "SELECT C_Charge_ID FROM C_Charge "
							+ "WHERE IsActive='Y' AND description like 'atencion medica'");
					if(charge_ID > 0)
					{
						//se borran lineas de factura creada
						DB.executeUpdate("DELETE FROM C_InvoiceLine WHERE C_Invoice_ID="+inv.get_ID(), po.get_TrxName());
						//se crea nueva linea
						MInvoiceLine iLine = new MInvoiceLine(inv);
						iLine.setC_Charge_ID(charge_ID);
						iLine.setQty(Env.ONE);
						iLine.setPrice(ord.getTotalLines().subtract(amtSplit));
						iLine.setC_Tax_ID(2000002);
						if (!iLine.save(po.get_TrxName()))
							throw new AdempiereException("Could not create Invoice Line");
						
						//se busca BP para isapre
						int ID_BPIs = DB.getSQLValue(po.get_TrxName(), "SELECT ci.C_Bpartner_ID"
								+ " FROM CC_Isapre ci"
								+ " INNER JOIN C_Bpartner bp ON (ci.CC_Isapre_ID = bp.CC_Isapre_ID)"
								+ " AND bp.C_BPartner_ID="+ord.getC_BPartner_ID());
						if(ID_BPIs <=0)
							ID_BPIs=ord.getC_BPartner_ID();
						//se crea factura para isapre
						MDocType dt = MDocType.get(po.getCtx(), ord.getC_DocType_ID());
						MInvoice inv2 = createInvoice(dt, inv.getDateInvoiced(),ord,po,inv,ID_BPIs);
						//se crea linea para factura isapre
						//se crea nueva linea
						MInvoiceLine iLine2 = new MInvoiceLine(inv2);
						iLine2.setC_Charge_ID(charge_ID);
						iLine2.setQty(Env.ONE);
						iLine2.setPrice(amtSplit);
						iLine2.setC_Tax_ID(2000002);
						if (!iLine2.save(po.get_TrxName()))
							throw new AdempiereException("Could not create Invoice Line");
						
						//se actualiza orden para que no se facture nuevamente
						DB.executeUpdate("UPDATE C_OrderLine SET QtyInvoiced=QtyEntered WHERE C_Order_ID="+ord.get_ID(),po.get_TrxName());
					}
				}
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

	private MInvoice createInvoice (MDocType dt,Timestamp invoiceDate, MOrder order,PO po,MInvoice inv, int ID_BPIs)
	{
		log.info(dt.toString());
		MInvoice invoice = new MInvoice (order, dt.getC_DocTypeInvoice_ID(), invoiceDate);
		invoice.setDescription("Generado desde factura "+inv.getDocumentNo()+" y orden "+order.getDocumentNo());
		invoice.setC_BPartner_ID(ID_BPIs);
		if (!invoice.save(po.get_TrxName()))
			throw new AdempiereException("Could not create Invoice");
		return invoice;
	}	//	createInvoice
	

}	