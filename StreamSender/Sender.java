package com;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.UIManager;
public class Sender extends JFrame{
	JPanel p1,p2,p3;
	JLabel l1;
	JScrollPane jsp;
	JTable table;
	Font f1,f2;
	JButton b1,b2,b3;
	StreamThread thread;
	DefaultTableModel dtm;
	JFileChooser chooser;
	File file;
	ArrayList<String> stream = new ArrayList<String>();
	static ArrayList<String> time = new ArrayList<String>();
public Sender(){
	setTitle("Stream Data Sender");
	p1 = new JPanel();
    l1 = new JLabel("<html><body><center><font size=4 color=#f5ea01>IDS Signature Stream Sender For Anomaly Detection</font></center></body></html>".toUpperCase());
	l1.setForeground(Color.white);
    p1.add(l1);
    p1.setBackground(new Color(204, 110, 155));

    f2 = new Font("Courier New", 1, 13);
    p2 = new JPanel();
    p2.setLayout(new BorderLayout());
	dtm = new DefaultTableModel(){
		public boolean isCellEditable(int r,int c){
			return false;
		}
	};
    table = new JTable(dtm);
	table.getTableHeader().setFont(new Font("Courier New", 1, 14));
    table.setFont(f2);
    table.setRowHeight(30);
	jsp = new JScrollPane(table);
    p2.add(jsp);
	dtm.addColumn("Stream Details");

	chooser = new JFileChooser(new File("dataset"));
	
	p3 = new JPanel();
	b1 = new JButton("Upload Dataset");
	b1.setFont(f2);
	p3.add(b1);
	b1.addActionListener(new ActionListener(){
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			clearTable();
			int option = chooser.showOpenDialog(Sender.this);
			if(option == chooser.APPROVE_OPTION){
				file = chooser.getSelectedFile();
				readFile();
				JOptionPane.showMessageDialog(Sender.this,"Stream file loaded");
			}
		}
	});

	b1 = new JButton("Send Data");
	b1.setFont(f2);
	p3.add(b1);
	b1.addActionListener(new ActionListener(){
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			int size = 0;
			String option = JOptionPane.showInputDialog(Sender.this,"Please enter stream size");
			try{
				size = Integer.parseInt(option.trim());
			}catch(NumberFormatException nfe){
				JOptionPane.showMessageDialog(Sender.this,"Stream size must be numeric only");
				return;
			}
			time.clear();
			thread = new StreamThread(dtm,stream,size);
			thread.start();
		}
	});

	getContentPane().add(p1, "North");
    getContentPane().add(p2, "Center");
	getContentPane().add(p3, "South");
}
public static void main(String a[])throws Exception{
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	Sender sender = new Sender();
	sender.setVisible(true);
	sender.setSize(800,500);
}
public void clearTable(){
	for(int i=dtm.getRowCount()-1;i>=0;i--){
		dtm.removeRow(i);
	}
}
public void readFile(){
	try{
		stream.clear();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = br.readLine())!=null){
			stream.add(line.trim());
		}
		br.close();
	}catch(Exception e){
		e.printStackTrace();
	}
}
}