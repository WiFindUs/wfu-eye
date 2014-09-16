package wifindus.eye.server;

import java.io.File;
import java.io.FileNotFoundException;
import wifindus.ConfigFile;

public class Server
{
	private volatile int clientPort = 33339;
	private volatile int dispatcherPortStart = 33339;
	private volatile int dispatcherPortCount = 33339;
	
	
	public Server()
	{
		LoadConfig();
		ConnectMySQL();
		SpawnThreads();
		DisconnectMySQL();
	}
	
	private void LoadConfig()
	{
		//load settings if possible
		ConfigFile config = null;
		try
		{
			config = new ConfigFile(new File("eye-server.conf"));
		}
		catch (FileNotFoundException e)
		{
			System.err.println(e.getMessage()+"\nUsing default settings...");
			config = new ConfigFile();
		}
		
		//assigns & sanity checking
		clientPort = config.get("clientPort", 33339);
		if (clientPort <= 1024)
			clientPort = 33339;
		dispatcherPortStart = config.get("dispatcherPortStart", 33340);
		if (dispatcherPortStart <= 1024 || dispatcherPortStart == clientPort)
			dispatcherPortStart = 33340;
		dispatcherPortCount = config.get("dispatcherPortCount", 5);
		if (dispatcherPortCount <= 0)
			dispatcherPortStart = 3;
	}
	
	private void ConnectMySQL()
	{
		//TODO: connect to the mysql server
	}
	
	private void SpawnThreads()
	{
		//TODO: start threads for TCP connections (dispatchers)
		//TODO: start thread for UDP listener (clients)
	}
	
	private void DisconnectMySQL()
	{
		//TODO: disconnect from the mysql server
	}
	
	public static void main(String[] args)
	{
		Server server = new Server();
	}
}
