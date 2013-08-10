package com.tl.rts.src;

public class GameSettings
{
	private static boolean drawMovement = true;
	private static float scrollSpeed;

	public static boolean getDrawMovement()
	{
		return drawMovement;
	}

	public static void setDrawMovement(boolean drawMovement)
	{
		GameSettings.drawMovement = drawMovement;
	}

	public static float getScrollSpeed()
	{
		return scrollSpeed;
	}

	public static void setScrollSpeed(float scrollSpeed)
	{
		GameSettings.scrollSpeed = scrollSpeed;
	}
	
}
