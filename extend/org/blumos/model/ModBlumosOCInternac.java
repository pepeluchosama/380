package org.blumos.model;

import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;

/**
 *	Validator for company blumos
 *
 *  @author ininoles
 */
public class ModBlumosOCInternac implements ModelValidator
{
	/**
	 *	Constructor.
	 *	The class is instantiated when logging in and client is selected/known
	 */
	public ModBlumosOCInternac ()
	{
		super ();
	}	//	MyValidator

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(ModBlumosOCInternac.class);
	/** Client			*/
	private int		m_AD_Client_ID = -1;
	

	/**
	 *	Initialize Validation
	 *	@param engine validation engine
	 *	@param client client
	 */
	public void initialize (ModelValidationEngine engine, MClient client)
	{
		//client = null for global validator
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		}
		else  {
			log.info("Initializing global validator: "+this.toString());
		}
		//	Tables to be monitored
		engine.addModelChange(MOrder.Table_Name, this);
		//	Documents to be monitored
		
				

	}	//	initialize

    /**
     *	Model Change of a monitored Table.
     *	
     */
	public String modelChange (PO po, int type) throws Exception
	{
		log.info(po.get_TableName() + " Type: "+type);		
		
		if((type == TYPE_BEFORE_CHANGE || type == TYPE_BEFORE_NEW) && po.get_Table_ID()==MOrder.Table_ID )  
		{
			MOrder order = (MOrder) po;
			if(!order.isSOTrx())
			{
				if(order.getC_DocTypeTarget_ID() == 1000052 || order.getAD_Client_ID() == 1000012)
					order.set_CustomColumn("Description2", "The buyer will not accept products whose " +
							"remaining shelf life is less than 75% of that indicated on the Certificate of Analysis, " +
							"or products that belong to more than one production batch, or products with manufacture " +
							"date earlier than that of a previously sent batch. All packing made of wood, " +
							"including dunnage, should be treated according to ISPM 15 from FAO and marked as such " +
							"in accordance with Annex II of said guideline.� Additionaly, in compliance with our " +
							"local regulations, all packages should weigh 20 kilograms or less. " +
							"This International Purchase Order is computer generated, thus no signature is required.");
				else
					order.set_CustomColumn("Description2","El comprador no aceptar� productos con vida �til menor " +
							"al 75% de lo indicado en el Certificado de An�lisis, o productos que pertenezcan a m�s de " +
							"un lote de producci�n, o productos con fecha de manufactura menor a la de un lote " +
							"previamente enviado. Todo el embalaje de madera, incluso el material de estiba, debe " +
							"ser tratado de acuerdo con las normas de ISPM 15 de la FAO y estar marcado seg�n el " +
							"Anexo II de la mencionada directriz. Adicionalmente, para cumplir con regulaci�n local, " +
							"ning�n envase debe de exceder los 20 kilogramos.");
			}			
		}		
		/*if((type == TYPE_BEFORE_NEW || type == TYPE_BEFORE_CHANGE)&& po.get_Table_ID()==MOrder.Table_ID
				&& po.is_ValueChanged("C_BPartner_ID"))  
		{
			MOrder order = (MOrder) po;
			if(!order.isSOTrx())
			{
				MBPartner part = new MBPartner(po.getCtx(), order.getC_BPartner_ID(), po.get_TrxName());
				if(part.get_ValueAsString("Inco_Term") != null)
					order.set_CustomColumn("Inco_Term", part.get_ValueAsString("Inco_Term"));
				if(part.get_ValueAsString("INCO_LINE") != null)
					order.set_CustomColumn("INCO_LINE", part.get_ValueAsString("INCO_LINE"));
				if(part.get_ValueAsInt("Inco_City_ID") > 0)				
						order.set_CustomColumn("Inco_City_ID", part.get_ValueAsInt("Inco_City_ID"));
				if(part.get_ValueAsInt("Inco_Region_ID") > 0)
					order.set_CustomColumn("Inco_Region_ID", part.get_ValueAsInt("Inco_Region_ID"));
				if(part.get_ValueAsInt("Pais_Origen_ID") > 0)
					order.set_CustomColumn("Pais_Origen_ID", part.get_ValueAsInt("Pais_Origen_ID"));
				if(part.get_ValueAsInt("PAIS_ADQUISICION_ID") > 0)
					order.set_CustomColumn("PAIS_ADQUISICION_ID", part.get_ValueAsInt("PAIS_ADQUISICION_ID"));
				if(part.get_ValueAsString("Insurance") != null)
					order.set_CustomColumn("Insurance", part.get_ValueAsString("Insurance"));				
				if(part.get_ValueAsString("Container") != null)
					order.set_CustomColumn("Container", part.get_ValueAsString("Container"));
				if(part.get_ValueAsInt("SalesRep_ID") > 0)
					order.set_CustomColumn("SalesRep_ID", part.get_ValueAsInt("SalesRep_ID"));
				if(part.get_ValueAsInt("C_PaymentTerm_ID") > 0)
					order.set_CustomColumn("C_PaymentTerm_ID", part.get_ValueAsInt("C_PaymentTerm_ID"));
				if(part.get_ValueAsString("PaymentFrom") != null)
					order.set_CustomColumn("PaymentFrom", part.get_ValueAsString("PaymentFrom"));
				if(part.get_ValueAsString("Inco_Via") != null)
					order.set_CustomColumn("Inco_Via", part.get_ValueAsString("Inco_Via"));
			}
		}*/
		return null;
	}	//	modelChange

	/**
	 *	Validate Document.
	 *	Called as first step of DocAction.prepareIt
     *	when you called addDocValidate for the table.
     *	Note that totals, etc. may not be correct.
	 *	@param po persistent object
	 *	@param timing see TIMING_ constants
     *	@return error message or null
	 */
	public String docValidate (PO po, int timing)
	{
		log.info(po.get_TableName() + " Timing: "+timing);

		
		return null;
	}	//	docValidate

	/**
	 *	User Login.
	 *	Called when preferences are set
	 *	@param AD_Org_ID org
	 *	@param AD_Role_ID role
	 *	@param AD_User_ID user
	 *	@return error message or null
	 */
	public String login (int AD_Org_ID, int AD_Role_ID, int AD_User_ID)
	{
		log.info("AD_User_ID=" + AD_User_ID);

		return null;
	}	//	login


	/**
	 *	Get Client to be monitored
	 *	@return AD_Client_ID client
	 */
	public int getAD_Client_ID()
	{
		return m_AD_Client_ID;
	}	//	getAD_Client_ID


	

	

}	