package com.sql.parse;

public class ColumnInfo
{
	private String columnName = null;
	private String columnJavaDataTypeName = null;
	private int jdbcColumnType = -1;

	public ColumnInfo(String columnName,String columnJavaDataTypeName)
	{
		this.columnName = columnName;
		this.columnJavaDataTypeName = columnJavaDataTypeName;
	}

	public ColumnInfo(String columnName,int jdbcColumnType)
	{
		this.columnName = columnName;
		this.jdbcColumnType = jdbcColumnType;
	}

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnJavaDataTypeName() {
		return columnJavaDataTypeName;
	}
	public void setColumnJavaDataTypeName(String columnJavaDataTypeName) {
		this.columnJavaDataTypeName = columnJavaDataTypeName;
	}
	public int getJdbcColumnType() {
		return jdbcColumnType;
	}

	public void setJdbcColumnType(int jdbcColumnType) {
		this.jdbcColumnType = jdbcColumnType;
	}
}
