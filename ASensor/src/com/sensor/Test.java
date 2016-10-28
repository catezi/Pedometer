package com.sensor;

import java.util.ArrayList;

public class Test {
	public static void main(String[] args) {
		ArrayList<Double> arrayList = new ArrayList<Double>();
		for (int i = 0; i <= 23; i ++) {
			arrayList.add((double)10*Math.random());
		}
		System.out.println(MathTools.calAVG(arrayList));
		System.out.println(MathTools.calSTD(arrayList));
	}
}
