package com.sensor;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Handler;
import android.os.Message;

public class CalSteps extends Thread {
	
	private ArrayList<Double> TotalAcceleration;
	private ArrayList<Double> DebugData;
	private Handler myHandler;
	private ArrayList<Double> Result;
	int times = 7;
	ArrayList<Long> WalkPace;
	
	public CalSteps(ArrayList<Double> TotalAcceleration, ArrayList<Long> WalkPace, ArrayList<Double> DebugData, Handler myHandler) {
		super();
		this.TotalAcceleration = TotalAcceleration;
		this.myHandler = myHandler;
		this.WalkPace = WalkPace;
		this.DebugData = DebugData;
	}
	
	public void run () {
		ArrayList<Double> newTotalAcceleration = new ArrayList<Double>();
		Iterator<Double> it = TotalAcceleration.iterator();
		while (it.hasNext()) {
			newTotalAcceleration.add(it.next());
		}
		Result = AutocorrelationAnalysis(newTotalAcceleration, times);
    	double B_STD = Result.get(0);
    	double Relative_AB_max = Result.get(1);
//    	DebugData.add(0, B_STD);
//    	DebugData.add(1, Relative_AB_max);
    	int B_length = (int)Math.floor(Result.get(2));
    	if (B_STD > 0.5 && Relative_AB_max > 0.7) {
    		WalkPace.set(0, (long)(WalkPace.get(0)+1));
    		WalkPace.set(1,System.currentTimeMillis());
    	}
    	synchronized (TotalAcceleration) {
	    	for (int i = B_length-1; i >=0; i --) {
	    		TotalAcceleration.remove(i);
	    	}
    	}
    	Message msg = Message.obtain(myHandler, Constant.msgFromCalStemps);
    	myHandler.sendMessage(msg);
	}
	
	public ArrayList<Double> AutocorrelationAnalysis(ArrayList<Double> TotalAcceleration, int times) {
    	double B_STD = 0;
    	double Relative_AB_max = Integer.MIN_VALUE;
    	double B_length = 0;
    	for (int i = 1; i <= times; i ++) {
    		ArrayList<Double> A = new ArrayList<Double>();
    		ArrayList<Double> B = new ArrayList<Double>();
    		for (int j = 0; j <= i+4; j ++) {
    			A.add(TotalAcceleration.get(j));
    			B.add(TotalAcceleration.get(j+times));
    		}
    		double Relative_AB = MathTools.calRelativeCoefficient(A, B, i+5);
    		if (Relative_AB >= Relative_AB_max) {
    			Relative_AB_max = Relative_AB;
    			B_STD = MathTools.calSTD(B);
    			B_length = (double)B.size();
    		}
    	}
    	ArrayList<Double> result = new ArrayList<Double>();
    	result.add(B_STD);
    	result.add(Relative_AB_max);
    	result.add(B_length);
    	DebugData.add(B_STD);
    	DebugData.add(Relative_AB_max);
    	return result;
    }
}
