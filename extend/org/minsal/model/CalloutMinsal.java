/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is Compiere ERP & CRM Smart Business Solution. The Initial
 * Developer of the Original Code is Jorg Janke. Portions created by Jorg Janke
 * are Copyright (C) 1999-2005 Jorg Janke.
 * All parts are Copyright (C) 1999-2005 ComPiere, Inc.  All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.minsal.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MAsset;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.adempiere.exceptions.AdempiereException;

/**
 *	Minsal Callouts.
 *	
 *  @author mfrojas.
 *  @version $Id: CalloutMinsal.java,v 2.0 2012/12/03  Exp $
 */
public class CalloutMinsal extends CalloutEngine
{		
	
	public String SetStock (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value) throws ParseException
	{
		if(value==null)
			return "";

		//Si el producto es nulo, stock es cero
		if(mTab.getValue("M_Product_ID")==null)
		{
			mTab.setValue("StockAvailable", Env.ZERO);
			return "";
		}
		
		
		int product = (Integer)mTab.getValue("M_Product_ID");
		//Si el producto es cero, stock es cero
		if(product <= 0)
		{
			mTab.setValue("StockAvailable", Env.ZERO);
			return "";
		}
		
		//Ubicacion
		//Si no hay ubicacion, el stock es cero.
		if(mTab.getValue("M_Locator_ID") == null)
		{
			mTab.setValue("StockAvailable", Env.ZERO);
			return "";
		}
		int loc = (Integer)mTab.getValue("M_Locator_ID");
		
		//Atributos
		int att = 0;
		if(mTab.getValue("M_AttributeSetInstance_ID") != null)
		{
			att = (Integer)mTab.getValue("M_AttributeSetInstance_ID");
		}
		String sql = "SELECT coalesce(sum(qtyonhand),0) from m_storage " +
				" WHERE m_locator_id = ? AND " +
				" M_Product_ID = ?";
		
		if(att > 0)
			sql = sql.concat(" AND m_Attributesetinstance_id = "+att);
		
		log.config("sql -> "+sql);
		
		BigDecimal qty = DB.getSQLValueBD(null, sql, loc, product);
		
		mTab.setValue("StockAvailable", qty);
		
		return "";

	}

	
}


