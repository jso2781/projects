package com.data.migration.ui.main.worker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.data.migration.config.model.MigrationDataInfo;
import com.data.migration.ui.main.AsIsDataMigrator;
import com.data.migration.ui.progress.ProcProgressBarHandler;

public class ToBeInsertProc implements Runnable
{
	private AsIsDataMigrator asIsDataMigrator = null;
	private ProcDataTransfer procDataTransfer = null;
	private List<MigrationDataInfo> procMigrationDataInfoList = null;
	private StringBuffer insertColumnsPrefixBuffer = new StringBuffer();
	private StringBuffer insertValuesBuffer = new StringBuffer();
	private StringBuffer insertStringBuffer = new StringBuffer();

	public ToBeInsertProc(AsIsDataMigrator asIsDataMigrator,ProcDataTransfer procDataTransfer,List<MigrationDataInfo> procMigrationDataInfoList)
	{
		this.asIsDataMigrator = asIsDataMigrator;
		this.procDataTransfer = procDataTransfer;
		this.procMigrationDataInfoList = procMigrationDataInfoList;
	}

	@Override
	public void run()
	{
		boolean isBatchYn = asIsDataMigrator.getChkBatchYn().isSelected() ? true : false;

//		Configuration configuration = asIsDataMigrator.getToBeSqlSessionFactory().getConfiguration();
		SqlSession toBeSqlSession = null;
		try
		{
			List<String> insertColumnNameList = new ArrayList<String>();
			Map<String,Object> insertParamMap = new HashMap<String,Object>();
			int procMigrationDataInfoListCount = procMigrationDataInfoList.size();

			for(int i=0;i<procMigrationDataInfoListCount;i++)
			{
				insertColumnsPrefixBuffer.setLength(0);
				insertValuesBuffer.setLength(0);
				insertStringBuffer.setLength(0);
				insertColumnNameList.clear();
				insertParamMap.clear();

				MigrationDataInfo procMdi = procMigrationDataInfoList.get(i);
				String asIsTableName = procMdi.getAsIsTableName();
				String toBeTableName = procMdi.getToBeTableName();
				Map<String, Object> modifyColumnValuesMap = procMdi.getModifyColumnValuesMap();
				List<Map<String,Object>> asIsTableDataList = procMdi.getAsIsTableDataList();
				List<String> asIsColumnNameList = procMdi.getAsIsColumnNameList();
				List<String> toBeColumnNameList = procMdi.getToBeColumnNameList();
				int asIsColumnListCount = asIsColumnNameList.size();
				int toBeColumnListCount = toBeColumnNameList.size();
				int asIsTableDataListCount = asIsTableDataList.size();

				for(int j=0;j<asIsColumnListCount;j++)
				{
					String asIsColumnName = asIsColumnNameList.get(j);

					for(int k=0;k<toBeColumnListCount;k++)
					{
						String toBeColumnName = toBeColumnNameList.get(k);

						if(asIsColumnName.equals(toBeColumnName))
						{
							insertColumnNameList.add(asIsColumnName);
						}
					}
				}

				int insertColumnNameListCount = insertColumnNameList.size();

				//AS-IS 와 TO-BE간 동일한 칼럼이 존재하면 입력 시작
				if(insertColumnNameListCount > 0)
				{
					insertParamMap.put("toBeTableName", toBeTableName);

					for(int j=0;j<insertColumnNameListCount;j++)
					{
						String insertColumnName = insertColumnNameList.get(j);

						insertColumnsPrefixBuffer.append(insertColumnName);

						if(j != (insertColumnNameListCount-1))
						{
							insertColumnsPrefixBuffer.append(",");
						}
					}

					insertParamMap.put("insertColumnNameList", insertColumnsPrefixBuffer.toString());

					asIsDataMigrator.setMessageConsole("=============================================="+toBeTableName+" \uB370\uC774\uBC84 \uC785\uB825 \uC2DC\uC791 : ==============================================");
					asIsDataMigrator.setMessageConsole("\uC9C4\uD589\uC911...................\n");

					/**
					 * 배치처리 여부에 따라 배치용 SqlSession 객체 생성
					 * 배치처리는 INSERT 문 하나에 모든 데이터를 붙여서 입력하는 방식(처리속도는 빠르나 입력시 데이터 오류가 발생하면 전체입력이 실패함)
					 * 배치처리가 아니면 레코드 하나당 INSERT 문 하나를 생성해서 순차적으로 입력하는 방식(처리속도 느림,데이터 오류 건은 SKIP 되고, 성공한 일부 건만 입력할 수 있음)
					 */
//					toBeSqlSession = isBatchYn ? procDataTransfer.getToBeSqlSession(ExecutorType.BATCH,true) : procDataTransfer.getToBeSqlSession(true);
					toBeSqlSession = procDataTransfer.getToBeSqlSession(true);

					//배치용 INSERT INTO 문 생성시 사용(VALUES 아래 레코드 부분 이어붙어기)
					List<String> insertColumnValueList = new ArrayList<String>();

					//입력 진행율 보여주기
					ProcProgressBarHandler ppb = new ProcProgressBarHandler(asIsDataMigrator.getProgressBar(), asIsTableName, toBeTableName, asIsTableDataListCount);

					ppb.start();

					//입력할 레코드 단위로 INSERT문 생성
					for(int j=0;j<asIsTableDataListCount;j++)
					{
						Map<String,Object> recordColumnMap = asIsTableDataList.get(j);

						for(int k=0;k<insertColumnNameListCount;k++)
						{
							String insertColumnName = insertColumnNameList.get(k);
							String insertColumnValue = recordColumnMap.get(insertColumnName) == null ? "NULL" : recordColumnMap.get(insertColumnName).toString() ;

							//AS-IS 데이터 중 특정칼럼을 특정값으로 대체함. ex)COMPANY_CD=POSAM
							insertColumnValue = modifyColumnValuesMap.get(insertColumnName) == null ? insertColumnValue : modifyColumnValuesMap.get(insertColumnName).toString();

							if(insertColumnValue.equals("NULL"))
							{
								insertValuesBuffer.append(insertColumnValue);
							}else
							{
								//CREATION_TIMESTAMP,CREATION_LOCAL_TIMESTAMP
								//LAST_UPDATE_TIMESTAMP,LAST_UPDATE_LOCAL_TIMESTAMP
								if(   insertColumnName.equals("CREATION_TIMESTAMP")
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
									insertValuesBuffer.append("to_timestamp('"+insertColumnValue.replaceAll("-", "/")+"'"+",'yyyy/mm/dd hh24:mi:ss.ff')");
								}else
								{
									insertValuesBuffer.append("'"+insertColumnValue.replaceAll("'", "'||CHR(39)||'").replaceAll("&","'||CHR(38)||'").replaceAll(",","'||CHR(44)||'")+"'");
								}
							}

							if(k != (insertColumnNameListCount-1))
							{
								insertValuesBuffer.append(",");
							}
						}

						//배치입력시 하나의 INSERT INTO 문에 전체 레코드를 이어붙어서 입력 처리함.
						if(isBatchYn)
						{
							insertColumnValueList.add(insertValuesBuffer.toString());
							insertParamMap.put("insertColumnValueList", insertColumnValueList);
						}else
						{
							insertParamMap.put("insertColumnValueList", insertValuesBuffer.toString());
						}

//						MappedStatement ms = configuration.getMappedStatement("insertToBeTable");   
//						BoundSql boundSql = ms.getBoundSql(insertParamMap);
//						String sql = boundSql.getSql();

						insertValuesBuffer.setLength(0);

//						setMessageConsole("입력문 : "+sql+"\n");

						//배치입력이 아닌 경우 INSERT 문 단건 처리
						if(!isBatchYn)
						{
							try
							{
								procDataTransfer.insert(toBeSqlSession,"insertToBeTable",insertParamMap);

								ppb.setAsIsTabledataRowNum((j+1));	//진행율 갱신위해 입력 행번지정
							}catch(SQLException re)
							{
//								insertStringBuffer.append("INSERT INTO "+toBeTableName+"(");
//								insertStringBuffer.append(insertParamMap.get("insertColumnNameList"));
//								insertStringBuffer.append(") VALUES(");
//								insertStringBuffer.append(insertParamMap.get("insertColumnValueList"));
//								insertStringBuffer.append(")\n");

//								setMessageConsole((j+1)+"\uBC88\uC9F8 \uAC74 Error Msg : "+re.getMessage()+ " : "+insertStringBuffer.toString());
//								setMessageConsole((j+1)+"\uBC88\uC9F8 \uAC74 Error Msg : "+re.getMessage());

								insertStringBuffer.setLength(0);
							}finally{}
						}
						
					}

					//복수건을 한건씩 입력 처리시
					if(!isBatchYn)
					{
						ppb.stop();
						ppb = null;
					}
					//배치입력 처리
					else if(isBatchYn)
					{
						try
						{
							procDataTransfer.insertBatch(toBeSqlSession,"insertToBeTableBatch",insertParamMap,ppb);
						}catch(SQLException re)
						{
							asIsDataMigrator.setMessageConsole("Error Msg : "+re.getMessage());
						}finally
						{
							ppb.stop();
							ppb = null;
						}
					}

					insertColumnValueList.clear();
					insertColumnValueList = null;
					if(toBeSqlSession != null)toBeSqlSession.close();

					asIsDataMigrator.setMessageConsole("=============================================="+toBeTableName+" \uB370\uC774\uBC84 \uC785\uB825 \uB05D ==============================================\n");
				}
			}
		}finally
		{
			//ProcDataTransfer SwingWorker 실행 중단
			procDataTransfer.cancel(true);
			
			//데이터 이관 버튼 활성화
			asIsDataMigrator.getBtnTransfer().setEnabled(true);
			if(toBeSqlSession != null)toBeSqlSession.close();
		}
	}
}
