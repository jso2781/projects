package com.sql.parse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsIsTableDataExtractHandler
{
	public AsIsTableDataExtractHandler()
	{
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	public void getAsIsTableColumnInfo(String tableName, List<ColumnInfo> columnList) throws SQLException
	{
		StringBuffer sb = new StringBuffer();
//		sb.append("SELECT \n");
//		sb.append("      A.TABLE_NAME\n");
//		sb.append("     ,A.COLUMN_ID\n");
//		sb.append("     ,A.COLUMN_NAME\n");
//		sb.append("     ,DECODE(NULLABLE,'N','Not Null') NULLABLE\n");
//		sb.append("     ,DATA_TYPE\n");
//		sb.append("    ||'('\n");
//		sb.append("    ||CASE WHEN DATA_TYPE IN ('NUMBRER') THEN DATA_PRECISION||','||DATA_SCALE\n");
//		sb.append("           WHEN DATA_TYPE IN ('DATE') THEN ''\n");
//		sb.append("           ELSE TO_CHAR(DATA_LENGTH)\n");
//		sb.append("       END\n");
//		sb.append("    ||')' DATA_SIZE\n");
//		sb.append("     ,DATA_DEFAULT DATA_DEFAULT\n");
//		sb.append("     ,B.COMMENTS COMMENTS\n");
//		sb.append(" FROM ALL_TAB_COLUMNS A\n");
//		sb.append("     ,ALL_COL_COMMENTS B\n");
//		sb.append("WHERE A.TABLE_NAME like '"+tableName.toUpperCase()+"'\n");
//		sb.append("  AND A.TABLE_NAME = B.TABLE_NAME\n");
//		sb.append("  AND A.COLUMN_NAME= B.COLUMN_NAME\n");
//		sb.append("  AND A.OWNER = B.OWNER\n");
//		sb.append("order by A.TABLE_NAME,A.COLUMN_ID\n");

		sb.append("SELECT \n");
		sb.append("      A.TABLE_NAME\n");
		sb.append("     ,A.COLUMN_ID\n");
		sb.append("     ,A.COLUMN_NAME\n");
		sb.append("     ,DECODE(NULLABLE,'N','Not Null') NULLABLE\n");
		sb.append("     ,DATA_TYPE\n");
		sb.append("     ,DATA_DEFAULT DATA_DEFAULT\n");
		sb.append("     ,B.COMMENTS COMMENTS\n");
		sb.append(" FROM ALL_TAB_COLUMNS A\n");
		sb.append("     ,ALL_COL_COMMENTS B\n");
		sb.append("WHERE A.TABLE_NAME like '"+tableName.toUpperCase()+"'\n");
		sb.append("  AND A.TABLE_NAME = B.TABLE_NAME\n");
		sb.append("  AND A.COLUMN_NAME= B.COLUMN_NAME\n");
		sb.append("  AND A.OWNER = B.OWNER\n");
		sb.append("order by A.TABLE_NAME,A.COLUMN_ID\n");

		Connection conn = null;
		PreparedStatement psmt = null;
		try
		{
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.81:1850:PGSCM", "pt289952", "nerver2#");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.73:1850:PGSCM", "pb800866", "!!8mw7kw");	//DGSCM - TEST 
			psmt = conn.prepareStatement(sb.toString());

			ResultSet rs = psmt.executeQuery();
			
			while(rs.next())
			{
				String columnName = rs.getString("COLUMN_NAME");
				String columnTypeName = rs.getString("DATA_TYPE");

//				if(columnName.equals("COMPANY_CD"))
//				{
//					System.out.println("TABLE_NAME: "+tableName.toUpperCase()+", COLUMN NAME: "+columnName+", TYPE NAME: "+columnTypeName);
//				}

				//칼럼정보 입력
				columnList.add(new ColumnInfo(columnName,columnTypeName));
			}
		}catch(SQLException e)
		{
			throw  e;
		}finally
		{
			if(psmt != null){try{psmt.close();}catch(SQLException e){e.printStackTrace();}}
			if(conn != null){try{conn.close();}catch(SQLException e){e.printStackTrace();}}
		}
	}

	public Map<Integer,Map<String,String>> getAsIsTableData(String tableName,List<ColumnInfo> columnList,String whereStr)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT *\n");
		sb.append("FROM "+tableName+"\n");
		if(whereStr != null)
		{
			sb.append("WHERE "+whereStr+" \n");
			if(tableName.toUpperCase().equals("TB_FI_FOREIGNAMT_VALUATION"))
			{
				sb.append("		AND (COMPANY_CD,BIZ_UNIT_CD,EVALUATE_DT,ORIGIN_SLIP_NO,ACCOUNT_CD,EVE_MGMT_CD)\n");
				sb.append("		in\n");
				sb.append("		(\n");
				sb.append("		    select COMPANY_CD,BIZ_UNIT_CD,EVALUATE_DT,ORIGIN_SLIP_NO,ACCOUNT_CD,EVE_MGMT_CD \n");
				sb.append("		    from tb_fi_slipd\n");
				sb.append("		    where COMPANY_CD = 'JOPC' \n");
				sb.append("		    and EVALUATE_DT > '20151031'\n");
				sb.append("		)\n");
			}
			else if(tableName.toUpperCase().equals("TB_FI_SLIP_MGMT"))
			{
//				sb.append("		and slip_no in\n");
//				sb.append("		(\n");
//				sb.append("		    select distinct slip_no \n");
//				sb.append("		    from TB_FI_SLIPD \n");
//				sb.append("		    WHERE COMPANY_CD='CYPC' \n");
//				sb.append("		    and APPROVAL_DT >= '20140101' \n");
//				sb.append("		    and APPROVAL_DT <= '20141130' \n");
//				sb.append("		)\n");

				sb.append("		AND SLIP_NO\n");
				sb.append("		in\n");
				sb.append("		(\n");
				sb.append("		    select distinct SLIP_NO \n");
				sb.append("		    from tb_fi_slipd\n");
				sb.append("		    where company_cd = 'JOPC' \n");
//				sb.append("		    and ACCOUNT_CD = '11110100'\n");
				sb.append("		    and SLIP_DT >= '20160101'\n");
				sb.append("		)\n");
			}else if(tableName.toUpperCase().equals("TB_FI_SLIPH"))
			{
//				sb.append("		and slip_no in\n");
//				sb.append("		(\n");
//				sb.append("		    select distinct slip_no \n");
//				sb.append("		    from TB_FI_SLIPD \n");
//				sb.append("		    WHERE COMPANY_CD='CYPC' \n");
//				sb.append("		    and APPROVAL_DT >= '20140101' \n");
//				sb.append("		)\n");

				sb.append("		AND SLIP_NO\n");
				sb.append("		in\n");
				sb.append("		(\n");
				sb.append("		    select distinct SLIP_NO \n");
				sb.append("		    from tb_fi_slipd\n");
				sb.append("		    where company_cd = 'JOPC' \n");
//				sb.append("		    and ACCOUNT_CD = '11110100'\n");
				sb.append("		    and SLIP_DT >= '20160101'\n");
				sb.append("		)\n");
			}else if(tableName.toUpperCase().equals("TB_FI_SLIPD"))
			{
//				sb.append("and APPROVAL_DT >= '20140101' \n");

				sb.append("		AND SLIP_NO\n");
				sb.append("		in\n");
				sb.append("		(\n");
				sb.append("		    select distinct SLIP_NO \n");
				sb.append("		    from tb_fi_slipd\n");
				sb.append("		    where company_cd = 'JOPC' \n");
//				sb.append("		    and ACCOUNT_CD = '11110100'\n");
				sb.append("		    and SLIP_DT >= '20160101'\n");
				sb.append("		)\n");
			}else if(tableName.toUpperCase().equals("TB_FI_REVPAY_SETTLEMGMT_INFO"))
			{
				sb.append("		AND (RCP_PAY_NO like '%2015%' or RCP_PAY_NO like '%2016%')\n");
			}else if(tableName.toUpperCase().equals("TB_FI_REV_BILLCHECK"))
			{
				sb.append("		AND DRFT_CK_DEPOSIT_DT > '20160203' \n");
			}else if(tableName.toUpperCase().equals("TB_CM_USER"))
			{
				sb.append("		AND USER_ID in('110398','110379','110281','110358','110252','110337','110341','110332','110236','110354','110451','100301','110445','110428','110400','110280','110417','110437','110372','110429','110288','110404','110450','110311','110458','110350','110244','110407')");
			}else if(tableName.toUpperCase().equals("TB_CM_EMP"))
			{
				sb.append("		AND EMP_NO in('110281','110379','110398','110252','110332','110337','110341','110358','110236','110354','110451','100301','110428','110445','110280','110400','110417','110288','110311','110372','110404','110429','110437','110450','110458','110244','110350','110407')");
			}else if(tableName.toUpperCase().equals("TB_FI_PAYABLEACT_BASE"))
			{
				sb.append("		AND PAYMENT_DT >= '20151229'");
			}else if(tableName.toUpperCase().equals("TB_FI_PAYABLE_BASE"))
			{
				sb.append("		AND PAYMENT_DT >= '20160101'");
			}else if(tableName.toUpperCase().equals("TB_FI_PAYABLE_DETAIL"))
			{
				sb.append("		AND PAYMENT_DT >= '20160101'");
			}else if(tableName.toUpperCase().equals("TB_FI_REVPAY_BANKSETTLE_INFO"))
			{
				sb.append("		AND RCP_PAY_NO like '%201601%'");
			}else if(tableName.toUpperCase().equals("TB_CM_WH_PRICE"))
			{
				sb.append("		AND (COMPANY_CD, STORAGE_FEE_CALC_PTN_CD, APPLYING_DT) IN");
				sb.append("		(\n");
				sb.append("		    SELECT COMPANY_CD, STORAGE_FEE_CALC_PTN_CD, MAX(APPLYING_DT) APPLYING_DT\n");
				sb.append("		    FROM TB_CM_WH_PRICE\n");
				sb.append("		    WHERE COMPANY_CD   = 'JOPC'\n");
				sb.append("		    AND APPLYING_DT <= '201608' || '31'\n");
				sb.append("		    GROUP BY COMPANY_CD, STORAGE_FEE_CALC_PTN_CD\n");
				sb.append("		)\n");
			}else if(tableName.toUpperCase().equals("TB_ST_MATERIAL_NUMBERS"))
			{
				sb.append("        AND (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("            SELECT COMPANY_CD,ORIGIN_RAW_INV_MGMT_NO\n");
				sb.append("            FROM TB_ST_MATERIAL_NUMBERS\n");
				sb.append("            WHERE (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("                SELECT COMPANY_CD,INV_MGMT_NO\n");
				sb.append("                FROM TB_SO_SALES_DETAIL\n");
				sb.append("                WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                        SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                        WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                        AND SALES_DT >= '20170901'\n");
				sb.append("                )\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}
			else if(tableName.toUpperCase().equals("TB_ST_OWNER_CHG") || tableName.toUpperCase().equals("TB_ST_OWNER_CHG_DETAIL"))
			{
				sb.append("        AND (COMPANY_CD,OWNER_CHG_NO) IN(\n");
				sb.append("            SELECT  distinct C.COMPANY_CD, C.OWNER_CHG_NO\n");
				sb.append("              FROM  TB_ST_MATERIAL_NUMBERS A\n");
				sb.append("                  , TB_ST_OWNER_CHG_DETAIL B\n");
				sb.append("                  , TB_ST_OWNER_CHG        C\n");
				sb.append("                  , (\n");
				sb.append("                        SELECT  COMPANY_CD      \n");
				sb.append("                              , STORAGE_FEE_CALC_PTN_CD\n");
				sb.append("                              , DP_TP           \n");
				sb.append("                              , STORAGE_UOM_CD\n");
				sb.append("                              , STORAGE_UPRI    \n");
				sb.append("                              , STORAGE_FREE_TERM_CNT\n");
				sb.append("                              , STORAGE_TERM_TP\n");
				sb.append("                          FROM  TB_CM_WH_PRICE\n");
				sb.append("                         WHERE (COMPANY_CD, STORAGE_FEE_CALC_PTN_CD, APPLYING_DT) IN\n");
				sb.append("                               (SELECT  COMPANY_CD, STORAGE_FEE_CALC_PTN_CD, MAX(APPLYING_DT) APPLYING_DT\n");
				sb.append("                                  FROM  TB_CM_WH_PRICE\n");
				sb.append("                                 WHERE  COMPANY_CD   = 'JOPC'\n");
				sb.append("                                   AND  APPLYING_DT <= '201608' || '31'\n");
				sb.append("                                 GROUP  BY COMPANY_CD, STORAGE_FEE_CALC_PTN_CD \n");
				sb.append("                               )\n");
				sb.append("                    )D\n");
				sb.append("             WHERE  A.COMPANY_CD                      = B.COMPANY_CD\n");
				sb.append("               AND  A.INV_MGMT_NO                     = B.INV_MGMT_NO\n");
				sb.append("               AND  B.COMPANY_CD                      = C.COMPANY_CD\n");
				sb.append("               AND  B.OWNER_CHG_NO                    = C.OWNER_CHG_NO\n");
				sb.append("               AND  C.COMPANY_CD                      = D.COMPANY_CD              \n");
				sb.append("               AND  C.STORAGE_FEE_DEMAND_CD           = D.STORAGE_FEE_CALC_PTN_CD\n");
				sb.append("               AND  A.COMPANY_CD                      = 'JOPC'\n");
				sb.append("               AND  NVL(A.OUT_STOCK_DT, '99991231')  >= '201608' || '01'\n");
				sb.append("               AND  C.OWNER_CHG_DT                   <= '201608' || '31'\n");
				sb.append("            )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_DEMAND"))
			{
				sb.append("        AND DEMAND_DT >= '20170901'\n");
			}else if(tableName.toUpperCase().equals("TB_SO_DEMAND_DETAIL"))
			{
				sb.append("        AND (company_cd,demand_no) IN(\n");
				sb.append("        select company_cd,demand_no from TB_SO_DEMAND\n");
				sb.append("        where company_cd = 'AAPC'\n");
				sb.append("        AND DEMAND_DT >= '20170901'\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_SALES"))
			{
				sb.append("        AND sales_dt >= '20170901'\n");
			}else if(tableName.toUpperCase().equals("TB_SO_SALES_DETAIL"))
			{
				sb.append("        AND (company_cd,sales_no) IN(\n");
				sb.append("        select company_cd,sales_no from TB_SO_SALES\n");
				sb.append("        where company_cd = 'AAPC'\n");
				sb.append("        AND sales_dt >= '20170901'\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_ORDER"))
			{
				sb.append("        AND (COMPANY_CD,ORD_NO) IN(\n");
				sb.append("            SELECT DISTINCT COMPANY_CD,ORD_NO\n");
				sb.append("            FROM TB_SO_SALES_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                    SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                    WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                    AND SALES_DT >= '20170901'\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_ORD_DETAIL"))
			{
				sb.append("        AND (COMPANY_CD,ORD_NO,ORD_NO_SEQ) IN(\n");
				sb.append("            SELECT DISTINCT COMPANY_CD,ORD_NO,ORD_NO_SEQ\n");
				sb.append("            FROM TB_SO_SALES_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                    SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                    WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                    AND SALES_DT >= '20170901'\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_SHIPMENT"))
			{
				sb.append("        AND (COMPANY_CD,DLV_ORD_NO) IN(\n");
				sb.append("            SELECT DISTINCT COMPANY_CD,DLV_ORD_NO\n");
				sb.append("            FROM TB_SO_SHIPMENT_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("                SELECT COMPANY_CD,INV_MGMT_NO\n");
				sb.append("                FROM TB_SO_SALES_DETAIL\n");
				sb.append("                WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                        SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                        WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                        AND SALES_DT >= '20170901'\n");
				sb.append("                )\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_SHIPMENT_DETAIL"))
			{
				sb.append("        AND (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("            SELECT COMPANY_CD,INV_MGMT_NO\n");
				sb.append("            FROM TB_SO_SALES_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                    SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                    WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                    AND SALES_DT >= '20170901'\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_CAR_ALLOCATION"))
			{
				sb.append("        AND (COMPANY_CD,TRANS_NO) IN(\n");
				sb.append("            SELECT DISTINCT COMPANY_CD,TRANS_NO\n");
				sb.append("            FROM TB_SO_SHIPMENT_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("                SELECT COMPANY_CD,INV_MGMT_NO\n");
				sb.append("                FROM TB_SO_SALES_DETAIL\n");
				sb.append("                WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                        SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                        WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                        AND SALES_DT >= '20170901'\n");
				sb.append("                )\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_PO_MAKER_PLAN_INFO"))
			{
				sb.append("        AND (COMPANY_CD,PROD_NO) IN(\n");
				sb.append("            SELECT DISTINCT COMPANY_CD,INV_NO\n");
				sb.append("            FROM TB_ST_MATERIAL_NUMBERS\n");
				sb.append("            WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("            AND STOCK_DT >= '20170901'\n");
				sb.append("            AND INV_PROG_CD = 'OH'\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_RTN"))
			{
				sb.append("        AND (COMPANY_CD,RTN_RCV_NO) IN(\n");
				sb.append("            SELECT COMPANY_CD,RTN_RCV_NO\n");
				sb.append("            FROM TB_SO_RTN_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO,SALES_NO_SEQ) IN(\n");
				sb.append("                SELECT COMPANY_CD,SALES_NO,SALES_NO_SEQ\n");
				sb.append("                FROM TB_SO_SALES_DETAIL\n");
				sb.append("                WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                        SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                        WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                        AND SALES_DT >= '20170901'\n");
				sb.append("                )\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_RTN_DETAIL"))
			{
				sb.append("        AND (COMPANY_CD,SALES_NO,SALES_NO_SEQ) IN(\n");
				sb.append("            SELECT COMPANY_CD,SALES_NO,SALES_NO_SEQ\n");
				sb.append("            FROM TB_SO_SALES_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                    SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                    WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                    AND SALES_DT >= '20170901'\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_SO_RTN_RESULT"))
			{
				sb.append("        AND (COMPANY_CD,RTN_RCV_NO) IN(\n");
				sb.append("            SELECT COMPANY_CD,RTN_RCV_NO\n");
				sb.append("            FROM TB_SO_RTN_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO,SALES_NO_SEQ) IN(\n");
				sb.append("                SELECT COMPANY_CD,SALES_NO,SALES_NO_SEQ\n");
				sb.append("                FROM TB_SO_SALES_DETAIL\n");
				sb.append("                WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                        SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                        WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                        AND SALES_DT >= '20170901'\n");
				sb.append("                )\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_ST_INV_COST"))
			{
				sb.append("        AND (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("            SELECT COMPANY_CD,INV_MGMT_NO\n");
				sb.append("            FROM TB_SO_SALES_DETAIL\n");
				sb.append("            WHERE (COMPANY_CD,SALES_NO) IN(\n");
				sb.append("                    SELECT COMPANY_CD,SALES_NO FROM TB_SO_SALES\n");
				sb.append("                    WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("                    AND SALES_DT >= '20170901'\n");
				sb.append("            )\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_PO_WH_INPUT"))
			{
				sb.append("        AND (COMPANY_CD,INV_MGMT_NO) IN(\n");
				sb.append("            SELECT COMPANY_CD,INV_MGMT_NO\n");
				sb.append("            FROM TB_ST_MATERIAL_NUMBERS\n");
				sb.append("            WHERE COMPANY_CD = 'AAPC'\n");
				sb.append("            AND STOCK_DT >= '20170901'\n");
				sb.append("            AND INV_PROG_CD = 'OH'\n");
				sb.append("        )\n");
			}else if(tableName.toUpperCase().equals("TB_CM_SHIPMENT"))
			{
				
			}
			else
			{
				sb.append("		order by ROWID desc");
				
			}
		}
		

		Map<Integer,Map<String,String>> tableDataMap = new HashMap<Integer,Map<String,String>>();
		Connection conn = null;
		PreparedStatement psmt = null;
		try
		{
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.73:1850:PGSCM", "pb800866", "!!8mw7kw");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.83:1750:DGSCM2", "POSSCM", "posco123");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.81:1850:PGSCM", "pt289952", "nerver2#");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "study", "study123");
			psmt = conn.prepareStatement(sb.toString());

			System.out.println("=============================================="+tableName+" 데이버 가져오기 시작 : ==============================================\n");
			System.out.println("실행 쿼리 : \n"+sb.toString()+"\n");

			ResultSet rs = psmt.executeQuery();

			int rowIdx = 0;
			while(rs.next())
			{
				int columnListCount = columnList.size();
				Map<String,String> recordDataMap = new HashMap<String, String>();

				for(int i=0;i<columnListCount;i++)
				{
					ColumnInfo ci = columnList.get(i);
					String columnName = ci.getColumnName();
					String columnValue = rs.getString(columnName);

					//레코드 내 칼럼값 입력
					recordDataMap.put(columnName, columnValue);
				}

				//레코드 입력
				tableDataMap.put(rowIdx, recordDataMap);

				rowIdx++;
			}
			System.out.println("데이터 건수 : "+tableDataMap.size());
			System.out.println("=============================================="+tableName+" 데이버 가져오기 끝 : ==============================================");
		}catch(SQLException e)
		{
			e.printStackTrace();
			if(psmt != null){try{psmt.close();}catch(SQLException se){se.printStackTrace();}}
			if(conn != null){try{conn.close();}catch(SQLException se){se.printStackTrace();}}
			return getAsIsTableData(tableName,columnList,null);
		}finally
		{
			if(psmt != null){try{psmt.close();}catch(SQLException e){e.printStackTrace();}}
			if(conn != null){try{conn.close();}catch(SQLException e){e.printStackTrace();}}
		}

		return tableDataMap;
	}
}
