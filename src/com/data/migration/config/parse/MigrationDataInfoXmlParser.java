package com.data.migration.config.parse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.data.migration.config.model.MigrationDataInfo;

/**
 * 
 * @author JOSEONGOK
 *
 */
public class MigrationDataInfoXmlParser extends DefaultHandler
{
	private String xmlFileName;  
	private String strElementData;

	private List<MigrationDataInfo> migrationDataInfoList = new ArrayList<MigrationDataInfo>();
	private MigrationDataInfo migrationDataInfo = null;

	//태그 안 내용에 특수문자(<,>,&)가 있으면 잘리는 현상방지하기 위해 문자열버퍼 사용
	private StringBuffer asIsTableNameBuf = new StringBuffer();
	private StringBuffer asIsWhereClauseBuf = new StringBuffer();
	private StringBuffer toBeTableNameBuf = new StringBuffer();
	private StringBuffer modifyColumnValuesBuf = new StringBuffer();

	private boolean bAsIsTableName  = false;
	private boolean bAsIsWhereClause = false;
	private boolean bToBeTableName = false;
	private boolean bModifyColumnValues = false;

	public List<MigrationDataInfo> getMigrationDataInfoList()
	{
		return migrationDataInfoList;
	}

	public MigrationDataInfoXmlParser(String aXmlFileName)
	{
		xmlFileName="";  
		strElementData="";  
		this.xmlFileName = aXmlFileName;  

		parseDocument();
	}

	private void parseDocument()
	{
		//parse
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try
		{
			parser = factory.newSAXParser();
			parser.parse(new File(xmlFileName), this);
		}catch(ParserConfigurationException e)
		{
//			System.out.println("ParserConfig error");
		}catch(SAXException e)
		{
//			System.out.println("SAXException : xml not well formed");
		}catch (IOException e)
		{
//			System.out.println("IO error");
		}

		factory = null;
		parser = null;
	}

	@Override 
	public void startElement(String url,String localName,String elementName,Attributes attributes) throws SAXException
	{
		if("AsIsTableName".equals(elementName))
		{
			bAsIsTableName = true;
			asIsTableNameBuf.setLength(0);
		}

		if("AsIsWhereClause".equals(elementName))
		{
			bAsIsWhereClause = true;
			asIsWhereClauseBuf.setLength(0);
		}

		if("ToBeTableName".equals(elementName))
		{
			bToBeTableName=true;
			toBeTableNameBuf.setLength(0);
		}

		if("ModifyColumnValues".equals(elementName))
		{
			bModifyColumnValues=true;
			modifyColumnValuesBuf.setLength(0);
		}

		if(bAsIsTableName)
		{
			migrationDataInfo = new MigrationDataInfo();
		}

//		this.elementNm= elementName;
	}

	@Override 
	public void characters(char[] ac, int i, int j) throws SAXException
	{
		strElementData= new String(ac, i, j); 

		if(bAsIsTableName)
		{
			asIsTableNameBuf.append(strElementData);
		}else if(bAsIsWhereClause)
		{
			asIsWhereClauseBuf.append(strElementData);
		}else if(bToBeTableName)
		{
			toBeTableNameBuf.append(strElementData);
		}else if(bModifyColumnValues)
		{
			modifyColumnValuesBuf.append(strElementData);
		}

//		System.out.println("chas-m_ElementName-["+strElementData+"]");
	}

	public void endElement(String url, String localName, String elementName) throws SAXException
	{
		strElementData = strElementData.trim();

		if(bAsIsTableName)
		{
			migrationDataInfoList.add(migrationDataInfo);
			migrationDataInfo.setAsIsTableName(asIsTableNameBuf.toString());
		}else if(bAsIsWhereClause)
		{
			migrationDataInfo.setAsIsWhereClause(asIsWhereClauseBuf.toString());
		}else if(bToBeTableName)
		{
			migrationDataInfo.setToBeTableName(toBeTableNameBuf.toString());
		}else if(bModifyColumnValues)
		{
			migrationDataInfo.setModifyColumnValues(modifyColumnValuesBuf.toString());
		}

		if("AsIsTableName".equals(elementName)){bAsIsTableName = false;}
		if("AsIsWhereClause".equals(elementName))bAsIsWhereClause = false;
		if("ToBeTableName".equals(elementName)) bToBeTableName=false;
		if("ModifyColumnValues".equals(elementName)) bModifyColumnValues=false;

	}
}
