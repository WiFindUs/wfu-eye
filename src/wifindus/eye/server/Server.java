package wifindus.eye.server;

import javax.swing.SwingUtilities;
import wifindus.DebuggerPanel;
import wifindus.eye.EyeApplication;

/**
 * A specialized form of {@link EyeApplication} that processes incoming
 * UDP packets from client devices, adds them to a MySQL database, and
 * informs TCP-connected {@link wifindus.eye.dispatcher.Dispatcher} instances that they must update.
 * @author Mark 'marzer' Gillard
 */
public class Server extends EyeApplication
{
	private static final long serialVersionUID = -6202164296309727570L;

	/**
	 * Creates a new Server.
	 * @param args The command-line arguments used to launch the application, as provided by main.
	 */
	public Server(String[] args)
	{
		super(args, false);
		getContentPane().add(new DebuggerPanel());
	}
	
	/**
	 * Application entry-point.
	 * <strong>Do not modify this function.</strong>
	 * @param args The command-line arguments used to launch the application.
	 */
	public static void main(final String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	new Server(args)
		    	.setTitle("WiFindUs Server");
		    }
		});
	}
}
