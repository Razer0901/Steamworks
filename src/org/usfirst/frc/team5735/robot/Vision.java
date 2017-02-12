package org.usfirst.frc.team5735.robot;

import java.util.ArrayList;

import org.opencv.core.Rect;

public class Vision {
	
	public static ArrayList<Rect> filterRectsPeg(ArrayList<Rect> inputRects) {
		ArrayList<Rect> output = new ArrayList<Rect>();
		Rect curBiggest1 = null;
		Rect curBiggest2 = null;
		double biggestArea = 0;
		for (int i = 0; i < inputRects.size(); i++) {
			for (int j = 0; j < inputRects.size(); j++) {
				if (j == i) {
					continue;
				}

				Rect rA = inputRects.get(i);
				Rect rB = inputRects.get(j);

				if (rA.area() < 300 || rB.area() < 300) {
				}
				if((double)(rA.height) /(double)(rA.width) <1){
					continue;
				}
				if((double)(rB.height) /(double)(rB.width) <1){
					continue;
				}
				if ((Math.abs(rA.area() - rB.area()) <= .2 * rB.area()) && Math.abs(rA.y - rB.y) <= 20) {
					if(rA.area() + rB.area() > biggestArea){
						curBiggest1 = rA;
						curBiggest2 = rB;
						biggestArea = rA.area() + rB.area();
					}
				}
			}
		}
		output.clear();
		if(curBiggest1 == null && curBiggest2 == null){
			return output;
		}
		output.add(curBiggest1);
		output.add(curBiggest2);
		return output;

	}
}
