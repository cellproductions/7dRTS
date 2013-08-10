package com.tl.rts.world;

import org.newdawn.slick.Color;

import com.tl.rts.src.Camera;

import tl.Util.TCursor;
import tl.Util.TPoint;
import tl.Util.TSize;

public class Entity
{
	protected Color teamColour;
	protected TPoint position;
	protected TSize size;
	protected Player owner;
	protected int offset;
	
	public Entity()
	{
		position = new TPoint();
		size = new TSize();
	}
	
	public Entity(TPoint point, TSize size)
	{
		position = new TPoint((int)point.x, (int)point.y);
		this.size = new TSize(size);
	}
	
	public void setSize(TSize size)
	{
		size.set(size);
	}
	
	public void setPosition(TPoint point)
	{
		position.set(point);
	}
	
	public TPoint getPosition()
	{
		return position;
	}
	
	public TSize getSize()
	{
		return size;
	}
	
	public void setColour(Color colour)
	{
		teamColour = colour;
	}
	
	public void setOwner(Player player)
	{
		owner = player;
		setColour(player.getColour());
	}
	
	public Player getOwner()
	{
		return owner;
	}
	
	protected boolean withinCamera()
	{
		float x = Camera.position.x;
		float y = Camera.position.y;
		float width = position.x + size.width;
		float height = position.y + size.height;
		return width >= x && position.x < x + Camera.size.width && height >= y && position.y < y + Camera.size.height;
	}
	
	public boolean mouseIsOver()
	{
		float x = TCursor.getX() / Camera.zoom + Camera.position.x;
		float y = TCursor.getY() / Camera.zoom + Camera.position.y;
		return x > position.x - offset && x <= position.x - offset + size.width && y > position.y - offset && y <= position.y - offset + size.height;
	}
}
