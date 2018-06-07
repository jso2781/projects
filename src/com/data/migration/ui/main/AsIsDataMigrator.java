package com.data.migration.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.data.migration.config.db.MyBatisConfigTask;
import com.data.migration.config.model.MigrationDataInfo;
import com.data.migration.config.parse.MigrationDataInfoParser;
import com.data.migration.config.parse.MigrationDataInfoXmlParser;
import com.data.migration.ui.logger.TextPaneHandler;
import com.data.migration.ui.main.worker.ProcDataTransfer;
import com.data.migration.ui.table.editor.TextAreaCellEditor;
import com.data.migration.ui.table.renderer.LineWrapCellRenderer;

public class AsIsDataMigrator extends JFrame
{
	private static final long serialVersionUID = 4090529252999091024L;

	private JPanel pnContent;
	private JTable tbMigrationTableList;

	private JScrollPane spnMigrationTableList;
	private JTextPane tpStatusView = null;
	private SimpleAttributeSet attrubuteSet = new SimpleAttributeSet();
	private JSplitPane sppContentCenter;
	private JScrollPane spnStatus;
	private JButton btnTransfer;
	private JButton btnClearConfig;
	private JButton btnSaveConfig;
	private MemoryStatusBar pnMemoryStatus;
	private MyBatisConfigTask myBatisConfigTask = new MyBatisConfigTask(this);
	private JCheckBox chkBatchYn;

	private static Logger logger = Logger.getLogger(AsIsDataMigrator.class.getName());
	private JCheckBox chkVPNYn;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					AsIsDataMigrator frame = new AsIsDataMigrator();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private static class UserFormatter extends Formatter
	{
		private StringBuffer sb = new StringBuffer();
		synchronized public String format(LogRecord record)
		{
			sb.setLength(0);

			//다른 내용은 안 쓰고 오로지 message만 출력.
			String message = formatMessage(record);
			sb.append(message);
//			sb.append("\n");
			return sb.toString();	
		}
	}

	/**
	 * Create the frame.
	 */
	public AsIsDataMigrator() {
		setBounds(new Rectangle(100, 100, 1200, 600));
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				getMigrationDataInfoList();
				setMessageDisplay();
				setMyBatisConfig();
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
//				System.out.println("componentResized !!");
				//화면창 크기가 변경되면 칼럼폭을 조정
//				TableCellWidthUtils.setColumnWidths(tbMigrationTableList,new Insets(0,3,0,3),false,false);
			}
			
		});

		addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				System.exit(0);
			}
		});

		setTitle("DB\uAC04 \uB370\uC774\uD130 \uC774\uAD00");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JMenuBar mnbConfig = new JMenuBar();
		setJMenuBar(mnbConfig);
		
		JMenu mnConfig = new JMenu("\uC124\uC815");
		mnbConfig.add(mnConfig);
		
		JMenuItem mntmDbConfig = new JMenuItem("DB \uC811\uC18D \uC124\uC815");
		mntmDbConfig.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DBConfigDialog dbConfigDialog = new DBConfigDialog(AsIsDataMigrator.this);
				dbConfigDialog.setLocationRelativeTo(AsIsDataMigrator.this);
				dbConfigDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dbConfigDialog.setVisible(true);
			}
		});
		mnConfig.add(mntmDbConfig);
		
		JMenu mnHelp = new JMenu("Help");
		mnbConfig.add(mnHelp);
		
		JMenuItem mntmHelp = new JMenuItem("V.\uBC84\uC804");
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				//제목 : 버전 , 내용 : 조성옥 v2.5( 최근 2017.12.21)
				JOptionPane.showMessageDialog(AsIsDataMigrator.this, "\uC870\uC131\uC625 v2.5( \uCD5C\uADFC 2017.12.21 )", "\uBC84\uC804", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		mnHelp.add(mntmHelp);
		pnContent = new JPanel();
		pnContent.setBorder(new EmptyBorder(2, 2, 2, 2));
		pnContent.setLayout(new BorderLayout(0, 0));
		setContentPane(pnContent);

		JPanel pnContentNorth = new JPanel();
		pnContent.add(pnContentNorth, BorderLayout.NORTH);
		pnContentNorth.setLayout(new BorderLayout(0, 0));
		
		JLabel lblTitle = new JLabel("AS-IS TO-BE \uB370\uC774\uD130 \uC774\uAD00");
		pnContentNorth.add(lblTitle, BorderLayout.WEST);
		
		JPanel pnContentNorthCenter = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnContentNorthCenter.getLayout();
		flowLayout.setVgap(1);
		flowLayout.setHgap(1);
		flowLayout.setAlignment(FlowLayout.RIGHT);
		pnContentNorth.add(pnContentNorthCenter, BorderLayout.CENTER);

		btnTransfer = new JButton("\uB370\uC774\uD130 \uC774\uAD00");
		btnTransfer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ProcDataTransfer transfer = new ProcDataTransfer(AsIsDataMigrator.this);
				transfer.execute();
			}
		});
		
		chkBatchYn = new JCheckBox("\uBC30\uCE58 \uCC98\uB9AC");
		
		chkBatchYn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String batchYnStr = chkBatchYn.isSelected() ? "Y" : "N";
				DBConfigDialog.setDbProperties("batchYn",batchYnStr);
			}
		});

		chkVPNYn = new JCheckBox("VPN\uD658\uACBD");
		chkVPNYn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String vpnYnStr = chkVPNYn.isSelected() ? "Y" : "N";
				DBConfigDialog.setDbProperties("vpnYn",vpnYnStr);

				if(chkVPNYn.isSelected())
				{
					/*
					 * AS-IS DB가 VPN 환경에서 접속이 가능하면
					 * 데이터 이관 버튼을 누르기 전에 VPN 접속을 먼저 해 주세요.
					 */
					JOptionPane.showMessageDialog(AsIsDataMigrator.this, "AS-IS DB\uAC00 VPN \uD658\uACBD\uC5D0\uC11C \uC811\uC18D\uC774 \uAC00\uB2A5\uD558\uBA74\n\uB370\uC774\uD130 \uC774\uAD00 \uBC84\uD2BC\uC744 \uB204\uB974\uAE30 \uC804\uC5D0 VPN \uC811\uC18D\uC744 \uBA3C\uC800 \uD574 \uC8FC\uC138\uC694.", "INFO", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		
		pnContentNorthCenter.add(chkVPNYn);

		pnContentNorthCenter.add(chkBatchYn);

//		Timer timer = new Timer(delay, listener);
		btnTransfer.setMargin(new Insets(2, 2, 2, 2));
		pnContentNorthCenter.add(btnTransfer);
		
		JButton btnAddRow = new JButton("\uD589\uCD94\uAC00");
		btnAddRow.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultTableModel tm  = (DefaultTableModel) tbMigrationTableList.getModel();

				int rowCount = tm.getRowCount();
				int rowSeqNum = rowCount + 1;

				tm.insertRow(tm.getRowCount(), new Object[]{false,rowSeqNum,"","","",""});
			}
		});
		
		btnSaveConfig = new JButton("\uC124\uC815\uC800\uC7A5");
		btnSaveConfig.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//설정 목록을 XML 파일로 저장
				MigrationDataInfoParser parser = new MigrationDataInfoParser();
				
				if(parser.checkMigrationDataInfoList(AsIsDataMigrator.this))
				{
					List<MigrationDataInfo> migrationDataInfoList = parser.getMigrationDataInfoList(tbMigrationTableList);
					if(parser.saveMigrationDataInfoListToXml(migrationDataInfoList))
					{
						JOptionPane.showMessageDialog(AsIsDataMigrator.this, "\uC800\uC7A5\uB418\uC5C8\uC2B5\uB2C8\uB2E4.", "INFORMATION", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		
		btnSaveConfig.setMargin(new Insets(2, 2, 2, 2));
		pnContentNorthCenter.add(btnSaveConfig);
		
		btnClearConfig = new JButton("\uCD08\uAE30\uD654");
		btnClearConfig.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int resultCode = JOptionPane.showConfirmDialog(AsIsDataMigrator.this, "\uBAA8\uB4E0 \uC124\uC815\uC744 \uC0AD\uC81C\uD558\uC2DC\uACA0\uC2B5\uB2C8\uAE4C?", "DELETE CONFIRM", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if(resultCode == JOptionPane.YES_OPTION)
				{
					//테이블에 로딩된 목록 삭제
					((DefaultTableModel)tbMigrationTableList.getModel()).setRowCount(0);

					//설정 목록이 없는 XML 파일로 저장
					MigrationDataInfoParser parser = new MigrationDataInfoParser();
					parser.saveMigrationDataInfoListToXml(null);
				}
			}
		});
		btnClearConfig.setMargin(new Insets(2, 2, 2, 2));
		pnContentNorthCenter.add(btnClearConfig);
		btnAddRow.setMargin(new Insets(2, 2, 2, 2));
		pnContentNorthCenter.add(btnAddRow);
		
		JButton btnDelRow = new JButton("\uD589\uC0AD\uC81C");

		btnDelRow.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultTableModel tm  = (DefaultTableModel) tbMigrationTableList.getModel();

				boolean isChecked = false;
				int rowCount = tbMigrationTableList.getRowCount();

				int checkedRowCount = 0;
				int selectedRowIdx = tbMigrationTableList.getSelectedRow();

				List<Integer> deletingRowIndexList = new ArrayList<Integer>();
				
				for(int i=rowCount-1;i>=0;i--)
				{
					isChecked = (Boolean) tbMigrationTableList.getValueAt(i, 0);

					if(isChecked)
					{
						deletingRowIndexList.add(i);
						checkedRowCount++;
					}
				}

				//우선적으로 체크박스 선택된 행만 삭제하고, 체크한 행이 존재하지 않으면 현재 선택한 행을 삭제함.
				if(checkedRowCount == 0 && selectedRowIdx > -1)
				{
					deletingRowIndexList.add(selectedRowIdx);
				}

				int deletingRowIndexListCount = deletingRowIndexList.size();
				if(deletingRowIndexListCount > 0)
				{
					int resultCode = JOptionPane.showConfirmDialog(AsIsDataMigrator.this, "\uC815\uB9D0 \uC0AD\uC81C\uD558\uC2DC\uACA0\uC2B5\uB2C8\uAE4C?", "DELETE CONFIRM", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

					if(resultCode == JOptionPane.YES_OPTION)
					{
						int deletingRowIdx = -1;
						for(int i=0;i<deletingRowIndexListCount;i++)
						{
							deletingRowIdx = deletingRowIndexList.get(i);
							tm.removeRow(deletingRowIdx);
						}
					}
				}

				if(checkedRowCount == 0 && selectedRowIdx == -1)
				{
					JOptionPane.showMessageDialog(AsIsDataMigrator.this, "\uCCB4\uD06C\uB418\uAC70\uB098 \uC120\uD0DD\uD55C \uD589\uC774 \uC874\uC7AC\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.", "WARN", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		btnDelRow.setMargin(new Insets(2, 2, 2, 2));
		pnContentNorthCenter.add(btnDelRow);

		sppContentCenter = new JSplitPane();
		sppContentCenter.setContinuousLayout(true);
		sppContentCenter.setResizeWeight(0.35);
		sppContentCenter.setOrientation(JSplitPane.VERTICAL_SPLIT);

		tbMigrationTableList = new JTable();

		tbMigrationTableList.setPreferredScrollableViewportSize(new Dimension(450, 70));
		tbMigrationTableList.setRowHeight(60);
		tbMigrationTableList.setToolTipText("AS-IS TO-BE \uB370\uC774\uD130 \uC774\uAD00 \uD14C\uC774\uBE14");
		tbMigrationTableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbMigrationTableList.setFillsViewportHeight(true);
		tbMigrationTableList.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tbMigrationTableList.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"\uC120\uD0DD", "\uC21C\uBC88", "AS-IS \uD14C\uC774\uBE14\uBA85", "AS-IS WHERE \uAD6C\uBB38", "TO-BE \uD14C\uC774\uBE14\uBA85", "\uBCC0\uACBD\uD560 \uCE7C\uB7FC\uAC12(\uCE7C\uB7FC\uC774\uB9841=\uCE7C\uB7FC\uAC121,\uCE7C\uB7FC\uC774\uB9842=\uCE7C\uB7FC\uAC122,...)"
			}
		) {
			private static final long serialVersionUID = -4213291847305164623L;
			Class<?>[] columnTypes = new Class[] {
				Boolean.class, Integer.class, String.class, String.class, String.class, String.class
			};
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		tbMigrationTableList.getColumnModel().getColumn(0).setPreferredWidth(32);
		tbMigrationTableList.getColumnModel().getColumn(0).setMaxWidth(32);
		tbMigrationTableList.getColumnModel().getColumn(1).setPreferredWidth(40);
		tbMigrationTableList.getColumnModel().getColumn(1).setMaxWidth(40);
		tbMigrationTableList.getColumnModel().getColumn(2).setPreferredWidth(180);
		tbMigrationTableList.getColumnModel().getColumn(3).setPreferredWidth(420);
		tbMigrationTableList.getColumnModel().getColumn(4).setPreferredWidth(180);
		tbMigrationTableList.getColumnModel().getColumn(5).setPreferredWidth(300);

		final LineWrapCellRenderer lwcr = new LineWrapCellRenderer();
		TextAreaCellEditor tce = new TextAreaCellEditor();
		tbMigrationTableList.setDefaultRenderer(String.class, lwcr);
		tbMigrationTableList.setDefaultEditor(String.class, tce);
		
		spnMigrationTableList = new JScrollPane(tbMigrationTableList);
		spnMigrationTableList.setPreferredSize(new Dimension(454, 70));

		sppContentCenter.setLeftComponent(spnMigrationTableList);

		tpStatusView = new JTextPane();
		tpStatusView.setBackground(Color.white);
		spnStatus = new JScrollPane(tpStatusView);

		final DefaultCaret dc1 = (DefaultCaret) tpStatusView.getCaret();
		StyleConstants.setBold(attrubuteSet, true);
		StyleConstants.setForeground(attrubuteSet,Color.black);
		dc1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		spnStatus.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
		{
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
	//			JScrollBar srcScroll = (JScrollBar)e.getSource();
	//			srcScroll.setValue(srcScroll.getMaximum());
				dc1.setDot(tpStatusView.getDocument().getLength());
			}
		});

		sppContentCenter.setRightComponent(spnStatus);

		pnContent.add(sppContentCenter, BorderLayout.CENTER);
		
		JPanel pnSystemStatus = new JPanel();
		pnSystemStatus.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		pnSystemStatus.add(progressBar, BorderLayout.CENTER);

		pnMemoryStatus = new MemoryStatusBar();
		FlowLayout fl_pnMemoryStatus = (FlowLayout) pnMemoryStatus.getLayout();
		fl_pnMemoryStatus.setAlignment(FlowLayout.RIGHT);
		fl_pnMemoryStatus.setVgap(0);
		fl_pnMemoryStatus.setHgap(0);
		pnMemoryStatus.setBorder(new EmptyBorder(0, 0, 0, 0));
		pnSystemStatus.add(pnMemoryStatus, BorderLayout.EAST);

		pnContent.add(pnSystemStatus, BorderLayout.SOUTH);
	}

	/**
	 * 데이터 이관 대상 목록을 설정파일로부터 화면테이블에 표기해줌.
	 */
	public void getMigrationDataInfoList()
	{
		String configDir = new File("config").getAbsolutePath();
		String filePath = null;

		try
		{
			filePath = URLDecoder.decode(configDir+File.separator+"MigrationDataInfoList.xml", "UTF-8");
			MigrationDataInfoXmlParser xmlParser = new MigrationDataInfoXmlParser(filePath);

			//MigrationDataInfoList.xml 로부터 이관 설정정보 가져옴.
			List<MigrationDataInfo> migrationDataInfoList = xmlParser.getMigrationDataInfoList();

			//가져온 이관 설정 정보를 화면테이블에 설정
			MigrationDataInfoParser migrationDataInfoParser = new MigrationDataInfoParser();
			migrationDataInfoParser.loadMigrationDataInfoList(tbMigrationTableList, migrationDataInfoList);
		}catch (UnsupportedEncodingException e){}
	}

	/**
	 * 화면 로딩시 - AS-IS, TO-BE 데이터베이스 설정
	 */
	public void setMyBatisConfig()
	{
		Thread th = new Thread(myBatisConfigTask);
		th.start();
	}

	public void setMessageDisplay()
	{
		TextPaneHandler textPaneHandler = new TextPaneHandler(tpStatusView);

		//콘솔 등 기본 핸들러 제거
		Handler[] handlers = logger.getHandlers();
		for(Handler handler : handlers)
		{
			logger.removeHandler(handler);
		} 
		logger.setUseParentHandlers(false);

		LogRecord logRd = new LogRecord(Level.ALL,"");
		Formatter formatter = new UserFormatter();	//시간등 기타 정보는 제외하고 전달받은 순수 메시지만 출력
		formatter.formatMessage(logRd);
		textPaneHandler.setFormatter(formatter);

		logger.addHandler(textPaneHandler);
	}

	public void setMessageConsole(String message,boolean autoCarrigeReturn)
	{
		if(autoCarrigeReturn)
		{
			message = message + "\n";
		}

		logger.info(message);
	}

	public void setMessageConsole(String message)
	{
		setMessageConsole(message,true);
	}

	public JButton getBtnTransfer() {
		return btnTransfer;
	}

	public JCheckBox getChkBatchYn() {
		return chkBatchYn;
	}

	public void setChkBatchYn(JCheckBox chkBatchYn) {
		this.chkBatchYn = chkBatchYn;
	}

	public JCheckBox getChkVPNYn() {
		return chkVPNYn;
	}

	public void setChkVPNYn(JCheckBox chkVPNYn) {
		this.chkVPNYn = chkVPNYn;
	}

	public JTable getTbMigrationTableList() {
		return tbMigrationTableList;
	}

	public JScrollPane getSpnMigrationTableList() {
		return spnMigrationTableList;
	}

	public MyBatisConfigTask getMyBatisConfigTask() {
		return myBatisConfigTask;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
}
