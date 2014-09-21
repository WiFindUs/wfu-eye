package wifindus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Debugger
{
	public enum Verbosity
	{
		Verbose,
		Information,
		Warning,
		Error,
		Exception
	}
	private BufferedWriter writer = null;
	private Verbosity minVerbosity = Verbosity.Information;
	private static final DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Debugger debugger = null;
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	public static void open(Verbosity minVerbosity, File file)
	{
		if (debugger != null)
			return;

		if (file == null)
			open(minVerbosity);
		else
			debugger = new Debugger(minVerbosity, file);
	}
	
	public static void open(Verbosity minVerbosity)
	{
		if (debugger != null)
			return;

		File dir = new File("logs");
		if (!dir.exists())
			dir.mkdir();
		if (dir.exists() && dir.isDirectory())
			debugger = new Debugger(minVerbosity, new File( "logs/session_" + new SimpleDateFormat("yy-MM-dd_HH-mm-ss").format(new Date()) + ".log" ));
	}
	
	public static void open(File file)
	{
		open(Verbosity.Information,file);
	}
	
	public static void open()
	{
		open(Verbosity.Information);
	}
	
	public static String getTimestamp()
	{
		return dateFormat.format(new Date());
	}

	public static void v(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Verbose, System.out, s);
	}
	
	public static void i(String s)
	{
		if (debugger != null)	
			debugger.log(Verbosity.Information, System.out, s);
	}
	
	public static void w(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Warning, System.out, s);
	}
	
	public static void e(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Error, System.err, s);
	}
	
	public static void ex(Exception e)
	{
		if (debugger != null)
			debugger.log(Verbosity.Exception,System.err,
					e.getClass().getName() + ": " + e.getMessage());
	}
	
	public static void close() 
	{
		if (debugger == null)
			return;
		debugger.dispose();
		debugger = null;		
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private Debugger(Verbosity minVerbosity, File file)
	{
		try
		{
			this.minVerbosity = minVerbosity;
			writer = new BufferedWriter(new FileWriter(file));
		}
		catch (IOException e)
		{
			log(Verbosity.Exception,System.err,
					e.getClass().getName() + ": " + e.getMessage());
			if (writer != null)
			{
				try { writer.close(); }
				catch (Exception e2) { }
				writer = null;
			}
		}
		finally
		{
			log(Verbosity.Information, System.out, "Session started.");
		}
	}
	
	private void log(Verbosity level, PrintStream stream, String s)
	{
		if (level.compareTo(minVerbosity) < 0)
			return;
		
		String message = getTimestamp() + (s == null ? "" : " " + s);
		if (writer != null)
		{
			try { writer.write(message + "\n"); }
			catch (IOException e) { }
		}
		stream.println(message);
	}
	
	private void dispose() 
	{
		log(Verbosity.Information, System.out, "Session terminated.");
		if (writer != null)
		{
			try { writer.close(); }
			catch (IOException e) { }
		}		
	}
}