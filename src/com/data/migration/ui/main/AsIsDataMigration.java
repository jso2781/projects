package com.data.migration.ui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sql.parse.AsIsTableDataExtractHandler;
import com.sql.parse.ColumnInfo;
import com.sql.parse.ToBeTableDataHandler;

public class AsIsDataMigration extends JFrame
{
	private static final long serialVersionUID = 1L;

	public AsIsDataMigration()
	{
		init();
	}

	private class StartActionListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e)
		{
			int asIsTableNameListCount = -1;
			AsIsTableDataExtractHandler adm = new AsIsTableDataExtractHandler();

			List<String> asIsTableNameList = getTableNameList();
			asIsTableNameListCount = asIsTableNameList.size();

			Map<String,List<ColumnInfo>> asIsTableTotalColumnInfoMap = new HashMap<String,List<ColumnInfo>>();
			Map<String,Map<Integer, Map<String, String>>> asIsTableTotalDataMap = new HashMap<String, Map<Integer,Map<String,String>>>();

			//������ DB�κ��� ���̱׷��̼��� ���̺���� Į������,�����Ͱ��� ������.(AS-IS)
			for(int i=0;i<asIsTableNameListCount;i++)
			{
				List<ColumnInfo> columnList  = new ArrayList<ColumnInfo>();
				String asIsTableName = asIsTableNameList.get(i);

				System.out.println("asIsTableName="+asIsTableName);
				try
				{
					adm.getAsIsTableColumnInfo(asIsTableName,columnList);	// AS-IS ���̺��� Į������ ��������
				}catch (SQLException e1)
				{
					e1.printStackTrace();
					return;
				}
				asIsTableTotalColumnInfoMap.put(asIsTableName, columnList);
				Map<Integer, Map<String, String>> asIsTableDataMap = adm.getAsIsTableData(asIsTableName, columnList, "COMPANY_CD='AAPC' ");
				asIsTableTotalDataMap.put(asIsTableName, asIsTableDataMap);
			}

			
			StringBuffer alretMessageStr = new StringBuffer();
			System.out.println("asIsTableNameListCount="+asIsTableNameListCount);
			if(asIsTableNameListCount > 0)
			{
				alretMessageStr.append("�Է��� AS-IS ���̺��\n");
				for(int i=0;i<asIsTableNameListCount;i++)
				{
					String asIsTableName = asIsTableNameList.get(i);
					alretMessageStr.append(asIsTableName+"\n");
				}

				alretMessageStr.append("��� �����Ͻðڽ��ϱ�?\n");
				System.out.println("asIsTableNameListCount="+alretMessageStr.toString());
				int result = JOptionPane.showConfirmDialog(AsIsDataMigration.this, alretMessageStr.toString(), "�Է� ���̺�� Ȯ��", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(result == JOptionPane.OK_OPTION)
				{
					Map<String,List<ColumnInfo>> toBeTableTotalColumnInfoMap = new HashMap<String,List<ColumnInfo>>();
					ToBeTableDataHandler tdh = new ToBeTableDataHandler();
					for(int i=0;i<asIsTableNameListCount;i++)
					{
						List<ColumnInfo> toBeTableColumnList  = new ArrayList<ColumnInfo>();
						String tableName = asIsTableNameList.get(i);

						tdh.getToBeTableColumnInfo(tableName, toBeTableColumnList);

						toBeTableTotalColumnInfoMap.put(tableName, toBeTableColumnList);
						
						//AS-IS�� TO-BE ���̺� Į�� ������ ���ؼ� �Է� �������� ���� ������ ���̱׷��̼� ó��
						tdh.setToBeMigration(tableName, toBeTableColumnList, asIsTableTotalColumnInfoMap.get(tableName), asIsTableTotalDataMap.get(tableName));
					}
				}
			}
		}
	}

	public void init()
	{
		JButton btnStart = new JButton("����");
		btnStart.addActionListener(new StartActionListener());
		
		JButton btnEnd = new JButton("����");
		btnEnd.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
				System.exit(0);
			}
		});

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel pnlControll = new JPanel();
		pnlControll.add(btnStart);
		pnlControll.add(btnEnd);
		getContentPane().add(pnlControll);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static List<String> getTableNameList()
    {
        //�ּ� ,���� ���� ����
        String patternStr = "(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)|(//.*)";//|\\s";
        
//        String lineSeparator = System.getProperty("line.separator");

        List<String> tableNameStringList = new ArrayList<String>();

        FileChannel fin = null;
        FileInputStream fis = null;
        try
        {
        	String configDir = new File("config").getAbsolutePath();
        	String fileFullPath = URLDecoder.decode(configDir+File.separator+"migrationTableList.sql", "UTF-8");
            File f = new File(fileFullPath);

            fis = new FileInputStream(f);
            fin = fis.getChannel();

            MappedByteBuffer mappedByteBuffer = fin.map(FileChannel.MapMode.READ_ONLY, 0, fin.size());
            
            Charset charset = Charset.forName("UTF-8");
            
            CharsetDecoder decoder = charset.newDecoder();
            
            CharBuffer cbuffer = decoder.decode(mappedByteBuffer);
            
            String fileContents = cbuffer.toString().replaceAll(patternStr, "");
            
            Scanner sc = new Scanner(fileContents);//.useDelimiter(lineSeparator);

            System.out.println("=================================== AS-IS ���̺� ����Ʈ ����=============================");
            while(sc.hasNext())
            {
                String lineStr = sc.nextLine();
                
                if(!lineStr.trim().equals(""))	// ������ skip
                {
                    System.out.println(lineStr.trim());
                    tableNameStringList.add(lineStr);
                }
            }
            System.out.println("=================================== AS-IS ���̺� ����Ʈ ��=============================");
            sc.close();
            mappedByteBuffer.clear();
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

        return tableNameStringList;
    }

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new AsIsDataMigration();
			}
		});
		
	}

}
