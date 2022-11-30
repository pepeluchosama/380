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
package org.clinicacolonial.process;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPBinding;

import org.compiere.model.MBPartner;
import org.compiere.model.MCity;
import org.compiere.model.MOrg;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MUser;
import org.compiere.model.X_CC_Hospitalization;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.ofb.process.CXFConnector;
import org.ofb.utils.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
/**
 *	
 *  @author Italo Ni�oles ininoles
 *  @version $Id: WSBionet.java $
 */
public class WSBionetFromReq extends SvrProcess
{	
	/** Properties						*/
	//private Properties 		m_ctx;	
	public String urlPdf = "";
	private int p_Requisition_ID =0;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		p_Requisition_ID=getRecord_ID();
		//m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		//Se ejecutara directamente desde el menu para pruebas
		String ret = "";
		if(p_Requisition_ID > 0)
		{
			MRequisition req = new MRequisition(getCtx(), p_Requisition_ID, get_TrxName());
			ret = llamarWSAcceptMessage(req); 
		}
		return ret;
	}	//	doIt
	  
    private String llamarWSAcceptMessage(MRequisition req) throws Exception
    {	
    	String msg = "";    	
    	MBPartner bp = new MBPartner(getCtx(), req.get_ValueAsInt("C_BPartner_ID"), get_TrxName());
    	Timestamp birthday = (Timestamp)bp.get_Value("Birthday");
    	/*String bDay = Integer.toString(birthday.getYear()+1900);
    	bDay=bDay+(birthday.getMonth()+1);
    	bDay=bDay+birthday.getDate();*/
    	String bDay = new SimpleDateFormat("yyyyMMdd").format(birthday);
    	String gender = "M";
    	if(bp.get_ValueAsString("gender").compareTo("02") ==0)
    		gender = "F";
    	else if(bp.get_ValueAsString("gender").compareTo("03") ==0)
    		gender = "U";
    	else if(bp.get_ValueAsString("gender").compareTo("99") ==0)
    		gender = "U";
    	//comuna
    	String comuna="Santiago";
    	String pais = "Chile";
    	int C_City = bp.get_ValueAsInt("C_City_ID");
    	if(C_City > 0)
    	{
    		MCity city = new MCity(getCtx(), C_City, get_TrxName());
    		comuna = city.getName();
    		pais = city.getC_Country().getName();
    	}
    	X_CC_Hospitalization hosp = new X_CC_Hospitalization(getCtx(),req.get_ValueAsInt("CC_Hospitalization_ID") , get_TrxName());
    	//MOrg org = new MOrg(getCtx(), hosp.getAD_Org_ID(), get_TrxName());
    	String dateReq = Integer.toString(req.getCreated().getYear()+1900);
    	if((req.getCreated().getMonth()+1) >= 10)
    		dateReq=dateReq+(req.getCreated().getMonth()+1);
    	else
    		dateReq=dateReq+"0"+(req.getCreated().getMonth()+1);    		
    	if(req.getCreated().getDate() >= 10)
    		dateReq=dateReq+req.getCreated().getDate();
    	else
    		dateReq=dateReq+"0"+req.getCreated().getDate();
    	if(req.getCreated().getHours() >=10)
    		dateReq=dateReq+req.getCreated().getHours();
    	else
    		dateReq=dateReq+"0"+req.getCreated().getHours();
    	if(req.getCreated().getMinutes() >=10)
    		dateReq=dateReq+req.getCreated().getMinutes();
    	else
    		dateReq=dateReq+"0"+req.getCreated().getMinutes();
    	if(req.getCreated().getSeconds() >=10)
    		dateReq=dateReq+req.getCreated().getSeconds();
    	else
    		dateReq=dateReq+"0"+req.getCreated().getSeconds();
    	MUser docUser = new MUser(getCtx(), Env.getAD_User_ID(getCtx()),get_TrxName());
    	MBPartner doctor = new MBPartner(getCtx(), docUser.getC_BPartner_ID(), get_TrxName());
    	//nueva llamada 15-03-2022
    	String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.connectors.connect.mirth.com/\">"
    			+ "<soapenv:Header/>"
    			+ "<soapenv:Body>"
    			+ "<ws:acceptMessage>"
    			+ "<!-- Optional: -->"
    			+ "<arg0>"
    			+ "<![CDATA["
    			+ "<OrdenDeLaboratorio>"
    			+ "<IdentificacionDePaciente>"
    			+ "<Nombres>"+bp.get_ValueAsString("Name1")+"</Nombres>"
    			+ "<Apellidos>"+bp.getName2()+" "+bp.get_ValueAsString("Name3")+"</Apellidos>"
    			+ "<Rut>"+bp.getValue()+"-"+bp.get_ValueAsString("Digito")+"</Rut>"
    			+ "<Fnacimiento>"+bDay+"</Fnacimiento>"
    			+ "<Genero>"+gender+"</Genero> <!-- M,F,U -->"
    			+ "<Direccion>"+bp.get_ValueAsString("Address1")+"</Direccion>"
    			+ "<Ciudad>"+comuna+"</Ciudad>"
    			+ "<Pais>"+pais+"</Pais>"
    			+ "<Telefono>"+bp.get_ValueAsString("PatientPhone")+"</Telefono>"
    			+ "<Email>"+bp.get_ValueAsString("Mail")+"</Email>"
    			+ "<Nacionalidad>CHL</Nacionalidad>"
    			+ "</IdentificacionDePaciente>"
    			+ "<Admision>"
    			+ "<TipoPaciente>AMB</TipoPaciente>"
    			+ "<NroAdmision>"+hosp.getDocumentNo()+"</NroAdmision>"
    			+ "<NivelTriage>5</NivelTriage>"
    			+ "</Admision>";
    			//se terminan campos de la cabecera
    			//se leen lineas
    	for (MRequisitionLine line : req.getLines()) 
    	{
    		request = request
    				+ "<Orden>"
        			+ "<ControlDeOrden>NW</ControlDeOrden> <!-- NW/CA (Nueva Prestacion/Cancelar Prestacion) -->"
        			+ "<NroSolicitud>"+req.get_ID()+"</NroSolicitud>"
        			//+ "<CodServicioSolicitante>"+org.getValue()+"</CodServicioSolicitante>"
        			+ "<CodServicioSolicitante>"+req.getAD_Org_ID()+"</CodServicioSolicitante>"
        			+ "</Orden>"
    				+ "<Solicitud>"
    				+ "<CodPrestacion>"+line.getM_Product().getValue()+"</CodPrestacion>"
        			+ "<DescPrestacion>"+line.getM_Product().getName()+"</DescPrestacion>"
        			+ "<FechaSolicitud>"+dateReq+"</FechaSolicitud>"
        			+ "<CodCentroExtraccion>"+req.getAD_Org_ID()+"</CodCentroExtraccion>"
        			+ "<RutMedico>7102741-4</RutMedico>"
        			//+ "<NombreMedico>"+doctor.get_ValueAsString("Name1")+"</NombreMedico>"
        			+ "<NombreMedico>ROBERTO ANTONIO</NombreMedico>"
        			//+ "<ApellidoMedico>"+bp.getName2()+" "+bp.get_ValueAsString("Name3")+"</ApellidoMedico>"
        			+ "<ApellidoMedico>ASPEE ARAVENA</ApellidoMedico>"
        			+ "</Solicitud>"
        			+ "<Diagnostico>"
        			+ "<TipoDeValor>ST</TipoDeValor>"
        			+ "<CodPrueba>18630-4</CodPrueba>"
        			+ "<DescPrueba></DescPrueba>"
        			+ "<Valor>0</Valor>"
        			+ "</Diagnostico>";
		}
    	request = request
    			+ "</OrdenDeLaboratorio>"
				+ "]]>"
    			+ "</arg0>"
    			+ "</ws:acceptMessage>"
    			+ "</soapenv:Body>"
    			+ "</soapenv:Envelope>";
    	
    	Source response = null;
    	log.config("request: "+request);
    					
    	try
    	{				
    		final CXFConnector wsc = new CXFConnector();					
    		wsc.setSoapAction("http://ws.connectors.connect.mirth.com/DefaultAcceptMessage/acceptMessageRequest");
    		wsc.setRequest(request);
    		wsc.setBinding(SOAPBinding.SOAP11HTTP_BINDING);
    		//wsc.setEndpointAddress("http://172.16.11.113:8081/services/adempiere_bus");
    		wsc.setEndpointAddress("http://172.16.11.114:8081/services/adempiere_bus");
    		wsc.setServiceName("DefaultAcceptMessageService");
    		wsc.setPortName("DefaultAcceptMessagePort");
    		wsc.setTargetNS("http://ws.connectors.connect.mirth.com/");    				
    		wsc.executeConnector();
    		response = wsc.getResponse();
    		
    	}
    	catch(Exception e)
    	{
    		throw new Exception("No se ha podido establecer conexion con el Servicio de Facturacion - " + e);
    	}
    	
    	
    	try
    	{

      		//mfrojas stringwriter
        		StringWriter sw = new StringWriter();
    		//comienza la lectura del xml recibido
    		DocumentBuilderFactory.newInstance().newDocumentBuilder();
    		SAXSource output = (SAXSource) response;
    		Transformer tf = TransformerFactory.newInstance().newTransformer();
    		
    		DOMResult result = new DOMResult();
    		tf.transform(output, result);
    		Document doc = (Document) result.getNode();
    		
    		Node datos = findReturn(doc.getChildNodes().item(0)); 
    		
    		//mfrojas extra:
    		Node datospre = doc.getDocumentElement();
    		tf.transform(new DOMSource(datospre), new StreamResult(sw));
    		log.config("result "+sw.toString());
    		
    		if(datos!=null){
    			NodeList att = datos.getChildNodes(); 
    			for(int x=0;x<att.getLength();x++)
    			{	
    				log.config(att.item(x).getLocalName());
    				if(att.item(x).getLocalName().equals("return") || att.item(x).getNodeName().equals("return"))
    				{	
    					msg = att.item(x).getFirstChild().getNodeValue();								
    				}    							
    			} // fin for return
    		}//FIN DATOS
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return msg;    			
    }
    public Node findReturn(Node node) {
		Node value = null;
		NodeList list = node.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			// Get child node
			Node childNode = list.item(i);
			if(childNode.getNodeName().equals(("ns2:acceptMessageResponse")))
			{
				value = childNode;
				break;
			}

			value=findReturn(childNode);

		}
		return value;
    }    
}	
