package com.tl.rts.world;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tl.rts.world.PlayerManager.Difficulty;
import com.tl.rts.src.StateLevel;

import tl.GUI.TLabel;
import tl.Util.TPoint;

public class AI
{	
	private static Random rand = new Random();
	private static ConcurrentLinkedQueue<Player> players;
	
	public static void initAI()
	{
		int time = 0;
		switch (PlayerManager.difficulty)
		{
			case EASY:
				time = 13;
				break;
			case MEDIUM:
				time = 6;
				break;
			case HARD:
				time = 2;
				break;
		}
		
		AIState.IDLE.delay = new Idle(time);
		AIState.EXPLORATORY.delay = new Idle(time);
		AIState.DEFENSIVE.delay = new Idle(time);
		AIState.AGGRESSIVE.delay = new Idle(time);
		AIState.RESEARCHER.delay = new Idle(time);
		AIState.DEAD.delay = new Idle(time);
		
		players = new ConcurrentLinkedQueue<>();
		players.addAll(PlayerManager.players);
		players.add(PlayerManager.player);
		for (Player player : players)
		{
			player.changeState(AIState.EXPLORATORY);
			if (player != PlayerManager.player)
			{
				switch (PlayerManager.difficulty)
				{
					case MEDIUM:
						player.updateTech(3f);
						break;
					case HARD:
						player.updateTech(7f);
						break;
					case EASY: // not used
						break;
				}
			}
		}
	}
	
	///////////////////////////////////////////// ACTIONS ////////////////////////////////////////////////////////////
	
	private static Star findClosestEmpty(Fleet fleet) // search surrounding stars, return an empty one if there are any, otherwise its own one if any, otherwise null
	{
		Star nearest = PathFinder.getNearest(fleet.getPosition());
		for (Star star : nearest.getConnections()) // search for empty stars
			if (star.getOwner() == null)
				return star;
		int size = nearest.getConnections().size();
		Star star = nearest.getConnections().get(rand.nextInt(size));
		ArrayList<Star> searched = new ArrayList<>(nearest.getConnections());
		while (star.getOwner() != fleet.getOwner() && !searched.isEmpty()) // search for own star
		{
			searched.remove(star);
			star = nearest.getConnections().get(rand.nextInt(size));
		}
		if (star.getOwner() != fleet.getOwner()) // no empty or own stars (star + fleet cut off) found, return null
			star = null;
		return star;
	}
	
	private static void seekEmptyStar(Player player)
	{
		for (Fleet fleet : player.ownedFleets)
		{
			if (!fleet.isPathing())
			{
				Star empty = findClosestEmpty(fleet);
				if (empty != null)
					fleet.moveTo(empty);
			}
		}
	}
	
	private static Star findClosestOwned(Fleet fleet)
	{
		Player nearest = getNearestPlayer(fleet.getOwner());
		Star closest = new Star();
		closest.setPosition(new TPoint(WorldGenerator.maxDim * 2, WorldGenerator.maxDim * 2));
		
		for (Star star : nearest.ownedStars)
			if (TPoint.distance(fleet.getPosition(), star.getPosition()) < TPoint.distance(fleet.getPosition(), closest.getPosition()))
				closest = star;
		return closest;
	}
	
	private static void seekOwnedStar(Player player)
	{
		for (Fleet fleet : player.ownedFleets)
			if (!fleet.isPathing())
				fleet.moveTo(findClosestOwned(fleet));
	}
	
	private static Star findDefensiveStar(Player player)
	{
		for (Star star : player.ownedStars) // look for a border star
			for (Star conn : star.getConnections())
				if (conn.getOwner() == null)
					return star;
		
		Star mostTech = new Star();
		for (Star star : player.ownedStars) // look for the star with the most technology on it
			if (mostTech.getTechnology() < star.getTechnology())
				mostTech = star;
		return mostTech;
	}
	
	private static void seekDefensiveStars(Player player)
	{
		for (Fleet fleet : player.ownedFleets)
			if (!fleet.isPathing())
				fleet.moveTo(findDefensiveStar(player));
	}
	
	private static Player getNearestPlayer(Player nearestTo)
	{
		TPoint nearestToPoint = nearestTo.startingStar.getPosition();
		Star nearest = new Star();
		nearest.setPosition(new TPoint(WorldGenerator.maxDim * 2, WorldGenerator.maxDim * 2));
		for (Player player : players)
			if (player != nearestTo)
				if (TPoint.distance(nearestToPoint, player.startingStar.getPosition()) < TPoint.distance(nearestToPoint, nearest.getPosition()))
					nearest = player.startingStar;
		return nearest.getOwner();
	}
	
	private static void placeFleetOnRandom(Player player)
	{
		ArrayList<Star> list = new ArrayList<>(player.ownedStars);
		Star randomStar = list.get(rand.nextInt(player.ownedStars.size()));
		randomStar.addFleet(new Fleet(randomStar.getPosition(), player, 1));
	}
	
	private static void colonizeRandomStar(Player player)
	{
		ArrayList<Fleet> list = new ArrayList<>(player.ownedFleets);
		for (Fleet fleet : list)
		{
			Star star = fleet.isOverStar();
			if (star != null && rand.nextBoolean())
			{
				star.removeFleet(true);
				star.setPopulation((short)(star.getPopulation() + 1));
				return;
			}
		}
	}
	
	private static void chooseRandomResearch(Player player)
	{
		switch (rand.nextInt(6))
		{
			case 0:
				player.updateSpeed();
				break;
			case 1:
				player.updateAttack();
				break;
			case 2:
				player.updateDefence();
				break;
			case 3:
				player.updateSystemDefence();
				break;
			case 4:
				placeFleetOnRandom(player);
				break;
			case 5:
				colonizeRandomStar(player);
				break;
		}
		player.spendTechPoint();
	}
	
	private static void placeFleetOnWeighted(Player player) // not really weighted
	{
		for (Star star : player.ownedStars)
		{
			boolean hasEmpty = false;
			for (Star conn : star.getConnections())
			{
				if (conn.getOwner() == null)
				{
					hasEmpty = true;
					break;
				}
			}
			if (hasEmpty)
			{
				star.addFleet(new Fleet(star.getPosition(), player, 1));
				return;
			}
		}
	}
	
	private static void colonizeWeighted(Player player)
	{
		for (Star star : player.ownedStars) // look for a star with a hovering fleet thats at the heart of the territory
		{
			if (star.getHoveringFleet() != null)
			{
				boolean hasEmpty = false;
				for (Star conn : star.getConnections())
				{
					if (conn.getOwner() == null)
					{
						hasEmpty = true;
						break;
					}
				}
				if (!hasEmpty)
				{
					star.removeFleet(1);
					star.setPopulation((short)(star.getPopulation() + 1));
					return;
				}
			}
		}
		for (Star star : player.ownedStars) // if no such star was found, look for a star with a hovering fleet
		{
			if (star.getHoveringFleet() != null)
			{
				star.removeFleet(1);
				star.setPopulation((short)(star.getPopulation() + 1));
				return;
			}
		}
	}
	
	private static void chooseWeightedResearch(Player player) // this should have been done much more gracefully
	{
		Player nearest = getNearestPlayer(player);
		Weighting speed = new Weighting((byte)0, Weighting.Type.SPEED);
		Weighting attack = new Weighting((byte)0, Weighting.Type.ATTACK);
		Weighting defence = new Weighting((byte)0, Weighting.Type.DEFENCE);
		Weighting systemdefence = new Weighting((byte)0, Weighting.Type.SYSDEFENCE);
		Weighting fleet = new Weighting((byte)0, Weighting.Type.FLEET);
		Weighting colonize = new Weighting((byte)0, Weighting.Type.COLONIZE);
		
		if (nearestIsTechingAttack(player, nearest))
		{
			++defence.weight;
			if (rand.nextBoolean())
				systemdefence.weight += 2;
			if (rand.nextBoolean())
				++fleet.weight;
			if (rand.nextBoolean())
				++colonize.weight;
		}
		if (nearestIsTechingSpeed(player, nearest))
		{
			++systemdefence.weight;
			if (rand.nextBoolean())
				++speed.weight;
			if (rand.nextBoolean())
				++attack.weight;
		}
		if (nearestIsTechingDefence(player, nearest))
		{
			attack.weight += 2;
			if (rand.nextBoolean())
				++fleet.weight;
			if (rand.nextBoolean())
				++speed.weight;
			if (rand.nextBoolean())
				++colonize.weight;
		}
		if ((float)nearest.ownedFleets.size() * .9f >= (float)player.ownedFleets.size() - .5f)
		{
			if (rand.nextBoolean())
				++defence.weight;
			if (rand.nextBoolean())
				++fleet.weight;
			if (rand.nextBoolean())
				++speed.weight;
			if (rand.nextBoolean())
				++systemdefence.weight;
		}
		if ((float)nearest.ownedStars.size() * .9f >= (float)player.ownedStars.size() - .5f)
		{
			colonize.weight += 2;
			if (rand.nextBoolean())
				++fleet.weight;
			if (rand.nextBoolean())
				++speed.weight;
		}
		
		Weighting weight = new Weighting((byte)0, Weighting.Type.ATTACK);
		if (weight.weight < speed.weight)
			weight = speed;
		if (weight.weight < attack.weight)
			weight = attack;
		if (weight.weight < defence.weight)
			weight = defence;
		if (weight.weight < systemdefence.weight)
			weight = systemdefence;
		if (weight.weight < fleet.weight)
			weight = fleet;
		if (weight.weight < colonize.weight)
			weight = colonize;
		
		switch (weight.type)
		{
			case SPEED:
				player.updateSpeed();
				break;
			case ATTACK:
				player.updateAttack();
				break;
			case DEFENCE:
				player.updateDefence();
				break;
			case SYSDEFENCE:
				player.updateSystemDefence();
				break;
			case FLEET:
				placeFleetOnWeighted(player);
				break;
			case COLONIZE:
				colonizeWeighted(player);
				break;
		}
	}
	
	private static void buyFleets(Player player)
	{
		int fsize = player.ownedFleets.size();
		int ssize = player.ownedStars.size();
		int tpoints = player.getTechPoints();
		if (fsize < ssize * .1f)
			for (int i = 0; i < Math.min(tpoints, (ssize * .1f > tpoints ? tpoints : ssize * .1f)); ++i)
				colonizeRandomStar(player);
	}
	
	private static void accumulateFleets(Player player, float percentage)
	{
		int fsize = player.ownedFleets.size();
		int size = (int)(fsize * percentage);
		ArrayList<Fleet> all = new ArrayList<Fleet>(player.ownedFleets);
		if (all.size() > 0)
		{
			ArrayList<Fleet> toMove = new ArrayList<Fleet>((int)(fsize * percentage));
			for (int i = 0; i < size; ++i)
			{
				toMove.add(all.get(i));
				all.remove(i);
			}
			ArrayList<Fleet> moveTo = new ArrayList<Fleet>(all);
			
			if (!moveTo.isEmpty())
			{
				int count = 0;
				for (Fleet fleet : toMove)
				{
					if (count == moveTo.size())
						count = 0;
					Fleet to = moveTo.get(count);
					Star star = to.isOverStar();
					if (star != null)
						fleet.moveTo(star);
					++count;
				}
			}
		}
	}
	
	public static void killPlayer(Player player)
	{
		StateLevel.lMessage.setText(player.getName() + " was defeated!");
		for (Fleet fleet : player.ownedFleets)
			fleet.kill();
		FleetManager.removeAll(player.ownedFleets);
		player.ownedFleets.clear();
		PlayerManager.removePlayer(player);
		players.remove(player);
		if (players.size() == 1)
		{
			StateLevel.lMessage.setText("Good game!");
			StateLevel.gOver.setVisible(true);
			((TLabel)StateLevel.gOver.child(0)).setText("Glory to " + players.iterator().next().getName() + "!"); // this probably shouldnt be here, but meh
		}
	}
	
	///////////////////////////////////////////// EVENTS ////////////////////////////////////////////////////////////
	
	private static boolean isBeingAttacked(Player player)
	{
		for (Player other : players)
			if (other != player)
				for (Fleet fleet : other.ownedFleets)
					if (fleet.isMoving() || fleet.isPathing())
						if (fleet.getCurrentDestination() != null)
							if (fleet.getCurrentDestination().getOwner() == player)
								return true;
		return false;
	}
	
	private static boolean hasTechPoints(Player player)
	{
		return player.getTechPoints() > 0;
	}
	
	private static boolean hasExploring(Player player)
	{
		boolean hasEmpty = true;
		for (Star star : player.ownedStars)
			for (Star conn : star.getConnections())
				if (conn.getOwner() == null)
					hasEmpty = true;
		return hasEmpty;
	}
	
	private static boolean playerIsRipe(Player player)
	{
		Player nearest = getNearestPlayer(player);
		AIState state = nearest.getState();
		return state == AIState.EXPLORATORY || state == AIState.RESEARCHER || state == AIState.IDLE;
	}
	
	private static boolean hasAnyStars(Player player)
	{
		return player.ownedStars.size() > 0;
	}
	
	private static boolean nearestIsTechingAttack(Player player, Player nearest)
	{
		float buffer = player.getAttack();
		float near = nearest.getAttack();
		return near * .9f >= buffer - .5f && near * 1.1 <= buffer + .5f && (nearest.getState() == AIState.RESEARCHER && rand.nextBoolean());
	}
	
	private static boolean nearestIsTechingDefence(Player player, Player nearest)
	{
		float buffer = player.getDefence();
		float near = nearest.getDefence();
		return near * .9f >= buffer - .5f && near * 1.1 <= buffer + .5f && (nearest.getState() == AIState.RESEARCHER && rand.nextBoolean());
	}
	
	private static boolean nearestIsTechingSpeed(Player player, Player nearest)
	{
		float buffer = player.getSpeed();
		float near = nearest.getSpeed();
		return near * .9f >= buffer - .5f && near * 1.1 <= buffer + .5f && (nearest.getState() == AIState.RESEARCHER && rand.nextBoolean());
	}
	
	///////////////////////////////////////////// DIFFICULTY ////////////////////////////////////////////////////////////
	
	public static void easyAI(Player player, int delta)
	{
		if (!hasAnyStars(player))
			player.changeState(AIState.DEAD);
		else if (isBeingAttacked(player) && rand.nextBoolean())
			player.changeState(AIState.DEFENSIVE);
		else if (hasTechPoints(player) && rand.nextBoolean())
			player.changeState(AIState.RESEARCHER);
		else if (hasExploring(player) && rand.nextBoolean())
			player.changeState(AIState.EXPLORATORY);
		else
		{
			if (playerIsRipe(player) && rand.nextBoolean())
				player.changeState(AIState.AGGRESSIVE);
			else
				player.changeState(AIState.IDLE);
		}
	}
	
	public static void mediumAI(Player player, int delta)
	{
		if (!hasAnyStars(player))
			player.changeState(AIState.DEAD);
		else if (hasExploring(player) && rand.nextBoolean())
			player.changeState(AIState.EXPLORATORY);
		else if (hasTechPoints(player) && rand.nextBoolean())
			player.changeState(AIState.RESEARCHER);
		else if (playerIsRipe(player) && rand.nextBoolean())
			player.changeState(AIState.AGGRESSIVE);
		else if (isBeingAttacked(player) && rand.nextBoolean())
			player.changeState(AIState.DEFENSIVE);
		else
			player.changeState(AIState.IDLE);
	}
	
	public static void hardAI(Player player, int delta)
	{
		if (playerIsRipe(player))
			player.changeState(AIState.AGGRESSIVE);
		else if (hasExploring(player))
			player.changeState(AIState.EXPLORATORY);
		else if (hasTechPoints(player))
			player.changeState(AIState.RESEARCHER);
		else if (isBeingAttacked(player))
			player.changeState(AIState.DEFENSIVE);
		else
			player.changeState(AIState.IDLE);
	}
	
	public static enum AIState
	{
		IDLE(new State()
		{
			public void execute(Player player, int delta) // watch for events
			{
				switch (rand.nextInt(5))
				{
					case 0:
						player.changeState(AIState.IDLE);
						break;
					case 1:
						player.changeState(AIState.EXPLORATORY);
						break;
					case 2:
						player.changeState(AIState.AGGRESSIVE);
						break;
					case 3:
						player.changeState(AIState.DEFENSIVE);
						break;
					case 4:
						player.changeState(AIState.RESEARCHER);
						break;
				}
			}
		}), 
		EXPLORATORY(new State()
		{
			public void execute(Player player, int delta) // seek out and capture empty stars
			{
				seekEmptyStar(player);
			}
		}), 
		AGGRESSIVE(new State()
		{
			public void execute(Player player, int delta) // seek out and conquer enemy stars
			{
				buyFleets(player);
				switch (PlayerManager.difficulty)
				{
					case EASY:
						accumulateFleets(player, .5f);
						break;
					case MEDIUM:
						accumulateFleets(player, .67f);
						break;
					case HARD:
						accumulateFleets(player, .75f);
						break;
				}
				seekOwnedStar(player);
			}
		}), 
		DEFENSIVE(new State()
		{
			public void execute(Player player, int delta) // build up fleets around borders and important areas
			{
				buyFleets(player);
				if (rand.nextBoolean())
				{
					switch (PlayerManager.difficulty)
					{
						case EASY:
							accumulateFleets(player, .5f);
							break;
						case MEDIUM:
							accumulateFleets(player, .67f);
							break;
						case HARD:
							accumulateFleets(player, .75f);
							break;
					}
				}
				seekDefensiveStars(player);
			}
		}), 
		RESEARCHER(new State()
		{
			public void execute(Player player, int delta) // go idle and focus on teching up
			{
				if (hasTechPoints(player))
				{
					if (PlayerManager.difficulty == Difficulty.EASY)
						chooseRandomResearch(player);
					else
						chooseWeightedResearch(player);
				}
			}
		}), 
		DEAD(new State()
		{
			public void execute(Player player, int delta) // cease everything
			{
				killPlayer(player);
			}
		});
		
		State state;
		public Idle delay;
		
		AIState(State state)
		{
			this.state = state;
		}
		
		public void execute(Player player, int delta)
		{
			try
			{
				if (!hasAnyStars(player) || player.getState() == AIState.DEAD)
					killPlayer(player);
				else
				{
					if (delay.finished())
					{
						if (PlayerManager.difficulty == Difficulty.EASY)
							easyAI(player, delta);
						else if (PlayerManager.difficulty == Difficulty.MEDIUM)
							mediumAI(player, delta);
						else
							hardAI(player, delta);
						state.execute(player, delta);
					}
					delay.update(delta);
				}
			}
			catch (NullPointerException e)
			{
				System.err.println("Error: AI has not been initialised!");
				e.printStackTrace();
			}
		}
	}
	
	private interface State
	{
		public void execute(Player player, int delta);
	}
	
	private static class Idle
	{
		int waitTime;
		int counter;
		float time;
		float elapsed;
		
		private Idle(int time)
		{
			waitTime = time;
			time = 0;
			elapsed = 0;
		}
		
		public void update(int delta) // not a very accurate timer, but it doesnt really need to be, so it'll do
		{
			elapsed = ((float)delta / 1000f);
			time += elapsed;
			if (time >= 1f)
			{
				time = 0;
				++counter;
			}
		}
		
		public boolean finished()
		{
			if (counter >= waitTime)
			{
				counter = 0;
				time = 0;
				return true;
			}
			return false;
		}
	}
	
	private static class Weighting
	{
		public static enum Type
		{
			ATTACK, DEFENCE, SPEED, SYSDEFENCE, FLEET, COLONIZE
		}
		
		public Type type;
		public byte weight;
		
		public Weighting(byte start, Type type)
		{
			weight = start;
			this.type = type;
		}
	}
}
