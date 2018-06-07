package com.data.migration.ui.table.renderer;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableCellWidthUtils
{
	/** �⺻ TABLE MODEL���� WIDTH�� �߰��Ѵ� */ 
    public static int APPEND_INSETS = 20;
    
    /**
     * ���̺��� �÷��� �о�ͼ� ���, ������ �� ����� ������ ��ŭ �����ִ� Utils claass
     * 
     * @param table			table
     * @param columnIndex	�÷� index
     * @param columnHeadName �÷� �����
     * 
     * @return
     */
    public static int calculateColumnWidth(JTable table, int columnIndex, String columnHeadName)
    {
        int width = calculateColumnWidth(table, columnIndex);
        TableCellRenderer renderer = table.getCellRenderer(0, columnIndex);
        Component comp = renderer.getTableCellRendererComponent(table,columnHeadName,false,false,0,columnIndex);
        
        int thisWidth = comp.getPreferredSize().width;
        
        if(thisWidth > width)
        {
            width = thisWidth;
        }
        return width;
    }

    public static int calculateColumnWidth(JTable table, int columnIndex, int currentColumnWidth)
    {
        int width = calculateColumnWidth(table, columnIndex);
        int thisWidth = currentColumnWidth;
        
        if(thisWidth > width)
        {
            width = thisWidth;
        }
        return width;
    }
    /**
     * ���̺��� �÷��� �о�ͼ� �������� ������ ��ŭ �����ִ� Utils claass
     * 
    * @param table			table
     * @param columnIndex	�÷� index
    * @return
     */
    public static int calculateColumnWidth(JTable table, int columnIndex)
    {
        int width = 0;
        int rowCount = table.getRowCount();

        for(int i = 0; i < rowCount; i++)
        {
            TableCellRenderer renderer = table.getCellRenderer(i, columnIndex);
            Component comp = renderer.getTableCellRendererComponent(table,table.getValueAt(i, columnIndex),false,false,i,columnIndex);
            
            int thisWidth = comp.getPreferredSize().width;
            
            if(thisWidth > width)
            {
                width = thisWidth;
            }
        }

        return width;
    }

    /**
     * ���̺��� default �÷� ����� �����Ѵ�.
     * 
     * @param table
     * @param insets
     * @param setMinimum
     * @param setMaximum
     */
    public static void setColumnWidths(JTable table,Insets insets,boolean setMinimum,boolean setMaximum)
    {
        int columnCount = table.getColumnCount();
        TableColumnModel tcm = table.getColumnModel();
        int spare = (insets == null ? 0 : insets.left + insets.right);
        
        for(int i = 0; i < columnCount; i++)
        {
        	int columnPreferredWidth = table.getColumnModel().getColumn(i).getPreferredWidth();
//        	int width = calculateColumnWidth(table, i,table.getColumnName(i));
        	int width = calculateColumnWidth(table, i,columnPreferredWidth);
            width += spare;

            TableColumn column = tcm.getColumn(i);
            column.setPreferredWidth(width);
            if (setMinimum == true) {
                column.setMinWidth(width);
            }
            if (setMaximum == true) {
                column.setMaxWidth(width);
            }
        }
    }
}
