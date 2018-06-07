package com.sql.parse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToBeTableDataHandler
{
	public void getToBeTableColumnInfo(String tableName, List<ColumnInfo> columnList)
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
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.81:1850:PGSCM", "PT289952", "sonata8*");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@10.132.32.52:1751:DOERP", "POSSCM_NEW", "new#scm");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.74:1550:DGSCM", "POSSCM", "dgscm1");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "study", "study123");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.16.200.62:1541:DEV", "poserp", "erp123");
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
			e.printStackTrace();
		}finally
		{
			if(psmt != null){try{psmt.close();}catch(SQLException e){e.printStackTrace();}}
			if(conn != null){try{conn.close();}catch(SQLException e){e.printStackTrace();}}
		}
	}

	

	/**
	 * AS-IS와 TO-BE 데이블 데이터간 칼럼을 비교한 뒤 동일한 칼럼명을 기준으로 
	 * 입력문(INSERT) SQL를 작성한뒤 마이그레이션 처리
	 * @param tableName
	 * @param toBeColumnList
	 * @param asIsColumnList
	 * @param asIsTableDataMap
	 * @return
	 */
	public void setToBeMigration(String tableName, List<ColumnInfo> toBeColumnList, List<ColumnInfo> asIsColumnList, Map<Integer,Map<String,String>> asIsTableDataMap)
	{
//		StringBuffer plsqlBuffer = new StringBuffer();
		StringBuffer insertSqlPrefixBuffer = new StringBuffer();
		StringBuffer insertSqlBuffer = new StringBuffer();
//		String currentPath = this.getClass().getResource("").getPath();
//		Path createdInsertStringFilePath = new File(currentPath+"createdInsertString.sql").toPath();

//		String hibernateKeyName = tableName.substring(3) + "_KEY";
//		String sequenceName = "SQ_"+hibernateKeyName;

		//식별자가 길이가 길어서 sequence 명을 축약한 경우
//		if(sequenceName.toUpperCase().equals("SQ_FI_REVPAY_SETTLEMGMT_INFO_KEY"))
//		{
//			sequenceName = "SQ_FI_REVPAY_SETTLE_INFO_KEY";
//		}else if(sequenceName.toUpperCase().equals("SQ_FI_REVPAY_BANKSETTLE_INFO_KEY"))
//		{
//			sequenceName = "SQ_FI_REVPAY_BANKSTTL_INFO_KEY";
//		}

		Connection conn = null;
		PreparedStatement psmt = null;
		CallableStatement cs = null;
		try
		{
			String insertPrefix = null;
			String insertSql = null;
			List<String> insertColumnNameList = new ArrayList<String>();

			int asIsColumnListCount = asIsColumnList.size();
			int toBeColumnListCount = toBeColumnList.size();

			for(int i=0;i<asIsColumnListCount;i++)
			{
				ColumnInfo asIsColumnInfo = asIsColumnList.get(i);
				String asIsColumnName = asIsColumnInfo.getColumnName();

				for(int j=0;j<toBeColumnListCount;j++)
				{
					ColumnInfo toBeColumnInfo = toBeColumnList.get(j);
					String tobeColumnName = toBeColumnInfo.getColumnName();
					if(asIsColumnName.equals(tobeColumnName))
					{
						insertColumnNameList.add(asIsColumnName);
					}
				}
			}

			int insertColumnNameListCount = insertColumnNameList.size();
			if(insertColumnNameListCount > 0)
			{
				insertSqlPrefixBuffer.append("INSERT INTO "+tableName+"(");
				for(int i=0;i<insertColumnNameListCount;i++)
				{
					String insertColumnName = insertColumnNameList.get(i);
//					if(i != (insertColumnNameListCount-1))
//					{
						insertSqlPrefixBuffer.append(insertColumnName);
//					}
//					else
//					{
//						insertSqlPrefixBuffer.append(insertColumnName+","+hibernateKeyName+")");
//					}

					if(i != (insertColumnNameListCount-1))
					{
						insertSqlPrefixBuffer.append(",");
					}
				}

				insertSqlPrefixBuffer.append(") VALUES(");

				insertPrefix = insertSqlPrefixBuffer.toString();

				try
				{
//					conn = DriverManager.getConnection("jdbc:oracle:thin:@172.16.200.62:1541:DEV", "poserp", "erp123");
					conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.74:1550:DGSCM", "POSSCM", "dgscm1");
				}catch (SQLException e)
				{
					System.out.println("TO-BE 접속실패!!!");
					return;
				}
//
//				plsqlBuffer.append("declare \n");
//				plsqlBuffer.append("    v_current_key_num number(16); \n");
//				plsqlBuffer.append("    v_next_key_num number(16); \n");
//				plsqlBuffer.append("    v_key_num_gap number(16); \n");
//				plsqlBuffer.append("begin \n");
//				plsqlBuffer.append("    begin \n");
//				plsqlBuffer.append("        select NVL(MAX("+hibernateKeyName+"),0) into v_current_key_num from "+tableName+"; \n");
//				plsqlBuffer.append("    end; \n");
//				plsqlBuffer.append(" \n");
//				plsqlBuffer.append("    begin \n");
//				plsqlBuffer.append("        select NVL("+sequenceName+".nextval,0) into v_next_key_num from dual; \n");
//				plsqlBuffer.append("    end; \n");
//				plsqlBuffer.append(" \n");
//				plsqlBuffer.append("    if v_current_key_num > v_next_key_num then \n");
//				plsqlBuffer.append("    begin \n");
//				plsqlBuffer.append("        v_key_num_gap := v_current_key_num - v_next_key_num; \n");
//				plsqlBuffer.append("        for n in 0..v_key_num_gap loop \n");
//				plsqlBuffer.append("        begin \n");
//				plsqlBuffer.append("            select "+sequenceName+".nextval into ? from dual; \n");
//				plsqlBuffer.append("        end; \n");
//				plsqlBuffer.append("        end loop; \n");
//				plsqlBuffer.append("    end; \n");
//				plsqlBuffer.append("    end if; \n");
//				plsqlBuffer.append("end; \n");
//
//				System.out.println("============================================== "+sequenceName+" 시쿼스 동기화 시작 ==============================================\n");
//
//
//				try
//				{
//					cs = conn.prepareCall(plsqlBuffer.toString());
////					cs.setString(1, hibernateKeyName);
////					cs.setString(2, tableName);
////					cs.setString(3, sequenceName);
////					cs.setString(4, sequenceName);
////			        cs.registerOutParameter(5, OracleTypes.NUMBER);
//
//					cs.registerOutParameter(1, OracleTypes.NUMBER);
//
//					cs.execute();
//
//					System.out.println("현재 시퀀스 번호 : "+sequenceName+" = " + cs.getLong(1));
//
//					conn.commit();
//
//					cs.close();
//				}
//				catch(SQLException e)
//				{
//					System.out.println("Error Msg : "+e.getMessage()+ " : "+insertSql);
//				}finally
//				{
//					if(cs != null){try{cs.close();}catch(SQLException e){e.printStackTrace();}}
//				}

//				System.out.println("============================================== "+sequenceName+" 시쿼스 동기화 끝 ==============================================\n");
				System.out.println("=============================================="+tableName+" 데이버 입력 시작 : ==============================================\n");
				System.out.println("진행중...................");

				//입력할 레코드 단위로 INSERT문 생성

				int asIsTableDataMapCount = asIsTableDataMap.size();
				for(int i=0;i<asIsTableDataMapCount;i++)
				{
					Map<String,String> recordColumnMap = asIsTableDataMap.get(i);
					for(int j=0;j<insertColumnNameListCount;j++)
					{
						String insertColumnName = insertColumnNameList.get(j);
						String insertColumnValue = recordColumnMap.get(insertColumnName) == null ? "NULL" : recordColumnMap.get(insertColumnName) ;

						if(insertColumnValue.equals("NULL"))
						{
							insertSqlBuffer.append(insertColumnValue);
						}else if(insertColumnName.equals("COMPANY_CD"))
						{
							insertSqlBuffer.append("'POSAM'");
						}else
						{
							//CREATION_TIMESTAMP,CREATION_LOCAL_TIMESTAMP
							//LAST_UPDATE_TIMESTAMP,LAST_UPDATE_LOCAL_TIMESTAMP
							if(    insertColumnName.equals("CREATION_TIMESTAMP")
								||insertColumnName.equals("CREATION_LOCAL_TIMESTAMP")
								||insertColumnName.equals("LAST_UPDATE_TIMESTAMP")
								||insertColumnName.equals("LAST_UPDATE_LOCAL_TIMESTAMP")
								||insertColumnName.equals("DATA_END_TIMESTAMP")
								||insertColumnName.equals("ARCHIVED_TIMESTAMP")
								||insertColumnName.equals("INV_PROG_CHG_DT")
								||insertColumnName.equals("INV_ST_CHG_DT")
								||insertColumnName.equals("INV_END_TIMESTAMP")
							)
							{
								insertSqlBuffer.append("to_timestamp('"+insertColumnValue.replaceAll("-", "/")+"'"+",'yyyy/mm/dd hh24:mi:ss.ff')");
							}else
							{
								insertSqlBuffer.append("'"+insertColumnValue.replaceAll("'", "'||CHR(39)||'").replaceAll("&","'||CHR(38)||'").replaceAll(",","'||CHR(44)||'")+"'");
							}
							
						}

						if(j != (insertColumnNameListCount-1))
						{
							insertSqlBuffer.append(",");
						}
					}

//					insertSqlBuffer.append("(SELECT NVL(MAX("+hibernateKeyName+")+1,0) FROM "+tableName+"))");
//					insertSqlBuffer.append(sequenceName+".nextval)");
					insertSqlBuffer.append(")\n");
					insertSql = insertPrefix + insertSqlBuffer.toString();

					insertSqlBuffer.setLength(0);

//					System.out.println(insertSql);
//					System.out.println("입력문 : "+insertSql+"\n");

					try
					{
						psmt = conn.prepareStatement(insertSql);

						psmt.executeUpdate();

						conn.commit();

						psmt.close();
					}
					catch(SQLException e)
					{
						System.out.println((i+1)+"번째 건 Error Msg : "+e.getMessage()+ " : "+insertSql);
					}finally
					{
						if(psmt != null){try{psmt.close();}catch(SQLException e){e.printStackTrace();}}
					}
				}
//				byte[] insertSqlBytes = insertSqlBuffer.toString().getBytes();
//				System.out.println("insertSqlBytes Count : "+insertSqlBytes.length);
//				Files.write(createdInsertStringFilePath, insertSqlBuffer.toString().getBytes(), StandardOpenOption.APPEND);
				
				System.out.println("=============================================="+tableName+" 데이버 입력 끝 ==============================================\n");
			}
		}finally
		{
			if(cs != null){try{cs.close();}catch(SQLException e){e.printStackTrace();}}
			if(psmt != null){try{psmt.close();}catch(SQLException e){e.printStackTrace();}}
			if(conn != null){try{conn.close();}catch(SQLException e){e.printStackTrace();}}
		}
	}
}
