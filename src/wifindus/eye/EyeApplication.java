package wifindus.eye;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import wifindus.ConfigFile;
import wifindus.Debugger;
import wifindus.MySQLConnection;

public class EyeApplication implements Closeable
{
	private volatile ConfigFile config = null;
	private volatile boolean abortThreads = false;
	private volatile List<Thread> threads = new ArrayList<>();
	private MySQLConnection mysql = new MySQLConnection();
	private boolean closed = false;
	private static boolean active = false;
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	public EyeApplication(String[] args)
	{
		if (active)
			throw new IllegalStateException("You may not have more than one instance of EyeApplication at once.");
		active = true;
		
		//start debugger
		Debugger.open();
		
		Debugger.i("Parsing command line arguments for config files...");
		//parse command line arguments for config parameters
		List<File> configFiles = new ArrayList<>();
		if (args != null && args.length > 0)
		{
			
			for (int i = 0; i < args.length-1; i++)
			{
				if (!args[i].substring(0, 1).equals("-"))
					continue;
				if (args[i].equalsIgnoreCase("-conf"))
				{
					configFiles.add(new File(args[++i]));
					Debugger.i("    Found '"+args[i]+"'.");
					continue;
				}
			}
		}
		
		//use eye.conf as default if none were supplied
		if (configFiles.size() == 0)
		{
			Debugger.i("    None found, using default 'eye.conf'.");
			configFiles.add(new File("eye.conf"));
		}
		
		//try loading files
		config = new ConfigFile(configFiles);
		
		//ensure required keys are present and valid, enforce defaults if not
		//mysql
		config.setString("mysql.username", config.getString("mysql.username", "root"));
		config.setString("mysql.address", config.getString("mysql.address", "localhost"));
		config.setInt("mysql.port", Math.min(Math.max(config.getInt("mysql.port", 3306),1024),65535));
		config.setString("mysql.database", config.getString("mysql.database", "wfu_eye_db"));
		//server
		config.setInt("server.udp_port", Math.min(Math.max(config.getInt("server.udp_port", 33339),1024),65535));
		config.setInt("server.tcp_port", Math.min(Math.max(config.getInt("server.tcp_port", 33340),1024),65535));
		config.setInt("server.tcp_count", Math.min(Math.max(config.getInt("server.tcp_count", 33340),1),5));
		//dispatcher
		config.setInt("dispatcher.tcp_port", Math.min(Math.max(config.getInt("dispatcher.tcp_port", 33340),1024),65535));
		
		//connect to mysql
		Debugger.i("Connecting to MySQL database '" + config.getString("mysql.database") + "@"
				+ config.getString("mysql.address") + ":" + config.getInt("mysql.port") + "...");
		try
		{			
			mysql.connect(config.getString("mysql.address"),
					config.getInt("mysql.port"),
					config.getString("mysql.database"),
					config.getString("mysql.username"),
					config.getString("mysql.password"));
		}
		catch (Exception e)
		{
			Debugger.ex(e);
		    closeInternal();
		    System.exit(1);
		}
	}
	
	public void close() throws IOException
	{
		closeInternal();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private class TCPListenThread implements Runnable
	{
		@Override
		public void run()
		{
			
		}		
	}
	
	//this is merely to provide an internal close() without requiring Closeable's throwing of IOException
	private void closeInternal()
	{
		if (closed)
			return;
		Debugger.i("Cleaning up...");
		mysql.disconnect();
		abortThreads = true;
		for (Thread thread : threads) 
		{
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				//
			}
		}
		threads.clear();
		closed = true;
		Debugger.close();
	}
}
