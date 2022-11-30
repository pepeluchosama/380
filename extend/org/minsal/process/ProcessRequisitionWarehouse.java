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
package org.minsal.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.model.X_GL_BudgetControlLine;
import org.compiere.model.X_GL_BudgetControlDetail;

/**
 *	
 *	
 *  @author mfrojas 
 *  @version $Id: ProcessRequisitionWarehouse.java $
 */
public class ProcessRequisitionWarehouse extends SvrProcess
{
	//private String			p_DocStatus = null;
	private int				p_Requisition_ID = 0; 
	private String				p_Action = "PR";
	//mfrojas Se agrega nuevo parametro para recibir mensaje al momento de anular.
	private String 			p_Message = "";
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	 protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			
			if (name.equals("Action"))
				p_Action = para[i].getParameterAsString();
			else if (name.equals("Message"))
				p_Message = para[i].getParameterAsString();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_Requisition_ID=getRecord_ID();
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		if (p_Requisition_ID > 0)
		{
			MRequisition req = new MRequisition(getCtx(), p_Requisition_ID, get_TrxName());
			
			//seteo de nuevo estado al procesar
			String newStatus = "DR";
			//se setea automaticamente a los nuevos estados
			if(req.getDocStatus().compareTo("DR")==0 && p_Action.compareTo("CO")==0)
				p_Action="IP";
			else if(req.getDocStatus().compareTo("IP")==0 && p_Action.compareTo("CO")==0)
				p_Action="CO";
			//estados para anulado
			else if((req.getDocStatus().compareTo("DR")==0 || req.getDocStatus().compareTo("IP")==0)
					&& p_Action.compareTo("VO")==0)
				p_Action="VO";
			/*else if(req.getDocStatus().compareTo("CO")==0)
				p_Action="VO";*/
			else 
				p_Action="CO";			
			
			//String newAction = "DR";	
			//String modality = req.get_Value("Modality").toString();
			
			//mfrojas	
			String actualStatus = req.getDocStatus();
			int workflowID = DB.getSQLValue(get_TrxName(), "SELECT coalesce(max(ad_workflow_id),0) from ad_workflow "
					+ " WHERE c_doctype_id = ? and ad_client_id = "+Env.getAD_Client_ID(getCtx()), req.getC_DocType_ID());
			
			log.config("paction "+p_Action);
			log.config("workflowID "+workflowID);
			log.config("estado actual "+actualStatus);
			
			if(workflowID > 0)
			{
				
				newStatus = p_Action;
				//Validador de permisos de rol
				int cant = DB.getSQLValue(get_TrxName(), "SELECT COUNT(1) FROM AD_Document_Action_Access daa" +
						" INNER JOIN AD_Ref_List rl ON (daa.AD_Ref_List_ID = rl.AD_Ref_List_ID) " +
						" WHERE value = '"+newStatus+"' AND AD_Role_ID = "+Env.getAD_Role_ID(getCtx())+
						" AND C_DocType_ID = "+req.getC_DocType_ID());

				if(cant > 0)
				{

					//mfrojas 20210329
					//Validar que todas las lineas tengan un producto asociado.
					String sqlpr = "SELECT count(1) from m_requisitionline WHERE (m_product_id <= 0 OR m_product_id is null)"
							+ " AND m_Requisition_id = "+req.get_ID();
					int counter = DB.getSQLValue(get_TrxName(), sqlpr);
					log.config("sql "+sqlpr);
					if(counter > 0)
					{
						throw new AdempiereException("Alguna de las lineas no tiene producto asociado.");
						
						
					}
						
					//Se debera validar las cantidades y las fechas
					//PARA SOLICITUDES DE CONSUMO
					
					if(req.getC_DocType().getDocBaseType().compareTo("RMV") != 0)
					{	
						MRequisitionLine reql = null;
						String sqlobtainproduct = "SELECT M_RequisitionLine_ID FROM M_RequisitionLine WHERE M_Requisition_ID = "+req.get_ID();
						PreparedStatement pstmt = null;
						ResultSet rs = null;
						
						PreparedStatement pstmt2 = null;
						ResultSet rs2 = null;
						
						int reqline_id = 0;
						pstmt=DB.prepareStatement(sqlobtainproduct, get_TrxName());
						rs = pstmt.executeQuery();
						
						int gl_budgetcontrolline_id = 0;
					
						X_GL_BudgetControlLine bugl = null;
						while(rs.next())
						{
							reqline_id = rs.getInt("M_RequisitionLine_ID");
							reql = new MRequisitionLine(getCtx(), reqline_id, get_TrxName());
							
						

							//Primero se debe obtener el lv4 del presupuesto
							
							String sqlget4 = "SELECT coalesce(max(gl_budgetcontrolline_id),0) from gl_budgetcontrolline WHERE "
								+ " M_product_id = "+reql.getM_Product_ID()+" AND ad_org_id = "+reql.getAD_Org_ID()+ " AND "
								+ " isactive='Y'"
								+ "  AND gl_budgetdetail_id in (select gl_budgetdetail_id from gl_budgetdetail WHERE "  
								+ " gl_budgetcontrol_id in (select gl_budgetcontrol_id from gl_budgetcontrol WHERE "  
								+ " gl_budgetcontrolheader_id in (select gl_budgetcontrolheader_Id from gl_budgetcontrolheader WHERE "  
								+ " isprogram='N' and isactive='Y' )))";
						
							gl_budgetcontrolline_id = DB.getSQLValue(get_TrxName(), sqlget4);
							if(gl_budgetcontrolline_id <=0 )
								continue;
							
							bugl = new X_GL_BudgetControlLine(getCtx(), gl_budgetcontrolline_id, get_TrxName());

							//Luego se debe validar si la cantidad en nivel 4 es menor a 12. 
							//De ser así, la cantidad total es acumulable.
							if(bugl.getQty().compareTo(new BigDecimal(12)) < 0)
							{
								//BigDecimal qtyused = Env.ZERO;
								//qtyused = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyused),0) FROM gl_budgetcontroldetail WHERE gl_budgetcontrolline_id = ?", gl_budgetcontrolline_id);
								//if(qtyused.compareTo(bugl.getQty()) >= 0)
								//	throw new AdempiereException("Cantidad usada es mayor o igual a la cantidad presupuestada: Linea "+reql.getLine());
								/*else
								{
									if(qtyused.add(reql.getQty()).compareTo(bugl.getQty()) > 0)
										throw new AdempiereException("La cantidad sobrepasa el disponible: Linea "+reql.getLine());
									else
									{*/
								if(newStatus.compareTo("CO") == 0)
								{	
									DB.executeUpdate("UPDATE gl_budgetcontroldetail SET qtyused = qtyused+"+reql.getQty()+" WHERE gl_budgetcontrolline_id = "+gl_budgetcontrolline_id, get_TrxName());
									
									//mfrojas demo 20200922
									//Relacionar linea de solicitud con presupuesto.
									int budgetcontroldetail  = DB.getSQLValue(get_TrxName(), "SELECT coalesce(max(gl_budgetcontroldetail_id),0) FROM gl_budgetcontroldetail "
											+ " WHERE gl_budgetcontrolline_id = "+gl_budgetcontrolline_id);
									if(budgetcontroldetail > 0)
									{
										try
										{
											reql.set_CustomColumn("GL_BudgetControlDetail_ID", budgetcontroldetail);
											reql.save();
										}catch(Exception e)
										{
											log.config("no hay campo glbudgetcontroldetail");
										}
												
									}
								
										
									
								}
							}
							
							//si la cantidad es 12, o multiplo de 12, se debe validar si la cantidad es acumulable o no.
							else if(bugl.getQty().compareTo(new BigDecimal(12)) >= 0 )
							{
								//aca se ve si es acumulable. Se necesita la categoria
								String sqlgetacc = "SELECT IsQuantityBased FROM m_product_category where m_product_category_id in "
										+ " (SELECT m_product_category_id from m_product WHERE m_product_id = ?)";
								String acc = DB.getSQLValueString(get_TrxName(), sqlgetacc, bugl.getM_Product_ID());
								BigDecimal qtyused = Env.ZERO;
								BigDecimal qtyav = Env.ZERO;
								BigDecimal qtyav2 = Env.ZERO;
								if(acc.compareTo("Y") == 0)
								{
									//Si es acumulable, entonces podemos utilizar el disponible de todas las lineas.
								
									//mfrojas actualizacion 20200429
									//Si la solicitud es del dia 26 en adelante, se debe validar contra el mes siguiente.
									BigDecimal getDay = DB.getSQLValueBD(get_TrxName(), "SELECT extract(day from daterequired) FROM "
											+ " m_requisition WHERE m_requisition_id = ? ", req.get_ID());
									if(getDay.compareTo(new BigDecimal(25)) > 0)
									{
										//mes +1
										qtyused = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyused),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) <= extract(month from ('"+req.getDateRequired()+"'::date+'1 month'::interval)::timestamp)", gl_budgetcontrolline_id);
										qtyav = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyentered),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) <= extract(month from ('"+req.getDateRequired()+"'::date+'1 month'::interval)::timestamp)", gl_budgetcontrolline_id);
								
										qtyav2 = qtyav.subtract(qtyused);
									}
									else
									{
										//mes
										qtyused = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyused),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) <= extract(month from '"+req.getDateRequired()+"'::timestamp)", gl_budgetcontrolline_id);
										qtyav = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyentered),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) <= extract(month from '"+req.getDateRequired()+"'::timestamp)", gl_budgetcontrolline_id);
									
										qtyav2 = qtyav.subtract(qtyused);

									}
									
									log.config("usado = "+qtyused);
									log.config("presupuestado = "+qtyav);
									log.config("disp = "+qtyav2);
								
									/*if(reql.getQty().compareTo(qtyav2) > 0)
										throw new AdempiereException("No hay disponible para producto "+reql.getM_Product().getName()+" Linea : "+reql.getLine());
									else
										{*/
										//Si el estado nuevo es "completo", se actualiza el presupuesto. Si no, continua.
									if(newStatus.compareTo("CO") == 0)
									{
										String sqlgetdetails = null;
										if(getDay.compareTo(new BigDecimal(25)) > 0)
											sqlgetdetails = "SELECT gl_budgetcontroldetail_id from gl_budgetcontroldetail WHERE "
													+ " gl_budgetcontrolline_id = "+gl_budgetcontrolline_id+" AND "
													+ " isactive='Y' and extract(month from datetrx) <= extract(month from ('"+req.getDateRequired()+"'::date+'1 month'::interval)::timestamp) and isactive='Y' ORDER BY datetrx";
										else
											sqlgetdetails = "SELECT gl_budgetcontroldetail_id from gl_budgetcontroldetail WHERE "
													+ " gl_budgetcontrolline_id = "+gl_budgetcontrolline_id+" AND "
													+ " isactive='Y' and extract(month from datetrx) <= extract(month from '"+req.getDateRequired()+"'::timestamp) and isactive='Y' ORDER BY datetrx";

										pstmt2=DB.prepareStatement(sqlgetdetails, get_TrxName());
										rs2=pstmt2.executeQuery();
										BigDecimal disp = Env.ZERO;
										disp = reql.getQty();
									
										while(rs2.next())
										{
											X_GL_BudgetControlDetail bud = new X_GL_BudgetControlDetail(getCtx(), rs2.getInt("gl_budgetcontroldetail_id"), get_TrxName());
											if(bud.getQtyEntered().compareTo((BigDecimal)bud.get_Value("QtyUsed")) <= 0)
											{
												continue;
											}
											else
											{
												log.config("disp en periodo "+bud.getQtyEntered().subtract(new BigDecimal(bud.get_ValueAsInt("QtyUsed"))));
												log.config("qtyentered "+bud.getQtyEntered());
												log.config("qtyused "+bud.get_Value("QtyUsed"));
												log.config("qtyused bigd "+bud.get_Value("QtyUsed") );
												//if(bud.getQtyEntered().subtract(new BigDecimal(bud.get_ValueAsInt("QtyUsed"))).compareTo(disp) >= 0)
												if(bud.getQtyEntered().subtract((BigDecimal) bud.get_Value("QtyUsed")).compareTo(disp) >= 0)
												{
													BigDecimal newused = (BigDecimal) bud.get_Value("QtyUsed");
													newused = newused.add(disp);
													bud.set_CustomColumn("QtyUsed", newused);
													bud.saveEx();
													break;
												}
												else
												{
													BigDecimal newused = bud.getQtyEntered().subtract((BigDecimal)bud.get_Value("QtyUsed"));
													bud.set_CustomColumn("QtyUsed", bud.getQtyEntered());
													disp = disp.subtract(newused);
												}
											
											}
											bud.saveEx();
											//mfrojas 20200922
												//Relacionar linea de solicitud con presupuesto.
											try
											{
												reql.set_CustomColumn("GL_BudgetControlDetail_ID", rs2.getInt("gl_budgetcontroldetail_id"));
												reql.save();
											}catch(Exception e)
											{
												log.config("no hay campo glbudgetcontroldetail");
											}
											
										}
									}
							
								}
								else
								{
									//Si no es acumulable, solo debemos buscar el disponible de la linea
									//correspondiente a la misma fecha.
							
									//mfrojas actualizacion 20200429
									//Si la solicitud es del dia 26 en adelante, se debe validar contra el mes siguiente.
									BigDecimal getDay = DB.getSQLValueBD(get_TrxName(), "SELECT extract(day from daterequired) FROM "
											+ " m_requisition WHERE m_requisition_id = ? ", req.get_ID());
									if(getDay.compareTo(new BigDecimal(25)) > 0)
									{
										//mes +1
										qtyused = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyused),0) FROM gl_budgetcontroldetail WHERE "
										+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) = extract(month from ('"+req.getDateRequired()+"'::date+'1 month'::interval)::timestamp)", gl_budgetcontrolline_id);
										qtyav = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyentered),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) = extract(month from ('"+req.getDateRequired()+"'::date+'1 month'::interval)::timestamp)", gl_budgetcontrolline_id);
								
										qtyav2 = qtyav.subtract(qtyused);

									}
									else
									{
										//mes
										qtyused = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyused),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) = extract(month from '"+req.getDateRequired()+"'::timestamp)", gl_budgetcontrolline_id);
										qtyav = DB.getSQLValueBD(get_TrxName(), "SELECT coalesce(sum(qtyentered),0) FROM gl_budgetcontroldetail WHERE "
												+ " gl_budgetcontrolline_id = ? AND isactive='Y' and extract(month from datetrx) = extract(month from '"+req.getDateRequired()+"'::timestamp)", gl_budgetcontrolline_id);
								
										qtyav2 = qtyav.subtract(qtyused);
	
									}
									/*if(qtyav2.compareTo(reql.getQty()) < 0)
									throw new AdempiereException("Error, no hay disponible para el producto "+reql.getM_Product().getName()+" Linea "+reql.getLine());
									else
									{*/
									//Si el estado nuevo es "completo", se actualiza el presupuesto. Si no, continua.
									if(newStatus.compareTo("CO") == 0)
									{
										String sqlgetdetails = null;
										if(getDay.compareTo(new BigDecimal(25)) > 0)
											sqlgetdetails = "SELECT gl_budgetcontroldetail_id from gl_budgetcontroldetail WHERE "
											+ " gl_budgetcontrolline_id = "+gl_budgetcontrolline_id+" AND "
											+ " isactive='Y' and extract(month from datetrx) = extract(month from ('"+req.getDateRequired()+"'::date+'1 month'::interval)::timestamp) and isactive='Y' ORDER BY datetrx";
										else
											sqlgetdetails = "SELECT gl_budgetcontroldetail_id from gl_budgetcontroldetail WHERE "
											+ " gl_budgetcontrolline_id = "+gl_budgetcontrolline_id+" AND "
											+ " isactive='Y' and extract(month from datetrx) = extract(month from '"+req.getDateRequired()+"'::timestamp) and isactive='Y' ORDER BY datetrx";
											
									
										int buddetail = DB.getSQLValue(get_TrxName(), sqlgetdetails);
										X_GL_BudgetControlDetail bud = new X_GL_BudgetControlDetail(getCtx(), buddetail, get_TrxName());
										BigDecimal newused = (BigDecimal)bud.get_Value("QtyUsed");
										bud.set_CustomColumn("QtyUsed", newused.add(reql.getQty()));
										bud.saveEx();
										
										//	mfrojas 20200922
										//Relacionar linea de solicitud con presupuesto.
										try
										{
											reql.set_CustomColumn("GL_BudgetControlDetail_ID", buddetail);
											reql.save();
										}catch(Exception e)
										{
											log.config("no hay campo glbudgetcontroldetail");
										}
									}
										
								}
								
							}
							
						
							
						}
					
					}	
					if(newStatus.compareTo("CO") != 0)
					{
						if(newStatus.compareTo("VO") == 0)
						{
							req.setDocStatus("VO");
							req.setProcessed(true);
						}
						else
							req.setDocStatus(newStatus);
						req.save();
					}
					else if(newStatus.compareTo("CO") == 0)
					{
						//20210310 mfrojas
						//Si el estado nuevo es "completar", se debe validar con las cantidades que existen
						//(stock actual - movimientos y consumos en proceso). 
						req.setDocStatus("IP");
						req.processIt("CO");
						req.save();
					}
						
				}
				else
					throw new AdempiereException("Error: Permisos de rol insuficientes");
				
			}
			else
				throw new AdempiereException("No existe flujo asociado al estado actual del documento");
		}
		return "Procesado";
	}	//	doIt
	
	

}
