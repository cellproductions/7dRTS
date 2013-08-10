package com.tl.rts.src;

import java.io.FileNotFoundException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.state.StateBasedGame;

@SuppressWarnings("deprecation")
public class Game extends StateBasedGame
{
	public static final String gametitle = "System Command";
	public static final String version = "1.1";
	public static final int screenWidth = 800;
	public static final int screenHeight = 600;
	public static TrueTypeFont guiFont;
	public static TrueTypeFont littleFont;
	
	public static final byte sLoading = 1;
	public static final byte sMenu = 2;
	public static final byte sLevelLoad = 3;
	public static final byte sLevel = 4;
	
	public static AppGameContainer game;
	public static Input input;

	public Game(String name)
	{
		super(name);
		addState(new StateLoading(sLoading));
		addState(new StateMenu(sMenu));
		addState(new StateLevelLoad(sLevelLoad));
		addState(new StateLevel(sLevel));
		enterState(sLoading);
	}

	public void initStatesList(GameContainer container) throws SlickException
	{
		
	}
	
	public static void main(String []args)
	{
		Logger.init();
		try
		{
			Config.readConfig();
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		Renderer.setRenderer(Renderer.VERTEX_ARRAY_RENDERER); // experimental
		try
		{
			game = new AppGameContainer(new Game(gametitle));
			game.setDisplayMode(screenWidth, screenHeight, Settings.fullscreen);
			if (game.isVSyncRequested())
				game.setVSync(true);
			else
				game.setTargetFrameRate(60);
			game.setShowFPS(false);
			game.setVerbose(false);
			game.setAlwaysRender(true);
			game.setUpdateOnlyWhenVisible(false);
			game.setClearEachFrame(false);
			game.start();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
		Logger.deinit();
	}
}
