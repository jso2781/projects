package mybatis.mapper.model;

import java.math.BigDecimal;

public class InsertParams
{
    private String companyCd;
    private String yyyyMmDd;
    private BigDecimal dayTotalCount = new BigDecimal(0);

    public String getCompanyCd() {
		return companyCd;
	}

	public void setCompanyCd(String companyCd) {
		this.companyCd = companyCd;
	}

	public String getYyyyMmDd() {
		return yyyyMmDd;
	}

	public void setYyyyMmDd(String yyyyMmDd) {
		this.yyyyMmDd = yyyyMmDd;
	}

	public BigDecimal getDayTotalCount() {
		return dayTotalCount;
	}

	public void setDayTotalCount(BigDecimal dayTotalCount) {
		this.dayTotalCount = dayTotalCount;
	}

	public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append("companyCd : "+companyCd+"\n");
    	sb.append("yyyyMmDd : "+yyyyMmDd+"\n");
    	sb.append("dayTotalCount : "+dayTotalCount);
    	return sb.toString();
    }
}
