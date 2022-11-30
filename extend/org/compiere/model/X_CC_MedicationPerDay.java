/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for CC_MedicationPerDay
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0 - $Id$ */
public class X_CC_MedicationPerDay extends PO implements I_CC_MedicationPerDay, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20220316L;

    /** Standard Constructor */
    public X_CC_MedicationPerDay (Properties ctx, int CC_MedicationPerDay_ID, String trxName)
    {
      super (ctx, CC_MedicationPerDay_ID, trxName);
      /** if (CC_MedicationPerDay_ID == 0)
        {
			setCC_MedicationPerDay_ID (0);
        } */
    }

    /** Load Constructor */
    public X_CC_MedicationPerDay (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_CC_MedicationPerDay[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_CC_Hospitalization getCC_Hospitalization() throws RuntimeException
    {
		return (I_CC_Hospitalization)MTable.get(getCtx(), I_CC_Hospitalization.Table_Name)
			.getPO(getCC_Hospitalization_ID(), get_TrxName());	}

	/** Set CC_Hospitalization_ID.
		@param CC_Hospitalization_ID CC_Hospitalization_ID	  */
	public void setCC_Hospitalization_ID (int CC_Hospitalization_ID)
	{
		if (CC_Hospitalization_ID < 1) 
			set_Value (COLUMNNAME_CC_Hospitalization_ID, null);
		else 
			set_Value (COLUMNNAME_CC_Hospitalization_ID, Integer.valueOf(CC_Hospitalization_ID));
	}

	/** Get CC_Hospitalization_ID.
		@return CC_Hospitalization_ID	  */
	public int getCC_Hospitalization_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_CC_Hospitalization_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set CC_MedicationPerDay ID.
		@param CC_MedicationPerDay_ID CC_MedicationPerDay ID	  */
	public void setCC_MedicationPerDay_ID (int CC_MedicationPerDay_ID)
	{
		if (CC_MedicationPerDay_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_CC_MedicationPerDay_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_CC_MedicationPerDay_ID, Integer.valueOf(CC_MedicationPerDay_ID));
	}

	/** Get CC_MedicationPerDay ID.
		@return CC_MedicationPerDay ID	  */
	public int getCC_MedicationPerDay_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_CC_MedicationPerDay_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}