package com.xiaoju.products.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xiaoju.products.bean.SQLResult;
import com.xiaoju.products.bean.SqlListManager;
import com.xiaoju.products.parse.LineParser;


/**
 * 从日志中提取 sql 的工具类
 * @author zdk
 */
public class LogUtil {

	/**
	 * 动作
	 */
	private static final String SELECT = "SELECT";
	private static final String UPDATE = "UPDATE";
	private static final String DELETE = "DELETE";
	private static final String INSERT = "INSERT";
	private static final String CREATE = "CREATE";
	private static final String ALL = "ALL";
	
	/**
	 * 编码：UTF-8
	 */
	private static final String UTF8 = "UTF-8";
	
	private static final String ZHUSHI = "--";
	
	private static final String LOGSUFFIX = ".log";

	/**
	 * 截取的sql必定是exec sql: 和 result:0之间的
	 */
	public static final String STARTTEXT = "exec sql:";
	
	public static final String NOTHAVE ="exec sql:set";
	
	public static final String ENDTEXT = "result:0";
	
	public static final String FILTER="udf.rank_udf";
	public static final String FILTER1="udf.dec2str";
	public static final String FILTER2="udf.transAcctNum";
	
	public static final String LEFT="LEFT";
	
	/**
	 * 单例的sql集合管理对象
	 */
	private static SqlListManager sqlListManager = new SqlListManager();
	
/*	public static void main(String[] args) {
		//String[] params = {"E:\\A-工作区域\\2019-即刻数据\\脚本"};
		String params = "E:\\A-工作区域\\2019-即刻数据\\脚本";
		new LogUtil().parse(params);
	}*/

	/**
	 * 这个方法可以废弃
	 * @param path
	 */
	@Deprecated
	public static void parse(String path) {
		LineParser parse = new LineParser();
		new PropertyFileUtil().init();
		if (StringUtils.isBlank(path)) {
			System.out.println("请传入需要 解析的文件目录路径！");
			return;
		}
		BufferedReader buffer_reader = null;
			
		try {
			File file = new File(path);
			if(!file.isDirectory()){
				//解析入库
				List<Map<String, String>>  list = getSqlFromLog(file);
				for(int i=0;i<list.size();i++){
					Map<String, String> map = list.get(i);
					 for (String key : map.keySet()) {
				            System.out.println("key= " + key + " and value= " + map.get(key));
				            parse.parse(key,map.get(key));	
				        }

				}
			}else{
				File[] filesOfDirs = file.listFiles();
				for(int i = 0; i < filesOfDirs.length; i++){
					//解析入库
					List<Map<String, String>>  list = getSqlFromLog(filesOfDirs[i]);
					for(int j=0;j<list.size();j++){
						Map<String, String> map = list.get(j);
						 for (String key : map.keySet()) {
					            System.out.println("key= " + key + " and value= " + map.get(key));
					            parse.parse(key,map.get(key));	
					        }

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (buffer_reader != null) {
				try {
					buffer_reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	/**
	 * 抽出来单独解析文件的
	 * @param logPath
	 */
	public static List<Map<String, String>> getSqlFromLog(File file) {
		BufferedReader buffer_reader = null;
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();  
		try {
			boolean subSql = false; //exec sql: 是否开始截取sql
			boolean subSql1 = false; //INSERT开头是否开始截取
			StringBuffer curSql = new StringBuffer();
			String user = "default"; //存放SQL执行数据库
//			File tmp_File = file.getAbsoluteFile();
//			buffer_reader = new BufferedReader(new FileReader(tmp_File));
			buffer_reader = new BufferedReader(new FileReader(file));
			String temp_read = null;
			//当前sql的字符串
			while ((temp_read = buffer_reader.readLine()) != null) {
				if(null == temp_read ) temp_read = "";
				//读取以 exec sql: 开头的，不以exec sql:set开头的，包含result:0的，解析为用户
				if(temp_read.contains(STARTTEXT)&&!temp_read.contains(NOTHAVE)&&temp_read.contains(ENDTEXT)){
					String s = new String(temp_read.substring( temp_read.indexOf(STARTTEXT) + STARTTEXT.length()).getBytes());
					String t = s.toUpperCase().replace("USE", "");
					String tt = t.toUpperCase().replace("RESULT:0", "");
					user = tt.trim();
				//读取以 exec sql: 开头的，不以exec sql:set开头的,解析为脚本
				}else if( temp_read.contains(STARTTEXT)&&!temp_read.contains(NOTHAVE)){
					String s = new String(temp_read.substring( temp_read.indexOf(STARTTEXT) + STARTTEXT.length()).getBytes());
					subSql = true;
					if(!s.trim().equals("")){
						curSql.append(deletezj(s));
					}
				}else if(!temp_read.contains(ENDTEXT)&&subSql){
					if(!temp_read.trim().equals("")){
						curSql.append(deletezj(temp_read).replace(FILTER, "").replace(FILTER1, "").replace(FILTER2, ""));
					}
				}else if(temp_read.contains(ENDTEXT)&&subSql){
					
					String s = temp_read.replace("result:0", "");
					curSql.append(deletezj(s).replace(FILTER, "").replace(FILTER1, "").replace(FILTER2, ""));
					Map<String, String> map = new HashMap<String, String>(); 
					map.put(file.getName()+","+user, curSql.toString());
					//sqlList.add(curSql.toString());
					list.add(map);
					curSql.setLength(0);
					subSql = false;
				}
				
				
			}
			buffer_reader.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer_reader != null) {
				try {
					buffer_reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		return list;

	}
	
	

	/**
	 * 是不是日志文件啊
	 * @param fileName
	 * @return
	 */
	public static boolean isLog(String fileName) {
		if(fileName.endsWith(LOGSUFFIX)) return true;
		return false;
	}
	
	/**
	 * 删除每行里的  -- 
	 * @param s
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String deletezj(String ss) throws UnsupportedEncodingException{
		String ls = ss.replace(" ", "");
		if(ls.toUpperCase().contains("LEFTJOIN")){
			ss = ss.toUpperCase().replace(LEFT, "INNER");
		}
		if(ss.contains("--")){
			return  new String(ss.substring(0,ss.indexOf("--")).getBytes());
		}else{
			return ss;
		}
	}
}