package org.applepirobotics.swerve.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class KeyboardInput implements RobotInput
{
	
	public KeyboardInput()
	{
	}
	
	public int checkHarvester()
	{
		if(Gdx.input.isKeyPressed(Keys.UP))
			return -1;
		if(Gdx.input.isKeyPressed(Keys.DOWN))
			return 1;
		return 0;
	}
	
	public int checkGyroKeys()
	{
		if(Gdx.input.isKeyPressed(Keys.Q))
			return -1;
		if(Gdx.input.isKeyPressed(Keys.E))
			return 1;
		return 0;
	}
	
	public float[] checkRotationPoint()
	{
		float rot[] = {0, 0};

		if(Gdx.input.isKeyPressed(Keys.J))
			rot[0] = -1;
		else if(Gdx.input.isKeyPressed(Keys.L))
			rot[0] = 1;
		if(Gdx.input.isKeyPressed(Keys.I))
			rot[1] = 1;
		else if(Gdx.input.isKeyPressed(Keys.K))
			rot[1] = -1;
		if(Gdx.input.isKeyPressed(Keys.U))
			rot[0] = 64;

		return rot;
	}
	
	public float[] getAxes()
	{			
		float axes[] = new float[3];
		
		if(Gdx.input.isKeyPressed(Keys.A))
			axes[0] = -1;
		else if(Gdx.input.isKeyPressed(Keys.D))
			axes[0] = 1;
		if(Gdx.input.isKeyPressed(Keys.W))
			axes[1] = 1;
		else if(Gdx.input.isKeyPressed(Keys.S))
			axes[1] = -1;
		if(Gdx.input.isKeyPressed(Keys.LEFT))
			axes[2] = -1;
		else if(Gdx.input.isKeyPressed(Keys.RIGHT))
			axes[2] = 1;
	
		return axes;
	}

	public boolean checkShooting()
	{
		return Gdx.input.isKeyPressed(Keys.SPACE);
	}

	public boolean checkMenu()
	{
		return Gdx.input.isKeyPressed(Keys.ESCAPE);
	}

	public boolean checkPassing() 
	{
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
	}
	
	public boolean checkConfirm()
	{
		return Gdx.input.isKeyPressed(Keys.ENTER);
	}
}
