package wifindus.eye;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import wifindus.ConfigFile;

public class EyeObject
{
	private volatile ConfigFile config = null;
	private List<Thread> activeThreads = new ArrayList<>();
	
	public EyeObject(String[] args)
	{
		//parse command line arguments for config parameters
		List<File> configFiles = new ArrayList<>();
		if (args != null && args.length > 0)
		{
			for (int i = 0; i < args.length-1; i++)
			{
				if (args[i].substring(0, 1) != "-")
					continue;

				String arg = args[i].toLowerCase();
				if (arg == "-conf")
				{
					configFiles.add(new File(args[++i]));
					continue;
				}
			}
		}
		
		//use eye.conf as default if none were supplied
		if (configFiles.size() == 0)
			configFiles.add(new File("eye.conf"));
		
		//try loading files
		config = new ConfigFile(configFiles);
		
		//ensure require keys are present and valid, enforce defaults if not
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
		
		System.out.println(config);
	}
	
	private class MySQLThread implements Runnable
	{
		@Override
		public void run()
		{
			
		}		
	};
}
