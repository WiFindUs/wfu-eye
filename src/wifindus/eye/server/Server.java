package wifindus.eye.server;

import java.io.File;
import wifindus.ConfigFile;

public class Server
{
	private ConfigFile config = null;
	
	public Server()
	{
		config = new ConfigFile(new File("eye-server.conf"));
		System.out.println(config);
		ConnectMySQL();
		MessageLoop();
		DisconnectMySQL();
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
		Server server = new Server();
	}
}
