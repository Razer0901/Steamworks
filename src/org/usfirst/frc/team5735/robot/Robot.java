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
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class Robot extends IterativeRobot {
	
	public static final int RIGHT_1_ID = 1, RIGHT_2_ID = 2, LEFT_1_ID = 3, LEFT_2_ID = 4;
	public static final int SHOOTER_ELEVATOR_ID = 6, SHOOTER_FLYWHEEL_ID = 6;
	public static final int PORT_DIO_ENCODER_RIGHT = 0, PORT_DIO_ENCODER_LEFT = 2;
	public static final double ROBOT_WHEEL_DIAMETER_INCHES = 6.0;
	
	private CANTalon right1, right2, left1, left2; 
	private CANTalon shooterElevator;
	private VictorSP shooterFlywheel;
	private Servo helloKittyMotor;

	private VictorSP intakeMotor; 
	private CANTalon climber;
	private boolean intaking;
	private boolean outtaking;
	private boolean helloKittyLeft;
	private boolean helloKittyStarted;
	private boolean seekingPeg;
	private boolean encoderDebug;
	
	private Encoder leftEncoder;
	private Encoder rightEncoder;
	
	private RobotDrive driveTrain;
	
	private XboxController driveController, subSystemController;

	@Override
	public void robotInit() {
		//Drive Train Controllers
		right1 = new CANTalon(2);
		right1.set(0);
		right2 = new CANTalon(7);
		right2.set(0);
		left1 = new CANTalon(3);
		left1.set(0);
		left2 = new CANTalon(6);
		left2.set(0);
		
		//Status Booleans
		intaking = false;
		outtaking = false;
		helloKittyLeft = false;
		helloKittyStarted = false;
		seekingPeg = false;
		encoderDebug = false;
		
		driveTrain = new RobotDrive(left1, left2, right1, right2);
		
		shooterElevator = new CANTalon(1);
		shooterElevator.set(0);
		shooterFlywheel = new VictorSP(2);
		shooterFlywheel.set(0);
		
		intakeMotor = new VictorSP(4);
		intakeMotor.set(0);
		intakeMotor.setInverted(true);
		
		climber = new CANTalon(8);
		climber.set(0);
		
		driveController = new XboxController(0);
		subSystemController = new XboxController(1);
		
		helloKittyMotor = new Servo(3);
		helloKittyMotor.setAngle(0);
		
		//Encoder
			//Right uses 0 & 1, left uses 2 & 3
		rightEncoder = new Encoder(PORT_DIO_ENCODER_RIGHT, PORT_DIO_ENCODER_RIGHT+1, false, Encoder.EncodingType.k4X);
		//leftEncoder = new Encoder(PORT_DIO_ENCODER_LEFT, PORT_DIO_ENCODER_LEFT+1, false, Encoder.EncodingType.k4X);
		rightEncoder.setDistancePerPulse((Math.PI * ROBOT_WHEEL_DIAMETER_INCHES / 2)/360);
		//leftEncoder.setDistancePerPulse((Math.PI * ROBOT_WHEEL_DIAMETER_INCHES / 2)/360);
		
		//Initialize Webcam
	    UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
	    camera.setResolution(320, 240);
	    visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
	          if (!pipeline.findContoursOutput().isEmpty()) {
	              ArrayList<Rect> inputRects = new ArrayList<Rect>();
	              for(MatOfPoint mat: pipeline.findContoursOutput()) {
	            	  inputRects.add(Imgproc.boundingRect(mat));
	              }
	              synchronized (imgLock) {
	            	  //filter code
	            	  this.boundingRectOutput = this.filterRectsPeg(inputRects);
	              }
	          }
	    });
	    
	    
	    
	}
	
	private void moveXFeet(int feet) {
		//double initDistL = leftEncoder.getDistance();
		//double initDistR = rightEncoder.getDistance();
		//driveTrain.arcadeDrive(0.5, 0);
		//while(leftEncoder.getDistance()-initDistL < feet*12 && rightEncoder.getDistance() - initDistR < feet*12);
		//while(rightEncoder.getDistance()-initDistR<feet*12);
		//driveTrain.arcadeDrive(0, 0);
	}
	
	private ArrayList<Rect> boundingRectOutput = new ArrayList<Rect>();
	private static int IMG_WIDTH = 320;
	private static int IMG_HEIGHT = 240;
	private VisionThread visionThread;
	private final Object imgLock = new Object();
	
	private static ArrayList<Rect> filterRectsPeg(ArrayList<Rect> inputRects) {
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

	@Override
	public void autonomousInit() {
		
	}

	@Override
	public void autonomousPeriodic() {
		intaking = false;
		outtaking = false;
	}

	@Override
	
	public void teleopPeriodic() {
        while(isEnabled()&&isOperatorControl()) {
        	//Shooter Flywheel
        	if(subSystemController.getLinearAxis(XboxController.RIGHT_TRIGGER_AXIS)>.1){
        		shooterFlywheel.set(0.65*subSystemController.getLinearAxis(XboxController.RIGHT_TRIGGER_AXIS));
        	}
        	else{
        		shooterFlywheel.set(0.8*subSystemController.getLinearAxis(XboxController.LEFT_TRIGGER_AXIS));
        	}

        	//Shooter Elevator
        	if(subSystemController.getRawButton(XboxController.A_BUTTON)){
        		shooterElevator.set(0.4);
        	}
        	else if (subSystemController.getRawButton(XboxController.B_BUTTON)){
        		shooterElevator.set(-0.4);
        	}
        	else{
        		shooterElevator.set(0);
        	}
        	
        	//Intake
        	if (driveController.isButtonPressed(XboxController.START_BUTTON)) {
        		if(intaking){
        			intaking = false;
        		}
        		else{
        			intaking = true;
        		}
        		outtaking = false;
        	}
        	if(driveController.isButtonPressed(XboxController.BACK_BUTTON)){
        		if(outtaking){
        			outtaking = false;
        		}
        		else{
        			outtaking = true;
        		}
        		intaking = false;
        	}
        	//Vision & peg seeking
        	if (driveController.isButtonPressed(XboxController.X_BUTTON)) {
        		if(seekingPeg){
        			visionThread.stop();
        			seekingPeg = false;
        		}
        	}
        	else{
        		visionThread.start()
        		seekingPeg = true;
        	}
//        		System.out.println("SeekingPeg: " + seekingPeg);
        	
	
        	if(this.boundingRectOutput.size() == 2 && seekingPeg){
        		double centerX = (this.boundingRectOutput.get(0).x + this.boundingRectOutput.get(1).x)/2.0;
        		
        		//String turnDir = "TURN " + ((centerX<this.IMG_WIDTH/2.0) ? "LEFT; " : "RIGHT; ");
        		//System.out.println(turnDir + "CenterX: " + centerX + "; IMG Center: " + this.IMG_WIDTH/2.0 + ".");
        		
        		if(centerX - this.IMG_WIDTH/2.0 > 15){
        			//System.out.println("DIFFERENCE: "+ (centerX - this.IMG_WIDTH/2.0));
        			driveTrain.arcadeDrive(0, 0.5);
        		}
        		else if (centerX - this.IMG_WIDTH/2.0 < -15){
        			//System.out.println("DIFFERENCE: "+ (centerX - this.IMG_WIDTH/2.0));
        			driveTrain.arcadeDrive(0, -0.5);
        		}
        		else{
        			//System.out.println("DIFFENERCE: ZERO");
        			driveTrain.arcadeDrive(0, 0);
        		}
        		
        	} 
        	else {
            	driveTrain.arcadeDrive(driveController.getLinearAxis(XboxController.RIGHT_Y_AXIS)*0.8,-driveController.getLinearAxis(XboxController.LEFT_X_AXIS)*0.8);
        	}
        	
        	
//        	if (driveController.isButtonPressed(XboxController.A_BUTTON)){
//        		moveXFeet(2);
//        	}
        	
        	if (intaking) {
        		intakeMotor.set(-1);
        	}
        	else if (outtaking) {
        		intakeMotor.set(1);
        	}
        	else {
        		intakeMotor.set(0);
        	}
        	
        	if (subSystemController.getRawButton(XboxController.RB_BUTTON)) {
        		climber.set(1);
        	}
        	else {
        		climber.set(0);
        	}
        
        	
        	if(subSystemController.isButtonPressed(XboxController.LB_BUTTON)){
        		helloKittyLeft = !helloKittyLeft;
        		
        	}
        	
        	if(helloKittyLeft){
        		helloKittyMotor.setAngle(60.0);
        	}
        	else{
        		helloKittyMotor.setAngle(120.0);
        		
        	}
        	
        	if(driveController.isButtonPressed(XboxController.RB_BUTTON)){
        		encoderDebug = !encoderDebug;
        	}
        	if(encoderDebug){
        		System.out.println("Encoder Debug: " + rightEncoder.getDistance());
        	}
        	
        	Timer.delay(0.05);
        }
	}

	@Override
	public void testPeriodic() {
		
	}
}
