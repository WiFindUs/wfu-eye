package wifindus.eye.server;

import java.io.IOException;
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
		try	{ server.close(); }
		catch (IOException e) { }
	}
}
