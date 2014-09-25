package wifindus.eye.dispatcher;

import javax.swing.SwingUtilities;
import wifindus.eye.EyeApplication;

public class Dispatcher extends EyeApplication
{
	private static final long serialVersionUID = 12094147960785467L;

	public Dispatcher(String[] args)
	{
		super(args);

	}

	//do not modify main :)
	public static void main(final String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	new Dispatcher(args)
		    	.setTitle("WiFindUs Dispatcher");
		    }
		});
	}
}