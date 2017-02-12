package org.usfirst.frc.team5735.robot;

import edu.wpi.first.wpilibj.Joystick;

public class XboxController extends Joystick {
	
	public static final int A_BUTTON = 1;
	public static final int B_BUTTON = 2;
	public static final int X_BUTTON = 3;
	public static final int Y_BUTTON = 4;
	public static final int LB_BUTTON = 5;
	public static final int RB_BUTTON = 6;
	public static final int BACK_BUTTON = 7;
	public static final int START_BUTTON = 8;
	public static final int LEFT_STICK_BUTTON = 9;
	public static final int RIGHT_STICK_BUTTON = 10;
	
	public static final int LEFT_X_AXIS = 0;
	public static final int LEFT_Y_AXIS = 1;
	public static final int LEFT_TRIGGER_AXIS = 2;
	public static final int RIGHT_TRIGGER_AXIS = 3;
	public static final int RIGHT_X_AXIS = 4;
	public static final int RIGHT_Y_AXIS = 5;

	private boolean[] isButtonHeld = new boolean[10];
	
	public XboxController(int port) {
		super(port);
	}
	
	public boolean isButtonHeld(int button){
		return getRawButton(button);
	}
	
	public boolean isButtonPressed(int button){
		if (getRawButton(button) == true){
			if(isButtonHeld[button-1] == false){
				isButtonHeld[button-1] = true;
				return true;
			}
		}
		if (isButtonReleased(button)) {
			return false;
		}
		return false;
	}
	
	public boolean isButtonReleased(int button){
		if (getRawButton(button) == false){
			if(isButtonHeld[button-1] == true){
				isButtonHeld[button-1] = false;
				return true;
			}
		}
		return false;
	}
	
	public double getLinearAxis(int axis){
		if (Math.abs(getRawAxis(axis)) < .05) {
			return 0;
		}
		if (axis == 1||axis==5){
			return -getRawAxis(axis);
		}
		return getRawAxis(axis);
	}
	
	public double getCubicAxis(int axis){
		if (axis == 1||axis==5){
			return Math.pow(-getRawAxis(axis),3);
		}
		return Math.pow(getRawAxis(axis),3);
	}

}