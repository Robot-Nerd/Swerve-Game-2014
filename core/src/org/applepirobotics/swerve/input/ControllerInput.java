package org.applepirobotics.swerve.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

public class ControllerInput implements RobotInput
{
	Controller controller;
	
	public ControllerInput()
	{
		controller = Controllers.getControllers().get(0);
	}
	
	public static boolean exists()
	{
		return Controllers.getControllers().size > 0;
	}
	
	public int checkHarvester()
	{
		if(controller.getButton(4))
			return -1;
		if(controller.getButton(6))
			return 1;
		return 0;
	}
	
	public int checkGyroKeys()
	{
		if(controller.getButton(3))
			return -1;
		if(controller.getButton(2))
			return 1;
		return 0;
	}
	
	public float[] checkRotationPoint()
	{
		float rot[] = {0, 0};
		
		String direction = controller.getPov(0).toString().toLowerCase();
		if(direction.contains("west"))
			rot[0] = -1;
		else if(direction.contains("east"))
			rot[0] = 1;
		if(direction.contains("north"))
			rot[1] = 1;
		else if(direction.contains("south"))
			rot[1] = -1;
		if(controller.getButton(8))
			rot[0] = 64;
			
		return rot;
	}
	
	public float[] getAxes()
	{			
		float axes[] = new float[3];

		axes[0] = controller.getAxis(3);
		axes[1] = -controller.getAxis(2);
		axes[2] = controller.getAxis(1);
		
		for(int a = 0; a < 3; a++)
			axes[a] = (float) (Math.signum(axes[a]) * Math.pow(axes[a], 2));
		
		return axes;
	}

	public boolean checkShooting()
	{
		return controller.getButton(7);
	}
	
	public boolean checkMenu()
	{
		return controller.getButton(9);
	}

	public boolean checkPassing() 
	{
		return controller.getButton(5);
	}
	
	public boolean checkConfirm()
	{
		return controller.getButton(1);
	}
}
