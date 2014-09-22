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
	/**
	 * A description of a Debugger verbosity level.
	 * @author Mark 'marzer' Gillard
	 */
	public enum Verbosity
	{
		/**
		 * ALL levels of message are output by the Debugger.
		 */
		Verbose,
		
		/**
		 * Levels of verbosity of 'Information' and higher are output by the Debugger.
		 */
		Information,
		
		/**
		 * Levels of verbosity of 'Warning' and higher are output by the Debugger.
		 */
		Warning,
		
		/**
		 * Levels of verbosity of 'Error' and higher are output by the Debugger.
		 */
		Error,
		
		/**
		 * Only Exceptions are output by the Debugger.
		 */
		Exception
	}
	
	private BufferedWriter writer = null;
	private Verbosity minVerbosity = Verbosity.Information;
	private static final DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Debugger debugger = null;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
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
			log(Verbosity.Information, System.out, "Session started (Debugger minimum verbosity: "+minVerbosity.toString()+").", true);
		}
	}
	
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

	/**
	 * Outputs a line of debugging information with a verbosity level of Verbosity.Verbose. Console output is done on stdout.
	 * @param s The string to output.
	 */
	public static void v(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Verbose, System.out, s);
	}
	
	/**
	 * Outputs a line of debugging information with a verbosity level of Verbosity.Information. Console output is done on stdout.
	 * @param s The string to output.
	 */
	public static void i(String s)
	{
		if (debugger != null)	
			debugger.log(Verbosity.Information, System.out, s);
	}
	
	/**
	 * Outputs a line of debugging information with a verbosity level of Verbosity.Warning. Console output is done on stdout.
	 * @param s The string to output.
	 */
	public static void w(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Warning, System.out, s);
	}
	
	/**
	 * Outputs a line of debugging information with a verbosity level of Verbosity.Error. Console output is done on stderr.
	 * @param s The string to output.
	 */
	public static void e(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Error, System.err, s);
	}
	
	/**
	 * Outputs debugging information about an Exception (with a verbosity level of Verbosity.Exception). Console output is done on stderr.
	 * @param e The exception that was thrown.
	 */
	public static void ex(Exception e)
	{
		if (debugger != null)
			debugger.log(Verbosity.Exception,System.err,
					e.getClass().getName() + ": " + e.getMessage());
	}
	
	/**
	 * Closes the debugger, flushing file output and no longer logging output to stdout/stderr.
	 */
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
	
	private void log(Verbosity level, PrintStream stream, String s, boolean force)
	{
		if (!force && level.compareTo(minVerbosity) < 0)
			return;
		
		String message = getTimestamp() + (s == null ? "" : " " + s);
		if (writer != null)
		{
			try { writer.write(message + "\n"); }
			catch (IOException e) { }
		}
		stream.println(message);
	}
	
	private void log(Verbosity level, PrintStream stream, String s)
	{
		log(level,stream,s,false);
	}
	
	private void dispose() 
	{
		log(Verbosity.Information, System.out, "Session terminated.",true);
		if (writer != null)
		{
			try { writer.close(); }
			catch (IOException e) { }
		}		
	}
}