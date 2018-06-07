package com.data.migration.config.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MigrationDataInfo
{
	private boolean chk;
	private Integer seq;
	private String asIsTableName;
	private String asIsWhereClause;
	private String toBeTableName;
	private String modifyColumnValues;
	private List<Map<String,Object>> asIsTableDataList = null;
	private List<String> asIsColumnNameList = null;
	private List<String> toBeColumnNameList = null;
	private Map<String,Object> modifyColumnValuesMap = new HashMap<String,Object>();

	public MigrationDataInfo(){}

	public boolean isChk() {
		return chk;
	}

	public void setChk(boolean chk) {
		this.chk = chk;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getAsIsTableName() {
		return asIsTableName;
	}

	public void setAsIsTableName(String asIsTableName) {
		this.asIsTableName = asIsTableName != null ? asIsTableName.toUpperCase() : null;
	}

	public String getAsIsWhereClause() {
		return asIsWhereClause;
	}

	public void setAsIsWhereClause(String asIsWhereClause) {
		this.asIsWhereClause = asIsWhereClause;
	}

	public String getToBeTableName() {
		return toBeTableName;
	}

	public void setToBeTableName(String toBeTableName) {
		this.toBeTableName = toBeTableName != null ? toBeTableName.toUpperCase() : null;
	}

	public void setModifyColumnValues(String modifyColumnValues)
	{
		this.modifyColumnValues = modifyColumnValues;

		if(modifyColumnValues != null && !"".equals(modifyColumnValues))
		{
			String key = null;
			String value = null;
			StringTokenizer st = null;

			String keyValuesArr[] = modifyColumnValues.split(",");

			int keyValuesArrCount = keyValuesArr.length;

			for(int i=0;i<keyValuesArrCount;i++)
			{
				st = new StringTokenizer(keyValuesArr[i], "=");

				if(st.countTokens() == 2)
				{
					key = st.nextToken().toUpperCase().trim();
					value = st.nextToken().trim();
					modifyColumnValuesMap.put(key, value);
				}

				st = null;
				key = null;
				value = null;
			}
		}
	}

	public Map<String,Object> getModifyColumnValuesMap() {
		return modifyColumnValuesMap;
	}

	public String getModifyColumnValues() {
		return modifyColumnValues;
	}

	public void setModifyColumnValuesMap(String key,Object value) {
		modifyColumnValuesMap.put(key, value);
	}

	public List<Map<String, Object>> getAsIsTableDataList() {
		return asIsTableDataList;
	}

	public void setAsIsTableDataList(List<Map<String, Object>> asIsTableDataList)
	{
		this.asIsTableDataList = asIsTableDataList;
	}

	public List<String> getAsIsColumnNameList()
	{
		return asIsColumnNameList;
	}

	public void setAsIsColumnNameList(List<String> asIsColumnNameList)
	{
		this.asIsColumnNameList = asIsColumnNameList;
	}

	public List<String> getToBeColumnNameList() {
		return toBeColumnNameList;
	}

	public void setToBeColumnNameList(List<String> toBeColumnNameList)
	{
		this.toBeColumnNameList = toBeColumnNameList;
	}
}
