package com.xiaoju.products.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xiaoju.products.bean.ColLine;
import com.xiaoju.products.bean.ColumnNode;
import com.xiaoju.products.bean.SQLResult;
import com.xiaoju.products.bean.TableNode;
import com.xiaoju.products.exception.DBException;
import com.xiaoju.products.util.Check;
import com.xiaoju.products.util.DBUtil;
import com.xiaoju.products.util.DBUtil.DB_TYPE;

/**
 * 元数据dao
 * @author yangyangthomas
 */
public class MetaDataDao {
	DBUtil dbUtil = new DBUtil(DB_TYPE.META);
	
	
	public List<ColumnNode> getColumn(String db, String table){
    	String sqlWhere  = "is_effective=1 and data_name='" + table + "'" + (Check.isEmpty(db) ? " " : (" and datastorage_name='"+db+"'"));
    	List<ColumnNode> colList = new ArrayList<ColumnNode>();
    	String sql = "SELECT rc.column_id,rc.column_name,rd.data_id,rd.data_name,rd.datastorage_name FROM r_data_column rc join " +
    			"(SELECT data_id,data_name,datastorage_name from r_data where " + sqlWhere + ") rd " +
    			"on rc.data_id=rd.data_id ORDER BY rc.column_position";
    	System.out.println("============"+sql);
		try {
			List<Map<String, Object>> rs = dbUtil.doSelect(sql);
			for (Map<String, Object> map : rs) {
				ColumnNode column = new ColumnNode();
				column.setId( Integer.valueOf(map.get("column_id").toString()).longValue());
				column.setColumn((String) map.get("column_name"));
				column.setTableId(Integer.valueOf(map.get("data_id").toString()).longValue());
				column.setTable((String) map.get("data_name"));
				column.setDb((String) map.get("datastorage_name"));
				colList.add(column);
			}
	    	return colList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException(sqlWhere, e);
		}
    }
	
	public List<TableNode> getTable(String db, String table){
    	String sqlWhere  = "is_effective=1 and data_name='" + table + "'" + (Check.isEmpty(db) ? " " : (" and datastorage_name='"+db+"'"));
    	List<TableNode> list = new ArrayList<TableNode>();
    	String sql = "SELECT data_id,data_name,datastorage_name from r_data where " + sqlWhere + "";
    	System.out.println("----------"+sql);
		try {
			List<Map<String, Object>> rs = dbUtil.doSelect(sql);
			for (Map<String, Object> map : rs) {
				TableNode tableNode = new TableNode();
				tableNode.setId((Long) map.get("data_id"));
				tableNode.setTable((String) map.get("data_name"));
				tableNode.setDb((String) map.get("datastorage_name"));
				list.add(tableNode);
			}
	    	return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException(sqlWhere, e);
		}
    }
	
	public void insert(String username,List<SQLResult> srList){
		String[] sts = username.split(",");
		String hiveSqlName = sts[0];
		String useUser = sts[1];
		for(int i=0;i<srList.size();i++){
			SQLResult st = new SQLResult();
			st=srList.get(i);
			//获取来目标用户，目标表
			Set<String> ss1 = st.getOutputTables();
			//System.out.println("解析出来的输出表="+ss1.toString());
			String user = "";
			String table = "";
			for (String str : ss1) {
			   String[]  strs = str.split("\\.");
			   if(strs.length!=0&&strs.length==2){
				   user = strs[0];
				   if(user.equals("default")){
					   user=useUser;
				   }
				   table = strs[1];
			   }
			} 
			//获取来源用户，来源表
			Set<String> ss = st.getInputTables();
			//System.out.println("解析出来的输入表="+ss.toString());
			for (String str : ss) {
				String fromUser = "";
				String fromTable = "";
				   String[]  strs = str.split("\\.");
				   if(strs.length!=0&&strs.length==2){
					   fromUser = strs[0];
					   fromTable = strs[1];
					   String sql = "insert into HiveTableKin(to_user,to_table,from_user,from_table,script_name) value('"+user+"','"+table+"','"+fromUser+"','"+fromTable+"','"+hiveSqlName+"')";
					   try {
						dbUtil.doInsert(sql);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   }if(strs.length!=0&&strs.length==1){
					   fromUser = strs[0];
					   fromTable = strs[1];
					   String sql = "insert into HiveTableKin(to_user,to_table,from_user,from_table) value('"+user+"','"+table+"','"+fromUser+"','"+fromTable+"','"+hiveSqlName+"')";
					   try {
						dbUtil.doInsert(sql);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   }
				} 
			
			//表血缘关系入库
			/*toNameParse(解析sql出来的列名称)=FREEZE_STATUS
			  colCondition(带条件的源字段)=MPS.DCM_MPS_LAUSERINFO_S.FREEZE_STATUS
			  fromNameSet(源字段)=[MPS.DCM_MPS_LAUSERINFO_S.FREEZE_STATUS]
			  conditionSet(计算字段)=[LEFTOUTERJOIN:TRIM(MPS.DCM_MPS_LAUSERINFO_S.COL_1) = SUM.C01_ORG_COMP_PARA.c01002_Org_Num, WHERE:AAM.DCM_AAM_EAAM_TELLER_INFO_S.pt_dt = '2019-01-31', WHERE:(SUM.C01_ORG_COMP_PARA.pt_dt = '2019-01-31' AND SUM.C01_ORG_COMP_PARA.c01002_Comp_Type_Cd = '001'), LEFTOUTERJOIN:MPS.DCM_MPS_LAUSERINFO_S.ID = AAM.DCM_AAM_EAAM_TELLER_INFO_S.TELLERNO, WHERE:MPS.DCM_MPS_LAUSERINFO_S.pt_dt = '2019-01-31']
			  allConditionSet(所有计算字段)=[LEFTOUTERJOIN:TRIM(MPS.DCM_MPS_LAUSERINFO_S.COL_1) = SUM.C01_ORG_COMP_PARA.c01002_Org_Num, WHERE:AAM.DCM_AAM_EAAM_TELLER_INFO_S.pt_dt = '2019-01-31', WHERE:(SUM.C01_ORG_COMP_PARA.pt_dt = '2019-01-31' AND SUM.C01_ORG_COMP_PARA.c01002_Comp_Type_Cd = '001'), LEFTOUTERJOIN:MPS.DCM_MPS_LAUSERINFO_S.ID = AAM.DCM_AAM_EAAM_TELLER_INFO_S.TELLERNO, WHERE:MPS.DCM_MPS_LAUSERINFO_S.pt_dt = '2019-01-31']
			  toTable(解析出来输出表)=default.C01_TELLER_INFO
			  toName(查询元数据出来的列名称)=null*/
			
			
			//获取字段去向及转换
			List<ColLine> listColl = st.getColLineList();
			for(int z=0;z<listColl.size();z++){
				ColLine cl = listColl.get(z);
				
				String from_user = ""; //来源用户
				String from_table = ""; //来源表名
				String from_column = ""; //来源字段名
				String to_user = ""; //写入用户
				String to_table = ""; //写入表名
				
				Set<String> colLine = cl.getFromNameSet();
				for (String str : colLine) {
					String[]  strs = str.split("\\.");
					 if(strs.length!=0&&strs.length==3){
						 from_user = strs[0];
						 from_table = strs[1];
						 from_column = strs[2];
					 }
					
				}
				String tousertable = cl.getToTable();
				String[]  strs = tousertable.split("\\.");
				if(strs.length!=0&&strs.length==2){
					to_user = strs[0];
					if(to_user.equals("default")){
						to_user=useUser;
					   }
					to_table = strs[1];
				}
				
				//System.out.println(cl.getToName());
				String str = null;
				if(cl.getToName()!=null){
				 str = cl.getToName().substring(cl.getToName().lastIndexOf('.')+1,cl.getToName().length());
				}
						
				 String sql = "insert into hivecolumnkin (fromUser,fromName,fromNameColumn,conditions,allCondition,toNameParse,totable,toname,touser,hivesqlname) "
				 		+ "value('"+from_user+"','"+from_table+"','"+from_column+"','','','"+cl.getToNameParse()+"'"
				 				+ ",'"+to_table+"','"+str+"','"+to_user+"','"+hiveSqlName+"')";
				 //System.out.println("======================="+sql);
				 try {
					 //System.out.println(sql);
					dbUtil.doInsert(sql);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			
		/*	System.out.println("************************************************开始获取字段去向及转换循环*****************************************************");
			for(int z=0;z<listColl.size();z++){
				ColLine cl = listColl.get(z);
				System.out.println("toNameParse(解析sql出来的列名称)="+cl.getToNameParse());
				System.out.println("colCondition(带条件的源字段)="+cl.getColCondition());
				System.out.println("fromNameSet(源字段)="+cl.getFromNameSet().toString());
				System.out.println("conditionSet(计算字段)="+cl.getConditionSet().toString());
				System.out.println("allConditionSet(所有计算字段)="+cl.getAllConditionSet());
				System.out.println("toTable(解析出来输出表)="+cl.getToTable());
				System.out.println("toName(查询元数据出来的列名称)="+cl.getToName());
				System.out.println("************************************************获取字段去向及转换循环"+z+"次*****************************************************");
			}*/
			
		}
		
    }
}
