package com.sensor;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Handler;
import android.os.Message;

public class ReferToWave extends Thread{
	
	private ArrayList<Double> TotalAcceleration;
	private ArrayList<Long> referenceWalkPace;
	private Handler myReferanceHandler;
//	private double threshold = 10;
	
	public ReferToWave(ArrayList<Double> TotalAcceleration, ArrayList<Long> referenceWalkPace, Handler myReferanceHandler) {
		this.TotalAcceleration = TotalAcceleration;
		this.referenceWalkPace = referenceWalkPace;
		this.myReferanceHandler = myReferanceHandler;
	}
	
	public void run() {
		ArrayList<Double> newTotalAcceleration = new ArrayList<Double>();
		ArrayList<Double> newReferanceTotalAcceleration = new ArrayList<Double>();
		Iterator<Double> it = TotalAcceleration.iterator();
		while (it.hasNext()) {
			newTotalAcceleration.add(it.next());
		}
		for (int i = 0; i <= newTotalAcceleration.size()-1; i ++) {
			if (i <= (newTotalAcceleration.size()-1)/2) {
				newReferanceTotalAcceleration.add(newTotalAcceleration.get(i)*2*i/(newTotalAcceleration.size()-1));
			}
			else {
				newReferanceTotalAcceleration.add(2-newTotalAcceleration.get(i)*2*i/(newTotalAcceleration.size()-1));
			}
		}
		double max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		it = newReferanceTotalAcceleration.iterator();
		while (it.hasNext()) {
			double t = it.next();
			if (t >= max) {
				max = t;
			}
			else if (t <= min) {
				min = t;
			}
		}
		double threshold = (max + min) / 2;
		
		for (int i = 1, peak = 0, valley = 0, up = 0, down = 0; i <= newReferanceTotalAcceleration.size()-1; i ++) {
			if (peak == 0) {
				if (up == 0) {
					if (newReferanceTotalAcceleration.get(i) - newReferanceTotalAcceleration.get(i-1) >= 0) {
						up = 1;
					}
				}
				else {
					if (newReferanceTotalAcceleration.get(i) - newReferanceTotalAcceleration.get(i-1) < 0) {
						up = 0;
						peak = 1;
					}
				}
			}
			else {
				if (newReferanceTotalAcceleration.get(i) - newReferanceTotalAcceleration.get(i-1) > 0) {
					down = 0;
					valley = 1;
				}
			}
			if (peak == 1 && valley == 1) {
				if (newReferanceTotalAcceleration.get(i-2) - newReferanceTotalAcceleration.get(i-1) > 0) {
					referenceWalkPace.set(0, (long)(referenceWalkPace.get(0)+1));
					referenceWalkPace.set(1, System.currentTimeMillis());
				}
				peak = 0;
				valley = 0;
			}
		}
		synchronized (TotalAcceleration) {
			for (int i = newTotalAcceleration.size()-1; i >= 0; i --) {
				TotalAcceleration.remove(i);
			}
		}
		Message msg = Message.obtain(myReferanceHandler, Constant.magFromReferToWave);
    	myReferanceHandler.sendMessage(msg);
	}
}
