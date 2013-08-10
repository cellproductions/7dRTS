package com.tl.rts.world;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Circle;

import com.tl.rts.src.Camera;
import com.tl.rts.src.Game;
import com.tl.rts.src.SoundManager;
import com.tl.rts.src.StateLevel;

import tl.Util.TCursor;
import tl.Util.TPoint;
import tl.Util.TSize;

public class Star extends Entity
{
	public static Image graphic;
	public static Color neutral = new Color(Color.white);
	public static final int radius = 18;
	public static final byte maxConnections = 3;
	public static Star selected;
	private ArrayList<Star> connections;
	private Circle circle;
	private String name;
	private short tech = 1;
	private short pop = 1;
	private Fleet fleet;
	private MapObjectStar mapObj;
	
	public Star()
	{
		super();
		connections = new ArrayList<>(maxConnections);
		setColour(neutral);
		setSize(new TSize(radius * 2, radius * 2));
		circle = new Circle(0, 0, radius);
		offset = radius;
	}
	
	public Star(Star star)
	{
		pop = star.pop;
		owner = star.owner;
	}
	
	@SuppressWarnings("deprecation")
	public Star(TPoint point, String name, short technology, short population)
	{
		super(point, new TSize(radius * 2, radius * 2 + Game.littleFont.getHeight(name) + 3));
		if (selected == null)
			selected = this;
		connections = new ArrayList<>(maxConnections);
		setColour(neutral);
		circle = new Circle((int)point.x, (int)point.y, radius);
		offset = radius;
		this.name = new String(name);
		tech = technology;
		pop = population;
		StarManager.addStar(this);
	}
	
	public void generateMinimapObject()
	{
		mapObj = new MapObjectStar(this, owner, connections);
	}
	
	public void addConnection(Star star)
	{
		if (connections.size() <= maxConnections && star.connections.size() <= maxConnections)
		{
			connections.add(star);
			star.connections.add(this);
		}
	}
	
	public boolean hasConnection(Star star)
	{
		return connections.contains(star);
	}
	
	public byte connectionCount()
	{
		return (byte)connections.size();
	}
	
	public ArrayList<Star> getConnections()
	{
		return new ArrayList<>(connections);
	}
	
	public void addFleet(Fleet uFleet)
	{
		// System.out.println("init hover " + (owner != null ? owner.getName() : owner) + " : " + uFleet.getOwner().getName());
		// System.out.println(fleet + " : owner == ufleets owner? " + (owner == uFleet.getOwner()));
		if (owner == uFleet.getOwner())
		{
			// System.out.println("yep");
			if (fleet == null)
				fleet = uFleet;
			else
			{
				fleet.addFleet(uFleet);
				// System.out.println("merged");
			}
		}
		else
		{
			// System.out.println("nope");
			if (owner == null)
			{
				// System.out.println("owner is null");
				if (fleet == null)
				{
					fleet = uFleet;
					// System.out.println("so is fleet");
				}
				setOwner(uFleet.getOwner());
				// System.out.println("owner is now " + uFleet.getOwner().getName());
				if (uFleet.getOwner() == PlayerManager.player)
					StateLevel.lMessage.setText(name + " has been captured.");
				pop = 1;
			}
			else
			{
				if (fleet == null)
				{
					// System.out.println("fleet is null, enemy fleet vs star");
					FleetManager.conflictResolver(uFleet, this);
				}
				else
				{
					// System.out.println("enemy fleet vs this fleet");
					FleetManager.conflictResolver(uFleet, fleet);
					if (fleet == null && uFleet.getFleetCount() > 0) // if this system's fleet has been destroyed and the attacking fleet is still alive
					{
						// System.out.println("they servived, fleet vs this star");
						FleetManager.conflictResolver(uFleet, this);
					}
				}
			}
		}
	}
	
	public void removeFleet(boolean dead)
	{
		if (fleet == Fleet.selected)
			StateLevel.updateFleetHUD(null);
		if (dead)
			owner.removeFleet(fleet);
		fleet = null;
	}
	
	public void removeFleet(int count) // could extend this one day, count = how many to remove
	{
		if (fleet != null)
		{
			if (count >= fleet.getFleetCount())
			{
				owner.removeFleet(fleet);
				fleet = null;
				if (fleet == Fleet.selected)
					StateLevel.updateFleetHUD(fleet);
			}
			else
				fleet.removeFleet(fleet.getFleetCount() - count, false);
		}
	}
	
	public void resolve(int count, Fleet victor)
	{
		if (count <= 0)
		{
			pop = 1;
			removeFleet(true);
			fleet = victor;
			owner.removeStar(this);
			if (fleet.getOwner() == PlayerManager.player || owner == PlayerManager.player)
				StateLevel.lMessage.setText(getName() + " was conquered by " + fleet.getOwner().getName());
			if (owner.ownedStars.isEmpty())
				AI.killPlayer(owner);
			setOwner(fleet.getOwner());
		}
		else
			pop = (short)count;
	}
	
	public Fleet getHoveringFleet()
	{
		return fleet;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setPosition(TPoint point)
	{
		super.setPosition(point);
		circle.setLocation(point.x, point.y);
	}
	
	public void setTechnology(short tech)
	{
		this.tech = tech;
	}
	
	public short getTechnology()
	{
		return tech;
	}
	
	public void setPopulation(short pop)
	{
		this.pop = pop;
	}
	
	public short getPopulation()
	{
		return pop;
	}
	
	public void setOwner(Player player)
	{
		super.setOwner(player);
		if (mapObj != null)
			mapObj.setOwner(player);
		player.addStar(this);
		resetPopTimer();
		if (StateLevel.levelLoaded)
			for (byte i = 0; i < tech; ++i)
				player.updateTotalTech();
	}
	
	private float popSpeed = .06f;
	private float techSpeed = .06f;
	private float popTimer = 0;
	private float techTimer = 0;
	private float timeBeforeTech = 90;
	private float timeBeforePop = 260;
	
	private void resetPopTimer()
	{
		popSpeed = .1f;
		popTimer = 0;
	}
	
	@SuppressWarnings("deprecation")
	public void draw(Graphics g, int delta)
	{
		if (owner != null)
		{
			float elapsed = ((float)delta / 100f);
			popTimer += popSpeed * elapsed;
			techTimer += techSpeed * elapsed;
			if (techTimer >= timeBeforeTech)
			{
				++tech;
				if (this == selected)
					StateLevel.updateStarHUD(this);
				if (tech <= 100)
					owner.updateTotalTech();
				else
					tech = 100;
				owner.updateTech((float)tech * (techSpeed / ((float)owner.getTotalTech() / (float)owner.ownedStars.size()))); // NEEDS RETHINKING
				techTimer = 0;
			}
			if (popTimer >= timeBeforePop)
			{
				++pop;
				if (pop > 100)
					pop = 100;
				if (this == selected)
					StateLevel.updateStarHUD(this);
				popTimer = 0;
				techSpeed += .03f + (pop / 100) * popSpeed;
			}
		}
		
		g.setAntiAlias(true);
		g.setLineWidth(2 * Camera.zoom);
		g.setColor(neutral);
		for (Star star : connections)
			g.drawLine(position.x, position.y, star.position.x, star.position.y);
		if (withinCamera())
		{
			g.drawImage(Star.graphic, circle.getCenterX() - radius, circle.getCenterY() - radius);
			g.setColor(teamColour);
			g.draw(circle);
			g.setColor(Color.green.brighter(.75f));
			g.setFont(Game.littleFont);
			g.setAntiAlias(false);
			g.drawString(name, (int)(position.x - Game.littleFont.getWidth(name) / 2), (int)(position.y + radius + 3));
			
			if (mouseIsOver())
			{
				String info = pop + " " + tech;
				g.drawString(info, (int)(position.x - Game.littleFont.getWidth(info) / 2), (int)(position.y + radius + 3 + Game.littleFont.getHeight()));
			}
		}
		g.setAntiAlias(false);
		g.setLineWidth(1);
	}
	
	public void mouseButtonPressed(int button, int x, int y)
	{
		if (mouseIsOver())
		{
			if (button == 1)
			{
				if (Game.input.isKeyDown(Input.KEY_LSHIFT))
				{
					if (Fleet.selected != null)
						Fleet.selected.addToPath(this);
				}
				else
					moveCommand();
			}
			else if (button == 0)
				selectCommand();
		}
		else
		{
			if (this == selected)
			{
				selected = null;
				StateLevel.updateStarHUD(null);
				if (owner == PlayerManager.player)
					StateLevel.updateTechGUI();
			}
		}
		mapObj.mouseButtonPressed(button, x, y);
	}
	
	public float getSystemDefence()
	{
		return Math.round(pop * (owner == null ? 0 : owner.getSystemDefence()) * 1000) / 1000; // owner could be null
	}
	
	public void selectCommand()
	{
		if (owner == PlayerManager.player)
			StateLevel.updateTechGUI();
		selected = this;
		StateLevel.updateStarHUD(this);
		SoundManager.playSound(StateLevel.systemSelect);
	}
	
	public void moveCommand()
	{
		if (Fleet.selected != null)
		{
			if (Fleet.selected.getOwner() == PlayerManager.player)
			{
				Fleet.selected.moveTo(this);
				SoundManager.playSound(StateLevel.fleetMove);
			}
		}
	}
	
	public boolean mouseIsOver()
	{
		float x = TCursor.getX() / Camera.zoom + Camera.position.x;
		float y = TCursor.getY() / Camera.zoom + Camera.position.y;
		return x > position.x - offset && x <= position.x - offset + size.width && y > position.y - offset && y <= position.y - offset + size.height;
	}
}
