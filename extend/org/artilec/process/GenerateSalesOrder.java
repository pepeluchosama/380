/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
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

package org.artilec.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.model.MCity;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MPaymentTerm;




/**
 *	
 *	
 *  @author mfrojas
 *  @version $Id: GenerateSalesOrder.java,v 1.2 2008/07/30 00:51:01 jjanke Exp $
 *  
 *  Generate Sales Order, Shipment, Invoice, according to Artilec needs
 */


public class GenerateSalesOrder extends SvrProcess {
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message 
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{	

		//Inicial: Valida stock antes de crear cualquier orden
		
		String sqlvalidastock = "SELECT warehousevalue, productvalue, qtyordered, documentno " +
				" FROM i_salesws where i_isimported = 'N' and processed='N' " +
				" order by documentno ";

		log.config("sql validate = "+sqlvalidastock);
		PreparedStatement pstmtstock = null;
		ResultSet rsstock = null;
		try
		{
			pstmtstock = DB.prepareStatement(sqlvalidastock, get_TrxName());
			rsstock = pstmtstock.executeQuery();

			while(rsstock.next())
			{
				String sqlprod = "SELECT coalesce(max(m_product_Id), 0) from m_product where value like '"+rsstock.getString("productvalue")+"' AND AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
				int prod = DB.getSQLValue(get_TrxName(), sqlprod);
				if(prod == 0)
					return "El producto "+rsstock.getString("productvalue")+" no existe en el sistema.";
				
				//valida stock en ubicacion
				int ware = DB.getSQLValue(get_TrxName(), "SELECT max(m_warehouse_id) from m_warehouse " +
								" where value like '"+rsstock.getString("warehousevalue")+"'");
				if(ware == 0)
					return "No hay almacen "+rsstock.getString("warehousevalue");
				
				String sqlstock = "SELECT coalesce(sum(qtyonhand),0) from m_storage where m_product_id = ? " +
						" AND m_locator_Id in (SELECT m_locator_id from m_locator where m_Warehouse_id = "+ware+" )";
				BigDecimal sqlstockdb = DB.getSQLValueBD(get_TrxName(), sqlstock, prod);
				
				if(sqlstockdb.compareTo(rsstock.getBigDecimal("qtyordered")) < 0)
					return "No hay suficiente stock del " +
							" producto "+rsstock.getString("productvalue")+" en " +
							" orden "+rsstock.getString("documentno")+" Almacen "+rsstock.getString("warehousevalue");
			}
		}catch (Exception e)
		{
			
		}
		finally
		{
			pstmtstock.close();
			rsstock.close();
		}
		
		String sql = "SELECT documentno, bpname, bpvalue, address1, address2, city," +
				" qtyordered, price, productvalue, paymentterm, documenttype, invoicetype, " +
				" inouttype, completeinout, warehousevalue, createpayment, giro, " +
				" email, phone, AD_Client_ID, AD_Org_ID, created, i_Salesws_id " +
				" FROM i_salesws where i_isimported = 'N' and processed='N' " +
				" order by documentno ";
		
		
		
		log.config("sql = "+sql);
		PreparedStatement pstmt = null;
		int cant = 0;
		
		
		//@mfrojas se agrega variable de tipo string, para agregar al valor de retorno, en caso de encontrar errores.
		String Hay_Errores = "";
		int pr=0;

		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			ResultSet rs = pstmt.executeQuery ();			
			String OrderFlag = null;
			String lastOrder = "";
			String flag_guia = ""; 
			String nrodocumento = "";
			String tipodocnv = "";
			String tipodocmin = "";
			String tipodocinv = "";
			String completeinout = "";
			String ID_invXML = "0";

			int doctypenv = 0;
			int doctypemin = 0;
			int doctypeinv = 0;
			
			int warehouse = 0;
			
			//Boolean flagComplete = false;
			MOrder ord = null;
			MInOut min = null;
			MInvoice inv = null;
			int lineNo = 0;
			while (rs.next ())
			{
				String LogError = "";
			
				nrodocumento = rs.getString("documentno");
				OrderFlag = rs.getString("documentno");
				completeinout = rs.getString("completeinout");

			
				//generación de cabecera de OV
				if(lastOrder.compareTo(OrderFlag) != 0 )
				{
					if(ord != null)
					{
						ord.setDocAction("CO");
						if(ord.processIt ("CO"))
						{
							if(ord.save())
								cant++;
						}
						//commitEx();
						ord = null;
					}
					if(ord == null)
					{			
						
						//se buscan valores necesarios para la NV
			
						//tipo de documento
						
						if(rs.getString("documenttype") == null)
							return "no hay tipo de docuento";
						tipodocnv = rs.getString("documenttype");
						
						if(rs.getString("invoicetype") == null)
							return "no hay tipo documento factura";
						tipodocinv = rs.getString("invoicetype");
						
						if(rs.getString("inouttype") == null)
							return "no hay tipo documento inout";
						tipodocmin = rs.getString("inouttype");
						
						
						//Almacen
						warehouse = DB.getSQLValue(get_TrxName(), "SELECT max(m_warehouse_id) from m_warehouse " +
								" where value like '"+rs.getString("warehousevalue")+"'");
						if(warehouse == 0)
							return "No hay almacen";
						
						//cliente

						if(rs.getString("BPValue") == null)
							return "El RUT viene vacio";
						
						//Obtener rut y digito
						String rut = rs.getString("BPValue").substring(0, rs.getString("BPValue").length()-1);
						String digito = rs.getString("BPValue").substring(rs.getString("BPValue").length()-1);
						
						log.config("rut "+rut+" digito "+digito);
						int ID_Cliente = DB.getSQLValue(get_TrxName(), "SELECT MAX(C_Bpartner_ID) FROM C_Bpartner " +
								" WHERE IsActive = 'Y' AND AD_Client_ID = 2000006 " +
								" AND value like '"+rs.getString("BPValue")+"'");
						//validacion de clientref
						if(ID_Cliente <= 0)
						{
							//Si el cliente no existe, se debe crear.
							MBPartner bp = new MBPartner(getCtx(), 0, get_TrxName());
							MLocation loc = new MLocation(getCtx(), 0, get_TrxName());
							
							
							bp.setClientOrg(rs.getInt("AD_Client_ID"), rs.getInt("AD_Org_ID"));
							bp.setValue(rut);
							bp.set_CustomColumn("digito", digito);
							if(rs.getString("BPName") != null)
							{
								bp.setName(rs.getString("BPName"));
								bp.setName2(rs.getString("BPName"));
							}
							else
							{
								bp.setName("default");
								bp.setName("default2");
							}
							bp.setIsVendor(true);
							bp.setIsCustomer(true);
							if(rs.getString("giro") != null)
								bp.set_CustomColumn("Giro", rs.getString("giro"));

							//Se agrega grupo segun solicitud.	
							bp.setC_BP_Group_ID(2000067);
							bp.setM_PriceList_ID(2000004);
							bp.setDescription(rs.getString("email"));
							
							if(!bp.save())
								return "No puedo guardar BP";

							//Direccion
							if(rs.getString("Address1") != null)
							{
								//Se puede agregar direccion
								loc.setAddress1(rs.getString("Address1"));
								
							}
							else
							{
								loc.setAddress1("Direccion por defecto");
							}
							

							if(rs.getString("Address2") != null)
							{
								//Se puede agregar direccion
								loc.setAddress1(rs.getString("Address2"));
								
							}
							else
							{
								//loc.setAddress2("Direccion por defecto");
							}

							String city = null;
							if(rs.getString("City") != null)
							{
								//Se puede agregar direccion
								loc.setCity(rs.getString("City"));
								int comuna = DB.getSQLValue(get_TrxName(), "SELECT coalesce(c_city_id,0) from c_city WHERE" +
										" name like '%"+rs.getString("City")+"%'");
								if(comuna > 0)
								{
									loc.setC_City_ID(comuna);
									MCity mcity = new MCity(getCtx(), comuna, get_TrxName());
									/*if(mcity.get_Value("C_Province_ID") != null && mcity.get_ValueAsInt("C_Province_ID") > 0)
									{
										int province = mcity.get_ValueAsInt("C_Province_ID");
										loc.set_CustomColumn("C_Province_ID", province);
									}*/
									if(mcity.get_Value("C_Region_ID") != null && mcity.get_ValueAsInt("C_Region_ID") > 0)
									{	
										int region = mcity.get_ValueAsInt("C_Region_ID");
										loc.set_CustomColumn("C_Region_ID", region);
									}
								}
								city = rs.getString("City");
								//Se agrega el resto de los campos de location
								
							}
							else
							{
								loc.setCity("Ciudad por defecto");
							}
							
							if(!loc.save())
								return "No puede guardar direccion";
							MBPartnerLocation bploc = new MBPartnerLocation(bp);
							bploc.setC_Location_ID(loc.getC_Location_ID());
							//mfrojas 20220927 agregar campos de direccion en base a city
							if(city != null)
							{
								//city
								
								int comuna = DB.getSQLValue(get_TrxName(), "SELECT coalesce(c_city_id,0) from c_city WHERE" +
										" name like '%"+city+"%'");
								if(comuna != 0)
								{
									bploc.set_CustomColumn("C_City_ID", comuna);
									MCity mcity = new MCity(getCtx(), comuna, get_TrxName());
									if(mcity.get_Value("C_Province_ID") != null && mcity.get_ValueAsInt("C_Province_ID") > 0)
									{
										int province = mcity.get_ValueAsInt("C_Province_ID");
										bploc.set_CustomColumn("C_Province_ID", province);
									}
									if(mcity.get_Value("C_Region_ID") != null && mcity.get_ValueAsInt("C_Region_ID") > 0)
									{	
										int region = mcity.get_ValueAsInt("C_Region_ID");
										bploc.set_CustomColumn("C_Region_ID", region);
									}
									
								}
								//provincia
								
								//region
							}
							
							
							if(!bploc.save())
								return "No puedo guardar bploc";
							bploc.setIsActive(true);
							
							bploc.save();
							ID_Cliente = bp.get_ID();
						}
						
						
						log.config("cliente ref"+ID_Cliente);						

						//direccion
						int bpartnerlocation = DB.getSQLValue(get_TrxName(), "select max(c_bpartner_location_id) from c_bpartner_location where isactive='Y' and c_bpartner_id = "+ID_Cliente);
						if(bpartnerlocation <= 0)
							return "No existe direccion";

						//termino de pago
						int tp = 0;
						if(rs.getString("paymentterm") != null)
						{
							String terminopago = rs.getString("paymentterm");
							int terminopagoint = Integer.valueOf(terminopago);
							String sqlterminopago = "SELECT max(c_paymentterm_id) from c_paymentterm where c_paymentterm_id = "+terminopagoint+" AND AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
							log.config(sqlterminopago);						
							tp = DB.getSQLValue(get_TrxName(), sqlterminopago);
							if(tp <= 0)
								LogError = LogError + "ERROR 104: No existe termino de pago. ";
						}else
							LogError = "ERROR 104: No existe termino de pago. ";						

						//validacion numero de doc
						String DocNo = null;
						//validacion 1
						int ID_Order = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM C_Order WHERE DocStatus IN ('CO','CL','DR','IP','IN') AND DocumentNo LIKE '"+nrodocumento+"'");
						if(ID_Order > 0)
							return "Ya existe este registro en la base de datos "+nrodocumento;
						
		
						
						//Si pasa todas las validaciones, entonces se puede ingresar la Orden.
						
						//Se debe validar si el cliente tiene el credito retenido
						
						MBPartner bp = new MBPartner(getCtx(), ID_Cliente, get_TrxName());
						if(bp.getSOCreditStatus().compareTo("H") == 0 || bp.getSOCreditStatus().compareTo("S") == 0)
							return "Credito Detenido";
						
						if(ID_Cliente > 0 && bpartnerlocation > 0 && tp > 0  && ID_Cliente > 0 )
						{
							ord = new MOrder(getCtx(),0,get_TrxName());
							ord.setAD_Org_ID(rs.getInt("AD_Org_ID"));
							int docnv = Integer.valueOf(tipodocnv);
							int doct = DB.getSQLValue(get_TrxName(), "SELECT max(c_doctype_id) FROM c_doctype WHERE " +
									" c_doctype_id =  '"+docnv+"' ");
							if(doct > 0)
							{
								ord.setC_DocType_ID(doct);
								ord.setC_DocTypeTarget_ID(doct);
							}
							
							else
							{
								ord.setC_DocType_ID(2000124);
								ord.setC_DocTypeTarget_ID(2000124);
								
							}
							if(rs.getTimestamp("created")!=null)
							{
								ord.setDateOrdered(rs.getTimestamp("created"));
								ord.setDateAcct(rs.getTimestamp("created"));
							}
							
								ord.setC_BPartner_ID(ID_Cliente);
								ord.setC_BPartner_Location_ID(bpartnerlocation);
								ord.setBill_Location_ID(bpartnerlocation);
			

							ord.setDocStatus("DR");
							ord.setDeliveryRule("A");
							ord.setM_Warehouse_ID(warehouse);							
							//ord.setDocumentNo(rs.getString("numero_nf"));
							//ord.setDocumentNo(rs.getString("numero_pedido"));
							ord.setDocumentNo(nrodocumento);
							ord.setFreightCostRule("I");
							//obtener id de lista de precios
							String sqlpricelist = "SELECT coalesce(max(m_pricelist_id),0) from m_pricelist where  AD_Client_ID = "+Env.getAD_Client_ID(getCtx())+"" +
									" AND issopricelist = 'Y' " ;
							pr = DB.getSQLValue(get_TrxName(), sqlpricelist);
							if(pr > 0)
								ord.setM_PriceList_ID(pr);
							else
								return "No existe lista de precios";
							
							ord.setC_PaymentTerm_ID(tp);
							ord.setIsSOTrx(true);
							//ord.setSalesRep_ID(100);
							if(bp.get_Value("SalesRep_ID") != null && bp.getSalesRep_ID() > 0)
								ord.setSalesRep_ID(bp.getSalesRep_ID());
							else
								ord.setSalesRep_ID(2059738);
							
							ord.setPOReference("000");

							//Flag para indicar que es una orden desde WS	
							ord.set_CustomColumn("FlagWS", "Y");
							
							//mfrojas 20220927 se agrega paymentrule
							MPaymentTerm payterm = new MPaymentTerm(getCtx(), tp, get_TrxName());
							if(payterm.get_Value("PaymentRule") != null)
								ord.setPaymentRule(payterm.get_Value("PaymentRule").toString());
							
							if(ord.save())
							{
								DB.executeUpdate("UPDATE I_SalesWS SET C_Order_ID = "+ord.get_ID()+" " +
										"WHERE I_SalesWS_ID IN ("+rs.getInt("I_SalesWS_ID")+")",get_TrxName());
							}
							
							
							//@mfrojas INGRESO DE MINOUT
							
							min = new MInOut(getCtx(),0,get_TrxName());
							//Generación de Cabecera de Despacho. 
							min.setDocumentNo(ord.getDocumentNo());
							min.setAD_Org_ID(ord.getAD_Org_ID());
							min.setIsSOTrx(true);
							int minouttype = Integer.valueOf(tipodocmin);
							log.config("tipo doc acopio "+tipodocmin);
							String sqldoctype = "SELECT coalesce(max(c_doctype_id),0) from c_doctype where c_doctype_id = "+minouttype+" " +
									" and AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
							log.config("sql doctype "+sqldoctype);
							int doc = DB.getSQLValue(get_TrxName(), sqldoctype);
							if(doc > 0)
								min.setC_DocType_ID(doc);
							else
								min.setC_DocType_ID();
							
							min.setMovementDate(ord.getDateOrdered());
							min.setDateAcct(ord.getDateOrdered());
							min.setC_BPartner_ID(ord.getC_BPartner_ID());
							min.setC_BPartner_Location_ID(ord.getC_BPartner_Location_ID());
							min.setM_Warehouse_ID(ord.getM_Warehouse_ID());
							min.setDeliveryRule("A");
							min.setFreightCostRule("I");
							min.setDocStatus("DR");
							min.setC_Order_ID(ord.get_ID());
							min.setMovementType("C-");
							//min.setM_RMA_ID(100);
							log.config("orden "+ord.get_ID());
							if(min.save())
							{
								DB.executeUpdate("UPDATE I_SalesWS SET M_InOut_ID = "+min.get_ID()+" " +
										"WHERE I_SalesWS_ID IN ("+rs.getInt("I_SalesWS_ID")+")",get_TrxName());
								log.config("bkn");
							}
							else
								return "No puedo grabar";

							//@mfrojas TERMINO DE MINOUT
							
							
							//@mfrojas INGRESO DE FACTURA
							
							inv = new MInvoice(getCtx(),0,get_TrxName());
							
							inv = new MInvoice(getCtx(), 0, get_TrxName());
							int invoicetype = Integer.valueOf(tipodocinv);

							sqldoctype = "SELECT coalesce(max(c_doctype_id),0) from c_doctype where c_doctype_id = "+invoicetype+" " +
									" and AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
							
							doc = 0;
							doc = DB.getSQLValue(get_TrxName(), sqldoctype);
							if(doc > 0)
							{
								inv.setC_DocType_ID(doc);
								inv.setC_DocTypeTarget_ID(doc);
							}
							else
							{
								//inv.setC_DocType_ID();
								inv.setC_DocTypeTarget_ID();
							}



							inv.setAD_Org_ID(ord.getAD_Org_ID());
							inv.setIsSOTrx(true);
							inv.setM_PriceList_ID(ord.getM_PriceList_ID());
							
							inv.setDocStatus("DR");
							inv.setC_BPartner_ID(ord.getC_BPartner_ID());
							inv.setC_BPartner_Location_ID(ord.getBill_Location_ID());
							
							//int counter = DB.getSQLValue(get_TrxName(), sqlvalidatedoc);
							/*if(counter == 0)
								inv.setDocumentNo(rs.getString("numero_nf"));
							else
							{
								DB.executeUpdate("UPDATE I_ImportInvoiceXMLW SET isabort='Y' WHERE numero_nf='"+rs.getString("numero_nf")+"'",get_TrxName());
								return "Error 201: Ya existe este número de factura ("+rs.getString("numero_nf")+") para el proveedor Solutec";
							}
							*/
							inv.setDateAcct(ord.getDateAcct());
							inv.setDateInvoiced(ord.getDateOrdered());
							inv.setSalesRep_ID(ord.getSalesRep_ID());
							inv.setC_PaymentTerm_ID(ord.getC_PaymentTerm_ID());
							inv.setC_Order_ID(ord.getC_Order_ID());
							
							if(inv.save())
							{
								DB.executeUpdate("UPDATE I_SalesWS SET C_Invoice_ID = "+inv.get_ID()+" " +
										"WHERE I_SalesWS_ID IN ("+rs.getInt("I_SalesWS_ID")+")",get_TrxName());
								log.config("graba factura");
							}
							else
								return "No puedo grabar factura";

							
							//@mfrojas TERMINO DE INGRESO DE FACTURA


							//	
						}
					}					
				
				}
				if(ord != null)
				{
					//generacion de detalle
					MOrderLine oLine = new MOrderLine(ord);
					//se buscan valores necesarios
					//producto
					String sqlst = "SELECT max(m_product_Id) from m_product where value like '"+rs.getString("productvalue")+"' AND AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
					log.config("sql de producto = "+sqlst);
					int prod_id = DB.getSQLValue(get_TrxName(), sqlst);					
					if(prod_id > 0)
					{
						//verificamos si existe prod en la lista de precios
						int existPL = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM M_ProductPrice" +
								" WHERE M_PriceList_Version_ID in(select m_pricelist_version_id from m_pricelist_version where m_pricelist_id = "+pr+")  AND M_Product_ID = "+prod_id);
						if(existPL > 0)
						{
							lineNo += 10;
							oLine.setLine(lineNo);
							oLine.setM_Product_ID(prod_id);
							//oLine.setM_AttributeSetInstance_ID(60000);
							//cantidad
							if(rs.getString("qtyordered") != null)
								oLine.setQty(rs.getBigDecimal("qtyordered"));
							else
								oLine.setQty(Env.ONE);
							//precio
							if(rs.getBigDecimal("price")!=null)
								oLine.setPrice(rs.getBigDecimal("price"));
							else
								oLine.setPrice(Env.ZERO);
							//neto de linea
							/*if(rs.getBigDecimal("valor_total") != null)
								oLine.setLineNetAmt(rs.getBigDecimal("valor_total"));*/					
							//descripcion

							//costo 
							
							if(oLine.save())
							{
								//@mfrojas ingreso de linea de inout.
								
								MInOutLine minl = new MInOutLine(min);
								
								minl.setM_Product_ID(prod_id);
								if(rs.getString("qtyordered") != null)
								{
									minl.setQty(rs.getBigDecimal("qtyordered"));
									minl.setQtyEntered(rs.getBigDecimal("qtyordered"));
									
								}
								else
								{
									minl.setQty(Env.ONE);
									minl.setQtyEntered(Env.ONE);
									
								}
								
								//int loc = DB.getSQLValue(get_TrxName(),"SELECT coalesce(max(m_locator_id),0) from m_locator where m_warehouse_id = "+ord.getM_Warehouse_ID()+" and isactive='Y'");
								int loc = DB.getSQLValue(get_TrxName(), "SELECT coalesce(max(m_locator_id),0) from m_storage " +
										" WHERE m_product_id = "+prod_id+" and m_locator_id in (SELECT m_locator_id from m_locator where m_warehouse_id = "+ord.getM_Warehouse_ID()+")having coalesce(sum(qtyonhand),0) > "+rs.getBigDecimal("qtyordered")); 
								if(loc > 0)
									minl.setM_Locator_ID(loc);
								else
									minl.setM_Locator_ID(1000066);
								
								minl.setC_OrderLine_ID(oLine.getC_OrderLine_ID());

								minl.save();
								
								//@mfrojas termino de linea de inout.
								
								
								
								//@mfrojas ingreso de linea de factura
								
								MInvoiceLine invl = new MInvoiceLine(inv);
								
								invl.setM_Product_ID(oLine.getM_Product_ID());
								invl.setQty(oLine.getQtyOrdered());
								invl.setQtyEntered(oLine.getQtyEntered());
								invl.setQtyInvoiced(oLine.getQtyOrdered());
								invl.setPrice(oLine.getPriceEntered());
								invl.setPriceEntered(oLine.getPriceEntered());
								invl.setPriceActual(oLine.getPriceEntered());
								invl.setDescription(oLine.getDescription());
								invl.setM_InOutLine_ID(minl.get_ID());
								invl.setC_OrderLine_ID(oLine.get_ID());
								invl.setC_Tax_ID(oLine.getC_Tax_ID());
								invl.save();
								//@mfrojas termino de ingreso linea de factura
								log.config("saved?");
							}
						}
						else
							{
								LogError = LogError + "ERROR 302: Producto "+rs.getString("ProductValue")+" no existe en lista de precio ";
								return "ERROR 302: Producto "+rs.getString("ProductValue")+" no existe en lista de precio ";
								
							}
						
					}else
					{
						LogError = LogError + "ERROR 301: Producto con código "+rs.getString("ProductValue");
						return "ERROR 301: Producto con código "+rs.getString("ProductValue");
					}
					if(LogError != null && LogError.trim().length() > 1)
					{
						Hay_Errores = ". Hubo errores en el proceso "+LogError;
						return Hay_Errores;
					}
				}
				lastOrder = OrderFlag;
				
				//se guardan ID para actualizar
				if(rs.getString("I_SalesWS_ID") != null)
					ID_invXML = ID_invXML+","+rs.getString("I_SalesWS_ID");
				
				//mfrojas
				//actualiza la linea actual
				
				DB.executeUpdate("UPDATE I_SalesWS SET C_Invoice_ID = "+inv.get_ID()+", " +
						" M_InOut_ID =  "+min.get_ID()+" , " +
						" C_Order_ID = "+ord.get_ID()+" " + 
						"WHERE I_SalesWS_ID IN ("+rs.getInt("I_SalesWS_ID")+")",get_TrxName());
				log.config("graba factura");

			}
			//se completa ultima nota de venta
			if(ord != null && ord.getDocStatus().compareTo("CO") != 0)
			{
				ord.setDocAction("CO");
				if(ord.processIt ("CO"))
				{
					if(ord.save())
						cant++;
				}
				
				//despacho se completa solo si la marca es YES
				log.config("complete in out "+completeinout);
				
				if(completeinout.compareTo("Y") == 0)
				{
					min.setDocAction("CO");
					min.processIt("CO");
				
					min.save();
				}

				inv.setDocAction("CO");
				inv.processIt("CO");
			
				inv.save();
				
				//commitEx();
				ord = null;
				min = null;
				inv = null;
			}
			
			//actualizamos registros procesados
			DB.executeUpdate("UPDATE I_SalesWS SET processed = 'Y' WHERE I_SalesWS_ID IN ("+ID_invXML+") ",get_TrxName());
			
			rs.close ();
			pstmt.close ();
			pstmt = null;	
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}		
		return "GENERADOS "+cant+" PROCESOS "+Hay_Errores;
	}	//	doIt
	public int GenerateShip(MOrder order)
	{
		MInOut min = new MInOut(getCtx(),0,get_TrxName());
		//Generación de Cabecera de Despacho. 
		min.setDocumentNo(order.getDocumentNo());
		min.setAD_Org_ID(order.getAD_Org_ID());
		min.setIsSOTrx(true);
		String sqldoctype = "SELECT coalesce(max(c_doctype_id),0) from c_doctype where name like '%Despacho%' and AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
		int doc = DB.getSQLValue(get_TrxName(), sqldoctype);
		if(doc > 0)
			min.setC_DocType_ID(doc);
		else
		{
			log.config("No existe tipo de documento de despacho");
			log.config("sql "+sqldoctype);
			return 0;
		}
		
		min.setMovementDate(order.getDateOrdered());
		min.setDateAcct(order.getDateOrdered());
		min.setC_BPartner_ID(order.getC_BPartner_ID());
		min.setC_BPartner_Location_ID(order.getC_BPartner_Location_ID());
		min.setM_Warehouse_ID(order.getM_Warehouse_ID());
		min.setDeliveryRule("A");
		min.setFreightCostRule("I");
		min.setDocStatus("DR");
		min.setC_Order_ID(order.get_ID());
		min.save();
		
		
		
		return 0;		
	}
	public int GenerateInv(MOrder order, MInOut ship)
	{
		return 0;		
	}
}
