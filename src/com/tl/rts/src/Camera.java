package com.tl.rts.src;

import org.newdawn.slick.Input;

import tl.Util.TBound;
import tl.Util.TPoint;
import tl.Util.TSize;

public class Camera
{
	private static Input input;
	public static TPoint position = new TPoint();
	public static TSize size;
	public static float zoom = 1f;
	private static TBound stateBounds;
	private static TPoint dest = new TPoint();
	private static TPoint direction = new TPoint();
	private static TPoint startPoint = new TPoint();
	private static double startDistance;
	private static boolean startMoving;
	private static boolean moving;
	private static float speed;
	private static float controlSpeed;
	
	public static void init(Input input, int width, int height)
	{
		Camera.input = input;
		size = new TSize(width, height);
		stateBounds = new TBound(new TPoint(0, 0), new TPoint(size.width, size.height));
	}
	
	public static void setStateBounds(TBound bounds)
	{
		stateBounds.set(bounds);
	}
	
	public static void setStateBounds(TPoint topLeft, TPoint bottomRight)
	{
		stateBounds.set(topLeft, bottomRight);
	}
	
	public static void resize(int width, int height)
	{
		size.set(width, height);
		checkBounds();
	}
	
	public static void setPosition(float x, float y)
	{
		position.set(x, y);
		startPoint.set(x, y);
		startDistance = TPoint.distance(position, dest);
		direction = TPoint.normalizeDestination(position, dest);
		checkBounds();
	}
	
	public static void setControlSpeed(float speed)
	{
		controlSpeed = speed;
	}
	
	public static void move(float x, float y, float speed)
	{
		dest.set(x, y);
		startPoint.set(position);
		startDistance = TPoint.distance(position, dest);
		direction = TPoint.normalizeDestination(position, dest);
		moving = true;
		startMoving = true;
		Camera.speed = speed;
	}
	
	public static void stop()
	{
		moving = false;
		startMoving = false;
	}
	
	public static boolean moving()
	{
		return startMoving && moving;
	}
	
	public static void update(int delta)
	{
		if (startMoving)
		{
			position = position.add(direction.x * speed * delta, direction.y * speed * delta);
			if (TPoint.distance(startPoint, position) < startDistance)
			{
				if (checkBounds())
					stop();
			}
			else
				stop();
		}
		else
		{
			boolean pressed = false;
			if (input.isKeyDown(Input.KEY_LEFT))
			{
				position.x -= controlSpeed * zoom * delta;
				pressed = true;
			}
			else if (input.isKeyDown(Input.KEY_RIGHT))
			{
				position.x += controlSpeed * zoom * delta;
				pressed = true;
			}
			if (input.isKeyDown(Input.KEY_UP))
			{
				position.y -= controlSpeed * zoom * delta;
				pressed = true;
			}
			else if (input.isKeyDown(Input.KEY_DOWN))
			{
				position.y += controlSpeed * zoom * delta;
				pressed = true;
			}
				
			if (pressed)
				checkBounds();
		}
	}
	
	private static boolean checkBounds()
	{
		boolean breached = false;
		float x1 = stateBounds.tCorner.x;
		float y1 = stateBounds.tCorner.y;
		float x2 = stateBounds.bCorner.x;
		float y2 = stateBounds.bCorner.y;
		if (position.x < x1)
		{
			position.x = x1;
			breached = true;
		}
		if (position.y < y1)
		{
			position.y = y1;
			breached = true;
		}
		if (position.x + size.width > x2)
		{
			position.x = x2 - size.width;
			breached = true;
		}
		if (position.y + size.height > y2)
		{
			position.y = y2 - size.height;
			breached = true;
		}
		return breached;
	}
}
