package com.tl.rts.world;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.newdawn.slick.Graphics;

public class FleetManager
{
	private static ConcurrentLinkedQueue<Fleet> fleets;
	
	public static void setCount(int shipCount)
	{
		fleets = new ConcurrentLinkedQueue<Fleet>();
	}
	
	public static void addFleet(Fleet fleet)
	{
		fleets.add(fleet);
	}
	
	synchronized public static void removeFleet(Fleet fleet)
	{
		if (fleet != null)
		{
			fleet.kill();
			fleets.remove(fleet);
			fleet = null;
		}
	}
	
	synchronized public static void removeAll(Collection<?> c)
	{
		fleets.removeAll(c);
	}
	
	public static void reset()
	{
		fleets.clear();
		Fleet.selected = null;
		Fleet.selectedCircle = null;
		Fleet.graphic = null;
		fleets = new ConcurrentLinkedQueue<>();
	}
	
	public static void draw(Graphics g, int delta)
	{
		for (Fleet fleet : fleets)
			fleet.draw(g, delta);
	}
	
	public static void mouseButtonPressed(int button, int x, int y)
	{
		for (Fleet fleet : fleets)
			fleet.mouseButtonPressed(button, x, y);
	}
	
	public static void conflictResolver(Fleet attack, Fleet defend)
	{
		Fleet a = new Fleet(attack);
		Fleet b = new Fleet(defend);
		Player attacker = a.getOwner();
		Player defender = b.getOwner();
		float bonus = .2f;
		// System.out.println(attacker.getName() + " vs " + defender.getName());
		// System.out.println((int)((float)a.getFleetCount() * attacker.getAttack()) + " - " + (int)((float)b.getFleetCount() * (defender.getDefence() + bonus)));
		attack.resolve(Math.min((int)((float)a.getFleetCount() * attacker.getAttack()) - (int)((float)b.getFleetCount() * (defender.getDefence() + bonus)), a.getFleetCount()));
		// System.out.println((int)((float)b.getFleetCount() * (defender.getDefence() + bonus)) + " - " + (int)((float)a.getFleetCount() * attacker.getAttack()));
		defend.resolve(Math.min((int)((float)b.getFleetCount() * (defender.getDefence() + bonus) - (int)((float)a.getFleetCount() * attacker.getAttack())), b.getFleetCount()));
	}
	
	public static void conflictResolver(Fleet attack, Star defend)
	{
		Fleet a = new Fleet(attack);
		Star b = new Star(defend);
		Player attacker = a.getOwner();
		Player defender = b.getOwner();
		float bonus = .1f;
		// System.out.println(attacker.getName() + " vs " + defender.getName());
		// System.out.println((int)((float)a.getFleetCount() * (attacker.getAttack() + bonus)) + " - " + (int)((float)b.getPopulation() * (defender.getSystemDefence())));
		attack.resolve(Math.min((int)((float)a.getFleetCount() * (attacker.getAttack() + bonus)) - (int)((float)b.getPopulation() * (defender.getSystemDefence())), a.getFleetCount()));
		// System.out.println((int)((float)b.getPopulation() * (defender.getSystemDefence())) + " - " + (int)((float)a.getFleetCount() * (attacker.getAttack() + bonus)));
		defend.resolve(Math.min((int)((float)b.getPopulation() * (defender.getSystemDefence())) - (int)((float)a.getFleetCount() * (attacker.getAttack() + bonus)), b.getPopulation()), attack);
	}
}
