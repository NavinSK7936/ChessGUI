package user;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

public class ATMTable extends JPanel {
	public TableModel tableModel;
	public JTable table;
	
	public ATMTable(String[] columnNames, Dimension dimension) {
		tableModel = new TableModel(columnNames);
		table = new JTable(tableModel);
		
		table.setPreferredScrollableViewportSize(dimension);
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		super.add(scrollPane);}
	
	public void setValueAt(Object value, int row, int col) {
		this.tableModel.setValueAt(value, row, col);}
	
	public int getRowCount() {
		return this.tableModel.getRowCount();}
	
	public void appendRow(Object[] values) {
		this.tableModel.appendRow(values);}
	
	class TableModel extends AbstractTableModel {
		private String[] columnNames;
		
		private ArrayList<Object[]> data = new ArrayList<Object[]>();
		
		TableModel(String[] columnNames) {
			this.columnNames = columnNames;}
		
		@Override public int getColumnCount() {
			return columnNames.length;}
		
		@Override public int getRowCount() {
			return data.size();}
		
		@Override public String getColumnName(int col) {
			return columnNames[col];}
		
		@Override public Object getValueAt(int row, int col) {
			return data.get(row)[col];}
		
		@Override public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();}
		
		@Override public boolean isCellEditable(int row, int col) {
        		return false;}
		
		@Override public void setValueAt(Object value, int row, int col) {
			data.get(row)[col] = value;
			super.fireTableCellUpdated(row, col);}
		
		public void appendRow(Object values[]) {
			data.add(values);
			int row = this.getRowCount();
			for(int column = 0; column < this.getColumnCount(); column++)
        		super.fireTableCellUpdated(row, column);
	       	super.fireTableRowsInserted(row, row);}
	}
}