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
package org.clinicacolonial.process;

import java.sql.*;
import java.util.logging.*;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.X_MED_Appointment;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.*;
/**
 *	
 *	
 *  @author mfrojas
 *  @version $Id: ProcessBlockSchedule.java $
 */

public class ProcessBlockSchedule extends SvrProcess
{
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	
	private int 			p_BPartnerMed_ID = 0;
	private int 			p_MEDSpecialty_ID = 0;
	private Timestamp 		p_DateFrom;
	private Timestamp 		p_DateTo;
	private Timestamp 		p_TimeFrom;
	private Timestamp 		p_TimeTo;
	
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			

			if(name.equals("C_BPartnerMed_ID"))
				p_BPartnerMed_ID = para[i].getParameterAsInt();		
			else if(name.equals("DateFrom"))
				p_DateFrom = para[i].getParameterAsTimestamp();
			else if(name.equals("DateTo"))
				p_DateTo = para[i].getParameterAsTimestamp();
			else if(name.equals("TimeFrom"))
				p_TimeFrom = para[i].getParameterAsTimestamp();
			else if(name.equals("TimeTo"))
				p_TimeTo = para[i].getParameterAsTimestamp();
			else if(name.equals("MED_Specialty_ID"))
				p_MEDSpecialty_ID = para[i].getParameterAsInt();		
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{	
		if(p_BPartnerMed_ID == 0)
			throw new IllegalArgumentException("No ha seleccionado un medico");
		if(p_MEDSpecialty_ID == 0)
			throw new AdempiereException("No ha seleccionado especialidad");
		if(p_DateFrom == null)
			throw new IllegalArgumentException("No ha seleccionado una fecha/hora desde");
		if(p_DateTo == null)
			throw new IllegalArgumentException("No ha seleccionado una fecha/hora hasta");
		if(p_TimeFrom == null)
			throw new IllegalArgumentException("No ha seleccionado una fecha/hora desde");
		if(p_TimeTo == null)
			throw new IllegalArgumentException("No ha seleccionado una fecha/hora hasta");
		
		/*
		String sqlcount = "SELECT count(1)  FROM MED_ScheduleTime  WHERE isactive='Y' AND State='DI' " +
				" AND timerequested::date >= '"+p_DateFrom+"' " +
				" AND timerequested::date <= '"+p_DateTo+"'::date " +
				" AND timerequested::time >= '"+p_TimeFrom+"' " +
				" AND timerequested::time <= '"+p_TimeTo+"' " +
				" AND MED_ScheduleDay_ID in (" +
				" SELECT MED_ScheduleDay_ID from MED_ScheduleDay WHERE MED_Schedule_ID in " +
				" ( SELECT MED_Schedule_ID from MED_Schedule WHERE C_BPartnerMed_ID = "+p_BPartnerMed_ID+"))";
		
		String sqlupdate = "UPDATE MED_ScheduleTime SET State = 'BL' WHERE isactive='Y' AND State='DI' " +
				" AND timerequested::date >= '"+p_DateFrom+"' " +
				" AND timerequested::date <= '"+p_DateTo+"'::date " +
				" AND timerequested::time >= '"+p_TimeFrom+"' " +
				" AND timerequested::time <= '"+p_TimeTo+"' " +
				" AND MED_ScheduleDay_ID in (" +
				" SELECT MED_ScheduleDay_ID from MED_ScheduleDay WHERE MED_Schedule_ID in " +
				" ( SELECT MED_Schedule_ID from MED_Schedule WHERE C_BPartnerMed_ID = "+p_BPartnerMed_ID+"))";
		log.config(sqlcount);
		int counter = DB.getSQLValue(get_TrxName(), sqlcount);
		if(counter > 0)
			DB.executeUpdate(sqlupdate, get_TrxName());
		*/
		
		//nuevo metodo de bloqueo de horas
		String sqlDet = "SELECT MED_ScheduleTime_ID "
				+ " FROM MED_ScheduleTime st "
				+ " INNER JOIN MED_ScheduleDay sd ON (st.MED_ScheduleDay_ID = sd.MED_ScheduleDay_ID)"
				+ " INNER JOIN MED_Schedule s ON (s.MED_Schedule_ID = sd.MED_Schedule_ID)"
				+ " WHERE s.C_BPartnerMed_ID="+p_BPartnerMed_ID+" AND s.MED_Specialty_ID="+p_MEDSpecialty_ID
				+ " AND st.isactive='Y'"
				/*+" AND st.timerequested::date >= '"+p_DateFrom+"' "
				+" AND st.timerequested::date <= '"+p_DateTo+"'::date "
				+" AND st.timerequested::time >= '"+p_TimeFrom+"' "
				+" AND st.timerequested::time <= '"+p_TimeTo+"' ";*/
				+" AND st.timerequested >= ? AND st.timerequested <= ?";
		
		log.config("sql de fecha: "+sqlDet);
		Timestamp timeFrom = new Timestamp(p_DateFrom.getTime());
		timeFrom.setHours(p_TimeFrom.getHours());
		timeFrom.setMinutes(p_TimeFrom.getMinutes());
		timeFrom.setSeconds(p_TimeFrom.getSeconds());
		
		Timestamp timeTo = new Timestamp(p_DateTo.getTime());
		timeTo.setHours(p_TimeTo.getHours());
		timeTo.setMinutes(p_TimeTo.getMinutes());
		timeTo.setSeconds(p_TimeTo.getSeconds());
		
		
		PreparedStatement pstmt = DB.prepareStatement(sqlDet, get_TrxName());
		pstmt.setTimestamp(1, timeFrom);
		pstmt.setTimestamp(2, timeTo);
		ResultSet rs = pstmt.executeQuery();
		int ID_MEDAppointment = 0;
		int cant = 0;
		String ID_anular = "0";
		try
		{
			while (rs.next())
			{	
				cant++;
				//se verifica si existe cita asociada 
				ID_MEDAppointment =  DB.getSQLValue(get_TrxName(), "SELECT MAX(MED_Appointment_ID) FROM MED_Appointment "
						+ " WHERE MED_ScheduleTime_ID="+rs.getInt("MED_ScheduleTime_ID"));
				if(ID_MEDAppointment > 0) //si existe cita se guarda informacion en la descripcion y se borra cita
				{
					X_MED_Appointment appoint = new X_MED_Appointment(getCtx(), ID_MEDAppointment, get_TrxName());
					//se actualiza hora
					MBPartner bp = new MBPartner(getCtx(), appoint.getC_BPartner_ID(),get_TrxName());
					DB.executeUpdate("UPDATE MED_ScheduleTime SET State = 'BL', Description='Cita Borrada a paciente "+bp.getValue()+
							"-"+bp.get_ValueAsString("digito")+" "+bp.get_ValueAsString("name1")+" "+bp.getName2()+
							" "+bp.get_ValueAsString("name3")+"' WHERE MED_ScheduleTime_ID="+rs.getInt("MED_ScheduleTime_ID"),get_TrxName());
					
					//se borra cita
					DB.executeUpdate("DELETE FROM MED_Appointment WHERE MED_Appointment_ID="+ID_MEDAppointment, get_TrxName());
				}
				else
					ID_anular = ID_anular + ","+rs.getInt("MED_ScheduleTime_ID");
			}
			//se hace update de id sin cita
			DB.executeUpdate("UPDATE MED_ScheduleTime SET State = 'BL' WHERE MED_ScheduleTime_ID IN ("+ID_anular+")",get_TrxName());
			
		}
		catch (Exception e)
		{
			log.config(e.toString());
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		return "Actualizados: "+cant+" registros";
	}
}
