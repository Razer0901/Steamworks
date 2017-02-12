package org.usfirst.frc.team5735.robot;

import java.util.ArrayList;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import com.ctre.CANTalon;

import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class Autonomous {

	private Encoder LEncoder, REncoder;
	private AHRS gyro;
	private double x, y;
	
	public Autonomous(Encoder left, Encoder right, AHRS ahrs){
		LEncoder = left;
		REncoder = right;
		gyro = ahrs;
		x = 0;
		y = 0;
	}
	/**
	 * Reset the values of the Encoders, Gyro, and current position
	 */
	public void init(){
		LEncoder.reset();
		REncoder.reset();
		gyro.reset();
		
		x = 0;
		y = 0;
	}
	
	public void moveTo(double xpos, double ypos){
		double dx = xpos - x;
		double dy = ypos - y;
		double radAng = Math.atan2(dy,  dx);
		radAng = (radAng<0) ? radAng + 2*Math.PI : radAng;
		double degAbs = Math.toDegrees(radAng);
		
	}
	
	
	
}
