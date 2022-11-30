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
package org.clinicacolonial.process;

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.process.*;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.ofb.utils.DateUtils;

/**
 *  Copy Order Lines
 *
 *	@author Jorg Janke
 *	@version $Id: CopyFromOrder.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class GenInvoiceFomOrder extends SvrProcess
{
	/**	The Order				*/
	private int		p_Order_ID;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		p_Order_ID = getRecord_ID();
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		MOrder order = new MOrder(getCtx(), p_Order_ID, get_TrxName());
		int charge_ID = DB.getSQLValue(get_TrxName(), "SELECT C_Charge_ID FROM C_Charge "
				+ "WHERE IsActive='Y' AND description like 'atencion medica'");
		if(charge_ID > 0)
		{
			BigDecimal amtSplit = (BigDecimal)order.get_Value("AmountSplit");
			MDocType dt = MDocType.get(getCtx(), order.getC_DocType_ID());
			//se genera primera factura a nombre de paciente
			MInvoice invoice = new MInvoice (order, dt.getC_DocTypeInvoice_ID(), DateUtils.today());
			invoice.setDescription("Generado automaticamente. Boleta paciente");
			invoice.setC_BPartner_ID(order.getC_BPartner_ID());
			if (!invoice.save(get_TrxName()))
				throw new AdempiereException("Could not create Invoice");
			//se crea linea
			MInvoiceLine iLine = new MInvoiceLine(invoice);
			iLine.setC_Charge_ID(charge_ID);
			iLine.setQty(Env.ONE);
			iLine.setPrice(order.getTotalLines().subtract(amtSplit));
			iLine.setC_Tax_ID(2000002);
			if (!iLine.save(get_TrxName()))
				throw new AdempiereException("Could not create Invoice Line");
			
			//se crea factura a nombre de la isapre
			int ID_BPIs = DB.getSQLValue(get_TrxName(), "SELECT ci.C_Bpartner_ID"
					+ " FROM CC_Isapre ci"
					+ " INNER JOIN C_Bpartner bp ON (ci.CC_Isapre_ID = bp.CC_Isapre_ID)"
					+ " AND bp.C_BPartner_ID="+order.getC_BPartner_ID());
			if(ID_BPIs <=0)
				ID_BPIs=order.getC_BPartner_ID();
			
			MInvoice invoice2 = new MInvoice (order, dt.getC_DocTypeInvoice_ID(), DateUtils.today());
			invoice2.setDescription("Generado automaticamente. Boleta Isapre");
			invoice2.setC_BPartner_ID(order.getC_BPartner_ID());
			if (!invoice2.save(get_TrxName()))
				throw new AdempiereException("Could not create Invoice");
			//se crea linea
			MInvoiceLine iLine2 = new MInvoiceLine(invoice2);
			iLine2.setC_Charge_ID(charge_ID);
			iLine2.setQty(Env.ONE);
			iLine2.setPrice(amtSplit);
			iLine2.setC_Tax_ID(2000002);
			if (!iLine2.save(get_TrxName()))
				throw new AdempiereException("Could not create Invoice Line");
		}
		return "Generadas";
	}	//	doIt
}	//	CopyFromOrder
