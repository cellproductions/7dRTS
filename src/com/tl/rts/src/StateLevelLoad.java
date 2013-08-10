package com.tl.rts.src;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import tl.GUI.TEAlignment;
import tl.GUI.TGUIValueEvent;
import tl.GUI.TLabel;
import tl.GUI.TButton;
import tl.GUI.TContainer;
import tl.GUI.TGUIClickedEvent;
import tl.GUI.TGUIComponent;
import tl.GUI.TGUIVBoxLayout;
import tl.GUI.TGUIObject;
import tl.GUI.TSlider;
import tl.Util.TPoint;
import tl.Util.TSize;

import com.tl.rts.world.Fleet;
import com.tl.rts.world.Player;
import com.tl.rts.world.PlayerManager;
import com.tl.rts.world.Star;
import com.tl.rts.world.WorldGenerator;

public class StateLevelLoad extends BasicGameState
{
	byte ID;
	private final int lheight = 20;
	private final int lwidth = (int)(Game.screenWidth * .3f);
	private int currWidth = 0;
	private int currWidthMax = 0;
	private final float lx = Game.screenWidth / 2 - lwidth / 2;
	private final float ly = Game.screenHeight / 2 - lheight / 2;
	private String text = "";
	private static int maxResCount = 2;
	private static int percent;
	private static int count;
	private static LinkedList<Resource> queue;
	
	Music music;
	
	
	public StateLevelLoad(byte id)
	{
		ID = id;
	}
	
	public void init(GameContainer container, final StateBasedGame game) throws SlickException
	{
		
	}
	
	public void enter(GameContainer container, final StateBasedGame game)
	{
		queue = new LinkedList<>();
		
		queue.add(new Resource("./res/audio/")
		{
			public void execute() throws SlickException
			{
				StateLevel.upgrade = new Sound(toLoad + "upgrade.ogg");
				StateLevel.failed = new Sound(toLoad + "failed.ogg");
				StateLevel.mOver = new Sound(toLoad + "mouseover.ogg");
				StateLevel.fleetSelect = new Sound(toLoad + "fleetselect.ogg");
				StateLevel.fleetMove = new Sound(toLoad + "fleetmove.ogg");
				StateLevel.systemSelect = new Sound(toLoad + "systemselect.ogg");
				StateLevel.theme1 = new Music(toLoad + "track2.ogg");
				StateLevel.theme2 = new Music(toLoad + "track3.ogg");
				MusicManager.addTrack(StateLevel.theme1);
				MusicManager.addTrack(StateLevel.theme2);
				text = "Loaded Audio...";
			}
		});
		queue.add(new Resource("")
		{
			public void execute() throws SlickException
			{
				text = "Loaded Graphics...";
				StateLevel.background = StateMenu.background;
			}
		});
		queue.add(new Resource("./res/images/")
		{
			public void execute() throws SlickException
			{
				Star.graphic = new Image(toLoad + "star.png");
				int size = Star.radius * 2;
				if (Star.graphic.getWidth() != size && Star.graphic.getHeight() != size)
					Star.graphic = Star.graphic.getScaledCopy(size, size);
				Fleet.graphic = new Image(toLoad + "ship.png");
			}	
		});
		queue.add(new Resource("")
		{
			public void execute() throws SlickException
			{
				text = "Generated Systems...";
				WorldGenerator.generateWorld();
			}
		});
		queue.add(new Resource("")
		{
			public void execute() throws SlickException
			{
				text = "Connected Systems...";
				WorldGenerator.generateConnections();
			}	
		});
		queue.add(new Resource("")
		{
			public void execute() throws SlickException
			{
				text = "Generated Ships...";
				WorldGenerator.generateShipStarts();
			}	
		});
		queue.add(new Resource("")
		{
			public void execute() throws SlickException
			{
				text = "Created GUI...";
				TGUIComponent minimap = new TGUIComponent(null, 0, Game.screenHeight - 202 - 1, 202, 202);
				TContainer<Object> display = new TContainer<Object>(minimap, 1, 1, 200, 200, 200, 200, 0)
				{{/*
					StarManager manager = new StarManager();
					Image image = new Image(200, 200);
					Graphics g = image.getGraphics();
					g.setColor(Color.black);
					g.fillRect(0, 0, 200, 200);
					g.setColor(Star.neutral);
					for (Star star : manager)
					{
						float x = (star.getPosition().x / WorldGenerator.getSize().width) * 200;
						float y = (star.getPosition().y / WorldGenerator.getSize().height) * 200;
						for (Star conn : star.getConnections())
							g.drawLine(x, y, (conn.getPosition().x / WorldGenerator.getSize().width) * 200, (conn.getPosition().y / WorldGenerator.getSize().height) * 200);
					}
					for (Star star : manager)
					{
						float x = (star.getPosition().x / WorldGenerator.getSize().width) * 200;
						float y = (star.getPosition().y / WorldGenerator.getSize().height) * 200;
						if (star.getOwner() != null)
							g.setColor(star.getOwner().getColour());
						else
							g.setColor(Star.neutral);
						g.fillRect(x - 2, y - 2, 4, 4);
					}
					g.flush();
						
					addItem(image, new Object());*/
					Image image = new Image(itemWidth(), itemHeight());
					Graphics g = image.getGraphics();
					g.setColor(Color.black);
					g.fillRect(0, 0, itemWidth(), itemHeight());
					g.flush();
					addItem(image, new Object());
					
					onMousePress(new TGUIClickedEvent()
					{
						public void execute(int button, int x, int y, TGUIComponent arg3)
						{
							if (button == 0)
							{
								deselect();/*
								if (Fleet.selected != null)
								{
									TPoint conv = new TPoint((x - getScreenX()) * WorldGenerator.getSize().width / itemWidth(),
											(y - getScreenY()) * WorldGenerator.getSize().height / itemHeight());
									Star selected = PathFinder.getNearest(conv);
									selected.selectCommand();
								}*/
							}
						}
					});
				}};
				minimap.addComponent(display);
				
				final TGUIComponent fleet = new TGUIComponent(null, minimap.width() + 1, Game.screenHeight - 100, false, Game.screenWidth - minimap.width() + 1, 100, .0f)
				{{
					setColour(background, Color.blue.darker());
					setColour(border, Color.blue.darker(.75f));
				}};
				new TGUIVBoxLayout(new TGUIObject(TPoint.ZERO, new TSize(fleet.width() / 2, fleet.height())), false)
				{{
					addComponent(new TLabel(fleet)
					{{
						setText("Owner: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(fleet)
					{{
						setText("Fleet attack: ", TEAlignment.CENTRE_LEFT);	
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					
					toggleStretching();
					organise();
				}};
				new TGUIVBoxLayout(new TGUIObject(TPoint.ZERO, new TSize(fleet.width(), fleet.height())), false)
				{{
					setPosition(fleet.width() / 2, 0);
					
					addComponent(new TLabel(fleet)
					{{
						setText("Fleet size: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(fleet)
					{{
						setText("Fleet defence: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					
					toggleStretching();
					organise();
				}};
				final TGUIComponent system = new TGUIComponent(null, minimap.width() + 1, Game.screenHeight - 100, false, Game.screenWidth - minimap.width() + 1, 100, .0f)
				{{
					setColour(background, Color.blue.darker());
					setColour(border, Color.blue.darker(.75f));
				}};
				system.addComponent(new TLabel(system, 0, 0, system.width() - 150, system.height(), "", TEAlignment.CENTRE)
				{{
					setColour(font_colour, Color.green.brighter(.75f));
				}});
				new TGUIVBoxLayout(new TGUIObject(TPoint.ZERO, new TSize(system.width() / 2, system.height())), false)
				{{
					addComponent(new TLabel(system));
					addComponent(new TLabel(system)
					{{
						setText("Owner: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(system)
					{{
						setText("Population: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					
					toggleStretching();
					organise();
				}};
				new TGUIVBoxLayout(new TGUIObject(new TPoint(), new TSize(fleet.width(), system.height())), false)
				{{
					setPosition(system.width() / 2, 0);
					
					addComponent(new TLabel(system));
					addComponent(new TLabel(system)
					{{
						setText("System defence: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(system)
					{{
						setText("Technology: ", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					
					toggleStretching();
					organise();
				}};
				
				final TGUIComponent menu = new TGUIComponent(null, Game.screenWidth / 2 - 80f, Game.screenHeight / 2 - (Game.screenHeight * .5f) / 2, false, 160, (int)(Game.screenHeight * .25f), .75f);
				menu.setColour(menu.background, Color.blue.darker());
				menu.setColour(menu.border, Color.blue.darker(.75f));
				new TGUIVBoxLayout(menu, false)
				{{
					addComponent(new TButton(menu)
					{{
						setTransparency(.5f);
						setText("Back");
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								menu.setVisible(false);
							}	
						});
					}});
					addComponent(new TButton(menu)
					{{
						setTransparency(.5f);
						setText("Settings");
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								menu.setVisible(false);
								StateLevel.gGameSettings.setVisible(true);
							}	
						});
					}});
					addComponent(new TButton(menu)
					{{
						setTransparency(.5f);
						setText("Main Menu");
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								menu.setVisible(false);
								game.enterState(Game.sMenu, new FadeOutTransition(Color.black, 500), new FadeInTransition(Color.black, 500));
							}	
						});
					}});
					
					toggleStretching();
					organise();
				}};
				
				TButton button = new TButton(null, Game.screenWidth - 100 - 1, 0, 100, 30, "Menu")
				{{
					setTransparency(.5f);
					setColour(this.background, Color.blue.darker());
					setColour(this.border_grey, Color.blue.darker(.75f));
					setColour(this.border_white, Color.blue.brighter());
					setColour(this.font_colour, Color.green.brighter(.75f));
					onMouseRelease(new TGUIClickedEvent()
					{
						public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
						{
							StateLevel.gTech.setVisible(false);
							StateLevel.gGameSettings.setVisible(false);
							menu.setVisible(!menu.getVisibility());
						}	
					});
				}};
				final TGUIComponent tech = new TGUIComponent(null, Game.screenWidth / 2 - 150, Game.screenHeight / 2 - 230, false, 322, 400, .5f)
				{{
					setColour(background, Color.blue.darker());
					setColour(border, Color.blue.darker(.75f));
				}};
				new TGUIVBoxLayout(tech, false)
				{{
					addComponent(new TButton(tech)
					{{
						setText("Close");
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								tech.setVisible(false);
							}
						});
					}});
					addComponent(new TLabel(tech)
					{{
						setTransparency(.5f);
						setText(PlayerManager.player.getName(), TEAlignment.CENTRE);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(tech)
					{{
						setTransparency(.5f);
						setText("Total Technology: " + PlayerManager.player.getTotalTech(), TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(tech)
					{{
						setTransparency(.5f);
						setText("Technology points: " + PlayerManager.player.getTechPoints(), TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(tech)
					{{
						setTransparency(.5f);
						setText("Click to upgrade:", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));	
					}});
					addComponent(new TButton(tech)
					{{
						setText("Fleet Speed: " + PlayerManager.player.getSpeed());
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								if (PlayerManager.player.getTechPoints() > 0 && !PlayerManager.player.speedAtMax())
								{
									PlayerManager.player.updateSpeed();
									PlayerManager.player.spendTechPoint();
									StateLevel.updateTechGUI();
									SoundManager.playSound(StateLevel.upgrade);
								}
								else
									SoundManager.playSound(StateLevel.failed);
							}
						});
					}});
					addComponent(new TButton(tech)
					{{
						setText("Fleet Attack: " + PlayerManager.player.getAttack());
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								if (PlayerManager.player.getTechPoints() > 0 && !PlayerManager.player.attackAtMax())
								{
									PlayerManager.player.updateAttack();
									PlayerManager.player.spendTechPoint();
									StateLevel.updateTechGUI();
									SoundManager.playSound(StateLevel.upgrade);
								}
								else
									SoundManager.playSound(StateLevel.failed);
							}
						});
					}});
					addComponent(new TButton(tech)
					{{
						setText("Fleet Defence: " + PlayerManager.player.getDefence());
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								if (PlayerManager.player.getTechPoints() > 0 && !PlayerManager.player.defenceAtMax())
								{
									PlayerManager.player.updateDefence();
									PlayerManager.player.spendTechPoint();
									StateLevel.updateTechGUI();
									SoundManager.playSound(StateLevel.upgrade);
								}
								else
									SoundManager.playSound(StateLevel.failed);
							}
						});
					}});
					addComponent(new TButton(tech)
					{{
						setText("System Defence: " + PlayerManager.player.getSystemDefence());
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								if (PlayerManager.player.getTechPoints() > 0 && !PlayerManager.player.systemDefenceAtMax())
								{
									PlayerManager.player.updateSystemDefence();
									PlayerManager.player.spendTechPoint();
									StateLevel.updateTechGUI();
									SoundManager.playSound(StateLevel.upgrade);
								}
								else
									SoundManager.playSound(StateLevel.failed);
							}
						});
					}});
					addComponent(new TButton(tech)
					{{	
						setText("Place fleet on " + (Star.selected == null ? "N/A" : (Star.selected.getOwner() == PlayerManager.player ? Star.selected.getName() : "N/A")));
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								boolean success = false;
								if (Star.selected != null)
								{
									if (Star.selected.getOwner() == PlayerManager.player)
									{
										if (PlayerManager.player.getTechPoints() > 0)
										{
											success = true;
											Star.selected.addFleet(new Fleet(Star.selected.getPosition(), PlayerManager.player, 1));
											PlayerManager.player.spendTechPoint();
											StateLevel.updateTechGUI();
											SoundManager.playSound(StateLevel.upgrade);
										}
									}
								}
								if (!success)
									SoundManager.playSound(StateLevel.failed);
							}
						});
					}});
					addComponent(new TButton(tech)
					{{
						setText("Colonize N/A");
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								boolean success = false;
								if (Fleet.selected != null)
								{
									Star over = Fleet.selected.isOverStar();
									if (Fleet.selected.getOwner() == PlayerManager.player && over != null && over.getPopulation() < 100)
									{
										if (PlayerManager.player.getTechPoints() > 0 && over.getHoveringFleet() != null)
										{
											success = true;
											over.removeFleet(1);
											over.setPopulation((short)(over.getPopulation() + 1));
											PlayerManager.player.spendTechPoint();
											StateLevel.updateTechGUI();
											SoundManager.playSound(StateLevel.upgrade);
										}
									}
								}
								if (!success)
									SoundManager.playSound(StateLevel.failed);
							}
						});
					}});
					
					toggleStretching();
					organise();
				}};
				TButton btech = new TButton(null, 0, 1, 130, 30, "Technology")
				{{
					setTransparency(.5f);
					setColour(this.background, Color.blue.darker());
					setColour(this.border_grey, Color.blue.darker(.75f));
					setColour(this.border_white, Color.blue.brighter());
					setColour(this.font_colour, Color.green.brighter(.75f));
					onMouseRelease(new TGUIClickedEvent()
					{
						public void execute(int button, int x, int y, TGUIComponent component)
						{
							menu.setVisible(false);
							tech.setVisible(!tech.getVisibility());
							StateLevel.gGameSettings.setVisible(false);
							StateLevel.updateTechGUI();
						}
					});
				}};
				int height = (PlayerManager.getPlayers().size() + 2) * 30 + 3 + 33;
				final TGUIComponent players = new TGUIComponent(null, Game.screenWidth / 2 - (tech.width() * .67f) / 2, Game.screenHeight / 2 - height / 2, false, (int)(tech.width() * .67f), height, .5f)
				{{
					setColour(background, Color.blue.darker());
					setColour(border, Color.blue.darker(.75f));
				}};
				new TGUIVBoxLayout(players, false)
				{{
					addComponent(new TButton(players)
					{{
						setText("Close");
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int button, int x, int y, TGUIComponent component)
							{
								players.setVisible(false);
							}
						});
					}});
					addComponent(new TLabel(players)
					{{
						setTransparency(.5f);
						setText("Players:", TEAlignment.CENTRE_LEFT);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(players)
					{{
						setTransparency(.5f);
						setColour(font_colour, PlayerManager.player.getColour());
						setText(PlayerManager.player.getName(), TEAlignment.CENTRE);
					}});
					
					for (final Player player : PlayerManager.getPlayers())
					{
						addComponent(new TLabel(players)
						{{
							this.setTransparency(.5f);
							this.setColour(font_colour, player.getColour());
							this.setText(player.getName(), TEAlignment.CENTRE);
						}});
					}
					
					toggleStretching();
					organise();
				}};
				TLabel label = new TLabel(null, 1, minimap.getScreenY() - 25 - 3, Game.screenWidth, 30, "Good luck. Have fun.", TEAlignment.CENTRE_LEFT)
				{{
					setTransparency(.75f);
					setColour(font_colour, Color.green.brighter(.75f));	
				}};
				final TGUIComponent over = new TGUIComponent(null, Game.screenWidth / 2 - tech.width() / 2, Game.screenHeight / 2 - 69 / 2, false, tech.width(), 69, .5f)
				{{
					setColour(background, Color.blue.darker());
					setColour(border, Color.blue.darker(.75f));
				}};
				new TGUIVBoxLayout(over, false)
				{{
					addComponent(new TLabel(over)
					{{
						setTransparency(.5f);
						setText("", TEAlignment.CENTRE);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TButton(over)
					{{
						setText("Return to Menu");
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int button, int x, int y, TGUIComponent component)
							{
								over.setVisible(false);
								game.enterState(Game.sMenu, new FadeOutTransition(Color.black, 500), new FadeInTransition(Color.black, 500));
							}
						});
					}});
					
					toggleStretching();
					organise();
				}};
				final TGUIComponent setting = new TGUIComponent(null, menu.getX() - 50, Game.screenHeight / 2 - (menu.height() + 330) / 2, false, menu.width() + 100, menu.height() + 330, .5f)
				{{
					setColour(background, Color.blue.darker());
					setColour(border, Color.blue.darker(.75f));
				}};
				new TGUIVBoxLayout(setting, false)
				{{
					addComponent(new TButton(setting)
					{{
						setText("Back");
						setTransparency(.5f);
						setColour(this.background, Color.blue.darker());
						setColour(this.border_grey, Color.blue.darker(.75f));
						setColour(this.border_white, Color.blue.brighter());
						setColour(this.font_colour, Color.green.brighter(.75f));
						
						onMouseRelease(new TGUIClickedEvent()
						{
							public void execute(int arg0, int arg1, int arg2, TGUIComponent arg3)
							{
								setting.setVisible(false);
								menu.setVisible(true);
							}
						});
					}});
					addComponent(new TLabel(setting)
					{{
						setText("Seed: ");
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(setting)
					{{
						setText(WorldGenerator.getSeed(), TEAlignment.CENTRE);
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(setting)
					{{
						setText("Scroll Speed:");	
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TSlider(setting)
					{{
						setRange(500, 50);
						setValue(175);
						setTransparency(.5f);
						setColour(background, Color.blue.darker());
						setColour(border_white, Color.blue.brighter());
						setColour(border_grey, Color.blue.darker(.75f));
						setColour(slider_background, Color.blue.darker());
						
						onValueChange(new TGUIValueEvent()
						{
							public void execute(long arg0, TGUIComponent arg1)
							{
								Camera.setControlSpeed((float)arg0 / 100f / Camera.zoom);
								GameSettings.setScrollSpeed((float)arg0 / 100f / Camera.zoom);
								((TLabel)setting.child(5)).setText("" + arg0, TEAlignment.CENTRE);
							}
						});
					}});
					addComponent(new TLabel(setting)
					{{
						setText("175", TEAlignment.CENTRE);	
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(setting)
					{{
						setText("Music Volume:");	
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TSlider(setting)
					{{
						setRange(100, 0);
						setValue((int)MusicManager.getVolume() * 100);
						setTransparency(.5f);
						setColour(background, Color.blue.darker());
						setColour(border_white, Color.blue.brighter());
						setColour(border_grey, Color.blue.darker(.75f));
						setColour(slider_background, Color.blue.darker());
						
						onValueChange(new TGUIValueEvent()
						{
							public void execute(long arg0, TGUIComponent arg1)
							{
								MusicManager.setVolume((int)arg0);
								Settings.musicVolume = (int)arg0;
								((TLabel)setting.child(8)).setText("" + arg0, TEAlignment.CENTRE);
							}
						});
					}});
					addComponent(new TLabel(setting)
					{{
						setText("" + (int)(MusicManager.getVolume() * 100), TEAlignment.CENTRE);	
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TLabel(setting)
					{{
						setText("Sound Volume:");	
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					addComponent(new TSlider(setting)
					{{
						setRange(100, 0);
						setValue((int)SoundManager.getVolume() * 100);
						setTransparency(.5f);
						setColour(background, Color.blue.darker());
						setColour(border_white, Color.blue.brighter());
						setColour(border_grey, Color.blue.darker(.75f));
						setColour(slider_background, Color.blue.darker());
						
						onValueChange(new TGUIValueEvent()
						{
							public void execute(long arg0, TGUIComponent arg1)
							{
								SoundManager.setVolume((int)arg0);
								Settings.soundVolume = (int)arg0;
								((TLabel)setting.child(11)).setText("" + arg0, TEAlignment.CENTRE);
							}
						});
					}});
					addComponent(new TLabel(setting)
					{{
						setText("" + (int)(SoundManager.getVolume() * 100), TEAlignment.CENTRE);	
						setTransparency(.5f);
						setColour(font_colour, Color.green.brighter(.75f));
					}});
					
					toggleStretching();
					organise();
				}};
				
				StateLevel.gMinimap = minimap;
				StateLevel.cMinimapDisplay = display;
				StateLevel.gHUDFleet = fleet;
				StateLevel.gHUDStar = system;
				
				StateLevel.bMenu = button;
				StateLevel.gMenu = menu;
				StateLevel.bTech = btech;
				StateLevel.gTech = tech;
				StateLevel.gPlayers = players;
				StateLevel.lMessage = label;
				StateLevel.gOver = over;
				StateLevel.gGameSettings = setting;
				WorldGenerator.generateMinimapObjects();
			}	
		});
		
		maxResCount = queue.size();
		
		currWidth = 0;
		currWidthMax = 0;
		percent = 0;
		count = 0;
		text = "";
	}
	
	@SuppressWarnings("deprecation")
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
		g.drawString(text, Game.screenWidth / 2 - Game.littleFont.getWidth(text) / 2, ly + lheight + 40);
		String l = "Loading: " + percent + "%";
		g.drawString(l, Game.screenWidth / 2 - Game.littleFont.getWidth(l) / 2, ly - 20);
	}

	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException
	{
		if (count < maxResCount)
		{
			if (!queue.isEmpty())
				queue.pop().begin();
			else
				count = maxResCount;
		}
		currWidthMax = percent / 100 * lwidth;
		if (currWidth < currWidthMax)
			currWidth += currWidthMax *.25;
		if (currWidth >= lwidth)
		{
			MusicManager.stop();
			game.enterState(Game.sLevel, new FadeOutTransition(Color.black, 500), new FadeInTransition(Color.black, 500));
		}
	}

	public int getID()
	{
		return ID;
	}
	
	private void updateCounter()
	{
		count++;
		percent = count * 100 / maxResCount;
	}
	
	private class Resource
	{
		String toLoad;
		public Resource(String path)
		{
			toLoad = path;
		}
		
		public void begin()
		{
			load();
		}
		
		public void execute() throws SlickException
		{
		}
		
		public void load()
		{
			try
			{
				execute();
				updateCounter();
			}
			catch (SlickException e)
			{
				e.printStackTrace();
			}
		}
	}
}