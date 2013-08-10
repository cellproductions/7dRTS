package com.tl.rts.world;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.newdawn.slick.Color;

import com.tl.rts.src.SoundManager;
import com.tl.rts.src.StateLevel;
import com.tl.rts.world.AI.AIState;

public class Player
{
	Color colour;
	String name;
	public Star startingStar;
	ConcurrentLinkedQueue<Star> ownedStars;
	ConcurrentLinkedQueue<Fleet> ownedFleets;
	private int techPoints;
	private float techCount;
	private int totalTech;
	private float attack;
	private float defence;
	private float speed;
	private float systemDefence;
	private AIState state = AIState.EXPLORATORY;
	
	public Player(String name)
	{
		this.name = name;
		ownedStars = new ConcurrentLinkedQueue<>();
		ownedFleets = new ConcurrentLinkedQueue<>();
		attack = 1;
		defence = 1;
		systemDefence = 1;
		speed = 2.5f;
		techPoints = 1;
		techCount = 0;
	}
	
	public Player(String name, Color colour)
	{
		this.name = name;
		this.colour = new Color(colour);
		ownedStars = new ConcurrentLinkedQueue<>();
		ownedFleets = new ConcurrentLinkedQueue<>();
		attack = 1;
		defence = 1;
		systemDefence = 1;
		speed = 2.5f;
		techPoints = 1;
		techCount = 0;
	}
	
	public void update(int delta)
	{
		state.execute(this, delta);
		if (ownedStars.isEmpty())
			; // getName() has been defeated
	}
	
	public void changeState(AIState state)
	{
		//System.out.println(getName() + " changing from " + this.state.toString() + " to " + state.toString());
		this.state = state;
	}
	
	public AIState getState()
	{
		return state;
	}
	
	public void addStar(Star star)
	{
		if (ownedStars.size() == 0) // bit untrustworthy, but ill allow it
			startingStar = star; // home star
		ownedStars.add(star);
	}
	
	public void removeStar(Star star)
	{
		ownedStars.remove(star);
	}
	
	public void addFleet(Fleet fleet)
	{
		ownedFleets.add(fleet);
	}
	
	public void removeFleet(Fleet fleet)
	{
		ownedFleets.remove(fleet);
		FleetManager.removeFleet(fleet);
	}
	
	public ArrayList<Star> getStars()
	{
		return new ArrayList<>(ownedStars);
	}
	
	public ArrayList<Fleet> getFleets()
	{
		return new ArrayList<>(ownedFleets);
	}
	
	public void setColour(Color colour)
	{
		this.colour = new Color(colour);
	}
	
	public Color getColour()
	{
		return colour;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void updateTotalTech() // this function does not matter to the AI
	{
		if (this == PlayerManager.player)
		{
			totalTech += 1;
			StateLevel.updateTechGUI();
		}
	}
	
	public int getTotalTech()
	{
		return totalTech;
	}
	
	public void setTotalTech(int tech)
	{
		totalTech = tech;
	}
	
	public int getTechPoints()
	{
		return techPoints;
	}
	
	public void spendTechPoint()
	{
		if (techPoints != 0)
			techPoints -= 1;
	}
	
	public void updateTech(float amount)
	{
		techCount += amount;
		//if (this == PlayerManager.player)
			//System.out.println(amount + " | " + techCount);
		techPoints += techCount;
		if (techPoints > 50)
			techPoints = 50;
		if (techCount >= 1)
		{
			techCount = 0;
			if (this == PlayerManager.player)
			{
				StateLevel.lMessage.setText("You have gained a tech point.");
				SoundManager.playSound(StateLevel.mOver);
			}
		}
		if (this == PlayerManager.player)
			StateLevel.updateTechGUI();
	}

	public float getAttack()
	{
		return attack;
	}

	public void updateAttack()
	{
		if (attack < 100)
			attack += .2f;
	}
	
	public boolean attackAtMax()
	{
		return attack >= 100;
	}

	public float getDefence()
	{
		return defence;
	}

	public void updateDefence()
	{
		if (defence < 100)
			defence += .2f;
	}
	
	public boolean defenceAtMax()
	{
		return defence >= 100;
	}
	
	public float getSystemDefence()
	{
		return systemDefence;
	}
	

	public void updateSystemDefence()
	{
		if (systemDefence < 100)
			systemDefence += .2f;
	}
	
	public boolean systemDefenceAtMax()
	{
		return systemDefence >= 100;
	}
	
	public float getSpeed()
	{
		return speed;
	}
	
	public void updateSpeed()
	{
		if (speed < 20)
			speed += .5f;
	}
	
	public boolean speedAtMax()
	{
		return speed >= 20;
	}
	
	public void reset()
	{
		attack = 1;
		defence = 1;
		systemDefence = 1;
		speed = 2.5f;
		techPoints = 1;
		techCount = 0;
		totalTech = 1;
		ownedStars.clear();
		ownedFleets.clear();
		StateLevel.updateTechGUI();
	}
}
