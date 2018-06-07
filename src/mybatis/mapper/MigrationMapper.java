package mybatis.mapper;

import java.util.List;
import java.util.Map;

import mybatis.mapper.model.SelectParams;

import com.sql.parse.ColumnInfo;

public interface MigrationMapper
{
	List<ColumnInfo> selectAsIsTableColumnInfo(SelectParams selectParams);
	public Map<Integer,Map<String,String>> selectAsIsTableDataList(SelectParams selectParams);
	public void insertAsIsTableDataList(SelectParams selectParams);
}
