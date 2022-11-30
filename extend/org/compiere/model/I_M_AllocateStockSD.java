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
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;

/** Generated Interface for M_AllocateStockSD
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0
 */
public interface I_M_AllocateStockSD 
{

    /** TableName=M_AllocateStockSD */
    public static final String Table_Name = "M_AllocateStockSD";

    /** AD_Table_ID=2000042 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name C_OrderLine_ID */
    public static final String COLUMNNAME_C_OrderLine_ID = "C_OrderLine_ID";

	/** Set Sales Order Line.
	  * Sales Order Line
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID);

	/** Get Sales Order Line.
	  * Sales Order Line
	  */
	public int getC_OrderLine_ID();

	public org.compiere.model.I_C_OrderLine getC_OrderLine() throws RuntimeException;

    /** Column name CCPPKg */
    public static final String COLUMNNAME_CCPPKg = "CCPPKg";

	/** Set CCPPKg	  */
	public void setCCPPKg (BigDecimal CCPPKg);

	/** Get CCPPKg	  */
	public BigDecimal getCCPPKg();

    /** Column name CCPPUMB */
    public static final String COLUMNNAME_CCPPUMB = "CCPPUMB";

	/** Set CCPPUMB	  */
	public void setCCPPUMB (BigDecimal CCPPUMB);

	/** Get CCPPUMB	  */
	public BigDecimal getCCPPUMB();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name I_InventoryCencosudSD_ID */
    public static final String COLUMNNAME_I_InventoryCencosudSD_ID = "I_InventoryCencosudSD_ID";

	/** Set I_InventoryCencosudSD ID	  */
	public void setI_InventoryCencosudSD_ID (int I_InventoryCencosudSD_ID);

	/** Get I_InventoryCencosudSD ID	  */
	public int getI_InventoryCencosudSD_ID();

	public I_I_InventoryCencosudSD getI_InventoryCencosudSD() throws RuntimeException;

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name M_AllocateStockSD_ID */
    public static final String COLUMNNAME_M_AllocateStockSD_ID = "M_AllocateStockSD_ID";

	/** Set M_AllocateStockSD ID	  */
	public void setM_AllocateStockSD_ID (int M_AllocateStockSD_ID);

	/** Get M_AllocateStockSD ID	  */
	public int getM_AllocateStockSD_ID();

    /** Column name M_PriorityCenco_ID */
    public static final String COLUMNNAME_M_PriorityCenco_ID = "M_PriorityCenco_ID";

	/** Set M_PriorityCenco_ID	  */
	public void setM_PriorityCenco_ID (int M_PriorityCenco_ID);

	/** Get M_PriorityCenco_ID	  */
	public int getM_PriorityCenco_ID();

	public I_M_PriorityCenco getM_PriorityCenco() throws RuntimeException;

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
}
