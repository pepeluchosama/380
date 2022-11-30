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

/** Generated Interface for R_ServiceRequest
 *  @author Adempiere (generated) 
 *  @version Release 3.8.0
 */
public interface I_R_ServiceRequest 
{

    /** TableName=R_ServiceRequest */
    public static final String Table_Name = "R_ServiceRequest";

    /** AD_Table_ID=2000030 */
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

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException;

    /** Column name AD_WF_NextNode_ID */
    public static final String COLUMNNAME_AD_WF_NextNode_ID = "AD_WF_NextNode_ID";

	/** Set AD_WF_NextNode_ID	  */
	public void setAD_WF_NextNode_ID (int AD_WF_NextNode_ID);

	/** Get AD_WF_NextNode_ID	  */
	public int getAD_WF_NextNode_ID();

	public org.compiere.model.I_AD_WF_Node getAD_WF_NextNode() throws RuntimeException;

    /** Column name AD_WF_Node_ID */
    public static final String COLUMNNAME_AD_WF_Node_ID = "AD_WF_Node_ID";

	/** Set Node.
	  * Workflow Node (activity), step or process
	  */
	public void setAD_WF_Node_ID (int AD_WF_Node_ID);

	/** Get Node.
	  * Workflow Node (activity), step or process
	  */
	public int getAD_WF_Node_ID();

	public org.compiere.model.I_AD_WF_Node getAD_WF_Node() throws RuntimeException;

    /** Column name AD_Workflow_ID */
    public static final String COLUMNNAME_AD_Workflow_ID = "AD_Workflow_ID";

	/** Set Workflow.
	  * Workflow or combination of tasks
	  */
	public void setAD_Workflow_ID (int AD_Workflow_ID);

	/** Get Workflow.
	  * Workflow or combination of tasks
	  */
	public int getAD_Workflow_ID();

	public org.compiere.model.I_AD_Workflow getAD_Workflow() throws RuntimeException;

    /** Column name BtnServiceRequest */
    public static final String COLUMNNAME_BtnServiceRequest = "BtnServiceRequest";

	/** Set BtnServiceRequest	  */
	public void setBtnServiceRequest (String BtnServiceRequest);

	/** Get BtnServiceRequest	  */
	public String getBtnServiceRequest();

    /** Column name C_Order_ID */
    public static final String COLUMNNAME_C_Order_ID = "C_Order_ID";

	/** Set Order.
	  * Order
	  */
	public void setC_Order_ID (int C_Order_ID);

	/** Get Order.
	  * Order
	  */
	public int getC_Order_ID();

	public org.compiere.model.I_C_Order getC_Order() throws RuntimeException;

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

    /** Column name CreditCardType */
    public static final String COLUMNNAME_CreditCardType = "CreditCardType";

	/** Set Credit Card.
	  * Credit Card (Visa, MC, AmEx)
	  */
	public void setCreditCardType (String CreditCardType);

	/** Get Credit Card.
	  * Credit Card (Visa, MC, AmEx)
	  */
	public String getCreditCardType();

    /** Column name DocStatus */
    public static final String COLUMNNAME_DocStatus = "DocStatus";

	/** Set Document Status.
	  * The current status of the document
	  */
	public void setDocStatus (String DocStatus);

	/** Get Document Status.
	  * The current status of the document
	  */
	public String getDocStatus();

    /** Column name DocumentNo */
    public static final String COLUMNNAME_DocumentNo = "DocumentNo";

	/** Set Document No.
	  * Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo);

	/** Get Document No.
	  * Document sequence number of the document
	  */
	public String getDocumentNo();

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

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name R_Category_ID */
    public static final String COLUMNNAME_R_Category_ID = "R_Category_ID";

	/** Set Category.
	  * Request Category
	  */
	public void setR_Category_ID (int R_Category_ID);

	/** Get Category.
	  * Request Category
	  */
	public int getR_Category_ID();

	public org.compiere.model.I_R_Category getR_Category() throws RuntimeException;

    /** Column name R_Request_ID */
    public static final String COLUMNNAME_R_Request_ID = "R_Request_ID";

	/** Set Request.
	  * Request from a Business Partner or Prospect
	  */
	public void setR_Request_ID (int R_Request_ID);

	/** Get Request.
	  * Request from a Business Partner or Prospect
	  */
	public int getR_Request_ID();

	public org.compiere.model.I_R_Request getR_Request() throws RuntimeException;

    /** Column name R_RequestType_ID */
    public static final String COLUMNNAME_R_RequestType_ID = "R_RequestType_ID";

	/** Set Request Type.
	  * Type of request (e.g. Inquiry, Complaint, ..)
	  */
	public void setR_RequestType_ID (int R_RequestType_ID);

	/** Get Request Type.
	  * Type of request (e.g. Inquiry, Complaint, ..)
	  */
	public int getR_RequestType_ID();

	public org.compiere.model.I_R_RequestType getR_RequestType() throws RuntimeException;

    /** Column name R_Resolution_ID */
    public static final String COLUMNNAME_R_Resolution_ID = "R_Resolution_ID";

	/** Set Resolution.
	  * Request Resolution
	  */
	public void setR_Resolution_ID (int R_Resolution_ID);

	/** Get Resolution.
	  * Request Resolution
	  */
	public int getR_Resolution_ID();

	public org.compiere.model.I_R_Resolution getR_Resolution() throws RuntimeException;

    /** Column name R_ServiceRequest_ID */
    public static final String COLUMNNAME_R_ServiceRequest_ID = "R_ServiceRequest_ID";

	/** Set ServiceRequest ID	  */
	public void setR_ServiceRequest_ID (int R_ServiceRequest_ID);

	/** Get ServiceRequest ID	  */
	public int getR_ServiceRequest_ID();

    /** Column name SalesRep_ID */
    public static final String COLUMNNAME_SalesRep_ID = "SalesRep_ID";

	/** Set Sales Representative.
	  * Sales Representative or Company Agent
	  */
	public void setSalesRep_ID (int SalesRep_ID);

	/** Get Sales Representative.
	  * Sales Representative or Company Agent
	  */
	public int getSalesRep_ID();

	public org.compiere.model.I_AD_User getSalesRep() throws RuntimeException;

    /** Column name Sector */
    public static final String COLUMNNAME_Sector = "Sector";

	/** Set Sector	  */
	public void setSector (int Sector);

	/** Get Sector	  */
	public int getSector();

    /** Column name SR_AccountType */
    public static final String COLUMNNAME_SR_AccountType = "SR_AccountType";

	/** Set SR_AccountType	  */
	public void setSR_AccountType (String SR_AccountType);

	/** Get SR_AccountType	  */
	public String getSR_AccountType();

    /** Column name SR_Amt */
    public static final String COLUMNNAME_SR_Amt = "SR_Amt";

	/** Set SR_Amt	  */
	public void setSR_Amt (BigDecimal SR_Amt);

	/** Get SR_Amt	  */
	public BigDecimal getSR_Amt();

    /** Column name SR_BPartner_Loaction_ID */
    public static final String COLUMNNAME_SR_BPartner_Loaction_ID = "SR_BPartner_Loaction_ID";

	/** Set SR_BPartner_Loaction_ID	  */
	public void setSR_BPartner_Loaction_ID (int SR_BPartner_Loaction_ID);

	/** Get SR_BPartner_Loaction_ID	  */
	public int getSR_BPartner_Loaction_ID();

	public org.compiere.model.I_C_BPartner_Location getSR_BPartner_Loaction() throws RuntimeException;

    /** Column name SR_BPartnerRef_ID */
    public static final String COLUMNNAME_SR_BPartnerRef_ID = "SR_BPartnerRef_ID";

	/** Set SR_BPartnerRef_ID	  */
	public void setSR_BPartnerRef_ID (int SR_BPartnerRef_ID);

	/** Get SR_BPartnerRef_ID	  */
	public int getSR_BPartnerRef_ID();

	public org.compiere.model.I_C_BPartner getSR_BPartnerRef() throws RuntimeException;

    /** Column name SR_ChargeDate */
    public static final String COLUMNNAME_SR_ChargeDate = "SR_ChargeDate";

	/** Set SR_ChargeDate	  */
	public void setSR_ChargeDate (Timestamp SR_ChargeDate);

	/** Get SR_ChargeDate	  */
	public Timestamp getSR_ChargeDate();

    /** Column name SR_Comments */
    public static final String COLUMNNAME_SR_Comments = "SR_Comments";

	/** Set SR_Comments	  */
	public void setSR_Comments (String SR_Comments);

	/** Get SR_Comments	  */
	public String getSR_Comments();

    /** Column name SR_CorrectValue */
    public static final String COLUMNNAME_SR_CorrectValue = "SR_CorrectValue";

	/** Set SR_CorrectValue	  */
	public void setSR_CorrectValue (String SR_CorrectValue);

	/** Get SR_CorrectValue	  */
	public String getSR_CorrectValue();

    /** Column name SR_CreditCardDueDate */
    public static final String COLUMNNAME_SR_CreditCardDueDate = "SR_CreditCardDueDate";

	/** Set SR_CreditCardDueDate	  */
	public void setSR_CreditCardDueDate (String SR_CreditCardDueDate);

	/** Get SR_CreditCardDueDate	  */
	public String getSR_CreditCardDueDate();

    /** Column name SR_CreditCardNo */
    public static final String COLUMNNAME_SR_CreditCardNo = "SR_CreditCardNo";

	/** Set SR_CreditCardNo	  */
	public void setSR_CreditCardNo (String SR_CreditCardNo);

	/** Get SR_CreditCardNo	  */
	public String getSR_CreditCardNo();

    /** Column name SR_Date */
    public static final String COLUMNNAME_SR_Date = "SR_Date";

	/** Set SR_Date	  */
	public void setSR_Date (Timestamp SR_Date);

	/** Get SR_Date	  */
	public Timestamp getSR_Date();

    /** Column name SR_Description */
    public static final String COLUMNNAME_SR_Description = "SR_Description";

	/** Set SR_Description	  */
	public void setSR_Description (String SR_Description);

	/** Get SR_Description	  */
	public String getSR_Description();

    /** Column name SR_DueDate */
    public static final String COLUMNNAME_SR_DueDate = "SR_DueDate";

	/** Set SR_DueDate	  */
	public void setSR_DueDate (Timestamp SR_DueDate);

	/** Get SR_DueDate	  */
	public Timestamp getSR_DueDate();

    /** Column name SR_EditionNo */
    public static final String COLUMNNAME_SR_EditionNo = "SR_EditionNo";

	/** Set SR_EditionNo	  */
	public void setSR_EditionNo (String SR_EditionNo);

	/** Get SR_EditionNo	  */
	public String getSR_EditionNo();

    /** Column name SR_EMail */
    public static final String COLUMNNAME_SR_EMail = "SR_EMail";

	/** Set SR_EMail	  */
	public void setSR_EMail (String SR_EMail);

	/** Get SR_EMail	  */
	public String getSR_EMail();

    /** Column name SR_InvoiceNo */
    public static final String COLUMNNAME_SR_InvoiceNo = "SR_InvoiceNo";

	/** Set SR_InvoiceNo	  */
	public void setSR_InvoiceNo (String SR_InvoiceNo);

	/** Get SR_InvoiceNo	  */
	public String getSR_InvoiceNo();

    /** Column name SR_Last4Digits */
    public static final String COLUMNNAME_SR_Last4Digits = "SR_Last4Digits";

	/** Set SR_Last4Digits	  */
	public void setSR_Last4Digits (String SR_Last4Digits);

	/** Get SR_Last4Digits	  */
	public String getSR_Last4Digits();

    /** Column name SR_Name */
    public static final String COLUMNNAME_SR_Name = "SR_Name";

	/** Set SR_Name	  */
	public void setSR_Name (String SR_Name);

	/** Get SR_Name	  */
	public String getSR_Name();

    /** Column name SR_NC */
    public static final String COLUMNNAME_SR_NC = "SR_NC";

	/** Set SR_NC	  */
	public void setSR_NC (BigDecimal SR_NC);

	/** Get SR_NC	  */
	public BigDecimal getSR_NC();

    /** Column name SR_OperationNo */
    public static final String COLUMNNAME_SR_OperationNo = "SR_OperationNo";

	/** Set SR_OperationNo	  */
	public void setSR_OperationNo (String SR_OperationNo);

	/** Get SR_OperationNo	  */
	public String getSR_OperationNo();

    /** Column name SR_PartialOrTotal */
    public static final String COLUMNNAME_SR_PartialOrTotal = "SR_PartialOrTotal";

	/** Set SR_PartialOrTotal	  */
	public void setSR_PartialOrTotal (String SR_PartialOrTotal);

	/** Get SR_PartialOrTotal	  */
	public String getSR_PartialOrTotal();

    /** Column name SR_Phone */
    public static final String COLUMNNAME_SR_Phone = "SR_Phone";

	/** Set SR_Phone	  */
	public void setSR_Phone (String SR_Phone);

	/** Get SR_Phone	  */
	public String getSR_Phone();

    /** Column name SR_PhoneUser_ID */
    public static final String COLUMNNAME_SR_PhoneUser_ID = "SR_PhoneUser_ID";

	/** Set SR_PhoneUser_ID	  */
	public void setSR_PhoneUser_ID (int SR_PhoneUser_ID);

	/** Get SR_PhoneUser_ID	  */
	public int getSR_PhoneUser_ID();

	public org.compiere.model.I_AD_User getSR_PhoneUser() throws RuntimeException;

    /** Column name SR_Reason */
    public static final String COLUMNNAME_SR_Reason = "SR_Reason";

	/** Set SR_Reason	  */
	public void setSR_Reason (String SR_Reason);

	/** Get SR_Reason	  */
	public String getSR_Reason();

    /** Column name SR_Reference */
    public static final String COLUMNNAME_SR_Reference = "SR_Reference";

	/** Set SR_Reference	  */
	public void setSR_Reference (String SR_Reference);

	/** Get SR_Reference	  */
	public String getSR_Reference();

    /** Column name SR_User_ID */
    public static final String COLUMNNAME_SR_User_ID = "SR_User_ID";

	/** Set SR_User_ID	  */
	public void setSR_User_ID (int SR_User_ID);

	/** Get SR_User_ID	  */
	public int getSR_User_ID();

	public org.compiere.model.I_AD_User getSR_User() throws RuntimeException;

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

    /** Column name User1_ID */
    public static final String COLUMNNAME_User1_ID = "User1_ID";

	/** Set User List 1.
	  * User defined list element #1
	  */
	public void setUser1_ID (int User1_ID);

	/** Get User List 1.
	  * User defined list element #1
	  */
	public int getUser1_ID();

	public org.compiere.model.I_AD_User getUser1() throws RuntimeException;

    /** Column name Zone */
    public static final String COLUMNNAME_Zone = "Zone";

	/** Set Zone	  */
	public void setZone (String Zone);

	/** Get Zone	  */
	public String getZone();
}
