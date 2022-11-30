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

import java.math.BigDecimal;
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
import org.compiere.model.*;
import org.compiere.process.SvrProcess;
import org.ofb.process.CXFConnector;
import org.ofb.utils.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *	integracion flavorsys
 *	
 *  @author Italo Niï¿½oles ininoles
 *  @version $Id: FSProyecto.java,v 1.2 19/05/2011 $
 */
public class FSProyecto extends SvrProcess
{	
	/** Properties						*/
	private Properties 		m_ctx;	
	private int p_Projecto_ID = 0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		p_Projecto_ID=getRecord_ID();
		m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		X_C_ProjectOFB pro = new X_C_ProjectOFB(m_ctx, p_Projecto_ID, get_TrxName());
		MBPartner cli = new MBPartner(getCtx(), pro.getC_BPartner_ID(), get_TrxName());
		Timestamp dataEntrada = pro.getCreated();
		if(dataEntrada == null)
			dataEntrada = DateUtils.today();
		Timestamp dataEntrega = (Timestamp) pro.get_Value("DateFinish") ;
		if(dataEntrega == null)
			dataEntrega = DateUtils.today();
		BigDecimal pvalor = (BigDecimal)pro.get_Value("PlannedMarginAmt");
		if(pvalor == null)
			pvalor = Env.ZERO;
		BigDecimal pVolumen = (BigDecimal)pro.get_Value("PlannedAmt");
		if(pVolumen == null)
			pVolumen = Env.ZERO;
		//String statuslegal = MRefList.getListName(getCtx(), 1000114, pro.get_ValueAsString("status_legal"));
		String statuslegal = pro.get_ValueAsString("status_legal");
		if(statuslegal != null && statuslegal.compareTo("No Aplica") ==0)
			statuslegal = "NA";			
		BigDecimal costo_max = (BigDecimal)pro.get_Value("costo_dosis");
		if(costo_max == null)
			costo_max = Env.ZERO;
		Timestamp dataDepto = (Timestamp) pro.get_Value("DateContractSignature") ;
		if(dataDepto == null)
			dataDepto = DateUtils.today();
		BigDecimal potSuceso = (BigDecimal)pro.get_Value("CompletePercentage");
		String salida = llamarWSProyecto(Integer.toString(pro.get_ID()),cli.getValue()+"-"+cli.get_ValueAsString("Digito"),
				pro.getSalesRep().getName(),convertDate(dataEntrada),convertDate(dataEntrega),pvalor,pro.getName(),
				pro.get_ValueAsString("proceso_productivo_cliente"),pVolumen,statuslegal,pro.getNote(),costo_max,pro.get_ValueAsString("Aplicacion_solutec"),convertDate(dataDepto),
				convertDate(dataDepto),pro.getAD_User().getName(),pro.get_ValueAsString("estado_aplicacion"),Integer.toString(potSuceso.intValue()));
		pro.set_CustomColumn("StatusWS",salida);
		pro.set_CustomColumn("iswebservicefs",true);
		pro.saveEx(get_TrxName());
		return salida;
	}	//	doIt
		
        
    private String llamarWSProyecto(String prAdempiere,String rut,String codVendedor,String dataEntrada,
    		String dataEntrega,BigDecimal PValor,String desProjeto,String codAplicacion,BigDecimal PotVolume,
    		String statusLegal,String briefing,BigDecimal custoMax,String depto,String dataDepto,
    		String dataStatus,String usuario,String tipoProduto,String potSucesso) throws Exception
    {
    	String token = "";	
    	final String request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:ProjetoIntIntf-IProjetoInt\">"
    			+ "   <soapenv:Header/>"
    			+ "   <soapenv:Body>"
    			+ "      <urn:InserirProyecto soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
    			+ "         <numero_projeto_adempiere xsi:type=\"xsd:string\">"+prAdempiere+"</numero_projeto_adempiere>"
    			+ "         <rut_cliente xsi:type=\"xsd:string\">"+rut+"</rut_cliente>"
    			+ "         <codigo_vendedor_adempiere xsi:type=\"xsd:string\">"+codVendedor+"</codigo_vendedor_adempiere>"
    			+ "         <data_entrada xsi:type=\"xsd:string\">"+dataEntrada+"</data_entrada>"
    			+ "         <data_entrega xsi:type=\"xsd:string\">"+dataEntrega+"</data_entrega>"
    			+ "         <Potencial_Valor xsi:type=\"xsd:double\">"+PValor+"</Potencial_Valor>"
    			+ "         <descricao_projeto xsi:type=\"xsd:string\">"+desProjeto+"</descricao_projeto>"
    			+ "         <codigo_aplicacao xsi:type=\"xsd:string\">"+codAplicacion+"</codigo_aplicacao>"
    			+ "         <Potencial_Volume xsi:type=\"xsd:double\">"+PotVolume.doubleValue()+"</Potencial_Volume>"
    			+ "         <status_legal>"+statusLegal+"</status_legal>"
    			+ "         <briefing>"+briefing+"</briefing>"
    			+ "         <custo_max>"+custoMax.doubleValue()+"</custo_max>"
    			+ "         <depto>"+depto+"</depto>"
    			+ "         <data_depto>"+dataDepto+"</data_depto>"
    			+ "         <data_status>"+dataStatus+"</data_status>"
    			+ "         <usuario>"+usuario+"</usuario>"
    			+ "         <tipo_produto>"+tipoProduto+"</tipo_produto>"
    			+ "         <potencial_sucesso>"+potSucesso+"</potencial_sucesso>"
    			+ "      </urn:InserirProyecto>"
    			+ "   </soapenv:Body>"
    			+ "</soapenv:Envelope>";

    			Source response = null;
    			
    			log.config("request: "+request );
    					
    			//try
    			//{				
    				final CXFConnector wsc = new CXFConnector();					
    				wsc.setSoapAction("urn:ProjetoIntIntf-IProjetoInt#InserirProyecto");
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
			if(childNode.getNodeName().equals(("NS1:InserirProyectoResponse")))
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
    	//MES
    	if((fecha.getMonth()+1) < 10)
    		salida = "0"+(fecha.getMonth()+1);
    	else
    		salida = Integer.toString((fecha.getMonth()+1));
    	//dia
    	if(fecha.getDate() < 10)
    		salida =salida+"/0"+fecha.getDate();
    	else
    		salida =salida+"/"+Integer.toString(fecha.getDate());
    	//año
    	salida = salida+"/"+(fecha.getYear()+1900);
    	return salida;
    }
}	//	InvoiceCreateInOut
