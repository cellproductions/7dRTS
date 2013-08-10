package com.tl.rts.src;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.SharedDrawable;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import com.tl.rts.world.PlayerManager;

import tl.GUI.TGUIManager;
import tl.Util.TCursor;

@SuppressWarnings("deprecation")
public class StateLoading extends BasicGameState
{
	private int ID;
	private final int lheight = 20;
	private final int lwidth = (int)(Game.screenWidth * .3f);
	private int currWidth = 0;
	private int currWidthMax = 0;
	private final float lx = Game.screenWidth / 2 - lwidth / 2;
	private final float ly = Game.screenHeight / 2 - lheight / 2;
	private final String text = "'" + Game.gametitle + "'(c) by Callum Nichols, 2013.";
	private final String credit = "'C&C Red Alert (INET)'(c) font by N3tRunn3r, 2007.";
	private static int maxResCount = 2;
	private static int percent;
	private static int count;
	private static final LinkedList<Resource> imageQueue = new LinkedList<>();
	
	public StateLoading(byte id)
	{
		ID = id;
	}

	public void init(final GameContainer container, StateBasedGame game) throws SlickException
	{
		Game.input = container.getInput();
		TCursor.init(Game.input);
		
		// seperated into two different try scopes because one might have a different font file some time
		try 
		{
			Font gFont;
			gFont = Font.createFont(Font.TRUETYPE_FONT, new File("./res/font.ttf"));
			gFont = gFont.deriveFont(28f); // set font size
			Game.guiFont = new TrueTypeFont(gFont, false);
		} 
		catch (FontFormatException | IOException e1) 
		{
			e1.printStackTrace();
		}
		try 
		{
			Font lFont;
			lFont = Font.createFont(Font.TRUETYPE_FONT, new File("./res/font.ttf"));
			lFont = lFont.deriveFont(22f); // set font size
			Game.littleFont = new TrueTypeFont(lFont, false);
		} 
		catch (FontFormatException | IOException e1) 
		{
			e1.printStackTrace();
		}
		TGUIManager.init(Game.input, Game.screenWidth, Game.screenHeight, Game.guiFont);
		MusicManager.setVolume(Settings.musicVolume);
		PlayerManager.player = new com.tl.rts.world.Player(Settings.name);
		
		try
		{
			imageQueue.add(new Resource("./res/images/cursor.png", new SharedDrawable(Display.getDrawable()))
			{
				public void execute() throws SlickException
				{
					TCursor.setImage(new Image(toLoad));
				}
			});
			imageQueue.add(new Resource("./res/images/title.png", new SharedDrawable(Display.getDrawable()))
			{
				public void execute() throws SlickException
				{
					StateMenu.title = new Image(toLoad);
					StateMenu.title.setFilter(Image.FILTER_NEAREST);
					StateMenu.title = StateMenu.title.getScaledCopy((int)(Game.screenWidth * .5f), (int)(Game.screenHeight * .25f));
				}
			});
			imageQueue.add(new Resource("./res/images/background.png", new SharedDrawable(Display.getDrawable()))
			{
				public void execute() throws SlickException
				{
					StateMenu.background = new Image(toLoad);
					StateMenu.background.setFilter(Image.FILTER_NEAREST);
					if (StateMenu.background.getWidth() != Game.screenWidth + 200 && StateMenu.background.getHeight() != Game.screenHeight + 200)
						StateMenu.background = StateMenu.background.getScaledCopy(Game.screenWidth + 200, Game.screenHeight + 200);
				}
			});
			imageQueue.add(new Resource("./res/audio/", new SharedDrawable(Display.getDrawable()))
			{
				public void execute() throws SlickException
				{
					StateMenu.theme = new Music(toLoad + "track1.ogg");
					MusicManager.addTrack(StateMenu.theme);
					//StateMenu.mOver = new Sound(toLoad + "mouseover.ogg");
				}
			});
			
			maxResCount = imageQueue.size();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
	}

	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException
	{
		g.clear();
		g.setColor(Color.black);
		g.fillRect(0, 0, Game.screenWidth, Game.screenHeight);
		g.setColor(Color.blue.darker());
		g.fillRect(lx, ly, currWidth, lheight);
		g.setColor(Color.blue.darker(.75f));
		g.drawRect(lx, ly, lwidth, lheight);
		g.setColor(Color.green.brighter(.75f));
		g.setFont(Game.littleFont);
		g.drawString(text, Game.screenWidth / 2 - Game.littleFont.getWidth(text) / 2, Game.screenHeight - 65);
		g.drawString(credit, Game.screenWidth / 2 - Game.littleFont.getWidth(text) / 2, Game.screenHeight - 40);
		String l = "Loading: " + percent + "%";
		g.drawString(l, Game.screenWidth / 2 - Game.littleFont.getWidth(l) / 2, ly - 20);
	}

	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException
	{
		if (count < maxResCount)
		{
			if (!imageQueue.isEmpty())
			{
				Resource res = imageQueue.getFirst();
				if (res.isFinished())
					imageQueue.pop();
				else
					if (!res.isRunning())
						res.begin();
			}
			else
				count = maxResCount;
		}
		currWidthMax = percent / 100 * lwidth;
		if (currWidth < currWidthMax)
			currWidth += currWidthMax *.25;
		if (currWidth >= lwidth)
			game.enterState(Game.sMenu, new FadeOutTransition(Color.black, 500), new FadeInTransition(Color.black, 500));
		//System.out.println(percent + " " + currWidth + "/" + lwidth);
	}

	public int getID()
	{
		return ID;
	}
	
	synchronized private void updateCounter()
	{
		count++;
		percent = count * 100 / maxResCount;
	}

	private class Resource extends Thread
	{
		String toLoad;
		boolean finished;
		boolean running;
		SharedDrawable sharedDrawable;
		public Resource(String path, SharedDrawable drawable)
		{
			toLoad = path;
			sharedDrawable = drawable;
		}
		
		public void begin()
		{
			running = true;
			this.start();
		}
		
		public void execute() throws SlickException
		{
		}
		
		public void run()
		{
			try
			{
				sharedDrawable.makeCurrent();
				execute();
				updateCounter();
				finished = true;
				sharedDrawable.releaseContext();
				sharedDrawable.destroy();
			}
			catch (LWJGLException | SlickException e)
			{
				System.out.println("At: " + toLoad);
				e.printStackTrace();
			}
		}
		
		public boolean isRunning()
		{
			return running;
		}
		
		public boolean isFinished()
		{
			return finished;
		}
	}
}
