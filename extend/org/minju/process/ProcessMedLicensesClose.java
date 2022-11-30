/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.minju.process;

import java.util.logging.Level;

import org.compiere.model.X_RH_MedicalLicenses;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

/**
 *	
 *	
 *  @author @mfrojas
 *  @version $Id: SeparateInvoices.java $
 */
public class ProcessMedLicensesClose extends SvrProcess
{
	//private String			p_DocStatus = null;
	private int				p_RH_MLicenses = 0; 
	//private int				role_ID = 0;
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	 protected void prepare()
	{
		 ProcessInfoParameter[] para = getParameter();
			for (int i = 0; i < para.length; i++)
			{
				String name = para[i].getParameterName();
				
				if (name.equals("DocStatus"))
					;//p_DocStatus = (String) para[i].getParameter();
				else
					log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
			p_RH_MLicenses=getRecord_ID();
			//role_ID = Env.getAD_Role_ID(getCtx());
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		String logM = "";
		if (p_RH_MLicenses > 0)
		{
			X_RH_MedicalLicenses med = new X_RH_MedicalLicenses(getCtx(),p_RH_MLicenses, get_TrxName());
			
			med.setDocStatus("CL");
			med.setProcessed(true);
			med.save();
		}
	   return "Procesado "+logM;
	   
	}	//	doIt

}
