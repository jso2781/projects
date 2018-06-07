package com.sql.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlQueryParser
{
	private List<String> paramList = new ArrayList<String>();
	private static String sqlFileName[] = null;

	public SqlQueryParser()
	{
		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	public List<String> getRowStringList()
    {
        //�ּ� ,���� ���� ����
        String patternStr = "(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)|(//.*)";//|\\s";
        
//        String lineSeparator = System.getProperty("line.separator");

        List<String> rowStringList = new ArrayList<String>();

        FileChannel fin = null;
        FileInputStream fis = null;
        try
        {
            String currentPath = this.getClass().getResource("").getPath();
            File f = new File(currentPath+"sql_query.txt");

            fis = new FileInputStream(f);
            fin = fis.getChannel();

            MappedByteBuffer mappedByteBuffer = fin.map(FileChannel.MapMode.READ_ONLY, 0, fin.size());
            
            Charset charset = Charset.forName("UTF-8");
            
            CharsetDecoder decoder = charset.newDecoder();
            
            CharBuffer cbuffer = decoder.decode(mappedByteBuffer);
            
            String fileContents = cbuffer.toString().replaceAll(patternStr, "");
            
            Scanner sc = new Scanner(fileContents);//.useDelimiter(lineSeparator);
            while(sc.hasNext())
            {
                String lineStr = sc.nextLine();
                
                if(!lineStr.trim().equals(""))
                {
                    System.out.println(lineStr.trim());
                    rowStringList.add(lineStr);
                }
            }
            sc.close();
        }catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }catch(IOException e)
        {
            e.printStackTrace();
        }finally
        {
        	try{if(fin != null)fin.close();}catch(IOException ioe){}
            try{if(fis != null)fis.close();}catch(IOException ioe){}
        }

        return rowStringList;
    }

	public List<String> getRowStringList(String sqlFileName)
    {
        //�ּ� ,���� ���� ����
//        String patternStr = "(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)|(//.*)";//|\\s";
        
//        String lineSeparator = System.getProperty("line.separator");

        List<String> rowStringList = new ArrayList<String>();

        FileChannel fin = null;
        FileInputStream fis = null;
        try
        {
            String currentPath = this.getClass().getResource("").getPath();
            File f = new File(currentPath+sqlFileName);

            fis = new FileInputStream(f);
            fin = fis.getChannel();

            MappedByteBuffer mappedByteBuffer = fin.map(FileChannel.MapMode.READ_ONLY, 0, fin.size());
            
            Charset charset = Charset.forName("UTF-8");
            
            CharsetDecoder decoder = charset.newDecoder();
            
            CharBuffer cbuffer = decoder.decode(mappedByteBuffer);

            String fileContents = cbuffer.toString();
//            String fileContents = cbuffer.toString().replaceAll(patternStr, "");
            
            Scanner sc = new Scanner(fileContents);//.useDelimiter(lineSeparator);
            while(sc.hasNext())
            {
                String lineStr = sc.nextLine();
                
                if(!lineStr.trim().equals(""))
                {
                    //System.out.println(lineStr.trim());
                    rowStringList.add(lineStr);
                }
            }

            rowStringList.add("\n");
            sc.close();
        }catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }catch(IOException e)
        {
            e.printStackTrace();
        }finally
        {
        	try{if(fin != null)fin.close();}catch(IOException ioe){}
            try{if(fis != null)fis.close();}catch(IOException ioe){}
        }

        return rowStringList;
    }

	/**
	 * ���� ���ϳ����� ���� ���ε� ������ ������� �ػ�ũ�ο� �����ͼ� ����
	 * @param rowStringList
	 */
	public void genDataSetFromQueryString(List<String> rowStringList, List<ColumnInfo> columnList)
	{
		if(rowStringList != null && rowStringList.size() > 0)
		{
			int rowCount = rowStringList.size();
			StringBuffer sb = new StringBuffer();
			List<String> contertedParamNamaList = new ArrayList<String>();
			
			String patternStr = "(:PARAM_(((?!:PARAM_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr +="|(:V_(((?!:V_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr += "|(:(((?!:)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);

			String patternStr1 = "\\bSET\\b|\\bWHERE\\b|\\bAND\\b|\\bOR\\b|\\bLIKE\\b|\\bBETWEEN\\b|\\bCASE\\b|\\bDECODE\\b|\\bNVL\\b|\\bNVL2\\b";
			patternStr1 +="|\\bWHEN\\b|\\bTHEN\\b|\\bELSE\\b|\\bEXIST\\b|\\bNOT\\b|\\bNULL\\b";
			patternStr1 +="|\\bREPLACE\\b|\\bTO_CHAR\\b|\\bTO_NUMBER\\b|\\bTO_DATE\\b|=|>|<|>=|<=|<>|!=|\\(|\\)|,";
			String patternStr2 = "(\\w+(?!\\.)\\w+)";
//			String patternStr3 = "\\?";
			Pattern replacePattern = Pattern.compile(patternStr1.toString(),Pattern.CASE_INSENSITIVE);
			Pattern pattern1 = Pattern.compile(patternStr2);
			
			String rowStr = null;
			Matcher matcher = null;
			Matcher matcher1 = null;

			//System.out.println("-----------------------TO_BE��  ��ȯ ���� ����--------------------------------------------------");
			sb.append("<ColumnInfo>\n");
			for(int i=0;i<rowCount;i++)
			{
				rowStr = rowStringList.get(i);
				
				if(rowStr.indexOf("?") > -1)		//���� �Ķ���͸� '?' �� ǥ���� ���
				{
					//�࿡ �ִ� ��� SQL ����� �� �Լ�,�񱳿����� ����
					//String replacingRowStr = rowStr.toUpperCase().replaceAll(patternStr1," ");
					String replacingRowStr = replacePattern.matcher(rowStr).replaceAll(" ");
//					String replacedParamStr = null;
					
//					System.out.println("��ȯ�� �� : "+replacingRowStr);
					matcher1 = pattern1.matcher(replacingRowStr);
					
					//SQL �񱳿����ڸ� �������� ���� �񱳴���� Į���� �Ķ���͸����� ó����.
					if(matcher1.find())
					{
						String param = matcher1.group();
						param = convert2CamelCase(param);
//						replacedParamStr = "#{"+param+"}";
						
						
						//�ߺ��� �Ķ���� �Է� ����
						if(!contertedParamNamaList.contains(param))
						{
							sb.append("  <Column id=\""+param+"\" type=\"STRING\" size=\"256\"/>\n");
							contertedParamNamaList.add(param);
						}
					}
					
//					String replacedRowStr = rowStr.replaceAll(patternStr3, replacedParamStr);
//					System.out.println(replacedRowStr);
				}else if(  rowStr.toUpperCase().indexOf(":PARAM_") > -1 
						||rowStr.toUpperCase().indexOf(":V_") > -1 
						||rowStr.toUpperCase().indexOf(":v_") > -1 
						||rowStr.toUpperCase().indexOf(":") > -1)	//���� �Ķ���͸� ':' �Ķ���͸�(����Ŭ ���ε� ���� ���)���� ǥ���� ���
				{
					//����Ŭ ���ε� �������� �Ķ���ͷ� �ν��ؼ� ó����.
					matcher = pattern.matcher(rowStr);
					String replacingRowStr = new String(rowStr);
					
					while(matcher.find())
					{
						//���ε� prefix ���ڸ� ����
						//String replacedParamStr = matcher.group().toUpperCase().replaceAll(":PARAM_|:V_|:","");
						String replacedParamStr = matcher.group().replaceAll(":PARAM_|:V_|:param_|:v_|:","");
						replacedParamStr = convert2CamelCase(replacedParamStr);
						replacingRowStr = replacingRowStr.replaceFirst(patternStr,"#{"+replacedParamStr+"}");
						
						//�ߺ��� �Ķ���� �Է� ����
						if(!contertedParamNamaList.contains(replacedParamStr))
						{
							sb.append("  <Column id=\""+replacedParamStr+"\" type=\"STRING\" size=\"256\"/>\n");
							contertedParamNamaList.add(replacedParamStr);
						}
					}
//					System.out.println(replacingRowStr);
				}else
				{
					//�Ķ���� ���ε��� ������� ���� �״�� ���
//					System.out.println(rowStr);
				}

				rowStr = null;
				matcher = null;
				matcher1 = null;
			}
			sb.append("</ColumnInfo>\n");
//			System.out.println("-----------------------TO_BE�� ��ȯ ���� ��--------------------------------------------------\n");
			System.out.println("-----------------------ds_search�� �����ͼ� ����--------------------------------------------------");
			System.out.println(sb.toString());
			System.out.println("-----------------------ds_search�� �����ͼ� ��--------------------------------------------------");
		}
	}

	/**
	 * ���� ���ϳ����� ���� ���ε� ������ ������� �ػ�ũ�ο� �����ͼ� ����
	 * @param rowStringList
	 */
	public void genDataSetFromQueryString(List<String> rowStringList)
	{
		if(rowStringList != null && rowStringList.size() > 0)
		{
			int rowCount = rowStringList.size();
			StringBuffer sb = new StringBuffer();
			List<String> contertedParamNamaList = new ArrayList<String>();
			
			String patternStr = "(:PARAM_(((?!:PARAM_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr +="|(:V_(((?!:V_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr += "|(:(((?!:)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);

			String patternStr1 = "\\bSET\\b|\\bWHERE\\b|\\bAND\\b|\\bOR\\b|\\bLIKE\\b|\\bBETWEEN\\b|\\bCASE\\b|\\bDECODE\\b|\\bNVL\\b|\\bNVL2\\b";
			patternStr1 +="|\\bWHEN\\b|\\bTHEN\\b|\\bELSE\\b|\\bEXIST\\b|\\bNOT\\b|\\bNULL\\b";
			patternStr1 +="|\\bREPLACE\\b|\\bTO_CHAR\\b|\\bTO_NUMBER\\b|\\bTO_DATE\\b|=|>|<|>=|<=|<>|!=|\\(|\\)|,";
			String patternStr2 = "(\\w+(?!\\.)\\w+)";
//			String patternStr3 = "\\?";
			Pattern replacePattern = Pattern.compile(patternStr1.toString(),Pattern.CASE_INSENSITIVE);
			Pattern pattern1 = Pattern.compile(patternStr2);
			
			String rowStr = null;
			Matcher matcher = null;
			Matcher matcher1 = null;

			//System.out.println("-----------------------TO_BE��  ��ȯ ���� ����--------------------------------------------------");
			sb.append("<ColumnInfo>\n");
			for(int i=0;i<rowCount;i++)
			{
				rowStr = rowStringList.get(i);
				
				if(rowStr.indexOf("?") > -1)		//���� �Ķ���͸� '?' �� ǥ���� ���
				{
					//�࿡ �ִ� ��� SQL ����� �� �Լ�,�񱳿����� ����
					//String replacingRowStr = rowStr.toUpperCase().replaceAll(patternStr1," ");
					String replacingRowStr = replacePattern.matcher(rowStr).replaceAll(" ");
//					String replacedParamStr = null;
					
//					System.out.println("��ȯ�� �� : "+replacingRowStr);
					matcher1 = pattern1.matcher(replacingRowStr);
					
					//SQL �񱳿����ڸ� �������� ���� �񱳴���� Į���� �Ķ���͸����� ó����.
					if(matcher1.find())
					{
						String param = matcher1.group();
						param = convert2CamelCase(param);
//						replacedParamStr = "#{"+param+"}";
						
						
						//�ߺ��� �Ķ���� �Է� ����
						if(!contertedParamNamaList.contains(param))
						{
							sb.append("  <Column id=\""+param+"\" type=\"STRING\" size=\"256\"/>\n");
							contertedParamNamaList.add(param);
						}
					}
					
//					String replacedRowStr = rowStr.replaceAll(patternStr3, replacedParamStr);
//					System.out.println(replacedRowStr);
				}else if(  rowStr.toUpperCase().indexOf(":PARAM_") > -1 
						||rowStr.toUpperCase().indexOf(":V_") > -1 
						||rowStr.toUpperCase().indexOf(":v_") > -1 
						||rowStr.toUpperCase().indexOf(":") > -1)	//���� �Ķ���͸� ':' �Ķ���͸�(����Ŭ ���ε� ���� ���)���� ǥ���� ���
				{
					//����Ŭ ���ε� �������� �Ķ���ͷ� �ν��ؼ� ó����.
					matcher = pattern.matcher(rowStr);
					String replacingRowStr = new String(rowStr);
					
					while(matcher.find())
					{
						//���ε� prefix ���ڸ� ����
						//String replacedParamStr = matcher.group().toUpperCase().replaceAll(":PARAM_|:V_|:","");
						String replacedParamStr = matcher.group().replaceAll(":PARAM_|:V_|:param_|:v_|:","");
						replacedParamStr = convert2CamelCase(replacedParamStr);
						replacingRowStr = replacingRowStr.replaceFirst(patternStr,"#{"+replacedParamStr+"}");
						
						//�ߺ��� �Ķ���� �Է� ����
						if(!contertedParamNamaList.contains(replacedParamStr))
						{
							sb.append("  <Column id=\""+replacedParamStr+"\" type=\"STRING\" size=\"256\"/>\n");
							contertedParamNamaList.add(replacedParamStr);
						}
					}
//					System.out.println(replacingRowStr);
				}else
				{
					//�Ķ���� ���ε��� ������� ���� �״�� ���
//					System.out.println(rowStr);
				}

				rowStr = null;
				matcher = null;
				matcher1 = null;
			}
			sb.append("</ColumnInfo>\n");
//			System.out.println("-----------------------TO_BE�� ��ȯ ���� ��--------------------------------------------------\n");
			System.out.println("-----------------------ds_search�� �����ͼ� ����--------------------------------------------------");
			System.out.println(sb.toString());
			System.out.println("-----------------------ds_search�� �����ͼ� ��--------------------------------------------------");
		}
		
		
	}

	/**
	 * Glue �����ӿ�ũ�� ������ ������
	 * �������� ���ε� �����κ��� DTO �÷����� ����
	 * �⺻������ ���ε� ������ ���ڿ��� �ν��Ѵ�.
	 * �������� ��ȸĮ��(=���Į����)���� ����Ʈ��
	 * ���ε� ������ Į������ ������ ��ȸĮ�� ������ �ν�(�ߺ�������).
	 * @param sqlRowStringList - ���������� ����� ���ڿ� ����Ʈ
	 * @param columnList - �������� ��ȸĮ��(=���Į����)���� ����Ʈ
	 */
	public List<ColumnInfo> genSearchParamLayoutFromQueryString2(List<String> sqlRowStringList,List<ColumnInfo> columnList)
	{
		if(sqlRowStringList != null && sqlRowStringList.size() > 0)
		{
			int rowCount = sqlRowStringList.size();

			String patternStr = "(:PARAM_(((?!:PARAM_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr +="|(:V_(((?!:V_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr += "|(:(((?!:)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);

			StringBuffer patternStr1 = new StringBuffer();
			patternStr1.append("\\bSET\\b|\\bWHERE\\b|\\bAND\\b|\\bOR\\b|\\bLIKE\\b|\\bBETWEEN\\b|\\bCASE\\b|\\bDECODE\\b|\\bNVL\\b|\\bNVL2\\b");
			patternStr1.append("|\\bWHEN\\b|\\bTHEN\\b|\\bELSE\\b|\\bEXIST\\b|\\bNOT\\b|\\bNULL\\b");
			patternStr1.append("|\\bREPLACE\\b|\\bTO_CHAR\\b|\\bTO_NUMBER\\b|\\bTO_DATE\\b");
			String patternStr2 = "(\\w+(?!\\.)\\w+)";
			String patternStr3 = "\\?";
			Pattern replacePattern = Pattern.compile(patternStr1.toString(),Pattern.CASE_INSENSITIVE);
			Pattern pattern1 = Pattern.compile(patternStr2);
			
			String rowStr = null;
			Matcher matcher = null;
			Matcher matcher1 = null;

			System.out.println("-----------------------TO-BE ���� ���� ����--------------------------------------------------");

			for(int i=0;i<rowCount;i++)
			{
				rowStr = sqlRowStringList.get(i);
				
				if(rowStr.indexOf("?") > -1)		//���� �Ķ���͸� '?' �� ǥ���� ���
				{
					//�࿡ �ִ� ��� SQL ����� �� �Լ�,�񱳿����� ����(�������� ġȯ)
//					String replacingRowStr = rowStr.toUpperCase().replaceAll(patternStr1.toString()," ");
					String replacingRowStr = replacePattern.matcher(rowStr).replaceAll(" ");
					String replacedParamStr = null;

//					System.out.println("��ȯ�� �� : "+replacingRowStr);
					matcher1 = pattern1.matcher(replacingRowStr);
					
					//SQL �񱳿����ڸ� �������� ���� �񱳴�� Į���� �Ķ���͸����� ó����.
					//(���� �񱳴�� Į���� ī��ǥ������� ��ȯ�ѵ� �Ķ���͸����� ���)
					if(matcher1.find())
					{
						String param = matcher1.group();
						param = convert2CamelCase(param);
						replacedParamStr = "?";

						paramList.add(param);	//Glue Activity Setting Param ���

						//�ߺ��� �Ķ���� �Է� ����
						if(!isDuplicationColumnInfo(columnList,param))
						{
							columnList.add(new ColumnInfo(param, "java.lang.String"));
						}
					}

					String replacedRowStr = rowStr.replaceAll(patternStr3, replacedParamStr);
					System.out.println(replacedRowStr);
				}else if(rowStr.toUpperCase().indexOf(":PARAM_") > -1 
						||rowStr.toUpperCase().indexOf(":V_") > -1 
						||rowStr.toUpperCase().indexOf(":v_") > -1 
						||rowStr.toUpperCase().indexOf(":") > -1)	//���� �Ķ���͸� ':' �Ķ���͸�(����Ŭ ���ε� ���� ���)���� ǥ���� ���
				{
					//����Ŭ ���ε� �������� �Ķ���ͷ� �ν��ؼ� ó����.
					matcher = pattern.matcher(rowStr);
					String replacedRowStr = new String(rowStr);
					
					while(matcher.find())
					{
						//���ε� prefix ���ڸ� ����
						String replacedParamStr = matcher.group().replaceAll(":PARAM_|:V_|:v_|:","");
						replacedParamStr = convert2CamelCase(replacedParamStr);
						replacedRowStr = replacedRowStr.replaceFirst(patternStr,"?");

						paramList.add(replacedParamStr);	//Glue Activity Setting Param ���

						//�ߺ��� Į������ �Է� ����
						if(!isDuplicationColumnInfo(columnList,replacedParamStr))
						{
							columnList.add(new ColumnInfo(replacedParamStr, "java.lang.String"));
						}
					}
					System.out.println(replacedRowStr);
				}else
				{
					//�Ķ���� ���ε��� ������� ���� �״�� ���
					System.out.println(rowStr);
				}

				rowStr = null;
				matcher = null;
				matcher1 = null;
			}

			System.out.println("\n-----------------------Glue Activity Setting �Ķ���� ����Ʈ-------------------------");
			StringBuffer glueActivitySettingParams = new StringBuffer();
			int paramListCount = paramList.size();	//Glue Activity Setting Param ���
			glueActivitySettingParams.append("class\t\t\tcom.cosmos.biz.activity.nexacro.GlueNexacroSearch\n");
			glueActivitySettingParams.append("DataSet\t\t\tds_Search\n");
			glueActivitySettingParams.append("dao\t\t\t\ttest-dao\n");
			glueActivitySettingParams.append("sql-key\t\t\tXXXXXXXX.selectXX\n");
			glueActivitySettingParams.append("param-count\t\t"+paramListCount+"\n");
			for(int i=0;i<paramListCount;i++)
			{
				String param = paramList.get(i);
				glueActivitySettingParams.append("param"+i+"\t\t\t"+param+"\n");
			}
			glueActivitySettingParams.append("result-key\t\t\tds_Output\n");
			System.out.println(glueActivitySettingParams.toString());

			System.out.println("\n-----------------------TO-BE ���� ���� ��--------------------------------------------------\n");

			paramList.clear();
		}
		return columnList;
	}

	/**
	 * �������� ���ε� �����κ��� DTO �÷����� ����(mybatis��)
	 * �⺻������ ���ε� ������ ���ڿ��� �ν��Ѵ�.
	 * �������� ��ȸĮ��(=���Į����)���� ����Ʈ��
	 * ���ε� ������ Į������ ������ ��ȸĮ�� ������ �ν�(�ߺ�������).
	 * @param sqlRowStringList - ���������� ����� ���ڿ� ����Ʈ
	 * @param columnList - �������� ��ȸĮ��(=���Į����)���� ����Ʈ
	 */
	public List<ColumnInfo> genSearchParamLayoutFromQueryString(List<String> sqlRowStringList,List<ColumnInfo> columnList)
	{
		if(sqlRowStringList != null && sqlRowStringList.size() > 0)
		{
			int rowCount = sqlRowStringList.size();

			String patternStr = "(:PARAM_(((?!:PARAM_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr +="|(:V_(((?!:V_)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			patternStr += "|(:(((?!:)|(?!,)|(?! )|(?!\\))|(?!\\|))\\w)*)";
			Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);

			StringBuffer patternStr1 = new StringBuffer();
			patternStr1.append("\\bSET\\b|\\bWHERE\\b|\\bAND\\b|\\bOR\\b|\\bLIKE\\b|\\bBETWEEN\\b|\\bCASE\\b|\\bDECODE\\b|\\bNVL\\b|\\bNVL2\\b");
			patternStr1.append("|\\bWHEN\\b|\\bTHEN\\b|\\bELSE\\b|\\bEXIST\\b|\\bNOT\\b|\\bNULL\\b");
			patternStr1.append("|\\bREPLACE\\b|\\bTO_CHAR\\b|\\bTO_NUMBER\\b|\\bTO_DATE\\b");
			String patternStr2 = "(\\w+(?!\\.)\\w+)";
			String patternStr3 = "\\?";
			Pattern replacePattern = Pattern.compile(patternStr1.toString(),Pattern.CASE_INSENSITIVE);
			Pattern pattern1 = Pattern.compile(patternStr2);
			
			String rowStr = null;
			Matcher matcher = null;
			Matcher matcher1 = null;

			System.out.println("-----------------------TO-BE ���� ���� ����--------------------------------------------------");

			for(int i=0;i<rowCount;i++)
			{
				rowStr = sqlRowStringList.get(i);
				
				if(rowStr.indexOf("?") > -1)		//���� �Ķ���͸� '?' �� ǥ���� ���
				{
					//�࿡ �ִ� ��� SQL ����� �� �Լ�,�񱳿����� ����(�������� ġȯ)
//					String replacingRowStr = rowStr.toUpperCase().replaceAll(patternStr1.toString()," ");
					String replacingRowStr = replacePattern.matcher(rowStr).replaceAll(" ");
					String replacedParamStr = null;

//					System.out.println("��ȯ�� �� : "+replacingRowStr);
					matcher1 = pattern1.matcher(replacingRowStr);
					
					//SQL �񱳿����ڸ� �������� ���� �񱳴�� Į���� �Ķ���͸����� ó����.
					//(���� �񱳴�� Į���� ī��ǥ������� ��ȯ�ѵ� �Ķ���͸����� ���)
					if(matcher1.find())
					{
						String param = matcher1.group();
						param = convert2CamelCase(param);
						replacedParamStr = "#{"+param+"}";

						//�ߺ��� �Ķ���� �Է� ����
						if(!isDuplicationColumnInfo(columnList,param))
						{
							columnList.add(new ColumnInfo(param, "java.lang.String"));
						}
					}

					String replacedRowStr = rowStr.replaceAll(patternStr3, replacedParamStr);
					System.out.println(replacedRowStr);
				}else if(rowStr.toUpperCase().indexOf(":PARAM_") > -1 
						||rowStr.toUpperCase().indexOf(":V_") > -1 
						||rowStr.toUpperCase().indexOf(":v_") > -1 
						||rowStr.toUpperCase().indexOf(":") > -1)	//���� �Ķ���͸� ':' �Ķ���͸�(����Ŭ ���ε� ���� ���)���� ǥ���� ���
				{
					//����Ŭ ���ε� �������� �Ķ���ͷ� �ν��ؼ� ó����.
					matcher = pattern.matcher(rowStr);
					String replacedRowStr = new String(rowStr);
					
					while(matcher.find())
					{
						//���ε� prefix ���ڸ� ����
						String replacedParamStr = matcher.group().replaceAll(":PARAM_|:V_|:v_|:","");
						replacedParamStr = convert2CamelCase(replacedParamStr);
						replacedRowStr = replacedRowStr.replaceFirst(patternStr,"#{"+replacedParamStr+"}");
						
						//�ߺ��� Į������ �Է� ����
						if(!isDuplicationColumnInfo(columnList,replacedParamStr))
						{
							columnList.add(new ColumnInfo(replacedParamStr, "java.lang.String"));
						}
					}
					System.out.println(replacedRowStr);
				}else
				{
					//�Ķ���� ���ε��� ������� ���� �״�� ���
					System.out.println(rowStr);
				}

				rowStr = null;
				matcher = null;
				matcher1 = null;
			}
			System.out.println("-----------------------TO-BE ���� ���� ��--------------------------------------------------\n");

		}
		return columnList;
	}

	private boolean isDuplicationColumnInfo(List<ColumnInfo> columnList,String columnName)
	{
		boolean isDup = false;
		int columnListCount = columnList.size();

		for(int i=0;i<columnListCount;i++)
		{
			ColumnInfo ci = columnList.get(i);
			if(ci.getColumnName().equals(columnName))
			{
				isDup = true;
				break;
			}
		}

		return isDup;
	}

	public void getColumnInfo(String sql, List<ColumnInfo> columnList)
	{
		Connection conn = null;
		try
		{
			
			//conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.81:1850:PGSCM", "PT289985", "cosmos8!");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.31.117.83:1750:DGSCM2", "POSSCM", "posco123");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@10.132.32.52:1751:DOERP", "POSSCM_NEW", "new#scm");
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "study", "study123");
			PreparedStatement psmt = conn.prepareStatement(sql);

			ParameterMetaData pmd = psmt.getParameterMetaData();
			int parameterCount = pmd.getParameterCount();
			System.out.println("���ε� ���� ���� : "+parameterCount);
			
			for(int i=1;i<=parameterCount;i++)
			{
//				int pMode = pmd.getParameterMode(i);
				
//				System.out.println("Name: " + pmd.getParameterType(i));
//				System.out.println("TYPE: " + pmd.getParameterTypeName(i));
//				System.out.println("Class Name: " + pmd.getParameterClassName(i));
				psmt.setString(i, "1");
//				if(ParameterMetaData.parameterModeIn == pMode)
//				{
//					System.out.println("in");
//				}else if(ParameterMetaData.parameterModeOut == pMode)
//				{
//					System.out.println("out");
//				}else if(ParameterMetaData.parameterModeInOut == pMode)
//				{
//					System.out.println("inout");
//				}else if(ParameterMetaData.parameterModeUnknown == pMode)
//				{
//					System.out.println("unknown");
//				}
			}
			ResultSet rs = psmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			
			int columnCount = rsmd.getColumnCount();

			Class<?> cls = null;
			for(int i=1;i<=columnCount;i++)
			{
//				String columnName = rsmd.getColumnName(i);
				String columnLabel = rsmd.getColumnLabel(i);
//				String columnTypeName = rsmd.getColumnTypeName(i);

				int columnType = rsmd.getColumnType(i);
				if( Types.BIT == columnType || 
					Types.BOOLEAN == columnType || 
					Types.TINYINT == columnType || 
					Types.SMALLINT == columnType || 
					Types.INTEGER == columnType || 
					Types.BIGINT == columnType || 
					Types.FLOAT == columnType || 
					Types.REAL == columnType || 
					Types.DOUBLE == columnType || 
					Types.NUMERIC == columnType || 
					Types.DECIMAL == columnType
				)
				{
				    cls = BigDecimal.class;
				}else if(	Types.CHAR == columnType || 
							Types.NCHAR == columnType || 
							Types.VARCHAR == columnType || 
							Types.NVARCHAR == columnType || 
							Types.LONGVARCHAR == columnType || 
							Types.LONGNVARCHAR == columnType || 
							Types.CLOB == columnType || 
							Types.NCLOB == columnType || 
							Types.ROWID == columnType || 
							Types.REF == columnType || 
							Types.DATALINK == columnType || 
							Types.SQLXML == columnType || 
							Types.JAVA_OBJECT == columnType || 
							Types.NULL == columnType || 
							Types.OTHER == columnType || 
							Types.DISTINCT == columnType || 
							Types.STRUCT == columnType
				)
				{
					cls = String.class;
				}else if(	Types.DATE == columnType || 
							Types.TIME == columnType || 
							Types.TIMESTAMP == columnType
				)
				{
					cls = Timestamp.class;
				}
				else if(	Types.BINARY == columnType || 
							Types.VARBINARY == columnType || 
							Types.LONGVARBINARY == columnType || 
							Types.ARRAY == columnType || 
							Types.BLOB == columnType
				)
				{
					cls = Array.class;
				}

				//System.out.println("COLUMN NAME: "+columnName+", COLUMN LABEL: "+columnLabel+", TYPE: "+cls.getName()+", TYPE NAME: "+columnTypeName);

				//�ߺ��� Į������ �Է� ����
				if(!isDuplicationColumnInfo(columnList,convert2CamelCase(columnLabel)))
				{
					columnList.add(new ColumnInfo(convert2CamelCase(columnLabel),cls.getName()));
				}
			}
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public String getSqlFileContents(String sqlFileName)
	{
		StringBuffer sb = new StringBuffer();
		String sqlContents = "";
//		String fileSeparator = File.separator;
		URL fileUrl = getClass().getResource(sqlFileName);

		System.out.println("fileUrl.getPath()="+fileUrl.getPath());
		File sqlFile = new File(fileUrl.getPath());
		
		if(sqlFile.exists())
		{
			FileInputStream fis = null;

			Scanner sc = null;
			try
			{
//				String lineSeparator = System.getProperty("line.separator");
				fis = new FileInputStream(sqlFile);
				
				FileChannel inc = fis.getChannel();
				MappedByteBuffer mb = inc.map(MapMode.READ_ONLY, 0, inc.size());
				Charset charset = Charset.forName("UTF-8");
				CharsetDecoder decoder = charset.newDecoder();
				CharBuffer charBuffer = decoder.decode(mb);

				sc = new Scanner(charBuffer);	//.useDelimiter(lineSeparator);
				while(sc.hasNext())
				{
					String content = sc.nextLine();
					sb.append(content+"\n");
				}

				sqlContents = sb.toString().trim();
				sc.close();
			}catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		if(!sqlContents.equals(""))
		{
			System.out.println("-----------------------AS-IS ���� ���� ����--------------------------------------------------");
			System.out.println(sqlContents);
			System.out.println("-----------------------AS-IS ���� ���� ��----------------------------------------------------\n");
		}

		return sqlContents;
	}

	public boolean genSqlDTO(List<ColumnInfo> columnList)
	{
		StringBuffer sb = new StringBuffer();
		int columnListCount = columnList.size();
		List<String> importingClassList = new ArrayList<String>();

		//import ������ �� CLASS ã��
		for(int i=0;i<columnListCount;i++)
		{
			ColumnInfo ci = columnList.get(i);
			String javaDataTypeName = ci.getColumnJavaDataTypeName();
			if(javaDataTypeName.indexOf("java.lang") == -1 && !importingClassList.contains(javaDataTypeName))
			{
				importingClassList.add(javaDataTypeName);
			}
		}

		sb.append("package com.sql.parse;\n\n");

		//import ���� �߰�
		int importingClassListCount = importingClassList.size();
		for(int i=0;i<importingClassListCount;i++)
		{
			String importStr = "import "+importingClassList.get(i)+";\n";
			sb.append(importStr);
		}

		sb.append("\n");

		sb.append("public class GenDTO\n{\n");

		//����(������Ƽ ����κ�)
		for(int i=0;i<columnListCount;i++)
		{
			ColumnInfo ci = columnList.get(i);
			String columnName = ci.getColumnName();
			String classFullName = ci.getColumnJavaDataTypeName();
			String className = classFullName.substring(classFullName.lastIndexOf(".")+1);
			sb.append("    private "+className+" "+columnName+";\n");
		}

		sb.append("\n");

		//����(�޼ҵ� ����κ�)
		for(int i=0;i<columnListCount;i++)
		{
			ColumnInfo ci = columnList.get(i);
			String columnName = ci.getColumnName();
			String classFullName = ci.getColumnJavaDataTypeName();
			String className = classFullName.substring(classFullName.lastIndexOf(".")+1);

			String getterName = columnName.substring(0,1).toUpperCase()+columnName.substring(1);

			//������Ƽ getter
			sb.append("    public "+className+" get"+getterName+"()\n    {\n        return "+columnName+";\n    }\n");

			//������Ƽ setter
			sb.append("    public void set"+getterName+"("+className+" "+columnName+")\n    {\n        this."+columnName+" = "+columnName+";\n    }\n");
		}

		sb.append("\n}");

		System.out.println("-----------------------TO-BE DTO ���� ����--------------------------------------------------");
		System.out.println(sb.toString());
		System.out.println("-----------------------TO-BE DTO ���� ��----------------------------------------------------\n");

		//GenDTO.java ���Ͽ� ����
        FileChannel fout = null;
        FileOutputStream fos = null;
        try
        {
        	String binPath = this.getClass().getResource("").getPath();
        	String srcPath = binPath.replace("/bin/", "/src/");
            File f = new File(srcPath+"GenDTO.java");
            System.out.println("filePath="+f.getPath());

            fos = new FileOutputStream(f);
            fout = fos.getChannel();

            String fileContents = new String(sb.toString().getBytes(),"UTF-8");

            ByteBuffer buf = ByteBuffer.wrap(fileContents.getBytes());
            fout.write(buf);
        }catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }finally
        {
        	try{if(fout != null)fout.close();}catch(IOException ioe){}
            try{if(fos != null)fos.close();}catch(IOException ioe){}
        }

		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SqlQueryParser sqlParser = new SqlQueryParser();

		List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

		//���� ����(���� ��������)�� ������ ���� �÷���
//		List<String> allFileRowStringList = new ArrayList<String>();

		//���� ������ �����ϴ� ���� DTO ������ ���ϸ� �߰�
//		sqlFileName = new String[]{"sqlContents1.sql"};
//		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql"};
//		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql","sqlContents3.sql"};
//		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql","sqlContents3.sql","sqlContents4.sql"};
//		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql","sqlContents3.sql","sqlContents4.sql","sqlContents5.sql"};
//		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql","sqlContents3.sql","sqlContents4.sql","sqlContents5.sql","sqlContents6.sql"};
//		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql","sqlContents3.sql","sqlContents4.sql","sqlContents5.sql","sqlContents6.sql","sqlContents7.sql"};
		sqlFileName = new String[]{"sqlContents1.sql","sqlContents2.sql","sqlContents3.sql","sqlContents4.sql","sqlContents5.sql","sqlContents6.sql","sqlContents7.sql","sqlContents8.sql"};
		
		int sqlFileNameCount = sqlFileName.length;

		for(int i=0;i<sqlFileNameCount;i++)
		{
			String sqlStr = sqlParser.getSqlFileContents(sqlFileName[i]);
			
			if(!sqlStr.equals(""))
			{
				//������Ÿ�����κ��� ��ȸ�� Į��(������Ƽ)���� ����
//				sqlParser.getColumnInfo(sqlStr,columnList);
				
				//AS-IS ���� �������� ���� ������ ������.
				List<String> rowStringList = sqlParser.getRowStringList(sqlFileName[i]);

//				allFileRowStringList.addAll(rowStringList);

				//�ػ�ũ�� ��ȸ�� �����ͼ� ����
//				sqlParser.genDataSetFromQueryString(rowStringList);

				//������ ���ε� ���������κ��� Į��(������Ƽ)���� ����(MyBatis��)
//				sqlParser.genSearchParamLayoutFromQueryString(rowStringList,columnList);

				//������ ���ε� ���������κ��� Į��(������Ƽ)���� ����(Glue��)
				sqlParser.genSearchParamLayoutFromQueryString2(rowStringList,columnList);
			}
		}

		//�ػ�ũ�� ��ȸ�� �����ͼ� ����(���� ������ �����ϴ� ���� �����ͼ�)
		sqlParser.genDataSetFromColumnList(columnList);

		//����� Į��(���� ��Ÿ���� + ���ε� ���� �÷�����)���� �������� TO-BE�� DTO����
//		sqlParser.genSqlDTO(columnList);
	}

	private void genDataSetFromColumnList(List<ColumnInfo> columnList)
	{
		int columnListCount = columnList.size();
		StringBuffer sb = new StringBuffer();

		sb.append("<ColumnInfo>\n");
		for(int i=0;i<columnListCount;i++)
		{
			ColumnInfo columnInfo = columnList.get(i);
			String columnName = columnInfo.getColumnName();
			String columnJavaDataTypeName = columnInfo.getColumnJavaDataTypeName();
			String dataTypeName = columnJavaDataTypeName.substring(columnJavaDataTypeName.lastIndexOf(".")+1);
			sb.append("  <Column id=\""+columnName+"\" type=\""+dataTypeName.toUpperCase()+"\" size=\"256\"/>\n");
			
		}
		sb.append("</ColumnInfo>\n");
		System.out.println("-----------------------ds_Search�� ���� �����ͼ� ����(�������� "+sqlFileName.length+"�� - "+Arrays.toString(sqlFileName)+") --------------------------------------------------");
		System.out.println(sb.toString());
		System.out.println("-----------------------ds_Search�� ���� �����ͼ� ��-------------------------------------------------------------------------------------------------------------------");
	}
	/**
	 * underscore ('_') �� ���ԵǾ� �ִ� ���ڿ��� Camel Case ( ��Ÿ�� 
     * ǥ��� - �ܾ��� ����ÿ� �빮�ڷ� �����ϴ� ����. ������ �ҹ���) �� ��ȯ���ִ� 
     * utility �޼��� ('_' �� ��Ÿ���� �ʰ� ù���ڰ� �빮���� ��쵵 ��ȯ ó�� 
     * ��.)
	 * @param underScore 
     *        - '_' �� ���Ե� ������ 
	 * @return Camel ǥ��� ������ 
	 */
	public static String convert2CamelCase(String underScoreStr)
	{ 
		boolean nextUpper = false;
		int len = underScoreStr.length();
		StringBuffer result = new StringBuffer();

		if(underScoreStr != null && underScoreStr.length() > 0 && underScoreStr.indexOf('_') == -1 && Character.isLowerCase(underScoreStr.charAt(0)))
		{ 
			return underScoreStr; 
		}

		for(int i=0;i<len;i++)
		{
			char currentChar = underScoreStr.charAt(i);

			if(currentChar == '_')
			{
				nextUpper = true;
			}else
			{
				if(nextUpper)
				{
					result.append(Character.toUpperCase(currentChar));
					nextUpper = false;
				}else
				{
					result.append(Character.toLowerCase(currentChar));
				}
			}
		}
		return result.toString();
	}

	/**
	 * camel ��Ÿ���� ������ Ŭ���� ��������� �Ǵ� ȭ�������Ʈ���� DB�÷��� ��Ÿ�Ϸ� ��ȯ  
	 * FROM camel or pascal style TO db style using underscore 
	 * userName or UserName => USER_NAME
	 * @param str
	 * @return value
	 */
	public static String camelToDbStyle(String str)
	{
		String regex = "([a-z])([A-Z])";
		String replacement = "$1_$2";
		String value = "";
        value = str.replaceAll(regex, replacement).toUpperCase();
        return value;
	}
}