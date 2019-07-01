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
import java.net.Socket;
import javax.swing.JButton;
import java.net.ServerSocket;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.UIManager;
import java.util.ArrayList;
import org.jfree.ui.RefineryUtilities;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.JFileChooser;
import java.awt.Cursor;
import java.util.Map;
public class StreamProcessor extends JFrame{
	JPanel p1,p2,p3;
	JLabel l1;
	JScrollPane jsp;
	JTable table;
	Font f1,f2;
	StreamData thread;
	DefaultTableModel dtm;
	JButton b1,b2,b3,b4;
	ServerSocket server;
	StringBuilder attributes = new StringBuilder();
	File train;
	JFileChooser chooser;
	int index;
	static int normal,anomaly;
public void readAttributes(){
	try{
		BufferedReader br = new BufferedReader(new FileReader("attributes.txt"));
		String line = null;
		while((line = br.readLine()) != null){
			line = line.trim();
			if(line.length() > 0)
				attributes.append(line+System.getProperty("line.separator"));
		}
		br.close();
	}catch(Exception e){
		e.printStackTrace();
	}
}

public void startStreamer(){
	try{
		server = new ServerSocket(3333);
		while(true){
			Socket socket = server.accept();
			thread = new StreamData(socket,dtm,this);
			thread.start();
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}
public StreamProcessor(){
	setTitle("Stream Data Receiver");
	p1 = new JPanel();
    l1 = new JLabel("<html><body><center><font size=6 color=#f5ea01>Data Mining for Security Applications</font></center></body></html>".toUpperCase());
	l1.setForeground(Color.white);
    p1.add(l1);
    p1.setBackground(new Color(204, 110, 155));

	chooser = new JFileChooser(new File("dataset"));

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
	dtm.addColumn("Stream Signature Records");
	dtm.addColumn("Detection Result");
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.getColumnModel().getColumn(0).setPreferredWidth(850);
	table.getColumnModel().getColumn(1).setPreferredWidth(150);

	p3 = new JPanel();

	b1 = new JButton("Upload Training Signature");
	b1.setFont(f2);
	p3.add(b1);
	b1.addActionListener(new ActionListener(){
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			int option = chooser.showOpenDialog(StreamProcessor.this);
			if(option == chooser.APPROVE_OPTION) {
				train = chooser.getSelectedFile();
			}
			JOptionPane.showMessageDialog(StreamProcessor.this,"Training file loaded");
		}
	});

	b2 = new JButton("Generate SVM Model");
	b2.setFont(f2);
	p3.add(b2);
	b2.addActionListener(new ActionListener(){
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglassCursor);
			SVM.svmTrain(train);
			Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
			setCursor(normalCursor);
			JOptionPane.showMessageDialog(StreamProcessor.this,"SVM training model signature generated");
		}
	});

	b3 = new JButton("View Detections");
	b3.setFont(f2);
	p3.add(b3);
	b3.addActionListener(new ActionListener(){
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			anomaly = 0;
			normal = 0;
			ViewAnomaly va = new ViewAnomaly();
			for(Map.Entry<String,ArrayList<String>> me : SVM.classify.entrySet()){
				String key = me.getKey();
				ArrayList<String> list = me.getValue();
				System.out.println(key+" "+list.size());
				if(key.equals("anomaly")){
					for(int i=0;i<list.size();i++){
						Object row[] = {list.get(i),"Anomaly"};
						va.dtm.addRow(row);
					}
					anomaly = list.size();
				}
				if(key.equals("normal")){
					normal = list.size();
				}
			}
			va.setSize(800,400);
			va.setVisible(true);
		}
	});

	b4 = new JButton("View Detections Chart");
	b4.setFont(f2);
	p3.add(b4);
	b4.addActionListener(new ActionListener(){
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			Chart chart1 = new Chart("View Detections Chart");
			chart1.pack();
			RefineryUtilities.centerFrameOnScreen(chart1);
			chart1.setVisible(true);
		}
	});

	getContentPane().add(p1, "North");
    getContentPane().add(p2, "Center");
	getContentPane().add(p3, "South");
	
	
}
public static void main(String a[])throws Exception{
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	StreamProcessor sp = new StreamProcessor();
	sp.readAttributes();
	sp.setVisible(true);
	sp.setExtendedState(JFrame.MAXIMIZED_BOTH);
	new ReceiverThread(sp);
}
public void clearTable(){
	for(int i=dtm.getRowCount()-1;i>=0;i--){
		dtm.removeRow(i);
	}
}
public void finish(){
	JOptionPane.showMessageDialog(StreamProcessor.this,"Data streaming over");
}
}