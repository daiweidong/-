package com.xiaoju.products;

import junit.framework.TestCase;

import com.xiaoju.products.util.LogUtil;

public class LineParserTest1 extends TestCase {

	public void testParseUnion() throws Exception {

		// 脚本目录
		String params = "E:\\A-工作区域\\2019-QUICKDATA\\脚本";
		new LogUtil().parse(params);

	}
}