/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.compiere.grid;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.model.MUOMConversion;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.ofb.model.OFBForward;

/**
 *  Create Invoice Transactions from PO Orders or Receipt
 *
 *  @author Jorg Janke
 *  @version  $Id: VCreateFromInvoice.java,v 1.4 2006/07/30 00:51:28 jjanke Exp $
 *
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 * 			<li>BF [ 1896947 ] Generate invoice from Order error
 * 			<li>BF [ 2007837 ] VCreateFrom.save() should run in trx
 */


public class CreateFromInvoice extends CreateFrom
{
	/**
	 *  Protected Constructor
	 *  @param mTab MTab
	 */
	String IsSOTrx; //OFB 
	int wType=0;//OFB 0 normal - 1 fat - 2 ptk
	boolean getPay=false;//OFB 
	boolean extinguir=false;//OFB 
	
	public CreateFromInvoice(GridTab mTab)
	{
		super(mTab);
		IsSOTrx=mTab.get_ValueAsString("IsSOTrx");//OFB
		log.info(mTab.toString());
	}   //  VCreateFromInvoice

	/**
	 *  Dynamic Init
	 *  @return true if initialized
	 */
	public boolean dynInit() throws Exception
	{
		log.config("");
		setTitle(Msg.getElement(Env.getCtx(), "C_Invoice_ID", false) + " .. " + Msg.translate(Env.getCtx(), "CreateFrom"));

		return true;
	}   //  dynInit

	/**
	 * Load PBartner dependent Order/Invoice/Shipment Field.
	 * @param C_BPartner_ID
	 */
	protected ArrayList<KeyNamePair> loadShipmentData (int C_BPartner_ID)
	{
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();

		//	Display
		StringBuffer display = new StringBuffer("s.DocumentNo||' - '||")
			.append(DB.TO_CHAR("s.MovementDate", DisplayType.Date, Env.getAD_Language(Env.getCtx())));
		//se sobreescribe variable display con nuevos campos
		if(OFBForward.UseMinjuDisplayFromInvoice())
		{
			display = new StringBuffer("s.DocumentNo||' - '||")
			.append(DB.TO_CHAR("s.MovementDate", DisplayType.Date, Env.getAD_Language(Env.getCtx())))
			.append("||' - '||")
			.append("(SELECT DocumentNo FROM C_Order WHERE C_Order_ID = s.C_Order_ID) ");
		}
		/**	ininoles se reemplaza sql original por uno nuevo desde 360
		/**
		StringBuffer sql = new StringBuffer("SELECT s.M_InOut_ID,").append(display)
			.append(" FROM M_InOut s "
			+ "WHERE s.C_BPartner_ID=? AND s.IsSOTrx='N' AND s.DocStatus IN ('CL','CO')"
			+ " AND s.M_InOut_ID IN "
				+ "(SELECT sl.M_InOut_ID FROM M_InOutLine sl"
				+ " LEFT OUTER JOIN M_MatchInv mi ON (sl.M_InOutLine_ID=mi.M_InOutLine_ID) "
				+ " JOIN M_InOut s2 ON (sl.M_InOut_ID=s2.M_InOut_ID) "
				+ " WHERE s2.C_BPartner_ID=? AND s2.IsSOTrx='N' AND s2.DocStatus IN ('CL','CO') "
				+ "GROUP BY sl.M_InOut_ID,mi.M_InOutLine_ID,sl.MovementQty "
				+ "HAVING (sl.MovementQty<>SUM(mi.Qty) AND mi.M_InOutLine_ID IS NOT NULL)"
				+ " OR mi.M_InOutLine_ID IS NULL) "
			+ "ORDER BY s.MovementDate");
			*/

		StringBuffer sql = new StringBuffer("SELECT s.M_InOut_ID,").append(display)
		.append(" FROM M_InOut s "
		+ "inner join C_DocType doc on (s.c_doctype_id=doc.c_doctype_id) " 
		+ "WHERE s.C_BPartner_ID=? AND (s.IsSOTrx='" + IsSOTrx + "' or doc.name like 'AD%') AND s.DocStatus IN ('CL','CO')"
		//ininoles se agrega filtro pot org para que mejore rendimiento
		+ " AND s.AD_Org_ID="+ getAD_Org_ID()
		//ininoles end
		+ "	AND Exists (select il.m_inoutline_id,sum(il.movementqty),sum(invl.qtyinvoiced) from m_inoutline il"
		+ " Left outer join c_invoiceline invl on (il.m_inoutline_id=invl.m_inoutline_id)"
		+ " where il.M_InOut_ID=s.M_InOut_ID group by il.m_inoutline_id"
		+ " having sum(il.movementqty)> coalesce(sum(invl.qtyinvoiced),0) )");
		//ininoles se agrega validacion "Solo despachos de hace 2 mese" 
		if(OFBForward.createFromInvSinceTwoMonth())
			sql = sql.append(" and s.MovementDate >= now() - interval '2 month' ");
		sql = sql.append(" ORDER BY s.MovementDate");
		
		
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, C_BPartner_ID);
			//pstmt.setInt(2, C_BPartner_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}

		return list;
	}

	/**
	 *  Load PBartner dependent Order/Invoice/Shipment Field.
	 *  @param C_BPartner_ID BPartner
	 */
	protected ArrayList<KeyNamePair> loadRMAData(int C_BPartner_ID) {
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();

		String sqlStmt = "SELECT r.M_RMA_ID, r.DocumentNo || '-' || r.Amt from M_RMA r "
				+ "WHERE ISSOTRX='N' AND r.DocStatus in ('CO', 'CL') "
				+ "AND r.C_BPartner_ID=? "
				+ "AND NOT EXISTS (SELECT * FROM C_Invoice inv "
				+ "WHERE inv.M_RMA_ID=r.M_RMA_ID AND inv.DocStatus IN ('CO', 'CL'))";

		PreparedStatement pstmt = null;
		try {
			pstmt = DB.prepareStatement(sqlStmt, null);
			pstmt.setInt(1, C_BPartner_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}
			rs.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlStmt.toString(), e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception ex) {
					log.severe("Could not close prepared statement");
				}
			}
		}

		return list;
	}

	/**
	 *  Load Data - Shipment not invoiced
	 *  @param M_InOut_ID InOut
	 */
	protected Vector<Vector<Object>> getShipmentData(int M_InOut_ID)
	{
		log.config("M_InOut_ID=" + M_InOut_ID);
		MInOut inout = new MInOut(Env.getCtx(), M_InOut_ID, null);
		p_order = null;
		if (inout.getC_Order_ID() != 0)
			p_order = new MOrder (Env.getCtx(), inout.getC_Order_ID(), null);

		m_rma = null;
		if (inout.getM_RMA_ID() != 0)
			m_rma = new MRMA (Env.getCtx(), inout.getM_RMA_ID(), null);

		//
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuffer sql = new StringBuffer("SELECT "	//	QtyEntered
			//+ "l.MovementQty-SUM(NVL(mi.Qty, 0)), l.QtyEntered/l.MovementQty," //OFB codigo original comentado
			+ " l.MovementQty-SUM(NVL(invl.QtyInvoiced, 0)), l.QtyEntered/l.MovementQty," //OFB nuevas cantidades
			+ " l.C_UOM_ID, COALESCE(uom.UOMSymbol, uom.Name),"			//  3..4
			+ " l.M_Product_ID, p.Name, po.VendorProductNo, l.M_InOutLine_ID, l.Line,"        //  5..9
			+ " l.C_OrderLine_ID " //  10
			+ " FROM M_InOutLine l "
			);
		if (Env.isBaseLanguage(Env.getCtx(), "C_UOM"))
			sql.append(" LEFT OUTER JOIN C_UOM uom ON (l.C_UOM_ID=uom.C_UOM_ID)");
		else
			sql.append(" LEFT OUTER JOIN C_UOM_Trl uom ON (l.C_UOM_ID=uom.C_UOM_ID AND uom.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("')");

		sql.append(" LEFT OUTER JOIN M_Product p ON (l.M_Product_ID=p.M_Product_ID)")
			.append(" INNER JOIN M_InOut io ON (l.M_InOut_ID=io.M_InOut_ID)")
			.append(" LEFT OUTER JOIN M_Product_PO po ON (l.M_Product_ID = po.M_Product_ID AND io.C_BPartner_ID = po.C_BPartner_ID)")
			.append(" LEFT OUTER JOIN M_MatchInv mi ON (l.M_InOutLine_ID=mi.M_InOutLine_ID)")
			.append(" LEFT OUTER JOIN C_InvoiceLine invl ON (invl.M_InOutLine_ID=l.M_InOutLine_ID)") // OFB para comparar con la cantidades facturadas ya

			.append(" WHERE l.M_InOut_ID=? AND l.MovementQty<>0 ")
			.append("GROUP BY l.MovementQty, l.QtyEntered/l.MovementQty, "
				+ "l.C_UOM_ID, COALESCE(uom.UOMSymbol, uom.Name), "
				+ "l.M_Product_ID, p.Name, po.VendorProductNo, l.M_InOutLine_ID, l.Line, l.C_OrderLine_ID ")
			.append("ORDER BY l.Line");

		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, M_InOut_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>(7);
				line.add(new Boolean(false));           //  0-Selection
				BigDecimal qtyMovement = rs.getBigDecimal(1);
				BigDecimal multiplier = rs.getBigDecimal(2);
				BigDecimal qtyEntered = qtyMovement.multiply(multiplier);
				line.add(qtyEntered);  //  1-Qty
				KeyNamePair pp = new KeyNamePair(rs.getInt(3), rs.getString(4).trim());
				//mfrojas 20220316
				//Dependiendo del ofbforward, UOM puede cambiar por codigo nombre
				if(!OFBForward.ArtilecCreateShipment())
					line.add(pp);                           //  2-UOM
				else
				{
					String sqlcode = "SELECT '   ' || value || ' - ' || coalesce(UPC,name) from m_product where m_product_id = "+rs.getInt(5);
					String namecode = DB.getSQLValueString(null, sqlcode);
					line.add(namecode);
				}

				pp = new KeyNamePair(rs.getInt(5), rs.getString(6));
				//mfrojas 20220316
				//Dependiendo del ofbforward,va el producto o  precio
				if(!OFBForward.ArtilecCreateShipment())
					line.add(pp);                           //  3-Product
				else
				{
					String sqlprice = "SELECT round(priceentered,2) from c_orderline where c_orderline_id = "+rs.getInt(10);
					BigDecimal price = DB.getSQLValueBD(null, sqlprice);
					line.add(price);
				}
				//mfrojas 20220316
				//Dependiendo del ofbforward,va vendorproductno o  producto
				if(!OFBForward.ArtilecCreateShipment())
					line.add(rs.getString(7));				// 4-VendorProductNo
				else
				{
					String sqlname = "SELECT coalesce(name,'-') from m_product where m_product_id = "+rs.getInt(5);
					String name = DB.getSQLValueString(null, sqlname);
					line.add(name);
				}
					
				int C_OrderLine_ID = rs.getInt(10);
				if (rs.wasNull())
					line.add(null);                     //  5-Order
				else
					line.add(new KeyNamePair(C_OrderLine_ID,"."));
				pp = new KeyNamePair(rs.getInt(8), rs.getString(9));
				line.add(pp);                           //  6-Ship
				line.add(null);                     	//  7-RMA
				data.add(line);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}

		return data;
	}   //  loadShipment

	/**
	 * Load RMA details
	 * @param M_RMA_ID RMA
	 */
	protected Vector<Vector<Object>> getRMAData(int M_RMA_ID)
	{
	    p_order = null;

//	    MRMA m_rma = new MRMA(Env.getCtx(), M_RMA_ID, null);

	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    StringBuffer sqlStmt = new StringBuffer();
	    sqlStmt.append("SELECT rl.M_RMALine_ID, rl.line, rl.Qty - COALESCE(rl.QtyInvoiced, 0), iol.M_Product_ID, p.Name, uom.C_UOM_ID, COALESCE(uom.UOMSymbol,uom.Name) ");
	    sqlStmt.append("FROM M_RMALine rl INNER JOIN M_InOutLine iol ON rl.M_InOutLine_ID=iol.M_InOutLine_ID ");

	    if (Env.isBaseLanguage(Env.getCtx(), "C_UOM"))
        {
	        sqlStmt.append("LEFT OUTER JOIN C_UOM uom ON (uom.C_UOM_ID=iol.C_UOM_ID) ");
        }
	    else
        {
	        sqlStmt.append("LEFT OUTER JOIN C_UOM_Trl uom ON (uom.C_UOM_ID=iol.C_UOM_ID AND uom.AD_Language='");
	        sqlStmt.append(Env.getAD_Language(Env.getCtx())).append("') ");
        }
	    sqlStmt.append("LEFT OUTER JOIN M_Product p ON p.M_Product_ID=iol.M_Product_ID ");
	    sqlStmt.append("WHERE rl.M_RMA_ID=? ");
	    sqlStmt.append("AND rl.M_INOUTLINE_ID IS NOT NULL");

	    sqlStmt.append(" UNION ");

	    sqlStmt.append("SELECT rl.M_RMALine_ID, rl.line, rl.Qty - rl.QtyDelivered, 0, c.Name, uom.C_UOM_ID, COALESCE(uom.UOMSymbol,uom.Name) ");
	    sqlStmt.append("FROM M_RMALine rl INNER JOIN C_Charge c ON c.C_Charge_ID = rl.C_Charge_ID ");
	    if (Env.isBaseLanguage(Env.getCtx(), "C_UOM"))
        {
	        sqlStmt.append("LEFT OUTER JOIN C_UOM uom ON (uom.C_UOM_ID=100) ");
        }
	    else
        {
	        sqlStmt.append("LEFT OUTER JOIN C_UOM_Trl uom ON (uom.C_UOM_ID=100 AND uom.AD_Language='");
	        sqlStmt.append(Env.getAD_Language(Env.getCtx())).append("') ");
        }
	    sqlStmt.append("WHERE rl.M_RMA_ID=? ");
	    sqlStmt.append("AND rl.C_Charge_ID IS NOT NULL");

	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    try
	    {
	        pstmt = DB.prepareStatement(sqlStmt.toString(), null);
	        pstmt.setInt(1, M_RMA_ID);
	        pstmt.setInt(2, M_RMA_ID);
	        rs = pstmt.executeQuery();

	        while (rs.next())
            {
	            Vector<Object> line = new Vector<Object>(7);
	            line.add(new Boolean(false));   // 0-Selection
	            line.add(rs.getBigDecimal(3));  // 1-Qty
	            KeyNamePair pp = new KeyNamePair(rs.getInt(6), rs.getString(7));
	            line.add(pp); // 2-UOM
	            pp = new KeyNamePair(rs.getInt(4), rs.getString(5));
	            line.add(pp); // 3-Product
	            line.add(null); //4-Vendor Product No
	            line.add(null); //5-Order
	            pp = new KeyNamePair(rs.getInt(1), rs.getString(2));
	            line.add(null);   //6-Ship
	            line.add(pp);   //7-RMA
	            data.add(line);
            }
	        rs.close();
	    }
	    catch (Exception ex)
	    {
	        log.log(Level.SEVERE, sqlStmt.toString(), ex);
	    }
	    finally
	    {
	    	DB.close(rs, pstmt);
	    	rs = null; pstmt = null;
	    }

	    return data;
	}

	/**
	 *  List number of rows selected
	 */
	public void info()
	{

	}   //  infoInvoice

	protected void configureMiniTable (IMiniTable miniTable)
	{
		miniTable.setColumnClass(0, Boolean.class, false);      //  0-Selection
		miniTable.setColumnClass(1, BigDecimal.class, true);        //  1-Qty
		miniTable.setColumnClass(2, String.class, true);        //  2-UOM
		miniTable.setColumnClass(3, String.class, true);        //  3-Product
		miniTable.setColumnClass(4, String.class, true);        //  4-VendorProductNo
		miniTable.setColumnClass(5, String.class, true);        //  5-Order
		miniTable.setColumnClass(6, String.class, true);        //  6-Ship
		miniTable.setColumnClass(7, String.class, true);        //  7-Invoice
		//  Table UI
		miniTable.autoSize();
	}

	/**
	 *  Save - Create Invoice Lines
	 *  @return true if saved
	 */
	public boolean save(IMiniTable miniTable, String trxName)
	{
		if(wType!=0)//faaguilar OFB
			return saveOFB(miniTable, trxName);
		//  Invoice
		int C_Invoice_ID = ((Integer)getGridTab().getValue("C_Invoice_ID")).intValue();
		MInvoice invoice = new MInvoice (Env.getCtx(), C_Invoice_ID, trxName);
		log.config(invoice.toString());

		if (p_order != null)
		{
			invoice.setOrder(p_order);	//	overwrite header values
			//ininoles se setea vendedor
			if(OFBForward.createFromInvCopySalesRep())
			{
				if(p_order != null && p_order.getSalesRep_ID() > 0)
					invoice.setSalesRep_ID(p_order.getSalesRep_ID());
				//invoice.setC_BPartner_Location_ID(p_order.getC_BPartner_Location_ID());
				invoice.setC_BPartner_Location_ID(p_order.getBill_Location_ID());
			}
			//ininoles se copia org desde orden
			if(OFBForward.CopyOrgFromOrderInv())
			{
				invoice.setAD_Org_ID(p_order.getAD_Org_ID());
			}
			invoice.saveEx();
		}

		if (m_rma != null)
		{
			invoice.setM_RMA_ID(m_rma.getM_RMA_ID());
			invoice.saveEx();
		}

//		MInOut inout = null;
//		if (m_M_InOut_ID > 0)
//		{
//			inout = new MInOut(Env.getCtx(), m_M_InOut_ID, trxName);
//		}
//		if (inout != null && inout.getM_InOut_ID() != 0
//			&& inout.getC_Invoice_ID() == 0)	//	only first time
//		{
//			inout.setC_Invoice_ID(C_Invoice_ID);
//			inout.saveEx();
//		}

		//  Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
			{
				MProduct product = null;
				//  variable values
				BigDecimal QtyEntered = (BigDecimal)miniTable.getValueAt(i, 1);              //  1-Qty

				
				//mfrojas ofbforward artilec

				KeyNamePair pp = null;
				
				int C_UOM_ID = 0;
				if(!OFBForward.ArtilecCreateShipment())
				{
					pp = (KeyNamePair)miniTable.getValueAt(i, 2);   //  2-UOM
					C_UOM_ID = pp.getKey();
				}
				else
					log.config("ART uom");
				//
				
				int M_Product_ID = 0;
				if(!OFBForward.ArtilecCreateShipment())
				{
					pp = (KeyNamePair)miniTable.getValueAt(i, 3);               //  3-Product
					if (pp != null)
						M_Product_ID = pp.getKey();
				}
				else
					log.config("ART prod");
				
				//
				int C_OrderLine_ID = 0;
				pp = (KeyNamePair)miniTable.getValueAt(i, 5);               //  5-OrderLine
				if (pp != null)
					C_OrderLine_ID = pp.getKey();
				int M_InOutLine_ID = 0;
				pp = (KeyNamePair)miniTable.getValueAt(i, 6);               //  6-Shipment
				if (pp != null)
					M_InOutLine_ID = pp.getKey();
				//
				int M_RMALine_ID = 0;
				pp = (KeyNamePair)miniTable.getValueAt(i, 7);               //  7-RMALine
				if (pp != null)
					M_RMALine_ID = pp.getKey();

				//	Precision of Qty UOM
				int precision = 2;
				if (M_Product_ID != 0)
				{
					product = MProduct.get(Env.getCtx(), M_Product_ID);
					precision = product.getUOMPrecision();
				}
				QtyEntered = QtyEntered.setScale(precision, BigDecimal.ROUND_HALF_DOWN);
				//
				log.fine("Line QtyEntered=" + QtyEntered
					+ ", Product_ID=" + M_Product_ID
					+ ", OrderLine_ID=" + C_OrderLine_ID + ", InOutLine_ID=" + M_InOutLine_ID);

				//	Create new Invoice Line
				MInvoiceLine invoiceLine = new MInvoiceLine (invoice);
				invoiceLine.setM_Product_ID(M_Product_ID, C_UOM_ID);	//	Line UOM
				invoiceLine.setQty(QtyEntered);							//	Invoiced/Entered
				BigDecimal QtyInvoiced = null;
				if (M_Product_ID > 0 && product.getC_UOM_ID() != C_UOM_ID) {
					QtyInvoiced = MUOMConversion.convertProductFrom(Env.getCtx(), M_Product_ID, C_UOM_ID, QtyEntered);
				}
				if (QtyInvoiced == null)
					QtyInvoiced = QtyEntered;
				invoiceLine.setQtyInvoiced(QtyInvoiced);

				//  Info
				MOrderLine orderLine = null;
				if (C_OrderLine_ID != 0)
					orderLine = new MOrderLine (Env.getCtx(), C_OrderLine_ID, trxName);
				//
				MRMALine rmaLine = null;
				if (M_RMALine_ID > 0)
					rmaLine = new MRMALine (Env.getCtx(), M_RMALine_ID, null);
				//
				MInOutLine inoutLine = null;
				if (M_InOutLine_ID != 0)
				{
					inoutLine = new MInOutLine (Env.getCtx(), M_InOutLine_ID, trxName);
					if (orderLine == null && inoutLine.getC_OrderLine_ID() != 0)
					{
						C_OrderLine_ID = inoutLine.getC_OrderLine_ID();
						orderLine = new MOrderLine (Env.getCtx(), C_OrderLine_ID, trxName);
					}
				}
				else if (C_OrderLine_ID > 0)
				{
					String whereClause = "EXISTS (SELECT 1 FROM M_InOut io WHERE io.M_InOut_ID=M_InOutLine.M_InOut_ID AND io.DocStatus IN ('CO','CL'))";
					MInOutLine[] lines = MInOutLine.getOfOrderLine(Env.getCtx(),
						C_OrderLine_ID, whereClause, trxName);
					log.fine ("Receipt Lines with OrderLine = #" + lines.length);
					if (lines.length > 0)
					{
						for (int j = 0; j < lines.length; j++)
						{
							MInOutLine line = lines[j];
							if (line.getQtyEntered().compareTo(QtyEntered) == 0)
							{
								inoutLine = line;
								M_InOutLine_ID = inoutLine.getM_InOutLine_ID();
								break;
							}
						}
						if (inoutLine == null)
						{
							inoutLine = lines[0];	//	first as default
							M_InOutLine_ID = inoutLine.getM_InOutLine_ID();
						}
					}
				}
				else if (M_RMALine_ID != 0)
				{
					String whereClause = "EXISTS (SELECT 1 FROM M_InOut io WHERE io.M_InOut_ID=M_InOutLine.M_InOut_ID AND io.DocStatus IN ('CO','CL'))";
					MInOutLine[] lines = MInOutLine.getOfRMALine(Env.getCtx(), M_RMALine_ID, whereClause, null);
					log.fine ("Receipt Lines with RMALine = #" + lines.length);
					if (lines.length > 0)
					{
						for (int j = 0; j < lines.length; j++)
						{
							MInOutLine line = lines[j];
							if (rmaLine.getQty().compareTo(QtyEntered) == 0)
							{
								inoutLine = line;
								M_InOutLine_ID = inoutLine.getM_InOutLine_ID();
								break;
							}
						}
						if (rmaLine == null)
						{
							inoutLine = lines[0];	//	first as default
							M_InOutLine_ID = inoutLine.getM_InOutLine_ID();
						}
					}

				}
				//	get Ship info
				
				//	Shipment Info
				if (inoutLine != null)
				{
					invoiceLine.setShipLine(inoutLine);		//	overwrites
				}
				else {
					log.fine("No Receipt Line");
					//	Order Info
					if (orderLine != null)
					{
						invoiceLine.setOrderLine(orderLine);	//	overwrites
					}
					else
					{
						log.fine("No Order Line");
						invoiceLine.setPrice();
						invoiceLine.setTax();
					}

					//RMA Info
					if (rmaLine != null)
					{
						invoiceLine.setRMALine(rmaLine);		//	overwrites
					}
					else
						log.fine("No RMA Line");
				}
				
				//ininoles se sobreescribe ad_org_id por la de la orden 02-10-2019
				if(OFBForward.CopyOrgFromOrderInvoice())
				{
					if(orderLine != null )
						invoiceLine.setAD_Org_ID(orderLine.getAD_Org_ID());
				}
				invoiceLine.saveEx();
			}   //   if selected
		}   //  for all rows

		return true;
	}   //  saveInvoice

	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
	    Vector<String> columnNames = new Vector<String>(7);
	    columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
	    columnNames.add(Msg.translate(Env.getCtx(), "Quantity"));
	    if(!OFBForward.ArtilecCreateShipment())
	    	columnNames.add(Msg.translate(Env.getCtx(), "C_UOM_ID"));
	    else
			columnNames.add("Codigo + Nombre");
	    if(!OFBForward.ArtilecCreateShipment())
	    	columnNames.add(Msg.translate(Env.getCtx(), "M_Product_ID"));
	    else
	    	columnNames.add("Precio");
	    if(!OFBForward.ArtilecCreateShipment())
	    	columnNames.add(Msg.getElement(Env.getCtx(), "VendorProductNo", false));
	    else
	    	columnNames.add("Producto");
	    columnNames.add(Msg.getElement(Env.getCtx(), "C_Order_ID", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "M_InOut_ID", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "M_RMA_ID", false));

	    return columnNames;
	}

	/**OFB
	 * costom methods*/
	
	/**
	 *  Save - Create Invoice Lines
	 *  @return true if saved
	 */
	public boolean saveOFB(IMiniTable miniTable, String trxName)
	{
		int C_Invoice_ID = ((Integer)getGridTab().getValue("C_Invoice_ID")).intValue();
		MInvoice invoice = new MInvoice (Env.getCtx(), C_Invoice_ID, trxName);
		Integer docTypeId = (Integer)getGridTab().getValue("C_DocTypeTarget_ID");
		MDocType docType = MDocType.get(Env.getCtx(), docTypeId);
    	if(docType.getDocBaseType().equals("FAT"))
    		wType=1;
    	else if  (docType.getDocBaseType().equals("PTK"))//protesto
    		wType=2;
    	else if  ( docType.getDocBaseType().equals("CDC"))//cambio doc cliente
    		wType=3;
    	else if  ( docType.getDocBaseType().equals("VDC"))//cambio doc vendor
    		wType=4;
    	else if  ( docType.getDocBaseType().equals("PRV") && docType.getName().toLowerCase().contains("nota"))//tipo para mutual nota de credito
    		wType=6;
    	else if  ( docType.getDocBaseType().equals("PRV"))//tipo para mutual solicitud de provision
    		wType=5;
    	else
    		wType=0;// create from comun
		log.config(invoice.toString());
		int C_ChargeCheck_ID=0;
		int C_ChargeLetra_ID=0;
		int C_Factoring_ID=0;
		int C_FactoringPay_ID=0;
		int C_Extinguir_ID=0;
		int C_FactoringPayL_ID=0;
		int C_ChargeChange_ID = 0;
		
		
		if(wType==2){
			String sql1 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC03' and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
		    C_ChargeCheck_ID=  DB.getSQLValue("C_Charge",sql1);      
		    String sql2 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC04'  and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
			C_ChargeLetra_ID=   DB.getSQLValue("C_Charge",sql2); 
		}
		if(wType==3 || wType==4){
			String sql3 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC05'  and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
			C_ChargeChange_ID= DB.getSQLValue("C_Charge",sql3); 
		}	
		if(wType==1){
			String sql4 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC06'  and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
			C_Factoring_ID= DB.getSQLValue("C_Charge",sql4);
			String sql5 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC07'  and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
			C_FactoringPay_ID= DB.getSQLValue("C_Charge",sql5);
			String sql6 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC08'  and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
			C_Extinguir_ID= DB.getSQLValue("C_Charge",sql6);
			String sql7 = "SELECT C_Charge_ID FROM C_Charge WHERE upper(TipoCargo)='TC09'  and isactive='Y' and AD_client_ID=" +invoice.getAD_Client_ID();
			C_FactoringPayL_ID= DB.getSQLValue("C_Charge",sql7);
		}
			int Tax_ID =  DB.getSQLValue("C_Tax","SELECT C_Tax_ID FROM C_Tax WHERE IsTaxExempt='Y' and AD_client_ID=" +invoice.getAD_Client_ID());
		
	//  Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
			{
				if(wType != 6 && wType != 5)
				{
					int C_InvoiceFac_ID=0;
					int C_Payment_ID=0;
					Timestamp trxDate;
					BigDecimal TrxAmt;
					String Tender=new String();
					if(getPay)
					{
						trxDate = (Timestamp)miniTable.getValueAt(i, 1);  //  1-DateTrx
						KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 2);   //  2-C_Payment_ID
						C_Payment_ID = pp.getKey();
						pp = (KeyNamePair)miniTable.getValueAt(i, 4);               //  3-Currency
						int C_Currency_ID = pp.getKey();
						TrxAmt = (BigDecimal)miniTable.getValueAt(i, 5); //  4-PayAmt
				        Tender = (String)miniTable.getValueAt(i, 8);        //7-Tender Type
						//
						log.fine("Line Date=" + trxDate
							+ ", Payment=" + C_Payment_ID + ", Currency=" + C_Currency_ID + ", Amt=" + TrxAmt);
					}
					else
					{
						trxDate = (Timestamp)miniTable.getValueAt(i, 4);  //  4-DateTrx
	                    KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 2);   //  2-Invoice
	                    C_InvoiceFac_ID = pp.getKey();
	                    TrxAmt = (BigDecimal)miniTable.getValueAt(i, 3); //  3-PayAmt
					}	
					
					MInvoiceLine invoiceLine = new MInvoiceLine (invoice);
					if(wType==2 && Tender.equals("Letra"))
					{
						invoiceLine.setDescription("Letra Protestada");
						invoiceLine.setC_Charge_ID(C_ChargeLetra_ID);
					  log.config("C_ChargeLetra_ID:"+C_ChargeLetra_ID);
					}
					else if (wType==2 && (Tender.indexOf("Cheque")>=0 || Tender.indexOf("Check")>=0) )
					{
						invoiceLine.setDescription("Cheque Protestado");
						invoiceLine.setC_Charge_ID(C_ChargeCheck_ID);
					  log.config("C_ChargeCheck_ID:"+C_ChargeCheck_ID);
					}
					else if (wType==3 || wType==4)
					{
						invoiceLine.setDescription("Cambio de documento");
						invoiceLine.setC_Charge_ID(C_ChargeChange_ID);
					  log.config("C_ChargeReturn_ID:"+C_ChargeChange_ID);
					}
					else if (wType==1)//factoring
					{
						invoiceLine.setDescription("Factoring");
					  if(!getPay && !extinguir)
						  invoiceLine.setC_Charge_ID(C_Factoring_ID);
					  else if(getPay && !extinguir){
						  		if(Tender.equals("Cheque") || Tender.equals("Check"))
						  			invoiceLine.setC_Charge_ID(C_FactoringPay_ID);
						  		else
						  			invoiceLine.setC_Charge_ID(C_FactoringPayL_ID);
					  }
					  else
						  invoiceLine.setC_Charge_ID(C_Extinguir_ID);
					  log.config("C_Factoring_ID:"+C_Factoring_ID+"-"+C_FactoringPay_ID);
					}
					  log.config("C_Payment_ID:"+C_Payment_ID);
					  
					  if(getPay)
						  invoiceLine.set_ValueOfColumn("C_Payment_ID", C_Payment_ID);
					  else
						  invoiceLine.set_ValueOfColumn("C_InvoiceFac_ID",C_InvoiceFac_ID);
					  
					  invoiceLine.setPriceActual(TrxAmt.abs() );
					  invoiceLine.setPriceEntered(TrxAmt.abs() );
					  invoiceLine.setQtyEntered(Env.ONE);
					  invoiceLine.setQtyInvoiced(Env.ONE);
					  invoiceLine.setC_Tax_ID(Tax_ID);
					  invoiceLine.saveEx();
				}
				else //codigo pra mutual
				{
					int C_InvoiceLine_ID=0;
					KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 6);   //  2-Invoice
					C_InvoiceLine_ID = pp.getKey();
					if(C_InvoiceLine_ID > 0)
					{
						MInvoiceLine invoiceLineFrom = new MInvoiceLine (Env.getCtx(),C_InvoiceLine_ID,trxName);
						MInvoiceLine invoiceLineTo = new MInvoiceLine (invoice);						
						invoiceLineTo.setAD_Org_ID(invoiceLineFrom.getAD_Org_ID());
						if(wType == 5)
							invoiceLineTo.set_CustomColumn("Ref_InvoiceLine_ID", invoiceLineFrom.get_ID());
						else if(wType == 6)
							invoiceLineTo.set_CustomColumn("Ref_InvoiceReqLine_ID", invoiceLineFrom.get_ID());
						invoiceLineTo.setM_Product_ID(invoiceLineFrom.getM_Product_ID());		
						if (invoiceLineFrom.getC_Charge_ID() > 0)
							invoiceLineTo.setC_Charge_ID(invoiceLineFrom.getC_Charge_ID());
						invoiceLineTo.setM_AttributeSetInstance_ID(invoiceLineFrom.getM_AttributeSetInstance_ID());	
						invoiceLineTo.setQtyEntered(invoiceLineFrom.getQtyEntered());
						invoiceLineTo.setQtyInvoiced(invoiceLineFrom.getQtyEntered());
						invoiceLineTo.setDescription(invoiceLineFrom.getDescription());
						invoiceLineTo.setC_UOM_ID(invoiceLineFrom.getC_UOM_ID());
						invoiceLineTo.setC_Tax_ID(invoiceLineFrom.getC_Tax_ID());
						invoiceLineTo.setPrice(invoiceLineFrom.getPriceEntered());
						invoiceLineTo.setLineNetAmt();
						invoiceLineTo.saveEx();						
					}
				}
			}
		}		
		return true;
	}
}
