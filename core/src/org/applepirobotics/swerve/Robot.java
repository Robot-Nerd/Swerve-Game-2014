package org.applepirobotics.swerve;

import org.applepirobotics.swerve.input.ControllerInput;
import org.applepirobotics.swerve.input.KeyboardInput;
import org.applepirobotics.swerve.input.RobotInput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Robot 
{
	public static final int WIDTH = 64, LENGTH = 64;
	public RobotInput input;
	public boolean harvesterDown = false, gyroEnabled = true;
	public float divisor = 1, speedMultiplier = 1;
	public float rotX = 0, rotY = 0;
	public float x, y, angle;
	public float[] angles = {0, 0, 0, 0};
	public float[] speeds = {0, 0, 0, 0};
	public float harvester = 0;
	public Vector2 overallSpeed;
	
	public Robot(float x, float y)
	{
		if(ControllerInput.exists())
			input = new ControllerInput();
		else
			input = new KeyboardInput();
		
		this.x = x;
		this.y = y;
		updateRotationPoint(0, 0);
	}
	
	public void update()
	{		
		float[] targetAngles = new float[4];
		float[] targetSpeeds = new float[4];
		
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		float axes[] = input.getAxes();
		float rotPoint[] = input.checkRotationPoint();
		if(rotPoint[0] != 0 || rotPoint[1] != 0)
		{
			if(rotPoint[0] == 64)
				updateRotationPoint(0, 0);
			else
				updateRotationPoint(rotX + rotPoint[0]*deltaTime*100,
									rotY + rotPoint[1]*deltaTime*100);
		}
		
		boolean inDeadband = true;
		float axisSum = 0;
		for(float axis : axes)
			axisSum += Math.abs(axis);
		if(axisSum > 0.04)
			inDeadband = false;
		
		int harv = input.checkHarvester();
		if(harv == -1)
			harvesterDown = false;
		else if(harv == 1)
			harvesterDown = true;
		
		if(harvesterDown)
		{
			if(harvester < 24)
				harvester += 50 * deltaTime;
		}
		else
		{
			if(harvester > 0)
				harvester -= 50 * deltaTime;
		}
			
		int gyro = input.checkGyroKeys();
		if(gyro == -1)
			gyroEnabled = false;
		else if(gyro == 1)
			gyroEnabled = true;
		
		Vector2 strafe = new Vector2(axes[0], axes[1]);
		if(gyroEnabled)
			strafe.rotate(angle);
			
		Vector2 wheels[] = new Vector2[4];
		wheels[0] = getRotationVector(1, 1, axes[2]).add(strafe);
		wheels[1] = getRotationVector(-1, 1, axes[2]).add(strafe);
		wheels[2] = getRotationVector(1, -1, axes[2]).add(strafe);
		wheels[3] = getRotationVector(-1, -1, axes[2]).add(strafe);
		
		float maxSpeed = 1;
		
		for(int w = 0; w < 4; w++)
		{
			targetAngles[w] = (float) Math.toDegrees(Math.atan2(wheels[w].x, wheels[w].y));
			targetSpeeds[w] = wheels[w].len();
			
			if(targetSpeeds[w] > maxSpeed)
				maxSpeed = targetSpeeds[w];
		}
		
		for(int w = 0; w < 4; w++)
		{
			targetSpeeds[w] /= maxSpeed / speedMultiplier;

			float error = getAngleDistance(angles[w], targetAngles[w]);
			if(Math.abs(error) > 90)
			{
				error = -Math.signum(error) * 180 + error;
				targetSpeeds[w] *= -1;
			}
			
			if(!inDeadband)
				angles[w] += pControl(0, error, 25, 600) * deltaTime;
			if(inDeadband || targetSpeeds[w] == 0)
				speeds[w] += pControl(speeds[w], 0, 5, 5) * deltaTime;
			else
				speeds[w] += pControl(speeds[w], targetSpeeds[w], 30, 12) * deltaTime; 
		
			angles[w] %= 360;
		}
		
		final int ideal[] = {-45, -135, 45, 135};
		float angularSpeed = 0;
		for(int a = 0; a < 4; a++)
			angularSpeed += (1 - Math.abs(getAngleDistance(angles[a], ideal[a]))/90)*speeds[a];
		angularSpeed /= 4;
		
		angle += angularSpeed * deltaTime * 540;
		
		overallSpeed = new Vector2(0, 0);	
		
		for(int w = 0; w < 4; w++)
			overallSpeed.add(new Vector2((float) Math.sin(Math.toRadians(angles[w])) * speeds [w],
									(float) Math.cos(Math.toRadians(angles[w])) * speeds [w]));
		
		overallSpeed = new Vector2(overallSpeed.x / 4, overallSpeed.y / 4).rotate(-angle);

		x += overallSpeed.x * deltaTime * 340;
		y += overallSpeed.y * deltaTime * 340;
	}
	
	public void updateRotationPoint(float x, float y)
	{
		rotX = x;
		rotY = y;
		
		divisor = (float) Math.sqrt(
				Math.pow(Math.abs(rotX) + WIDTH/2, 2) +
				Math.pow(Math.abs(rotY) + LENGTH/2, 2));
	}
	
	public Vector2 getRotationVector(int offsetX, int offsetY, float z)
	{
		float x = rotX + offsetX*WIDTH/2;
		float y = rotY + offsetY*LENGTH/2;
		
		return new Vector2(z * x / divisor, z * y / divisor).rotate90(1);
	}
	
	public float getAngleDistance(float current, float target)
	{
		float dist = target - current;
		if(dist > 180)
			dist -= 360;
		else if(dist < -180)
			dist += 360;
		return dist;
	}
	
	public float pControl(float process, float setpoint, float gain, float max)
	{
		float error = setpoint - process;
		return Math.min(Math.abs(error)*gain, max) * Math.signum(error);
	}
	
	public void surroundingsInteract()
	{
		for(Robot opponent : SwerveDrive.robots)
		{
			if(opponent != null && opponent != this)
			{
				if(Math.abs(x - opponent.x) < 64 && Math.abs(y - opponent.y) < 64)
				{
					float diffx = x - opponent.x;
					float diffy = y - opponent.y;
					
					if(Math.signum(overallSpeed.x) == Math.signum(opponent.overallSpeed.x))
					{
						if(Math.max(Math.abs(overallSpeed.x), Math.abs(opponent.overallSpeed.x)) != 
								Math.abs(overallSpeed.x))
						{
							if(Math.abs(diffx) > Math.abs(diffy))
							{
								x += Math.signum(diffx) * WIDTH - diffx;
							}
						}
					}
					else
						x += opponent.overallSpeed.x * Gdx.graphics.getDeltaTime() * 340;
					
					if(opponent.x <= 147 || opponent.x >= 653)
						x += Math.signum(diffx) * WIDTH - diffx;
					
					if(Math.signum(overallSpeed.y) == Math.signum(opponent.overallSpeed.y))
					{
						if(Math.max(Math.abs(overallSpeed.y), Math.abs(opponent.overallSpeed.y)) != 
								Math.abs(overallSpeed.y))
						{
							if(Math.abs(diffy) > Math.abs(diffx))
							{
								float diff = y - opponent.y;
								y += Math.signum(diff) * LENGTH - diff;
							}
						}
					}
					else
						y += opponent.overallSpeed.y * Gdx.graphics.getDeltaTime() * 340;
					
					if(opponent.y <= -838 || opponent.y >= 365)
						x += Math.signum(diffx) * WIDTH - diffx;
				}
			}
					
			if(y > 365)
				y = 365;
			else if(y < -838)
				y = -838;
			if(x < 147)
				x = 147;
			else if (x > 653)
				x = 653;
		}
	}
}
