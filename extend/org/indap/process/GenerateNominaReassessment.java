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
package org.indap.process;

import java.util.Calendar;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MConversionRate;
import org.compiere.model.MConversionType;
import org.compiere.model.MDocType;
import org.compiere.model.MPeriod;
import org.compiere.model.X_HR_EmployeeChange;
import org.compiere.model.X_HR_ProcessBP;
import org.compiere.model.X_HR_ProcessBPDetail;
import org.compiere.model.X_HR_ProcessHeader;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.DB;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.eevolution.model.MHREmployee;
import org.eevolution.model.X_HR_Concept;
import org.eevolution.model.X_HR_Process;
import org.ofb.model.OFBForward;
import org.ofb.utils.DateUtils;

/**
 *	
 *	
 *  @author Italo Ni�oles ininoles
 *  @version $Id: ProcessNomina.java $
 */
public class GenerateNominaReassessment extends SvrProcess
{
	private int				p_C_BPartner_ID=0;
	private int				p_AD_Org_ID=0;
	private int				p_Parent_Org_ID=0;
	private String			p_suplencia="";
	private int				p_PeriodFrom_ID=0;
	private int				p_PeriodTo_ID=0;
	private int				p_ProcessBP_ID=0;
	
	private static CLogger		s_log = CLogger.getCLogger(MConversionRate.class);
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	 protected void prepare()
	{
		 ProcessInfoParameter[] para = getParameter();
			for (int i = 0; i < para.length; i++)
			{
				String name = para[i].getParameterName();				
				if (name.equals("C_BPartner_ID"))
					p_C_BPartner_ID = para[i].getParameterAsInt();
				else if (name.equals("AD_Org_ID"))
					p_AD_Org_ID = para[i].getParameterAsInt();
				else if (name.equals("Parent_Org_ID"))
					p_Parent_Org_ID = para[i].getParameterAsInt();
				else if (name.equals("Suplencia"))
					p_suplencia = para[i].getParameterAsString();
				else if (name.equals("C_Period_ID"))
				{
					p_PeriodFrom_ID = para[i].getParameterAsInt();
					p_PeriodTo_ID = para[i].getParameter_ToAsInt();
				}
				else if (name.equals("HR_ProcessBPRef_ID"))
					p_ProcessBP_ID = para[i].getParameterAsInt();
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
		String logM = "";
			//validacion de parametros llenos
		if(p_AD_Org_ID <=0 && p_Parent_Org_ID <=0 && p_C_BPartner_ID <=0)
			throw new AdempiereException("Debe seleccionar una organizaci�n o funcionario");
		if(p_PeriodFrom_ID <=0 || p_PeriodTo_ID <=0)
			throw new AdempiereException("Debe seleccionar un rango de periodos");
		int ID_Periodo = p_PeriodFrom_ID;
		
		//se crea cabecera
		X_HR_ProcessHeader head = new X_HR_ProcessHeader(getCtx(), 0, get_TrxName());
		head.setC_Period_ID(p_PeriodFrom_ID);
		head.setC_PeriodTo_ID(p_PeriodTo_ID);
		head.setDescription("Reliquidaci�n "+head.getC_Period().getName()+" a "+head.getC_PeriodTo().getName());
		head.saveEx(get_TrxName());
		//variable periodo
		Timestamp startYear = new Timestamp(head.getC_Period().getStartDate().getTime());
		startYear = DateUtils.SumarMeses(head.getC_Period().getStartDate(), -2);
	
		Timestamp endYear = new Timestamp(head.getC_PeriodTo().getEndDate().getTime());
		String ID_BpartnerUsed = "0";
		while (ID_Periodo <= p_PeriodTo_ID)
		{
			//MPeriod peFrom = new MPeriod(getCtx(), p_PeriodFrom_ID, get_TrxName());
			MPeriod periodo = new MPeriod(getCtx(), ID_Periodo, get_TrxName());
			
			X_HR_Process pro = new X_HR_Process(getCtx(),0, get_TrxName());
			pro.setAD_Org_ID(Env.getAD_Org_ID(getCtx()));
			pro.setC_DocTypeTarget_ID(MDocType.getDocType(MDocType.DOCBASETYPE_Payroll));
			pro.setName("Reliquidaci�n "+periodo.getName());
			pro.setDateAcct(periodo.getEndDate());
			pro.setHR_Payroll_ID(2000002);
			pro.set_CustomColumn("C_Period_ID",periodo.get_ID());
			pro.setC_DocType_ID(MDocType.getDocType(MDocType.DOCBASETYPE_Payroll));
			pro.set_CustomColumn("DateDoc",periodo.getEndDate());
			//se setea cabecera
			pro.set_CustomColumn("HR_ProcessHeader_ID", head.get_ID());
			pro.saveEx(get_TrxName());
			
			//ininoles variables para calculo de haberes porcentuales
			BigDecimal[] factoresVolt = {Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO};
			BigDecimal[] factores = {Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO,Env.ZERO};
			Calendar fecha = Calendar.getInstance();
		    fecha.setTime(pro.getDateAcct());
		    int qtyDaysM = fecha.getActualMaximum(Calendar.DAY_OF_MONTH);
		    BigDecimal amtAuxEx = Env.ZERO;
		    //int qtyDaysM = 30;
			//ininoles end
			//borrado de regitros anteriores 
		    // ya no se borrara registros anteriores. Se podra reliquidar varias veces.
			/*DB.executeUpdate("DELETE FROM HR_ProcessBPDetail WHERE HR_ProcessBPDetail_ID IN (SELECT HR_ProcessBPDetail_ID" +
					" FROM HR_ProcessBPDetail d INNER JOIN HR_ProcessBP p ON (d.HR_ProcessBP_ID = p.HR_ProcessBP_ID)" +
					" WHERE p.IsReassessment = 'Y' AND HR_Process_ID="+pro.get_ID()+")",get_TrxName());
			
			DB.executeUpdate("DELETE FROM HR_ProcessBP WHERE IsReassessment = 'Y' AND HR_Process_ID="+pro.get_ID(),get_TrxName());
			*/
			//ciclo principal de socios de negocio
			String sqlBP = "SELECT bp.C_Bpartner_ID, bp.AD_Org_ID FROM C_Bpartner bp " +
					" INNER JOIN HR_Employee e ON (bp.C_Bpartner_ID = e.C_Bpartner_ID)" +
					" WHERE bp.IsActive = 'Y' ";
			if(p_AD_Org_ID > 0)
				sqlBP+= " AND bp.AD_Org_ID = "+p_AD_Org_ID;
			if(p_C_BPartner_ID > 0)
				sqlBP+= " AND bp.C_Bpartner_ID = "+p_C_BPartner_ID;			
			if(p_suplencia != null && p_suplencia.trim().length() > 0)
				sqlBP+= " AND e.suplencia LIKE '"+p_suplencia+"'";
			//validacion de parametro org padre
			if(p_Parent_Org_ID > 0)
				sqlBP+= " AND bp.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_OrgInfo WHERE Parent_Org_ID = "+p_Parent_Org_ID+") ";
			//validacion que no se repita sueldo del mismo mes
			/*sqlBP+= " AND e.C_Bpartner_ID NOT IN (SELECT pbp2.C_Bpartner_ID FROM HR_ProcessBP pbp2" +
					" INNER JOIN HR_Process pr2 ON (pr2.HR_Process_ID = pbp2.HR_Process_ID)" +
					" WHERE C_Period_ID = "+pro.get_ValueAsInt("C_Period_ID")+
					" AND pr2.HR_Process_ID <> "+pro.get_ID()+" AND pr2.IsActive='Y')";*/
			//validacion solo se traen funcionarios con change o incidentes (menos dias no trabajados) desde 2 meses atras
			sqlBP+= " AND (e.C_Bpartner_ID IN (SELECT ech.C_Bpartner_ID FROM HR_EmployeeChange ech" +
					" WHERE Processed = 'Y' AND IsUsed = 'N' AND DateTrx Between ? AND ?) OR " +
					" e.C_Bpartner_ID IN(SELECT mo.C_Bpartner_ID FROM HR_Movement mo WHERE mo.IsActive='Y' " +
					" AND IsUsed = 'N' AND HR_Concept_ID <> 2000054 AND ValidFrom Between ? AND ? ))";
			
			
			String mes ="-"+Integer.toString(pro.getDateAcct().getMonth()+1)+"-";
			
			
			PreparedStatement pstmt = DB.prepareStatement(sqlBP, get_TrxName());
			pstmt.setTimestamp(1, startYear);
			//pstmt.setTimestamp(2, periodo.getEndDate());
			pstmt.setTimestamp(2, endYear);
			pstmt.setTimestamp(3, periodo.getStartDate());
			pstmt.setTimestamp(4, periodo.getEndDate());
			ResultSet rs = pstmt.executeQuery();			
			while(rs.next())
			{
				//ininoles 09-06-2020
				//se guardan ids de socios de negocio para actualizar change a posterior
				ID_BpartnerUsed = ID_BpartnerUsed+","+rs.getInt("C_Bpartner_ID");			
				//se genera registro de socio de negocio en proceso
				X_HR_ProcessBP pBP = new X_HR_ProcessBP(getCtx(), 0, get_TrxName());
				pBP.setAD_Org_ID(rs.getInt("AD_Org_ID"));
				pBP.set_CustomColumn("IsReassessment", true);
				pBP.setHR_Process_ID(pro.get_ID());
				pBP.setC_BPartner_ID(rs.getInt("C_Bpartner_ID"));
				//ininoles se obtiene informacion d empleado para comparaciones futuras
				//int ID_Job = DB.getSQLValue(get_TrxName(), "SELECT HR_Job_ID FROM HR_Employee where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID"));
				MHREmployee employee = MHREmployee.getActiveEmployee(getCtx(),OFBForward.getBP(rs.getInt("C_Bpartner_ID")), get_TrxName());
				//se busca cantidad de dias trabajados
				BigDecimal cantDays = DB.getSQLValueBD(get_TrxName(), "SELECT COUNT(DISTINCT(EXTRACT(day FROM DateTrx))) FROM HR_AttendanceLine" +
						" WHERE C_BPartner_ID="+rs.getInt("C_Bpartner_ID")+" AND EXTRACT(month FROM DateTrx) = "+(pro.getDateAcct().getMonth()+1));
				if(cantDays != null)
					pBP.set_CustomColumn("WorkedDays", cantDays);
				pBP.saveEx();
				
				//ininoles antes de calcular tasa se verifica si es INP para restar un mes
				if(rs.getInt("C_Bpartner_ID") == 2007540)
					log.config("log bp en analisis");
				//Timestamp dateTemp = pro.getDateAcct();
				//Timestamp dateTope = dateTemp;
				Timestamp dateTope = pro.getDateAcct();
				String isINP = DB.getSQLValueString(get_TrxName(), "SELECT MAX(IsINP) FROM HR_Employee " +
						" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(rs.getInt("C_Bpartner_ID") == 2007593)
					log.config("log bp en analisis");
				if(isINP != null && isINP.compareTo("Y") == 0)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(pro.getDateAcct().getTime());
					cal.add(Calendar.MONTH,-1);
					dateTope = new Timestamp(cal.getTimeInMillis());	
				}
				//ciclo 1.B conceptos de bienestar haberes
				//String sqlBienestar = "SELECT * FROM HR_Welfare WHERE C_Bpartner_ID = ? AND Amount > 0 ";			
				String sqlBienestar = "SELECT * FROM HR_Welfare WHERE C_Bpartner_ID = ? AND Amount > 0 AND IsTaxable = 'N' " +
						" AND EndDate >= ? AND StartDate <= ?";
				PreparedStatement pstmtBienestar = DB.prepareStatement(sqlBienestar, get_TrxName());
				pstmtBienestar.setInt(1, OFBForward.getBP(rs.getInt("C_Bpartner_ID")));
				pstmtBienestar.setTimestamp(2, periodo.getEndDate());
				pstmtBienestar.setTimestamp(3, periodo.getEndDate());
				ResultSet rsBienestar = pstmtBienestar.executeQuery();			
				while(rsBienestar.next())
				{
					//se valida si corresponde por mes
					String mesWel = "";
					if(rsBienestar.getString("months") != null && rsBienestar.getString("months").trim().length() > 0)
						mesWel = "-"+rsBienestar.getString("months")+"-";
					else
						mesWel = mes;
					if(mesWel.contains(mes))//corresponde por mes
					{
						//porcentaje
						if(rsBienestar.getString("Type") != null && rsBienestar.getString("Type").compareTo("P") == 0)
						{
							if(rsBienestar.getInt("HR_ConceptRef_ID") > 0)
							{							
								BigDecimal amtCon = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(Amount) FROM HR_ProcessBPDetail" +
										" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND  HR_Concept_ID = "+rsBienestar.getInt("HR_ConceptRef_ID"));
								if(amtCon == null)
									amtCon = Env.ZERO;
								BigDecimal amount = rsBienestar.getBigDecimal("Amount");
								amount = amount.multiply(amtCon).divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
								//se crea registro de detalle BP
								X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
								bpDet.setHR_ProcessBP_ID(pBP.get_ID());
								bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
								if(rsBienestar.getInt("HR_Concept_ID") > 0)
									bpDet.setHR_Concept_ID(rsBienestar.getInt("HR_Concept_ID"));
								if(amount != null)
									bpDet.setAmount(amount);
								bpDet.saveEx();
							}
						}
						//monto fijo
						if(rsBienestar.getString("Type") != null && rsBienestar.getString("Type").compareTo("M") == 0) 
						{
							//se crea registro de detalle BP
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							if(rsBienestar.getInt("HR_Concept_ID") > 0)
								bpDet.setHR_Concept_ID(rsBienestar.getInt("HR_Concept_ID"));
							if(rsBienestar.getBigDecimal("Amount") != null)
								bpDet.setAmount(rsBienestar.getBigDecimal("Amount"));
							bpDet.saveEx();
						}				
						if(rsBienestar.getString("Type") != null && rsBienestar.getString("Type").compareTo("U") == 0) 
						{
							//se crea registro de detalle BP
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							if(rsBienestar.getInt("HR_Concept_ID") > 0)
								bpDet.setHR_Concept_ID(rsBienestar.getInt("HR_Concept_ID"));
							if(rsBienestar.getBigDecimal("Amount") != null)
							{
								BigDecimal tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
								tasa = rsBienestar.getBigDecimal("Amount").multiply(tasa);
								bpDet.setAmount(tasa.setScale(0, RoundingMode.HALF_EVEN));								
							}
							bpDet.saveEx();
						}
					}
				}	
				//ciclo que calcula los factores de cada cambio
				String sqlFact = "SELECT HR_EmployeeChange_ID, DateTrx FROM HR_EmployeeChange " +
				" WHERE IsACtive = 'Y' AND Processed = 'Y' AND C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+
				" AND DateTrx BETWEEN ? AND ? Order By DateTrx desc";
				PreparedStatement pstmtFact = DB.prepareStatement(sqlFact, get_TrxName());
				pstmtFact.setTimestamp(1, periodo.getStartDate());
				pstmtFact.setTimestamp(2, periodo.getEndDate());
				ResultSet rsFact = pstmtFact.executeQuery();	
				int indFact =0; 
				int difDays=0;
				int lastFac = 0;
				int cantF = 0;
				int finMes = 30;
				while(rsFact.next())
				{
					Timestamp dateTrxF = rsFact.getTimestamp("DateTrx");
					difDays = qtyDaysM-(dateTrxF.getDate()-1);
					difDays = difDays-lastFac;
					if(difDays == qtyDaysM)
						finMes = difDays;
					factoresVolt[indFact] = new BigDecimal(difDays).divide(new BigDecimal(finMes), 6, RoundingMode.HALF_EVEN);
					//factores[indFact] = factores[indFact].subtract(lastFac);
					lastFac = lastFac+difDays;
					cantF++;
					//antes de voltear se calcula ultimo valor(primero al dar vuelta)					
				}	
				factoresVolt[cantF] = new BigDecimal(finMes-lastFac).divide(new BigDecimal(finMes), 6, RoundingMode.HALF_EVEN);
				             
				//se voltea arreglo de factores
				for (int a=0; a<=cantF;a++)
				{
					factores[a]=factoresVolt[cantF-a];
				}				
				//ciclo 2 que genera el detalle del sueldo
				//String sqlConcept = "SELECT * FROM HR_PayrollConcept WHERE IsActive = 'Y' AND HR_Payroll_ID = ? ORDER BY SeqNo ";
				String sqlConcept = " SELECT pc.HR_Concept_ID FROM HR_PayrollConcept pc" +
						" INNER JOIN HR_Concept c ON (pc.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE pc.IsActive = 'Y' AND c.IsActive = 'Y' AND pc.HR_Payroll_ID = ?"+
						" AND (c.C_Year_ID IS NUll OR c.C_Year_ID="+periodo.getC_Year_ID()+") ORDER BY pc.SeqNo";
				PreparedStatement pstmtConcept = DB.prepareStatement(sqlConcept, get_TrxName());
				pstmtConcept.setInt(1, pro.getHR_Payroll_ID());
				ResultSet rsConcept = pstmtConcept.executeQuery();			
				while(rsConcept.next())
				{
					//X_HR_PayrollConcept pConc = new X_HR_PayrollConcept(getCtx(), rsConcept.getInt("HR_PayrollConcept_ID"), get_TrxName());
					X_HR_Concept conc = new X_HR_Concept(getCtx(), rsConcept.getInt("HR_Concept_ID"), get_TrxName());
					//se revisa tipo para saber de donde sacar monto
					if(conc.get_ID()== 2000102 && rs.getInt("C_Bpartner_ID") == 2010197)
						log.config("stop para debug: "+conc.get_ID());
					
					//Variables para calculo porcentual
					BigDecimal factorP2 = Env.ONE; 
					BigDecimal factorP1 = Env.ZERO;
					BigDecimal amtP1 = Env.ZERO;
					//si tipo no es dividible lo hace de forma normal
					if(!conc.get_ValueAsBoolean("IsDivisibleAmount")) 
					{
						factorP2 = Env.ONE;
					}
					else
					{
						//se busca si existe registro de cambio 
						String sqlCh = "SELECT HR_EmployeeChange_ID FROM HR_EmployeeChange " +
								" WHERE IsACtive = 'Y' AND Processed = 'Y' AND C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+
								" AND DateTrx BETWEEN ? AND ? ORDER BY DateTrx asc";
						PreparedStatement pstmtCh = DB.prepareStatement(sqlCh, get_TrxName());
						pstmtCh.setTimestamp(1, periodo.getStartDate());
						pstmtCh.setTimestamp(2, periodo.getEndDate());
						ResultSet rsCh = pstmtCh.executeQuery();	
						//se reutiliza variable indice factores
						indFact = 0;
						while(rsCh.next())
						{
							X_HR_EmployeeChange chan = new X_HR_EmployeeChange(getCtx(), rsCh.getInt("HR_EmployeeChange_ID"), get_TrxName());
							factorP1= factores[indFact];
							//se calcula sueldo normal
							amtP1 = Env.ZERO;
							//haber tipo tabla
							if(conc.get_ValueAsString("TypeConcept").compareTo("T") == 0)
							{
								//se trae valor con select
								String sqlValT = "SELECT COALESCE(MAX(MinValue),0) FROM HR_Attribute WHERE HR_Concept_ID = "+conc.get_ID()+" AND ? BETWEEN dateStart AND dateEnd ";
								if(conc.get_ValueAsString("HR_Reference1") != null && conc.get_ValueAsString("HR_Reference1").trim().length() > 0)
								{
									if(conc.get_ValueAsString("HR_Reference1").compareTo("grade") == 0)
									{
										if(chan.getgradeOld() != null && chan.getgrade() != null && chan.getgrade().trim().length() > 0)
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference1")+" = '"+chan.getgradeOld().trim()+"'";
										}
										else
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference1")+" = (SELECT "+conc.get_ValueAsString("HR_Reference1")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
										}
									}
									else if(conc.get_ValueAsString("HR_Reference1").compareTo("benio") == 0)
									{
										if(chan.get_ValueAsString("benioOld") != null && chan.get_ValueAsString("benio")!= null && chan.get_ValueAsString("benio").trim().length() > 0)
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference1")+" = '"+chan.get_ValueAsString("benioOld").trim()+"'";
										}
										else
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference1")+" = (SELECT "+conc.get_ValueAsString("HR_Reference1")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
										}
									}
									else
									{
										sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference1")+" = (SELECT "+conc.get_ValueAsString("HR_Reference1")+" FROM HR_Employee " +
										" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
									}
								}
								if(conc.get_ValueAsString("HR_Reference2") != null && conc.get_ValueAsString("HR_Reference2").trim().length() > 0)
								{
									if(conc.get_ValueAsString("HR_Reference2").compareTo("grade") == 0)
									{
										if(chan.getgradeOld() != null && chan.getgrade().trim().length() > 0)
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference2")+" = '"+chan.getgradeOld().trim()+"' ";
										}
										else
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference2")+" = (SELECT "+conc.get_ValueAsString("HR_Reference2")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
										}
									}
									else if(conc.get_ValueAsString("HR_Reference2").compareTo("benio") == 0)
									{
										if(chan.get_ValueAsString("benioOld") != null && chan.get_ValueAsString("benio")!= null && chan.get_ValueAsString("benio").trim().length() > 0)
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference2")+" = '"+chan.get_ValueAsString("benioOld").trim()+"'";
										}
										else
										{
											sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference2")+" = (SELECT "+conc.get_ValueAsString("HR_Reference2")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
										}
									}
									else
									{
										sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference2")+" = (SELECT "+conc.get_ValueAsString("HR_Reference2")+" FROM HR_Employee " +
										" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
									}
								}
								if(conc.get_ValueAsString("HR_Reference3") != null && conc.get_ValueAsString("HR_Reference3").trim().length() > 0)
								{
									sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference3")+" = (SELECT "+conc.get_ValueAsString("HR_Reference3")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
								}
								if(conc.get_ValueAsString("HR_Reference4") != null && conc.get_ValueAsString("HR_Reference4").trim().length() > 0)
								{
									sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference4")+" = (SELECT "+conc.get_ValueAsString("HR_Reference4")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
								}
								if(conc.get_ValueAsString("HR_Reference5") != null && conc.get_ValueAsString("HR_Reference5").trim().length() > 0)
								{
									sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference5")+" = (SELECT "+conc.get_ValueAsString("HR_Reference5")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
								}
								if(conc.get_ValueAsString("HR_Reference6") != null && conc.get_ValueAsString("HR_Reference6").trim().length() > 0)
								{
									sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference6")+" = (SELECT "+conc.get_ValueAsString("HR_Reference6")+" FROM HR_Employee " +
											" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
								}
								//if(conc.getHR_Job_ID() > 0)
								if(conc.get_ValueAsBoolean("UseJob"))
								{
									if(chan.getHR_JobOld_ID() > 0)
									{
										//ininoles se agreg excepci�n para conceptos 2000013 y 2000014 si se cumple condicion
										//se usara estamento profesional
										if((conc.get_ID() == 2000013 || conc.get_ID() == 2000014) &&
											(employee.get_ValueAsBoolean("IsProfessional") || employee.get_ValueAsBoolean("IsProfessional2") || employee.get_ValueAsBoolean("IsProfessional3")) && 
											(chan.getHR_JobOld_ID() == 2000002))
											sqlValT = sqlValT + " AND HR_Job_ID = 2000000";
										else
											sqlValT = sqlValT + " AND HR_Job_ID = "+chan.getHR_JobOld_ID();
									}
									else
									{
										//ininoles se agreg excepci�n para conceptos 2000013 y 2000014 si se cumple condicion
										//se usara estamento profesional
										if((conc.get_ID() == 2000013 || conc.get_ID() == 2000014) &&
												(employee.get_ValueAsBoolean("IsProfessional") || employee.get_ValueAsBoolean("IsProfessional2") || employee.get_ValueAsBoolean("IsProfessional3")) && 
												(employee.getHR_Job_ID() == 2000002))
												sqlValT = sqlValT + " AND HR_Job_ID = 2000000";
										else
										{
											sqlValT = sqlValT + " AND HR_Job_ID = (SELECT HR_Job_ID FROM HR_Employee " +
													" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
										}
									}
								}
								if(conc.get_ValueAsBoolean("UseRegion"))
								{
									if(chan.getC_RegionOld_ID() > 0)
									{
										sqlValT = sqlValT + " AND C_Region_ID = "+chan.getC_RegionOld_ID();
									}
									else{
										sqlValT = sqlValT + " AND C_Region_ID = (SELECT C_Region_ID FROM HR_Employee " +
										" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
									}
								}
								if(conc.get_ValueAsBoolean("UseOrg"))
								{
									if(chan.get_ValueAsInt("AD_OrgOld_ID") > 0)
									{
										sqlValT = sqlValT + " AND AD_Org_ID = "+chan.get_ValueAsInt("AD_OrgOld_ID");
									}
									else{
										sqlValT = sqlValT + " AND AD_Org_ID = (SELECT AD_Org_ID FROM HR_Employee " +
										" where AD_Org_ID = "+rs.getInt("AD_Org_ID")+")";
									}
								}
								amtP1 = DB.getSQLValueBD(get_TrxName(), sqlValT,pro.getDateAcct());																
								if(amtP1 == null)
									amtP1 = Env.ZERO;
								else
								{
									amtP1 = amtP1.multiply(factorP1);
									amtP1 = amtP1.setScale(3, RoundingMode.HALF_EVEN);
								}
								X_HR_ProcessBPDetail bpDetF = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
								bpDetF.setHR_ProcessBP_ID(pBP.get_ID());
								bpDetF.setAD_Org_ID(pBP.getAD_Org_ID());
								bpDetF.setHR_Concept_ID(conc.get_ID());
								bpDetF.setAmount(amtP1);
								bpDetF.saveEx();
							}
							indFact++;
							//factor2 va acumulando el saldo del factor
							factorP2 = factorP2.subtract(factorP1);
						}
					}
					//tipo tabla					
					BigDecimal amtCon = Env.ZERO;
					if(conc.get_ValueAsString("TypeConcept").compareTo("T") == 0)
					{
						//se trae valor con select
						String sqlValT = "SELECT COALESCE(MAX(MinValue),0) FROM HR_Attribute WHERE HR_Concept_ID = "+conc.get_ID()+" AND ? BETWEEN dateStart AND dateEnd ";
						if(conc.get_ValueAsString("HR_Reference1") != null && conc.get_ValueAsString("HR_Reference1").trim().length() > 0)
						{
							sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference1")+" = (SELECT "+conc.get_ValueAsString("HR_Reference1")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference2") != null && conc.get_ValueAsString("HR_Reference2").trim().length() > 0)
						{
							sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference2")+" = (SELECT "+conc.get_ValueAsString("HR_Reference2")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference3") != null && conc.get_ValueAsString("HR_Reference3").trim().length() > 0)
						{
							sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference3")+" = (SELECT "+conc.get_ValueAsString("HR_Reference3")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference4") != null && conc.get_ValueAsString("HR_Reference4").trim().length() > 0)
						{
							sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference4")+" = (SELECT "+conc.get_ValueAsString("HR_Reference4")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference5") != null && conc.get_ValueAsString("HR_Reference5").trim().length() > 0)
						{
							sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference5")+" = (SELECT "+conc.get_ValueAsString("HR_Reference5")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference6") != null && conc.get_ValueAsString("HR_Reference6").trim().length() > 0)
						{
							sqlValT = sqlValT + " AND "+conc.get_ValueAsString("HR_Reference6")+" = (SELECT "+conc.get_ValueAsString("HR_Reference6")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						//if(conc.getHR_Job_ID() > 0)
						if(conc.get_ValueAsBoolean("UseJob"))
						{
							if((conc.get_ID() == 2000013 || conc.get_ID() == 2000014) &&
									(employee.get_ValueAsBoolean("IsProfessional") || employee.get_ValueAsBoolean("IsProfessional2") || employee.get_ValueAsBoolean("IsProfessional3")) && 
									(employee.getHR_Job_ID() == 2000002))
									sqlValT = sqlValT + " AND HR_Job_ID = 2000000";
							else
							{
								sqlValT = sqlValT + " AND HR_Job_ID = (SELECT HR_Job_ID FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
							}
						}
						if(conc.get_ValueAsBoolean("UseRegion"))
						{
							sqlValT = sqlValT + " AND C_Region_ID = (SELECT C_Region_ID FROM HR_Employee " +
							" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}	
						if(conc.get_ValueAsBoolean("UseOrg"))
						{
							sqlValT = sqlValT + " AND AD_Org_ID = (SELECT AD_Org_ID FROM HR_Employee " +
							" where AD_Org_ID = "+rs.getInt("AD_Org_ID")+")";
						}
						//ininoles exepcion se reemplaza grado por grado de planta cuando es suplencia interna
						if((conc.get_ID() == 2000103 || conc.get_ID() == 2000012) &&
								employee.get_ValueAsString("Suplencia").compareTo("06")==0)
						{
							sqlValT = sqlValT.replace("grade", "gradeRef");
						}//ininoles end
						
						amtCon = DB.getSQLValueBD(get_TrxName(), sqlValT,pro.getDateAcct());
						//calculos para valor porcionado
						if(amtCon == null)
							amtCon = Env.ZERO;
						else
						{
							amtCon = amtCon.multiply(factorP2);
							amtCon = amtCon.setScale(3, RoundingMode.HALF_EVEN);
						}
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						bpDet.setHR_Concept_ID(conc.get_ID());
						bpDet.setAmount(amtCon);
						bpDet.saveEx();
					}	
					//Monto fijo
					else if(conc.get_ValueAsString("TypeConcept").compareTo("F") == 0)
					{
						if((BigDecimal)conc.get_Value("Amount") != null)
							amtCon = (BigDecimal)conc.get_Value("Amount");
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						bpDet.setHR_Concept_ID(conc.get_ID());
						bpDet.setAmount(amtCon);
						bpDet.saveEx();
					}
					// tipo porcentual
					else if(conc.get_ValueAsString("TypeConcept").compareTo("P") == 0)
					{
						//ciclo 3 que lee cada configuracion y calcula monto
						String sqlAttributet = "SELECT HR_ConceptRef_ID, MinValue FROM HR_Attribute WHERE HR_Concept_ID = "+conc.get_ID()+" AND ? BETWEEN dateStart AND dateEnd ";
						if(conc.get_ValueAsString("HR_Reference1") != null && conc.get_ValueAsString("HR_Reference1").trim().length() > 0)
						{
							sqlAttributet = sqlAttributet + " AND "+conc.get_ValueAsString("HR_Reference1")+" = (SELECT "+conc.get_ValueAsString("HR_Reference1")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference2") != null && conc.get_ValueAsString("HR_Reference2").trim().length() > 0)
						{
							sqlAttributet = sqlAttributet + " AND "+conc.get_ValueAsString("HR_Reference2")+" = (SELECT "+conc.get_ValueAsString("HR_Reference2")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference3") != null && conc.get_ValueAsString("HR_Reference3").trim().length() > 0)
						{
							sqlAttributet = sqlAttributet + " AND "+conc.get_ValueAsString("HR_Reference3")+" = (SELECT "+conc.get_ValueAsString("HR_Reference3")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference4") != null && conc.get_ValueAsString("HR_Reference4").trim().length() > 0)
						{
							sqlAttributet = sqlAttributet + " AND "+conc.get_ValueAsString("HR_Reference4")+" = (SELECT "+conc.get_ValueAsString("HR_Reference4")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference5") != null && conc.get_ValueAsString("HR_Reference5").trim().length() > 0)
						{
							sqlAttributet = sqlAttributet + " AND "+conc.get_ValueAsString("HR_Reference5")+" = (SELECT "+conc.get_ValueAsString("HR_Reference5")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsString("HR_Reference6") != null && conc.get_ValueAsString("HR_Reference6").trim().length() > 0)
						{
							sqlAttributet = sqlAttributet + " AND "+conc.get_ValueAsString("HR_Reference6")+" = (SELECT "+conc.get_ValueAsString("HR_Reference6")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						//if(conc.getHR_Job_ID() > 0)
						if(conc.get_ValueAsBoolean("UseJob"))
						{
							//ininoles se agreg excepci�n para conceptos 2000013 y 2000014 si se cumple condicion
							//se usara estamento profesional
							if((conc.get_ID() == 2000013 || conc.get_ID() == 2000013) &&
									(employee.get_ValueAsBoolean("IsProfessional") || employee.get_ValueAsBoolean("IsProfessional2") || employee.get_ValueAsBoolean("IsProfessional3")) && 
									(employee.getHR_Job_ID() == 2000002))
								sqlAttributet = sqlAttributet + " AND HR_Job_ID = 2000000";
							else
							{
								sqlAttributet = sqlAttributet + " AND HR_Job_ID = (SELECT HR_Job_ID FROM HR_Employee " +
								" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
							}
						}
						//if(conc.get_ValueAsInt("C_Region_ID") > 0)
						if(conc.get_ValueAsBoolean("UseRegion"))
						{
							sqlAttributet = sqlAttributet + " AND C_Region_ID = (SELECT C_Region_ID FROM HR_Employee " +
							" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						if(conc.get_ValueAsBoolean("UseOrg"))
						{
							sqlAttributet = sqlAttributet + " AND AD_Org_ID = (SELECT AD_Org_ID FROM HR_Employee " +
							" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID")+")";
						}
						
						PreparedStatement pstmtAtt = DB.prepareStatement(sqlAttributet, get_TrxName());
						pstmtAtt.setTimestamp(1, pro.getDateAcct());
						ResultSet rsAtt = pstmtAtt.executeQuery();						
						int indDetEx= 1;
						while(rsAtt.next())
						{
							//se busca valor para aplicar porcentaje
							BigDecimal amtAtt = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(Amount) FROM HR_ProcessBPDetail" +
								" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND  HR_Concept_ID = "+rsAtt.getInt("HR_ConceptRef_ID"));
							if(amtAtt == null)
								amtAtt = Env.ZERO;		
							//ininoles exepcion se reemplaza grado por grado de planta cuando es suplencia interna y se calcula nuevo monto
							if((conc.get_ID() == 2000103 || conc.get_ID() == 2000012) &&
									employee.get_ValueAsString("Suplencia").compareTo("06")==0)
							{				
								if(indDetEx == 1)//sueldo base
								{
									amtAtt = DB.getSQLValueBD(get_TrxName(), "SELECT MinValue FROM HR_Attribute WHERE HR_Concept_ID = "+rsAtt.getInt("HR_ConceptRef_ID")+conc.get_ID()+" AND ? BETWEEN dateStart AND dateEnd "+
											 " AND IsActive = 'Y' AND grade = '"+employee.get_ValueAsString("gradeRef")+"'",pro.getDateAcct());
									amtAuxEx = amtAtt;
								}
								if(indDetEx == 2)//asignaci�n de antiguedad
								{
									amtAtt = DB.getSQLValueBD(get_TrxName(), "SELECT MinValue FROM HR_Attribute WHERE HR_Concept_ID = "+rsAtt.getInt("HR_ConceptRef_ID")+conc.get_ID()+" AND ? BETWEEN dateStart AND dateEnd "+
											 " AND IsActive = 'Y' AND benio = '"+employee.get_ValueAsString("benio")+"'",pro.getDateAcct());
									if(amtAtt == null)
										amtAtt = Env.ZERO;
									if(amtAuxEx == null)
										amtAuxEx = Env.ZERO;
									amtAtt = amtAtt.multiply(amtAuxEx);
									amtAtt = amtAtt.divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
								}
								indDetEx++;
							}//ininoles end
							if(amtAtt == null)
								amtAtt= Env.ZERO;
							
							amtCon = amtAtt.multiply(rsAtt.getBigDecimal("MinValue")).divide(Env.ONEHUNDRED, RoundingMode.HALF_EVEN);
							amtCon = amtCon.setScale(0, RoundingMode.HALF_EVEN);
							//se crea registro de detalle BP
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							bpDet.setHR_Concept_ID(conc.get_ID());
							//ininoles se agrega excepcion para concepto 2000012
							if(conc.get_ID() == 2000012 || conc.get_ID() == 2000103)
							{
								if(employee.get_ValueAsString("Suplencia").compareTo("07")==0)
									bpDet.setAmount(Env.ZERO);								
								else
									bpDet.setAmount(amtCon);
							}
							else
								bpDet.setAmount(amtCon);
							bpDet.saveEx();
						}
					}
					//ininoles descuentos se calcularan luego de los haberes y de recalcular totales
					/*else if(conc.get_ValueAsString("TypeConcept").compareTo("D1") == 0)
					{
						//se busca monto tope o sumatoria
						BigDecimal pTope = DB.getSQLValueBD(get_TrxName(), "SELECT MaxAmount FROM HR_Employee c WHERE c.C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
						if(pTope == null)
							pTope =Env.ONE;
						BigDecimal tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
						if(tasa == null)
							tasa = Env.ONE;
						BigDecimal tope = pTope.multiply(tasa);
						BigDecimal sumH = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
								" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
								" WHERE d.Amount > 0 AND c.IsTaxable = 'Y' AND d.HR_ProcessBP_ID = "+pBP.get_ID());
						if(sumH == null)
							sumH = Env.ZERO;
						if(sumH.compareTo(tope) > 0 && tope.compareTo(Env.ZERO) > 0)
							sumH = tope;
						//logica para buscar nombres de campos y calcular descuentos
						BigDecimal amtTotal = Env.ZERO;
						BigDecimal porc = Env.ZERO;
						if(conc.get_ValueAsString("HR_Discount1") != null && conc.get_ValueAsString("HR_Discount1").trim().length() > 0)
						{
							porc = DB.getSQLValueBD(get_TrxName(), "SELECT "+conc.get_ValueAsString("HR_Discount1")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID"));
							if(porc == null)
								porc = Env.ZERO;
						}
						amtTotal = porc.multiply(sumH);						
						amtTotal = amtTotal.divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
						//se niega monto total
						amtTotal = amtTotal.negate();
						//antes de crear registro se verifica topey se genera linea extra de ser necesario
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						bpDet.setHR_Concept_ID(conc.get_ID());
						bpDet.setAmount(amtTotal);
						bpDet.saveEx();
					}*/
					//bonos
					else if(conc.get_ValueAsString("TypeConcept").compareTo("B1") == 0)
					{
						String mesBono = conc.get_ValueAsString("months");
						mesBono = "-"+mesBono+"-";
						//se revisa periociad para saber si corresponde bono
						if(mesBono.contains(mes))//corresponde bono
						{
							//logica para buscar nombres de campos y calcular descuentos
							//BigDecimal amtTotal = Env.ZERO;
							BigDecimal porc = Env.ZERO;
							if(conc.get_ValueAsString("HR_Bono1") != null && conc.get_ValueAsString("HR_Bono1").trim().length() > 0)
							{
								porc = DB.getSQLValueBD(get_TrxName(), "SELECT "+conc.get_ValueAsString("HR_Bono1")+" FROM HR_Employee " +
										" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID"));
							}
							if(porc == null)
								porc = Env.ZERO;
							if(porc.compareTo(Env.ZERO) != 0)
							{
								//ininoles se usara nuevo metodo de calculo de bonos en base a total imponible y 3 meses atras
								//ciclo de atrbutos de bonos								
								/*
								String sqlAttributetB = "SELECT HR_ConceptRef_ID FROM HR_Attribute WHERE HR_Concept_ID = "+conc.get_ID();
								PreparedStatement pstmtAttB = DB.prepareStatement(sqlAttributetB, get_TrxName());
								ResultSet rsAttB = pstmtAttB.executeQuery();			
								while(rsAttB.next())
								{
									//se busca valor para aplicar porcentaje
									BigDecimal amtAtt = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(Amount) FROM HR_ProcessBPDetail" +
										" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND HR_Concept_ID = "+rsAttB.getInt("HR_ConceptRef_ID"));
									if(amtAtt == null)
										amtAtt = Env.ZERO;
									amtAtt = amtAtt.multiply(porc).divide(Env.ONEHUNDRED, RoundingMode.HALF_EVEN);
									amtAtt = amtAtt.setScale(0, RoundingMode.HALF_EVEN);
									
									//se crea registro de detalle BP
									X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
									bpDet.setHR_ProcessBP_ID(pBP.get_ID());
									bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
									bpDet.setHR_Concept_ID(conc.get_ID());
									bpDet.setAmount(amtAtt);
									bpDet.saveEx();
								}*/
								BigDecimal amtBono = Env.ZERO;
								for(int a=0; a<3; a++)
								{
									//se busca total imponible para calcular monto de ese mes
									BigDecimal amtLast = DB.getSQLValueBD(get_TrxName(), "SELECT TotalAmt FROM HR_ProcessBP bp" +
											" INNER JOIN HR_Process pr ON (bp.HR_Process_ID = pr.HR_Process_ID) " +
											" WHERE bp.C_BPartner_ID="+rs.getInt("C_BPartner_ID")+" AND EXTRACT(month FROM pr.dateAcct)="+(pro.getDateAcct().getMonth()-a)+
											" AND pr.DocStatus IN ('CO') AND bp.IsActive = 'Y' AND bp.Processed = 'Y'");
									if(amtLast == null)
										amtLast = Env.ZERO;
									//se calcula porcentaje
									if(amtLast.compareTo(Env.ZERO) > 0)
									{
										amtLast = amtLast.multiply(porc).divide(Env.ONEHUNDRED, RoundingMode.HALF_EVEN);										
									}
									amtBono = amtBono.add(amtLast);
								}
								amtBono = amtBono.setScale(0, RoundingMode.HALF_EVEN);
								
								//se crea registro de detalle BP
								X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
								bpDet.setHR_ProcessBP_ID(pBP.get_ID());
								bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
								bpDet.setHR_Concept_ID(conc.get_ID());
								bpDet.setAmount(amtBono);
								bpDet.saveEx();
								
							}	
						}
					}
					/*else if(conc.get_ValueAsString("TypeConcept").compareTo("D2") == 0)
					{
						//se busca monto tope o sumatoria
						BigDecimal pTope = DB.getSQLValueBD(get_TrxName(), "SELECT MaxAmount FROM HR_Job j" +
								" INNER JOIN HR_Employee c ON (j.HR_Job_ID = c.HR_Job_ID)" +
								" WHERE c.C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
						if(pTope == null)
							pTope =Env.ONE;
						BigDecimal tasa = MConversionRate.getRate(2000000,228,pro.getDateAcct(),114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
						if(tasa == null)
							tasa = Env.ZERO;
						BigDecimal tope = pTope.multiply(tasa);
						BigDecimal sumH = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(Amount),0) FROM HR_ProcessBPDetail " +
								" WHERE Amount > 0 AND HR_ProcessBP_ID="+pBP.get_ID());
						if(sumH == null)
							sumH = Env.ZERO;
						if(sumH.compareTo(tope) > 0 && tope.compareTo(Env.ZERO) > 0)
							sumH = tope;
						//logica para buscar nombres de campos y calcular descuentos
						BigDecimal amtTotal = Env.ZERO;
						BigDecimal porc = Env.ZERO;
						if(conc.get_ValueAsString("HR_Discount2") != null && conc.get_ValueAsString("HR_Discount2").trim().length() > 0)
						{
							porc = DB.getSQLValueBD(get_TrxName(), "SELECT "+conc.get_ValueAsString("HR_Discount2")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID"));
						}
						amtTotal = porc.multiply(sumH);						
						amtTotal = amtTotal.divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
						//se niega monto total
						amtTotal = amtTotal.negate();
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						bpDet.setHR_Concept_ID(conc.get_ID());
						bpDet.setAmount(amtTotal);
						bpDet.saveEx();
					}*/
					/*else if(conc.get_ValueAsString("TypeConcept").compareTo("D3") == 0)
					{
						//se busca monto tope o sumatoria
						BigDecimal pTope = DB.getSQLValueBD(get_TrxName(), "SELECT MaxAmount FROM HR_Job j" +
								" INNER JOIN HR_Employee c ON (j.HR_Job_ID = c.HR_Job_ID)" +
								" WHERE c.C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
						if(pTope == null)
							pTope =Env.ONE;
						BigDecimal tasa = MConversionRate.getRate(2000000,228,pro.getDateAcct(),114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
						if(tasa == null)
							tasa = Env.ONE;
						BigDecimal tope = pTope.multiply(tasa);
						BigDecimal sumH = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(Amount),0) FROM HR_ProcessBPDetail " +
								" WHERE Amount > 0 AND HR_ProcessBP_ID="+pBP.get_ID());
						if(sumH == null)
							sumH = Env.ZERO;
						if(sumH.compareTo(tope) > 0)
							sumH = tope;
						//logica para buscar nombres de campos y calcular descuentos
						BigDecimal amtTotal = Env.ZERO;
						BigDecimal porc = Env.ZERO;
						if(conc.get_ValueAsString("HR_Discount3") != null && conc.get_ValueAsString("HR_Discount3").trim().length() > 0)
						{
							porc = DB.getSQLValueBD(get_TrxName(), "SELECT "+conc.get_ValueAsString("HR_Discount3")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID"));
						}
						amtTotal = porc.multiply(sumH);						
						amtTotal = amtTotal.divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
						//se niega monto total
						amtTotal = amtTotal.negate();
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						bpDet.setHR_Concept_ID(conc.get_ID());
						bpDet.setAmount(amtCon);
						bpDet.saveEx();
					}*/
				}
				//ciclo 3 conceptos de bienestar descuentos
				//String sqlBienestarD = "SELECT * FROM HR_Welfare WHERE C_Bpartner_ID = ? AND Amount <= 0 AND IsTaxable = 'N'";
				String sqlBienestarD = "SELECT * FROM HR_Welfare WHERE C_Bpartner_ID = ? AND Amount <= 0 AND IsTaxable = 'N'"+
					"AND EndDate >= ? AND StartDate <= ?";
				PreparedStatement pstmtBienestarD = DB.prepareStatement(sqlBienestarD, get_TrxName());
				pstmtBienestarD.setInt(1, rs.getInt("C_Bpartner_ID"));
				pstmtBienestarD.setTimestamp(2, periodo.getEndDate());
				pstmtBienestarD.setTimestamp(3, periodo.getEndDate());
				ResultSet rsBienestarD = pstmtBienestarD.executeQuery();			
				while(rsBienestarD.next())
				{
					//se valida si corresponde bono por mes
					String mesWel = "";
					if(rsBienestarD.getString("months") != null && rsBienestarD.getString("months").trim().length() > 0)
						mesWel = "-"+rsBienestarD.getString("months")+"-";
					else
						mesWel = mes;
					if(mesWel.contains(mes))//corresponde bono
					{
						//porcentaje
						if(rsBienestarD.getString("Type") != null && rsBienestarD.getString("Type").compareTo("P") == 0)
						{
							if(rsBienestarD.getInt("HR_ConceptRef_ID") > 0)
							{							
								BigDecimal amtCon = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(Amount) FROM HR_ProcessBPDetail" +
										" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND  HR_Concept_ID = "+rsBienestarD.getInt("HR_ConceptRef_ID"));
								if(amtCon == null)
									amtCon = Env.ZERO;
								BigDecimal amount = rsBienestarD.getBigDecimal("Amount");
								amount = amount.multiply(amtCon).divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
								//se crea registro de detalle BP
								X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
								bpDet.setHR_ProcessBP_ID(pBP.get_ID());
								bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
								if(rsBienestarD.getInt("HR_Concept_ID") > 0)
									bpDet.setHR_Concept_ID(rsBienestarD.getInt("HR_Concept_ID"));
								if(amount != null)
									bpDet.setAmount(amount);
								bpDet.saveEx();
							}
						}
						//monto fijo
						else if(rsBienestarD.getString("Type") != null && rsBienestarD.getString("Type").compareTo("M") == 0) 
						{
							//se crea registro de detalle BP
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							if(rsBienestarD.getInt("HR_Concept_ID") > 0)
								bpDet.setHR_Concept_ID(rsBienestarD.getInt("HR_Concept_ID"));
							if(rsBienestarD.getBigDecimal("Amount") != null)
								bpDet.setAmount(rsBienestarD.getBigDecimal("Amount"));
							bpDet.saveEx();
						}		
						if(rsBienestarD.getString("Type") != null && rsBienestarD.getString("Type").compareTo("U") == 0) 
						{
							//se crea registro de detalle BP
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							if(rsBienestarD.getInt("HR_Concept_ID") > 0)
								bpDet.setHR_Concept_ID(rsBienestarD.getInt("HR_Concept_ID"));
							if(rsBienestarD.getBigDecimal("Amount") != null)
							{
								BigDecimal tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
								tasa = rsBienestarD.getBigDecimal("Amount").multiply(tasa);
								bpDet.setAmount(tasa.setScale(0, RoundingMode.HALF_EVEN));
								
							}
							bpDet.saveEx();
						}
					}
				}
				//ciclo 4 conceptos de incidencias
				String sqlInc = "SELECT * FROM HR_Movement WHERE IsActive = 'Y' AND C_Bpartner_ID = ? AND EXTRACT(month FROM validfrom) = ? "
						+ " AND  HR_Concept_ID <> 2000054 ";	//que no traiga dias no trabajados		
				PreparedStatement pstmtInc = DB.prepareStatement(sqlInc, get_TrxName());
				pstmtInc.setInt(1, rs.getInt("C_Bpartner_ID"));
				pstmtInc.setInt(2, pro.getDateAcct().getMonth()+1);
				ResultSet rsInc = pstmtInc.executeQuery();			
				while(rsInc.next())
				{
					//porcentaje
					if(rsInc.getString("Type") != null && rsInc.getString("Type").compareTo("P") == 0)
					{
						if(rsInc.getInt("HR_ConceptRef_ID") > 0)
						{							
							BigDecimal amtCon = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(Amount) FROM HR_ProcessBPDetail " +
									" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND  HR_Concept_ID = "+rsInc.getInt("HR_ConceptRef_ID"));
							if(amtCon == null)
								amtCon = Env.ZERO;
							BigDecimal amount = rsInc.getBigDecimal("Amount");
							amount = amount.multiply(amtCon).divide(Env.ZERO, 0, RoundingMode.HALF_EVEN);
							//se crea registro de detalle BP
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							if(rsInc.getInt("HR_Concept_ID") > 0)
								bpDet.setHR_Concept_ID(rsInc.getInt("HR_Concept_ID"));
							if(amount != null)
								bpDet.setAmount(amount);
							bpDet.saveEx();
						}
					}
					//monto fijo
					else if(rsInc.getString("Type") != null && rsInc.getString("Type").compareTo("M") == 0) 
					{
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						if(rsInc.getInt("HR_Concept_ID") > 0)
							bpDet.setHR_Concept_ID(rsInc.getInt("HR_Concept_ID"));
						if(rsInc.getBigDecimal("Amount") != null)
							bpDet.setAmount(rsInc.getBigDecimal("Amount"));
						bpDet.saveEx();
					}
				}	
				//se agregan horas extra
				//se dejan horas extra de otros procesos en 0
				int ID_ConceptHE = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
						" WHERE upper(c.description) = 'HEXTRA'");
				int ID_ConceptHENO = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
						" WHERE upper(c.description) = 'HEXTRANOCHE'");
				/*DB.executeUpdate("UPDATE HR_ProcessBPDetail SET Amount = 0"
						+ " WHERE HR_Concept_ID = "+ID_ConceptHE+" "
						+ " AND HR_ProcessBP_ID IN ("
						+ " SELECT HR_ProcessBP_ID FROM HR_ProcessBP pbp"
						+ " INNER JOIN HR_Process p ON (p.HR_Process_ID = pbp.HR_Process_ID)"
						+ " WHERE pbp.IsActive='Y' AND C_Period_ID = "+periodo.get_ID()
						+ " AND pbp.C_Bpartner_ID="+pBP.getC_BPartner_ID()+" AND p.IsActive='Y')", get_TrxName());*/
				//se busca si funcionario tiene horas extra
				//horas extra diurnas
				//Se calcula valor hora extra para el mes
				BigDecimal vHExtra = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE c.IsHEAmt = 'Y' AND d.HR_ProcessBP_ID IN (SELECT HR_ProcessBP_ID FROM HR_ProcessBP pbp"
						+ " INNER JOIN HR_Process p ON (p.HR_Process_ID = pbp.HR_Process_ID)"
						+ " WHERE pbp.IsActive='Y' AND C_Period_ID = "+periodo.get_ID()
						+ " AND pbp.C_Bpartner_ID="+pBP.getC_BPartner_ID()+" AND p.IsActive='Y')");
				if(vHExtra == null)
					vHExtra = Env.ZERO;
				vHExtra = vHExtra.divide(new BigDecimal("190.0"), 0, RoundingMode.HALF_EVEN);
				
				BigDecimal dayhourscalc = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(DayTimeReal) as DayTimeReal " +
						" FROM HR_HourPlanningControl pc" +
						" INNER JOIN HR_HourPlanningLine pl ON (pc.HR_HourPlanningControl_ID = pl.HR_HourPlanningControl_ID)" +
						" WHERE C_BPartner_ID = "+pBP.getC_BPartner_ID()+
						" AND C_Period_ID = " +(periodo.get_ID()-1));
				if(dayhourscalc != null && dayhourscalc.compareTo(Env.ZERO) != 0)
				{
					dayhourscalc = dayhourscalc.divide(new BigDecimal("60.0"), 0,RoundingMode.HALF_EVEN);
					dayhourscalc = dayhourscalc.multiply(new BigDecimal("1.25"));
					dayhourscalc = dayhourscalc.multiply(vHExtra) ;
				}
				if(dayhourscalc != null && dayhourscalc.compareTo(Env.ZERO) > 0)
				{
					//se crea registro de detalle BP
					X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
					bpDet.setHR_ProcessBP_ID(pBP.get_ID());
					bpDet.setAD_Org_ID(pBP.getAD_Org_ID());					
					if(ID_ConceptHE > 0)
						bpDet.setHR_Concept_ID(ID_ConceptHE);		
					bpDet.setAmount(dayhourscalc);
					bpDet.saveEx();
				}
				
				//horas extra nocturna
				BigDecimal nightHoursCalc = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(NightTimeReal) as NightTimeReal " +
						" FROM HR_HourPlanningControl pc" +
						" INNER JOIN HR_HourPlanningLine pl ON (pc.HR_HourPlanningControl_ID = pl.HR_HourPlanningControl_ID)" +
						" WHERE C_BPartner_ID = "+pBP.getC_BPartner_ID()+
						" AND C_Period_ID = "  +(periodo.get_ID()-1));
				if(nightHoursCalc != null && nightHoursCalc.compareTo(Env.ZERO) != 0)
				{
					nightHoursCalc = nightHoursCalc.divide(new BigDecimal("60.0"), 0,RoundingMode.HALF_EVEN);
					nightHoursCalc = nightHoursCalc.multiply(new BigDecimal("1.5"));
					nightHoursCalc = nightHoursCalc.multiply(vHExtra) ;
				}
				if(nightHoursCalc != null && nightHoursCalc.compareTo(Env.ZERO) > 0)
				{
					//se crea registro de detalle BP
					X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
					bpDet.setHR_ProcessBP_ID(pBP.get_ID());
					bpDet.setAD_Org_ID(pBP.getAD_Org_ID());					
					if(ID_ConceptHENO > 0)
						bpDet.setHR_Concept_ID(ID_ConceptHENO);		
					bpDet.setAmount(nightHoursCalc);
					bpDet.saveEx();
				}
				
				//se agrega linea de diferencia isapre
				/*BigDecimal tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
				if(tasa == null)
					tasa = Env.ONE;
				BigDecimal pUF = DB.getSQLValueBD(get_TrxName(), "SELECT MAX(ISAPRE_UF) FROM HR_Employee " +
						" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(pUF == null)
					pUF = Env.ZERO;
				BigDecimal pSaludUF = pUF.multiply(tasa);
				BigDecimal topeUF = DB.getSQLValueBD(get_TrxName(), "SELECT MAX(TOPEISAPRE_UF) FROM HR_Employee " +
						" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(topeUF == null)
					topeUF = Env.ZERO;
				BigDecimal topeSalud = topeUF.multiply(tasa);
				BigDecimal amtSalud = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
						" FROM HR_ProcessBPDetail d " +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND  upper(c.description) = 'SALUD'");
				if(amtSalud == null)
					amtSalud = Env.ZERO;
				topeSalud = topeSalud.setScale(0, RoundingMode.HALF_EVEN);
				
				if(amtSalud.compareTo(pSaludUF) < 0 && amtSalud.compareTo(topeSalud) < 0)
				{				
					if(pSaludUF.compareTo(topeSalud)> 0)
					{
						//Se crea registro de diferencia entre tope y plan
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'ATSALUD'");
						if(ID_Concept > 0)
							bpDet.setHR_Concept_ID(ID_Concept);							
						bpDet.setAmount((pSaludUF.subtract(topeSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.saveEx();
						//se crea registro de adicional menor a tope
						X_HR_ProcessBPDetail bpDet2 = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet2.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet2.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept2 = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'ASALUD'");
						if(ID_Concept2 > 0)
							bpDet2.setHR_Concept_ID(ID_Concept2);							
						bpDet2.setAmount((topeSalud.subtract(amtSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet2.saveEx();					
					}
					else if(pSaludUF.compareTo(topeSalud)<= 0)
					{
						//se crea registro de adicional menor a tope
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'ASALUD'");
						if(ID_Concept > 0)
							bpDet.setHR_Concept_ID(ID_Concept);							
						bpDet.setAmount((pSaludUF.subtract(amtSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.saveEx();
					}
				}else if(amtSalud.compareTo(pSaludUF) < 0 && amtSalud.compareTo(topeSalud) >= 0)
				{
					//Se crea registro de diferencia entre tope y plan
					X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
					bpDet.setHR_ProcessBP_ID(pBP.get_ID());
					bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
					int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
							" WHERE upper(c.description) = 'ATSALUD'");
					if(ID_Concept > 0)
						bpDet.setHR_Concept_ID(ID_Concept);							
					bpDet.setAmount((pSaludUF.subtract(amtSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
					bpDet.saveEx();
				}else if(amtSalud.compareTo(topeSalud) > 0)
				{
					//Se crea registro de diferencia entre tope y 7%
					X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
					bpDet.setHR_ProcessBP_ID(pBP.get_ID());
					bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
					int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
							" WHERE upper(c.description) = 'ATSALUD'");
					if(ID_Concept > 0)
						bpDet.setHR_Concept_ID(ID_Concept);							
					bpDet.setAmount((amtSalud.subtract(topeSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
					bpDet.saveEx();
				}
				*/
				//Se agrega linea de aumento de no tributable y de descuento mayor a tope
				/*BigDecimal amtSaludImp = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1" +
						" FROM HR_ProcessBPDetail d " +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+
						" AND (upper(c.description) = 'SALUD' OR upper(c.description) = 'ASALUD')");
				if(amtSaludImp == null)
					amtSaludImp = Env.ZERO;
				if(pUF == null)
					pUF = Env.ZERO;
				if(tasa == null)
					tasa = Env.ZERO;
				BigDecimal totalPlanSalud = pUF.multiply(tasa);
				BigDecimal topeUF = DB.getSQLValueBD(get_TrxName(), "SELECT MAX(TOPEISAPRE_UF) FROM HR_Employee " +
						" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(topeUF == null)
					topeUF = Env.ZERO;
				BigDecimal topeSaludImp = topeUF.multiply(tasa);
				if(topeSaludImp  != null && topeSaludImp.compareTo(Env.ZERO) != 0 &&
						amtSaludImp  != null && amtSaludImp.compareTo(Env.ZERO) != 0)
				{
					//se sobreescribe monto de salus imponible si monto de plan es menor a 7% legal
					if(totalPlanSalud != null && totalPlanSalud.compareTo(Env.ZERO) > 0
							&&	totalPlanSalud.compareTo(amtSaludImp) < 0)
						amtSaludImp = totalPlanSalud;						
					if(amtSaludImp.compareTo(topeSaludImp) > 0)
					{
						//se crea registro que devuelve isapre no imponible														
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
							" WHERE upper(c.description) = 'DSALUD'");
						if(ID_Concept > 0)
						bpDet.setHR_Concept_ID(ID_Concept);							
						bpDet.setAmount((amtSaludImp.subtract(topeSaludImp)).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.saveEx();
						//se crea registro isapre no imponible
						X_HR_ProcessBPDetail bpDet2 = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet2.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet2.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept2 = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'NISALUD'");
						if(ID_Concept2 > 0)
							bpDet2.setHR_Concept_ID(ID_Concept2);							
						bpDet2.setAmount((amtSaludImp.subtract(topeSaludImp).negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet2.saveEx();
					}
				}*/
				//calculo de totales
				//total Imponible
				//se busca monto tope o sumatoria
				/*BigDecimal pTope = DB.getSQLValueBD(get_TrxName(), "SELECT MaxAmount FROM HR_Employee c WHERE c.C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(pTope == null)
					pTope =Env.ONE;
										
				//BigDecimal tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
				if(tasa == null)
					tasa = Env.ZERO;
				BigDecimal tope = pTope.multiply(tasa);
				tope = tope.setScale(0, RoundingMode.HALF_EVEN);
				BigDecimal tImp = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE d.Amount > 0 AND c.IsTaxable = 'Y' AND d.HR_ProcessBP_ID = "+pBP.get_ID());
				if(tImp == null)
					tImp = Env.ZERO;
				if(tImp.compareTo(tope) > 0 && tope.compareTo(Env.ZERO) > 0)
					pBP.set_CustomColumn("TotalAmt", tope);
				else 
				{
					tImp = tImp.setScale(0, RoundingMode.HALF_EVEN);
					pBP.set_CustomColumn("TotalAmt", tImp);
				}
				//total tributable				
				BigDecimal dTributable = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE c.IsTaxableDiscount = 'Y' AND d.HR_ProcessBP_ID = "+pBP.get_ID());
				if(dTributable == null)
					dTributable = Env.ZERO;				
				//se calcula total haberes
				BigDecimal tHaberes = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0)" +
						" FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE d.HR_ProcessBP_ID = "+pBP.get_ID()+" AND HR_Concept_Category_ID = 2000001");				
				BigDecimal tTributable = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0)" +
						" FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE d.HR_ProcessBP_ID = "+pBP.get_ID()+" AND HR_Concept_Category_ID = 2000001" +
						" AND c.IsTaxable = 'Y'");						
				tTributable = tTributable.add(dTributable);
				pBP.set_CustomColumn("Total3", tHaberes); //se guarda total haberes
				pBP.set_CustomColumn("Total1", tTributable);	
				*/
				//pBP.saveEx();
				//se calcula impuesto a la renta
				//ininoles impuesto a la renta se calculara una vez recalculados los totales despues de calculo de diferencias
				/* 
				String useUTax = DB.getSQLValueString(get_TrxName(), "SELECT MAX(UseUniqueTax) FROM HR_Employee " +
						" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(useUTax != null && useUTax.compareTo("Y") == 0)
				{
					String Type = DB.getSQLValueString(get_TrxName(), "SELECT MAX(TypeUniqueTax) FROM HR_Employee " +
							" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
					int ID_UniqueTax = DB.getSQLValue(get_TrxName(), "SELECT HR_SingleTax_id FROM HR_SingleTax " +
							" WHERE "+pBP.get_Value("Total1")+" BETWEEN minAmt AND MaxAmt AND C_Period_ID = ? AND Type='"+Type+"'",
							MPeriod.getC_Period_ID(getCtx(), pro.getDateAcct(),pro.getAD_Org_ID()));
					if(ID_UniqueTax > 0)
					{
						BigDecimal percent = DB.getSQLValueBD(get_TrxName(),
								"SELECT Percentage FROM HR_SIngleTax WHERE HR_SIngleTax_ID="+ID_UniqueTax);
						if(percent == null)
							percent = Env.ZERO;
						BigDecimal discount = DB.getSQLValueBD(get_TrxName(),
								"SELECT DiscountAmt FROM HR_SIngleTax WHERE HR_SIngleTax_ID="+ID_UniqueTax);
						if(discount == null)
							discount = Env.ZERO;
						BigDecimal taxUniqueAmt = (BigDecimal)pBP.get_Value("Total1");
						taxUniqueAmt = (taxUniqueAmt.multiply(percent)).subtract(discount);
						//se crea detalle
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'UNICO'");
						if(ID_Concept > 0)
							bpDet.setHR_Concept_ID(ID_Concept);
						bpDet.setAmount((taxUniqueAmt.negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.saveEx();
												
					}
				}*/
				//Liquido a pago (se calcula despues de calcular impuesto unico)
				BigDecimal tLiq = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(Amount),0) FROM HR_ProcessBPDetail " +
						" WHERE HR_ProcessBP_ID="+pBP.get_ID());
				if(tLiq != null)
				{
					tLiq = tLiq.setScale(0, RoundingMode.HALF_EVEN);
					pBP.set_CustomColumn("Total2", tLiq);
				}
				pBP.saveEx();		
				//ciclo 6 conceptos de bienestar con monto inponible
				//String sqlBienestarTr = "SELECT * FROM HR_Welfare WHERE C_Bpartner_ID = ? AND IsTaxable = 'Y'";
				String sqlBienestarTr = "SELECT * FROM HR_Welfare WHERE C_Bpartner_ID = ? AND IsTaxable = 'Y'"+
				 	" AND EndDate >= ? AND StartDate <= ? ";
				PreparedStatement pstmtBienestarTr = DB.prepareStatement(sqlBienestarTr, get_TrxName());
				pstmtBienestarTr.setInt(1, rs.getInt("C_Bpartner_ID"));
				pstmtBienestarTr.setTimestamp(2, periodo.getEndDate());
				pstmtBienestarTr.setTimestamp(3, periodo.getEndDate());
				ResultSet rsBienestarTr = pstmtBienestarTr.executeQuery();			
				while(rsBienestarTr.next())
				{
					//se valida si corresponde bono por mes
					String mesWel = "";
					if(rsBienestarTr.getString("months") != null && rsBienestarTr.getString("months").trim().length() > 0)
						mesWel = "-"+rsBienestarTr.getString("months")+"-";
					else
						mesWel = mes;
					if(mesWel.contains(mes))//corresponde bono
					{
						//porcentaje
						if(rsBienestarTr.getString("Type") != null && rsBienestarTr.getString("Type").compareTo("P") == 0)
						{
							if(rs.getInt("HR_Concept_ID") > 0)
							{							
								BigDecimal amtCon = (BigDecimal)pBP.get_Value("TotalAmt");
								if(amtCon == null)
									amtCon = Env.ZERO;
								BigDecimal amount = rs.getBigDecimal("Amount");
								amount = amount.multiply(amtCon).divide(Env.ZERO, 0, RoundingMode.HALF_EVEN);
								//se crea registro de detalle BP
								X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
								bpDet.setHR_ProcessBP_ID(pBP.get_ID());
								bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
								bpDet.setHR_Concept_ID(rsBienestar.getInt("HR_Concept_ID"));
								if(amount != null)
									bpDet.setAmount(amount);
								bpDet.saveEx();
							}
						}	
					}
				}				
				//se calcula total para bonos
				BigDecimal tBonus = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE c.IsBonusAmt = 'Y' AND d.HR_ProcessBP_ID = "+pBP.get_ID());
				pBP.set_CustomColumn("TotalBonus", tBonus);	
				pBP.saveEx();
				//ininoles se agrega exepcion para cuando no hay change y si horas extras
				int existChange = DB.getSQLValue(get_TrxName(),"SELECT COUNT(1) FROM HR_EmployeeChange " +
						" WHERE Processed = 'Y' AND DateTrx Between ? AND ? AND C_Bpartner_ID ="+pBP.getC_BPartner_ID(),startYear,periodo.getEndDate());
				int existHE = DB.getSQLValue(get_TrxName(),"SELECT COUNT(1) FROM HR_ProcessBPDetail " +
						" WHERE IsActive='Y' AND HR_Concept_ID IN (2000051,2000128) AND HR_ProcessBP_ID = "+pBP.get_ID());
				if(existChange <=0 && existHE > 0)
				{
					DB.executeUpdate("UPDATE HR_ProcessBPDetail SET Amount=0 WHERE HR_Concept_ID NOT IN (2000051,2000128,2000102,2000060,2000057,2000159,2000169,2000170,2000177,2000200) " +
							" AND HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				}				
				//se actualizan campos calculados por emilio
				DB.executeUpdate("UPDATE HR_ProcessBP SET HR_RETIREMENT = TotalAMT * 0.014 " +
						" WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				
				DB.executeUpdate("UPDATE HR_ProcessBP SET HR_SIS = TotalAMT * 0.0141 " +
						" WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				
				DB.executeUpdate("UPDATE HR_ProcessBP SET HR_Health = TotalAMT * 0.0093 " +
						" WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				commitEx();
				
				//ininoles se actualiza nuevo campo de monto
				DB.executeUpdate("UPDATE HR_ProcessBPDetail SET AmountOld = Amount WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				//se actualizan todos las incicencias 				
				DB.executeUpdate("UPDATE HR_Movement SET IsUsed = 'Y' WHERE C_Bpartner_ID ="+pBP.getC_BPartner_ID(),get_TrxName());
				
				
				//ininoles se busca registro antiguo para comparacion;
				//se busca reliquidcion ya hecha
				/*int ID_ProcessBPRel = DB.getSQLValue(get_TrxName(), "SELECT MAX(HR_ProcessBP_ID) FROM HR_ProcessBP " +
						" WHERE HR_Process_ID="+pro.get_ID()+" AND C_Bpartner_ID = "+pBP.getC_BPartner_ID()+
						" AND IsReassessment = 'Y' AND HR_ProcessBP_ID <> "+pBP.get_ID());*/
				int ID_ProcessBPRel = DB.getSQLValue(get_TrxName(), "SELECT MAX(HR_ProcessBP_ID) FROM HR_ProcessBP pbp" +
							" INNER JOIN HR_Process p ON (p.HR_Process_ID = pbp.HR_Process_ID)" +
							" WHERE pbp.IsActive='Y' AND C_Period_ID = "+periodo.get_ID()+" AND pbp.C_Bpartner_ID="+pBP.getC_BPartner_ID()+
							" AND p.IsActive='Y' AND IsReassessment = 'Y' AND HR_ProcessBP_ID <> "+pBP.get_ID());
				if(ID_ProcessBPRel > 0)
				{	
					pBP.set_CustomColumn("HR_ProcessBPRef_ID",ID_ProcessBPRel);
					
					String sqlCompa = "SELECT HR_Concept_ID,HR_ProcessBPDetail_ID FROM HR_ProcessBPDetail " +
					" WHERE Amount <> 0 AND HR_ProcessBP_ID="+pBP.get_ID();
			
					PreparedStatement pstmtCompa = null;
					pstmtCompa = DB.prepareStatement(sqlCompa, get_TrxName());
					ResultSet rsCompa = pstmtCompa.executeQuery();
					String ID_ConcepUsed = "1," ;
					//String ID_Concept = "";
					while(rsCompa.next())
					{
						if(!ID_ConcepUsed.contains(Integer.toString(rsCompa.getInt("HR_Concept_ID"))))						
						{
							//se busca valor antiguo para resta
							BigDecimal amtRes = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(AmountOld) FROM HR_ProcessBPDetail " +
									" WHERE HR_ProcessBP_ID="+ID_ProcessBPRel+" AND HR_Concept_ID="+rsCompa.getInt("HR_Concept_ID"));
							//se actualiza valor
							if(amtRes != null)
								DB.executeUpdate("UPDATE HR_ProcessBPDetail SET Amount = Amount-("+amtRes.intValue()+")"+
										" WHERE HR_ProcessBPDetail_ID="+rsCompa.getInt("HR_ProcessBPDetail_ID"), get_TrxName());
						}
						ID_ConcepUsed = ID_ConcepUsed+rsCompa.getInt("HR_Concept_ID");
					}
				}
				else //se busca liquidaci�n del mes en otros procesos
				{
					int ID_ProcessBPOld = DB.getSQLValue(get_TrxName(), "SELECT MAX(HR_ProcessBP_ID) FROM HR_ProcessBP pbp" +
							" INNER JOIN HR_Process p ON (p.HR_Process_ID = pbp.HR_Process_ID)" +
							" WHERE pbp.IsActive='Y' AND C_Period_ID = "+periodo.get_ID()+" AND pbp.C_Bpartner_ID="+pBP.getC_BPartner_ID()+
							" AND p.IsActive='Y' AND IsReassessment <> 'Y'");
					
					if(ID_ProcessBPOld > 0)
					{
						//se setea registro de sueldo usado
						pBP.set_CustomColumn("HR_ProcessBPRef_ID",ID_ProcessBPOld);
						
						//se borran los descuentos
						//DB.executeUpdate("DELETE FROM HR_ProcessBPDetail WHERE Amount < 0 AND HR_ProcessBP_ID="+pBP.get_ID(), get_TrxName());
						
						//ciclo de comparaci�n
						//String sqlCompa = "SELECT HR_Concept_ID,Amount,HR_ProcessBPDetail_ID FROM HR_ProcessBPDetail " +
						//		" WHERE Amount > 0 AND HR_ProcessBP_ID="+pBP.get_ID();
						
						String sqlCompa = "SELECT HR_Concept_ID,Amount,HR_ProcessBPDetail_ID FROM HR_ProcessBPDetail " +
						//		" WHERE Amount <> 0 AND HR_ProcessBP_ID="+pBP.get_ID();
						//se crea excepci�n para asignacion de antiguedad
								" WHERE (Amount <> 0 OR HR_Concept_ID=2000102) AND HR_ProcessBP_ID="+pBP.get_ID();
						
						PreparedStatement pstmtCompa = null;
						pstmtCompa = DB.prepareStatement(sqlCompa, get_TrxName());
						ResultSet rsCompa = pstmtCompa.executeQuery();
						String ID_ConcepUsed = "1," ;
						//String ID_Concept = "";
						while(rsCompa.next())
						{
							if(!ID_ConcepUsed.contains(Integer.toString(rsCompa.getInt("HR_Concept_ID"))))						
							{
								//se busca valor antiguo para resta
								BigDecimal amtRes = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(Amount) FROM HR_ProcessBPDetail " +
										" WHERE HR_ProcessBP_ID="+ID_ProcessBPOld+" AND HR_Concept_ID="+rsCompa.getInt("HR_Concept_ID"));
								//se actualiza valor
								if(amtRes != null)
									DB.executeUpdate("UPDATE HR_ProcessBPDetail SET Amount = Amount-("+amtRes.intValue()+")"+
											" WHERE HR_ProcessBPDetail_ID="+rsCompa.getInt("HR_ProcessBPDetail_ID"), get_TrxName());
							}
							ID_ConcepUsed = ID_ConcepUsed+rsCompa.getInt("HR_Concept_ID");
						}
					}
				}				
				//recalculo de totales
				int ID_ProcessBPOLD = DB.getSQLValue(get_TrxName(), "SELECT MAX(HR_ProcessBP_ID) FROM HR_ProcessBP pbp " +
						" INNER JOIN HR_Process p ON (p.HR_Process_ID = pbp.HR_Process_ID)" +
						" WHERE pbp.IsActive='Y' AND C_Period_ID = "+periodo.get_ID()+" AND pbp.C_Bpartner_ID="+pBP.getC_BPartner_ID()+
						" AND p.IsActive='Y' AND IsReassessment <> 'Y'");
				X_HR_ProcessBP pbpOld = new X_HR_ProcessBP(getCtx(), ID_ProcessBPOLD, get_TrxName());
				//total Imponible
				//se busca monto tope o sumatoria
				BigDecimal pTope = DB.getSQLValueBD(get_TrxName(), "SELECT MaxAmount FROM HR_Employee c WHERE c.C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(pTope == null)
					pTope =Env.ONE;										
				BigDecimal tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
				if(tasa == null)
					tasa = Env.ZERO;
				BigDecimal tope = pTope.multiply(tasa);
				tope = tope.setScale(0, RoundingMode.HALF_EVEN);
				BigDecimal tImp = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE c.IsTaxable = 'Y' AND d.HR_ProcessBP_ID = "+pBP.get_ID());
				if(tImp == null)
					tImp = Env.ZERO;
				//ininoles se suma total imponible de periodo anterior
				BigDecimal tImpOld = DB.getSQLValueBD(get_TrxName(), "SELECT TotalAmt FROM HR_ProcessBP pbp " +
						" WHERE HR_ProcessBP_ID = "+pbpOld.get_ID());
				if(tImpOld == null)
					tImpOld = Env.ZERO;
				tImp=tImp.add(tImpOld);
				if(tImp.compareTo(tope) > 0 && tope.compareTo(Env.ZERO) > 0)
					pBP.set_CustomColumn("TotalAmt", tope);
				else 
				{
					tImp = tImp.setScale(0, RoundingMode.HALF_EVEN);
					//se resta monto periodo anterior
					pBP.set_CustomColumn("TotalAmt", tImp.subtract(tImpOld));
				}
				pBP.saveEx(get_TrxName());
				//se calculan descuentos luego de la actualizacion de montos solo si imponible cambia
				if(((BigDecimal)pBP.get_Value("TotalAmt")).compareTo(Env.ZERO) > 0)
				{	
					String sqlConceptDe = " SELECT pc.HR_Concept_ID FROM HR_PayrollConcept pc" +
							" INNER JOIN HR_Concept c ON (pc.HR_Concept_ID = c.HR_Concept_ID)" +
							" WHERE pc.IsActive = 'Y' AND c.IsActive = 'Y' AND TypeConcept = 'D1' AND pc.HR_Payroll_ID = ?"+
							" AND (c.C_Year_ID IS NUll OR c.C_Year_ID="+periodo.getC_Year_ID()+") ORDER BY pc.SeqNo";
					PreparedStatement pstmtConceptDe = DB.prepareStatement(sqlConceptDe, get_TrxName());
					pstmtConceptDe.setInt(1, pro.getHR_Payroll_ID());
					ResultSet rsConceptDe = pstmtConceptDe.executeQuery();			
					while(rsConceptDe.next())
					{
						X_HR_Concept conc = new X_HR_Concept(getCtx(), rsConceptDe.getInt("HR_Concept_ID"), get_TrxName());
						//logica para buscar nombres de campos y calcular descuentos
						BigDecimal amtTotal = Env.ZERO;
						BigDecimal porc = Env.ZERO;
						if(conc.get_ValueAsString("HR_Discount1") != null && conc.get_ValueAsString("HR_Discount1").trim().length() > 0)
						{
							porc = DB.getSQLValueBD(get_TrxName(), "SELECT "+conc.get_ValueAsString("HR_Discount1")+" FROM HR_Employee " +
									" where C_Bpartner_ID = "+rs.getInt("C_Bpartner_ID"));
							if(porc == null)
								porc = Env.ZERO;
						}
						amtTotal = porc.multiply((BigDecimal)pBP.get_Value("TotalAmt"));						
						amtTotal = amtTotal.divide(Env.ONEHUNDRED, 0, RoundingMode.HALF_EVEN);
						//se niega monto total
						amtTotal = amtTotal.negate();
						//antes de crear registro se verifica topey se genera linea extra de ser necesario
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						bpDet.setHR_Concept_ID(conc.get_ID());
						bpDet.setAmount(amtTotal);						
						bpDet.saveEx();
						bpDet.set_CustomColumn("AmountOld",bpDet.getAmount());
						bpDet.saveEx();
					}
					
					//se calcula adicional de salud de ser necesario
					tasa = getRate(2000000,228,dateTope,114,pro.getAD_Client_ID(), pro.getAD_Org_ID());
					if(tasa == null)
						tasa = Env.ONE;
					BigDecimal pUF = DB.getSQLValueBD(get_TrxName(), "SELECT MAX(ISAPRE_UF) FROM HR_Employee " +
							" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
					if(pUF == null)
						pUF = Env.ZERO;
					BigDecimal pSaludUF = pUF.multiply(tasa);
					BigDecimal topeUF = DB.getSQLValueBD(get_TrxName(), "SELECT MAX(TOPEISAPRE_UF) FROM HR_Employee " +
							" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
					if(topeUF == null)
						topeUF = Env.ZERO;
					BigDecimal topeSalud = topeUF.multiply(tasa);
					topeSalud = topeSalud.setScale(0, RoundingMode.HALF_EVEN);
					
					BigDecimal amtSalud = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
							" FROM HR_ProcessBPDetail d " +
							" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
							" WHERE HR_ProcessBP_ID = "+pBP.get_ID()+" AND  upper(c.description) = 'SALUD'");
					if(amtSalud == null)
						amtSalud = Env.ZERO;
					//se suma monto de mes de liquidacion
					BigDecimal amtSaludOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
							" FROM HR_ProcessBPDetail d " +
							" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
							" WHERE upper(c.description) = 'SALUD' AND HR_ProcessBP_ID = "+pbpOld.get_ID()); 
					if(amtSaludOld == null)
						amtSaludOld = Env.ZERO;		
					amtSalud = amtSalud.add(amtSaludOld);
					if(amtSalud.compareTo(pSaludUF) < 0 && amtSalud.compareTo(topeSalud) < 0)
					{				
						if(pSaludUF.compareTo(topeSalud)> 0)
						{
							//Se crea registro de diferencia entre tope y plan
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
									" WHERE upper(c.description) = 'ATSALUD'");
							if(ID_Concept > 0)
								bpDet.setHR_Concept_ID(ID_Concept);		
							//se busca rebistro anterior para resta
							BigDecimal amtATSaludOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
									" FROM HR_ProcessBPDetail d " +
									" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
									" WHERE upper(c.description) = 'ATSALUD' AND HR_ProcessBP_ID = "+pbpOld.get_ID());
							if(amtATSaludOld == null)
								amtATSaludOld = Env.ZERO;						
							bpDet.setAmount((pSaludUF.subtract(topeSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
							bpDet.setAmount(bpDet.getAmount().add(amtATSaludOld));
							bpDet.saveEx();
							bpDet.set_CustomColumn("AmountOld",bpDet.getAmount());
							bpDet.saveEx();
							//se crea registro de adicional menor a tope
							X_HR_ProcessBPDetail bpDet2 = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet2.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet2.setAD_Org_ID(pBP.getAD_Org_ID());
							int ID_Concept2 = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
									" WHERE upper(c.description) = 'ASALUD'");
							if(ID_Concept2 > 0)
								bpDet2.setHR_Concept_ID(ID_Concept2);
							BigDecimal amtASaludOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
									" FROM HR_ProcessBPDetail d " +
									" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
									" WHERE upper(c.description) = 'ASALUD' AND HR_ProcessBP_ID = "+pbpOld.get_ID());
							if(amtASaludOld == null)
								amtASaludOld = Env.ZERO;
							bpDet2.setAmount((topeSalud.subtract(amtSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
							bpDet2.setAmount(bpDet2.getAmount().add(amtASaludOld));							
							bpDet2.saveEx();					
							bpDet2.set_CustomColumn("AmountOld",bpDet.getAmount());
							bpDet2.saveEx();					
						}
						else if(pSaludUF.compareTo(topeSalud)<= 0)
						{
							//se crea registro de adicional menor a tope
							X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
							bpDet.setHR_ProcessBP_ID(pBP.get_ID());
							bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
							int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
									" WHERE upper(c.description) = 'ASALUD'");
							if(ID_Concept > 0)
								bpDet.setHR_Concept_ID(ID_Concept);				
							BigDecimal amtASaludOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
									" FROM HR_ProcessBPDetail d " +
									" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
									" WHERE upper(c.description) = 'ASALUD' AND HR_ProcessBP_ID = "+pbpOld.get_ID());
							if(amtASaludOld == null)
								amtASaludOld = Env.ZERO;		
							bpDet.setAmount((pSaludUF.subtract(amtSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
							bpDet.setAmount(bpDet.getAmount().add(amtASaludOld));
							bpDet.saveEx();
							bpDet.set_CustomColumn("AmountOld",bpDet.getAmount());
							bpDet.saveEx();
						}
					}else if(amtSalud.compareTo(pSaludUF) < 0 && amtSalud.compareTo(topeSalud) >= 0)
					{
						//Se crea registro de diferencia entre tope y plan
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'ATSALUD'");
						if(ID_Concept > 0)
							bpDet.setHR_Concept_ID(ID_Concept);			
						BigDecimal amtATSaludOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
								" FROM HR_ProcessBPDetail d " +
								" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
								" WHERE upper(c.description) = 'ATSALUD' AND HR_ProcessBP_ID = "+pbpOld.get_ID());
						if(amtATSaludOld == null)
							amtATSaludOld = Env.ZERO;						
						bpDet.setAmount((pSaludUF.subtract(amtSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.setAmount(bpDet.getAmount().add(amtATSaludOld));
						bpDet.saveEx();
						bpDet.set_CustomColumn("AmountOld",bpDet.getAmount());
						bpDet.saveEx();
					}else if(amtSalud.compareTo(topeSalud) > 0)
					{
						//Se crea registro de diferencia entre tope y 7%
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'ATSALUD'");
						if(ID_Concept > 0)
							bpDet.setHR_Concept_ID(ID_Concept);		
						BigDecimal amtATSaludOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
								" FROM HR_ProcessBPDetail d " +
								" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
								" WHERE upper(c.description) = 'ATSALUD' AND HR_ProcessBP_ID = "+pbpOld.get_ID());
						if(amtATSaludOld == null)
							amtATSaludOld = Env.ZERO;
						bpDet.setAmount((amtSalud.subtract(topeSalud).negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.setAmount(bpDet.getAmount().add(amtATSaludOld));
						bpDet.saveEx();
						bpDet.set_CustomColumn("AmountOld",bpDet.getAmount());
						bpDet.saveEx();
					}
				}				
				
				//se calcula total tributable				
				BigDecimal dTributable = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0) FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE c.IsTaxableDiscount = 'Y' AND d.HR_ProcessBP_ID = "+pBP.get_ID());
				if(dTributable == null)
					dTributable = Env.ZERO;
				BigDecimal tTributable = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0)" +
						" FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE d.HR_ProcessBP_ID = "+pBP.get_ID()+" AND HR_Concept_Category_ID = 2000001" +
						" AND c.IsTaxable = 'Y'");						
				tTributable = tTributable.add(dTributable);
				//se busca registro de total tributable de liquidacion del mes
				/*BigDecimal tTributableOld = DB.getSQLValueBD(get_TrxName(), "SELECT Total1 FROM HR_ProcessBP pbp " +
						" WHERE HR_ProcessBP_ID ="+pbpOld.get_ID());
				if(tTributableOld == null)
					tTributableOld = Env.ZERO;				
				tTributable = tTributable.add(tTributableOld);*/
				pBP.set_CustomColumn("Total1", tTributable);
				//se calcula total haberes
				BigDecimal tHaberes = DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(d.Amount),0)" +
						" FROM HR_ProcessBPDetail d" +
						" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
						" WHERE d.HR_ProcessBP_ID = "+pBP.get_ID()+" AND HR_Concept_Category_ID = 2000001");				
				pBP.set_CustomColumn("Total3", tHaberes); //se guarda total haberes
				pBP.saveEx(get_TrxName());
				//se calcula impuesto a la renta
				
 				String useUTax = DB.getSQLValueString(get_TrxName(), "SELECT MAX(UseUniqueTax) FROM HR_Employee " +
						" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
				if(useUTax != null && useUTax.compareTo("Y") == 0)
				{
					BigDecimal taxUniqueAmt = (BigDecimal)pBP.get_Value("Total1");
					//se suma tributable de mes de liquidacion
					BigDecimal tTributableOld = DB.getSQLValueBD(get_TrxName(), "SELECT Total1 FROM HR_ProcessBP pbp " +
							" WHERE HR_ProcessBP_ID ="+pbpOld.get_ID());
					if(tTributableOld == null)
						tTributableOld = Env.ZERO;	
					taxUniqueAmt = taxUniqueAmt.add(tTributableOld);
					String Type = DB.getSQLValueString(get_TrxName(), "SELECT MAX(TypeUniqueTax) FROM HR_Employee " +
							" WHERE C_Bpartner_ID="+rs.getInt("C_Bpartner_ID"));
					int ID_UniqueTax = DB.getSQLValue(get_TrxName(), "SELECT HR_SingleTax_id FROM HR_SingleTax " +
							" WHERE "+taxUniqueAmt+" BETWEEN minAmt AND MaxAmt AND C_Period_ID = ? AND Type='"+Type+"'",
							periodo.get_ID());
					if(ID_UniqueTax > 0)
					{
						BigDecimal percent = DB.getSQLValueBD(get_TrxName(),
								"SELECT Percentage FROM HR_SIngleTax WHERE HR_SIngleTax_ID="+ID_UniqueTax);
						if(percent == null)
							percent = Env.ZERO;
						BigDecimal discount = DB.getSQLValueBD(get_TrxName(),
								"SELECT DiscountAmt FROM HR_SIngleTax WHERE HR_SIngleTax_ID="+ID_UniqueTax);
						if(discount == null)
							discount = Env.ZERO;
	
						taxUniqueAmt = (taxUniqueAmt.multiply(percent)).subtract(discount);
						//se crea detalle
						//se crea registro de detalle BP
						X_HR_ProcessBPDetail bpDet = new X_HR_ProcessBPDetail(getCtx(), 0, get_TrxName());
						bpDet.setHR_ProcessBP_ID(pBP.get_ID());
						bpDet.setAD_Org_ID(pBP.getAD_Org_ID());
						int ID_Concept = DB.getSQLValue(get_TrxName(), "SELECT HR_Concept_ID FROM HR_Concept c " +
								" WHERE upper(c.description) = 'UNICO'");
						if(ID_Concept > 0)
							bpDet.setHR_Concept_ID(ID_Concept);
						//se resta impuesto unico de liquidacion
						BigDecimal amtIUnicoOld = DB.getSQLValueBD(get_TrxName(), "SELECT SUM(d.Amount)*-1 " +
								" FROM HR_ProcessBPDetail d " +
								" INNER JOIN HR_Concept c ON (d.HR_Concept_ID = c.HR_Concept_ID)" +
								" WHERE upper(c.description) = 'UNICO' AND HR_ProcessBP_ID = "+pbpOld.get_ID());
						if(amtIUnicoOld == null)
							amtIUnicoOld = Env.ZERO;
						bpDet.setAmount((taxUniqueAmt.negate()).setScale(0, RoundingMode.HALF_EVEN));
						bpDet.setAmount(bpDet.getAmount().add(amtIUnicoOld));
						bpDet.saveEx();								
						bpDet.set_CustomColumn("AmountOld",bpDet.getAmount());
						bpDet.saveEx();
					}
				}
				
				//ininoles se actualiza nuevo campo de monto de descuentos
				//DB.executeUpdate("UPDATE HR_ProcessBPDetail SET AmountOld = Amount WHERE Amount < 0 AND HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				//ininoles se actualizan nuevos campos pedidos por alberto 
				DB.executeUpdate("UPDATE HR_ProcessBPDetail SET HR_ProcessBPRef_ID = "+p_ProcessBP_ID+" WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				DB.executeUpdate("UPDATE HR_ProcessBPDetail SET C_PeriodProcess_ID = "+periodo.get_ID()+" WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				DB.executeUpdate("UPDATE HR_ProcessBPDetail SET C_PeriodReassessment_ID  = "+p_PeriodTo_ID+" WHERE HR_ProcessBP_ID = "+pBP.get_ID(), get_TrxName());
				
				//recalculo de total liquido
				pBP.set_CustomColumn("Total2", DB.getSQLValueBD(get_TrxName(), "SELECT COALESCE(SUM(Amount),0) FROM HR_ProcessBPDetail " +
						" WHERE HR_ProcessBP_ID="+pBP.get_ID()));
				pBP.saveEx(get_TrxName());
			}
			//se busca siguiente periodo
			if(periodo.getPeriodNo() < 12)
			{
				ID_Periodo = DB.getSQLValue(get_TrxName(), "SELECT C_Period_ID FROM C_Period WHERE C_Year_ID="+periodo.getC_Year_ID()+
						" AND PeriodNo="+(periodo.getPeriodNo()+1));
			}
			else if(periodo.getPeriodNo() == 12)
			{
				//se busca periodo del prox a�o.
				ID_Periodo = DB.getSQLValue(get_TrxName(), "SELECT C_Period_ID FROM C_Period WHERE C_Year_ID="+(periodo.getC_Year_ID()+1)+
						" AND PeriodNo= 1");
			}
		}	
		//se actualizan change despues de todos los calculos
		DB.executeUpdate("UPDATE HR_EmployeeChange SET IsUsed = 'Y' WHERE C_Bpartner_ID IN ("+ID_BpartnerUsed+")",get_TrxName());
		
		return "Procesado "+logM;
	   
	}	//	doIt
	
	public static BigDecimal getRate (int CurFrom_ID, int CurTo_ID,
			Timestamp ConvDate, int ConversionType_ID, int AD_Client_ID, int AD_Org_ID)
		{
			if (CurFrom_ID == CurTo_ID)
				return Env.ONE;
			//	Conversion Type
			int C_ConversionType_ID = ConversionType_ID;
			if (C_ConversionType_ID == 0)
				C_ConversionType_ID = MConversionType.getDefault(AD_Client_ID);
			//	Conversion Date
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());

			//	Get Rate
			String sql = "SELECT MultiplyRate "
				+ "FROM C_Conversion_Rate "
				+ "WHERE IsTaxable = 'Y' " 
				+ " AND C_Currency_ID=?"					//	#1
				+ " AND C_Currency_ID_To=?"					//	#2
				+ " AND	C_ConversionType_ID=?"				//	#3
				//+ " AND	? BETWEEN ValidFrom AND ValidTo"	//	#4	TRUNC (?) ORA-00932: inconsistent datatypes: expected NUMBER got TIMESTAMP
				+ " AND ? BETWEEN EXTRACT (month from ValidFrom) AND EXTRACT (month from ValidTo)"
				+ " AND AD_Client_ID IN (0,?)"				//	#5
				+ " AND AD_Org_ID IN (0,?) "				//	#6
				+ "ORDER BY AD_Client_ID DESC, AD_Org_ID DESC, ValidFrom DESC";
			BigDecimal retValue = null;
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, CurFrom_ID);
				pstmt.setInt(2, CurTo_ID);
				pstmt.setInt(3, C_ConversionType_ID);
				pstmt.setInt(4, ConvDate.getMonth()+1);
				pstmt.setInt(5, AD_Client_ID);
				pstmt.setInt(6, AD_Org_ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					retValue = rs.getBigDecimal(1);
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				s_log.log(Level.SEVERE, "getRate", e);
			}
			try
			{
				if (pstmt != null)
					pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				pstmt = null;
			}			
			if (retValue == null)
				s_log.info ("getRate - not found - CurFrom=" + CurFrom_ID 
				  + ", CurTo=" + CurTo_ID
				  + ", " + ConvDate 
				  + ", Type=" + ConversionType_ID + (ConversionType_ID==C_ConversionType_ID ? "" : "->" + C_ConversionType_ID) 
				  + ", Client=" + AD_Client_ID 
				  + ", Org=" + AD_Org_ID);
			return retValue;
		}	//	getRate	
}
