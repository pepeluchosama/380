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
import org.compiere.model.MConversionRate;
import org.compiere.model.X_I_OrderMC;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;

/**
 *  
 *
 *	@author mfrojas
 *	@version $Id: ProcessMINSALChileCompraStatus.java,v 1.2 2006/07/30 00:51:02 mfrojas Exp $
 */
public class ProcessMINSALChileCompraStatusV2 extends SvrProcess
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
		
		//Se debe buscar todas las OC de adempiere, que se encuentren en un estado distinto a borrador, 
		//Y que su estado sea el del parametro . 
		
		
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
		
		String sqlcount = "SELECT count(1) from c_order WHERE docstatus not in "
				+ " ('VO', 'CL') and isactive='Y' and issotrx='N' "
				+ " AND (statuscc is null or statuscc != '12') and ad_Client_id = "+Env.getAD_Client_ID(getCtx());
				
		int conteo = DB.getSQLValue(get_TrxName(), sqlcount);
		if(conteo == 0)
			return "No hay ordenes";
		
		String sql = "SELECT c_order_id, documentno from c_order where docstatus not in "
				+ " ('VO', 'CL') and isactive='Y' and issotrx='N' "
				+ " AND (statuscc is null or statuscc != '12') and ad_Client_id = "+Env.getAD_Client_ID(getCtx());

		if(p_DocNo != null && p_DocNo.length()>5)
			sql = sql.concat(" AND documentno like '"+p_DocNo+"'");
		log.config("sql "+sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		pstmt = DB.prepareStatement(sql, get_TrxName());
		rs = pstmt.executeQuery();
		
		String docno = "";
		int ordid = 0;
		
		//Definicion de variables json y stream
		InputStream is = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		MOrder ordaux = null;
		int orders[] = new int[conteo];
		int i = 0;
		while(rs.next())
		{
			orders[i] = rs.getInt("c_order_id");
			log.config("orden "+i+" = "+orders[i]);
			i++;
			
		}
		log.config("tamaño "+orders.length);
		pstmt.close();
		rs.close();
		
		//Todas las ordenes estan en el array. 
		
		for(int j = 0; j<orders.length; j++)
		{
			
			log.config("order "+orders[j]);
			if(orders[j] < 1)
				continue;
			ord = new MOrder(getCtx(), orders[j], get_TrxName());
			//Mientras exista resultados, se debe consultar a la OC de mercado publico por su estado
			
			docno = ord.getDocumentNo();
			log.config("docno "+docno);
			
			if(docno.length()<9)
				continue;
			
			log.config("docno "+docno);
			website = "https://api.mercadopublico.cl/servicios/v1/publico/ordenesdecompra.json?codigo="+docno+"&ticket="+p_Token;
			is = null;
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
				
			}
			finally
			{
				
			}
			log.config("websitepost = "+website);
			log.config("validator "+validator);
		
			if(validator == 1)
				continue;
			rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
		
			sb = new StringBuilder();
			int counter = 0;
		
			while ((counter = rd.read()) != -1)
				sb.append((char)counter);
		
			jsonText = sb.toString();
			log.config("jsonText = "+jsonText);
		
			json = new JSONObject(jsonText);
			jsonarr = json.getJSONArray("Listado");
	    	    
			//mfrojas 20201023
			//Se debe obtener el estado de la OC. Si esta enviada a proveedor, se puede asociar. 
			//Si no, no se asocia.
	    
			codigoestado = null;
			codigoestado = jsonarr.getJSONObject(0).get("CodigoEstado").toString();
			if(codigoestado == null)
				continue;

			
			ord.set_CustomColumn("StatusCC", codigoestado);
			//Si el estado es 6 (ACEPTADA), entonces la OC pasa a estado completo
			
			if(codigoestado.compareTo("6") == 0 || codigoestado.compareTo("4") == 0 || codigoestado.compareTo("5") == 0 || codigoestado.compareTo("12") == 0)
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
			}
			ord.save();
			
		}


		return "";
	}	//	doIt
	
	
}	//	CopyFromOrder
