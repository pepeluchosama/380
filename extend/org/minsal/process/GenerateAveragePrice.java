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
 * Contributor(s): Chris Farley - northernbrewer                              *
 *****************************************************************************/
package org.minsal.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;

import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProductPrice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.ofb.utils.DateUtils;

/**
 *	
 *	
 */
public class GenerateAveragePrice extends SvrProcess
{	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	private Timestamp p_DateTrx;
	protected void prepare()
	{	

		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			
			if(name.equals("DateTrx"))
				p_DateTrx = para[i].getParameterAsTimestamp();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message 
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{	
		MPriceList pList = null;
		int cant=0;
		int cantUp=0;
		//se calcula rango de fechas
		Timestamp startDate = new Timestamp(p_DateTrx.getTime());
		startDate.setMonth(p_DateTrx.getMonth()-2);
		startDate.setDate(1);
		
		/*Calendar cal = Calendar.getInstance();
		cal.setTime(p_DateTrx);
		cal.add(Calendar.MONTH, -1);
		int diaFinal = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH,diaFinal);*/
		//ininoles fecha final sera hoy
		//Timestamp endDate = new Timestamp(cal.getTime().getTime());
		Timestamp endDate = DateUtils.today();
		
		//se busca si existe lista de precios existente
		int ID_PList = 0;
		ID_PList = DB.getSQLValue(get_TrxName(), "SELECT M_PriceList_ID FROM M_PriceList "
				+ " WHERE IsActive = 'Y' AND NAME like '%Precios Promedios%'");
		
		if(ID_PList > 0)
			pList = new MPriceList(getCtx(), ID_PList, get_TrxName());
		else //si no existe se crea lp
		{
			pList = new MPriceList(getCtx(), 0, get_TrxName());
			pList.setAD_Org_ID(0);
			pList.setName("Precios Promedios");
			pList.setC_Currency_ID(228);
			pList.setPricePrecision(2);
			pList.saveEx(get_TrxName());
		}
		//se busca version de lista de precios
		MPriceListVersion plVersion = null;
		int ID_PListversión = 0;
		ID_PListversión = DB.getSQLValue(get_TrxName(), "SELECT MAX(M_PriceList_Version_ID) FROM M_PriceList_Version "
				+ " WHERE IsActive = 'Y' AND NAME like '%Precios Promedio%'");
		
		if(ID_PListversión > 0)
			plVersion = new MPriceListVersion(getCtx(), ID_PListversión, get_TrxName()); 
		else
		{
			//se crea version de lista de precios
			plVersion = new MPriceListVersion(pList);
			plVersion.setName("Precios Promedio "+p_DateTrx.toString()); 
			plVersion.setValidFrom(p_DateTrx);
			plVersion.setM_DiscountSchema_ID(DB.getSQLValue(get_TrxName(), "SELECT MAX(M_DiscountSchema_ID) "
					+ " FROM M_DiscountSchema WHERE IsActive = 'Y'")); 
			plVersion.saveEx(get_TrxName());
		}
		
		//se crean los precios por producto
		String sqlProd = "SELECT M_Product_ID FROM M_Product "
				+ " WHERE IsActive = 'Y' AND IsPurchased = 'Y'";
		BigDecimal AmtPP=Env.ZERO;
		
		PreparedStatement pstmt = DB.prepareStatement (sqlProd, get_TrxName());	
		ResultSet rs = pstmt.executeQuery();
		int flag = 0;
		while(rs.next())
		{
			//se calcula precio promedio del producto
			AmtPP = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(ol.PriceEntered*iol.QtyEntered)/SUM(iol.QtyEntered) " + 
					" FROM M_InOutLine iol" + 
					" INNER JOIN M_InOut io ON (iol.M_InOut_ID = io.M_InOut_ID)" + 
					" INNER JOIN C_DocType dt ON (io.C_DocType_ID = dt.C_DocType_ID)" + 
					" INNER JOIN C_OrderLine ol ON (iol.C_OrderLine_ID = ol.C_OrderLine_ID)" + 
					" WHERE io.DocStatus IN ('CO','CL') AND io.IsSoTrx = 'N' AND dt.DocBaseType IN ('MMR')"+
					" AND iol.QtyEntered > 0 "+
					" AND iol.M_Product_ID= " + rs.getInt("M_Product_ID")+
					" AND movementdate BETWEEN ? AND ?", startDate,endDate);
			if(AmtPP == null)
				AmtPP  = Env.ZERO;
			//se revisa si existe regstro sino, se crea			
			flag = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM M_ProductPrice WHERE M_Product_ID="+rs.getInt("M_Product_ID")+
					" AND IsActive='Y' AND M_PriceList_Version_ID="+plVersion.get_ID());
			if(flag > 0)
			{
				MProductPrice pp = MProductPrice.get(getCtx(), plVersion.get_ID(), rs.getInt("M_Product_ID"), get_TrxName());
				pp.setPrices(AmtPP, AmtPP,pp.getPriceLimit()==null?Env.ZERO:pp.getPriceLimit());
				pp.saveEx(get_TrxName());
				cantUp++;
			}
			else
			{
				//se crea registro para el producto
				MProductPrice pp = new MProductPrice(getCtx(), plVersion.get_ID(), rs.getInt("M_Product_ID"), get_TrxName());
				pp.setPrices(AmtPP, AmtPP, AmtPP);
				pp.saveEx(get_TrxName());
				cant++;
			}
		}
		return "Se han actualizado "+cantUp+" precios y se han generado "+cant+" precios ";
	}	//	doIt
	
	
}	

