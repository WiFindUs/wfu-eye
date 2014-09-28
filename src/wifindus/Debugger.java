package wifindus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.SwingUtilities;

/**
 * A global, static class encapsulating console output,
 * exception error reporting and logfile generation. 
 * @author Mark 'marzer' Gillard
 */
public final class Debugger
{
	/**
	 * A description of a Debugger verbosity level.
	 * @author Mark 'marzer' Gillard
	 */
	public enum Verbosity
	{
		/**
		 * 0: All messages are output by the Debugger.
		 */
		Verbose,
		
		/**
		 * 1: Only messages with a verbosity level of <code>Information</code> and higher are output by the Debugger.
		 */
		Information,
		
		/**
		 * 2: Only messages with a verbosity level of <code>Warning</code> and higher are output by the Debugger.
		 */
		Warning,
		
		/**
		 * 3: Only messages with a verbosity level of <code>Error</code> and higher are output by the Debugger.
		 */
		Error,
		
		/**
		 * 4: Only <code>Exceptions</code> are output by the Debugger.
		 */
		Exception
	}
	
	private BufferedWriter writer = null;
	private Verbosity minVerbosity = Verbosity.Information;
	private static final DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Debugger debugger = null;
	private volatile static ArrayList<DebuggerEventListener> listeners = new ArrayList<>();
	
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
			log(Verbosity.Information, System.out, "Writing output to '" + file + "'.", true);
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Opens a debugger session.
	 * @param minVerbosity The minimum level of output to generate. Calls made to output functions that
	 * use a verbosity lower than this value will be ignored.
	 * @param file path of the log file to which debugger output will be written.
	 */
	public static void open(Verbosity minVerbosity, File file)
	{
		if (debugger != null)
			return;

		if (file == null)
			open(minVerbosity);
		else
			debugger = new Debugger(minVerbosity, file);
	}
	
	/**
	 * Opens a debugger session, using an automatically-generated timestamp filename in 'logs/'. 
	 * @param minVerbosity The minimum level of output to generate. Calls made to output functions that
	 * use a verbosity lower than this value will be ignored.
	 */
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
	
	/**
	 * Opens a debugger session with a minimum verbosity of <code>Information</code>.
	 * @param file path of the log file to which debugger output will be written.
	 */
	public static void open(File file)
	{
		open(Verbosity.Information,file);
	}
	
	/**
	 * Opens a debugger session with a minimum verbosity of <code>Information</code>,
	 * using an automatically-generated timestamp filename in 'logs/'. 
	 */
	public static void open()
	{
		open(Verbosity.Information);
	}
	
	/**
	 * Get the current time as a formatted string.
	 * @return The current system time, formatted as [HH:mm:ss].
	 */
	public static String getTimestamp()
	{
		return dateFormat.format(new Date());
	}

	/**
	 * Outputs a line of debugging information with a verbosity level of <code>Verbose</code>. Console output is done on stdout.
	 * @param s The string to output.
	 */
	public static void v(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Verbose, System.out, s);
	}
	
	/**
	 * Outputs a line of debugging information with a verbosity level of <code>Information</code>. Console output is done on stdout.
	 * @param s The string to output.
	 */
	public static void i(String s)
	{
		if (debugger != null)	
			debugger.log(Verbosity.Information, System.out, s);
	}
	
	/**
	 * Outputs a line of debugging information with a verbosity level of <code>Warning</code>. Console output is done on stdout.
	 * @param s The string to output.
	 */
	public static void w(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Warning, System.out, s);
	}
	
	/**
	 * Outputs a line of debugging information with a verbosity level of <code>Error</code>. Console output is done on stderr.
	 * @param s The string to output.
	 */
	public static void e(String s)
	{
		if (debugger != null)
			debugger.log(Verbosity.Error, System.err, s);
	}
	
	/**
	 * Outputs debugging information about an Exception (with a verbosity level of <code>Exception</code>). Console output is done on stderr.
	 * @param e The exception that was thrown.
	 */
	public static void ex(Exception e)
	{
		if (debugger != null)
			debugger.log(Verbosity.Exception,System.err,
					e.getClass().getName() + ": " + e.getMessage());
	}
	
	/**
	 * Outputs debugging information about an SQLException (with a verbosity level of <code>Exception</code>). Console output is done on stderr.
	 * @param e The SQLException that was thrown.
	 */
	public static void ex(SQLException e)
	{
		if (debugger != null)
			debugger.log(Verbosity.Exception,System.err,
				e.getClass().getName() + ": " + e.getMessage() + " (SQLState: " + e.getSQLState() +", VendorError: " + e.getErrorCode() + ")");
	}
	
	/**
	 * Closes the debugger,
	 * flushing file output and no longer logging output to stdout/stderr.
	 */
	public static void close() 
	{
		if (debugger == null)
			return;
		debugger.dispose();
		debugger = null;		
	}
	
	/**
	 * Adds a new event listener.
	 * @param listener subscribes an event listener to this object's state events.
	 */
	public static final void addEventListener(DebuggerEventListener listener)
	{
		if (listener == null || listeners.contains(listener))
			return;
		listeners.add(listener);
	}
	
	/**
	 * Removes an existing event listener. 
	 * @param listener unsubscribes an event listener from this object's state events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public static final void removeEventListener(DebuggerEventListener listener)
	{
		if (listener == null)
			return;
		listeners.remove(listener);
	}
	
	/**
	 * Unsubscribes all event listeners from this object's state events.
	 */
	public static final void clearEventListeners()
	{
		listeners.clear();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private void log(final Verbosity level, PrintStream stream, String text, boolean force)
	{
		if (!force && level.compareTo(minVerbosity) < 0)
			return;
		
		final String timestamp = getTimestamp();
		final String s = (text == null ? "" : text.trim());
		final String message = timestamp + " " + s;
		if (writer != null)
		{
			try { writer.write(message + "\n"); }
			catch (IOException e) { }
		}
		stream.println(message);
		
		SwingUtilities.invokeLater(new Runnable() {
		     public void run()
		     {
		         for (DebuggerEventListener listener : listeners)
		         {
		        	 try
		        	 {
		        		 listener.debuggerLoggedText(level, timestamp, s);
		        	 }
		        	 catch (Exception e)
		        	 {
		        		 System.err.println(e.getClass().getName() + ": " + e.getMessage());
		        	 }
		         }
		     }
		 });
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