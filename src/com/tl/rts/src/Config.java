package com.tl.rts.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

public class Config
{
	private static final String configPath = "./res/config.cfg";
	private static LinkedList<Setting> list = new LinkedList<>();
	
	public static void readConfig() throws FileNotFoundException
	{
		Scanner scanner = new Scanner(new FileInputStream(configPath));
		while (scanner.hasNext())
		{
			String line = scanner.nextLine();
			if (isLegit(line))
			{
				Setting setting = new Setting(line);
				list.add(setting);
				if (setting.getOption().contentEquals("fullscreen"))
					Settings.fullscreen = Boolean.parseBoolean(setting.getValue());
				else if (setting.getOption().contentEquals("music"))
					Settings.musicVolume = Integer.parseInt(setting.getValue());
				else if (setting.getOption().contentEquals("sound"))
					Settings.soundVolume = Integer.parseInt(setting.getValue());
				else if (setting.getOption().contentEquals("name"))
					Settings.name = setting.getValue(false);
			}
		}
		scanner.close();
	}
	
	public static void writeConfig() throws IOException
	{
		Path path = Paths.get(configPath);
		String config = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		
		for (byte i = 0; i < list.size(); ++i)
		{
			Setting setting = list.get(i);
			String start = setting.getOption() + "=";
			String replace = new String(start);
			switch(i)
			{
				case 0:
					replace += String.valueOf(Settings.fullscreen);
					break;
				case 1:
					replace += String.valueOf(Settings.musicVolume);
					break;
				case 2:
					replace += String.valueOf(Settings.soundVolume);
					break;
				case 3:
					replace += Settings.name;
					break;
			}
			config = config.replace(start + setting.getValue(), replace);
		}
		Files.write(path, config.getBytes());
	}
	
	private static boolean isLegit(String line)
	{
		String test = line.trim();
		if (test.isEmpty())
			return false;
		return !test.startsWith("#") && test.contains("=");
	}
	
	public static class Setting
	{
		String option;
		String value;
		
		public Setting(String line)
		{
			option = line.substring(0, line.indexOf('=')).trim();
			value = line.substring(line.indexOf('=') + 1);
			if (value.contains("#"))
				value = value.substring(0, value.indexOf('#')).trim();
		}
		
		public String getOption()
		{
			return option;
		}
		
		public String getValue()
		{
			return value;
		}
		
		public String getValue(boolean trim)
		{
			if (trim)
				return value.trim();
			return value;
		}
	}
}
