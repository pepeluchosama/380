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

/** Generated Model for WS_InfoPDF
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0 - $Id$ */
public class X_WS_InfoPDF extends PO implements I_WS_InfoPDF, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20220517L;

    /** Standard Constructor */
    public X_WS_InfoPDF (Properties ctx, int WS_InfoPDF_ID, String trxName)
    {
      super (ctx, WS_InfoPDF_ID, trxName);
      /** if (WS_InfoPDF_ID == 0)
        {
			setWS_InfoPDF_ID (0);
        } */
    }

    /** Load Constructor */
    public X_WS_InfoPDF (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_WS_InfoPDF[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	/** Set Document No.
		@param DocumentNo 
		Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo)
	{
		set_Value (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

	/** Set File Name.
		@param FileName 
		Name of the local file or URL
	  */
	public void setFileName (String FileName)
	{
		set_Value (COLUMNNAME_FileName, FileName);
	}

	/** Get File Name.
		@return Name of the local file or URL
	  */
	public String getFileName () 
	{
		return (String)get_Value(COLUMNNAME_FileName);
	}

	/** Set Rut.
		@param Rut Rut	  */
	public void setRut (String Rut)
	{
		set_Value (COLUMNNAME_Rut, Rut);
	}

	/** Get Rut.
		@return Rut	  */
	public String getRut () 
	{
		return (String)get_Value(COLUMNNAME_Rut);
	}

	/** Set WS_InfoPDF ID.
		@param WS_InfoPDF_ID WS_InfoPDF ID	  */
	public void setWS_InfoPDF_ID (int WS_InfoPDF_ID)
	{
		if (WS_InfoPDF_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_WS_InfoPDF_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_WS_InfoPDF_ID, Integer.valueOf(WS_InfoPDF_ID));
	}

	/** Get WS_InfoPDF ID.
		@return WS_InfoPDF ID	  */
	public int getWS_InfoPDF_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_WS_InfoPDF_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}