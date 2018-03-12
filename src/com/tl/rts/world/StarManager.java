package com.tl.rts.world;

import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.Graphics;

public class StarManager implements Iterable<Star>
{
	private static ArrayList<Star> stars;
	
	public static void setCount(int starCount)
	{
		stars = new ArrayList<>(starCount);
		MapObjectDrawer.setCount(starCount);
	}
	
	public static void addStar(Star star)
	{
		stars.add(star);
	}
	
	public static Star getStar(int index)
	{
		return stars.get(index);
	}

	/**
	 * @author Hanzallah Burney
	 * @param star
	 * @return Index of a star
	 */
	public static int getIndex( Star star)
	{
		int i = 0;
		while (getStar(i) != star)
		{
			i++;
		}
		return i;
	}
	
	public static void reset()
	{
		stars.clear();
		Star.graphic = null;
		stars = new ArrayList<>(WorldGenerator.numStars);
		MapObjectDrawer.reset();
	}
	
	public static void draw(Graphics g, int delta)
	{
		for (Star star : stars)
			star.draw(g, delta);
	}

	public Iterator<Star> iterator()
	{
		return stars.iterator();
	}
	
	public static void mouseButtonPressed(int button, int x, int y)
	{
		for (Star star : stars)
			star.mouseButtonPressed(button, x, y);
	}
}
