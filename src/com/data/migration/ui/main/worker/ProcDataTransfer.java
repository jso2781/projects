package com.data.migration.ui.main.worker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import mybatis.mapper.model.SelectParams;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.data.migration.config.model.MigrationDataInfo;
import com.data.migration.config.parse.MigrationDataInfoParser;
import com.data.migration.ui.main.AsIsDataMigrator;
import com.data.migration.ui.main.DBConfigDialog;
import com.data.migration.ui.progress.ProcProgressBarHandler;

public class ProcDataTransfer extends SwingWorker<Boolean, List<MigrationDataInfo>>
{
	private AsIsDataMigrator asIsDataMigrator = null;
	private JTable tbMigrationTableList = null;
	public ProcDataTransfer(AsIsDataMigrator asIsDataMigrator)
	{
		this.asIsDataMigrator = asIsDataMigrator;
		this.tbMigrationTableList = asIsDataMigrator.getTbMigrationTableList();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Boolean doInBackground() throws Exception
	{
		MigrationDataInfoParser parser = new MigrationDataInfoParser();
		if(parser.checkMigrationDataInfoList(asIsDataMigrator,true))
		{
			//�������� ���� ����
			List<MigrationDataInfo> migrationDataInfoList = parser.getMigrationDataInfoList(tbMigrationTableList,true);
			parser.saveMigrationDataInfoListToXml(migrationDataInfoList);

			//������ �̰� ��� ���̺� ��� ����
			String asIsTableName = null;
			String asIsWhereClause = null;
			MigrationDataInfo mdi = null;

			List<MigrationDataInfo> procMigrationDataInfoList = new ArrayList<MigrationDataInfo>();
			int migrationDataInfoListCount = migrationDataInfoList.size();

			StringBuffer alretMessageStr = new StringBuffer();
			StringBuffer queryStringStr = new StringBuffer();
			alretMessageStr.append("\uC785\uB825\uD560 TO-BE \uD14C\uC774\uBE14\uBA85\n");

			asIsDataMigrator.setMessageConsole("=================================== AS-IS \uD14C\uC774\uBE14 \uB9AC\uC2A4\uD2B8 \uC2DC\uC791=============================");
			for(int i=0;i<migrationDataInfoListCount;i++)
			{
				mdi = migrationDataInfoList.get(i);
				asIsTableName = mdi.getAsIsTableName();
				asIsWhereClause = mdi.getAsIsWhereClause();
				asIsDataMigrator.setMessageConsole(asIsTableName);
				alretMessageStr.append(asIsTableName+"\n");

				SelectParams sp = new SelectParams();
				sp.setAsIsTableName(asIsTableName);
				sp.setWhereClause(asIsWhereClause);

				Configuration configuration = getAsIsSqlSessionFactory().getConfiguration();

				MappedStatement ms = configuration.getMappedStatement("selectAsIsTableDataList");
				BoundSql boundSql = ms.getBoundSql(sp);
				String sql = boundSql.getSql();

				queryStringStr.append("=============================================="+asIsTableName+" \uC2E4\uD589 \uCFFC\uB9AC \uC2DC\uC791 ==============================================\n");
				queryStringStr.append("Execute Query : \n"+sql+"\n");
				queryStringStr.append("=============================================="+asIsTableName+" \uC2E4\uD589 \uCFFC\uB9AC \uB05D ==============================================\n\n");
			}
			asIsDataMigrator.setMessageConsole("=================================== AS-IS \uD14C\uC774\uBE14 \uB9AC\uC2A4\uD2B8 \uB05D=============================\n\n");

			asIsDataMigrator.setMessageConsole(queryStringStr.toString());

			alretMessageStr.append("\uACC4\uC18D \uC9C4\uD589\uD558\uC2DC\uACA0\uC2B5\uB2C8\uAE4C?\n");

			int result = JOptionPane.showConfirmDialog(asIsDataMigrator, alretMessageStr.toString(), "\uC785\uB825 \uD14C\uC774\uBE14\uBA85 \uD655\uC778", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if(JOptionPane.YES_OPTION != result)
			{
				return false;
			}

			//������ �̰� ��ư ��Ȱ��ȭ(�Է� ó���� �Ϸ�ɶ�����)
			asIsDataMigrator.getBtnTransfer().setEnabled(false);

			//������ �̰� ó��
			//������ DB�κ��� ���̱׷��̼��� ���̺���� Į������,�����Ͱ��� ������.(AS-IS)
			for(int i=0;i<migrationDataInfoListCount;i++)
			{
				SqlSession asIsSqlSession = getAsIsSqlSession();
				mdi = migrationDataInfoList.get(i);
				asIsTableName = mdi.getAsIsTableName();
				asIsWhereClause = mdi.getAsIsWhereClause();

				SelectParams sp = new SelectParams();
				sp.setAsIsTableName(asIsTableName);
				sp.setWhereClause(asIsWhereClause);

				Configuration configuration = getAsIsSqlSessionFactory().getConfiguration();

				MappedStatement ms = configuration.getMappedStatement("selectAsIsTableDataList");   
				BoundSql boundSql = ms.getBoundSql(sp);
				String sql = boundSql.getSql();

//				asIsDataMigrator.setMessageConsole("AS-IS Table Name="+asIsTableName);

				asIsDataMigrator.setMessageConsole("=============================================="+asIsTableName+" \uB370\uC774\uD130 \uAC00\uC838\uC624\uAE30 \uC2DC\uC791 ==============================================\n");
				asIsDataMigrator.setMessageConsole("Execute Query : \n"+sql+"\n");

				List<Map<String,Object>> resultMapList = null;
				try
				{
					resultMapList = selectList(asIsSqlSession, "selectAsIsTableDataList", sp);
				}catch(SQLException re)
				{
					if(asIsSqlSession != null)asIsSqlSession.close();
					return false;	//Exception �߻����� ������ �̰� ó�� �ߴ�
				}finally
				{
					if(asIsSqlSession != null)asIsSqlSession.close();
				}

				if(resultMapList != null)
				{
					int resultMapListCount = resultMapList.size();

					asIsDataMigrator.setMessageConsole("Result List Count : "+resultMapListCount);
					asIsDataMigrator.setMessageConsole("=============================================="+asIsTableName+" \uB370\uC774\uBC84 \uAC00\uC838\uC624\uAE30 \uB05D ==============================================\n");

					if(resultMapListCount > 0)
					{
						SqlSession toBeSqlSession = getToBeSqlSession();

						try
						{
							//TO-BE ���̺��� Į������ ��������
							SelectParams sp1 = new SelectParams();
							sp1.setTableName(mdi.getToBeTableName());

							List<Map<String,Object>> toBeTableColumnInfoList = selectList(toBeSqlSession, "selectTableColumnList", sp1);

//							asIsDataMigrator.setMessageConsole("doInBackground() toBeTableColumnInfoList.size()="+toBeTableColumnInfoList.size());
							if(toBeTableColumnInfoList != null)
							{
								//AS-IS ���̺��� Į������ ��������
								asIsSqlSession = getAsIsSqlSession();
								SelectParams sp2 = new SelectParams();
								sp2.setTableName(mdi.getAsIsTableName());
								List<Map<String,Object>> asIsTableColumnInfoList = selectList(asIsSqlSession, "selectTableColumnList", sp2);

								int asIsTableColumnInfoListCount = asIsTableColumnInfoList.size();

//								asIsDataMigrator.setMessageConsole("doInBackground() asIsTableColumnInfoList.size()="+asIsTableColumnInfoList.size());

								List<String> asIsColumnNameList = new ArrayList<String>();
								for(int k=0;k<asIsTableColumnInfoListCount;k++)
								{
									String columnName = (String) asIsTableColumnInfoList.get(k).get("COLUMN_NAME");
									asIsColumnNameList.add(columnName);
								}

								int toBeTableColumnInfoListCount = toBeTableColumnInfoList.size();
								List<String> toBeColumnNameList = new ArrayList<String>();
								for(int j=0;j<toBeTableColumnInfoListCount;j++)
								{
									String columnName = (String) toBeTableColumnInfoList.get(j).get("COLUMN_NAME");
									toBeColumnNameList.add(columnName);
								}

								MigrationDataInfo inMdi = new MigrationDataInfo();
								
								inMdi.setAsIsTableName(asIsTableName);
								inMdi.setAsIsWhereClause(asIsWhereClause);
								inMdi.setAsIsColumnNameList(asIsColumnNameList);
								inMdi.setAsIsTableDataList(resultMapList);
								inMdi.setToBeTableName(mdi.getToBeTableName());
								inMdi.setToBeColumnNameList(toBeColumnNameList);
								inMdi.setModifyColumnValues(mdi.getModifyColumnValues());

								procMigrationDataInfoList.add(inMdi);
							}
						}catch(SQLException re)
						{
							if(asIsSqlSession != null)asIsSqlSession.close();
							if(toBeSqlSession != null)toBeSqlSession.close();
							return false;	//Exception �߻����� ������ �̰� ó�� �ߴ�
						}finally
						{
							if(asIsSqlSession != null)asIsSqlSession.close();
							if(toBeSqlSession != null)toBeSqlSession.close();
						}
					}
				}
			}

			int procMigrationDataInfoListCount = procMigrationDataInfoList.size();

			if(procMigrationDataInfoListCount > 0)
			{
				boolean isVpnYn = asIsDataMigrator.getChkVPNYn().isSelected() ? true : false;

				//VPNȯ�� ������ ��� VPN�� �Ѱų� ������ �˸���, ��� ������ ���� ���� Ȯ��â�� ��.
				if(isVpnYn)
				{
					//AS-IS �����Ͱ� ����Ǿ����ϴ�.
					//
					//  ���̺�1 34��
					//  ���̺�2 120��
					//  ���̺�3 12000��
					//
					//VPN ����(����)�� ������ �̰��� �����Ͻ÷��� YES ��ư�� �����ֽʽÿ�.
					//
					//  AS-IS DB --> TO-BE DB(VPN����) = VPN ������ YES ��ư
					//  AS-IS DB(VPN����) --> TO-BE DB = VPN �������� �� YES ��ư
					//
					//TO-BE DB ���̺�� ������ �̰��� �����ϰڽ��ϱ�?

					int dataRowCount = 0;
					MigrationDataInfo inMdi = null;
					StringBuffer sb = new StringBuffer();

					sb.append("AS-IS \uB370\uC774\uD130\uAC00 \uCD94\uCD9C\uB418\uC5C8\uC2B5\uB2C8\uB2E4.\n\n");
					for(int i=0;i<procMigrationDataInfoListCount;i++)
					{
						inMdi = procMigrationDataInfoList.get(i);
						asIsTableName = inMdi.getAsIsTableName();
						dataRowCount = inMdi.getAsIsTableDataList().size();
						sb.append("  "+asIsTableName+" "+dataRowCount+" \uAC74\n");
					}

					inMdi = null;
					asIsTableName = null;

					sb.append("\nVPN \uC811\uC18D(\uD574\uC81C)\uD6C4 \uB370\uC774\uD130 \uC774\uAD00\uC744 \uC9C4\uD589\uD558\uC2DC\uB824\uBA74 YES \uBC84\uD2BC\uC744 \uB20C\uB824\uC8FC\uC2ED\uC2DC\uC694.\n\n");
					sb.append("  AS-IS DB --> TO-BE DB(VPN\uC811\uC18D) = VPN \uC811\uC18D\uD6C4 YES \uBC84\uD2BC\n");
					sb.append("  AS-IS DB(VPN\uC811\uC18D) --> TO-BE DB = VPN \uC811\uC18D\uD574\uC81C \uD6C4 YES \uBC84\uD2BC\n");
					sb.append("\nTO-BE DB \uD14C\uC774\uBE14\uB85C \uB370\uC774\uD130 \uC774\uAD00\uC744 \uC9C4\uD589\uD558\uACA0\uC2B5\uB2C8\uAE4C?");
					
					
					int vpnYnResult = JOptionPane.showConfirmDialog(asIsDataMigrator, sb.toString(), "QUESTION", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					sb.setLength(0);
					sb = null;

					if(JOptionPane.YES_OPTION  == vpnYnResult)
					{
						publish(procMigrationDataInfoList);
					}else
					{
						//������ �̰� ��ư Ȱ��ȭ
						asIsDataMigrator.getBtnTransfer().setEnabled(true);
						return false;
					}
				}else
				{
					//AS-IS�� TO-BE ���̺� Į�� ������ ���ؼ� �Է� �������� ���� ������ ���̱׷��̼� ó��
					publish(procMigrationDataInfoList);
				}
			}
		}
		return true;
	}

	/**
	 * AS-IS�� TO-BE ���̺� �����Ͱ� Į���� ���� �� ������ Į������ �������� 
	 * �Է¹�(INSERT) SQL�� �ۼ��ѵ� ���̱׷��̼� ó��
	 */
	@Override
	protected void process(List<List<MigrationDataInfo>> chunks)
	{
		for(List<MigrationDataInfo> procMigrationDataInfoList : chunks) 
		{
			ToBeInsertProc tbp = new ToBeInsertProc(asIsDataMigrator,this, procMigrationDataInfoList);
			Thread th = new Thread(tbp);
			th.start();
		}

		chunks = null;
	}

	@Override
	protected void done()
	{
		try
		{
			get();
			System.gc();
			asIsDataMigrator.getBtnTransfer().setEnabled(true);
		}catch(InterruptedException ie){}
		catch(ExecutionException ex)
		{
			ex.printStackTrace();
		}
	}

	public List<Map<String,Object>> selectList(SqlSession sqlSession,String queryName,SelectParams sp) throws SQLException
	{
		List<Map<String,Object>> resultMapList = null;

		try
		{
			resultMapList = sqlSession.selectList(queryName, sp);
		}catch(PersistenceException re)
		{
			if(re.getCause() instanceof PersistenceException)
			{
				re = (PersistenceException)re.getCause();
			}

			if(re.getCause() instanceof SQLException)
			{
				SQLException sqlEx = (SQLException)re.getCause();

				while(sqlEx != null)
				{
//					int errorCode = sqlEx.getErrorCode();
//					String sqlState = sqlEx.getSQLState();
					String sqlMsg = sqlEx.getMessage();

					if(sqlMsg.indexOf("The Network Adapter could not establish the connection") > -1 ||
						sqlMsg.indexOf("Connection refused") > -1)
					{
						Configuration config = sqlSession.getConfiguration();
						PooledDataSource ds = (PooledDataSource) config.getEnvironment().getDataSource();
						sqlMsg += "\n\ndriver="+ds.getDriver()+"\nurl="+ds.getUrl()+"\nuser_id="+ds.getUsername()+"\n";

						JOptionPane.showMessageDialog(asIsDataMigrator, sqlMsg, "SQLException", JOptionPane.WARNING_MESSAGE);
						DBConfigDialog dbConfigDialog = new DBConfigDialog(asIsDataMigrator);
						dbConfigDialog.setLocationRelativeTo(asIsDataMigrator);
						dbConfigDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dbConfigDialog.setVisible(true);
						throw sqlEx;
					}

					if(sqlEx.getNextException() == null)
					{
//						JOptionPane.showMessageDialog(AsIsDataMigrator.this, sqlMsg, "SQLException", JOptionPane.WARNING_MESSAGE);
						throw sqlEx;
					}else
					{
						sqlEx = sqlEx.getNextException();
					}
				}
			}
		}finally
		{
			sqlSession.close();
		}
		return resultMapList;
	}

	@SuppressWarnings("unchecked")
	public void insertBatch(SqlSession sqlSession,String queryName,Map<String, Object> insertParamMap,ProcProgressBarHandler ppb) throws SQLException
	{
		try
		{

			List<String> rowStrList = (List<String>) insertParamMap.get("insertColumnValueList");
			int rowStrListCount = rowStrList.size();

//			int insertFetchCount = 3000;
//			int loop = (int) Math.ceil((double)rowStrListCount/insertFetchCount);

//			if(rowStrListCount > insertFetchCount)
//			{
//				Map<String, Object> subInsertParamMap = new HashMap<String,Object>();
//				String toBeTableName = (String) insertParamMap.get("toBeTableName");
//				String insertColumnNameList = (String) insertParamMap.get("insertColumnNameList");
//				subInsertParamMap.put("toBeTableName", toBeTableName);
//				subInsertParamMap.put("insertColumnNameList", insertColumnNameList);
//
//				for(int i=0;i<=loop;i++)
//				{
//					int fromIndex = i * insertFetchCount;
//					int toIndex = (i + 1) * insertFetchCount;
//					if(toIndex > rowStrListCount)
//					{
//						toIndex = rowStrListCount;
//					}
//					System.out.println("fromIndex="+fromIndex+", toIndex="+toIndex);
//					subInsertParamMap.put("insertColumnValueList", rowStrList.subList(fromIndex,toIndex));
//					sqlSession.insert(queryName, subInsertParamMap);
//					sqlSession.commit();
//
//					ppb.setAsIsTabledataRowNum(toIndex);
//				}
//			}
//			else
//			{
				sqlSession.insert(queryName, insertParamMap);
				sqlSession.commit();

				ppb.setAsIsTabledataRowNum(rowStrListCount);
//			}
		}catch(PersistenceException re)
		{
//			setMessageConsole("insert PersistenceException="+re.getMessage());
			if(re.getCause() instanceof PersistenceException)
			{
				re = (PersistenceException)re.getCause();
			}

			if(re.getCause() instanceof SQLException)
			{
				SQLException sqlEx = (SQLException)re.getCause();

				while(sqlEx != null)
				{
//					int errorCode = sqlEx.getErrorCode();
//					String sqlState = sqlEx.getSQLState();
					String sqlMsg = sqlEx.getMessage();

//					setMessageConsole("SQLException : errorCode="+errorCode+", sqlState="+sqlState+", message="+sqlMsg);

					if(sqlMsg.indexOf("The Network Adapter could not establish the connection") > -1 ||
						sqlMsg.indexOf("Connection refused") > -1)
					{
						Configuration config = sqlSession.getConfiguration();
						PooledDataSource ds = (PooledDataSource) config.getEnvironment().getDataSource();
						sqlMsg += "\n\ndriver="+ds.getDriver()+"\nurl="+ds.getUrl()+"\nuser_id="+ds.getUsername()+"\n";

						JOptionPane.showMessageDialog(asIsDataMigrator, sqlMsg, "SQLException", JOptionPane.WARNING_MESSAGE);
						DBConfigDialog dbConfigDialog = new DBConfigDialog(asIsDataMigrator);
						dbConfigDialog.setLocationRelativeTo(asIsDataMigrator);
						dbConfigDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dbConfigDialog.setVisible(true);

						throw sqlEx;
					}

					if(sqlEx.getNextException() == null)
					{
//						setMessageConsole("SQLException : errorCode="+errorCode+", sqlState="+sqlState+", message="+sqlMsg);

//						JOptionPane.showMessageDialog(AsIsDataMigrator.this, sqlMsg, "SQLException", JOptionPane.WARNING_MESSAGE);
						throw sqlEx;
					}else
					{
						sqlEx = sqlEx.getNextException();
					}
				}
			}
		}
	}

	public void insert(SqlSession sqlSession,String queryName,Map<String, Object> insertParamMap) throws SQLException
	{
		try
		{
			sqlSession.insert(queryName, insertParamMap);
			sqlSession.commit();
		}catch(PersistenceException re)
		{
//			setMessageConsole("insert PersistenceException="+re.getMessage());
			if(re.getCause() instanceof PersistenceException)
			{
				re = (PersistenceException)re.getCause();
			}

			if(re.getCause() instanceof SQLException)
			{
				SQLException sqlEx = (SQLException)re.getCause();

				while(sqlEx != null)
				{
//					int errorCode = sqlEx.getErrorCode();
//					String sqlState = sqlEx.getSQLState();
					String sqlMsg = sqlEx.getMessage();

//					setMessageConsole("SQLException : errorCode="+errorCode+", sqlState="+sqlState+", message="+sqlMsg);

					if(sqlMsg.indexOf("The Network Adapter could not establish the connection") > -1 ||
						sqlMsg.indexOf("Connection refused") > -1)
					{
						Configuration config = sqlSession.getConfiguration();
						PooledDataSource ds = (PooledDataSource) config.getEnvironment().getDataSource();
						sqlMsg += "\n\ndriver="+ds.getDriver()+"\nurl="+ds.getUrl()+"\nuser_id="+ds.getUsername()+"\n";

						JOptionPane.showMessageDialog(asIsDataMigrator, sqlMsg, "SQLException", JOptionPane.WARNING_MESSAGE);
						DBConfigDialog dbConfigDialog = new DBConfigDialog(asIsDataMigrator);
						dbConfigDialog.setLocationRelativeTo(asIsDataMigrator);
						dbConfigDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dbConfigDialog.setVisible(true);

						throw sqlEx;
					}

					if(sqlEx.getNextException() == null)
					{
//						setMessageConsole("SQLException : errorCode="+errorCode+", sqlState="+sqlState+", message="+sqlMsg);

//						JOptionPane.showMessageDialog(AsIsDataMigrator.this, sqlMsg, "SQLException", JOptionPane.WARNING_MESSAGE);
						throw sqlEx;
					}else
					{
						sqlEx = sqlEx.getNextException();
					}
				}
			}
		}
	}

	public SqlSessionFactory getAsIsSqlSessionFactory()
	{
		return asIsDataMigrator.getMyBatisConfigTask().getAsIsSqlSessionFactory();
	}

	public SqlSessionFactory getToBeSqlSessionFactory()
	{
		return asIsDataMigrator.getMyBatisConfigTask().getToBeSqlSessionFactory();
	}

	public SqlSession getAsIsSqlSession()
	{
		return asIsDataMigrator.getMyBatisConfigTask().getAsIsSqlSession();
	}

	public SqlSession getToBeSqlSession()
	{
		return getToBeSqlSession(false);
	}

	public SqlSession getToBeSqlSession(boolean isAutoCommit)
	{
		return asIsDataMigrator.getMyBatisConfigTask().getToBeSqlSession(null,isAutoCommit);
	}

	public SqlSession getToBeSqlSession(ExecutorType executorType,boolean isAutoCommit)
	{
		return asIsDataMigrator.getMyBatisConfigTask().getToBeSqlSession(executorType,isAutoCommit);
	}
}