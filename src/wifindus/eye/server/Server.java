package wifindus.eye.server;

import wifindus.eye.EyeObject;

public class Server extends EyeObject
{
	/*
	private volatile int clientPort = 33339;
	private volatile int dispatcherPortStart = 33339;
	private volatile int dispatcherPortCount = 33339;
	*/	
	
	public Server(String[] args)
	{
		super(args);
	}
	
	/*
	private void LoadConfig()
	{
		//load settings if possible
		ConfigFile config = new ConfigFile(new File("eye.conf"));
		
		//assigns & sanity checking
		clientPort = config.getInt("clientPort", 33339);
		if (clientPort <= 1024)
			clientPort = 33339;
		dispatcherPortStart = config.getInt("dispatcherPortStart", 33340);
		if (dispatcherPortStart <= 1024 || dispatcherPortStart == clientPort)
			dispatcherPortStart = 33340;
		dispatcherPortCount = config.getInt("dispatcherPortCount", 5);
		if (dispatcherPortCount <= 0)
			dispatcherPortStart = 3;
		
		System.out.println(config);
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
	*/
	
	public static void main(String[] args)
	{
		Server server = new Server(args);
	}
}
