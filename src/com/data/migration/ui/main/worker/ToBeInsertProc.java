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

				//AS-IS �� TO-BE�� ������ Į���� �����ϸ� �Է� ����
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
					 * ��ġó�� ���ο� ���� ��ġ�� SqlSession ��ü ����
					 * ��ġó���� INSERT �� �ϳ��� ��� �����͸� �ٿ��� �Է��ϴ� ���(ó���ӵ��� ������ �Է½� ������ ������ �߻��ϸ� ��ü�Է��� ������)
					 * ��ġó���� �ƴϸ� ���ڵ� �ϳ��� INSERT �� �ϳ��� �����ؼ� ���������� �Է��ϴ� ���(ó���ӵ� ����,������ ���� ���� SKIP �ǰ�, ������ �Ϻ� �Ǹ� �Է��� �� ����)
					 */
//					toBeSqlSession = isBatchYn ? procDataTransfer.getToBeSqlSession(ExecutorType.BATCH,true) : procDataTransfer.getToBeSqlSession(true);
					toBeSqlSession = procDataTransfer.getToBeSqlSession(true);

					//��ġ�� INSERT INTO �� ������ ���(VALUES �Ʒ� ���ڵ� �κ� �̾�پ��)
					List<String> insertColumnValueList = new ArrayList<String>();

					//�Է� ������ �����ֱ�
					ProcProgressBarHandler ppb = new ProcProgressBarHandler(asIsDataMigrator.getProgressBar(), asIsTableName, toBeTableName, asIsTableDataListCount);

					ppb.start();

					//�Է��� ���ڵ� ������ INSERT�� ����
					for(int j=0;j<asIsTableDataListCount;j++)
					{
						Map<String,Object> recordColumnMap = asIsTableDataList.get(j);

						for(int k=0;k<insertColumnNameListCount;k++)
						{
							String insertColumnName = insertColumnNameList.get(k);
							String insertColumnValue = recordColumnMap.get(insertColumnName) == null ? "NULL" : recordColumnMap.get(insertColumnName).toString() ;

							//AS-IS ������ �� Ư��Į���� Ư�������� ��ü��. ex)COMPANY_CD=POSAM
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

						//��ġ�Է½� �ϳ��� INSERT INTO ���� ��ü ���ڵ带 �̾�پ �Է� ó����.
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

//						setMessageConsole("�Է¹� : "+sql+"\n");

						//��ġ�Է��� �ƴ� ��� INSERT �� �ܰ� ó��
						if(!isBatchYn)
						{
							try
							{
								procDataTransfer.insert(toBeSqlSession,"insertToBeTable",insertParamMap);

								ppb.setAsIsTabledataRowNum((j+1));	//������ �������� �Է� �������
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

					//�������� �ѰǾ� �Է� ó����
					if(!isBatchYn)
					{
						ppb.stop();
						ppb = null;
					}
					//��ġ�Է� ó��
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
			//ProcDataTransfer SwingWorker ���� �ߴ�
			procDataTransfer.cancel(true);
			
			//������ �̰� ��ư Ȱ��ȭ
			asIsDataMigrator.getBtnTransfer().setEnabled(true);
			if(toBeSqlSession != null)toBeSqlSession.close();
		}
	}
}
