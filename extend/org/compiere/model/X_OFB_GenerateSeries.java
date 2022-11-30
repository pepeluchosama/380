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
import java.sql.Timestamp;
import java.util.Properties;

/** Generated Model for OFB_GenerateSeries
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0 - $Id$ */
public class X_OFB_GenerateSeries extends PO implements I_OFB_GenerateSeries, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200310L;

    /** Standard Constructor */
    public X_OFB_GenerateSeries (Properties ctx, int OFB_GenerateSeries_ID, String trxName)
    {
      super (ctx, OFB_GenerateSeries_ID, trxName);
      /** if (OFB_GenerateSeries_ID == 0)
        {
			setOFB_GenerateSeries_ID (0);
        } */
    }

    /** Load Constructor */
    public X_OFB_GenerateSeries (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_OFB_GenerateSeries[")
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

	/** Set dia.
		@param dia dia	  */
	public void setdia (String dia)
	{
		set_Value (COLUMNNAME_dia, dia);
	}

	/** Get dia.
		@return dia	  */
	public String getdia () 
	{
		return (String)get_Value(COLUMNNAME_dia);
	}

	/** Set fecha.
		@param fecha fecha	  */
	public void setfecha (Timestamp fecha)
	{
		set_Value (COLUMNNAME_fecha, fecha);
	}

	/** Get fecha.
		@return fecha	  */
	public Timestamp getfecha () 
	{
		return (Timestamp)get_Value(COLUMNNAME_fecha);
	}

	/** Set OFB_GenerateSeries ID.
		@param OFB_GenerateSeries_ID OFB_GenerateSeries ID	  */
	public void setOFB_GenerateSeries_ID (int OFB_GenerateSeries_ID)
	{
		if (OFB_GenerateSeries_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_OFB_GenerateSeries_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_OFB_GenerateSeries_ID, Integer.valueOf(OFB_GenerateSeries_ID));
	}

	/** Get OFB_GenerateSeries ID.
		@return OFB_GenerateSeries ID	  */
	public int getOFB_GenerateSeries_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_OFB_GenerateSeries_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}