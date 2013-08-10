package com.tl.rts.world;


import java.util.Random;

import com.tl.rts.src.Logger;

public class SystemNameGenerator
{
	private static int seed[] = new int[3];
	private static Random rand = new Random();
	public static final String defaultSyllables = "..lexegezacebisousesarmaindirea.eratenberalavetiedorquanteisrion";
	private static String syllables = defaultSyllables;
	
	private static void init()
	{
		seed[0] = rand.nextInt(Integer.MAX_VALUE);
		seed[1] = rand.nextInt(Integer.MAX_VALUE);
		seed[2] = rand.nextInt(Integer.MAX_VALUE);
	}
	
	private static void changeSeed()
	{
		int tmp = (seed[0] + seed[1] + seed[2]) % 10000;
	    seed[0] = seed[1];
	    seed[1] = seed[2];
	    seed[2] = tmp;
	}
	
	private static String create() // tweaked generator used in Elite
	{
		String name;
		//int flag = seed[0] & 0x40;
		
		do
		{
			init();
			name = "";
			for (int i = 0; i < 3; ++i)
			{
				int pos = ((seed[2] >> 8) & 0x1f) << 1;
				changeSeed();
				
				try
				{
					name = name + syllables.substring(Math.max(pos, 0), Math.min(Math.max(pos, 0), 60) + 2);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					Logger.log("NameGenerator Error: Math.min(" + pos + " + 2, 63) is out of range.");
					Logger.log("NameGenerator Error:          " + (pos + 2) + "          where max is " + syllables.length() + ".");
					e.printStackTrace();
				}
				name = name.replaceAll("\\W", "");
			}
		} while (name.isEmpty());
		
		try
		{
			name = (char)(name.charAt(0) - ('a' - 'A')) + name.substring(1);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			System.out.println("NameGenerator Error: name " + name + " is " + (name.isEmpty() ? "empty, and" : "not empty, but") + " charAt(0) is out of range.");
			e.printStackTrace();
		}
		return  name.trim();
	}
	
	public static String createName()
	{	
		return create();
	}
	
	public static String createName(boolean addNumerals)
	{
		String ret = create();
		if (addNumerals)
		{
			byte max = rand.nextBoolean() ? (byte)(rand.nextInt(6)) : 0;
			if (max == 5)
				ret += " V";
			else if (max == 4)
				ret += " IV";
			else if (max != 0)
			{
				ret += " ";
				for (byte i = 0; i < max; ++i)
					ret += "I";
			}
		}
		return ret;
	}
	
	public static void setSeed(long seed)
	{
		rand.setSeed(seed);
	}
	
	public static void replaceSyllables(String newSyllables)
	{
		newSyllables = newSyllables.replaceAll("\\W", "").trim();
		if (newSyllables.length() < syllables.length() - 3)
		{
			for (int i = newSyllables.length(); i < syllables.length() - 3; ++i)
			{
				int c = rand.nextInt(27);
				char ch = (char)((c < 0 ? c + c + c : c) + 'a');
				newSyllables += ch;
			}
		}
		else if (newSyllables.length() > syllables.length() - 3)
			newSyllables = newSyllables.substring(0, syllables.length() - 3);
		syllables = ".." + newSyllables.substring(0, syllables.indexOf('.', 3)) + "." + newSyllables.substring(syllables.indexOf('.', 3) + 1);
	}
}
