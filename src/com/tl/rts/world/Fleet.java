package com.tl.rts.world;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Circle;

import com.tl.rts.src.Game;
import com.tl.rts.src.SoundManager;
import com.tl.rts.src.StateLevel;

import tl.Util.TPoint;
import tl.Util.TSize;

public class Fleet extends Entity
{
	public static Fleet selected;
	public static Circle selectedCircle;
	public static Image graphic;
	private static final int radius = 20;
	private int fleetCount;
	private Circle circle;
	private String info;
	private MapObjectFleet mapObj;
	
	LinkedList<Star> path;
	private Star dest;
	private boolean moving;
	private boolean startMoving;
	private TPoint distance;
	private double startDist;
	private TPoint startPoint;
	private double angle;
	
	public Fleet()
	{
		super();
		setSize(new TSize(radius, radius));
		circle = new Circle(0, 0, radius);
		offset = radius;
		fleetCount = 1;
		info = "" + fleetCount;
		distance = new TPoint();
		startPoint = new TPoint();
		angle = 0;
		startDist = 0;
	}
	
	public Fleet(Fleet fleet)
	{
		fleetCount = fleet.fleetCount;
		owner = fleet.owner;
	}
	
	public Fleet(TPoint point, Player owner, int count)
	{
		super(point, new TSize(radius * 2, radius * 2));
		if (selected == null)
		{
			selected = this;
			selectedCircle = new Circle((int)position.x, (int)position.y, radius - 2);
		}
		setOwner(owner);
		circle = new Circle((int)point.x, (int)point.y, radius);
		offset = radius;
		fleetCount = count;
		info = "" + fleetCount;
		distance = new TPoint();
		startPoint = new TPoint(point);
		angle = 0;
		startDist = 0;
		path = new LinkedList<>();
		mapObj = new MapObjectFleet(this, owner);
		FleetManager.addFleet(this);
	}
	
	public void setPosition(TPoint point)
	{
		super.setPosition(point);
		circle.setLocation(point.x, point.y);
		startPoint.set(point);
	}
	
	public void kill()
	{
		mapObj.kill();
	}
	
	public void addFleet(Fleet fleet)
	{
		fleetCount += fleet.fleetCount;
		fleet.mapObj.kill();
		owner.removeFleet(fleet);
		info = "" + fleetCount;
	}
	
	public void removeFleet(int count, boolean move) // if move, split the fleet into 2 for moving
	{
		fleetCount = count; // make sure that count is how many ships are left after being split (if they are being split)
		info = "" + fleetCount;
		if (this == selected)
			StateLevel.updateFleetHUD(this);
		if (move)
			owner.addFleet(new Fleet(position, owner, count));
	}
	
	public void resolve(int count)
	{
		if (count <= 0)
		{
			Star where = PathFinder.getNearest(position);
			mapObj.kill();
			fleetCount = 0;
			if (this == Fleet.selected)
			{
				Fleet.selected = null;
				StateLevel.updateFleetHUD(null);
			}
			if (owner == where.getOwner()) // if this is the defending fleet
				where.removeFleet(true);
			else
				owner.removeFleet(this);
			if (owner == PlayerManager.player)
				StateLevel.lMessage.setText("A fleet was lost over " + where.getName());
			// update message system that this fleet was lost on where.getName()
		}
		else
			removeFleet(count, false); // count = how many ships are left after the fight
	}
	
	public int getFleetCount()
	{
		return fleetCount;
	}
	
	public void setOwner(Player player)
	{
		super.setOwner(player);
		player.addFleet(this);
	}
	
	public void move(int delta)
	{
		if (startMoving)
		{
			if (!moving)
				getNextPoint();
			else
			{
				float elapsed = ((float)delta / 100f);
				position = position.add(distance.x * owner.getSpeed() * elapsed, distance.y * owner.getSpeed() * elapsed);
				circle.setCenterX(circle.getCenterX() + (distance.x * owner.getSpeed() * elapsed));
				circle.setCenterY(circle.getCenterY() + (distance.y * owner.getSpeed() * elapsed));
				if (this == selected)
				{
					selectedCircle.setCenterX(position.x + (distance.x * owner.getSpeed() * elapsed));
					selectedCircle.setCenterY(position.y + (distance.y * owner.getSpeed() * elapsed));
				}
				//if (owner == PlayerManager.player)
					//System.out.println(((startDist - TPoint.distance(startPoint, position)) / owner.getSpeed())); // a kind of timer
				if (TPoint.distance(startPoint, position) >= startDist)
				{
					Star reached = PathFinder.getNearest(position);
					//System.out.println("reached " + reached.getName());
					position.set(reached.getPosition());
					circle.setCenterX(position.x);
					circle.setCenterY(position.y);
					if (this == selected)
					{
						selectedCircle.setCenterX(position.x);
						selectedCircle.setCenterY(position.y);
					}
					reached.addFleet(this);
					moving = false;
				}
			}
		}
	}
	
	public void addToPath(Star destination)
	{
		path.addAll(PathFinder.createPath(this, destination));
		mapObj.setPath(path);
	}
	
	private void getNextPoint()
	{
		if (!path.isEmpty())
		{
			moving = true;
			Star over = isOverStar();
			if (over != null)
				over.removeFleet(false);
			dest = path.pop();
			startPoint.set(position);
			TPoint destPoint = dest.getPosition();
			startDist = TPoint.distance(position, destPoint);
			distance = TPoint.normalizeDestination(position, destPoint);
			angle = TPoint.angle(position, destPoint) + 90;
		}
		else
			stop();
	}
	
	public void moveTo(Star destination)
	{
		startMoving = true;
		if (moving)
			path.clear();
		else
		{
			Star over = isOverStar();
			if (over != null)
				over.removeFleet(false);
			else
				path.clear();
		}
		addToPath(destination);
	}
	
	private void stop()
	{
		startMoving = false;
		moving = false;
		angle = 0;
		mapObj.clearPath();
	}
	
	public Star isOverStar()
	{
		Star nearest = PathFinder.getNearest(position);
		if (nearest != null)
			if (nearest.getPosition().compare(position) == 0)
				return nearest;
		return null;
	}
	
	public boolean isMoving()
	{
		return moving;
	}
	
	public boolean isPathing()
	{
		return startMoving;
	}
	
	public Star getCurrentDestination()
	{
		return dest;
	}
	
	@SuppressWarnings("deprecation")
	synchronized public void draw(Graphics g, int delta)
	{
		move(delta);
		
		if (withinCamera())
		{
			graphic.setRotation((float)angle);
			g.drawImage(graphic, position.x - graphic.getWidth() / 2, position.y - graphic.getHeight() / 2);
			graphic.setRotation((float)-angle); // need to correct it because 'graphic' is shared across every fleet in the game
			g.setColor(teamColour);
			g.setAntiAlias(true);
			g.setLineWidth(2);
			g.draw(circle);
			if (this == selected)
			{
				g.setColor(Color.orange);
				g.draw(selectedCircle);
			}
			g.setAntiAlias(false);
			g.setLineWidth(1);
			g.setColor(Color.green.brighter(.75f));
			g.drawString(info, position.x  - Game.littleFont.getWidth(info) / 2, position.y - radius - 3 - Game.littleFont.getHeight(info));
		}
		/*
		if (GameSettings.getDrawMovement() && this == selected)	BROKEN
		{
			g.setColor(Color.red);
			g.setLineWidth(3);
			Star prev = PathFinder.getNearest(position);
			for (Star star : path)
			{
				g.drawLine(prev.getPosition().x, prev.getPosition().y, star.getPosition().x, star.getPosition().y);
				prev = star;
			}
			g.setLineWidth(1);
		}*/
	}
	
	public void mouseButtonPressed(int button, int x, int y)
	{
		if (mouseIsOver())
		{
			if (button == 0)
			{
				if (owner == PlayerManager.player)
				{
					selected = this;
					selectedCircle = new Circle((int)position.x, (int)position.y, radius - 2);
					StateLevel.updateTechGUI();
					SoundManager.playSound(StateLevel.fleetSelect);
				}
				StateLevel.updateFleetHUD(this);
			}
		}
		else
		{
			if (button == 0)
			{
				if (this == selected && !StateLevel.cMinimapDisplay.mouseIsOver())
				{
					selected = null;
					StateLevel.updateFleetHUD(null);
				}
			}
		}
	}

	public float getFleetDamage()
	{
		return Math.round(fleetCount * owner.getAttack() * 1000) / 1000;
	}
	
	public long getFleetDefence()
	{
		return Math.round(fleetCount * owner.getDefence() * 1000) / 1000;
	}
}
