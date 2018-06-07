package com.data.migration.ui.progress;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;
import javax.swing.Timer;

public class ProcProgressBarHandler
{
	private Timer timer = null;
	private int asIsTabledataRowNum = 0;
	private JProgressBar pb = null;

	public ProcProgressBarHandler(JProgressBar pb,String asIsTableName,String tobeTableName,int asIsTableDataRowCount)
	{
		this.pb = pb;
		timer = new Timer(350, new ProgressTask(pb,asIsTableName,tobeTableName,asIsTableDataRowCount));
	}

	public void start()
	{
		if(timer != null && !timer.isRunning())
		{
			timer.start();
		}
	}

	public int getAsIsTabledataRowNum() {
		return asIsTabledataRowNum;
	}

	public void setAsIsTabledataRowNum(int asIsTabledataRowNum) {
		this.asIsTabledataRowNum = asIsTabledataRowNum;
	}

	public void stop()
	{
		if(timer != null && timer.isRunning())
		{
			timer.stop();
			timer = null;

			//진행률 초기화
			pb.setValue(0);
			pb.setString("");

			Rectangle pbRec1 = pb.getBounds();
			pbRec1.x = 0;
			pbRec1.y = 0;
			pb.paintImmediately(pbRec1);
		}
	}

	private class ProgressTask implements ActionListener
	{
		private JProgressBar pb = null;
		private String asIsTableName = null;
		private String tobeTableName = null;
		private int asIsTableDataRowCount = -1;
		private String progressLabelStr = null;

		public ProgressTask(JProgressBar pb,String asIsTableName,String tobeTableName,int asIsTableDataRowCount)
		{
			this.pb = pb;
			this.asIsTableName = asIsTableName;
			this.tobeTableName =tobeTableName;
			this.asIsTableDataRowCount = asIsTableDataRowCount;

			this.progressLabelStr = this.asIsTableName + " --> "+ this.tobeTableName+" ";

			//데이터 이관 표기 초기화
			this.pb.setStringPainted(true);
			this.pb.setMinimum(0);
			this.pb.setMaximum(100);
			this.pb.setValue(0);

			this.pb.setString(this.progressLabelStr + "0% (" + asIsTabledataRowNum + "/" + this.asIsTableDataRowCount + ") ");

			Rectangle pbRec = this.pb.getBounds();
			pbRec.x = 0;
			pbRec.y = 0;
			this.pb.paintImmediately(pbRec);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int procPercent = (int)((100*asIsTabledataRowNum)/asIsTableDataRowCount);
			pb.setValue(procPercent);
			pb.setString(this.progressLabelStr+procPercent + "% (" + asIsTabledataRowNum + "/" + asIsTableDataRowCount + ") ");

			Rectangle pbRec = pb.getBounds();
			pbRec.x = 0;
			pbRec.y = 0;
			pb.paintImmediately(pbRec);

			if(procPercent >= 99)
			{
				timer.stop();
				timer = null;

				//진행률 초기화
				pb.setValue(0);
				pb.setString("");

				Rectangle pbRec1 = pb.getBounds();
				pbRec1.x = 0;
				pbRec1.y = 0;
				pb.paintImmediately(pbRec1);
			}
		}
	}
}
