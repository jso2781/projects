package com.data.migration.ui.main.worker;

import java.util.ArrayList;
import java.util.List;

public class MathTest {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		List<String> alist = new ArrayList<String>();
		for(int i=0;i<9999;i++)
		{
			alist.add("");
		}

		int insertFetchCount = 10000;
		int rowStrListCount = alist.size();
		System.out.println("rowStrListCount/insertFetchCount="+rowStrListCount/insertFetchCount);
		System.out.println("Math.ceil(rowStrListCount/insertFetchCount)="+Math.ceil((double)rowStrListCount/insertFetchCount));
		
		int loop = (int) Math.ceil((double)rowStrListCount/insertFetchCount);
		System.out.println("loop="+loop);
		
		if(rowStrListCount > insertFetchCount)
		{
			loop = (int) Math.floor(rowStrListCount/insertFetchCount);

			for(int i=0;i<=loop;i++)
			{
				int fromIndex = i * insertFetchCount;
				int toIndex = (i + 1) * insertFetchCount;
				if(toIndex > rowStrListCount)
				{
					toIndex = rowStrListCount;
				}
				System.out.println("fromIndex="+fromIndex+", toIndex="+toIndex);
			}
		}else
		{
			System.out.println("fromIndex="+0+", toIndex="+rowStrListCount);
		}
		
	}

}
