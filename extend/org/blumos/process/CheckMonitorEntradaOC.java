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
package org.blumos.process;

import java.sql.Timestamp;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.soap.SOAPBinding;

import org.compiere.util.*;
import org.check.api.CheckAPI;
import org.compiere.model.*;
import org.compiere.process.SvrProcess;
import org.ofb.process.CXFConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *	integracion check para blumos
 *	
 *  @author Italo Niï¿½oles ininoles
 *  @version $Id: FSProyecto.java,v 1.2 19/05/2011 $
 */
public class CheckMonitorEntradaOC extends SvrProcess
{	
	/** Properties						*/
	private Properties 		m_ctx;	
	private int ID_Order = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ID_Order=getRecord_ID();
		m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		/*MOrder order = new MOrder(m_ctx, ID_Order, get_TrxName());
		MOrderLine[] lines = order.getLines(true, null);	//	Line is default
		String salida = "";
		String salidaHead = "";
		for (int i = 0; i < lines.length; i++)
		{
			MOrderLine line = lines[i];			
			/*salida = llamarWSProyecto(line);
			salidaHead = salidaHead+salida;			
			line.set_CustomColumn("StatusWS",salida);
		}*/
		/*order.set_CustomColumn("iswebservicefs",true);
		order.saveEx(get_TrxName());*/
		CheckAPI check = new CheckAPI();
		return check.check();
	}	//	doIt
		
        
    private String llamarWSProyecto(MOrderLine oLine) throws Exception
    {
    	MOrder order = new MOrder(getCtx(), oLine.getC_Order_ID(), get_TrxName());
    	MBPartner bPart = new MBPartner(getCtx(), order.getC_BPartner_ID(), get_TrxName());
    	String token = "";	
    	final String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">"
    			+ "<soapenv:Header/>"
    			+ "	  <soapenv:Body>"
    			+ "      <tem:MonitorEntrada>"
    			+ "         <!--Optional:-->"
    			+ "         <tem:value>"
    			+ "            <!--Zero or more repetitions:-->"
    			+ "            <tem:PendienteRecepcionInsertDTO>"
    			+ "               <tem:pre_uid_ins>0B816008-3B32-4021-A988-AD8021229862</tem:pre_uid_ins>"
    			+ "               <tem:pre_uid_erp>0B816008-3B32-4021-A988-AD8021229862</tem:pre_uid_erp>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_id_erp>"+oLine.get_ID()+"</tem:pre_id_erp>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_cod_tdo>"+order.getC_DocType().getName()+"</tem:pre_cod_tdo>"
    			+ "               <tem:pre_eta_pre>"+convertDate((Timestamp)order.get_Value("ETA_BODEGA"))+"</tem:pre_eta_pre>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_num_doc>"+order.getDocumentNo()+"</tem:pre_num_doc>"
    			+ "               <tem:pre_lin_doc>"+oLine.getLine()+"</tem:pre_lin_doc>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_cod_pro>"+bPart.getValue()+"-"+bPart.get_ValueAsString("DIGITO")+"</tem:pre_cod_pro>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_des_pro>"+bPart.getName()+"</tem:pre_des_pro>"
    			+ "               <tem:pre_fec_emi>"+convertDate(order.getDateOrdered())+"</tem:pre_fec_emi>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_cod_mat>"+oLine.getM_Product().getValue()+"</tem:pre_cod_mat>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_des_mat>"+oLine.getM_Product().getName()+"</tem:pre_des_mat>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_uni_mat>"+oLine.getM_Product().getC_UOM().getUOMSymbol()+"</tem:pre_uni_mat>"
    			+ "               <tem:pre_can_mat>"+oLine.getQtyOrdered().intValue()+"</tem:pre_can_mat>"
    			+ "               <tem:pre_ent_mat>"+oLine.getQtyOrdered().intValue()+"</tem:pre_ent_mat>"
    			+ "               <tem:pre_pdt_mat>0</tem:pre_pdt_mat>"
    			+ "               <tem:pre_val_pre>"+oLine.getPriceActual()+"</tem:pre_val_pre>"
    			+ "               <tem:pre_val_pdt>0</tem:pre_val_pdt>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_obs_pre>"+oLine.getDescription()+"</tem:pre_obs_pre>"
    			+ "               <tem:pre_ffa_mat>"+convertDate(order.getDateOrdered())+"</tem:pre_ffa_mat>"
    			+ "               <tem:pre_fve_mat>"+convertDate(order.getDateOrdered())+"</tem:pre_fve_mat>"
    			+ "               <!--Optional:-->"
    			+ "               <tem:pre_lot_mat>0</tem:pre_lot_mat>"
    			+ "            </tem:PendienteRecepcionInsertDTO>"
    			+ "         </tem:value>"
    			+ "      </tem:MonitorEntrada>"
    			+ "   </soapenv:Body>"
    			+ "</soapenv:Envelope>";

    			Source response = null;
    			
    			log.config("request: "+request );
    					
    			//try
    			//{				
    				final CXFConnector wsc = new CXFConnector();					
    				wsc.setSoapAction("http://tempuri.org/MonitorEntrada");
    				wsc.setRequest(request);
    				//wsc.setBinding(SOAPBinding.SOAP11HTTP_BINDING);
    				wsc.setBinding(SOAPBinding.SOAP12HTTP_BINDING);
    				wsc.setEndpointAddress("https://checkwebservice-training-blumos.azurewebsites.net/CheckWMSWebService.asmx");
    				//wsc.setEndpointAddress("http://checkwebservice-training-blumos.azurewebsites.net/CheckWMSWebService.asmx");
    				wsc.setServiceName("CheckWMSWebService");
    				//wsc.setPortName("CheckWMSWebServiceSoap");
    				wsc.setPortName("CheckWMSWebServiceSoap12");    				
    				//wsc.setTargetNS("http://tempuri.org/");
    				wsc.setTargetNS("http://microsoft.com/wsdl/types/");
    				wsc.executeConnector();
    				response = wsc.getResponse(); 
    				
    			//}
    			//catch(Exception e)
    			//{
    			//	throw new Exception("No se ha podido establecer conexion con el Servicio de Facturacion");
    			//}
    				
    				
    			try
    			{
    				//comienza la lectura del xml recibido
    				DocumentBuilderFactory.newInstance().newDocumentBuilder();
    				SAXSource output = (SAXSource) response;
    				Transformer tf = TransformerFactory.newInstance().newTransformer();
    			
    				DOMResult result = new DOMResult();
    				tf.transform(output, result);
    				Document doc = (Document) result.getNode();
    				
    				Node datos = findReturn(doc.getChildNodes().item(0)); 
    									
    				if(datos!=null){
    					NodeList att = datos.getChildNodes(); 
    					for(int x=0;x<att.getLength();x++)
    					{	
    						log.config(att.item(x).getLocalName());
    						if(att.item(x).getLocalName().equals("Detalle") || att.item(x).getNodeName().equals("Detalle"))
    							token=att.item(x).getFirstChild().getNodeValue();
    					} // fin for return
    				}//FIN DATOS
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    			}
    		return token;    			
    }  
    public Node findReturn(Node node) {
		Node value = null;
		NodeList list = node.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			// Get child node
			Node childNode = list.item(i);
			if(childNode.getNodeName().equals(("MonitorEntradaResult")))
			{
				value = childNode;
				break;
			}
			value=findReturn(childNode);
		}
		return value;
    }
    public String findText(String cadena, String fragmentoIni, String fragmentoFin)
    {
    	String newText = "";
    	boolean flag;
    	int indiceIni = 0;
    	int indiceFin = 0;
    	
    	flag = cadena.contains(fragmentoIni);
    	
    	if (flag)    		
    	{
    		indiceIni = cadena.indexOf(fragmentoIni);
    		indiceIni = indiceIni + fragmentoIni.length();    		
    		indiceFin = cadena.indexOf(fragmentoFin);
    		newText = cadena.substring(indiceIni, indiceFin);
    	}
    	else
    		newText = "Error - No existe Cadena Especificada";    	
    	
    	return newText;
    }		
    public String convertDate(Timestamp fecha)
    {
    	String salida = "";    	
    	//año
    	salida = salida+(fecha.getYear()+1900);
    	salida = salida+"-";
    	//MES
    	if((fecha.getMonth()+1) < 10)
    		salida =salida+"0"+(fecha.getMonth()+1);
    	else
    		salida =salida+Integer.toString((fecha.getMonth()+1));
		salida = salida+"-";
    	//dia
    	if(fecha.getDate() < 10)
    		salida =salida+"0"+fecha.getDate();
    	else
    		salida =salida+Integer.toString(fecha.getDate());
    	salida=salida+"T00:00:00.0000";
    	return salida;
    }
}	//	InvoiceCreateInOut
