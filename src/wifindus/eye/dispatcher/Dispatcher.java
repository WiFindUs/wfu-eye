package wifindus.eye.dispatcher;

import wifindus.eye.EyeApplication;

public class Dispatcher extends EyeApplication
{
	public Dispatcher(String[] args)
	{
		super(args);
	}
	
	
	
	
	
	public static void main(String[] args)
	{
		Dispatcher dispatcher = new Dispatcher(args);
		dispatcher.dispose();
	}
}
