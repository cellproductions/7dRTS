package com.tl.rts.src;

import java.util.LinkedList;

import org.newdawn.slick.Sound;

public class SoundManager
{
	static LinkedList<Sound> tracks = new LinkedList<>();
	static float volume;
	
	public static void addTrack(Sound sound)
	{
		tracks.add(sound);
	}
	
	public static void setVolume(int volume)
	{
		SoundManager.volume = (float)volume / 100f;
	}
	
	public static void playSound(Sound sound)
	{
		if (sound != null)
			sound.play(1, (float)Math.pow(SoundManager.volume, 2.0));
	}
	
	public static float getVolume()
	{
		return volume;
	}
	
	public static void stop()
	{
		for (Sound sound : tracks)
			if (sound.playing())
				sound.stop();
	}
}
