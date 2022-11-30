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

import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.soap.SOAPBinding;

import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.process.SvrProcess;
import org.ofb.process.CXFConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *	integracion flavorsys
 *	
 *  @author Italo Ni�oles ininoles
 *  @version $Id: FSProyecto.java,v 1.2 19/05/2011 $
 */
public class FSChat extends SvrProcess
{	
	/** Properties						*/
	private Properties 		m_ctx;	
	private int p_Seguimiento_ID = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		p_Seguimiento_ID=getRecord_ID();
		m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		X_T_SEGUIMIENTO seg = new X_T_SEGUIMIENTO(getCtx(), p_Seguimiento_ID, get_TrxName());
		X_C_ProjectOFB pro = new X_C_ProjectOFB(m_ctx, seg.getC_ProjectOFB_ID(), get_TrxName());		
		MUser user = new MUser(getCtx(), seg.get_ValueAsInt("AD_USER_ID_COMENTARIO"), get_TrxName());		
		String salida = llamarWSChat(Integer.toString(pro.get_ID()),user.getName(),seg.getDescription());
		seg.set_CustomColumn("StatusWS",salida);
		seg.set_CustomColumn("iswebservicefs",true);
		seg.saveEx(get_TrxName());
		return salida;
	}	//	doIt
		
        
    private String llamarWSChat(String prAdempiere,String user,String msg) throws Exception
    {
    	String token = "";	
    	final String request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:ProjetoIntIntf-IProjetoInt\">"
    			+ "<soapenv:Header/>"
    			+ "<soapenv:Body>"
    			+ "<urn:InserirChat soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
    			+ "<numero_projeto_adempiere xsi:type=\"xsd:string\">"+prAdempiere+"</numero_projeto_adempiere>"
    			+ "<user xsi:type=\"xsd:string\">"+user+"</user>"
    			+ "<mensagem xsi:type=\"xsd:string\">"+msg+"</mensagem>"
    			+ "</urn:InserirChat>"
    			+ "</soapenv:Body>"
    			+ "</soapenv:Envelope>";

    			Source response = null;
    			
    			log.config("request: "+request );
    			//try
    			//{				
    				final CXFConnector wsc = new CXFConnector();					
    				wsc.setSoapAction("urn:ProjetoIntIntf-IProjetoInt#InserirChat");
    				wsc.setRequest(request);
    				wsc.setBinding(SOAPBinding.SOAP11HTTP_BINDING);
    				wsc.setEndpointAddress("http://100.100.100.111:8080/soap/IProjetoInt");
    				wsc.setServiceName("IProjetoIntservice");
    				wsc.setPortName("IProjetoIntPort");
    				wsc.setTargetNS("urn:ProjetoIntIntf-IProjetoInt");
    				wsc.executeConnector();
    				response = wsc.getResponse(); 
    				
    			//}
    			//catch(Exception e)
    			//{
    				//throw new Exception("No se ha podido establecer conexion con el Servicio de Facturacion"+"-"+e.toString());
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
    						if(att.item(x).getLocalName().equals("return") || att.item(x).getNodeName().equals("return"))
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
			if(childNode.getNodeName().equals(("NS1:InserirChatResponse")))
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
}	//	InvoiceCreateInOut
