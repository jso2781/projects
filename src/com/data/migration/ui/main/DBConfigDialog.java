package com.data.migration.ui.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringEscapeUtils;

public class DBConfigDialog extends JDialog
{
	private static final long serialVersionUID = -7674854938346767833L;
	private final JPanel contentPanel = new JPanel();
	private JTextField tfAsIsConnectionName;
	private JTextField tfAsIsUserId;
	private JPasswordField pfAsIsPassword;
	private JTextField tfAsIsHostAddress;
	private JTextField tfAsIsPort;
	private JTextField tfAsIsServiceName;
	private JComboBox cbAsIsAuthMode;
	private Properties dbProperties = new Properties();
	private JLabel lblAsIsConnectionStatus;
	private JTextField tfToBeConnectionName;
	private JTextField tfToBeUserId;
	private JTextField tfToBeHostAddress;
	private JTextField tfToBePort;
	private JTextField tfToBeServiceName;
	private JPasswordField pfToBePassword;
	private JLabel lblToBeConnectionStatus;
	private JComboBox cbToBeAuthMode;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DBConfigDialog dialog = new DBConfigDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DBConfigDialog(AsIsDataMigrator owner)
	{
		super(owner);
		setModal(true);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				String configDir = new File("config").getAbsolutePath();
				String currentDirStr = null;
				InputStreamReader isr = null;
				try
				{
					currentDirStr = URLDecoder.decode(configDir, "UTF-8");
					File fbsDBFile = new File(currentDirStr+File.separator+"db.properties");
					isr = new InputStreamReader(new FileInputStream(fbsDBFile),"UTF-8");
					dbProperties.load(isr);

					/********************************************** AS-IS 접속정보 로딩 시작 ******************************************/
					String asIsConnectionName = (String) dbProperties.get("as_is_connection_name");
					String asIsUserId = (String) dbProperties.get("as_is_user_id");
					String asIsPassword = (String) dbProperties.get("as_is_password");
					String asIsHostAddress = (String) dbProperties.get("as_is_host_address");
					String asIsPort = (String) dbProperties.get("as_is_port");
					String asIsServiceName = (String) dbProperties.get("as_is_service_name");
					String asIsAuthMode = (String) dbProperties.get("as_is_auth_mode");

					if(asIsConnectionName != null && !asIsConnectionName.equals(""))
					{
						tfAsIsConnectionName.setText(asIsConnectionName);
					}

					if(asIsUserId != null && !asIsUserId.equals(""))
					{
						tfAsIsUserId.setText(asIsUserId);
					}

					if(asIsPassword != null && !asIsPassword.equals(""))
					{
						pfAsIsPassword.setText(asIsPassword);
					}

					if(asIsHostAddress != null && !asIsHostAddress.equals(""))
					{
						tfAsIsHostAddress.setText(asIsHostAddress);
					}

					if(asIsPort != null && !asIsPort.equals(""))
					{
						tfAsIsPort.setText(asIsPort);
					}

					if(asIsServiceName != null && !asIsServiceName.equals(""))
					{
						tfAsIsServiceName.setText(asIsServiceName);
					}

					if(asIsAuthMode != null && !asIsAuthMode.equals(""))
					{
						cbAsIsAuthMode.setSelectedItem(asIsAuthMode);
					}
					/********************************************** AS-IS 접속정보 로딩 끝 ******************************************/

					/********************************************** TO-BE 접속정보 로딩 시작 ******************************************/
					String toBeConnectionName = (String) dbProperties.get("to_be_connection_name");
					String toBeUserId = (String) dbProperties.get("to_be_user_id");
					String toBePassword = (String) dbProperties.get("to_be_password");
					String toBeHostAddress = (String) dbProperties.get("to_be_host_address");
					String toBePort = (String) dbProperties.get("to_be_port");
					String toBeServiceName = (String) dbProperties.get("to_be_service_name");
					String toBeAuthMode = (String) dbProperties.get("to_be_auth_mode");

					if(toBeConnectionName != null && !toBeConnectionName.equals(""))
					{
						tfToBeConnectionName.setText(toBeConnectionName);
					}

					if(toBeUserId != null && !toBeUserId.equals(""))
					{
						tfToBeUserId.setText(toBeUserId);
					}

					if(toBePassword != null && !toBePassword.equals(""))
					{
						pfToBePassword.setText(toBePassword);
					}

					if(toBeHostAddress != null && !toBeHostAddress.equals(""))
					{
						tfToBeHostAddress.setText(toBeHostAddress);
					}

					if(toBePort != null && !toBePort.equals(""))
					{
						tfToBePort.setText(toBePort);
					}

					if(toBeServiceName != null && !toBeServiceName.equals(""))
					{
						tfToBeServiceName.setText(toBeServiceName);
					}

					if(toBeAuthMode != null && !toBeAuthMode.equals(""))
					{
						cbToBeAuthMode.setSelectedItem(toBeAuthMode);
					}
					/********************************************** TO-BE 접속정보 로딩 끝 ******************************************/

					// 오라클 JDBC 드라이버 클랙스 로딩
					Class.forName("oracle.jdbc.driver.OracleDriver");
				}catch(UnsupportedEncodingException e1)
				{
					e1.printStackTrace();
				}catch(FileNotFoundException e1)
				{
					e1.printStackTrace();
				}catch(IOException e1)
				{
					e1.printStackTrace();
				}catch(ClassNotFoundException e1)
				{
					e1.printStackTrace();
				}finally
				{
					if(isr != null){try{isr.close();}catch(IOException e1){}}
				}
			}
		});
		setTitle("\uB370\uC774\uD130\uBCA0\uC774\uC2A4 \uC811\uC18D \uC815\uBCF4");
		setBounds(100, 100, 717, 399);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(0, 2, 0, 0));
		{
			JPanel asIsDBConfigDetailPanel = new JPanel();
			contentPanel.add(asIsDBConfigDetailPanel);
			asIsDBConfigDetailPanel.setLayout(null);
			
			JLabel lblAsIsConnectionName = new JLabel("Connection Name");
			lblAsIsConnectionName.setBounds(15, 30, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsConnectionName);
			
			tfAsIsConnectionName = new JTextField();
			tfAsIsConnectionName.setBounds(124, 27, 217, 21);
			asIsDBConfigDetailPanel.add(tfAsIsConnectionName);
			tfAsIsConnectionName.setColumns(10);
			
			JLabel lblAsIsUserId = new JLabel("User ID");
			lblAsIsUserId.setBounds(15, 53, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsUserId);
			
			tfAsIsUserId = new JTextField();
			tfAsIsUserId.setColumns(10);
			tfAsIsUserId.setBounds(124, 50, 217, 21);
			asIsDBConfigDetailPanel.add(tfAsIsUserId);
			
			JLabel lblAsIsPassword = new JLabel("Password");
			lblAsIsPassword.setBounds(15, 76, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsPassword);
			
			JLabel lblAsIsHostAddress = new JLabel("Host Address");
			lblAsIsHostAddress.setBounds(15, 153, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsHostAddress);
			
			tfAsIsHostAddress = new JTextField();
			tfAsIsHostAddress.setColumns(10);
			tfAsIsHostAddress.setBounds(124, 150, 217, 21);
			asIsDBConfigDetailPanel.add(tfAsIsHostAddress);
			
			JLabel lblAsIsHostInfo = new JLabel("AS-IS Host Infomation");
			lblAsIsHostInfo.setFont(new Font("굴림", Font.BOLD, 14));
			lblAsIsHostInfo.setBounds(15, 126, 199, 15);
			asIsDBConfigDetailPanel.add(lblAsIsHostInfo);
			
			JLabel lblAsIsUserInfomation = new JLabel("AS-IS User Infomation");
			lblAsIsUserInfomation.setFont(new Font("굴림", Font.BOLD, 14));
			lblAsIsUserInfomation.setBounds(15, 5, 207, 15);
			asIsDBConfigDetailPanel.add(lblAsIsUserInfomation);
			
			tfAsIsPort = new JTextField();
			tfAsIsPort.setColumns(10);
			tfAsIsPort.setBounds(124, 172, 217, 21);
			asIsDBConfigDetailPanel.add(tfAsIsPort);
			
			JLabel lblAsIsPort = new JLabel("Port");
			lblAsIsPort.setBounds(15, 175, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsPort);
			
			JLabel lblAsIsServiceName = new JLabel("Service Name");
			lblAsIsServiceName.setBounds(15, 197, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsServiceName);
			
			tfAsIsServiceName = new JTextField();
			tfAsIsServiceName.setColumns(10);
			tfAsIsServiceName.setBounds(124, 194, 217, 21);
			asIsDBConfigDetailPanel.add(tfAsIsServiceName);
			
			JLabel lblAsIsAuthMode = new JLabel("Auth Mode");
			lblAsIsAuthMode.setBounds(15, 220, 108, 15);
			asIsDBConfigDetailPanel.add(lblAsIsAuthMode);
			
			cbAsIsAuthMode = new JComboBox();
			cbAsIsAuthMode.setModel(new DefaultComboBoxModel(new String[] {"DEFALUT", "SYSDBA", "SYSOPER"}));
			cbAsIsAuthMode.setBounds(124, 216, 120, 23);
			asIsDBConfigDetailPanel.add(cbAsIsAuthMode);
			
			pfAsIsPassword = new JPasswordField();
			pfAsIsPassword.setBounds(124, 73, 217, 21);
			asIsDBConfigDetailPanel.add(pfAsIsPassword);
			
			lblAsIsConnectionStatus = new JLabel("Status : ");
			lblAsIsConnectionStatus.setBounds(15, 260, 326, 15);
			asIsDBConfigDetailPanel.add(lblAsIsConnectionStatus);
			
			JButton btnAsIsTest = new JButton("\uD14C\uC2A4\uD2B8");
			btnAsIsTest.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					boolean isConnSuccess = false;
					
					/********************************************** AS-IS 접속 테스트 시작 *****************************************/
					String asIsUserId = tfAsIsUserId.getText();
					char[] asIsPasswordCharArray = pfAsIsPassword.getPassword();
					String asIsPassword = new String(asIsPasswordCharArray);
					String asIsHostAddress = tfAsIsHostAddress.getText();
					String asIsPort = tfAsIsPort.getText();
					String asIsServiceName = tfAsIsServiceName.getText();

					if(asIsUserId.equals(""))
					{
						JOptionPane.showMessageDialog(getOwner(), "User ID is required.", "AS-IS Warning", JOptionPane.WARNING_MESSAGE);
						tfAsIsUserId.setSelectionStart(0);
						lblAsIsConnectionStatus.setText("Status : Failure");
						return;
					}

					if(asIsPassword.equals(""))
					{
						JOptionPane.showMessageDialog(getOwner(), "Password is required.", "AS-IS Warning", JOptionPane.WARNING_MESSAGE);
						pfAsIsPassword.setSelectionStart(0);
						lblAsIsConnectionStatus.setText("Status : Failure");
						return;
					}

					if(asIsHostAddress.equals(""))
					{
						JOptionPane.showMessageDialog(getOwner(), "Host Address is required.", "AS-IS Warning", JOptionPane.WARNING_MESSAGE);
						tfAsIsHostAddress.setSelectionStart(0);
						lblAsIsConnectionStatus.setText("Status : Failure");
						return;
					}

					if(asIsPort.equals(""))
					{
						JOptionPane.showMessageDialog(getOwner(), "Port is required.", "AS-IS Warning", JOptionPane.WARNING_MESSAGE);
						tfAsIsPort.setSelectionStart(0);
						lblAsIsConnectionStatus.setText("Status : Failure");
						return;
					}

					if(asIsServiceName.equals(""))
					{
						JOptionPane.showMessageDialog(getOwner(), "Service Name is required.", "AS-IS Warning", JOptionPane.WARNING_MESSAGE);
						tfAsIsServiceName.setSelectionStart(0);
						lblAsIsConnectionStatus.setText("Status : Failure");
						return;
					}

					//ex) jdbc:oracle:thin:@172.31.117.81:1850:PGSCM
					Connection asIsConn = null;
					String asIsUrl = "jdbc:oracle:thin:@"+asIsHostAddress+":"+asIsPort+":"+asIsServiceName;
					
					try
					{
						asIsConn=DriverManager.getConnection(asIsUrl,asIsUserId,asIsPassword);
						isConnSuccess = true;
					}catch(SQLException e1)
					{
						isConnSuccess = false;
						JOptionPane.showMessageDialog(getOwner(), e1.getMessage(), "AS-IS Warning", JOptionPane.WARNING_MESSAGE);
					}finally
					{
						if(asIsConn != null){try{asIsConn.close();}catch(SQLException e1){}}
					}

					lblAsIsConnectionStatus.setText(isConnSuccess ? "Status : Success" :"Status : Failure");
					/********************************************** AS-IS 접속 테스트 끝 ******************************************/
				}
			});
			btnAsIsTest.setMargin(new Insets(2, 2, 2, 2));
			btnAsIsTest.setBounds(285, 273, 56, 25);
			asIsDBConfigDetailPanel.add(btnAsIsTest);
		}
		
		JPanel toBeDBConfigDetailPanel = new JPanel();
		toBeDBConfigDetailPanel.setLayout(null);
		contentPanel.add(toBeDBConfigDetailPanel);
		
		JLabel lblToBeConnectionName = new JLabel("Connection Name");
		lblToBeConnectionName.setBounds(15, 30, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBeConnectionName);
		
		tfToBeConnectionName = new JTextField();
		tfToBeConnectionName.setColumns(10);
		tfToBeConnectionName.setBounds(124, 27, 217, 21);
		toBeDBConfigDetailPanel.add(tfToBeConnectionName);
		
		JLabel lblToBeUserId = new JLabel("User ID");
		lblToBeUserId.setBounds(15, 53, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBeUserId);
		
		tfToBeUserId = new JTextField();
		tfToBeUserId.setColumns(10);
		tfToBeUserId.setBounds(124, 50, 217, 21);
		toBeDBConfigDetailPanel.add(tfToBeUserId);
		
		JLabel lblToBePassword = new JLabel("Password");
		lblToBePassword.setBounds(15, 76, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBePassword);
		
		JLabel lblToBeHostAddress = new JLabel("Host Address");
		lblToBeHostAddress.setBounds(15, 153, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBeHostAddress);
		
		tfToBeHostAddress = new JTextField();
		tfToBeHostAddress.setColumns(10);
		tfToBeHostAddress.setBounds(124, 150, 217, 21);
		toBeDBConfigDetailPanel.add(tfToBeHostAddress);
		
		JLabel lblToBeHostInfo = new JLabel("TO-BE Host Infomation");
		lblToBeHostInfo.setFont(new Font("굴림", Font.BOLD, 14));
		lblToBeHostInfo.setBounds(15, 126, 199, 15);
		toBeDBConfigDetailPanel.add(lblToBeHostInfo);
		
		JLabel lblToBeUserInfomation = new JLabel("TO-BE User Infomation");
		lblToBeUserInfomation.setFont(new Font("굴림", Font.BOLD, 14));
		lblToBeUserInfomation.setBounds(15, 5, 207, 15);
		toBeDBConfigDetailPanel.add(lblToBeUserInfomation);
		
		tfToBePort = new JTextField();
		tfToBePort.setColumns(10);
		tfToBePort.setBounds(124, 172, 217, 21);
		toBeDBConfigDetailPanel.add(tfToBePort);
		
		JLabel lblToBePort = new JLabel("Port");
		lblToBePort.setBounds(15, 175, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBePort);
		
		JLabel lblToBeServiceName = new JLabel("Service Name");
		lblToBeServiceName.setBounds(15, 197, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBeServiceName);
		
		tfToBeServiceName = new JTextField();
		tfToBeServiceName.setColumns(10);
		tfToBeServiceName.setBounds(124, 194, 217, 21);
		toBeDBConfigDetailPanel.add(tfToBeServiceName);
		
		JLabel lblToBeAuthMode = new JLabel("Auth Mode");
		lblToBeAuthMode.setBounds(15, 220, 108, 15);
		toBeDBConfigDetailPanel.add(lblToBeAuthMode);
		
		cbToBeAuthMode = new JComboBox();
		cbToBeAuthMode.setModel(new DefaultComboBoxModel(new String[] {"DEFALUT", "SYSDBA", "SYSOPER"}));
		cbToBeAuthMode.setBounds(124, 216, 120, 23);
		toBeDBConfigDetailPanel.add(cbToBeAuthMode);
		
		pfToBePassword = new JPasswordField();
		pfToBePassword.setBounds(124, 73, 217, 21);
		toBeDBConfigDetailPanel.add(pfToBePassword);
		
		lblToBeConnectionStatus = new JLabel("Status : ");
		lblToBeConnectionStatus.setBounds(15, 260, 326, 15);
		toBeDBConfigDetailPanel.add(lblToBeConnectionStatus);
		
		JButton btnToBeTest = new JButton("\uD14C\uC2A4\uD2B8");
		btnToBeTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				boolean isConnSuccess = false;

				/********************************************** TO-BE 접속 테스트 시작 *****************************************/
				String toBeUserId = tfToBeUserId.getText();
				char[] toBePasswordCharArray = pfToBePassword.getPassword();
				String toBePassword = new String(toBePasswordCharArray);
				String toBeHostAddress = tfToBeHostAddress.getText();
				String toBePort = tfToBePort.getText();
				String toBeServiceName = tfToBeServiceName.getText();

				if(toBeUserId.equals(""))
				{
					JOptionPane.showMessageDialog(getOwner(), "User ID is required.", "TO-BE Warning", JOptionPane.WARNING_MESSAGE);
					tfToBeUserId.setSelectionStart(0);
					lblToBeConnectionStatus.setText("Status : Failure");
					return;
				}

				if(toBePassword.equals(""))
				{
					JOptionPane.showMessageDialog(getOwner(), "Password is required.", "TO-BE Warning", JOptionPane.WARNING_MESSAGE);
					pfToBePassword.setSelectionStart(0);
					lblToBeConnectionStatus.setText("Status : Failure");
					return;
				}

				if(toBeHostAddress.equals(""))
				{
					JOptionPane.showMessageDialog(getOwner(), "Host Address is required.", "TO-BE Warning", JOptionPane.WARNING_MESSAGE);
					tfToBeHostAddress.setSelectionStart(0);
					lblToBeConnectionStatus.setText("Status : Failure");
					return;
				}

				if(toBePort.equals(""))
				{
					JOptionPane.showMessageDialog(getOwner(), "Port is required.", "TO-BE Warning", JOptionPane.WARNING_MESSAGE);
					tfToBePort.setSelectionStart(0);
					lblToBeConnectionStatus.setText("Status : Failure");
					return;
				}

				if(toBeServiceName.equals(""))
				{
					JOptionPane.showMessageDialog(getOwner(), "Service Name is required.", "TO-BE Warning", JOptionPane.WARNING_MESSAGE);
					tfToBeServiceName.setSelectionStart(0);
					lblToBeConnectionStatus.setText("Status : Failure");
					return;
				}

				//ex) jdbc:oracle:thin:@172.31.117.81:1850:PGSCM
				Connection toBeConn = null;
				String toBeUrl = "jdbc:oracle:thin:@"+toBeHostAddress+":"+toBePort+":"+toBeServiceName;

				try
				{
					System.out.println("To-Be DB Connection Test Start!!");
					System.out.println("To-Be DB Connection toBeUrl="+toBeUrl+"\ntoBeUserId="+toBeUserId+"\ntoBePassword="+toBePassword);
					toBeConn=DriverManager.getConnection(toBeUrl,toBeUserId,toBePassword);
					isConnSuccess = true;
					System.out.println("To-Be DB Connection Test End!!");
				}catch(SQLException e1)
				{
					isConnSuccess = false;
					System.out.println("To-Be DB Connection Test Failure!!");
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getOwner(), e1.getMessage(), "TO-BE Warning", JOptionPane.WARNING_MESSAGE);
				}finally
				{
					if(toBeConn != null){try{toBeConn.close();}catch(SQLException e1){}}
				}

				lblToBeConnectionStatus.setText(isConnSuccess ? "Status : Success" :"Status : Failure");
				/********************************************** TO-BE 접속 테스트 끝 ******************************************/
			}
		});
		btnToBeTest.setMargin(new Insets(2, 2, 2, 2));
		btnToBeTest.setBounds(285, 273, 56, 25);
		toBeDBConfigDetailPanel.add(btnToBeTest);
		{
			JPanel controlButtonPane = new JPanel();
			controlButtonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(controlButtonPane, BorderLayout.SOUTH);
			
			JButton btnOk = new JButton("\uD655\uC778");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					/********************************************** AS-IS 접속정보 저장 시작 *****************************************/
					String asIsConnectionName = tfAsIsConnectionName.getText();
					String asIsUserId = tfAsIsUserId.getText();
					char[] asIsPasswordCharArray = pfAsIsPassword.getPassword();
					String asIsPassword = new String(asIsPasswordCharArray);
					String asIsHostAddress = tfAsIsHostAddress.getText();
					String asIsPort = tfAsIsPort.getText();
					String asIsServiceName = tfAsIsServiceName.getText();
					String asIsAuthMode = (String) cbAsIsAuthMode.getSelectedItem();

					asIsServiceName = StringEscapeUtils.escapeJavaScript(asIsServiceName);
					
					if(!asIsConnectionName.equals(""))
					{
						dbProperties.put("as_is_connection_name",asIsConnectionName);
					}

					if(!asIsUserId.equals(""))
					{
						dbProperties.put("as_is_user_id",asIsUserId);
					}

					if(!asIsPassword.equals(""))
					{
						dbProperties.put("as_is_password",asIsPassword);
					}

					if(!asIsHostAddress.equals(""))
					{
						dbProperties.put("as_is_host_address",asIsHostAddress);
					}

					if(!asIsPort.equals(""))
					{
						dbProperties.put("as_is_port",asIsPort);
					}

					if(!asIsServiceName.equals(""))
					{
						dbProperties.put("as_is_service_name",asIsServiceName);
					}

					if(!asIsAuthMode.equals(""))
					{
						dbProperties.put("as_is_auth_mode",asIsAuthMode);
					}
					/********************************************** AS-IS 접속정보 저장 끝 *****************************************/

					/********************************************** TO-BE 접속정보 저장 시작 *****************************************/
					String toBeConnectionName = tfToBeConnectionName.getText();
					String toBeUserId = tfToBeUserId.getText();
					char[] toBePasswordCharArray = pfToBePassword.getPassword();
					String toBePassword = new String(toBePasswordCharArray);
					String toBeHostAddress = tfToBeHostAddress.getText();
					String toBePort = tfToBePort.getText();
					String toBeServiceName = tfToBeServiceName.getText();
					String toBeAuthMode = (String) cbToBeAuthMode.getSelectedItem();

					toBeServiceName = StringEscapeUtils.escapeJavaScript(toBeServiceName);

					if(!toBeConnectionName.equals(""))
					{
						dbProperties.put("to_be_connection_name",toBeConnectionName);
					}

					if(!toBeUserId.equals(""))
					{
						dbProperties.put("to_be_user_id",toBeUserId);
					}

					if(!toBePassword.equals(""))
					{
						dbProperties.put("to_be_password",toBePassword);
					}

					if(!toBeHostAddress.equals(""))
					{
						dbProperties.put("to_be_host_address",toBeHostAddress);
					}

					if(!toBePort.equals(""))
					{
						dbProperties.put("to_be_port",toBePort);
					}

					if(!toBeServiceName.equals(""))
					{
						dbProperties.put("to_be_service_name",toBeServiceName);
					}

					if(!toBeAuthMode.equals(""))
					{
						dbProperties.put("to_be_auth_mode",toBeAuthMode);
					}
					/********************************************** TO-BE 접속정보 저장 끝 *******************************************/

					//AS-IS TO-BE DB Properties 저장
					saveDbProperties(dbProperties);

					AsIsDataMigrator adm = (AsIsDataMigrator) getOwner();

					adm.setMessageConsole("DBConfigDialog Save Success!!");

					//sqlSessionFactory 갱신
					adm.setMyBatisConfig();

					dispose();
				}
			});
			controlButtonPane.add(btnOk);
			
			JButton btnCancel = new JButton("\uCDE8\uC18C");
			btnCancel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					DBConfigDialog.this.dispose();
				}
			});
			controlButtonPane.add(btnCancel);
		}
	}

	public static void setDbProperties(String key,String v)
	{
		String configDir = new File("config").getAbsolutePath();

		String currentDirStr = null;
		InputStreamReader isr = null;
		FileOutputStream os = null;
		try
		{
			Properties dbPropertiesTemp = new Properties();

			currentDirStr = URLDecoder.decode(configDir, "UTF-8");

			File dbPropertiesFile1 = new File(currentDirStr+File.separator+"db.properties");
			isr = new InputStreamReader(new FileInputStream(dbPropertiesFile1),"UTF-8");
			dbPropertiesTemp.load(isr);

			dbPropertiesTemp.put(key, v);

			File dbPropertiesFile2 = new File(currentDirStr+File.separator+"db.properties");
			os = new FileOutputStream(dbPropertiesFile2);
			dbPropertiesTemp.store(os, "AS-IS Data Migration DB Properties");
		}catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException e)
		{
			e.printStackTrace();
		}finally
		{
			if(isr != null){try{isr.close();}catch(IOException e){}}
			if(os != null){try{os.close();}catch(IOException e){}}
		}
	}

	public static void saveDbProperties(Properties dbProperties)
	{
		String configDir = new File("config").getAbsolutePath();
		String dbPropertiesFileFullPath = null;
		FileOutputStream os = null;
		File dbPropertiesFile = null;

		try
		{
			dbPropertiesFileFullPath = URLDecoder.decode(configDir+File.separator+"db.properties", "UTF-8");
			dbPropertiesFile = new File(dbPropertiesFileFullPath);

			os = new FileOutputStream(dbPropertiesFile);
			dbProperties.store(os, "AS-IS Data Migration DB Properties");
		}catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException e)
		{
			e.printStackTrace();
		}finally
		{
			if(os != null){try{os.close();}catch(IOException e){}}
		}

		dbPropertiesFileFullPath = null;
		dbPropertiesFile = null;
	}
}
