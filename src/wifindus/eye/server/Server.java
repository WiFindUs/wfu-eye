package wifindus.eye.server;

import wifindus.eye.EyeApplication;

public class Server extends EyeApplication
{
	public Server(String[] args)
	{
		super(args);
	}
	
	public static void main(String[] args)
	{
		Server server = new Server(args);
		
		
		server.dispose();
	}
}
