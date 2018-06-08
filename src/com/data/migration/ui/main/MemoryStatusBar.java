/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package com.data.migration.ui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

@SuppressWarnings("serial")
public class MemoryStatusBar extends JPanel implements PropertyChangeListener
{
//	private MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

	//시간간격(밀리초 400)
	int sleepMiliSec = 400;

	private Runtime jvm = Runtime.getRuntime();
	private JProgressBar progressBar;
	private JProgressBar memoryBar;
//	private JLabel messageText;
	private JButton btnGc;

//	private JLabel totalMemoryText;
//	private TaskProgress task;
	
	public MemoryStatusBar()
	{
//		progressBar = new JProgressBar();
		memoryBar = new JProgressBar();
		
//		messageText = new JLabel();
		btnGc = new JButton("GC");
//		totalMemoryText = new JLabel("Total " + (Runtime.getRuntime().maxMemory()/1024/1024) + "MB");

//		progressBar.setMaximumSize(new Dimension(32767, 20));
//		progressBar.setMinimumSize(new Dimension(114, 20));
//		progressBar.setPreferredSize(new Dimension(394, 20));
//
//		memoryBar.setMaximumSize(new Dimension(32767, 20));
//		memoryBar.setMinimumSize(new Dimension(114, 20));
//		memoryBar.setPreferredSize(new Dimension(394, 20));
//
//		messageText.setMaximumSize(new Dimension(10, 20));
//		messageText.setMinimumSize(new Dimension(10, 20));
//		messageText.setPreferredSize(new Dimension(10, 20));
//
//		totalMemoryText.setMaximumSize(new Dimension(32767, 20));
//		totalMemoryText.setMinimumSize(new Dimension(114, 20));
//		totalMemoryText.setPreferredSize(new Dimension(394, 20));

		btnGc.setMaximumSize(new Dimension(30, 20));
		btnGc.setMinimumSize(new Dimension(30, 20));
		btnGc.setPreferredSize(new Dimension(30, 20));
		btnGc.setMargin(new Insets(0, 0, 0, 0));

//		setLayout(new GridLayout(1,0));
//		add(messageText);
//		add(Box.createHorizontalGlue());
		add(memoryBar);
//		add(totalMemoryText);
		add(btnGc);
//		add(progressBar);
		
		btnGc.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.gc();
			}
		});

//		MemoryDisplay memory = new MemoryDisplay(memoryBar);
//		new Timer(400, memory).start();

		ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new MemoryDisplayRunnable(memoryBar), 0, sleepMiliSec, TimeUnit.MILLISECONDS);
	}
	
//	public void showMessage(String text) {
//		messageText.setText(text);
//	}
	
//	public void startTimer(long duration, int interval, TimeUnit unit) {
//		progressBar.setEnabled(true);
//		if (duration > 0) {
//			progressBar.setStringPainted(true);
//			progressBar.setMaximum(100);
//			progressBar.setMinimum(0);
//			task = new TaskProgress(unit.toMillis(duration), interval);
//			task.addPropertyChangeListener(this);
//			task.execute();
//		} else {
//			progressBar.setStringPainted(false);
//			progressBar.setIndeterminate(true);
//			task = new TaskProgress(duration, interval);
//			task.addPropertyChangeListener(this);
//			task.execute();
//		}
//	}
	
	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			progressBar.setValue((Integer)evt.getNewValue());
		} 
	}

	
//	public void stopTimer() {
//		if (task != null) {
//			task.cancel(true);
//			task = null;
//		}
//		progressBar.setIndeterminate(false);
//		progressBar.setString("");
//		progressBar.setEnabled(false);
//	}
	
	/*
	 * Emits progress property from a background thread.
	 */
//	class TaskProgress extends SwingWorker<Void, Integer> {
//		private long startTimeInMillis;
//		private long _total = 100;
//		private int _interval = 100;
//		
//		public TaskProgress(long total, int interval) {
//			_total	= Math.max(total, 1);
//			_interval = Math.max(interval, 1);
//		}
//		@Override
//		public Void doInBackground() {
//			startTimeInMillis = System.currentTimeMillis();
//			long endTimeInMillis = startTimeInMillis + _total;
//			long current = System.currentTimeMillis();
//			while (current < endTimeInMillis && !isCancelled()) {
//				try {
//					current = System.currentTimeMillis();
//					int pctComplete = (int)((100*(current - startTimeInMillis))/_total);
//					setProgress(pctComplete);
//					Thread.sleep(_interval);
//				} catch (InterruptedException ignore) {
//				}
//			}
//			return null;
//		}
//	}

	public class MemoryDisplayRunnable implements Runnable
	{
		private JProgressBar bar = null;

		public MemoryDisplayRunnable(JProgressBar bar)
		{
			this.bar = bar;
			bar.setStringPainted(true);
			bar.setMinimum(0);
			bar.setMaximum(100);
		}

		@Override
		public void run()
		{
			try
			{
				long totalMemory = jvm.totalMemory();
				long usedMemory = totalMemory-jvm.freeMemory();
				int usedPct = (int)((100*usedMemory)/totalMemory);
				bar.setForeground(getColor(usedPct));
				bar.setValue((int)usedPct);
				bar.setString(usedPct + "% (" + mb(usedMemory) + "/" + mb(totalMemory) + "MB) ");
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		private long mb(long m)
		{
			return m/1024/1024;
		}

		Color getColor(int pct)
		{
			int red = 255*pct/100;
			int green = 255*(100-pct)/100;
			return new Color(red, green, 0);
		}
	}

	public class MemoryDisplay implements ActionListener
	{
		private JProgressBar bar = null;

		public MemoryDisplay(JProgressBar bar)
		{
			this.bar = bar;
			bar.setStringPainted(true);
			bar.setMinimum(0);
			bar.setMaximum(100);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			long totalMemory = jvm.totalMemory();
			long usedMemory = totalMemory-jvm.freeMemory();
			int usedPct = (int)((100*usedMemory)/totalMemory);
			bar.setForeground(getColor(usedPct));
			bar.setValue((int)usedPct);
			bar.setString(usedPct + "% (" + mb(usedMemory) + "/" + mb(totalMemory) + "MB) ");

//			MemoryUsage mu = memoryBean.getHeapMemoryUsage();
//			long maxHeapMemory = mu.getMax();
//			long usedHeapMemory = mu.getUsed();
//			int usedPct = (int)((100*usedHeapMemory)/maxHeapMemory);
//			bar.setForeground(getColor(usedPct));
//			bar.setValue((int)usedPct);
//			bar.setString(usedPct + "% (" + mb(usedHeapMemory) + "/" + mb(maxHeapMemory) + "MB) ");
		}
		
		private long mb(long m)
		{
			return m/1024/1024;
		}

		Color getColor(int pct)
		{
			int red = 255*pct/100;
			int green = 255*(100-pct)/100;
			return new Color(red, green, 0);
		}
	}
	
}
