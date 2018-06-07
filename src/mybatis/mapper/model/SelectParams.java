package mybatis.mapper.model;

public class SelectParams
{
	private String tableName;
	private String asIsTableName;
	private String toBeTableName;
	private String whereClause;

	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
    public String getAsIsTableName() {
		return asIsTableName;
	}
	public void setAsIsTableName(String asIsTableName) {
		this.asIsTableName = asIsTableName;
	}
	public String getToBeTableName() {
		return toBeTableName;
	}
	public void setToBeTableName(String toBeTableName) {
		this.toBeTableName = toBeTableName;
	}
	public String getWhereClause() {
		return whereClause;
	}
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append("tableName : "+tableName+"\n");
    	sb.append("asIsTableName : "+asIsTableName+"\n");
    	sb.append("toBeTableName : "+toBeTableName+"\n");
    	sb.append("whereClause : "+whereClause+"\n");
    	return sb.toString();
    }
}
