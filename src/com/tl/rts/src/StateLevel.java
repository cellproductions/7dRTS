package com.tl.rts.src;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import com.tl.rts.world.AI;
import com.tl.rts.world.Fleet;
import com.tl.rts.world.FleetManager;
import com.tl.rts.world.MapObjectDrawer;
import com.tl.rts.world.PlayerManager;
import com.tl.rts.world.Star;
import com.tl.rts.world.StarManager;
import com.tl.rts.world.WorldGenerator;
import com.tl.rts.src.Camera;

import tl.GUI.TEAlignment;
import tl.GUI.TLabel;
import tl.GUI.TButton;
import tl.GUI.TContainer;
import tl.GUI.TGUIComponent;
import tl.Util.TCursor;
import tl.Util.TPoint;
import tl.Util.TSize;

public class StateLevel extends BasicGameState
{
	byte ID;
	
	public static Music theme1;
	public static Music theme2;
	public static Sound upgrade;
	public static Sound failed;
	public static Sound mOver;
	public static Sound fleetSelect;
	public static Sound fleetMove;
	public static Sound systemSelect;
	public static Image background;
	
	public static TGUIComponent gMinimap;
	public static TContainer<Object> cMinimapDisplay;
	public static TGUIComponent gHUDFleet;
	public static void updateFleetHUD(Fleet fleet)
	{
		boolean isNull = fleet == null;
		((TLabel)gHUDFleet.child(0)).setText("Owner: " + (isNull ? "" : fleet.getOwner().getName()));
		((TLabel)gHUDFleet.child(1)).setText("Fleet attack: " + (isNull ? "" : Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(fleet.getFleetDamage()))));
		((TLabel)gHUDFleet.child(2)).setText("Fleet size: " + (isNull ? "" : fleet.getFleetCount()));
		((TLabel)gHUDFleet.child(3)).setText("Fleet defence: " + (isNull ? "" : Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(fleet.getFleetDefence()))));
		if (!isNull)
		{
			gHUDStar.setVisible(false);
			gHUDFleet.setVisible(true);
		}
		else
			gHUDFleet.setVisible(false);
	}
	public static TGUIComponent gHUDStar;
	public static void updateStarHUD(Star star)
	{
		boolean isNull = star == null;
		((TLabel)gHUDStar.child(0)).setText((isNull ? "" : star.getName()));
		((TLabel)gHUDStar.child(2)).setText("Owner: " + (isNull ? "" : star.getOwner() == null ? "N/A" : star.getOwner().getName()));
		((TLabel)gHUDStar.child(3)).setText("Population: " + (isNull ? "" : star.getPopulation()));
		((TLabel)gHUDStar.child(5)).setText("System defence: " + (isNull ? "" : Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(star.getSystemDefence()))));
		((TLabel)gHUDStar.child(6)).setText("Technology: " + (isNull ? "" : star.getTechnology()));
		if (!isNull)
		{
			gHUDFleet.setVisible(false);
			gHUDStar.setVisible(true);
		}
		else
			gHUDStar.setVisible(false);
	}
	
	public static TButton bMenu;
	public static TGUIComponent gMenu;
	public static TButton bTech;
	public static TGUIComponent gTech;
	public static void updateTechGUI()
	{
		if (gTech != null)
		{
			((TLabel)gTech.child(2)).setText("Total technology: " + PlayerManager.player.getTotalTech(), TEAlignment.CENTRE_LEFT);
			((TLabel)gTech.child(3)).setText("Technology points: " + PlayerManager.player.getTechPoints(), TEAlignment.CENTRE_LEFT);
			((TButton)gTech.child(5)).setText("Fleet Speed: " + Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(PlayerManager.player.getSpeed())));
			((TButton)gTech.child(6)).setText("Fleet Attack: " + Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(PlayerManager.player.getAttack())));
			((TButton)gTech.child(7)).setText("Fleet Defence: " + Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(PlayerManager.player.getDefence())));
			((TButton)gTech.child(8)).setText("System Defence: " + Float.parseFloat(new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)).format(PlayerManager.player.getSystemDefence())));
			((TButton)gTech.child(9)).setText("Place fleet on " + (Star.selected == null ? "N/A" : (Star.selected.getOwner() == PlayerManager.player ? Star.selected.getName() : "N/A")));
			Fleet check = Fleet.selected == null ? null : (Fleet.selected.getOwner() == PlayerManager.player ? Fleet.selected : null);
			Star over = check != null ? check.isOverStar() : null;
			((TButton)gTech.child(10)).setText("Colonize " + (check != null && over != null ? over.getName() : "N/A"));
		}
	}
	public static TGUIComponent gPlayers;
	public static TLabel lMessage;
	private static int timer = 0;
	private static int counter = 0;
	private static final int maxTime = 100;
	private static int alphaTimer = 0;
	private static int alphaCounter = 0;
	private static final int maxFade = 75;
	private static boolean fading = false;
	public static TGUIComponent gOver;
	public static TGUIComponent gGameSettings;
	
	public static boolean levelLoaded;
	
	public StateLevel(byte id)
	{
		ID = id;
	}

	public void init(GameContainer container, StateBasedGame game) throws SlickException
	{
		
	}
	
	public void enter(GameContainer container, StateBasedGame game)
	{
		Camera.stop();
		Camera.setStateBounds(new TPoint(0, 0), new TPoint(WorldGenerator.getSize().width, WorldGenerator.getSize().height));
		Camera.setControlSpeed(1.75f);
		GameSettings.setScrollSpeed(1.75f);
		TSize size = WorldGenerator.getSize();
		Camera.setPosition(size.width / 2 - Camera.size.width / 2, size.height / 2 - Camera.size.height / 2);
		TPoint pos = PlayerManager.player.getStars().get(0).getPosition();
		Camera.move(pos.x - (Camera.size.width / 2), pos.y - (Camera.size.height / 2), 1000f);
		// start playing music
		
		cWidth = (float)Camera.size.width / (float)WorldGenerator.getSize().width * (float)200;
		cHeight = (float)Camera.size.height / (float)WorldGenerator.getSize().height * (float)200;
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		
		levelLoaded = true;
		updateTechGUI();
		AI.initAI();
		MusicManager.play(new Random().nextBoolean() ? theme1 : theme2);
	}

	private float cWidth; // width of the yellow rectangle representing the camera on the minimap
	private float cHeight;
	private int delta;
	
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException
	{
		g.clear();
		float x = Camera.position.x;
		float y = Camera.position.y;
		float xdist = ((WorldGenerator.getSize().width / 2) - x) * 100 / WorldGenerator.getSize().width;
		float ydist = ((WorldGenerator.getSize().height / 2) - y) * 100 / WorldGenerator.getSize().height;
		g.translate(xdist, ydist);
		g.drawImage(background, -100, -100);
		g.translate(-xdist, -ydist);
		
		float cx = Game.screenWidth / 2 + x;
		float cy = Game.screenHeight / 2 + y;
		
		//g.translate(-x, -y);
		//g.translate(cx, cy);
		//g.scale(Camera.zoom, Camera.zoom);
		//g.translate(-cx, -cy);
		g.translate(-Camera.position.x, -Camera.position.y);
		StarManager.draw(g, delta);
		FleetManager.draw(g, delta);
		//g.translate((int)Camera.position.x, (int)Camera.position.y);
		
		//g.scale(factor < 1 ? factor + 1 : factor, factor < 1 ? factor + 1 : factor);
		g.resetTransform();
		drawGUIs(g);
		g.setColor(Color.yellow);
		float width = cWidth / Camera.zoom; // cWidth to scale
		float height = cHeight / Camera.zoom; // cHeight to scale
		float posx = (x / WorldGenerator.getSize().width * cMinimapDisplay.itemWidth() + cMinimapDisplay.getScreenX()); //  - width / 2
		float posy = (y / WorldGenerator.getSize().height * cMinimapDisplay.itemHeight() + cMinimapDisplay.getScreenY()); //  - height / 2
		if (posx < cMinimapDisplay.getScreenX())
			posx = cMinimapDisplay.getScreenX();
		else if (posx + width / 2f >= cMinimapDisplay.getScreenX() + cMinimapDisplay.itemWidth())
			posx = cMinimapDisplay.getScreenX() + cMinimapDisplay.itemWidth() - width - 1;
		if (posy < cMinimapDisplay.getScreenY())
			posy = cMinimapDisplay.getScreenY();
		else if (posy + height / 2f >= cMinimapDisplay.getScreenY() + cMinimapDisplay.itemHeight())
			posy = cMinimapDisplay.getScreenY() + cMinimapDisplay.itemHeight() - height - 1;
		
		g.drawRect(posx - width / 2f, posy - height / 2f, width, height);
		MapObjectDrawer.draw(g);
	}
	
	private boolean justPlayed = false;

	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException
	{
		if (!theme1.playing() && !justPlayed)
		{
			MusicManager.play(theme2);
			justPlayed = true;
		}
		if (!theme2.playing() && justPlayed)
		{
			justPlayed = false;
			MusicManager.play(theme1);
		}
		
		this.delta = delta;
		Camera.update(delta);
		
		PlayerManager.update(delta);
		
		if (!lMessage.getText().contentEquals(""))
		{
			if (!fading)
			{
				++timer;
				if (timer >= 60)
				{
					++counter;
					if (counter >= maxTime)
					{
						counter = 0;
						timer = 0;
						fading = true;
						alphaTimer = 0;
						alphaCounter = 0;
					}
				}
			}
			else
			{
				++alphaTimer;
				if (alphaTimer >= 60)
				{
					++alphaCounter;
					lMessage.setTransparency(.75f - (float)alphaCounter / 100f);
					lMessage.setPosition(lMessage.getX(), cMinimapDisplay.getScreenY() - 25 - 3 - (alphaCounter / 2));
					if (alphaCounter >= maxFade)
					{
						lMessage.setText("");
						fading = false;
						lMessage.setPosition(lMessage.getX(), cMinimapDisplay.getScreenY() - 25 - 3);
						lMessage.setTransparency(.75f);
					}
				}
			}
		}
		
		if (Game.input.isMouseButtonDown(0))
		{
			if (mouseIsOverMinimap())
			{
				int width = WorldGenerator.getSize().width;
				int height = WorldGenerator.getSize().height;
				Camera.setPosition((TCursor.getX() - cMinimapDisplay.getScreenX() - ((float)Camera.size.width / (float)width * (float)cMinimapDisplay.itemWidth() / 2)) * width / cMinimapDisplay.itemWidth(), 
						(TCursor.getY() - cMinimapDisplay.getScreenY() - ((float)Camera.size.height / (float)height * (float)cMinimapDisplay.itemHeight() / 2)) * height / cMinimapDisplay.itemHeight());
			}
		}
	}
	
	public void leave(GameContainer container, StateBasedGame game)
	{
		gMinimap = null;
		cMinimapDisplay = null;
		gHUDFleet = null;
		gHUDStar = null;
		bMenu = null;
		gMenu = null;
		bTech = null;
		gTech = null;
		gPlayers = null;
		lMessage = null;
		gOver = null;
		gGameSettings = null;
		Camera.zoom = 1f;

		MusicManager.stop();
		FleetManager.reset();
		PlayerManager.reset();
		StarManager.reset();
		WorldGenerator.reset();
	}

	public int getID()
	{
		return ID;
	}
	
	protected void drawGUIs(Graphics g)
	{
		gMinimap.update(g);
		gHUDFleet.update(g);
		gHUDStar.update(g);
		bMenu.update(g);
		gMenu.update(g);
		bTech.update(g);
		gTech.update(g);
		if (gPlayers != null)
			gPlayers.update(g);
		lMessage.update(g);
		gOver.update(g);
		gGameSettings.update(g);
	}
	
	public void mousePressed(int button, int x, int y)
	{
		gMinimap.mousePressed(button, x, y);
		gHUDFleet.mousePressed(button, x, y);
		gHUDStar.mousePressed(button, x, y);
		bMenu.mousePressed(button, x, y);
		gMenu.mousePressed(button, x, y);
		bTech.mousePressed(button, x, y);
		gTech.mousePressed(button, x, y);
		gPlayers.mousePressed(button, x, y);
		gOver.mousePressed(button, x, y);
		gGameSettings.mousePressed(button, x, y);
	
		if (!bMenu.mouseIsOver() && !bTech.mouseIsOver()) // have to manually do it? wtf?
		{
			if (!(x > gMenu.getScreenX() && x <= gMenu.getScreenX() + gMenu.width() && y > gMenu.getScreenY() && y <= gMenu.getScreenY() + gMenu.height() && gMenu.getVisibility()))
			{
				if (!(x > gTech.getScreenX() && x <= gTech.getScreenX() + gTech.width() && y > gTech.getScreenY() && y <= gTech.getScreenY() + gTech.height() && gTech.getVisibility()))
				{
					if (!(x > gPlayers.getScreenX() && x <= gPlayers.getScreenX() + gPlayers.width() && y > gPlayers.getScreenY() && y <= gPlayers.getScreenY() + gPlayers.height() && gPlayers.getVisibility()))
					{
						if (!(x > gGameSettings.getScreenX() && x <= gGameSettings.getScreenX() + gGameSettings.width() && y > gGameSettings.getScreenY() && y <= gGameSettings.getScreenY() + gGameSettings.height() && gGameSettings.getVisibility()))
						{
							StarManager.mouseButtonPressed(button, x, y);
							FleetManager.mouseButtonPressed(button, x, y);
						}
					}
				}
			}
		}
	}
	
	public void mouseReleased(int button, int x, int y)
	{
		gMinimap.mouseReleased(button, x, y);
		gHUDFleet.mouseReleased(button, x, y);
		gHUDStar.mouseReleased(button, x, y);
		bMenu.mouseReleased(button, x, y);
		gMenu.mouseReleased(button, x, y);
		bTech.mouseReleased(button, x, y);
		gTech.mouseReleased(button, x, y);
		gPlayers.mouseReleased(button, x, y);
		gOver.mouseReleased(button, x, y);
		gGameSettings.mouseReleased(button, x, y);
	}
	
	public void mouseDragged(int oldx, int oldy, int newx, int newy)
	{
		if (Game.input.isMouseButtonDown(1))
			Camera.setPosition(Camera.position.x - (newx - oldx), Camera.position.y - (newy - oldy));
	}
	
	public void mouseWheelMoved(int value)
	{
		float cx = Game.screenWidth / 2 + Camera.position.x;
		float cy = Game.screenHeight / 2 + Camera.position.y;
		
		if (value > 0)
			Camera.zoom += .05f;
		else
			Camera.zoom -= .05f;
		
		if (Camera.zoom > 1f)
			Camera.zoom = 1f;
		else if (Camera.zoom < .1f)
			Camera.zoom = .1f;
		//Camera.setPosition(cx - Game.screenWidth / 2, cy - Game.screenHeight / 2);
		Camera.resize((int)(Game.screenWidth / Camera.zoom), (int)(Game.screenHeight / Camera.zoom));
		Camera.setControlSpeed(GameSettings.getScrollSpeed() / Camera.zoom);
	}
	
	public void keyPressed(int key, char c)
	{
		if (key == Input.KEY_TAB)
		{
			gPlayers.setVisible(!gPlayers.getVisibility());
			gMenu.setVisible(false);
			gTech.setVisible(false);
		}
		else if (key == Input.KEY_H || key == Input.KEY_HOME)
		{
			TPoint start = PlayerManager.player.startingStar.getPosition();
			Camera.setPosition(start.x - Camera.size.width / 2, start.y - Camera.size.height / 2);
		}
	}
	
	public static boolean mouseIsOverMinimap()
	{
		float x = TCursor.getX();
		float y = TCursor.getY();
		return x > cMinimapDisplay.getScreenX() && x <= cMinimapDisplay.getScreenX() + cMinimapDisplay.width() && y > cMinimapDisplay.getScreenY() && y <= cMinimapDisplay.getScreenY() + cMinimapDisplay.height();
	}
}
