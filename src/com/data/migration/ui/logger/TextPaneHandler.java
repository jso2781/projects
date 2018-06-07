package com.data.migration.ui.logger;

import java.awt.Color;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * @author JOSEONGOK
 *
 */
public class TextPaneHandler extends StreamHandler
{
	private JTextPane textPane = null;
	private SimpleAttributeSet attrubuteSet = new SimpleAttributeSet();

	public TextPaneHandler(JTextPane textPane)
	{
		this.textPane = textPane;
		StyleConstants.setBold(attrubuteSet, true);
		StyleConstants.setForeground(attrubuteSet,Color.black);
	}

	public void setTextPane(JTextPane textPane)
	{
		this.textPane = textPane;
		StyleConstants.setBold(attrubuteSet, true);
		StyleConstants.setForeground(attrubuteSet,Color.black);
	}

	@Override
	public synchronized void publish(LogRecord record)
	{
//		super.publish(record);
//		flush();

		StyledDocument doc = textPane.getStyledDocument();

//		textPane.setCaretPosition(doc.getLength());
		try{doc.insertString(doc.getLength(),getFormatter().format(record),attrubuteSet);}catch(BadLocationException e){}
	}

}
