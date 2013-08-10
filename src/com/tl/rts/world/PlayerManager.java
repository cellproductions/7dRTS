package com.tl.rts.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerManager implements Iterable<Player>
{
	static ConcurrentLinkedQueue<Player> players;
	static byte numPlayers;
	public static Player player;
	public static Difficulty difficulty = Difficulty.EASY;
	
	public static void setPlayerCount(byte numPlayers)
	{
		PlayerManager.numPlayers = numPlayers;
		players = new ConcurrentLinkedQueue<>();
	}
	
	public static void addPlayer(Player player)
	{
		players.add(player);
	}
	
	public static void removePlayer(Player player)
	{
		players.remove(player);
	}
	
	public static ArrayList<Player> getPlayers()
	{
		return new ArrayList<>(players);
	}
	
	public static byte getPlayerSize()
	{
		return numPlayers;
	}
	
	public static void reset()
	{
		player.reset();
		players.clear();
		numPlayers = WorldGenerator.numPlayers;
		players = new ConcurrentLinkedQueue<>();
	}

	public Iterator<Player> iterator()
	{
		return players.iterator();
	}
	
	public static void update(int delta)
	{
		for (Player player : players)
			player.update(delta);
	}
	
	public static enum Difficulty
	{
		EASY, MEDIUM, HARD
	}
}
