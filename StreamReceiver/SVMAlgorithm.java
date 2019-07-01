package com;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.filters.*;
import weka.filters.unsupervised.attribute.*;
import weka.classifiers.*;
import libsvm.*;
import java.util.*;
public class SVMAlgorithm extends Classifier implements WeightedInstancesHandler {
	protected static final long serialVersionUID = 14172;
	protected svm_parameter param; // LibSVM oprions
	protected int normalize; // normalize input data
	protected svm_problem prob; // LibSVM Problem
	protected svm_model model; // LibSVM Model
	protected String error_msg;
	protected Filter filter = null;
public SVMAlgorithm() {
	param = new svm_parameter();
	param.svm_type = svm_parameter.C_SVC;
	param.kernel_type = svm_parameter.RBF;
	param.degree = 3;
	param.gamma = 0;
	param.coef0 = 0;
	param.nu = 0.5;
	param.cache_size = 40;
	param.C = 1;
	param.eps = 1e-3;
	normalize = 0;
	param.p = 0.1;
	param.shrinking = 1;
	param.probability = 0;
	param.nr_weight = 0;
	param.weight_label = new int[0];
	param.weight = new double[0];        
}
protected static double atof(String s) {
	return Double.valueOf(s).doubleValue();
}
protected static int atoi(String s) {
	return Integer.parseInt(s);
}
protected String InstanceToSparse(Instance instance) {
	String line = new String();
	int c = (int) instance.classValue();
	if (c == 0)
		c = -1;
	line = c + " ";
	for (int j = 1; j < instance.numAttributes(); j++) {
		if (j-1 == instance.classIndex()) {				
			continue;
		}
		if (instance.isMissing(j-1)) 
			continue;
		if (instance.value(j - 1) != 0)
			line += " " + j + ":" + instance.value(j - 1);
	}
	return (line + "\n");
}
protected Vector DataToSparse(Instances data) {
	Vector sparse = new Vector(data.numInstances() + 1);
	for (int i = 0; i < data.numInstances(); i++) { // for each instance
		sparse.add(InstanceToSparse(data.instance(i)));
	}
	return sparse;
}
public double[] distributionForInstance (Instance instance) throws Exception {
	int svm_type = svm.svm_get_svm_type(model);
	int nr_class = svm.svm_get_nr_class(model);
	int[] labels = new int[nr_class];
	double[] prob_estimates = null;
	if (param.probability == 1) {
		if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
			System.err.println("Do not use distributionForInstance for regression models!");
			return null;
		} else {
			svm.svm_get_labels(model, labels);
			prob_estimates = new double[nr_class];
		}
	}
	if (filter != null) {
		filter.input(instance);
		filter.batchFinished();
		instance = filter.output();
	}
	String line = InstanceToSparse(instance);
	StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
	double target = atof(st.nextToken());
	int m = st.countTokens() / 2;
	svm_node[] x = new svm_node[m];
	for (int j = 0; j < m; j++) {
		x[j] = new svm_node();
		x[j].index = atoi(st.nextToken());
		x[j].value = atof(st.nextToken());
	}
	double v;
	double[] weka_probs = new double[nr_class];
	if (param.probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {           
		v = svm.svm_predict_probability(model, x, prob_estimates);
		for (int k=0; k < prob_estimates.length; k++) {
			if (labels[k] == -1) 
				labels[k] = 0;
				weka_probs[labels[k]] = prob_estimates[k];
		}
	} else {
		v = svm.svm_predict(model, x);
		if (v == -1) 
			v = 0;
			weka_probs[(int)v] = 1;
	}
	return weka_probs;                
}
public void buildClassifier(Instances insts) throws Exception {
	if (normalize == 1) {
		if (getDebug())
			System.err.println("Normalizing...");
			filter = new Normalize();
			filter.setInputFormat(insts);
			insts = Filter.useFilter(insts, filter);
		}
		if (getDebug())
			System.err.println("Converting to libsvm format...");
		Vector sparseData = DataToSparse(insts);
		Vector vy = new Vector();
		Vector vx = new Vector();
		int max_index = 0;
		if (getDebug())
			System.err.println("Tokenizing libsvm data...");
		for (int d = 0; d < sparseData.size(); d++) {
			String line = (String) sparseData.get(d);
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			vy.addElement(st.nextToken());
			int m = st.countTokens() / 2;
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if (m > 0)
				max_index = Math.max(max_index, x[m - 1].index);
			vx.addElement(x);
		}
		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for (int i = 0; i < prob.l; i++)
			prob.x[i] = (svm_node[]) vx.elementAt(i);
		prob.y = new double[prob.l];
		for (int i = 0; i < prob.l; i++)
			prob.y[i] = atof((String) vy.elementAt(i));
		
		if (param.gamma == 0)
			param.gamma = 1.0 / max_index;
		
		error_msg = svm.svm_check_parameter(prob, param);
		if (error_msg != null) {
			System.err.print("Error: " + error_msg + "\n");
			System.exit(1);
		}
		if (getDebug())
			System.err.println("Training model");
		try {
			model = svm.svm_train(prob, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
}
public String toString() {
	return "svm algorithm";
}
}