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

/** Generated Interface for HR_HoursUsedDetail
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0
 */
public interface I_HR_HoursUsedDetail 
{

    /** TableName=HR_HoursUsedDetail */
    public static final String Table_Name = "HR_HoursUsedDetail";

    /** AD_Table_ID=2000066 */
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

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name hours */
    public static final String COLUMNNAME_hours = "hours";

	/** Set hours	  */
	public void sethours (int hours);

	/** Get hours	  */
	public int gethours();

    /** Column name HR_AdministrativeRequests_ID */
    public static final String COLUMNNAME_HR_AdministrativeRequests_ID = "HR_AdministrativeRequests_ID";

	/** Set HR_AdministrativeRequests	  */
	public void setHR_AdministrativeRequests_ID (int HR_AdministrativeRequests_ID);

	/** Get HR_AdministrativeRequests	  */
	public int getHR_AdministrativeRequests_ID();

	public I_HR_AdministrativeRequests getHR_AdministrativeRequests() throws RuntimeException;

    /** Column name HR_HourPlanningLine_ID */
    public static final String COLUMNNAME_HR_HourPlanningLine_ID = "HR_HourPlanningLine_ID";

	/** Set Hour Planning Line  ID	  */
	public void setHR_HourPlanningLine_ID (int HR_HourPlanningLine_ID);

	/** Get Hour Planning Line  ID	  */
	public int getHR_HourPlanningLine_ID();

	public I_HR_HourPlanningLine getHR_HourPlanningLine() throws RuntimeException;

    /** Column name HR_HoursAvailable_ID */
    public static final String COLUMNNAME_HR_HoursAvailable_ID = "HR_HoursAvailable_ID";

	/** Set HR_HoursAvailable ID	  */
	public void setHR_HoursAvailable_ID (int HR_HoursAvailable_ID);

	/** Get HR_HoursAvailable ID	  */
	public int getHR_HoursAvailable_ID();


    /** Column name HR_HoursUsedDetail_ID */
    public static final String COLUMNNAME_HR_HoursUsedDetail_ID = "HR_HoursUsedDetail_ID";

	/** Set HR_HoursUsedDetail ID	  */
	public void setHR_HoursUsedDetail_ID (int HR_HoursUsedDetail_ID);

	/** Get HR_HoursUsedDetail ID	  */
	public int getHR_HoursUsedDetail_ID();

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
