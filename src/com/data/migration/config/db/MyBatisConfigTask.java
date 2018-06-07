package com.data.migration.config.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import mybatis.mapper.model.SelectParams;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.data.migration.ui.main.AsIsDataMigrator;

public class MyBatisConfigTask implements Runnable
{
	private SqlSessionFactory asIsSqlSessionFactory = null;
	private SqlSessionFactory toBeSqlSessionFactory = null;
	private SqlSession asIsSqlSession = null;
	private SqlSession toBeSqlSession = null;
	private AsIsDataMigrator asIsDataMigrator = null;
	private Properties dbProperties = null; 
	public MyBatisConfigTask(){}

	public MyBatisConfigTask(AsIsDataMigrator asIsDataMigrator)
	{
		this.asIsDataMigrator = asIsDataMigrator;
	}

	public MyBatisConfigTask(SqlSessionFactory asIsSqlSessionFactory,SqlSessionFactory toBeSqlSessionFactory)
	{
		this.asIsSqlSessionFactory = asIsSqlSessionFactory;
		this.toBeSqlSessionFactory = toBeSqlSessionFactory;
	}

	@Override
	public void run()
	{
		String resource = "mybatis/config/mybatis_config.xml";
		InputStream mybatisConfigIn1 = null;
		InputStream mybatisConfigIn2 = null;
		File dbPropertiesFile = null;
		FileInputStream fin = null;
		try
		{
			if(asIsSqlSession != null)
			{
				asIsSqlSession.close();
				asIsSqlSession = null;
			}

			if(toBeSqlSession != null)
			{
				toBeSqlSession.close();
				toBeSqlSession = null;
			}

			mybatisConfigIn1 = Resources.getResourceAsStream(resource);
			mybatisConfigIn2 = Resources.getResourceAsStream(resource);

			String configDir = new File("config").getAbsolutePath();
			String currentDirStr = URLDecoder.decode(configDir, "UTF-8");
			dbPropertiesFile = new File(currentDirStr+File.separator+"db.properties");

			dbProperties = new Properties();

			fin= new FileInputStream(dbPropertiesFile);
			dbProperties.load(fin);

			asIsSqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfigIn1,"asIsEnvironment",dbProperties);
			asIsDataMigrator.setMessageConsole("AS-IS DB Configuration !!");
			asIsDataMigrator.setMessageConsole("    AS-IS CONNECTION NAME : "+dbProperties.getProperty("as_is_connection_name"));
			asIsDataMigrator.setMessageConsole("    AS-IS HOST ADDRESS : "+dbProperties.getProperty("as_is_host_address"));
			asIsDataMigrator.setMessageConsole("    AS-IS SERVICE NAME : "+dbProperties.getProperty("as_is_service_name"));
			asIsDataMigrator.setMessageConsole("    AS-IS PORT : "+dbProperties.getProperty("as_is_port"));
			asIsDataMigrator.setMessageConsole("    AS-IS USER ID : "+dbProperties.getProperty("as_is_user_id"));

			toBeSqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfigIn2,"toBeEnvironment",dbProperties);
			asIsDataMigrator.setMessageConsole("\nTO-BE DB Configuration !!");
			asIsDataMigrator.setMessageConsole("    TO-BE CONNECTION NAME : "+dbProperties.getProperty("to_be_connection_name"));
			asIsDataMigrator.setMessageConsole("    TO-BE HOST ADDRESS : "+dbProperties.getProperty("to_be_host_address"));
			asIsDataMigrator.setMessageConsole("    TO-BE SERVICE NAME : "+dbProperties.getProperty("to_be_service_name"));
			asIsDataMigrator.setMessageConsole("    TO-BE PORT : "+dbProperties.getProperty("to_be_port"));
			asIsDataMigrator.setMessageConsole("    TO-BE USER ID : "+dbProperties.getProperty("to_be_user_id")+"\n\n");
			asIsDataMigrator.setMessageConsole("BATCH PROCESSING USE : "+dbProperties.getProperty("batchYn")+"\n");

			//배치처리 여부 지정
			asIsDataMigrator.getChkBatchYn().setSelected("Y".equals(dbProperties.getProperty("batchYn")) ? true : false);

			//VPN환경 여부 지정
			asIsDataMigrator.getChkVPNYn().setSelected("Y".equals(dbProperties.getProperty("vpnYn")) ? true : false);

//			getSqlSession();

//			String driver = prop.getProperty("driver");
//			String hostAddress = prop.getProperty("host_address");
//			String port = prop.getProperty("port");
//			String serviceName = prop.getProperty("service_name");
//			String user_id = prop.getProperty("user_id");
//			String password = prop.getProperty("password");
//
//			String url = "jdbc:oracle:thin:@"+hostAddress+":"+port+":"+serviceName;
//
//			System.out.println("FbsAckViewer setMyBatisConfig properties host_address="+hostAddress);
//
//			TransactionFactory transactionFactory = new JdbcTransactionFactory();
//			PooledDataSource pooledDs = new PooledDataSource(driver, url, user_id, password);
//			pooledDs.setPoolPingEnabled(true);
//			pooledDs.setPoolPingQuery("select 1 from dual");
//			pooledDs.setPoolPingConnectionsNotUsedFor(43200);
//			pooledDs.setPoolTimeToWait(30000);
//
//
//			Environment env = new Environment("development", transactionFactory, pooledDs);
//			
//			Configuration config = new Configuration(env);
//			config.setJdbcTypeForNull(JdbcType.NULL);
//			config.setCacheEnabled(true);
//			config.setLazyLoadingEnabled(true);
//			config.setMultipleResultSetsEnabled(true);
//			config.setUseColumnLabel(true);
//			config.setUseGeneratedKeys(false);
//			config.setDefaultExecutorType(ExecutorType.SIMPLE);
//			config.setDefaultStatementTimeout(25000);
//
//			config.getTypeAliasRegistry().registerAlias("fbsAckInfo", FbsAckInfo.class);
//			config.addMapper(FbsAckMapper.class);	//mybatis.mapper.FbsAckMapper
//
//			sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
		}catch(FileNotFoundException e)
		{
			asIsDataMigrator.setMessageConsole("DB Configuration not found exception : "+dbPropertiesFile.getAbsoluteFile());
		}catch(UnsupportedEncodingException e)
		{
			asIsDataMigrator.setMessageConsole("UTF-8 Unsupported Encoding Exception !!");
		}catch(IOException e)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
//			e.printStackTrace(pw);

			asIsDataMigrator.setMessageConsole(sw.toString());

			try{sw.close();}catch(IOException e1){}
			pw.close();
		}finally
		{
			if(mybatisConfigIn1 != null){try{mybatisConfigIn1.close();}catch(IOException e){}}
			if(mybatisConfigIn2 != null){try{mybatisConfigIn2.close();}catch(IOException e){}}
			if(fin != null){try{fin.close();}catch(IOException e){}}
			System.gc();
		}
	}

	public Properties getDbProperties() {
		return dbProperties;
	}

	public void setDbProperties(Properties dbProperties) {
		this.dbProperties = dbProperties;
	}

	public SqlSessionFactory getAsIsSqlSessionFactory()
	{
		return asIsSqlSessionFactory;
	}

	public void setAsIsSqlSessionFactory(SqlSessionFactory asIsSqlSessionFactory) {
		this.asIsSqlSessionFactory = asIsSqlSessionFactory;
	}

	public SqlSessionFactory getToBeSqlSessionFactory() {
		return toBeSqlSessionFactory;
	}

	public void setToBeSqlSessionFactory(SqlSessionFactory toBeSqlSessionFactory) {
		this.toBeSqlSessionFactory = toBeSqlSessionFactory;
	}

	public SqlSession getAsIsSqlSession()
	{
		if(asIsSqlSession != null)
		{
			asIsSqlSession.close();
			
		}

		asIsSqlSession = asIsSqlSessionFactory.openSession();

		return asIsSqlSession;
	}

	public void setAsIsSqlSession(SqlSession asIsSqlSession)
	{
		this.asIsSqlSession = asIsSqlSession;
	}

	public SqlSession getToBeSqlSession(ExecutorType batch,boolean isAutoCommit)
	{
		if(toBeSqlSession != null)
		{
			toBeSqlSession.close();
		}

		if(isAutoCommit)
		{
			if(ExecutorType.BATCH == batch)
			{
				toBeSqlSession = toBeSqlSessionFactory.openSession(ExecutorType.BATCH,isAutoCommit);
			}else
			{
				toBeSqlSession = toBeSqlSessionFactory.openSession(isAutoCommit);
			}
		}else
		{
			if(ExecutorType.BATCH == batch)
			{
				toBeSqlSession = toBeSqlSessionFactory.openSession(ExecutorType.BATCH);
			}else
			{
				toBeSqlSession = toBeSqlSessionFactory.openSession();
			}
		}

		return toBeSqlSession;
	}

	public void setToBeSqlSession(SqlSession toBeSqlSession) {
		this.toBeSqlSession = toBeSqlSession;
	}

	public AsIsDataMigrator getAsIsDataMigrator() {
		return asIsDataMigrator;
	}

	public void setAsIsDataMigrator(AsIsDataMigrator asIsDataMigrator) {
		this.asIsDataMigrator = asIsDataMigrator;
	}

	public static void main(String args[])
	{
		SqlSessionFactory asIsSqlSessionFactory = null;
		SqlSessionFactory toBeSqlSessionFactory = null;
		SqlSession asIsSqlSession = null;
//		SqlSession toBeSqlSession = null;
		
		MyBatisConfigTask mcr = new MyBatisConfigTask(asIsSqlSessionFactory,toBeSqlSessionFactory);
		mcr.run();

		StringBuffer sb = new StringBuffer();
		sb.append("  COMPANY_CD = 'AAPC'\n");
		sb.append("AND sales_dt >= '20171128'\n");
		SelectParams sp = new SelectParams();
		sp.setAsIsTableName("TB_SO_SALES");
		sp.setWhereClause(sb.toString());

		Configuration configuration = mcr.getAsIsSqlSessionFactory().getConfiguration();

		MappedStatement ms = configuration.getMappedStatement("selectAsIsTableDataList");   
		BoundSql boundSql = ms.getBoundSql(sp);
		String sql = boundSql.getSql();

		System.out.println("\nquery string : \n"+sql);

		asIsSqlSession = mcr.getAsIsSqlSessionFactory().openSession();
		List<Map<String,Object>> resultMapList = asIsSqlSession.selectList("selectAsIsTableDataList", sp);

		int resultMapListCount = resultMapList.size();

		System.out.println("\nresultMapListCount="+resultMapListCount);

		for(int i=0;i<resultMapListCount;i++)
		{
			if(i == 0)
			{
				Map<String,Object> recordMap = resultMapList.get(i);
				Iterator<String> recordMapKeys = recordMap.keySet().iterator();
				while(recordMapKeys.hasNext())
				{
					String columnName = recordMapKeys.next();
					System.out.println("columnName="+columnName+", value="+recordMap.get(columnName));
				}
			}
		}

		asIsSqlSession.close();
	}
}
