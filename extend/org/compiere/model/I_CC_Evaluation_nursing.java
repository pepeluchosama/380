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

/** Generated Interface for CC_Evaluation_nursing
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0
 */
public interface I_CC_Evaluation_nursing 
{

    /** TableName=CC_Evaluation_nursing */
    public static final String Table_Name = "CC_Evaluation_nursing";

    /** AD_Table_ID=2000048 */
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

    /** Column name AD_User_ID */
    public static final String COLUMNNAME_AD_User_ID = "AD_User_ID";

	/** Set User/Contact.
	  * User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID);

	/** Get User/Contact.
	  * User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID();

    /** Column name AD_User2_ID */
    public static final String COLUMNNAME_AD_User2_ID = "AD_User2_ID";

	/** Set User/Contact2.
	  * User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User2_ID (int AD_User2_ID);

	/** Get User/Contact2.
	  * User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User2_ID();

    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/** Set Business Partner .
	  * Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/** Get Business Partner .
	  * Identifies a Business Partner
	  */
	public int getC_BPartner_ID();

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException;

    /** Column name CC_Evaluation_nursing_ID */
    public static final String COLUMNNAME_CC_Evaluation_nursing_ID = "CC_Evaluation_nursing_ID";

	/** Set CC_Evaluation_nursing ID	  */
	public void setCC_Evaluation_nursing_ID (int CC_Evaluation_nursing_ID);

	/** Get CC_Evaluation_nursing ID	  */
	public int getCC_Evaluation_nursing_ID();

    /** Column name CC_Hospitalization_ID */
    public static final String COLUMNNAME_CC_Hospitalization_ID = "CC_Hospitalization_ID";

	/** Set CC_Hospitalization ID	  */
	public void setCC_Hospitalization_ID (int CC_Hospitalization_ID);

	/** Get CC_Hospitalization ID	  */
	public int getCC_Hospitalization_ID();

	public I_CC_Hospitalization getCC_Hospitalization() throws RuntimeException;

    /** Column name Comments */
    public static final String COLUMNNAME_Comments = "Comments";

	/** Set Comments.
	  * Comments or additional information
	  */
	public void setComments (String Comments);

	/** Get Comments.
	  * Comments or additional information
	  */
	public String getComments();

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

    /** Column name DateTrx */
    public static final String COLUMNNAME_DateTrx = "DateTrx";

	/** Set Transaction Date.
	  * Transaction Date
	  */
	public void setDateTrx (Timestamp DateTrx);

	/** Get Transaction Date.
	  * Transaction Date
	  */
	public Timestamp getDateTrx();

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

    /** Column name Description1 */
    public static final String COLUMNNAME_Description1 = "Description1";

	/** Set Description1.
	  * Optional short description of the record
	  */
	public void setDescription1 (String Description1);

	/** Get Description1.
	  * Optional short description of the record
	  */
	public String getDescription1();

    /** Column name Diuresis */
    public static final String COLUMNNAME_Diuresis = "Diuresis";

	/** Set Diuresis	  */
	public void setDiuresis (BigDecimal Diuresis);

	/** Get Diuresis	  */
	public BigDecimal getDiuresis();

    /** Column name Diuresis1 */
    public static final String COLUMNNAME_Diuresis1 = "Diuresis1";

	/** Set Diuresis1	  */
	public void setDiuresis1 (BigDecimal Diuresis1);

	/** Get Diuresis1	  */
	public BigDecimal getDiuresis1();

    /** Column name Diuresis2 */
    public static final String COLUMNNAME_Diuresis2 = "Diuresis2";

	/** Set Diuresis2	  */
	public void setDiuresis2 (BigDecimal Diuresis2);

	/** Get Diuresis2	  */
	public BigDecimal getDiuresis2();

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

    /** Column name IsReadOnly1 */
    public static final String COLUMNNAME_IsReadOnly1 = "IsReadOnly1";

	/** Set IsReadOnly1	  */
	public void setIsReadOnly1 (boolean IsReadOnly1);

	/** Get IsReadOnly1	  */
	public boolean isReadOnly1();

    /** Column name Pulse */
    public static final String COLUMNNAME_Pulse = "Pulse";

	/** Set Pulse	  */
	public void setPulse (BigDecimal Pulse);

	/** Get Pulse	  */
	public BigDecimal getPulse();

    /** Column name SizeX */
    public static final String COLUMNNAME_SizeX = "SizeX";

	/** Set Size X.
	  * X (horizontal) dimension size
	  */
	public void setSizeX (BigDecimal SizeX);

	/** Get Size X.
	  * X (horizontal) dimension size
	  */
	public BigDecimal getSizeX();

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

    /** Column name Weight */
    public static final String COLUMNNAME_Weight = "Weight";

	/** Set Weight.
	  * Weight of a product
	  */
	public void setWeight (BigDecimal Weight);

	/** Get Weight.
	  * Weight of a product
	  */
	public BigDecimal getWeight();
}
