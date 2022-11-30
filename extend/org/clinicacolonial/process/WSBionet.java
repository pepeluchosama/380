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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPBinding;

import org.compiere.process.SvrProcess;
import org.ofb.process.CXFConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
/**
 *	
 *  @author Italo Ni�oles ininoles
 *  @version $Id: WSBionet.java $
 */
public class WSBionet extends SvrProcess
{	
	/** Properties						*/
	//private Properties 		m_ctx;	
	public String urlPdf = "";
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		//p_C_Invoice_ID=getRecord_ID();
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
		return llamarWSAcceptMessage();
	}	//	doIt
	  
    private String llamarWSAcceptMessage() throws Exception
    {	
    	String msg = "";    	
    	
    	//antigua llamada
    	/*final String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.connectors.connect.mirth.com/\">"
    			+ "<soapenv:Header/>"
    			+ "<soapenv:Body>"
    			+ "<ws:acceptMessage>"
    			+ "<!-- Optional: -->"
    			+ "<arg0>"
    			+ "<![CDATA[<LaboratoryOrder>"
    			+ "<PatientIdentification>"
		    	+ "<GivenName>Cristian Diego</GivenName>"
		    	+ "<FamilyName>Carrasco Codocedo</FamilyName>"
		    	+ "<Rut>16351236-k</Rut>"
		    	+ "</PatientIdentification>"
		    	+ "<CommonOrder>"
		    	+ "	<PlacerGroupNumber>LIB10750526</PlacerGroupNumber>"
				+ "<OrderingFacility>TCOLO</OrderingFacility>"
				+ "</CommonOrder>"
				+ "<ObservationRequest>"
				+ "<UniversalServiceIdentifier>0302023-001</UniversalServiceIdentifier>"
				+ "<CollectorIdentifier>TMECLIN</CollectorIdentifier>"
				+ "</ObservationRequest>"
				+ "<ObservationResult>"
				+ "<ValueType>ST</ValueType>"
				+ "<Identifier>18630-4</Identifier>"
				+ "<Value>ASMA</Value>"
				+ "</ObservationResult>"
				+ "<CommonOrder>"
				+ "<PlacerGroupNumber>LIB10750527</PlacerGroupNumber>"
				+ "<OrderingFacility>TCOLO</OrderingFacility>"
				+ "</CommonOrder>"
				+ "<ObservationRequest>"
				+ "<UniversalServiceIdentifier>0302023-001</UniversalServiceIdentifier>"
				+ "<CollectorIdentifier>TMECLIN</CollectorIdentifier>"
				+ "</ObservationRequest>"
				+ "<ObservationResult>"
				+ "<ValueType>ST</ValueType>"
				+ "<Identifier>18630-4</Identifier>"
				+ "<Value>ASMA</Value>"
				+ "</ObservationResult>"
				+ "</LaboratoryOrder>]]>"
    			+ "</arg0>"
    			+ "</ws:acceptMessage>"
    			+ "</soapenv:Body>"
    			+ "</soapenv:Envelope>";*/
    	
    	//nueva llamada 15-03-2022
    	final String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.connectors.connect.mirth.com/\">"
    			+ "<soapenv:Header/>"
    			+ "<soapenv:Body>"
    			+ "<ws:acceptMessage>"
    			+ "<!-- Optional: -->"
    			+ "<arg0>"
    			+ "<![CDATA["
    			+ "<OrdenDeLaboratorio>"
    			+ "<IdentificacionDePaciente>"
    			+ "<Nombres>Cristian Diego</Nombres>"
    			+ "<Apellidos>Carrasco Codocedo</Apellidos>"
    			+ "<Rut>16351236-k</Rut>"
    			+ "<Fnacimiento>20170619</Fnacimiento>"
    			+ "<Genero>F</Genero> <!-- M,F,U -->"
    			+ "<Direccion>Los Canarios 324</Direccion>"
    			+ "<Ciudad>las condes</Ciudad>"
    			+ "<Pais>PAIS NO INFORMADO</Pais>"
    			+ "<Telefono></Telefono>"
    			+ "<Email></Email>"
    			+ "<Nacionalidad>CHL</Nacionalidad>"
    			+ "</IdentificacionDePaciente>"
    			+ "<Admision>"
    			+ "<TipoPaciente>AMB</TipoPaciente>"
    			+ "<NroAdmision></NroAdmision>"
    			+ "<NivelTriage>5</NivelTriage>"
    			+ "</Admision>"
    			
    			+ "<Orden>"
    			+ "	<ControlDeOrden>NW</ControlDeOrden> <!-- NW/CA (Nueva Prestacion/Cancelar Prestacion) -->"
    			+ "<NroSolicitud>LIB10750526</NroSolicitud>"
    			+ "<CodServicioSolicitante>TCOLO</CodServicioSolicitante>"
    			+ "</Orden>"
    			
    			+ "<Solicitud>"
    			+ "<CodPrestacion>0302023-001</CodPrestacion>"
    			+ "<DescPrestacion></DescPrestacion>"
    			+ "<FechaSolicitud>20220310153705</FechaSolicitud>"
    			+ "<CodCentroExtraccion>TMECLIN</CodCentroExtraccion>"
    			+ "<RutMedico>7402331-2</RutMedico>"
    			+ "<NombreMedico>MARCO</NombreMedico>"
    			+ "<ApellidoMedico>CLAVERO PEREZ</ApellidoMedico>"
    			+ "</Solicitud>"
    			
    			+ "<Diagnostico>"
    			+ "<TipoDeValor>ST</TipoDeValor>"
    			+ "<CodPrueba>18630-4</CodPrueba>"
    			+ "<DescPrueba></DescPrueba>"
    			+ "<Valor>ASMA</Valor>"
    			+ "</Diagnostico>"
    			
    			+ "<Orden>"
    			+ "	<ControlDeOrden>NW</ControlDeOrden> <!-- NW/CA (Nueva Prestacion/Cancelar Prestacion) -->"
    			+ "<NroSolicitud>LIB10750527</NroSolicitud>"
    			+ "<CodServicioSolicitante>TCOLO2</CodServicioSolicitante>"
    			+ "</Orden>"
    			
    			+ "<Solicitud>"
    			+ "<CodPrestacion>0302023-001</CodPrestacion>"
    			+ "<DescPrestacion></DescPrestacion>"
    			+ "<FechaSolicitud>20220310153705</FechaSolicitud>"
    			+ "<CodCentroExtraccion>TMECLIN</CodCentroExtraccion>"
    			+ "<RutMedico>7402331-2</RutMedico>"
    			+ "<NombreMedico>MARCO</NombreMedico>"
    			+ "<ApellidoMedico>CLAVERO PEREZ</ApellidoMedico>"
    			+ "</Solicitud>"
    			
    			+ "<Diagnostico>"
    			+ "<TipoDeValor>ST</TipoDeValor>"
    			+ "<CodPrueba>18630-4</CodPrueba>"
    			+ "<DescPrueba></DescPrueba>"
    			+ "<Valor>ASMA</Valor>"
    			+ "</Diagnostico>"
    			
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
    		wsc.setEndpointAddress("http://172.16.11.113:8081/services/adempiere_bus");
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
