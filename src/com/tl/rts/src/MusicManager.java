package com.tl.rts.src;

import java.util.LinkedList;

import org.newdawn.slick.Music;

public class MusicManager
{
	static LinkedList<Music> tracks = new LinkedList<>();
	static float volume;
	
	public static void addTrack(Music music)
	{
		tracks.add(music);
		music.setVolume(MusicManager.volume);
	}
	
	public static void setVolume(int volume)
	{
		MusicManager.volume = (float)volume / 100f;
		for (Music music : tracks)
			music.setVolume((float)Math.pow(MusicManager.volume, 2.0));
	}
	
	public static float getVolume()
	{
		return volume;
	}
	
	public static void loop(Music music)
	{
		music.loop();
		music.setVolume(volume);
	}
	
	public static void play(Music music)
	{
		music.play();
		music.setVolume(volume);
	}
	
	public static void stop()
	{
		for (Music music : tracks)
			if (music.playing())
				music.stop();
	}
}
