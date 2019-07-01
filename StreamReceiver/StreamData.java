package com;
import java.net.Socket;
import javax.swing.table.DefaultTableModel;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileWriter;
import java.io.File;
public class StreamData extends Thread{
	Socket socket;
	DefaultTableModel dtm;
	ObjectInputStream in;
	ObjectOutputStream out;
	StreamProcessor sp;
public void save(String name,String data)throws Exception{
	FileWriter fw = new FileWriter("process/"+name+".txt");
	fw.write(data);
	fw.close();
}
public StreamData(Socket socket,DefaultTableModel dtm,StreamProcessor sp){
	this.socket = socket;
	this.dtm = dtm;
	this.sp = sp;
	try{
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}catch(Exception e){
		e.printStackTrace();
	}
}
public void run(){
	try{
		Object input[] = (Object[])in.readObject();
		if(input != null){
			String type = (String)input[0];
			if(type.equals("request")){
				String signature = (String)input[1];
				FileWriter fw = new FileWriter("test.arff");
				fw.write(sp.attributes.toString()+signature);
				fw.close();
				SVM.svmTest(new File("test.arff"),sp);
				Object res[] = {"process"};
				out.writeObject(res);
				out.flush();
				sp.index = sp.index + 1;
			}
			if(type.equals("finish")){
				sp.finish();
			}
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}
}