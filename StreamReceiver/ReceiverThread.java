package com;
public class ReceiverThread extends Thread{
	StreamProcessor sp;
public ReceiverThread(StreamProcessor sp){
	this.sp = sp;
	start();
}
public void run(){
	sp.startStreamer();
}
}