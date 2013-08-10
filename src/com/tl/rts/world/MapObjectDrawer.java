package com.tl.rts.world;

import java.util.ArrayList;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;


public class MapObjectDrawer
{
	private static ArrayList<MapObjectStar> list;
	private static LinkedList<MapObjectFleet> fleets = new LinkedList<>();
	
	public static void setCount(int starCount)
	{
		list = new ArrayList<>(starCount);
	}
	
	public static void initFleetObjects()
	{
		for (MapObjectFleet obj : fleets)
			obj.init();
	}
	
	public static void addStarObject(MapObjectStar object)
	{
		list.add(object);
	}
	
	public static void addFleetObject(MapObjectFleet object)
	{
		fleets.add(object);
	}
	
	public static void removeFleetObject(MapObjectFleet object)
	{
		fleets.remove(object);
	}
	
	public static void draw(Graphics g)
	{
		for (MapObjectStar object : list)
			object.draw(g);
		for (MapObjectFleet object : fleets)
			object.draw(g);
	}
	
	public static void reset()
	{
		list.clear();
		fleets.clear();
		list = new ArrayList<>(WorldGenerator.numStars);
	}
}
