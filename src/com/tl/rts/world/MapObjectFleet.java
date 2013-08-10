package com.tl.rts.world;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;

import tl.GUI.TContainer;
import tl.Util.TBound;
import tl.Util.TPoint;

import com.tl.rts.src.GameSettings;
import com.tl.rts.src.StateLevel;

public class MapObjectFleet
{
	private Fleet holder;
	private Player owner;
	private TBound bounds;
	private TPoint drawFrom;
	private Circle circle;
	private static final byte size = 4;
	private LinkedList<TPoint> path;
	
	public MapObjectFleet(Fleet fleet, Player owner)
	{
		holder = fleet;
		this.owner = owner;
		this.path = new LinkedList<>();
		MapObjectDrawer.addFleetObject(this);
	}
	
	private TPoint convertToMiniPoint(TPoint point)
	{
		TContainer<Object> map = StateLevel.cMinimapDisplay;
		return new TPoint(point.x / WorldGenerator.getSize().width * map.itemWidth() + map.getScreenX(),
				point.y / WorldGenerator.getSize().height * map.itemHeight() + map.getScreenY());
	}
	public void init()
	{
		drawFrom = convertToMiniPoint(holder.getPosition());
		bounds = new TBound(drawFrom.x - size / 2, drawFrom.y - size / 2, size, size);
		circle = new Circle(drawFrom.x, drawFrom.y, size);
	}
	
	public void kill()
	{
		MapObjectDrawer.removeFleetObject(this);
	}
	
	public void setPath(LinkedList<Star> path)
	{
		this.path.clear();
		for (Star point : path)
			this.path.add(new TPoint(convertToMiniPoint(point.getPosition())));
	}
	
	public void clearPath()
	{
		path.clear();
	}
	
	public void draw(Graphics g)
	{
		drawFrom = convertToMiniPoint(holder.getPosition());
		bounds = new TBound(drawFrom.x - size / 2, drawFrom.y - size / 2, size, size);
		circle = new Circle(drawFrom.x, drawFrom.y, size);
		if (GameSettings.getDrawMovement() && holder == Fleet.selected)
		{
			g.setColor(Color.red);
			TPoint prev = drawFrom;
			for (TPoint point : path)
			{
				g.drawLine(prev.x, prev.y, point.x, point.y);
				prev = point;
			}
		}
		g.setColor(owner.getColour());
		g.draw(circle);
	}
	
	public void mouseButtonPressed(int button, int x, int y)
	{
		if (bounds.isWithin(x, y))
		{
			// allow ship selection
		}
	}
}
