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
	/*
	 * =============== ROBOT DRAWING ===============
	 *    ______INTAKE______ 
	 *   |                  |
	 *  L|                  |R
	 *  E|      SHOOTER     |I
	 *  F|                  |G
	 *  T|      CLIMBER     |H
	 *   |                  |T
	 *   |______ GEAR ______|
	 */
	
	//=============== Constants ===============
	//Robot ports/CAN IDs
	public static final int RIGHT_FRONT_ID = 1, RIGHT_REAR_ID = 2, LEFT_FRONT_ID = 3, LEFT_REAR_ID = 4; //Drivetrain CAN IDs
	public static final int SHOOTER_ELEVATOR_ID = 5, SHOOTER_FLYWHEEL_ID = 6, INTAKE_ID = 7, CLIMBER_ID = 8; //Subsystem CAN IDs
	public static final int ENCODER_RIGHT_ID = 0, ENCODER_LEFT_ID = 2; //Encoder DIO IDs
	public static final int HELLO_KITTY_ID = 0; //Xbox Controller USB Ports
	public static final int DRIVE_CONTROLLER_PORT = 0, SUBSYSTEM_CONTROLLER_PORT = 1; //Xbox Controller USB Ports
	
	//Calculation Constants
	public static final double ROBOT_WHEEL_DIAMETER_INCHES = 6.0;
	
	//Speed/Angle Constants
	public static final double SHOOTER_ELEVATOR_SPEED = 0.1;
	public static final double HELLO_KITTY_LEFT_ANGLE = 60, HELLO_KITTY_RIGHT_ANGLE = 120;
	
	//=============== Instant Fields ===============
	//Motors
	private CANTalon rightFront, rightRear, leftFront, leftRear; //Drivetrain Motors
	private CANTalon shooterElevator, shooterFlywheel; //Shooter Motors
	private CANTalon intake; //Intake Motor
	private CANTalon climber; //Climber Motor
	private Servo helloKitty; //Fuel Regulator Servo
	
	//Sensors
	private Encoder leftEncoder;
	private Encoder rightEncoder;
	
	//Drivetrain
	private RobotDrive driveTrain;
	
	//Controllers
	private XboxController driveController, subSystemController;
	
	//=============== Robot Status ===============
	private boolean isIntakeIntaking, isIntakeOuttaking; //Intake status
	private boolean isHelloKittyLeft; //Fuel regulator servo on the left
	private boolean isSeekingPeg; //Vision is active
	private boolean isEncoderDebug; //Encoder print statements

	@Override
	public void robotInit() {
		//=============== Robot Status ===============
		isIntakeIntaking = false;
		isIntakeOuttaking = false;
		isHelloKittyLeft = true;
		isSeekingPeg = false;
		isEncoderDebug = false;
		
		//=============== Motors ===============
		//Drivetrain
		rightFront = new CANTalon(RIGHT_FRONT_ID);
		rightFront.set(0);
		rightRear = new CANTalon(RIGHT_REAR_ID);
		rightRear.set(0);
		leftFront = new CANTalon(LEFT_FRONT_ID);
		leftFront.set(0);
		leftRear = new CANTalon(LEFT_REAR_ID);
		leftRear.set(0);
		
		driveTrain = new RobotDrive(leftFront, leftRear, rightFront, rightRear); //Initialize Drivetrain
		
		//Subsystem
		shooterElevator = new CANTalon(SHOOTER_ELEVATOR_ID);
		shooterElevator.set(0);
		shooterFlywheel = new CANTalon(SHOOTER_FLYWHEEL_ID);
		shooterFlywheel.set(0);
		
		intake = new CANTalon(INTAKE_ID);
		intake.set(0);
		
		climber = new CANTalon(CLIMBER_ID);
		climber.set(0);
		
		//=============== Controllers ===============
		driveController = new XboxController(DRIVE_CONTROLLER_PORT);
		subSystemController = new XboxController(SUBSYSTEM_CONTROLLER_PORT);
		
		helloKitty = new Servo(HELLO_KITTY_ID);
		helloKitty.setAngle(HELLO_KITTY_LEFT_ANGLE); //Servo starts left
		
		//Encoder
		rightEncoder = new Encoder(ENCODER_RIGHT_ID, ENCODER_RIGHT_ID+1, false, Encoder.EncodingType.k4X);
		leftEncoder = new Encoder(ENCODER_LEFT_ID, ENCODER_LEFT_ID+1, false, Encoder.EncodingType.k4X);
		rightEncoder.setDistancePerPulse((Math.PI * ROBOT_WHEEL_DIAMETER_INCHES / 2)/512);
		leftEncoder.setDistancePerPulse((Math.PI * ROBOT_WHEEL_DIAMETER_INCHES / 2)/512);
		
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
	            	  this.boundingRectOutput = Vision.filterRectsPeg(inputRects);
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

	public void autonomousInit() {
		
	}

	@Override
	public void autonomousPeriodic() {
		isIntakeIntaking = false;
		isIntakeOuttaking = false;
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
        		if(isIntakeIntaking){
        			isIntakeIntaking = false;
        		}
        		else{
        			isIntakeIntaking = true;
        		}
        		isIntakeOuttaking = false;
        	}
        	if(driveController.isButtonPressed(XboxController.BACK_BUTTON)){
        		if(isIntakeOuttaking){
        			isIntakeOuttaking = false;
        		}
        		else{
        			isIntakeOuttaking = true;
        		}
        		isIntakeIntaking = false;
        	}
        	//Vision & peg seeking
        	if (driveController.isButtonPressed(XboxController.X_BUTTON)) {
        		if(isSeekingPeg){
        			try{
        				visionThread.sleep(40);
        			}
        			catch(InterruptedException e){
        				
        			}
        			isSeekingPeg = false;
        		}
        	}
        	else{
        		isSeekingPeg = true;
        		visionThread.interrupt();
        	}
//        		System.out.println("SeekingPeg: " + seekingPeg);
        	
	
        	if(this.boundingRectOutput.size() == 2 && isSeekingPeg){
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
        	
        	if (isIntakeIntaking) {
        		intake.set(-1);
        	}
        	else if (isIntakeOuttaking) {
        		intake.set(1);
        	}
        	else {
        		intake.set(0);
        	}
        	
        	if (subSystemController.getRawButton(XboxController.RB_BUTTON)) {
        		climber.set(1);
        	}
        	else {
        		climber.set(0);
        	}
        
        	
        	if(subSystemController.isButtonPressed(XboxController.LB_BUTTON)){
        		isHelloKittyLeft = !isHelloKittyLeft;
        		
        	}
        	
        	if(isHelloKittyLeft){
        		helloKitty.setAngle(60.0);
        	}
        	else{
        		helloKitty.setAngle(120.0);
        		
        	}
        	
        	if(driveController.isButtonPressed(XboxController.RB_BUTTON)){
        		isEncoderDebug = !isEncoderDebug;
        	}
        	if(isEncoderDebug){
        		System.out.println("Encoder Debug: " + rightEncoder.getDistance());
        	}
        	
        	Timer.delay(0.05);
        }
	}

	@Override
	public void testPeriodic() {
		
	}
}
