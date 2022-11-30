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
import org.compiere.util.KeyNamePair;

/** Generated Model for CC_SelectBPLine
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0 - $Id$ */
public class X_CC_SelectBPLine extends PO implements I_CC_SelectBPLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20210825L;

    /** Standard Constructor */
    public X_CC_SelectBPLine (Properties ctx, int CC_SelectBPLine_ID, String trxName)
    {
      super (ctx, CC_SelectBPLine_ID, trxName);
      /** if (CC_SelectBPLine_ID == 0)
        {
			setCC_SelectBP_ID (0);
			setCC_SelectBPLine_ID (0);
        } */
    }

    /** Load Constructor */
    public X_CC_SelectBPLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_CC_SelectBPLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
    {
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_Name)
			.getPO(getC_BPartner_ID(), get_TrxName());	}

	/** Set Business Partner .
		@param C_BPartner_ID 
		Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner .
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_CC_SelectBP getCC_SelectBP() throws RuntimeException
    {
		return (I_CC_SelectBP)MTable.get(getCtx(), I_CC_SelectBP.Table_Name)
			.getPO(getCC_SelectBP_ID(), get_TrxName());	}

	/** Set CC_SelectBP ID.
		@param CC_SelectBP_ID CC_SelectBP ID	  */
	public void setCC_SelectBP_ID (int CC_SelectBP_ID)
	{
		if (CC_SelectBP_ID < 1) 
			set_Value (COLUMNNAME_CC_SelectBP_ID, null);
		else 
			set_Value (COLUMNNAME_CC_SelectBP_ID, Integer.valueOf(CC_SelectBP_ID));
	}

	/** Get CC_SelectBP ID.
		@return CC_SelectBP ID	  */
	public int getCC_SelectBP_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_CC_SelectBP_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set CC_SelectBPLine ID.
		@param CC_SelectBPLine_ID CC_SelectBPLine ID	  */
	public void setCC_SelectBPLine_ID (int CC_SelectBPLine_ID)
	{
		if (CC_SelectBPLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_CC_SelectBPLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_CC_SelectBPLine_ID, Integer.valueOf(CC_SelectBPLine_ID));
	}

	/** Get CC_SelectBPLine ID.
		@return CC_SelectBPLine ID	  */
	public int getCC_SelectBPLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_CC_SelectBPLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Summary Level.
		@param IsSummary 
		This is a summary entity
	  */
	public void setIsSummary (boolean IsSummary)
	{
		set_Value (COLUMNNAME_IsSummary, Boolean.valueOf(IsSummary));
	}

	/** Get Summary Level.
		@return This is a summary entity
	  */
	public boolean isSummary () 
	{
		Object oo = get_Value(COLUMNNAME_IsSummary);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }
    
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (newRecord)
		{

			insert_Tree("DO");

			
		}
		
		//	Value/Name change

		return success;
	}	//	afterSave
}