package org.applepirobotics.swerve.input;

import org.applepirobotics.swerve.Robot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class AIEnemyInput implements RobotInput
{
	private Robot self, opponent;
	private int zone;
	private float pinningCounter = 1.5f;
	
	public AIEnemyInput(Robot self, int zone, Robot opponent)
	{
		this.self = self;
		this.opponent = opponent;
		this.zone = zone;
	}
	
	public float[] getAxes()
	{		
		if(Math.abs(self.x - opponent.x) < 70 && Math.abs(self.y - opponent.y) < 70)
		{
			if(opponent.y > 362 || opponent.y < -835 || opponent.x < 150 || opponent.x > 650)
				pinningCounter -= Gdx.graphics.getDeltaTime();
			else if(pinningCounter > 0)
				pinningCounter = 1.5f;
		}	
			
		float axes[] = new float[3];
		int dir = 1;
		axes[0] = 0;
				
		Vector2 target = new Vector2(opponent.x - self.x, 0);
		
		if(zone == -1)
			target.y = Math.min(opponent.y, -20);
		else if(zone == 1)
			target.y = Math.max(opponent.y, -455);
		else if(zone == 9)
			target.y = Math.max(opponent.y, -236);
		else if(zone == 10)
			target.y = Math.min(Math.max(opponent.y, -455), -20);
		else if(zone == 11)
			target.y = Math.min(opponent.y, -236);
		else
			target.y = opponent.y;
		
		if(target.y == -20 || target.y == -236 || target.y == -455)
			if(target.y - self.y > 40)
				target.x = 800 - opponent.x - self.x;
				
			
		target.y -= self.y;
		
		float targetAngle = (float) Math.toDegrees(Math.atan2(target.x, target.y));
		
		float error = self.getAngleDistance(self.angle % 360, targetAngle);
		if(Math.abs(error) > 90)
		{
			error = -Math.signum(error) * 180 + error;
			dir = -1;
		}
		
		if(pinningCounter < 0)
		{
			dir *= -1;
			pinningCounter -= Gdx.graphics.getDeltaTime();

			if(pinningCounter < -1)
				pinningCounter = 1.5f;
		}
		
		axes[2] = self.pControl(0, error, 0.1f, 1);
		
		axes[1] = self.pControl(0, target.len(), 0.05f, (1 - Math.abs(error/45)) * dir);
	
		return axes;
	}
	
	public int checkGyroKeys()
	{
		return -1;
	}

	public int checkHarvester() 
	{
		return -1;
	}

	public float[] checkRotationPoint() 
	{
		return new float[2];
	}

	public boolean checkShooting() 
	{
		return false;
	}
	
	public boolean checkMenu()
	{
		return false;
	}
	
	public boolean checkPassing()
	{
		return false;
	}
	
	public boolean checkConfirm()
	{
		return false;
	}
}