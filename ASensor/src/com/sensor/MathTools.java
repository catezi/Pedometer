package com.sensor;

import java.util.ArrayList;
import java.util.Iterator;

public class MathTools {
	public static double calTotalAcceleration(double num1, double num2, double num3) {
		return Math.sqrt(Math.pow(num1,2)+Math.pow(num2,2)+Math.pow(num3,2));
	}
	public static double calAVG(ArrayList<Double> TotalAcceleration) {
		double sum = 0;
		Iterator<Double> it = TotalAcceleration.iterator();
		while (it.hasNext()) {
			double i = it.next();
			sum = sum + i;
		}
		return sum/TotalAcceleration.size();
	}
	public static double calSTD(ArrayList<Double> TotalAcceleration) {
		double AVG = calAVG(TotalAcceleration);
		double Variance = 0;
		Iterator<Double> it = TotalAcceleration.iterator();
		while (it.hasNext()) {
			double i = it.next();
			Variance = Variance + Math.pow((i-AVG), 2);
		}
		return Math.sqrt(Variance/TotalAcceleration.size());
	}
	public static double calRelativeCoefficient(ArrayList<Double> A, ArrayList<Double> B, int t) {
		double A_AVG = calAVG(A);
		double B_AVG = calAVG(B);
		double A_STD = calSTD(A);
		double B_STD = calSTD(B);
		double sum = 0;
		Iterator<Double> A_it = A.iterator();
		Iterator<Double> B_it = B.iterator();
		while (A_it.hasNext()) {
			double x = A_it.next();
			double y = B_it.next();
			sum = sum + (x-A_AVG)*(y-B_AVG);
		}
		return sum/(t*A_STD*B_STD);
	}
}
