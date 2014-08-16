package org.applepirobotics.swerve.input;

public interface RobotInput 
{
	public int checkHarvester();
	public int checkGyroKeys();
	public float[] checkRotationPoint();
	public float[] getAxes();
	public boolean checkPassing();
	public boolean checkShooting();
	public boolean checkMenu();
	public boolean checkConfirm();
}