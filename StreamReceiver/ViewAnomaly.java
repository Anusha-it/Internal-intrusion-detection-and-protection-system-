package com;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import java.awt.Font;
import javax.swing.table.TableColumn;
public class ViewAnomaly extends JFrame{
	DefaultTableModel dtm;
	JScrollPane jsp;
	JTable table;
public ViewAnomaly(){
	setTitle("View Anomaly Signature");
	dtm = new DefaultTableModel(){
		public boolean isCellEditable(){
			return false;
		}
	};
	dtm.addColumn("Signature");
	dtm.addColumn("Detection");
	
	table = new JTable(dtm);
	table.getTableHeader().setFont(new Font("Courier New",Font.BOLD,15));
	table.setFont(new Font("Courier New",Font.BOLD,14));
	table.setRowHeight(30);
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.getColumnModel().getColumn(0).setPreferredWidth(850);
	table.getColumnModel().getColumn(1).setPreferredWidth(150);
	jsp = new JScrollPane(table);
	getContentPane().add(jsp);
}
}