package com.tl.rts.src;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class Logger
{
	private static FileOutputStream stream;
	private static PrintStream print;
	
	public static void init()
	{
		try
		{
			stream = new FileOutputStream("log.txt");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		print = new PrintStream(stream);
		System.setOut(print);
		System.setErr(print);
	}
	
	@SuppressWarnings("deprecation")
	public static void log(String message)
	{
		Date date = new Date();
		System.out.println(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + " - " + message);
	}
	
	public static void deinit()
	{
		try
		{
			stream.close();
			print.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
