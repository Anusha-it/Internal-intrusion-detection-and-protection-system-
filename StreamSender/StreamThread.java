package com;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
public class StreamThread extends Thread{
	DefaultTableModel dtm;
	ArrayList<String> stream;
	int size;
public StreamThread(DefaultTableModel dtm,ArrayList<String> stream,int size){
	this.dtm = dtm;
	this.stream = stream;
	this.size = size;
}
public void run(){
	try{
		int index = 0;
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<stream.size();i++){
			String input = stream.get(i);
			sb.append(input+System.getProperty("line.separator"));
			index = index + 1;
			if(index == size){
				Socket socket = new Socket("localhost",3333);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				Object req[] = {"request",sb.toString()};
				out.writeObject(req);
				out.flush();
				Object row[] = {"stream size "+index+" sent to stream processor"};
				dtm.addRow(row);
				Object res[] = (Object[])in.readObject();
				String type = (String)res[0];
				index = 0;
				sb.delete(0,sb.length());
			}
			sleep(100);
		}
		Socket socket = new Socket("localhost",3333);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		Object req[] = {"finish"};
		out.writeObject(req);
		out.flush();
	}catch(Exception e){
		e.printStackTrace();
	}
}
}