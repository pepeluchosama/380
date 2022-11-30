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
package org.ofb.process;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.compiere.process.SvrProcess;



/**
 *	Generate XML Consolidated from Invoice (Generic) 
 *	
 *  @author mfrojas
 *  @version $Id: ExportDTEInvoiceCG.java,v 1.2 19/05/2011 $
 */
public class ExportDTEInvoiceFOLArtilecGDE2 extends SvrProcess
{	
	/** Properties						*/
	public String urlPdf = "";
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
	}	//	prepare

	
	/**
	 * 	Create Shipment
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt () throws Exception
	{
		
		DocumentoDTE.SiiDte.DTEDefType dte = new DocumentoDTE.SiiDte.DTEDefType();
		dte.setDTE_Choice(new DocumentoDTE.SiiDte.DTE_Choice());

		//Documento
		dte.getDTE_Choice().setDocumento(new DocumentoDTE.SiiDte.Documento());
		DocumentoDTE.SiiDte.Documento doc = dte.getDTE_Choice().getDocumento();

		//Documento/Encabezado
		doc.setEncabezado(new DocumentoDTE.SiiDte.Encabezado());

		//Documento/Encabezado/IdDoc
		DocumentoDTE.SiiDte.IdDoc idDoc = new DocumentoDTE.SiiDte.IdDoc();
		idDoc.setTipoDTE(DocumentoDTE.SiiDte.DTEType.n33);
		idDoc.setFolio(BigInteger.valueOf(1));
		idDoc.setFchEmis(new com.liquid_technologies.ltxmllib12.DateTime(com.liquid_technologies.ltxmllib12.DateTimeType.date, "2010-01-01"));
		doc.getEncabezado().setIdDoc(idDoc);

		//Documento/Encabezado/Emisor
		DocumentoDTE.SiiDte.Emisor emisor = new DocumentoDTE.SiiDte.Emisor();
		emisor.setRUTEmisor("76129486-5");
		emisor.setRznSoc("Razon social");
		emisor.setGiroEmis("Giro emisor");
		emisor.getActeco().add(BigInteger.valueOf(1234));;
		doc.getEncabezado().setEmisor(emisor);

		//Documento/Encabezado/Receptor
		DocumentoDTE.SiiDte.Receptor receptor = new DocumentoDTE.SiiDte.Receptor();
		receptor.setRUTRecep("11111111-1");
		receptor.setRznSocRecep("Razon social");
		receptor.setCmnaRecep("Santiago");
		receptor.setGiroRecep("TI");
		receptor.setDirRecep("Santiago");
		doc.getEncabezado().setReceptor(receptor);

		//Documento/Encabezado/Totales
		DocumentoDTE.SiiDte.Totales totales = new DocumentoDTE.SiiDte.Totales();
		totales.setMntTotal(BigInteger.valueOf(123456));
		doc.getEncabezado().setTotales(totales);

		//Documento/Detalle
		DocumentoDTE.SiiDte.Detalle det = new DocumentoDTE.SiiDte.Detalle();
		det.setNroLinDet(BigInteger.valueOf(1));
		det.setNmbItem("Nombre");
		det.setQtyItem(BigDecimal.valueOf(10));
		det.setPrcItem(BigDecimal.valueOf(5000));
		det.setMontoItem(BigInteger.valueOf(50000));

		doc.getDetalle().add(det);

					

		// Llamada al servicio
		String ambiente = dteboxcliente.Ambiente.Homologacion;
		String fechaResolucion = "2010-01-01";
		int numeroResolucion = 10;
		int tipoPdf417 = dteboxcliente.TipoPDF417.Fuente;
		        
		  //QA
        //String apiURL = "http://25.60.57.223/api/Core.svc/Core";
		String apiURL = "http://200.6.99.206/api/Core.svc/core";

        //String apiAuth = "21063e43-2510-42ee-b973-029508e6657a";
		String apiAuth = "2a84137d-a654-4f46-b873-2be446dadba7";

		        
		dteboxcliente.Servicio servicio = new dteboxcliente.Servicio(apiURL, apiAuth);
		dteboxcliente.ResultadoEnvioDocumento resultado = servicio.EnviarDocumento(dte, ambiente, fechaResolucion, numeroResolucion, tipoPdf417);
		        
		//Procesar resultado
		if (resultado.getResultadoServicio().getEstado() == 0){
		    String ted = resultado.getTED();
		    String pdf417 = resultado.getPDF417();
		            
		}else{
		    String error = resultado.getResultadoServicio().getDescripcion();
		    String error2 = resultado.getResultadoServicio().getDescripcion();
		}
		
		return "";
	}	//	doIt
	
	public String CreateXMLCG()
    {
		String error ="";
		String ted = "";
		String pdf417 = "";
		try 
		{		
			DocumentoDTE.SiiDte.DTEDefType dte = new DocumentoDTE.SiiDte.DTEDefType();
			dte.setDTE_Choice(new DocumentoDTE.SiiDte.DTE_Choice());
	
			//Documento
			dte.getDTE_Choice().setDocumento(new DocumentoDTE.SiiDte.Documento());
			DocumentoDTE.SiiDte.Documento doc = dte.getDTE_Choice().getDocumento();
	
			//Documento/Encabezado
			doc.setEncabezado(new DocumentoDTE.SiiDte.Encabezado());
	
			//Documento/Encabezado/IdDoc
			DocumentoDTE.SiiDte.IdDoc idDoc = new DocumentoDTE.SiiDte.IdDoc();
			idDoc.setTipoDTE(DocumentoDTE.SiiDte.DTEType.n33);
			idDoc.setFolio(BigInteger.valueOf(2255));
			
			idDoc.setFchEmis(new com.liquid_technologies.ltxmllib12.DateTime(com.liquid_technologies.ltxmllib12.DateTimeType.date,"2010-01-01"));
            idDoc.setFmaPago(DocumentoDTE.SiiDte.IdDoc_FmaPago.n1);
            doc.getEncabezado().setIdDoc(idDoc);



			//Documento/Encabezado/IdDoc/MntPagos
			DocumentoDTE.SiiDte.MntPagos mntPago = new DocumentoDTE.SiiDte.MntPagos();
			mntPago.setFchPago(new com.liquid_technologies.ltxmllib12.DateTime(com.liquid_technologies.ltxmllib12.DateTimeType.date, "2010-01-01"));
			mntPago.setMntPago(BigInteger.valueOf(12345));

			mntPago.setGlosaPagos("Glosa de pagos");
			idDoc.getMntPagos().add(mntPago);
			doc.getEncabezado().setIdDoc(idDoc);
            
			//Documento/Encabezado/Emisor
			DocumentoDTE.SiiDte.Emisor emisor = new DocumentoDTE.SiiDte.Emisor();
			emisor.setRUTEmisor("76129486-5");

			emisor.setRznSoc("Razon social");

	        emisor.setGiroEmis("Giro Emisor");
			emisor.getActeco().add(BigInteger.valueOf(1234));
			emisor.setDirOrigen("Address1");
			emisor.setCmnaOrigen("Comuna");
			emisor.setCiudadOrigen("City");

			doc.getEncabezado().setEmisor(emisor);
	
			//Documento/Encabezado/Receptor
			DocumentoDTE.SiiDte.Receptor receptor = new DocumentoDTE.SiiDte.Receptor();
			receptor.setRUTRecep("11111111-1");

			receptor.setRznSocRecep("Razon social");

			
	        receptor.setDirRecep("direccion");
	
	        receptor.setCmnaRecep("Comuna");

	        receptor.setCiudadRecep("Ciudad");
	        receptor.setGiroRecep("Giro");

			doc.getEncabezado().setReceptor(receptor);
			
			//Documento/Encabezado/Totales
			DocumentoDTE.SiiDte.Totales totales = new DocumentoDTE.SiiDte.Totales();
	
			totales.setMntTotal(BigInteger.valueOf(123456));
	
			totales.setMntNeto(BigInteger.valueOf(123456));
			totales.setMntExe(BigInteger.valueOf(123456));
	
				totales.setTasaIVA(BigDecimal.valueOf(19));
		
			doc.getEncabezado().setTotales(totales);
	
			
	
	        
            DocumentoDTE.SiiDte.Detalle det = new DocumentoDTE.SiiDte.Detalle();
            det.setNroLinDet(BigInteger.valueOf(1));
            det.setNmbItem("Nombre");
            det.setQtyItem(BigDecimal.valueOf(10));
            det.setPrcItem(BigDecimal.valueOf(5000));
            det.setMontoItem(BigInteger.valueOf(5000));

            doc.getDetalle().add(det);

		
			// Llamada al servicio
			String ambiente = dteboxcliente.Ambiente.Homologacion;
			String fechaResolucion = "2010-01-01";
			int numeroResolucion = 10;
			int tipoPdf417 = dteboxcliente.TipoPDF417.Fuente;
			        
			String apiURL = "http://200.6.99.206/api/Core.svc/core";
			String apiAuth = "2a84137d-a654-4f46-b873-2be446dadba7";
			        
			dteboxcliente.Servicio servicio = new dteboxcliente.Servicio(apiURL, apiAuth);
			dteboxcliente.ResultadoEnvioDocumento resultado = servicio.EnviarDocumento(dte, ambiente, fechaResolucion, numeroResolucion, tipoPdf417);
			dte.toXml();
			log.config("dte "+dte.toXml());
			//Procesar resultado
			log.config("resultado "+resultado.getResultadoServicio().getEstado());
			
			if (resultado.getResultadoServicio().getEstado() == 0){
			            
			    ted = resultado.getTED();
			    pdf417 = resultado.getPDF417();
			            
			}
			else
			{			            
			    error = resultado.getResultadoServicio().getDescripcion();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
        return "XML CG Generated "+ted+" "+pdf417+" "+error;
		//return error;
    }        

	
}	//	InvoiceCreateInOut
