package com.tl.rts.world;

import java.util.ArrayList;
import java.util.LinkedList;

import com.tl.rts.world.WorldGenerator.Couple;

import tl.Util.TPoint;

public class PathFinder
{
	private static Star startStar;
	public static LinkedList<Star> createPath(Fleet start, Star destination)
	{
		LinkedList<Star> noEnter = new LinkedList<Star>();
		LinkedList<Star> queue = new LinkedList<>();
		
		startStar = getNearest(start.getPosition());
		Star prev = new Star();
		searchPath(startStar, prev, destination, queue, noEnter);
		
		if (start.getPosition().compare(startStar.getPosition()) == 0) // only remove the starting star if the fleet is sitting idle on a system
			queue.remove(startStar);
		if (queue.peekFirst() == start.getCurrentDestination() && !queue.isEmpty())
			queue.removeFirst();
			/*
		System.out.println("pathing:");
		for (Star star : queue)
			System.out.println(star.getName());*/
		return queue;
	}
	
	private static void searchPath(Star search, Star prev, Star destination, LinkedList<Star> queue, LinkedList<Star> noEnter)
	{
		//System.out.println(search.getName());
		queue.add(search);
		noEnter.add(prev);
		if (search == destination)
			return;
		else
		{
			Star next;
			if (search.connectionCount() == 1 && search != startStar)
			{
				do
				{
					if (queue.peekLast() == null)
						return;
					noEnter.add(queue.pollLast());
					next = queue.peekLast();
					if (queue.peekLast() == null)
						return;
				} while (queue.peekLast().connectionCount() <= 2);
			}
			else
			{
				next = getNearest(search.getConnections(), destination, noEnter);
				if (next == null)
				{
					do
					{
						if (queue.peekLast() == null)
							return;
						next = queue.pollLast();
					} while (hasVisitedAll(next.getConnections(), next, noEnter));
				}
			}
			prev = search;
			searchPath(next, prev, destination, queue, noEnter);
		}
	}
	
	private static boolean hasVisitedAll(ArrayList<Star> visited, Star check, LinkedList<Star> noEnter)
	{
		for (Star star : visited)
		{
			if (!noEnter.contains(star))
			{
				check = star;
				return false;
			}
		}
		return true;
	}
	
	public static Star getNearest(TPoint position)
	{
		Couple nearest = new Couple(null, (double)WorldGenerator.maxDim * 2);
		for (Star star : new StarManager())
		{
			double dist = TPoint.distance(position, star.getPosition());
			if (dist < nearest.distance)
				nearest.set(star, dist);
		}
		return nearest.star;
	}
	
	private static Star getNearest(ArrayList<Star> connections, Star destination, LinkedList<Star> noEnter)
	{
		Couple nearest = new Couple(null, (double)WorldGenerator.maxDim * 2);
		for (Star star : connections)
		{
			if (!noEnter.contains(star))
			{
				double dist = TPoint.distance(star.getPosition(), destination.getPosition());
				//System.out.println(star.getName() + " " + dist + " : " + nearest.distance);
				if (dist < nearest.distance)
					nearest.set(star, dist);
			}
		}
			
		return nearest.star;
	}
}
