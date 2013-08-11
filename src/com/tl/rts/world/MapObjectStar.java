package com.tl.rts.world;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import com.tl.rts.src.StateLevel;

import tl.GUI.TContainer;
import tl.Util.TBound;
import tl.Util.TPoint;

public class MapObjectStar
{
	private Star holder;
	private Player owner;
	private TBound bounds;
	private TPoint drawFrom;
	private static final byte size = 4;
	private ArrayList<TPoint> connections;
	
	public MapObjectStar(Star star, Player owner, ArrayList<Star> connections)
	{
		holder = star;
		drawFrom = convertToMiniPoint(star.getPosition());
		bounds = new TBound(drawFrom.x - size / 2, drawFrom.y - size / 2, size, size);
		this.owner = owner;
		this.connections = new ArrayList<>(connections.size());
		for (Star conns : connections)
			this.connections.add(new TPoint(convertToMiniPoint(conns.getPosition())));
		MapObjectDrawer.addStarObject(this);
	}
	
	private TPoint convertToMiniPoint(TPoint point)
	{
		TContainer<Object> map = StateLevel.cMinimapDisplay;
		return new TPoint(point.x / WorldGenerator.getSize().width * map.itemWidth() + map.getScreenX(),
				point.y / WorldGenerator.getSize().height * map.itemHeight() + map.getScreenY());
	}
	
	public void setOwner(Player owner)
	{
		this.owner = owner;
	}
	
	public void draw(Graphics g)
	{
		g.setColor(Star.neutral);
		for (TPoint point : connections)
			g.drawLine(drawFrom.x, drawFrom.y, point.x, point.y);
		if (owner != null)
			g.setColor(owner.getColour());
		TPoint point = bounds.getTopCorner();
		g.fillRect(point.x, point.y, size, size);
	}
	
	public void mouseButtonPressed(int button, int x, int y)
	{
		if (bounds.isWithin(x, y))
		{
			if (button == 0)
				holder.selectCommand();
			else if (button == 1)
				holder.moveCommand();
		}
	}
}
