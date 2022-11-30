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

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.OutputKeys;

import javax.xml.transform.TransformerException;

import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.process.SvrProcess;
import org.ofb.model.OFBForward;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.StringReader;
import java.io.StringWriter;

 

import org.adempiere.exceptions.AdempiereException;
import org.apache.commons.codec.binary.Base64; 
import org.apache.commons.httpclient.HttpClient;
import org.ofb.process.CXFConnector;


import org.compiere.model.MOrg;
import org.compiere.model.X_CC_Hospitalization;
import org.compiere.model.X_MED_LabDetail;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


//url
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;





/**
 *	Generate XML Consolidated from Invoice (Generic) 
 *	
 *  @author mfrojas
 *  @version $Id: XMLObtBonoInterfaz.java,v 1.2 19/05/2011 $
 */
public class XMLObtBonoInterfaz extends SvrProcess
{	
	/** Properties						*/
	private Properties 		m_ctx;	
	private int p_CC_Hosp_ID = 0;
	public String urlPdf = "";
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		p_CC_Hosp_ID=getRecord_ID();
		m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		X_CC_Hospitalization hosp=new X_CC_Hospitalization(m_ctx,p_CC_Hosp_ID,get_TrxName());
		String msg = "";
		msg=CreateXMLVentaBoleta(hosp);		
		return msg;
	}	//	doIt
	
	public String CreateXMLVentaBoleta(X_CC_Hospitalization hosp)
    {	
		String wsRespuesta = "";
        /*MDocType doc = new MDocType(invoice.getCtx(), invoice.getC_DocTypeTarget_ID(), invoice.get_TrxName());
        if(doc.get_Value("CreateXML") == null)
            return "";
        if(!((Boolean)doc.get_Value("CreateXML")).booleanValue())
            return "";
        int typeDoc = Integer.parseInt((String)doc.get_Value("DocumentNo"));
        if(typeDoc == 0)
            return "";*/
        String mylog = new String();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            //Document document = implementation.createDocument(null, "Envelope", null);
            Document document = implementation.createDocument("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope", null);
            //document.
            document.setXmlVersion("1.0");            
            document.setTextContent("text/xml");
            Element root = document.getDocumentElement();
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:soapenc", "http://schemas.xmlsoap.org/soap/encoding/");
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:urn", "WsInterfaz");
            Attr atr = document.createAttribute("xmlns:xsi");
            atr.setValue("http://www.w3.org/2001/XMLSchema-instance");     
            Attr atr2 = document.createAttribute("xmlns:xsd");
            atr2.setValue("http://www.w3.org/2001/XMLSchema");
            Attr atr3 = document.createAttribute("xmlns:soapenv");
            atr3.setValue("http://schemas.xmlsoap.org/soap/envelope/");
            Attr atr4 = document.createAttribute("xmlns:urn");
            atr4.setValue("urn:WsInterfaz");
            Attr atr5 = document.createAttribute("xmlns:soapenc");
            atr5.setValue("http://schemas.xmlsoap.org/soap/encoding/");
            
            MOrg company = MOrg.get(hosp.getCtx(), hosp.getAD_Org_ID());

            Element Encabezado = document.createElement("soapenv:Header");
            document.getDocumentElement().appendChild(Encabezado);
            

            Element Body = document.createElement("soapenv:Body");
            document.getDocumentElement().appendChild(Body);
            
            Element Documento = document.createElement("urn:ObtBonInterMul");
            //document.getDocumentElement().appendChild(Documento);
            Body.appendChild(Documento);
            Documento.setAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
            //Element IdDoc = document.createElement("IdDoc");
            //Encabezado.appendChild(IdDoc);
            mylog = "IdDoc";
            
            //mfrojas
            
            int user_id = Env.getAD_User_ID(hosp.getCtx());
            MUser user = new MUser(hosp.getCtx(), user_id, hosp.get_TrxName());
            MBPartner bpuser = new MBPartner(hosp.getCtx(),user.getC_BPartner_ID(),hosp.get_TrxName());
            String valueuser = bpuser.getValue().concat("-").concat(bpuser.get_Value("digito").toString());
            //String valueuser = "15753162-K";
            log.config("valueuser "+valueuser);
            
            if(hosp.get_Value("NroTransac") == null || hosp.get_Value("NroTransac").toString().length() < 3)
            	throw new AdempiereException("No hay nro de transaccion");
            
            Element CodUsuario = document.createElement("CodUsuario");
            org.w3c.dom.Text text = document.createTextNode("96790040-0");
            CodUsuario.appendChild(text);
            Documento.appendChild(CodUsuario);
            
            Element CodClave = document.createElement("CodClave");
            CodClave.setAttribute("type", "xsd:string");
            org.w3c.dom.Text cl = document.createTextNode("96790040");
            CodClave.appendChild(cl);
            Documento.appendChild(CodClave);
            
            Element NumTransac = document.createElement("NumTransac");
            org.w3c.dom.Text transac = document.createTextNode(hosp.get_Value("NroTransac").toString());
            NumTransac.setAttribute("type", "xsd:int");
            NumTransac.appendChild(transac);
            Documento.appendChild(NumTransac);
          
            
	         /*String ExportDir = (String)company.get_Value("ExportDir");
	          try
	          {
	        	  File theDir = new File(ExportDir);
	        	  if (!theDir.exists())
	        		  ExportDir = (String)company.get_Value("ExportDir2"); 
	          }
	          catch(Exception e)
	          {
	        	  throw new AdempiereException("no existe directorio");
	          }
	          */
	          //ExportDir = ExportDir.replace("\\", "/");
	          javax.xml.transform.Source source = new DOMSource(document);
	          //javax.xml.transform.Result result = new StreamResult(new File(ExportDir, (new StringBuilder()).append("1").append(".xml").toString()));
	          javax.xml.transform.Result console = new StreamResult(System.out);
	          Transformer transformer = TransformerFactory.newInstance().newTransformer();
	          transformer.setOutputProperty("indent", "yes");
	          transformer.setOutputProperty("encoding", "UTF-8");
	          //transformer.transform(source, result);
	          transformer.transform(source, console);
           
            
            //convertir a base 64                       
    		//File file = new File(ExportDir, (new StringBuilder()).append("1").append(".xml").toString());

    		//setear atributos de cabecera
    		//Document docValid = builder.parse(file);
    		//Element raiz = docValid.getDocumentElement();
    		//raiz.setAttribute("version", "1.0");

    		
    		
    		/*raiz.setAttribute("xmlns:soapenc", "http://schemas.xmlsoap.org/soap/encoding/");
    		raiz.setAttribute("xmlns:urn", "urn:WsInterfaz");
    		raiz.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
    		raiz.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
    		raiz.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
*/
    		
    		//se guarda nuevo xml
  /*          source = new DOMSource(docValid);
            result = new StreamResult(new File(ExportDir, (new StringBuilder()).append("1").append(".xml").toString()));
            console = new StreamResult(System.out);
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.transform(source, result);
            transformer.transform(source, console);
    */        
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            
            log.config("veamos "+writer.getBuffer().toString());
            
            String xmlstring = writer.getBuffer().toString();
            
    		
            //codificacion a base64
    	/*	byte[] fileArray = new byte[(int) file.length()];
    		InputStream inputStream;
    		inputStream = new FileInputStream(file);
			inputStream.read(fileArray);    		
			
			log.config("filearray string "+raiz);
    		byte[] encoded = Base64.encodeBase64(fileArray);     
    		String encodedFile = "";
    		try 
    		{	
    			encodedFile = new String(encoded);
    		} catch (Exception e) {
    		}          
    		*/
   
    		//mfrojas
    		//Enlace con interfaz4 bonoelectronico
    		
    		
   			Source response = null;
   			//log.config("result "+result.toString());
			
   			log.config("preconn");
			try
			{				
				final CXFConnector wsc = new CXFConnector();					
				wsc.setSoapAction("urn:WsInterfaz");
				wsc.setRequest(xmlstring);
				wsc.setBinding(SOAPBinding.SOAP11HTTP_BINDING);
				wsc.setEndpointAddress("https://interfaz4.bonoelectronico.cl/WsInterfaz.php");
				//wsc.setEndpointAddress("http://interfaz4pre.bonoelectronico.cl/WsInterfaz.php");
				wsc.setServiceName("WsInterfaz");
				wsc.setPortName("WsInterfazSOAP");
				wsc.setTargetNS("urn:WsInterfaz");
				wsc.executeConnector();
				log.config("supprecon");
				response = wsc.getResponse(); 
				
			}
			catch(Exception e)
			{
				log.config("exception");
				throw new Exception("No se pudo conectar");
			}
				
				
			log.config("PASE");
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
			SAXSource output = (SAXSource) response;
			Transformer tf = TransformerFactory.newInstance().newTransformer();
		
			log.config("PASE2");
			DOMResult resultmsg = new DOMResult();
			tf.transform(output, resultmsg);
			Document doc = (Document) resultmsg.getNode();
			
			log.config("PASE3");
			Node datospre = doc.getDocumentElement();
			//Node to string
			
			StringWriter sw = new StringWriter();
			
			  try {
			      Transformer t = TransformerFactory.newInstance().newTransformer();
			      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			      t.setOutputProperty(OutputKeys.INDENT, "yes");
			      t.transform(new DOMSource(datospre), new StreamResult(sw));
			    } catch (TransformerException te) {
			      System.out.println("nodeToString Transformer Exception");
			    }
			  
			  log.config("stringggg "+sw.toString());
			  
			//Desde ahora, verificar si el error es 0 o es 1.
			String codError = findText(sw.toString(), "<CodError xsi:type=\"xsd:int\">", "</CodError>");
			log.config("coderror "+codError);
			
			if(codError.compareTo("1") == 0)
			{
				//Hay error, se debe indicar el error
				return findText(sw.toString(), "<GloError xsi:type=\"xsd:string\">", "</GloError>");
				
			}
			else
			{
				//No hay error, se lee por linea
				String linestr = "";
				BufferedReader rd = new BufferedReader(new StringReader(sw.toString()));
				int amountcopay = 0;
				int segcomp = 0;
				int amountcopayline = 0;
				int segcompline = 0;
				int montobon = 0;
				int montobonline = 0;
				String codprestacion = "";
				String find = "";
				int prodxml = 0;
				while((linestr = rd.readLine()) != null)
				{
					if(linestr.contains("CodPrestacion"))
					{
						codprestacion = findText(linestr,"<CodPrestacion xsi:type=\"xsd:string\">","</CodPrestacion>" );
						prodxml = DB.getSQLValue(get_TrxName(), "SELECT coalesce(max(m_product_id),0) from m_product WHERE value like '"+codprestacion+"'");
					}
					amountcopayline = 0;
					segcompline = 0;
					//COPAGO
					if(linestr.contains("MontoCopago"))
					{
						find = findText(linestr,"<MontoCopago xsi:type=\"xsd:int\">","</MontoCopago>" );
						log.config("linestring = "+linestr);
						if(amountcopay == 0)
							amountcopay = new Integer(find);
						else if(amountcopay > 0)
							amountcopay += new Integer(find);
						amountcopayline = new Integer(find);
						log.config("amountcopay = "+amountcopay);
						log.config("amountcopayline = "+amountcopayline);
						if(prodxml > 0)
						{
							log.config("codprestacion "+codprestacion);

							String updatesqlcopago = "UPDATE med_labdetail SET copayline = "+amountcopayline+" " +						
								" WHERE m_product_id = "+prodxml+"" +
								" AND cc_hospitalization_id = "+p_CC_Hosp_ID;	
							log.config("updatesqlcopago");
							DB.executeUpdate(updatesqlcopago, get_TrxName());
						}
					}
					//BONIFICACION
					if(linestr.contains("MontoBon"))
					{
						find = findText(linestr,"<MontoBon xsi:type=\"xsd:int\">","</MontoBon>" );
						log.config("linestring = "+linestr);
						if(montobon == 0)
							montobon = new Integer(find);
						else if(montobon > 0)
							montobon += new Integer(find);
						montobonline = new Integer(find);
						log.config("montobon = "+montobon);
						log.config("montobonline = "+montobonline);
						if(prodxml > 0)
						{
							log.config("codprestacion "+codprestacion);

							String updatesqlbon = "UPDATE med_labdetail SET insurerline = "+montobonline+" " +						
								" WHERE m_product_id = "+prodxml+"" +
								" AND cc_hospitalization_id = "+p_CC_Hosp_ID;	
							log.config("updatesqlbon");
							DB.executeUpdate(updatesqlbon, get_TrxName());
						}
					}
					//BONIFICACION ADICIONAL
					if(linestr.contains("MtoBonAdic"))
					{
						find = findText(linestr,"<MtoBonAdic xsi:type=\"xsd:int\">","</MtoBonAdic>" );
						log.config("linestring = "+linestr);
						if(segcomp == 0)
							segcomp = new Integer(find);
						else if(segcomp > 0)
							segcomp += new Integer(find);
						segcompline = new Integer(find);
						log.config("segcomp = "+segcomp);
						log.config("segcompline = "+segcompline);
						
						if(prodxml > 0)
						{
							log.config("codprestacion "+codprestacion);

							String updatesqladicional = "UPDATE med_labdetail SET insuranceline = "+segcompline+" " +						
								" WHERE m_product_id = "+prodxml+"" +
								" AND cc_hospitalization_id = "+p_CC_Hosp_ID;	
							log.config("updatesqlcopago");
							DB.executeUpdate(updatesqladicional, get_TrxName());
						}
					
					}
				}
				
				log.config("amountcopay = "+amountcopay);
				String audit =  findText(sw.toString(), "<CodAuditoria xsi:type=\"xsd:string\">", "</CodAuditoria>");
				String transaccion =  findText(sw.toString(), "<NumTransac xsi:type=\"xsd:int\">", "</NumTransac>");
				log.config("audit "+audit+" transac "+transaccion);
				
				//String post_url = "http://interfaz4.bonoelectronico.cl/login.php";
				String post_url = "http://interfaz4pre.bonoelectronico.cl/login.php";
				String user_agent = "Mozilla/5.0";
				String params = "NumTransac="+transaccion+"&NroAuditoria="+audit;
				log.config("parametros "+params);
				
				
				//FOLIO
				
				String folio = findText(sw.toString(), "<FolioBono xsi:type=\"xsd:int\">", "</FolioBono>");
				if(folio == null)
				{
					folio = "N";
				}
				String updatesql = "UPDATE cc_hospitalization SET copay = '"+amountcopay+"', foliono = '"+folio+"', " +
						" complementaryinsurance =  "+segcomp+", insurerbonus = "+montobon+" " +
						" WHERE " +
						" cc_hospitalization_id = "+p_CC_Hosp_ID;		
				DB.executeUpdate(updatesql, get_TrxName());
				
				//mfrojas 20211004
				//update sql, para actualizar el campo payamt2 y el campo payamt
				
				//payamt2 = total prestaciones, suma de campo "monto total" en linea
				
				String payamt2sql  = "SELECT coalesce(sum(amount),0) from med_labdetail WHERE " +
						" cc_hospitalization_id = ? ";
				BigDecimal payamt2 = DB.getSQLValueBD(get_TrxName(), payamt2sql, p_CC_Hosp_ID);
				
				
				//payamt = total copago (donde copago sea mayor a cero) mas monto total (donde copago es cero)
				
				String payamtsql1 = "SELECT coalesce(sum(copayline),0) from med_labdetail WHERE " +
						" cc_hospitalization_id = ? and copayline > 0 ";
				
				String payamtsql2 = "SELECT coalesce(sum(amount),0) from med_labdetail WHERE " +
						" cc_hospitalization_id = ? and copayline <= 0 ";
				
				BigDecimal payamtaux1 = DB.getSQLValueBD(get_TrxName(), payamtsql1, p_CC_Hosp_ID);
				BigDecimal payamtaux2 = DB.getSQLValueBD(get_TrxName(), payamtsql2, p_CC_Hosp_ID);
				
				
				updatesql = "UPDATE cc_hospitalization SET payamt2 = "+payamt2+", payamt="+payamtaux1.add(payamtaux2)+" WHERE " +
						" cc_hospitalization_id = "+p_CC_Hosp_ID;
				DB.executeUpdate(updatesql, get_TrxName());

				URL obj = new URL(post_url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", user_agent);
                con.setRequestProperty("Accept", "application/xml");
                con.setRequestProperty("Content-Type","application/xml; charset=utf-8");
				
				con.setDoOutput(true);
				con.setUseCaches(false);
				OutputStream os = con.getOutputStream();
				os.write(params.getBytes());
				
				
				os.flush();
				os.close();
				
				

			    
				int responseCode = con.getResponseCode();
				log.config("response code "+responseCode);
				String subs3 = "";
				if (responseCode == HttpURLConnection.HTTP_OK) 
				{ //success
					BufferedReader in = new BufferedReader(new InputStreamReader(
							con.getInputStream()));
					String inputLine;
					StringBuffer responsebuf = new StringBuffer();
	                          

	                				
					while ((inputLine = in.readLine()) != null) 
					{
						responsebuf.append(inputLine);
					}
					in.close();

					// print result
					//System.out.println(responsebuf.toString());
					log.config("responsebuf "+responsebuf.toString());
					
	
				} 
				else 
				{
					System.out.println("POST no funciona");
					log.config("no funciona");
				}
						
			}
			

    		
    	}
        catch(Exception e)
        {
            log.severe((new StringBuilder()).append("CreateXML: ").append(mylog).append("--").append(e.getMessage()).toString());
            return (new StringBuilder()).append("CreateXML: ").append(mylog).append("--").append(e.getMessage()).toString();
        }       
        
    
        
        return "Generado "+ wsRespuesta;
    }	
        

  
    public Node findReturn(Node node) {
		Node value = null;
		NodeList list = node.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			// Get child node
			Node childNode = list.item(i);
			if(childNode.getNodeName().equals(("ns1:SolicitarSesionResponse")))
			{
				value = childNode;
				break;
			}

			value=findReturn(childNode);

		}
		return value;
    }
    public Node findError(Node node) {
		Node value = null;
		log.config("finderror-1");

		NodeList list = node.getChildNodes();
		log.config("finderror");
		for (int i=0; i<list.getLength(); i++) {
			// Get child node
			Node childNode = list.item(i);
			log.config("finderror2");
			log.config("childnode "+childNode.getNodeName());
			if(childNode.getNodeName().contains(("CodError")))
			{
				value = childNode;
				log.config("nodename "+childNode.getNodeName());
				break;
			}

			value=findError(childNode);

		}
		
		log.config("termine ");
		log.config("ultimo node "+value.getNodeName() +" node blabla "+value.getNodeValue());
		return value;
    }

    public Node findReturnEnviarDTE(Node node) {
		Node value = null;
		NodeList list = node.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			// Get child node
			Node childNode = list.item(i);
			if(childNode.getNodeName().equals(("ns1:EnviarDTEResponse")))
			{
				value = childNode;
				break;
			}

			value=findReturnEnviarDTE(childNode);

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
