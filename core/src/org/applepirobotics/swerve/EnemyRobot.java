package org.applepirobotics.swerve;

import org.applepirobotics.swerve.input.AIEnemyInput;

import com.badlogic.gdx.math.Vector2;

public class EnemyRobot extends Robot
{	
	public EnemyRobot(float x, float y, int zone, float speed, Robot target)
	{
		super(x, y);
		input = new AIEnemyInput(this, zone, target);
		speedMultiplier = speed;
	}
	
	public Vector2 getRotationVector(int offsetX, int offsetY, float z)
	{
		Vector2 vector = new Vector2(0, 0);
		
		if(offsetX > 0)
			vector.y = z;
		else
			vector.y = -z;
			
		return vector;
	}
}
