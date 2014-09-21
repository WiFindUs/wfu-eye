package wifindus.eye.dispatcher;

import java.io.IOException;
import wifindus.eye.EyeApplication;

public class Dispatcher extends EyeApplication
{
	public Dispatcher(String[] args)
	{
		super(args);
	}

	//do not modify main :)
	public static void main(String[] args)
	{
		Dispatcher dispatcher = new Dispatcher(args);
		try	{ dispatcher.close(); }
		catch (IOException e) { }
	}
}
