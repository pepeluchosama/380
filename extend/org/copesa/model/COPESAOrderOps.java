package org.copesa.model;

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MOrder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.copesa.utils.DateUtils;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.PO;
import java.util.Properties;

public class COPESAOrderOps {
	private static CLogger			log = CLogger.getCLogger (COPESAOrderOps.class);
	
	public static BigDecimal getMonthlyPrice(MOrder _order) throws Exception 
	{
		int orderid = _order.getC_Order_ID();
		if (orderid <= 0 || _order.getDocStatus().compareTo("DR") == 0)
			return null;
	    /*String sql = "SELECT Round(total, 2) FROM   co_factcalendar_header fac " +
	                 "JOIN c_order co on co.c_order_id =  fac.c_order_id " +
                     "WHERE  co.c_order_id = ? " +
                     "  AND fac.ini <= Greatest(Now(), co.datepromised + '2 days'::interval) " +
                     "  AND fac.fin > Greatest(Now(), co.datepromised + '2 days'::interval)";*/
	    String sql = "select round(sum(col.monthlyamount) * 1.19, 2) " +      // Se pone IVA en duro a prop�sito por motivos de eficiencia
                     "from c_orderline col " +
                     "join c_order co on col.c_order_id = co.c_order_id  " +
                     "where co.c_order_id = ? " +
                     "and Greatest(Now(), co.datepromised + '2 days'::interval) between col.datepromised2 and col.datepromised3";
	    
	    PreparedStatement pstmt = DB.prepareStatement(sql, _order.get_TrxName());
	    pstmt.setInt(1, orderid);
	    BigDecimal amount = null;
	    ResultSet rs = pstmt.executeQuery();
	    if (rs.next() )
	    	amount = rs.getBigDecimal(1);
	    rs.close();
	    pstmt.close();
	    return amount;
	    
	}

	public static BigDecimal getCopayment(MOrder _order) throws Exception 
	{
		int orderid = _order.getC_Order_ID();
		if (orderid <= 0 || _order.getDocStatus().compareTo("DR") == 0 || _order.getDatePromised().compareTo(_order.getDateOrdered()) == 0)
			return null;
		
	    /*String sql = "SELECT Round(total, 2) " +
                     "FROM   co_factcalendar_header " +
                     "WHERE  c_order_id = ? AND periodo = 0";
	    */
		
		String sql = "select round(sum(col.linenetamt) * 1.19, 2) " + 
					 " from c_orderline col " + 
					 " join c_order co on col.c_order_id = co.c_order_id " +  
					 " where co.c_order_id = ? " + 
					 "   and (col.datepromised2 + '1 day'::interval > col.datepromised3 Or co.paymentrule = 'I') " +
					 "   and col.linenetamt > 10";
		
	    PreparedStatement pstmt = DB.prepareStatement(sql, _order.get_TrxName());
	    pstmt.setInt(1, orderid);
	    
	    ResultSet rs = pstmt.executeQuery();
	    BigDecimal amount = null;
	    if (rs.next() )
	    	amount = rs.getBigDecimal(1);
	    rs.close();
	    pstmt.close();
	    return amount;
	    
	}

	public static Timestamp getDateFirstInvoice(MOrder _order) throws Exception 
	{
		int orderid = _order.getC_Order_ID();
		if (orderid <= 0 || _order.getDocStatus().compareTo("DR") == 0 || _order.getDatePromised().compareTo(_order.getDateOrdered()) == 0)
			return null;

		//String sql = "SELECT Min(fin) FROM co_factcalendar_header WHERE c_order_id = ?";
	    String sql = "select copesa_getdatefirstinvoice(?)";
	    PreparedStatement pstmt = DB.prepareStatement(sql, _order.get_TrxName());
	    pstmt.setInt(1, orderid);
	    
	    ResultSet rs = pstmt.executeQuery();
	    Timestamp dateFirstInvoice = null;
	    if (rs.next() )
	       dateFirstInvoice = rs.getTimestamp(1);
	    rs.close();
	    pstmt.close();
	    return dateFirstInvoice;
	    
	}
	
	public static void UpdatePrices(MOrder _order) throws Exception
	{
		int orderid = _order.getC_Order_ID();
		if (orderid <= 0)
			return;
		
		BigDecimal monthPrice = COPESAOrderOps.getMonthlyPrice(_order);
		BigDecimal copayment = COPESAOrderOps.getCopayment(_order);
		Timestamp dateFirstInvoice = COPESAOrderOps.getDateFirstInvoice(_order);

		String sql = "UPDATE C_Order set pricepat = ?, copayment = ?,  DateFirstInvoice = ? WHERE c_order_id = ?";
	    
	    PreparedStatement pstmt = DB.prepareStatement(sql, _order.get_TrxName());
	    pstmt.setBigDecimal(1, monthPrice);
	    pstmt.setBigDecimal(2, copayment);
	    pstmt.setTimestamp(3, dateFirstInvoice);
	    pstmt.setInt(4, orderid );
	    
	    pstmt.execute();
	    pstmt.close();
	    
	}
	
	public static void AddFreightLines(int _orderid, int _userid, String _trxName) throws Exception
	{
		if (_orderid <= 0)
			return;
		
		if (_userid <= 0)
			return;
		
		String sql = "SELECT COPESA_UpdateFreight( ?, ?)";
	    
	    PreparedStatement pstmt = DB.prepareStatement(sql, _trxName);
	    pstmt.setInt(1, _orderid);
	    pstmt.setInt(2, _userid);
	    
	    pstmt.execute();
	    pstmt.close();
	    
	}

	
	public static void SetMovDates(int _orderid, String _trxName) throws Exception
	{
		if (_orderid <= 0)
			return;
		
		String sql = "SELECT COPESA_setmovdates( ? )";
	    
	    PreparedStatement pstmt = DB.prepareStatement(sql, _trxName);
	    pstmt.setInt(1, _orderid);
	    pstmt.execute();
	    pstmt.close();
	    
	}

	public static void setOrderLinesDates(int _orderid, String _trxName) throws Exception
	{
		
		if (_orderid <= 0 )
			return;

		String sql = "select copesa_setorderlinesdates(?)";
	    
	    PreparedStatement pstmt = DB.prepareStatement(sql, _trxName);
	    pstmt.setInt(1, _orderid);
	    pstmt.execute();
	    pstmt.close();
	}

	public static void SetDatesForNoPAT(MOrder _order)
	{
		int orderid = _order.getC_Order_ID();
		if (orderid <= 0)
			return;

		String sql = "UPDATE C_OrderLine set datepromised3 = to_date('01-01-3022', 'DD-MM-YYYY'), isactive = 'Y' WHERE C_Order_ID = " + orderid + " AND isfree = 'N'";
		DB.executeUpdate(sql, _order.get_TrxName());
	}

	
	
	public static int getLineGeozone(MOrderLine _orderline) throws Exception 
	{
		int geozoneid = -1;
		
		int orderlineid = _orderline.getC_OrderLine_ID();
		if (orderlineid <= 0 )
			return -1;
		
		int mprodid = _orderline.getM_Product_ID();
		if (mprodid <= 0)
			mprodid = _orderline.get_ValueAsInt("M_ProductRef_ID");
		
		if( mprodid <= 0 )
			return -1;
		
		
		int locid = _orderline.getC_BPartner_Location_ID();
		if (locid <= 0)
			return -1;
		
		String sql = "select case when cat.description ~ 'NOEDITORIAL' then 'G' ELSE 'E' end " +
		             "from m_product mp " +
		             "join m_product_category cat on (cat.m_product_category_id = mp.m_product_category_id )" +
			         "where mp.m_product_id = ? ";

		PreparedStatement pstmt = DB.prepareStatement(sql, _orderline.get_TrxName());
	    pstmt.setInt(1, mprodid);
	    ResultSet rs = pstmt.executeQuery();
	    String cat = "E";
	    if (rs.next() )
	    	cat = rs.getString(1);
	    rs.close();
	    pstmt.close();
			  
		
	    sql = "select MAX(gzc.C_Geozone_ID) " +
	          "FROM C_GeozoneCities gzc " + 
	          "JOIN C_Geozone gz ON (gzc.C_Geozone_ID = gz.C_Geozone_ID) " + 
	          "JOIN C_BPartner_Location cbp on cbp.c_city_id = gzc.c_city_id " +  
	          "where cbp.c_bpartner_location_id = ? " +
	          "  and gzc.IsActive = 'Y' " +
	          "  and gz.type = ?";
	    
	    pstmt = DB.prepareStatement(sql, _orderline.get_TrxName());
	    pstmt.setInt(1, locid);
	    pstmt.setString(2, cat);
	    rs = pstmt.executeQuery();
	    if (rs.next() )
	    	geozoneid = rs.getInt(1);
	    rs.close();
	    pstmt.close();
	    return geozoneid;
	    
	}
	
    public static void createFreeLine(MOrder _order, MOrderLine _line, int _freeDays, BigDecimal _newAmt, BigDecimal _newAmtPAT, int _duration) throws Exception
    {
			MOrderLine oLineNew = new MOrderLine(_order);
			oLineNew.setAD_Org_ID(_line.getAD_Org_ID());
			oLineNew.setC_BPartner_Location_ID(_line.getC_BPartner_Location_ID());
			oLineNew.set_CustomColumn("C_BPartnerRef_ID", _line.get_ValueAsInt("C_BPartnerRef_ID"));
			oLineNew.setM_Product_ID(_line.getM_Product_ID());
			oLineNew.setQty(_line.getQtyEntered());
			oLineNew.set_CustomColumn("C_CalendarCOPESA_ID", _line.get_ValueAsInt("C_CalendarCOPESA_ID"));
			//ininoles seteamos nuevo monto
			//oLineNew.setPrice(Env.ONE);
			oLineNew.setPrice(_newAmt);
			oLineNew.set_CustomColumn("C_OrderLineRef_ID", _line.get_ID());
			oLineNew.set_CustomColumn("IsFree", true);
			//oLineNew.set_CustomColumn("DatePromised2", order.getDateOrdered());
			oLineNew.set_CustomColumn("DatePromised2", _order.getDatePromised());
			oLineNew.set_CustomColumn("M_Locator_ID", _line.get_ValueAsInt("M_Locator_ID"));
			
			//se suman dias a fecha fin
			//ininoles nueva validacion y cambios para fecha fin
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(_order.getDateOrdered().getTime());
			if(_freeDays > 0)
			{
				calendar.add(Calendar.DATE, _freeDays);
				Timestamp datEnd = new Timestamp(calendar.getTimeInMillis());					
				oLineNew.set_CustomColumn("DatePromised3", datEnd);
			}else if(_freeDays == 0){									
				oLineNew.set_CustomColumn("DatePromised3", _order.getDateOrdered());
			}						
			//campo gracia
			if(_newAmtPAT != null && _newAmtPAT.compareTo(Env.ZERO) > 0)
			{
				oLineNew.set_CustomColumn("MonthlyAmount",_newAmtPAT);
			}
			int geozoneid = _line.get_ValueAsInt("C_Geozone_ID");
			if (geozoneid <= 0)
			    geozoneid = COPESAOrderOps.getLineGeozone(_line);
			if (geozoneid > 0)
			{	
			    oLineNew.set_CustomColumn("C_Geozone_ID", geozoneid);
			    _line.set_CustomColumn("C_Geozone_ID", geozoneid);
			}    
			oLineNew.save();
			//actualizamos fecha de inicio de linea base
			calendar.add(Calendar.DATE, 1);
			_line.set_CustomColumn("DatePromised2",new Timestamp(calendar.getTimeInMillis()));
			
			Timestamp dateEnd = null;
			if (_order.getPaymentRule().compareTo("D") == 0 )
				dateEnd = DateUtils.veryDistantDate();
			else
			{
				if (_duration == 0 )
					_duration = 1;
				
				if (_duration > 0 )
				{	
					calendar.add(Calendar.DATE, _duration - 1);
					dateEnd = new Timestamp(calendar.getTimeInMillis());
				}
			}
			_line.set_CustomColumn("DatePromised3", dateEnd);		

			_line.set_CustomColumn("C_OrderLineRef_ID", oLineNew.get_ID());												
			_line.save();																						

    }

    public static boolean isThereStock(String _trxName, int _warehouseid, int _locatorid, int _productid)
    {
		BigDecimal qtyStock = DB.getSQLValueBD(_trxName, "SELECT bomqtyavailablecopesa(M_Product_ID," +_warehouseid +","+ _locatorid +")"+
				" FROM M_Product WHERE M_Product_ID = "+_productid);
		
		return qtyStock.compareTo(BigDecimal.ZERO) > 0;
    	
    }
	
    public static boolean allLocationsTheSame(MOrder _order, int _locationid)
    {
    	// Si encontramos una direcci�n distinta, entonces no todas las direcciones en la orden son iguales al par�metro
    	for( MOrderLine line: _order.getLines() )
    	{
    		if ( line.getC_BPartner_Location_ID() != _locationid )
    			return false;
    	}
    	return true;
    }
    
    public static boolean isOnlyOneLocationRequired(String _trxName, int _pricelistid)
    {
		String isonelocation = DB.getSQLValueString(_trxName, "SELECT issamebplocator from m_pricelist where m_pricelist_id = "+_pricelistid);
		
		return isonelocation.compareTo("Y") == 0;
    	
    }

    public static int altLineCalendar(MOrder _order, MProduct _product)
    {
		if(_product.get_ValueAsBoolean("IsPrimaryCalendar") == false)
		{
			String sql = "SELECT Count(*) + (case when NOT EXISTS(SELECT * FROM   c_orderline ol " + 
                         "                                        INNER JOIN m_product mp ON ( ol.m_product_id = mp.m_product_id ) " + 
                         "                                        WHERE  c_order_id = " + _order.get_ID() + " AND mp.isprimarycalendar = 'Y') " + 
                         "                          OR NOT EXISTS(SELECT * FROM c_orderline ol WHERE  c_order_id = " + _order.get_ID() + 
                         ") Then 1 else 0 end) " +
                         "FROM c_orderline ol " + 
                         "INNER JOIN m_product mp ON ol.m_product_id = mp.m_product_id " +
                         "WHERE c_order_id = " + _order.get_ID() + "AND  mp.isprimarycalendar = 'Y' " + 
                         "AND ol.isactive = 'Y' " + 
                         "AND ol.c_calendarcopesa_id IS NOT NULL AND Cal_contains(ol.c_calendarcopesa_id," + _product.get_Value("C_CalendarCOPESA_ID") + ") = 't' ";
			
			int cantContain = DB.getSQLValue(_order.get_TrxName(), sql);
			
			if( cantContain <= 0 && _product.get_ValueAsInt("C_CalendarCOPESARef_ID") > 0 )
				return _product.get_ValueAsInt("C_CalendarCOPESARef_ID");
		}	    	

		return _product.get_ValueAsInt("C_CalendarCOPESA_ID");
    }
    
    
    public static boolean validLocation(String _trxName, int _locationid )
    {
    	String SQL = "select count(*) " +
                     "from c_bpartner_location loc " +
                     "join ad_sysconfig cfg on cfg.name = 'COPESA_ZONAS_SINREPARTO' " + 
                     "where cfg.value ~ (trim(loc.sector) || '-' || trim(loc.zone))" +
                     "  and loc.c_bpartner_location_id = " + _locationid;
    	int cant = DB.getSQLValue(_trxName, SQL);
    	
    	return cant == 0;
    }
    
	public static boolean validStock (MOrder _order, boolean throwexception) throws AdempiereException
	{
		MOrderLine[] oLines2 = _order.getLines(false, null);
		for (int i = 0; i < oLines2.length; i++)
		{
			MOrderLine line = oLines2[i];
			if(line.getM_Product_ID() > 0)
			{
				if(line.getM_Product().getProductType().compareToIgnoreCase("I")==0)
				{
					String sql = "select bomqtyavailablecopesa("+line.getM_Product_ID() + "," + _order.getM_Warehouse_ID()+","+line.get_ValueAsInt("M_Locator_ID")+")";
					BigDecimal qty = DB.getSQLValueBD(_order.get_TrxName(), sql);
					
					// Si la orden est� en estado inv�lido, la funci�n sql no la est� contando, por lo que se deber restar en este punto.
					if( _order.getDocStatus().compareTo("IN") == 0 )
						qty = qty.subtract(Env.ONE);
					
					if(qty.compareTo(Env.ZERO)<0)
					{
						if(throwexception)
							throw new AdempiereException("No queda stock de producto: " + line.getM_Product().getName() + "." );
						else
						    return false;
					}	
				}
			}
		}
		return true;
	}	//	copyValues    

	
	public static void splitLTFull(PO po, MOrder order) throws SQLException {
		//buscamos ID linea de producto full
		int ID_LineProdFull = 0;
		
		String sql = "select c_orderline_id from c_orderline where isactive = 'Y' and c_order_id = ? and m_product_id = 2000003";
		PreparedStatement pstmt = DB.prepareStatement(sql, order.get_TrxName());
		pstmt.setInt(1, order.getC_Order_ID());
		ResultSet rs = pstmt.executeQuery();
		while (rs.next() )
		{
			ID_LineProdFull = rs.getInt(1);
			divideLTFull(po, order, ID_LineProdFull);
		}
		rs.close();
		pstmt.close();
	}

	public static void divideLTFull(PO po, MOrder order, int ID_LineProdFull) {
		if(ID_LineProdFull > 0)
		{
			int ID_LV = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(M_Product_ID) FROM M_Product WHERE upper(Description) like '%FULLLV%'");
			MProduct prodLV = new MProduct(po.getCtx(), ID_LV, po.get_TrxName());
			int ID_SD = DB.getSQLValue(po.get_TrxName(), "SELECT MAX(M_Product_ID) FROM M_Product WHERE upper(Description) like '%FULLSD%'");
			MProduct prodSD = new MProduct(po.getCtx(), ID_SD, po.get_TrxName());
			if(ID_LV > 0 && ID_SD > 0)
			{
				//actualizamos la linea existente con producto L-V
				if(prodLV.get_ValueAsInt("C_CalendarCOPESA_ID") > 0)
					DB.executeUpdate("UPDATE C_OrderLine SET M_Product_ID = "+ID_LV+", C_CalendarCOPESA_ID = "+prodLV.get_ValueAsInt("C_CalendarCOPESA_ID")+" WHERE C_OrderLine_ID = "+ID_LineProdFull, po.get_TrxName());
				else
					DB.executeUpdate("UPDATE C_OrderLine SET M_Product_ID = "+ID_LV+" WHERE C_OrderLine_ID = "+ID_LineProdFull, po.get_TrxName());
				//se actualiza precio mensual L-V
				MOrderLine oLineOld = new MOrderLine(po.getCtx(), ID_LineProdFull, po.get_TrxName());
				int ID_Level = oLineOld.get_ValueAsInt("LevelNo");
				BigDecimal newAmtPAT = null;
				if(ID_Level == 1)
				{
					newAmtPAT= DB.getSQLValueBD(po.get_TrxName(), "SELECT MAX(PricePAT) as PricePAT " +
							" FROM M_ProductPrice pp " +
							" INNER JOIN M_PriceList_Version plv ON pp.M_PriceList_Version_ID = plv.M_PriceList_Version_ID " +
							" INNER JOIN M_PriceList pl ON plv.M_PriceList_ID = pl.M_PriceList_ID " +
							" WHERE pp.IsActive = 'Y' AND M_product_ID = "+ID_LV+
							" AND Levels = "+ID_Level+" AND pl.M_priceList_ID = "+order.getM_PriceList_ID());
				}else if (ID_Level > 1)
				{
					newAmtPAT = DB.getSQLValueBD(po.get_TrxName(), "SELECT MAX(PricePAT) as PricePAT " +
							" FROM M_ProductPriceRef pp " +
							" INNER JOIN M_PriceList_Version plv ON pp.M_PriceList_Version_ID = plv.M_PriceList_Version_ID " +
							" INNER JOIN M_PriceList pl ON plv.M_PriceList_ID = pl.M_PriceList_ID " +
							" WHERE pp.IsActive = 'Y' AND M_product_ID = "+ID_LV+
							" AND Levels = "+ID_Level+" AND pl.M_priceList_ID = "+order.getM_PriceList_ID());
				}
				if(newAmtPAT != null)
					DB.executeUpdate("UPDATE C_OrderLine SET monthlyAmount = "+newAmtPAT+" WHERE C_OrderLine_ID = "+ID_LineProdFull, po.get_TrxName());
					
				//creamos nueva linea con producto S-D Precio 0
				MOrderLine oLineBase = new MOrderLine(po.getCtx(),ID_LineProdFull,po.get_TrxName());
				MOrderLine oLine = new MOrderLine(order);
				oLine.setAD_Org_ID(order.getAD_Org_ID());
				oLine.setC_BPartner_Location_ID(oLineBase.getC_BPartner_Location_ID());
				//oLine.set_CustomColumn("AD_User_ID", oLineBase.get_ValueAsInt("AD_User_ID"));
				oLine.set_CustomColumn("C_BPartnerRef_ID", oLineBase.get_ValueAsInt("C_BPartnerRef_ID"));
				oLine.setM_Product_ID(ID_SD);
				oLine.setQty(Env.ONE);
				oLine.setPrice(Env.ZERO);
				//se acualiza precio mensual S-D
				BigDecimal newAmtPAT2 = null;
				if(ID_Level == 1)
				{
					newAmtPAT2= DB.getSQLValueBD(po.get_TrxName(), "SELECT MAX(PricePAT) as PricePAT " +
							" FROM M_ProductPrice pp " +
							" INNER JOIN M_PriceList_Version plv ON pp.M_PriceList_Version_ID = plv.M_PriceList_Version_ID " +
							" INNER JOIN M_PriceList pl ON plv.M_PriceList_ID = pl.M_PriceList_ID " +
							" WHERE pp.IsActive = 'Y' AND M_product_ID = "+ID_SD+
							" AND Levels = "+ID_Level+" AND pl.M_priceList_ID = "+order.getM_PriceList_ID());
				}else if (ID_Level > 1)
				{
					newAmtPAT2 = DB.getSQLValueBD(po.get_TrxName(), "SELECT MAX(PricePAT) as PricePAT " +
							" FROM M_ProductPriceRef pp " +
							" INNER JOIN M_PriceList_Version plv ON pp.M_PriceList_Version_ID = plv.M_PriceList_Version_ID " +
							" INNER JOIN M_PriceList pl ON plv.M_PriceList_ID = pl.M_PriceList_ID " +
							" WHERE pp.IsActive = 'Y' AND M_product_ID = "+ID_SD+
							" AND Levels = "+ID_Level+" AND pl.M_priceList_ID = "+order.getM_PriceList_ID());
				}
				if(newAmtPAT2 != null)
					oLine.set_CustomColumn("MonthlyAmount",newAmtPAT2);							
				oLine.save();
				if(prodSD.get_ValueAsInt("C_CalendarCOPESA_ID") > 0)
				{
					oLine.set_CustomColumn("C_CalendarCOPESA_ID", prodSD.get_ValueAsInt("C_CalendarCOPESA_ID"));
					oLine.save();
				}
			}	
		}
	}
	
	public static boolean create_osc(MOrder order) {
		if(order.isSOTrx())
		{
			int orderid = order.getC_Order_ID();
			if (orderid <= 0)
				return false;
			
			String sql = "SELECT copesa_create_osc(?)";
		    
			try
			{
			    PreparedStatement pstmt = DB.prepareStatement(sql, order.get_TrxName());
			    pstmt.setInt(1, orderid);
			    pstmt.execute();
			    pstmt.close();
			}
			catch(Exception e)
			{
				log.severe(e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	public static String closeOrder(int _orderid, String _trxname, Properties _ctx) {
		String sql = "update c_invoiceline cil set c_orderline_id = null from c_orderline col where cil.c_orderline_id = col.c_orderline_id " +
		             "   and col.m_product_id is null and col.c_order_id = " + _orderid;
		DB.executeUpdate(sql, _trxname);
		
		MOrder order = new MOrder(_ctx, _orderid, _trxname);
		order.setDocStatus("CL");
		order.prepareIt();
		order.closeIt();
		order.save();
		return "Cierre de documento ejecutado (c_order_id=" + _orderid + ")";
	}	
	
}
