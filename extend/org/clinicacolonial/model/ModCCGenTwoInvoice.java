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
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.adempiere.exceptions.AdempiereException;

/**
 *	Validator for HFBC
 *  Cant complete order with paymentrule cash
 *  @author ininoles
 */
public class ModCCGenTwoInvoice implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModCCGenTwoInvoice ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModCCGenTwoInvoice.class);
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
				if(ord.isSOTrx())
				{
					//se crea segunda factura
					MDocType dt = MDocType.get(po.getCtx(), ord.getC_DocType_ID());
					MInvoice inv2 = createInvoice(dt, inv.getDateInvoiced(),ord,po,inv);
					//se recorren facturas y se actualizan montos
					BigDecimal factor1 = new BigDecimal("50.0");
					BigDecimal factor2 = new BigDecimal("50.0");
					//Factura1
					MInvoiceLine[] lines = inv.getLines(false);
					for (int i = 0; i < lines.length; i++)
					{  
						MInvoiceLine line = lines[i];
						line.setPrice(line.getPriceEntered().multiply(factor1));
						line.setLineNetAmt();
						line.setTaxAmt();
						line.saveEx();
						inv.calculateTaxTotal();
						inv.saveEx();
					}
					//Factura1
					//se revisa monto de la segunda factura
					MInvoiceLine[] lines2 = inv2.getLines(false);
					for (int a = 0; a < lines2.length; a++)
					{
						MInvoiceLine line2 = lines2[a];							
						line2.setPrice(line2.getPriceEntered().multiply(factor2));
						line2.setLineNetAmt();
						line2.setTaxAmt();
						line2.saveEx();
						inv2.calculateTaxTotal();
						inv2.saveEx();
						//ID_ultimaLinea = line.get_ID();
					}
					//se revisa monto de pago creado en la segunda factura y se actualiza de ser necesario
					int ID_Payment2 = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(C_Payment_ID) FROM C_Payment "
							+ " WHERE C_Invoice_ID = "+inv2.get_ID()+ " AND DocStatus IN ('CO','DR','IP','IN')");
					if(ID_Payment2 > 0)
					{
						// se actualiza pago
						MPayment pay2 = new MPayment(po.getCtx(), ID_Payment2, po.get_TrxName());
						pay2.setPayAmt(inv2.getGrandTotal());
						pay2.saveEx(po.get_TrxName());
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

	private MInvoice createInvoice (MDocType dt,Timestamp invoiceDate, MOrder order,PO po,MInvoice inv)
	{
		log.info(dt.toString());
		MInvoice invoice = new MInvoice (order, dt.getC_DocTypeInvoice_ID(), invoiceDate);
		invoice.setDescription("Generado desde factura "+inv.getDocumentNo()+" y orden "+order.getDocumentNo());
		invoice.setC_BPartner_ID(2007319);
		if (!invoice.save(po.get_TrxName()))
			throw new AdempiereException("Could not create Invoice");
		//
		MOrderLine[] oLines = order.getLines();
		for (int i = 0; i < 3; i++)
		{
			MOrderLine oLine = oLines[i];
			//
			MInvoiceLine iLine = new MInvoiceLine(invoice);
			iLine.setOrderLine(oLine);
			//	Qty = Ordered - Invoiced	
			iLine.setQtyInvoiced(oLine.getQtyOrdered().subtract(oLine.getQtyInvoiced()));
			if (oLine.getQtyOrdered().compareTo(oLine.getQtyEntered()) == 0)
				iLine.setQtyEntered(iLine.getQtyInvoiced());
			else
				iLine.setQtyEntered(iLine.getQtyInvoiced().multiply(oLine.getQtyEntered())
					.divide(oLine.getQtyOrdered(), 12, BigDecimal.ROUND_HALF_UP));
			if (!iLine.save(po.get_TrxName()))
				throw new AdempiereException("Could not create Invoice Line from Order Line");
		}
		//	Manually Process Invoice
		//invoice.processIt(DocAction.ACTION_Complete);
		invoice.saveEx(po.get_TrxName());
		//if (!MOrder.DOCSTATUS_Completed.equals(invoice.getDocStatus()))
		//	throw new AdempiereException("@C_Invoice_ID@: " + invoice.getProcessMsg());
		return invoice;
	}	//	createInvoice
	

}	