package com.data.migration.ui.table.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;

public class TextAreaCellEditor extends AbstractCellEditor implements TableCellEditor
//extends AbstractCellEditor implements TableCellEditor 
{
	private static final long serialVersionUID = -5126290543790510399L;

	/** The Swing component being edited. */
	private JComponent editorComponent;
	/**
	 * The delegate class which handles all methods sent from the
	 * <code>CellEditor</code>.
	 */
	private EditorDelegate delegate;
	/**
	 * An integer specifying the number of clicks needed to start editing.
	 * Even if <code>clickCountToStart</code> is defined as zero, it
	 * will not initiate until a click occurs.
	 */
	private int clickCountToStart = 1;

	private JTextArea ta = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	/**
	 * Constructs a <code>DefaultCellEditor</code> that uses a text field.
	 *
	 * @param textField  a <code>JTextField</code> object
	 */
	public TextAreaCellEditor()
	{
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);

		scrollPane.setHorizontalScrollBar(scrollPane.createHorizontalScrollBar()); 
		scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
		scrollPane.setBorder(new EmptyBorder(0,0,0,0));
	    
		editorComponent = ta;
		this.clickCountToStart = 2;

		delegate = new EditorDelegate()
		{
			private static final long serialVersionUID = 1L;

			public void setValue(Object value)
			{
				ta.setText((value != null) ? value.toString() : "");
			}

			public Object getCellEditorValue()
			{
				return ta.getText();
			}
		};
//		ta.addKeyListener(delegate);	//Tab키로 현재 선택셀 편집완료 처리(다음셀로 이동됨)
		ta.addFocusListener(delegate);	//셀에서 포커스가 이동해야 편집완료 처리(셀내 Tab 넣을 수 있습니다.)
	}

	/**
	 * Returns a reference to the editor component.
	 *
	 * @return the editor <code>Component</code>
	 */
	public Component getComponent()
	{
		return editorComponent;
	}

	/**
	 * Specifies the number of clicks needed to start editing.
	 *
	 * @param count  an int specifying the number of clicks needed to start editing
	 * @see #getClickCountToStart
	 */
	public void setClickCountToStart(int count)
	{
		clickCountToStart = count;
	}

	/**
	 * Returns the number of clicks needed to start editing.
	 * @return the number of clicks needed to start editing
	 */
	public int getClickCountToStart()
	{
		return clickCountToStart;
	}

//
//  Override the implementations of the superclass, forwarding all methods 
//  from the CellEditor interface to our delegate. 
//

	/**
	 * Forwards the message from the <code>CellEditor</code> to
	 * the <code>delegate</code>.
	 * @see EditorDelegate#getCellEditorValue
	 */
	public Object getCellEditorValue()
	{
		return delegate.getCellEditorValue();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to
	 * the <code>delegate</code>.
	 * @see EditorDelegate#isCellEditable(EventObject)
	 */
	public boolean isCellEditable(EventObject anEvent)
	{
		return delegate.isCellEditable(anEvent); 
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to
	 * the <code>delegate</code>.
	 * @see EditorDelegate#shouldSelectCell(EventObject)
	 */
	public boolean shouldSelectCell(EventObject anEvent)
	{
		return delegate.shouldSelectCell(anEvent); 
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to
	 * the <code>delegate</code>.
	 * @see EditorDelegate#stopCellEditing
	 */
	public boolean stopCellEditing()
	{
		return delegate.stopCellEditing();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to
	 * the <code>delegate</code>.
	 * @see EditorDelegate#cancelCellEditing
	 */
	public void cancelCellEditing()
	{
		delegate.cancelCellEditing();
	}

//
//  Implementing the CellEditor Interface
//
	/** Implements the <code>TableCellEditor</code> interface. */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		delegate.setValue(value);

		scrollPane.setToolTipText(value != null ? value.toString() : "");

		return scrollPane;
	}

	/**
	 * The protected <code>EditorDelegate</code> class.
	 */
	private class EditorDelegate implements ActionListener, ItemListener, KeyListener, FocusListener, Serializable
	{

		private static final long serialVersionUID = 2404541323424660982L;

		/**  The value of this cell. */
		protected Object value;

		/**
		 * Returns the value of this cell. 
		 * @return the value of this cell
		 */
		public Object getCellEditorValue()
		{
			return value;
		}

		/**
		 * Sets the value of this cell. 
		 * @param value the new value of this cell
		 */
		public void setValue(Object value)
		{
			this.value = value; 
		}

		/**
		 * Returns true if <code>anEvent</code> is <b>not</b> a
		 * <code>MouseEvent</code>.  Otherwise, it returns true
		 * if the necessary number of clicks have occurred, and
		 * returns false otherwise.
		 *
		 * @param   anEvent		 the event
		 * @return  true  if cell is ready for editing, false otherwise
		 * @see #setClickCountToStart
		 * @see #shouldSelectCell
		 */
		public boolean isCellEditable(EventObject anEvent)
		{
			if (anEvent instanceof MouseEvent)
			{
				return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
			}
			return true;
		}
		
		/**
		 * Returns true to indicate that the editing cell may
		 * be selected.
		 *
		 * @param   anEvent		 the event
		 * @return  true 
		 * @see #isCellEditable
		 */
		public boolean shouldSelectCell(EventObject anEvent)
		{
			return true; 
		}

		/**
		 * Returns true to indicate that editing has begun.
		 *
		 * @param anEvent		  the event
		 */
		@SuppressWarnings("unused")
		public boolean startCellEditing(EventObject anEvent)
		{
			return true;
		}

		/**
		 * Stops editing and
		 * returns true to indicate that editing has stopped.
		 * This method calls <code>fireEditingStopped</code>.
		 *
		 * @return  true 
		 */
		public boolean stopCellEditing()
		{
			fireEditingStopped(); 
			return true;
		}

		/**
		 * Cancels editing.  This method calls <code>fireEditingCanceled</code>.
		 */
		public void cancelCellEditing()
		{
			fireEditingCanceled(); 
		}

		/**
		 * When an action is performed, editing is ended.
		 * @param e the action event
		 * @see #stopCellEditing
		 */
		public void actionPerformed(ActionEvent e)
		{
			TextAreaCellEditor.this.stopCellEditing();
		}

		/**
		 * When an item's state changes, editing is ended.
		 * @param e the action event
		 * @see #stopCellEditing
		 */
		public void itemStateChanged(ItemEvent e)
		{
			TextAreaCellEditor.this.stopCellEditing();
		}

		/**
		 * When an key is input, editing is ended.
		 * @param e the action event
		 * @see #stopCellEditing
		 */
		public void keyTyped(KeyEvent e)
		{
			keyPressed(e);
		}

		/**
		 * When an key is input, editing is ended.
		 * @param e the action event
		 * @see #stopCellEditing
		 */
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_TAB)
			{
				TextAreaCellEditor.this.stopCellEditing();
			}
		}

		/**
		 * When an key is input, editing is ended.
		 * @param e the action event
		 */
		public void keyReleased(KeyEvent e){}

		@Override
		public void focusGained(FocusEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void focusLost(FocusEvent e)
		{
			System.out.println("EditorDelegate focusLost");
			TextAreaCellEditor.this.stopCellEditing();
		}
	}
}
