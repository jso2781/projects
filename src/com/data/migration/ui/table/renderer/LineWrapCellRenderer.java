package com.data.migration.ui.table.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class LineWrapCellRenderer extends JTextArea implements TableCellRenderer
{
	private static final long serialVersionUID = 2289481516070650744L;
	private Border focusCellHighlightBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
//	private Border focusCellForegroundBorder = UIManager.getBorder("Table.focusCellForeground");
//	private Border focusCellBackgroundBorder = UIManager.getBorder("Table.focusCellBackground");

	private Color foregroundColor =  UIManager.getColor("Table.foreground");
	private Color backgroundColor =  UIManager.getColor("Table.background");
	private Color selectionForegroundColor =  UIManager.getColor("Table.selectionForeground");
	private Color selectionBackgroundColor =  UIManager.getColor("Table.selectionBackground");
	private Color focusCellForegroundColor =  UIManager.getColor("Table.focusCellForeground");
	private Color focusCellBackgroundColor =  UIManager.getColor("Table.focusCellBackground");
	private Border eb = new EmptyBorder(1, 2, 1, 2);
	
	public LineWrapCellRenderer()
	{
		 setOpaque(true);
		 setLineWrap(true);
		 setWrapStyleWord(true);
//		 DefaultCaret c = (DefaultCaret) getCaret();
//		 c.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) 
	{
		if(isSelected)
		{
//			setForeground(new Color(250, 0, 250));
			setForeground(selectionForegroundColor);
			setBackground(selectionBackgroundColor);
		}else
		{
			setForeground(new Color(80,90,155));
			setForeground(foregroundColor);
			setBackground(backgroundColor);
		}

//		setFont(table.getFont());
//
		if(hasFocus)
		{
			setBorder(focusCellHighlightBorder);
			if(table.isCellEditable(row, column))
			{
				setForeground(focusCellForegroundColor);
				setBackground(focusCellBackgroundColor);
			}
		}else
		{
			setBorder(eb);
		}

//		if(isNumber(value))
//		{
//			setAlignmentX(Component.RIGHT_ALIGNMENT);
//		}else
//		{
//			setAlignmentX(Component.LEFT_ALIGNMENT);
//		}

		setText((value != null) ? value.toString() : "");

		return this;
	}
	

//	private boolean isNumber(Object v)
//	{
//		boolean isNum = false;
//		
//		try
//		{
//			if(v instanceof String)
//			{
//				Double.parseDouble((String)v);
//				isNum = true;
//			}else if(v instanceof Integer || v instanceof Long || v instanceof Float || v instanceof Double)
//			{
//				isNum = true;
//			}
//		}catch(NumberFormatException nfe)
//		{
//			return false;
//		}
//
//		return isNum;
//	}
}
