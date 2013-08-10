package com.tl.rts.src;

import java.io.IOException;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import com.tl.rts.world.PlayerManager;
import com.tl.rts.world.PlayerManager.Difficulty;
import com.tl.rts.world.WorldGenerator;

import tl.GUI.TButton;
import tl.GUI.TButtonToggle;
import tl.GUI.TContainer;
import tl.GUI.TEAlignment;
import tl.GUI.TGUIClickedEvent;
import tl.GUI.TGUIComponent;
import tl.GUI.TGUIComponent.ComponentType;
import tl.GUI.TGUIMouseOverEvent;
import tl.GUI.TGUIObject;
import tl.GUI.TGUISelectionEvent;
import tl.GUI.TGUITextEvent;
import tl.GUI.TGUIVBoxLayout;
import tl.GUI.TGUIValueEvent;
import tl.GUI.TLabel;
import tl.GUI.TSlider;
import tl.GUI.TTextBox;
import tl.Util.TCursor;
import tl.Util.TPoint;
import tl.Util.TSize;

public class StateMenu extends BasicGameState implements MusicListener
{
	public byte ID;
	
	public static Image title;
	public static Image background;
	public static Music theme;
	public static Sound mOver;
	protected boolean roomLoad = true;
	
	public static TGUIComponent gMenu;
	public static TGUIComponent gOptions;
	public static TGUIComponent gNew;
	
	private Random rand = new Random();
	boolean style;
	
	public StateMenu(byte id)
	{
		ID = id;
	}

	public void init(final GameContainer container, final StateBasedGame game) throws SlickException
	{
		int width = (int)(Game.screenWidth * .25f);
		int height = (int)(Game.screenHeight * .45f);
		gMenu = new TGUIComponent(null, Game.screenWidth / 2 - width / 2, Game.screenHeight / 2 - height / 2 + 75, width, height);
		new TGUIVBoxLayout(gMenu, false)
		{{
			addComponent(new TButton(gMenu)
			{{
				setText("New Game");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						title.setAlpha(0f);
						gMenu.setVisible(false);
						gNew.setVisible(true);
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TButton(gMenu)
			{{
				setText("Load Game");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TButton(gMenu)
			{{
				setText("Options");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						gMenu.setVisible(false);
						gOptions.setVisible(true);
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TButton(gMenu)
			{{
				setText("Quit");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						Game.game.exit();
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			
			toggleStretching();
			organise();
		}};
		gMenu.setTransparency(0f);
		for (byte i = 0; i < gMenu.childCount(); ++i)
		{
			TButton button = (TButton)gMenu.child(i);
			button.setTransparency(.5f);
			button.background.r = Color.blue.darker().r;
			button.background.g = Color.blue.darker().g;
			button.background.b = Color.blue.darker().b;
			button.border_white.r = Color.blue.brighter().r;
			button.border_white.g = Color.blue.brighter().g;
			button.border_white.b = Color.blue.brighter().b;
			button.border_grey.r = Color.blue.darker(.75f).r;
			button.border_grey.g = Color.blue.darker(.75f).g;
			button.border_grey.b = Color.blue.darker(.75f).b;
			button.font_colour.r = Color.green.brighter(.75f).r;
			button.font_colour.g = Color.green.brighter(.75f).g;
			button.font_colour.b = Color.green.brighter(.75f).b;
		}
		
		gOptions = new TGUIComponent(null, Game.screenWidth / 2 - width, Game.screenHeight / 2 - height / 2 + 75, false, width * 2, height / 2, .25f);
		// back, music, sound, fullscreen, name
		new TGUIVBoxLayout(gOptions, false)
		{{
			addComponent(new TButton(gOptions)
			{{
				setText("Done");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						gOptions.setVisible(false);
						gMenu.setVisible(true);
						// write settings to config file
						try
						{
							Config.writeConfig();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TLabel(gOptions)
			{{
				setText("Music: ", TEAlignment.CENTRE_RIGHT);
			}});
			addComponent(new TLabel(gOptions)
			{{
				setText("Sound: ", TEAlignment.CENTRE_RIGHT);
			}});
			addComponent(new TLabel(gOptions)
			{{
				setText("Name: ", TEAlignment.CENTRE_RIGHT);
			}});
			toggleStretching();
			organise();
			for (byte i = 0; i < gOptions.childCount(); ++i)
			{
				TGUIComponent comp = gOptions.child(i);
				comp.setSize(100, comp.height());
			}
		}};
		new TGUIVBoxLayout(gOptions, false)
		{{
			setPosition(103, 0);
			addComponent(new TButtonToggle(gOptions)
			{{	
				addItem("Fullscreen: On");
				addItem("Fullscreen: Off");
				setIndex(Settings.fullscreen ? 1 : 0);
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						try
						{
							Settings.fullscreen = !Settings.fullscreen;
							container.setFullscreen(!container.isFullscreen());
						}
						catch (SlickException e)
						{
							e.printStackTrace();
						}
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TSlider(gOptions)
			{{	
				setRange(100, 0);
				setValue(Settings.musicVolume);
				
				onValueChange(new TGUIValueEvent()
				{
					public void execute(long arg0, TGUIComponent arg1)
					{
						((TLabel)gOptions.child(gOptions.childCount() - 3)).setText(arg0 + "%");
						Settings.musicVolume = (int)arg0;
						MusicManager.setVolume((int)arg0);
					}
				});
			}});
			addComponent(new TSlider(gOptions)
			{{
				setRange(100, 0);
				setValue(Settings.soundVolume);
				onValueChange(new TGUIValueEvent()
				{
					public void execute(long arg0, TGUIComponent arg1)
					{
						((TLabel)gOptions.child(gOptions.childCount() - 2)).setText(arg0 + "%");
						Settings.soundVolume = (int)arg0;
						SoundManager.setVolume((int)arg0);
					}
				});
			}});
			addComponent(new TTextBox(gOptions)
			{{
				setText(Settings.name);
				setCaretRight();
				
				onEnterPressed(new TGUITextEvent()
				{
					public void execute(String arg0, TGUIComponent arg1)
					{
						Settings.name = arg0;
					}
				});
				onTextChange(new TGUITextEvent()
				{
					public void execute(String arg0, TGUIComponent arg1)
					{
						Settings.name = arg0;
						PlayerManager.player.setName(arg0);
					}
				});
			}});
			
			toggleStretching();
			organise();
			for (byte i = 4; i < gOptions.childCount(); ++i)
			{
				TGUIComponent comp = gOptions.child(i);
				comp.setSize(180, comp.height());
			}
		}};
		new TGUIVBoxLayout(gOptions, false)
		{{
			setPosition(286, 0);
			addComponent(new TButton(gOptions)
			{{
				setText("Cancel");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						gOptions.setVisible(false);
						gMenu.setVisible(true);
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TLabel(gOptions)
			{{
				setText(Settings.musicVolume + "%", TEAlignment.CENTRE);
			}});
			addComponent(new TLabel(gOptions)
			{{
				setText(Settings.soundVolume + "%", TEAlignment.CENTRE);
			}});
			addComponent(new TButton(gOptions)
			{{
				setText("Change");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						Settings.name = ((TTextBox)gOptions.child(7)).getText();
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			
			toggleStretching();
			organise();
		}};
		gOptions.setColour(gOptions.background, Color.blue.darker());
		gOptions.setColour(gOptions.border, Color.blue.darker(.75f));
		for (byte i = 0; i < gOptions.childCount(); ++i)
		{
			TGUIComponent comp = gOptions.child(i);
			comp.setTransparency(.5f);
			comp.setColour(comp.background, Color.blue.darker());
			if (comp.getType() == ComponentType.button)
			{
				TButton button = (TButton)comp;
				button.setColour(button.background, Color.blue.darker());
				button.setColour(button.border_white, Color.blue.brighter());
				button.setColour(button.border_grey, Color.blue.darker(.75f));
				button.setColour(button.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.buttonToggle)
			{
				TButtonToggle button = (TButtonToggle)comp;
				button.setColour(button.background, Color.blue.darker());
				button.setColour(button.border_white, Color.blue.brighter());
				button.setColour(button.border_grey, Color.blue.darker(.75f));
				button.setColour(button.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.label)
			{
				TLabel label = (TLabel)comp;
				label.setColour(label.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.textBox)
			{
				TTextBox box = (TTextBox)comp;
				box.setColour(box.background, Color.blue.darker());
				box.setColour(box.border, Color.blue.brighter());
				box.setColour(box.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.slider)
			{
				TSlider box = (TSlider)comp;
				box.setColour(box.background, Color.blue.darker());
				box.setColour(box.border_white, Color.blue.brighter());
				box.setColour(box.border_grey, Color.blue.darker(.75f));
				box.setColour(box.slider_background, Color.blue.darker());
			}
		}
		
		width = (int)(Game.screenWidth * .67f);
		height = (int)(Game.screenHeight * .67f);
		gNew = new TGUIComponent(null, Game.screenWidth / 2 - width / 2, Game.screenHeight / 2 - height / 2, false, width, height, .25f);
		new TGUIVBoxLayout(new TGUIObject(TPoint.ZERO, new TSize(180, (int)(gNew.height() * .4))), false)
		{{
			addComponent(new TButton(gNew)
			{{
				setText("Back");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						gNew.setVisible(false);
						gMenu.setVisible(true);
						title.setAlpha(1f);
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TLabel(gNew)
			{{
				setText("Total stars:", TEAlignment.CENTRE_RIGHT);	
			}});
			addComponent(new TLabel(gNew)
			{{
				setText("Starting stars:", TEAlignment.CENTRE_RIGHT);	
			}});
			addComponent(new TLabel(gNew)
			{{
				setText("Starting ships:", TEAlignment.CENTRE_RIGHT);	
			}});
			
			toggleStretching();
			organise();
		}};
		new TGUIVBoxLayout(new TGUIObject(new TPoint(180 + 6, 0), new TSize(180, (int)(gNew.height() * .4))), false)
		{{
			setPosition(180 + 3, 0);
			
			addComponent(new TLabel(gNew)
			{{
				setText("Difficulty:", TEAlignment.CENTRE_RIGHT);	
			}});
			addComponent(new TSlider(gNew)
			{{
				setRange(20, 5);
				setValue(10);
				
				onValueChange(new TGUIValueEvent()
				{
					public void execute(long arg0, TGUIComponent arg1)
					{
						((TLabel)gNew.child(getID() + 6)).setText("" + arg0 * 10);
						WorldGenerator.setStarMax((int)arg0 * 10);
					}	
				});
			}});
			addComponent(new TSlider(gNew)
			{{
				setRange(5, 1);
				setValue(1);
				
				onValueChange(new TGUIValueEvent()
				{
					public void execute(long arg0, TGUIComponent arg1)
					{
						((TLabel)gNew.child(getID() + 6)).setText("" + arg0);
						WorldGenerator.setStartStars((byte)arg0);
					}	
				});
			}});
			addComponent(new TSlider(gNew)
			{{
				setRange(10, 0);
				setValue(3);
				
				onValueChange(new TGUIValueEvent()
				{
					public void execute(long arg0, TGUIComponent arg1)
					{
						((TLabel)gNew.child(getID() + 6)).setText("" + arg0);
						WorldGenerator.setStartShips((byte)arg0);
					}	
				});
			}});
			
			toggleStretching();
			organise();
			
			for (byte i = 4; i < gNew.childCount(); ++i)
			{
				TGUIComponent comp = gNew.child(i);
				comp.setSize(180, comp.height());
			}
		}};
		gNew.addComponent(new TLabel(gNew, 369/2 - 180/2, (int)(gNew.height() * .4f + 6), 180, 25, "Player colour:"));
		new TGUIVBoxLayout(new TGUIObject(new TPoint(0, 0), new TSize(369, (int)((gNew.height() - (gNew.height() * .4 + 28) - 6) * .6))), true)
		{{
			setPosition(0, gNew.height() * .4f + 6 + 28);
			toggleStretching();
			addComponent(new TContainer<Color>(gNew)
			{{
				setGapSize(5);
				final int size = 363 / (363 / (5 * 8 + 5));
				setImageSize(size, size);
				addItem(new Image(size, size), new Color(Color.blue));
				addItem(new Image(size, size), new Color(Color.red));
				addItem(new Image(size, size), new Color(Color.green));
				addItem(new Image(size, size), new Color(Color.yellow));
				//addItem(new Image(size, size), new Color(Color.gray));
				addItem(new Image(size, size), new Color(Color.magenta));
				addItem(new Image(size, size), new Color(Color.cyan));
				addItem(new Image(size, size), new Color(139, 69, 19, 255));
				addItem(new Image(size, size), new Color(171, 0, 255, 255));
				addItem(new Image(size, size), new Color(255, 145, 0, 255));
				
				for (byte i = 0; i < itemCount(); ++i)
				{
					Image image = getGraphic(i);
					Graphics g = image.getGraphics();
					g.setColor(getObject(i));
					g.fillRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
					g.flush();
				}
				
				onSelectionChange(new TGUISelectionEvent()
				{
					public void execute(int arg0, TGUIComponent arg1)
					{
						PlayerManager.player.setColour(getObject(arg0));
						PlayerManager.player.getColour().a = 1f;
					}
				});
			}});
		}};
		new TGUIVBoxLayout(new TGUIObject(TPoint.ZERO, new TSize(180, (int)(gNew.height() * .4))), false)
		{{
			setPosition(180 + 3 + 183, 0);
			
			addComponent(new TButtonToggle(gNew)
			{{
				addItem("Easy");
				addItem("Medium");
				addItem("Hard");
				
				onToggle(new TGUISelectionEvent()
				{
					public void execute(int arg0, TGUIComponent arg1)
					{
						switch (getIndex())
						{
							case 0:
								PlayerManager.difficulty = Difficulty.EASY;
								break;
							case 1:
								PlayerManager.difficulty = Difficulty.MEDIUM;
								break;
							case 2:
								PlayerManager.difficulty = Difficulty.HARD;
								break;
						}
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TLabel(gNew)
			{{
				setText("100");	
			}});
			addComponent(new TLabel(gNew)
			{{
				setText("1");	
			}});
			addComponent(new TLabel(gNew)
			{{
				setText("3");	
			}});
			
			toggleStretching();
			organise();
			
			for (byte i = 10; i < gNew.childCount(); ++i)
			{
				TGUIComponent comp = gNew.child(i);
				comp.setSize((int)(gNew.width() - comp.getX() - 3), comp.height());
			}
		}};
		new TGUIVBoxLayout(new TGUIObject(TPoint.ZERO, new TSize(180, (int)(gNew.height() * .4))), false)
		{{
			setPosition(180 + 3 + 183, gNew.height() * .4f + 6 + 28 + 5);
			
			addComponent(new TLabel(gNew)
			{{
				setText("Seed:", TEAlignment.CENTRE);	
			}});
			addComponent(new TTextBox(gNew)
			{{
				onTextChange(new TGUITextEvent()
				{
					public void execute(String arg0, TGUIComponent arg1)
					{
						WorldGenerator.setSeed(arg0);
					}
				});	
			}});
			addComponent(new TButton(gNew)
			{{
				setText("Change Seed");
				
				onMouseRelease(new TGUIClickedEvent()
				{
					public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
					{
						WorldGenerator.setSeed(((TTextBox)gNew.child(getID() - 1)).getText());
					}
				});
				onMouseOver(new TGUIMouseOverEvent()
				{
					public void execute(TGUIComponent arg0)
					{
						SoundManager.playSound(StateMenu.mOver);
					}
				});
			}});
			addComponent(new TLabel());
			
			toggleStretching();
			organise();
			
			for (byte i = 14; i < gNew.childCount(); ++i)
			{
				TGUIComponent comp = gNew.child(i);
				comp.setSize((int)(gNew.width() - comp.getX() - 3), comp.height());
			}
		}};
		gNew.addComponent(new TButton(gNew, gNew.width() - 163 - 3, gNew.height() - 30 - 3, 163, 30, "Begin")
		{{
			onMouseRelease(new TGUIClickedEvent()
			{
				public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
				{
					gNew.setVisible(false);
					gMenu.setVisible(true);
					title.setAlpha(1f);
					game.enterState(Game.sLevelLoad, new FadeOutTransition(Color.black, 500), new FadeInTransition(Color.black, 500));
				}
			});	
			onMouseOver(new TGUIMouseOverEvent()
			{
				public void execute(TGUIComponent arg0)
				{
					SoundManager.playSound(StateMenu.mOver);
				}
			});
		}});
		gNew.addComponent(new TLabel(gNew, 3, gNew.height() - 3 - 50 - 3 - 25, 363, 25, "Number of AI enemies:", TEAlignment.CENTRE));
		gNew.addComponent(new TSlider(gNew, (3 + 180) / 2, gNew.height() - 3 - 40, 180, 35, 7, 1, 3)
		{{
			onValueChange(new TGUIValueEvent()
			{
				public void execute(long arg0, TGUIComponent arg1)
				{
					((TLabel)gNew.child(gNew.childCount() - 1)).setText("" + arg0);
					WorldGenerator.setAICount((byte)arg0);
					PlayerManager.setPlayerCount((byte)arg0);
				}
			});	
		}});
		gNew.addComponent(new TLabel(gNew, 180 + 3 + 3 + (183 / 2), gNew.height() - 3 - 40, 180, 25, "" + ((TSlider)(gNew.child(gNew.childCount() - 1))).getValue(), TEAlignment.CENTRE_LEFT));
		gNew.setColour(gNew.background, Color.blue.darker());
		gNew.setColour(gNew.border, Color.blue.darker(.75f));
		for (byte i = 0; i < gNew.childCount(); ++i)
		{
			TGUIComponent comp = gNew.child(i);
			comp.setTransparency(.5f);
			comp.setColour(comp.background, Color.blue.darker());
			if (comp.getType() == ComponentType.button)
			{
				TButton button = (TButton)comp;
				button.setColour(button.background, Color.blue.darker());
				button.setColour(button.border_white, Color.blue.brighter());
				button.setColour(button.border_grey, Color.blue.darker(.75f));
				button.setColour(button.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.buttonToggle)
			{
				TButtonToggle button = (TButtonToggle)comp;
				button.setColour(button.background, Color.blue.darker());
				button.setColour(button.border_white, Color.blue.brighter());
				button.setColour(button.border_grey, Color.blue.darker(.75f));
				button.setColour(button.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.label)
			{
				TLabel label = (TLabel)comp;
				label.setColour(label.font_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.container)
			{
				@SuppressWarnings("unchecked")
				TContainer<Color> box = (TContainer<Color>)comp;
				box.setColour(box.background, Color.blue.darker());
				box.setColour(box.border, Color.blue.brighter());
				box.setColour(box.selected_colour, Color.green.brighter(.75f));
			}
			else if (comp.getType() == ComponentType.slider)
			{
				TSlider box = (TSlider)comp;
				box.setColour(box.background, Color.blue.darker());
				box.setColour(box.border_white, Color.blue.brighter());
				box.setColour(box.border_grey, Color.blue.darker(.75f));
				box.setColour(box.slider_background, Color.blue.darker());
			}
			else if (comp.getType() == ComponentType.textBox)
			{
				TTextBox box = (TTextBox)comp;
				box.setColour(box.background, Color.blue.darker());
				box.setColour(box.border, Color.blue.brighter());
				box.setColour(box.font_colour, Color.green.brighter(.75f));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void enter(GameContainer container, StateBasedGame game) throws SlickException
	{
		if (roomLoad)
		{
			container.setMouseCursor(TCursor.graphic, 0, 0);
			((TContainer<Color>)gNew.child(9)).setSelected(2);
			WorldGenerator.setStarMax(100);
			WorldGenerator.setStartStars((byte)1);
			WorldGenerator.setStartShips((byte)3);
			WorldGenerator.setAICount((byte)3);
			
			roomLoad = false;
		}
		style = rand.nextBoolean();
		Camera.init(container.getInput(), Game.screenWidth, Game.screenHeight);
		Camera.setStateBounds(new TPoint(-100, -100), new TPoint(900, 900));
		Camera.setPosition(0, 0);
		MusicManager.loop(theme);
		SoundManager.setVolume(Settings.soundVolume);
	}

	@SuppressWarnings("deprecation")
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException
	{
		g.clear();
		float x = TCursor.getX();
		float y = TCursor.getY();
		float xdist = ((Game.screenWidth / 2) - x) * 100 / Game.screenWidth;
		float ydist = ((Game.screenHeight / 2) - y) * 100 / Game.screenHeight;
		if (!style)
			g.translate(xdist, ydist);
		else
			g.translate(Camera.position.x, Camera.position.y);
		g.drawImage(background, -100, -100);
		if (!style)
			g.translate(-xdist, -ydist);
		else
			g.translate(-Camera.position.x, -Camera.position.y);
		g.drawImage(title, Game.screenWidth / 2 - title.getWidth() / 2, 10);
		drawGUIs(g);
		g.setFont(Game.littleFont);
		g.setColor(Color.green.brighter(.75f));
		g.drawString("Version " + Game.version, 3, Game.screenHeight - Game.littleFont.getHeight());
		g.setFont(Game.guiFont);
	}

	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException
	{
		if (style)
		{
			if (!Camera.moving())
				Camera.move(rand.nextInt(100), rand.nextInt(100), .03f);
			Camera.update(delta);
			//if (Camera.position.x < -100 || Camera.position.y < -100 || Camera.position.x + Camera.size.width > 1000 || Camera.position.y + Camera.size.height > 1000)
				//Camera.stop();
		}
	}

	public int getID()
	{
		return ID;
	}
	
	protected void drawGUIs(Graphics g)
	{
		gMenu.update(g);
		gOptions.update(g);
		gNew.update(g);
	}
	
	public void mousePressed(int button, int x, int y)
	{
		gMenu.mousePressed(button, x, y);
		gOptions.mousePressed(button, x, y);
		gNew.mousePressed(button, x, y);
	}
	
	public void mouseReleased(int button, int x, int y)
	{
		gMenu.mouseReleased(button, x, y);
		gOptions.mouseReleased(button, x, y);
		gNew.mouseReleased(button, x, y);
	}
	
	public void keyPressed(int key, char c)
	{
		gOptions.keyPressed(key, c);
		gNew.keyPressed(key, c);
	}

	public void musicEnded(Music music)
	{
		
	}

	public void musicSwapped(Music music, Music newMusic)
	{
		
	}
}
