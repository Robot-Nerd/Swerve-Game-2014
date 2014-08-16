package org.applepirobotics.swerve;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Ball 
{
	public Robot robot;
	public float x, y, z;
	private Vector3 velocity;
	private boolean readyToFire = false;
	public boolean scored = false;
	public boolean trussed = false;
	public boolean outOfBounds = false;
	public float waitTimer = 0;
	
	public Ball(float x, float y)
	{
		this.x = x;
		this.y = y;
		this.z = 3;
		velocity = new Vector3((float) Math.random(), (float) Math.random(), 0);
		robot = SwerveDrive.robots.get(0);
	}
	
	public void update()
	{	
		if(readyToFire)
		{
			if(robot.input.checkShooting() && robot.harvester > 20)
			{
				velocity.x = (float) Math.sin(Math.toRadians(robot.angle)) * 2 + robot.overallSpeed.x;
				velocity.y = (float) Math.cos(Math.toRadians(robot.angle)) * 2 + robot.overallSpeed.y;
				velocity.z = 28;
				readyToFire = false;
			}
			else if(robot.input.checkPassing() && robot.harvester < 2)
			{
				double angle = Math.toRadians(robot.angle);
				
				x += -32 * Math.sin(angle);
				y += -32 * Math.cos(angle);
				
				velocity.x = (float) -Math.sin(angle) * 0.6f + robot.overallSpeed.x;
				velocity.y = (float) -Math.cos(angle) * 0.6f + robot.overallSpeed.y;
				velocity.z = 5;
				readyToFire = false;
			}
			else
			{
				x = robot.x;
				y = robot.y;
			}
		}
		else if(z == 0)
		{
			Vector2 distance = new Vector2(robot.x - x, robot.y -y);
			
			if(Math.abs(distance.x) < 56 && Math.abs(distance.y) < 80)
			{
				if(((Math.abs(robot.getAngleDistance((float) Math.toDegrees(Math.atan2(distance.x, distance.y)), 
													robot.angle % 360)) < 30) && robot.harvesterDown) ||
													(Math.abs(distance.x) < 16 && Math.abs(distance.y) < 16)) 
				{
					velocity.x = 0;
					velocity.y = 0;
					readyToFire = true;
				}
				else if(distance.y < 56 && !readyToFire)
				{	
					velocity.x = robot.overallSpeed.x + Math.signum(robot.overallSpeed.x) 
							* (float) Math.pow(robot.overallSpeed.x, 2);
					velocity.y = robot.overallSpeed.y + Math.signum(robot.overallSpeed.y) 
							* (float) Math.pow(robot.overallSpeed.y, 2);
				}
			}
		}
		
		if(z == 0)
		{
			velocity.x += robot.pControl(velocity.x, 0, 2, 2) * Gdx.graphics.getDeltaTime();
			velocity.y += robot.pControl(velocity.y, 0, 2, 2) * Gdx.graphics.getDeltaTime();
		}
		
		if(!outOfBounds)
		{
			if(x < 132)
			{
				if(z < 2.5)
					velocity.x = Math.abs(velocity.x);
				else
					outOfBounds = true;			
			}
			else if(x > 666)
			{
				if(z < 2.5 && !outOfBounds)
					velocity.x = -Math.abs(velocity.x);
				else
					outOfBounds = true;
			}
			
			if(outOfBounds)
			{
				if(z > 1 && z < 6.5 && y < 150 && y > -195)
					waitTimer = 0;
				else
					waitTimer = 1.5f;
			}
		}
		else
		{
			waitTimer -= Gdx.graphics.getDeltaTime();
			if(waitTimer < 0 && waitTimer > -0.5)
			{
				velocity.x = 0;
				velocity.y = 0;
				if(x < 400)
					x = 31;
				else
					x = 769;
				if(y > -403)
					y = -24;
				else
					y = -783;
			}
			
			Vector2 distance = new Vector2(robot.x - x, robot.y -y);
			
			if(waitTimer < -0.5 && Math.abs(distance.x) < 300 && Math.abs(distance.y) < 250 && velocity.x == 0)
			{
				velocity.x = distance.x/270;
				velocity.y = distance.y/270;
				velocity.z = distance.len()/25; 
				
				waitTimer = 2;
			}
				
			if(x > 132 && x < 666)
				outOfBounds = false;
		}

		if(y < -856)
			velocity.y = Math.abs(velocity.y);
		else if(y > 380)
		{
			if(!outOfBounds && z > 5.2 && z < 6.7 && Math.random() < 0.75 && velocity.y > 0 && (x < 385 || x > 415))
				scored = true;
			else if(!scored)
			{
				if(z > 0)
					velocity.y = Math.max(0.25f, velocity.y/2);	
				velocity.y = -Math.abs(velocity.y);
			}
		}
		
		if(z > 3 && z < 5 && y > -272 && y < -199)
		{
			if(robot.y + 236 < 0)
				velocity.y = -Math.abs(velocity.y);
			else
				velocity.y = Math.abs(velocity.y);
		}
		else if(z > 5 && y > -199 && y < -180 && robot.y < -272)
			trussed = true;
		
		if(z > 0)
			velocity.z -= Gdx.graphics.getDeltaTime() * 59;
		
		x += velocity.x * Gdx.graphics.getDeltaTime() * 340;
		y += velocity.y * Gdx.graphics.getDeltaTime() * 340;
		z += velocity.z * Gdx.graphics.getDeltaTime();
		
		if(z < 0)
			z = 0;
	}
}
