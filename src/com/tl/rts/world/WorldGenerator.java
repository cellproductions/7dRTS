package com.tl.rts.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Color;

import com.tl.rts.src.StateMenu;

import tl.GUI.TContainer;
import tl.GUI.TTextBox;
import tl.Util.TPoint;
import tl.Util.TSize;

public class WorldGenerator
{
	static String seed;
	static final int maxDim = 25000;
	static final TSize maxSize = new TSize(maxDim, maxDim);
	static ArrayList<Star> stars;
	static int numStars;
	static byte startStars;
	static byte startShips;
	static byte numPlayers;
	static int maxSpread;
	static int minSpread;
	
	public static void setSeed(String newSeed)
	{
		seed = newSeed;
		SystemNameGenerator.setSeed(seed.hashCode());
	}
	
	public static void setStarMax(int max)
	{
		numStars = max;
		stars = new ArrayList<>(max);
		maxSpread = (maxSize.width + maxSize.height) / max;
		minSpread = (int)((Star.radius * 2) + ((numStars * 100 / 200) * .5f));
		StarManager.setCount(max);
	}
	
	public static void setStartStars(byte stars)
	{
		startStars = stars;
	}
	
	public static void setStartShips(byte ships)
	{
		startShips = ships;
		FleetManager.setCount(ships);
	}
	
	public static void setAICount(byte aiCount)
	{
		numPlayers = (byte)aiCount;
		PlayerManager.setPlayerCount(aiCount);
	}
	
	public static String getSeed()
	{
		return seed;
	}
	
	public static void generateWorld()
	{
		if (((TTextBox)StateMenu.gNew.child(15)).getText().isEmpty())
			seed = "" + new Random().nextLong();
		
		int offset = maxDim - minSpread - Star.radius;
		Random rand = new Random(seed.hashCode());
		TPoint randPoint = new TPoint();
		String name = "";
		HashSet<String> names = new HashSet<>(); // speeds up name collision detection a tad
		for (int i = 0; i < numStars; ++i)
		{
			do
			{
				int xseed = minSpread + rand.nextInt(offset);
				int yseed = minSpread + rand.nextInt(offset);
				randPoint.set(xseed, yseed);
			} while (!checkDistances(randPoint));
			
			do
			{
				name = SystemNameGenerator.createName(true);
			} while (!checkNames(name, names));
			
			stars.add(new Star(randPoint, name, (short)(rand.nextInt(10) + 1), (short)0));
		}
	}
	
	public static void generateConnections() // NOTE: a strand is a small group of connected stars
	{
		Random rand = new Random(seed.hashCode());
		for (Star star : stars) // find the closest stars and randomly add them
		{
			LinkedList<Couple> closest = new LinkedList<>();
			for (Star check : stars)
				if (star != check)
					closest.add(new Couple(check, TPoint.distance(star.getPosition(), check.getPosition())));
			Collections.sort(closest);
			byte cons =  (byte)(1 + rand.nextInt(Star.maxConnections));
			for (byte i = 0; i < cons; ++i)
			{
				Star s = closest.get(i).star;
				if (rand.nextBoolean() && !star.hasConnection(s) && !s.hasConnection(star))
					star.addConnection(closest.get(i).star);
			}
		}
		for (Star star : stars) // find and connect any stars, that have 0 connections, to the nearest star
			if (star.connectionCount() == 0)
				star.addConnection(findClosest(stars, star).star);

		for (byte i = 0; i < 3; ++i) // do a few runs to make sure that every strand is connected
		{
			LinkedList<LinkedList<Star>> sets = new LinkedList<>(); // collect all sets of stars (stars that are connected together in groups)
			for (Star outer : stars)
			{
				LinkedList<Star> set = new LinkedList<>();
				sets.add(set);
				set.add(outer);
				for (Star inner : outer.getConnections())
					addToSet(set, inner);
			}
			{
				LinkedList<LinkedList<Star>> toRemove = new LinkedList<>(); // remove all duplicate sets
				for (LinkedList<Star> hash : sets)
					for (LinkedList<Star> inner : sets)
						if (inner != hash)
							if (inner.contains(hash.iterator().next()))
								toRemove.add(inner);
				sets = toRemove;
			}
		
			int count = sets.size(); // connect the groups together
			if (count > 1)
			{
				for (LinkedList<Star> set : sets)
				{
					ArrayList<Star> list = new ArrayList<>(stars.size() - set.size());
					list.addAll(stars);
					list.removeAll(set);
					HashMap<Couple, StarPair> closest = new HashMap<>(list.size() * set.size());
					for (Star outer : set)
					{
						Couple couple = findClosest(list, outer);
						closest.put(couple, new StarPair(outer, couple.star));
					}
					ArrayList<StarPair> pairs = new ArrayList<>(closest.size()); // take the pairs, sort them
					pairs.addAll(closest.values());
					Collections.sort(pairs, new Comparator<StarPair>()
					{
						public int compare(StarPair arg0, StarPair arg1)
						{
							double dist1 = TPoint.distance(arg0.a.getPosition(), arg0.b.getPosition());
							double dist2 = TPoint.distance(arg1.a.getPosition(), arg1.b.getPosition());
							return dist1 < dist2 ? -1 : dist1 == dist2 ? 0 : 1;
						}
					});
					for (StarPair pair : pairs)
					{
						if (!pair.a.hasConnection(pair.b) && !pair.b.hasConnection(pair.a) && !set.contains(pair.b))
						{
							pair.a.addConnection(pair.b);
							break;
						}
					}
				}
			}
		}
	}
	
	private static <T> Couple findClosest(Collection<T> collection, T star)
	{
		Star outer = (Star)star;
		Couple closest = new Couple(outer, (double)maxDim);
		for (T check : collection)
		{
			Star inner = (Star)check;
			if (star != inner)
			{
				double dist = TPoint.distance(outer.getPosition(), inner.getPosition());
				if (closest.distance > dist)
					closest = new Couple(inner, dist);
			}
		}
		return closest;
	}
	/*
	private static HashSet<Star> getStarsStrand(HashSet<HashSet<Star>> all, Star toFind) // return a strand that toFind is in
	{
		for (HashSet<Star> set : all)
			if (set.contains(toFind))
				return set;
		return null; // not in any strand
	}
	*/
	/*
	private static HashSet<Star> findClosestStrand(ArrayList<HashSet<Star>> remaining, HashSet<Star> hash, HashSet<Star> avoid) // return closest strand to another strand
	{
		HashMap<Couple, HashSet<Star>> shortest = new HashMap<>();
		for (Star outer : hash)
			for (HashSet<Star> set : remaining)
				if (set != hash && set != avoid)
					shortest.put(findClosest(set, outer), set);
		return shortest.values().iterator().next();
	}
	*/
	private static void addToSet(LinkedList<Star> set, Star star)
	{
		if (!set.contains(star))
		{
			set.add(star);
			for (Star inner : star.getConnections())
				addToSet(set, inner);
		}
	}
	
	private static class StarPair
	{
		public Star a;
		public Star b;
		
		public StarPair(Star a, Star b)
		{
			this.a = a;
			this.b = b;
		}
	}
	
	public static class Couple implements Comparable<Couple>
	{
		Star star;
		Double distance;
		
		public Couple(Star star, Double distance) { this.star = star; this.distance = distance; }
		
		public void set(Star star, Double distance)  { this.star = star; this.distance = distance; }
		
		public int compareTo(Couple c)
		{
			return distance < c.distance ? -1 : (distance == c.distance ? 0 : 1);
		}
	}
	/*
	private static boolean setStarts(Star star, Player player, ArrayList<Star> contacted)
	{
		int size = contacted.size();
		if (star.getOwner() == null)
		{
			contacted.add(star);
			star.setOwner(player);
		}	
		if (contacted.size() != startStars)
		{
			Star locals[] = star.getConnections();
			for (byte i = (byte)size; i < startStars && i < locals.length - 1; ++i)
			{
				if (locals[i].getOwner() == null)
				{
					contacted.add(locals[i]);
					locals[i].setOwner(player);
				}
			}
			
			if (size == startStars)
				return false;
			
			setStarts(locals[locals.length - 1], player, contacted);
		}
		return false;
	}
	*/
	
	private static void setStarts(Star star, Player player, ArrayList<Star> contacted, Random rand)
	{
		if (contacted.size() == startStars)
			return;
		if (star.getOwner() == null)
		{
			contacted.add(star);
			star.setOwner(player);
			star.setPopulation((short)1);
			star.setTechnology((short)1);
			
			if (contacted.size() < startStars)
			{
				ArrayList<Star> locals = new ArrayList<Star>(star.getConnections());
				if (contacted.containsAll(locals))
				{
					do
						star = stars.get(rand.nextInt(numStars));
					while (star.getOwner() != null);
				}
				for (Star local : locals)
					setStarts(local, player, contacted, rand);
			}
		}
	}
	
	public static void generatePlayerStarts()
	{
		Random rand = new Random(seed.hashCode());
		{
			PlayerManager.player.setTotalTech(startStars);
			//for (int i = 0; i < 20; ++i) // DELETE
				//PlayerManager.player.updateSpeed();
			
			String rep = new String(SystemNameGenerator.defaultSyllables); // make the AI names sound a bit more robotic
			rep = rep.replace('l', 't');
			rep = rep.replace('b', 'z');
			rep = rep.replace('s', 'z');
			SystemNameGenerator.replaceSyllables(rep);
			
			@SuppressWarnings("unchecked")
			TContainer<Color> container = (TContainer<Color>)StateMenu.gNew.child(9);
			ArrayList<Color> colours = new ArrayList<>(container.itemCount() - 1);
			for (byte i = 0; i < container.itemCount(); ++i)
				if (i != container.getSelected())
					colours.add(container.getObject(i));
			
			for (byte i = 0; i < numPlayers; ++i) // randomly create a new AI player with a random name and a random colour
			{
				byte col = (byte)rand.nextInt(colours.size());
				PlayerManager.addPlayer(new Player(SystemNameGenerator.createName(), colours.get(col)));
				colours.remove(col);
			}
		}
		
		ArrayList<Star> contacted = new ArrayList<>(startStars);
		do
			setStarts(stars.get(rand.nextInt(numStars)), PlayerManager.player, contacted, rand); // assign stars to the real player
		while (contacted.size() != startStars);
		for (Player player : PlayerManager.getPlayers())
		{
			contacted = new ArrayList<>(startStars);
			do
				setStarts(stars.get(rand.nextInt(numStars)), player,contacted, rand); // assign stars to the AI
			while (contacted.size() != startStars); // if the starting star count hasnt been used up, but the ai is boxed in, 
													// then jump to a random star and continue from there
		}
	}
	
	public static void generateShipStarts()
	{
		generatePlayerStarts();
		
		ArrayList<Star> pstars = PlayerManager.player.getStars();
		Iterator<Star> itr = pstars.iterator();
		for (int i = 0; i < startShips; ++i)
		{
			Star star = itr.next();
			Fleet fleet = new Fleet(star.getPosition(), PlayerManager.player, 1);
			star.addFleet(fleet);
			if (!itr.hasNext())
				itr = pstars.iterator();
		}
		
		for (Player player : PlayerManager.getPlayers())
		{
			pstars = player.getStars();
			itr = pstars.iterator();
			for (int i = 0; i < startShips; ++i)
			{
				Star star = itr.next();
				Fleet fleet = new Fleet(star.getPosition(), player, 1);
				star.addFleet(fleet);
				if (!itr.hasNext())
					itr = pstars.iterator();
			}
		}
	}
	
	public static void generateMinimapObjects()
	{
		for (Star star : stars)
			star.generateMinimapObject();
		MapObjectDrawer.initFleetObjects();
	}
	
	private static boolean checkDistances(TPoint toCheck)
	{
		for (Star star : stars)
		{
			double dist = TPoint.distance(toCheck, star.getPosition());
			if (dist < minSpread)
				return false;
		}
		return true;
	}
	
	private static boolean checkNames(String toCheck, HashSet<String> map)
	{
		if (map.contains(toCheck))
			return false;
		map.add(toCheck);
		return true;
	}
	
	public static final TSize getSize()
	{
		return maxSize;
	}
	
	public static void reset()
	{
		stars.clear();
		stars = new ArrayList<>(numStars);
		maxSpread = (maxSize.width + maxSize.height) / numStars;
		minSpread = (int)((Star.radius * 2) + ((numStars * 100 / 200) * .5f));
		StarManager.setCount(numStars);
	}
}