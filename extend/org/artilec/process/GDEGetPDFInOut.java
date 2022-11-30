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

import java.util.Properties;

import org.compiere.util.*;
import org.ofb.model.OFBForward;
import org.apache.commons.net.ftp.FTPClient;
import org.compiere.model.*;
import org.compiere.process.SvrProcess;

import java.io.OutputStream;
//import java.awt.Desktop;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
//import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.net.ftp.FTP; // Nos permite indicar si transfer BINARY o ASCII



/**
 *	Generate XML Consolidated from Invoice (Generic) 
 *	
 *  @author ininoles
 *  @version $Id: GDEGetPDF.java,v 1.2 19/05/2011 $
 */
public class GDEGetPDFInOut extends SvrProcess
{	
	/** Properties						*/
	private Properties 		m_ctx;	
	private int p_M_InOut_ID = 0;
	public String urlPdf = "";
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		p_M_InOut_ID=getRecord_ID();
		m_ctx = Env.getCtx();
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		MInOut ship=new MInOut(m_ctx,p_M_InOut_ID,get_TrxName());
		String msg = "";
		msg=GetPDF(ship);		
		return msg;
	}	//	doIt
	
	public String GetPDF(MInOut inOut)
    {
		// ﻿Recuperar el PDF de un documento
		// Recuperar PDF
		// Dependencias
		// - DTEBOXClienteJDK142.jar

		String ambiente = dteboxcliente.Ambiente.Homologacion;
        if(OFBForward.AmbienteGDE().compareToIgnoreCase("H") ==0)
        	ambiente = dteboxcliente.Ambiente.Homologacion;
        else if (OFBForward.AmbienteGDE().compareToIgnoreCase("P") ==0)
        	ambiente = dteboxcliente.Ambiente.Produccion;

		dteboxcliente.GrupoBusqueda grupo = dteboxcliente.GrupoBusqueda.Emitidos;

		MOrg org = MOrg.get(inOut.getCtx(), inOut.getAD_Org_ID());
		String rut = (String)org.get_Value("Rut");

		MDocType docType = new MDocType(inOut.getCtx(), inOut.getC_DocType_ID(), inOut.get_TrxName());
		int tipoDTE = dteboxcliente.TipoDocumento.TIPO_33;
		if(docType.get_ValueAsString("DocumentNo").compareTo("33") == 0)
			 tipoDTE = dteboxcliente.TipoDocumento.TIPO_33;
		else if(docType.get_ValueAsString("DocumentNo").compareTo("34") == 0)
			tipoDTE = dteboxcliente.TipoDocumento.TIPO_34;
		else if(docType.get_ValueAsString("DocumentNo").compareTo("52") == 0)
			tipoDTE = dteboxcliente.TipoDocumento.TIPO_52;
		else if(docType.get_ValueAsString("DocumentNo").compareTo("56") == 0)
			tipoDTE = dteboxcliente.TipoDocumento.TIPO_56;
		else if(docType.get_ValueAsString("DocumentNo").compareTo("61") == 0)
			tipoDTE = dteboxcliente.TipoDocumento.TIPO_61;
		else
			tipoDTE = dteboxcliente.TipoDocumento.TIPO_52;
				
		long folio = Long.parseLong(inOut.getDocumentNo());
		//String apiURL = "http://200.6.99.206/api/Core.svc/core";
		String apiURL = "http://192.168.0.200/api/Core.svc/Core";
		//String apiAuth = "2a84137d-a654-4f46-b873-2be446dadba7"; //pruebas			
		//String apiAuth = "9fbafbd5-62ac-4fb0-b0a0-d6e5d17fa19c"; //primer intento			
		//String apiAuth = "2ec64694-829f-4a6e-b369-dc8936320e09"; //segundo intento
		String apiAuth = OFBForward.GDEapiAuth();
		
		dteboxcliente.Servicio servicio = new dteboxcliente.Servicio(apiURL, apiAuth);
		dteboxcliente.ResultadoRecuperarPDF resultado = servicio.RecuperarPdf(ambiente, grupo, rut, tipoDTE, folio);
		// Procesar respuesta
		if (resultado.getResultadoServicio().getEstado() == 0
				|| resultado.getResultadoServicio().getEstado() == 1) 
		{
			log.config("datos Usados: apiURL="+apiURL+" - apiAuth="+apiAuth+" - ambiente="+ambiente+
					" - grupo="+grupo+" - rut="+rut+" - tipoDTE="+tipoDTE+" - folio="+folio); 
		    //Usar los datos que vienen en el resultado de la llamada
			log.config("log resultado: "+resultado.getResultadoServicio().getDescripcion()+" - "+
					resultado.getResultadoServicio().getExcepcionOriginal()+ " - "+
					resultado.getResultadoServicio().getEstado());
		    byte[] pdf = resultado.getDatos();
		    if(pdf != null)
		    {
			    String ExportDir = "";
			    //mfrojas
			    try 
			    {
			    	String pathServer = OFBForward.PathGDEServer();
			    	String pathCliente = OFBForward.PathGDEClient();
			    	if(pathServer != null && pathCliente != null
			    			&& pathServer .trim().length() > 0
			    			&& pathCliente .trim().length() > 0)
			    	{
			    		File theDir = new File(pathServer);
		  	        	if (!theDir.exists())
		  	        		ExportDir = pathCliente; 
		  	          
			    		log.config("Archivo a guardar: "+ExportDir+folio+".pdf");
				    	OutputStream out = new FileOutputStream(ExportDir+folio+".pdf");
				    	out.write(pdf);
				    	out.close();
				    	
				    	//se sube pdf a ftp
				    	String remote_working_dir_path = OFBForward.GDEDirPath();
				    	String local_filepath = ExportDir+folio+".pdf";
				    	String remote_filename = folio+".pdf";
				    	//String remoteURL = OFBForward.GDERemoteURL();
				    	String finalURL = OFBForward.GDERemoteURL()+folio+".pdf";
	
				    	FileInputStream fis = new FileInputStream(local_filepath);
				    	FTPClient client = new FTPClient();
				    	try {
				    	    client.setBufferSize(512); // Opcional para definir Buffer size en bytes
				    	    client.connect(OFBForward.GDEFTPServer(),OFBForward.GDEFTPServerPort()); // no el puerto es por defecto, podemos usar client.connect("servidor.ftp.com");
				    	    client.login(OFBForward.GDEFTPUser(),OFBForward.GDEFTPPass());
				    	    client.enterLocalPassiveMode(); // IMPORTANTE!!!! 
				    	    client.setFileType(FTP.BINARY_FILE_TYPE);
				    	    client.changeWorkingDirectory(remote_working_dir_path);
				    	    boolean uploadFile = client.storeFile(remote_filename,fis);
				    	    client.logout();
				    	    client.disconnect();
	
				    	    if ( uploadFile == false ) 
				    	        throw new Exception("Error al subir el fichero");
				    	    else// se guarda url
				    	    {
				    	    	inOut.set_CustomColumn("UrlGde",finalURL);
				    	    	inOut.saveEx(get_TrxName());
				    	    }			    	    	
				    	} catch (Exception eFTPClient) 
				    	{
				    	    // Gestionar el error, mostrar pantalla, reescalar excepcion... etc...
				    		log.config("ERROR: "+eFTPClient.toString());
				    	} finally {
				    	    fis.close();
				    	}
				    	
				    	/*
				    	log.config("Archivo a abrir: "+pathCliente+folio+".pdf");
				    	invoice.setDescription(pathCliente+folio+".pdf");
				    	invoice.saveEx(get_TrxName());
				    	Process p = Runtime.getRuntime().exec ("rundll32 SHELL32.DLL,ShellExec_RunDLL "+pathCliente+folio+".pdf");
				    	File path = new File(pathCliente+folio+".pdf");
					    Desktop.getDesktop().open(path);
					    */
			    	}
				} catch (IOException e) 
			    {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
		    }
		    //Guardar en adjunto ?
		    /*log.config("pdf byte "+pdf);
		    MAttachment att = new MAttachment(getCtx(), 0, get_TrxName());
		    att.setAD_Table_ID(318);
		    att.setBinaryData(pdf);
		    att.setRecord_ID(p_C_Invoice_ID);
		    att.setTitle("pdf");
		    att.save();*/
		    
		} 
		else 
		{
			//Descripción del error, actuar acorde
			String error = resultado.getResultadoServicio().getDescripcion();
			return error;
		}


		return null;
    }
	public void sendMail(MInvoice inv)
	{
		if(inv.getAD_User_ID() > 0)
		{
			if(inv.getAD_User().getEMail() != null
					&& inv.getAD_User().getEMail().trim().length() > 0)
			{
				String ln = System.getProperty("line.separator");
				StringBuilder str = new StringBuilder();
				str.append("Estimado Usuario:");
				str.append(ln);
				str.append(ln);
				str.append("Se a generado la factura numero "+inv.getDocumentNo());
				str.append(ln);
				str.append(ln);
				str.append("URL: "+inv.get_ValueAsString("UrlGde"));
				
				MClient client = new MClient(getCtx(),inv.getAD_Client_ID(),get_TrxName());
				EMail mail = new EMail(client, client.getRequestEMail(),inv.getAD_User().getEMail(), "Factura n�mero "+inv.getDocumentNo(),str.toString());
				mail.createAuthenticator(client.getRequestUser(), client.getRequestUserPW());
				mail.send();
			}
		}
	}
	
}	//	InvoiceCreateInOut
