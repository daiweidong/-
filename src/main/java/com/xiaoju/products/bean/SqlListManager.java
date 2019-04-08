package com.xiaoju.products.bean;

import java.util.ArrayList;
import java.util.List;


/**
 * 注意，这是一个单例，管理全局sqlList的对象
 * @author zdk
 */
public class SqlListManager {
	
//	public static List<String> sqlList = new ArrayList<String>();	//这个不要了吧
	
	
	/**
	 * sql的ast树结构结合，这个才是最需要的，上面那个可以不要
	 */
/*	public static List<List<SQLStatement>> insertAstList = new ArrayList<List<SQLStatement>>();
	
	public static List<List<SQLStatement>> createAstList = new ArrayList<List<SQLStatement>>();*/
	
	/**
	 * 把sqlList细化了，分为insert和create的sql集合
	 */
	public static List<String> insertSqlList = new ArrayList<String>();
	
	public static List<String> createSqlList = new ArrayList<String>();
	
	
	/**
	 * 饿汉单例
	 */
	private static SqlListManager intance  = new SqlListManager();

	public SqlListManager() {
		super();
	}

	
	public static SqlListManager getIntance() {
		return intance;
	}

//	public static void addSqlList(String sql){
//		sqlList.add(sql);
//	}
	
	public static void addInsertSqlList(String sql){
		insertSqlList.add(sql);
	}
	
	public static void addCreateSqlList(String sql){
		createSqlList.add(sql);
	}
	
/*	public static void  addInsertAstList(String sql){
		Object AstList = SqlUtil.getAst(sql);
//		if(null == AstList) AstList = new ArrayList<SQLStatement>();
		if(null != AstList){
			insertAstList.add((List<SQLStatement>) AstList);
		}
	}
	
	public static void  addCreateAstList(String sql){
		Object AstList = SqlUtil.getAst(sql);
//		if(null == sqlAstList) AstList = new ArrayList<SQLStatement>();
		if(null != AstList){
			createAstList.add((List<SQLStatement>) AstList);
		}
	}*/
}
