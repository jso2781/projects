package com.data.migration.config.parse;

import java.io.File;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.data.migration.config.model.MigrationDataInfo;
import com.data.migration.ui.main.AsIsDataMigrator;


public class MigrationDataInfoParser
{
	public MigrationDataInfoParser(){}

	public boolean saveMigrationDataInfoListToXml(List<MigrationDataInfo> migrationDataInfoList)
	{
		boolean isSaveSuccess = false;
//		System.out.println("SiteMapTreeNodeParser saveSiteMapTreeNodeToXml configTreeRootNode Name="+rootTreeNode.getUserObject().toString());

		String configDir = new File("config").getAbsolutePath();
		String configFileFullPath = null;

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			factory.setIgnoringElementContentWhitespace(true);
			Document doc = factory.newDocumentBuilder().newDocument();

			createElements(migrationDataInfoList, doc);

			// Save the document to disk...
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			//tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DOMSource domSource = new DOMSource(doc);

			configFileFullPath = URLDecoder.decode(configDir+File.separator+"MigrationDataInfoList.xml", "UTF-8");
			File siteMapFile = new File(configFileFullPath);
			StreamResult sr = new StreamResult(siteMapFile);
			tf.transform(domSource, sr);

			isSaveSuccess = true;

			factory = null;
			doc = null;
			tf = null;
			domSource = null;
			configDir = null;
			configFileFullPath = null;
			siteMapFile = null;
			sr = null;
		}catch (Exception ex)
		{
			ex.printStackTrace();
			isSaveSuccess = false;
		}
		
		return isSaveSuccess;
	}

	public static void main(String[] args) 
	{
		new MigrationDataInfoParser();
	}

	public List<MigrationDataInfo> getMigrationDataInfoList(JTable migrationTable)
	{
		return getMigrationDataInfoList(migrationTable,false);
	}

	public List<MigrationDataInfo> getMigrationDataInfoList(JTable migrationTable,boolean isCheckBox)
	{
		boolean isCheck = true;
		DefaultTableModel tm = (DefaultTableModel) migrationTable.getModel();
		
		int rowCount = tm.getRowCount();

		String asIsTableName = null;
		String asIsWhereClause = null;
		String toBeTableName = null;
		String modifyColumnValues = null;
		MigrationDataInfo mdi = null;

		List<MigrationDataInfo> migrationDataInfoList = new ArrayList<MigrationDataInfo>();

		for(int i=0;i<rowCount;i++)
		{
			if(isCheckBox)
			{
				isCheck = (Boolean) tm.getValueAt(i, 0);

				if(!isCheck)continue;
			}

			mdi = new MigrationDataInfo();
			asIsTableName = (String) tm.getValueAt(i, 2);
			asIsWhereClause = (String) tm.getValueAt(i, 3);
			toBeTableName = (String) tm.getValueAt(i, 4);
			modifyColumnValues = (String) tm.getValueAt(i, 5);

			mdi.setAsIsTableName(asIsTableName);
			mdi.setAsIsWhereClause(asIsWhereClause);
			mdi.setToBeTableName(toBeTableName);
			mdi.setModifyColumnValues(modifyColumnValues);

			migrationDataInfoList.add(mdi);
		}

		asIsTableName = null;
		asIsWhereClause = null;
		toBeTableName = null;
		modifyColumnValues = null;
		mdi = null;

		if(migrationDataInfoList.size() > 0)
		{
			return migrationDataInfoList;
		}else
		{
			migrationDataInfoList = null;
			return null;
		}
	}
	public void loadMigrationDataInfoList(JTable migrationTable,List<MigrationDataInfo> migrationDataInfoList)
	{
		int migrationDataInfoListCount = migrationDataInfoList.size();
		MigrationDataInfo mdi = null;

		DefaultTableModel dtm = (DefaultTableModel) migrationTable.getModel();
		dtm.setRowCount(0);

		for(int i=0;i<migrationDataInfoListCount;i++)
		{
			int rowCount = dtm.getRowCount();
			int rowSeq = rowCount == 0 ? 1 : (rowCount+1);

			mdi = migrationDataInfoList.get(i);

			dtm.addRow(new Object[]{false,rowSeq,mdi.getAsIsTableName(),mdi.getAsIsWhereClause(),mdi.getToBeTableName(),mdi.getModifyColumnValues()});
		}

		mdi = null;
	}

	public boolean checkMigrationDataInfoList(AsIsDataMigrator asIsDataMigrator,boolean isCheckBox)
	{
		boolean isCheck = true;
		int checkRowCount = 0;
		JTable migrationTable = asIsDataMigrator.getTbMigrationTableList();

		int rowCount = migrationTable.getRowCount();
		String asIsTableName = null;
		String asIsWhereClause = null;
		String toBeTableName = null;

		for(int i=0;i<rowCount;i++)
		{
			if(isCheckBox)
			{
				isCheck = (Boolean) migrationTable.getValueAt(i, 0);

				if(isCheck)
				{
					checkRowCount++;
				}else
				{
					continue;
				}
			}

			asIsTableName = (String) migrationTable.getValueAt(i, 2);
			asIsWhereClause = (String) migrationTable.getValueAt(i, 3);
			toBeTableName = (String) migrationTable.getValueAt(i, 4);

			if(asIsTableName == null || "".equals(asIsTableName))
			{
				JOptionPane.showMessageDialog(asIsDataMigrator, "AS-IS Table Name is Empty!", "WARNNING", JOptionPane.WARNING_MESSAGE);

				migrationTable.editCellAt(i,2);
				migrationTable.changeSelection(i, 2, false, false);
//				Component editor = migrationTable.getEditorComponent();
//				editor.requestFocusInWindow();


				return false;
			}else if(asIsWhereClause == null || "".equals(asIsWhereClause))
			{
				JOptionPane.showMessageDialog(asIsDataMigrator, "AS-IS Where Clause is Empty!", "WARNNING", JOptionPane.WARNING_MESSAGE);

				migrationTable.editCellAt(i,3);
				migrationTable.changeSelection(i, 3, false, false);
//				Component editor = migrationTable.getEditorComponent();
//				editor.requestFocusInWindow();

				return false;
			}else if(toBeTableName == null || "".equals(toBeTableName))
			{
				JOptionPane.showMessageDialog(asIsDataMigrator, "TO-BE Table Name is Empty!", "WARNNING", JOptionPane.WARNING_MESSAGE);

				migrationTable.editCellAt(i,4);
				migrationTable.changeSelection(i, 4, false, false);
//				Component editor = migrationTable.getEditorComponent();
//				editor.requestFocusInWindow();

				return false;
			}
		}

		asIsTableName = null;
		asIsWhereClause = null;
		toBeTableName = null;

		if(isCheckBox && checkRowCount == 0)
		{
			JOptionPane.showMessageDialog(asIsDataMigrator, "\uCCB4\uD06C\uD55C \uD589\uC774 \uC874\uC7AC\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.", "WARNNING", JOptionPane.WARNING_MESSAGE);
			return false;
		}else
		{
			return true;
		}
		
	}

	public boolean checkMigrationDataInfoList(AsIsDataMigrator asIsDataMigrator)
	{
		return checkMigrationDataInfoList(asIsDataMigrator, false);
	}

	public void createElements(List<MigrationDataInfo> migrationDataInfoList, Document doc)
	{
		Element rootElement = doc.createElement("MigrationDataInfoList");
		doc.appendChild(rootElement);

		if(migrationDataInfoList != null && migrationDataInfoList.size() > 0)
		{
			int migrationDataInfoListCount = migrationDataInfoList.size();
			MigrationDataInfo md = null;
			Element migrationDataInfoElement = null;
			Element asIsTableNameElement = null;
			Element asIsWhereClauseElement = null;
			Element toBeTableNameElement = null;
			Element modifyColumnValuesElement = null;
			for(int i=0;i<migrationDataInfoListCount;i++)
			{
				md = migrationDataInfoList.get(i);
				
				migrationDataInfoElement = doc.createElement("MigrationDataInfo");

				asIsTableNameElement = doc.createElement("AsIsTableName");
//				asIsTableNameElement.setTextContent(toXMLString(md.getAsIsTableName()));
				asIsTableNameElement.setTextContent(md.getAsIsTableName());
				migrationDataInfoElement.appendChild(asIsTableNameElement);

				asIsWhereClauseElement = doc.createElement("AsIsWhereClause");
//				asIsWhereClauseElement.setTextContent(toXMLString(md.getAsIsWhereClause()));
				asIsWhereClauseElement.setTextContent(md.getAsIsWhereClause());
				migrationDataInfoElement.appendChild(asIsWhereClauseElement);

				toBeTableNameElement = doc.createElement("ToBeTableName");
//				toBeTableNameElement.setTextContent(toXMLString(md.getToBeTableName()));
				toBeTableNameElement.setTextContent(md.getToBeTableName());
				migrationDataInfoElement.appendChild(toBeTableNameElement);

				modifyColumnValuesElement = doc.createElement("ModifyColumnValues");
//				modifyColumnValuesElement.setTextContent(toXMLString(md.getModifyColumnValues()));
				modifyColumnValuesElement.setTextContent(md.getModifyColumnValues());
				migrationDataInfoElement.appendChild(modifyColumnValuesElement);

				rootElement.appendChild(migrationDataInfoElement);
			}

			rootElement = null;
			migrationDataInfoElement = null;
			asIsTableNameElement = null;
			asIsWhereClauseElement = null;
			toBeTableNameElement = null;
			modifyColumnValuesElement = null;
		}
	}

	/**
	 * XML에 생성시 입력되는 문자열 중 특수문자를 변환시켜준다.
	 * @param input
	 * @return
	 */
	public String toXMLString(String input)
	{
		String xmlString = "";
		
		if(input != null)
		{
			CharBuffer cb = CharBuffer.wrap(input);
	
			while ( cb.hasRemaining())
			{
				char tempChar = cb.get();
	
//				if ( tempChar == '"' )
//				{
//					xmlString += "&quot;";
//				}else if ( tempChar == '&' )
//				{
//					xmlString += "&amp;";
//				}
//				else if ( tempChar == '\'' )
//				{
//					xmlString += "&apos;";
//				} 
//				else 
				if ( tempChar == '<' )
				{
					xmlString += "&lt;";
				} else if ( tempChar == '>' )
				{
					xmlString += "&gt;";
				}else
				{
					xmlString += tempChar;
				}
			}
		}
		return xmlString;
	}
}
