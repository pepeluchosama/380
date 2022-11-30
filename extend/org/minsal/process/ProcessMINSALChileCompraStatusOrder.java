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
package org.minsal.process;

import java.math.BigDecimal;
import java.util.logging.Level;

import org.json.JSONObject;
import org.json.JSONArray;

import org.compiere.process.*;
import org.compiere.util.DB;
import org.compiere.util.Env;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MConversionRate;
import org.compiere.model.X_I_OrderMC;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MLocation;

/**
 *  
 *
 *	@author mfrojas
 *	@version $Id: ProcessMINSALChileCompraStatus.java,v 1.2 2006/07/30 00:51:02 mfrojas Exp $
 */
public class ProcessMINSALChileCompraStatusOrder extends SvrProcess
{
	/**	The Order				*/
	private String	p_DocNo;
	private String  p_Token;
	private String  p_Status;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("DocNo"))
				p_DocNo = para[i].getParameterAsString();
			else if(name.equals("Token"))
				p_Token = para[i].getParameterAsString();
			else if(name.equals("Status"))
				p_Status = para[i].getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Execute procedure to look for PO in ChileCompra
	 *  @return qty of registers processed
	 *  @throws 
	 */
	protected String doIt() throws Exception
	{
		
		//Este proceso, lo que hace es ir a buscar una OC específica a chile compra, y luego, guardarla en adempiere. 
	    String sql = "SELECT count(1) from c_order where documentno like '"+p_DocNo+"' and isactive='Y' " +
	    		" AND issotrx='N'";
	    int val = DB.getSQLValue(get_TrxName(), sql);
	    if(val > 0)
	    	return "La orden "+p_DocNo+" ya se encuentra en el sistema";
		
		
		log.config("TOKEN = "+p_Token);
		log.config("ORDEN = "+p_DocNo);
		log.config("STATUS = "+p_Status);
		//JSONParser parser = new JSONParser();
	    //JsonParser jp = new JsonParser();
		//String date_s = "01-01-2017";
		//log.config("date_s "+date_s);
		//String website = "http://mindicador.cl/api/"+moneda+"/"+date_s+"";
		
		JSONObject json = null;
		JSONArray jsonarr = null;
		String codigoestado = null;
		MOrder ord = null;
		String jsonText = null;
		String website = null;
		//Consulta inicial
		
		//Todas las ordenes estan en el array. 
		

			website = "https://api.mercadopublico.cl/servicios/v1/publico/ordenesdecompra.json?codigo="+p_DocNo+"&ticket="+p_Token;
			InputStream is = null;
			log.config("websitepre = "+website);
			int validator = 0;
			try
			{
				is = new URL(website).openStream();
			}
			catch(Exception e)
			{
				log.config(" msg "+e.toString());
				validator = 1;
				e = null;
				return "No se encuentra la orden ingresada";
				
			}
			finally
			{
				
			}
			log.config("websitepost = "+website);
			log.config("validator "+validator);
		

			BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
		
			StringBuilder sb = new StringBuilder();
			int counter = 0;
		
			while ((counter = rd.read()) != -1)
				sb.append((char)counter);
		
			jsonText = sb.toString();
			log.config("jsonText = "+jsonText);
		
			json = new JSONObject(jsonText);
			jsonarr = json.getJSONArray("Listado");
	    	   
			
			codigoestado = null;
			try
			{
				codigoestado = jsonarr.getJSONObject(0).get("CodigoEstado").toString();
			}catch(Exception e)
			{
				log.config("errormsg = "+e.toString());
				return "No se encuentra Orden";
			}
		   
		    String Codigo = jsonarr.getJSONObject(0).get("Codigo").toString();
		    String Nombre = jsonarr.getJSONObject(0).get("Nombre").toString();
		    String Descripcion = jsonarr.getJSONObject(0).get("Descripcion").toString();
		    
			
			ord = new MOrder(getCtx(), 0, get_TrxName());
			ord.setDocumentNo(p_DocNo);
			ord.setC_DocTypeTarget_ID(2000108);
			ord.setIsSOTrx(false);
			ord.setDocAction("CO");
			ord.setAD_Org_ID(2000344);
			

			//ver si existe el bpartner. Si no, crearlo

			String Proveedor = jsonarr.getJSONObject(0).get("Proveedor").toString();
			log.config("Proveedor "+Proveedor);
			
		    
		    JSONObject Prov = new JSONObject(jsonarr.getJSONObject(0).get("Proveedor").toString());
		    
		    
		    String Nombre2 = Prov.getString("Nombre");
		    String Rut = Prov.getString("RutSucursal");
		    
			codigoestado = jsonarr.getJSONObject(0).get("CodigoEstado").toString();

		    
		    //Se quitan los puntos y el digito verificador del rut
		    String Rut2 = Rut.replace(".", "");
		    Rut2 = Rut2.substring(0, Rut2.length()-2);

			log.config("Proveedor "+Proveedor);
			log.config("Proveedor "+Nombre2);
			log.config("Rut Proveedor "+Rut2);
			
			sql = "SELECT coalesce(max(c_bpartner_id),0) from c_bpartner WHERE value like '"+Rut2+"'";
			int bp = DB.getSQLValue(get_TrxName(), sql);
			int direccion = 0;
			if(bp == 0)
			{
				//Proveedor no existe, se debe crear
				MBPartner mbp = new MBPartner(getCtx(), 0, get_TrxName());
				mbp.setValue(Rut2);
				mbp.setName(Nombre2);
				
				mbp.setName2(Nombre2);
				mbp.set_CustomColumn("PartnerType","J");
				//mbp.set_CustomColumn("name4",Nombre2);
				mbp.setC_BP_Group_ID(2000002);
				mbp.setIsVendor(true);
				mbp.setIsCustomer(true);
				mbp.save();
				bp = mbp.get_ID();
				//se crea la direccion
				
				MBPartnerLocation bploc = new MBPartnerLocation(getCtx(), 0, get_TrxName());
				MLocation loc = new MLocation(getCtx(), 0, get_TrxName());
				loc.setAddress1(Prov.getString("Direccion"));
				loc.setAddress2(Prov.getString("Comuna"));
				loc.setAddress3(Prov.getString("Comuna"));
				loc.setAddress4(Prov.getString("Comuna"));
				loc.setCity(Prov.getString("Region"));
				loc.setC_Country_ID(152);
				loc.save();
				
				bploc.setC_BPartner_ID(bp);
				bploc.setC_Location_ID(loc.get_ID());
				bploc.setName("DireccionProveedor");
				bploc.save();
				direccion = bploc.get_ID();
				
			}
			
			
			ord.setC_BPartner_ID(bp);
			if(direccion > 0)
				ord.setC_BPartner_Location_ID(direccion);

			ord.set_CustomColumn("StatusCC", codigoestado);
			ord.save();
			
			//Lineas
			
		    JSONObject line = new JSONObject(jsonarr.getJSONObject(0).get("Items").toString());
		    JSONArray jsonarrline = line.getJSONArray("Listado");
		    
		    BigDecimal PrecioNeto = Env.ZERO;
		    String Producto = null;
		    String EspProveedor = null;
		    String CodProd = null;
		    BigDecimal Cantidad = Env.ZERO;

		    int prod = 0;
		    
			for (int i = 0; i < jsonarrline.length(); i++)
			{
			    prod = 0;
				PrecioNeto = new BigDecimal(jsonarrline.getJSONObject(i).get("PrecioNeto").toString());
			    
			    Producto = jsonarrline.getJSONObject(i).get("Producto").toString();
			    EspProveedor = jsonarrline.getJSONObject(i).get("EspecificacionProveedor").toString();
			    CodProd = jsonarrline.getJSONObject(i).get("CodigoProducto").toString();
			    Cantidad = new BigDecimal(jsonarrline.getJSONObject(i).get("Cantidad").toString());
			    log.config("Precio neto "+PrecioNeto);
			    log.config("Producto "+Producto);
			    log.config("EspProveedor "+EspProveedor);
			    
			    MOrderLine omc = new MOrderLine(getCtx(), 0, get_TrxName());
			    

			    omc.setAD_Org_ID(2000006);
			    omc.setDescription("Producto: ".concat(Producto.concat(" \n- ").concat(Descripcion)));
			    omc.setQtyEntered(Cantidad);
			    omc.setQtyOrdered(Cantidad);
			    omc.setPriceEntered(PrecioNeto);
			    omc.setPriceActual(PrecioNeto);
			    //omc.setQtyReserved(Cantidad);
			    //Producto
			    //Existe el producto?
			    sql = "SELECT coalesce(max(m_product_id),0) from m_product where value like '"+CodProd+"' and isactive='Y' AND " +
			    		" AD_Client_ID = "+Env.getAD_Client_ID(getCtx());
			    
			    prod = DB.getSQLValue(get_TrxName(), sql);
			    if(prod > 0)
			    	omc.setM_Product_ID(prod);
			    else
			    {
			    	//Si el producto no existe, se buscara en algun codigo generico
			    	sql = "SELECT coalesce(max(m_product_id),0) from c_bpartner_product where vendorproductno like '"+CodProd+"'";
			    	prod = DB.getSQLValue(get_TrxName(), sql);
			    	//Si existe en codigo generico, entonces se utiliza el productid asociado
			    	if(prod > 0)
			    		omc.setM_Product_ID(prod);
			    	else
			    	{
			    		sql = "SELECT coalesce(max(m_product_id),0) from m_product where value like '%AUX%'";
			    		prod = DB.getSQLValue(get_TrxName(), sql);
			    		omc.setM_Product_ID(prod);
			    	}
			    }
			    //omc.setProductName(Producto);
			    
			    omc.setC_BPartner_ID(bp);
			   // omc.setC_BPartner_Location_ID(direccion);
			    omc.setC_Order_ID(ord.get_ID());
			    //Campos auxiliares de codigo y nombre de producto
			    omc.set_CustomColumn("ProductValueMP", CodProd);
			    omc.set_CustomColumn("ProductNameMP", Producto);
			    omc.setC_UOM_ID(100);
			    omc.setC_Tax_ID(2000003);
			    omc.save();
			}

			
			//Si el estado es 6 (ACEPTADA), entonces la OC pasa a estado completo
			
/*			if(codigoestado.compareTo("6") == 0 || codigoestado.compareTo("4") == 0 || codigoestado.compareTo("5") == 0 || codigoestado.compareTo("12") == 0)
			{
				if(ord.getDocStatus().compareTo("CO") != 0)
				{	
					ord.setDocStatus("IP");
					ord.processIt("CO");
				}
			}
			else if(codigoestado.compareTo("9") == 0)
			{
				//anular
				ord.voidIt();
			}*/
			ord.save();
			
		


		return " Orden generada ";
	}	//	doIt
	
	
}	//	CopyFromOrder
