package org.applepirobotics.swerve;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

public class SwerveDrive extends ApplicationAdapter
{
	public static Array<Robot> robots;
	
	Texture menu, arrow, field, truss, blueGoal;
	BitmapFont text;
	SpriteBatch sprites;
	Ball ball;
	Array<ShapeRenderer> robotShapes;
	ShapeRenderer ballShapes;
	OrthographicCamera camera;
	int score = 0;
	float time = 60, enemySpeed = 0.8f;
	boolean atMenu = false, useTimer = true;
	int menuSelection = 0, previous = 0;
	int[] enemyStrategy;
	
	public void create()
	{	
		parseDifficulty("e1");
		resetField();
		text = new BitmapFont();
		text.setColor(Color.RED);
		menu = new Texture(Gdx.files.internal("menu.png"));
		arrow = new Texture(Gdx.files.internal("arrow.png"));
		field = new Texture(Gdx.files.internal("field.png"));
		truss = new Texture(Gdx.files.internal("truss.png"));
		blueGoal = new Texture(Gdx.files.internal("bluegoal.png"));
		sprites = new SpriteBatch();
		ballShapes = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 600);
	}

	public void render() 
	{
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		for(int a = 0; a < robots.size; a++)
			robots.get(a).update();
		
		for(int a = 0; a < robots.size; a++)
			robots.get(a).surroundingsInteract();
		
		ball.update();		
		
		if(useTimer)
			time -= Gdx.graphics.getDeltaTime();
		else
			time = 999;
		
		if(robots.get(0).y > 180)
			camera.position.y = 180;
		else if(robots.get(0).y < -653)
			camera.position.y = -653;
		else
			camera.position.y = robots.get(0).y;
		
		if(ball.scored && ball.y > 515)
		{
			score += 10 + (ball.trussed ? 10 : 0);
			ball = new Ball(-100, -850);
		}
		
		sprites.setProjectionMatrix(camera.combined);
		ballShapes.setProjectionMatrix(camera.combined);
		
		for(ShapeRenderer r : robotShapes)
			r.setProjectionMatrix(camera.combined);
		
		sprites.begin();
		sprites.draw(field, 0, -953);
		sprites.end();
		

		for(int a = 0; a < robots.size; a++)
		{	
			ShapeRenderer s = robotShapes.get(a);
			Robot r = robots.get(a);
			
			s.begin(ShapeType.Filled);
			s.identity();
			s.translate(r.x, r.y, 0);
			s.rotate(0, 0, 1, -r.angle);
			s.setColor(Color.GRAY);
			s.rect(-32, -32, 64, 64);
			s.setColor(a == 0 ? Color.BLUE : Color.RED);
			s.rect(-37, -44.5f, 10, 25, 5, 12.5f, -r.angles[0]);
			s.rect(27, -44.5f, 10, 25, 5, 12.5f, -r.angles[1]);
			s.rect(-37, 19.5f, 10, 25, 5, 12.5f, -r.angles[2]);
			s.rect(27, 19.5f, 10, 25, 5, 12.5f, -r.angles[3]);
			s.end();
		}
		
		drawBall();
		
		robotShapes.get(0).begin(ShapeType.Filled);
		if(robots.get(0).gyroEnabled)
			robotShapes.get(0).setColor(Color.GREEN);
		else
			robotShapes.get(0).setColor(Color.RED);
		robotShapes.get(0).circle(robots.get(0).rotX, robots.get(0).rotY, 3);
		robotShapes.get(0).end();
		
		robotShapes.get(0).begin(ShapeType.Line);
		robotShapes.get(0).setColor(Color.BLACK);
		robotShapes.get(0).rect(-27, -32 - robots.get(0).harvester * 1.2f, 54, 12 + robots.get(0).harvester);
		robotShapes.get(0).end();
		
		sprites.begin();
		sprites.draw(blueGoal, 111, 411);
		sprites.draw(truss, 61, -248);
		text.drawMultiLine(sprites, "Score:\n" + (score + (ball.trussed ? 10 : 0)), 55, 470);
		text.drawMultiLine(sprites, "Time:\n" + (int)time, 705, 470);
		sprites.end();
		
		if(ball.z > 5 && ball.y < -200)
			drawBall();
		
		if(time < 0)
		{
			Gdx.gl.glClearColor(1, 1, 1, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			camera.position.y = 180;
			score += ball.trussed ? 10 : 0;
			ball.trussed = false;
			sprites.begin();
			text.drawMultiLine(sprites, "Score:\n" + score + "\nPush enter to play again", 390, 240);
			sprites.end();
			if(robots.get(0).input.checkConfirm())
			{
				resetField();
			}
		}
		
		if(robots.get(0).input.checkMenu())
		{
			menuSelection = 0;
			atMenu = true;
		}
			
		if(atMenu)
		{
			camera.position.y = 180;
			float axis = robots.get(0).input.getAxes()[1];
			int direction = Math.abs(axis) > 0.25 ? (int) Math.signum(axis) : 0;
			if(direction != previous)
				menuSelection -= direction;
			previous = direction;
			menuSelection = Math.min(Math.max(menuSelection, 0), 2);
			sprites.begin();
			sprites.draw(menu, 0, -120);
			sprites.draw(arrow, 560, 257 - 71 * menuSelection);
			sprites.end();
			
			if(robots.get(0).input.checkConfirm())
			{
				switch(menuSelection)
				{
					case 0:
						resetField();
						atMenu = false;
						break;
					case 1:
						Gdx.input.getTextInput(new TextInputListener()
						{
							public void input(String text) 
							{
								parseDifficulty("s" + text);
								resetField();
							}
							public void canceled() 
							{
								parseDifficulty("s0.8");
								resetField();
							}
						}, "Enter enemy speed from 1 - 3", "");
						Gdx.input.getTextInput(new TextInputListener()
						{
							public void input(String text) 
							{
								parseDifficulty("e" + text);	
							}
							public void canceled() 
							{
								parseDifficulty("e1");
							}
						}, "Number of enemies", "");
						atMenu = false;
						break;
					case 2:
						useTimer = !useTimer;
						atMenu = false;
				}
			}
		}
	
	}
	
	public void parseDifficulty(String str)
	{
		char param = str.charAt(0);
		str = str.substring(1);
		
		if(param == 'e')
		{
			int enemies = 1;
			try{
				enemies = Integer.parseInt(str);
			} catch(NumberFormatException e) {}
	
			switch(enemies)
			{
				case 0:
					enemyStrategy = null;
					break;
				case 1:
					enemyStrategy = new int[1];
					break;
				case 2:
					enemyStrategy = new int[2];
					enemyStrategy[0] = -1;
					enemyStrategy[1] = 1;
					break;
				case 3:
					enemyStrategy = new int[3];
					enemyStrategy[0] = 9;
					enemyStrategy[1] = 1;
					enemyStrategy[2] = 11;
					break;
				default:
					try{
						enemyStrategy = new int[enemies];
					} catch(Exception e) {
						enemyStrategy = new int[1];
					}
			}
		}
		else
		{
			int speed = 1;
			
			try{
				speed = Integer.parseInt(str);
			} catch(NumberFormatException e) {}
			
			enemySpeed = 0.6f + 0.2f * speed;
		}
	}
	
	public void drawBall()
	{
		ballShapes.begin(ShapeType.Filled);
		ballShapes.setColor(Color.BLUE);
		ballShapes.circle(ball.x, ball.y, 24 + ball.z * 1.5f);
		ballShapes.end();
	}
	
	public void resetField()
	{
		robots = new Array<Robot>();
		robotShapes = new Array<ShapeRenderer>();
		
		robots.add(new Robot(400, 240));
		robotShapes.add(new ShapeRenderer());
		
		if(enemyStrategy != null)
			for(int r = 0; r < enemyStrategy.length; r++)
			{
				robots.add(new EnemyRobot(150+100*(r+1), -600, enemyStrategy[r], enemySpeed, robots.get(0)));
				robotShapes.add(new ShapeRenderer());
			}
		
		ball = new Ball(-150, -850);
		score = 0;
		time = 60;
	}
}
