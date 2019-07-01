package com;
import java.io.FileReader;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
public class SVM {
	static Instances train;
	static int lastIndex;
	static Evaluation eval;
	static SVMAlgorithm nn;
	static LinkedHashMap<String,ArrayList<String>> classify = new LinkedHashMap<String,ArrayList<String>>();
public static void svmTrain(File trainFile){
	try{
		classify.clear();
		FileReader reader = new FileReader(trainFile); 
		train = new Instances(reader);
		lastIndex = train.numAttributes() - 1;
		train.setClassIndex(lastIndex);

		nn = new SVMAlgorithm();
		nn.buildClassifier(train);
		eval = new Evaluation(train);
		
	}catch(Exception e){
		e.printStackTrace();
	}
}

public static void svmTest(File testFile,StreamProcessor sp){
	try{
		clear(sp);
		FileReader reader = new FileReader(testFile); 
		Instances test = new Instances(reader);
		test.setClassIndex(test.numAttributes() - 1);
		eval.evaluateModel(nn,test);

		for(int i=0;i<test.numInstances();i++){
			double index = nn.classifyInstance(test.instance(i));
			String className = train.attribute(lastIndex).value((int)index);
			Object row[] = {test.instance(i),className};
			sp.dtm.addRow(row);
			if(classify.containsKey(className)){
				classify.get(className).add(test.instance(i).toString());
			} else {
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(test.instance(i).toString());
				classify.put(className,temp);
			}
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}

public static void clear(final StreamProcessor sp){
	Thread th = new Thread() {
		@Override
		public void run() {
			sp.clearTable();
		}
	};
	th.start();
}
}