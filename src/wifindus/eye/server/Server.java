package wifindus.eye.server;

import wifindus.ConfigFile;

public class Server
{
	private ConfigFile config = null;
	
	public Server()
	{
		ReadConfigFiles();
		ConnectMySQL();
		MessageLoop();
		DisconnectMySQL();
	}
	
	private void ReadConfigFiles()
	{
		//TODO: read config files from the working directory
	}
	
	private void ConnectMySQL()
	{
		//TODO: connect to the mysql server
	}
	
	private void MessageLoop()
	{
		//TODO: listen for new connections
		//TODO: send new incidents to 
	}
	
	private void DisconnectMySQL()
	{
		//TODO: disconnect from the mysql server
	}
	
	public static void main(String[] args)
	{
		
	}
}
