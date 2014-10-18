package wifindus.eye.server;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import wifindus.Debugger;
import wifindus.DebuggerPanel;
import wifindus.ParsedUDPPacket;
import wifindus.eye.EyeApplication;
import wifindus.eye.Hash;

/**
 * A specialized form of {@link EyeApplication} that processes incoming
 * UDP packets from client devices, adds them to a MySQL database, and
 * informs TCP-connected {@link wifindus.eye.dispatcher.Dispatcher} instances that they must update.
 * @author Mark 'marzer' Gillard
 */
public class Server extends EyeApplication
{
	private static final long serialVersionUID = -6202164296309727570L;
	private volatile DatagramSocket udpListenSocket;
	private transient volatile boolean abortThreads = false;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new Server.
	 * @param args The command-line arguments used to launch the application, as provided by main.
	 */
	public Server(String[] args)
	{
		super(args, true);
		
		int udpListenPort = getConfig().getInt("server.udp_port");
		Debugger.i("Opening listener UDP socket on port " + udpListenPort + "...");
		try
		{
			udpListenSocket = new DatagramSocket(udpListenPort);
			udpListenSocket.setSoTimeout(1000);
			Debugger.i("Socket opened OK.");
		}
		catch (SocketException e)
		{
			Debugger.ex(e);
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public void windowClosing(WindowEvent e)
	{
		abortThreads = true;
		if (udpListenSocket != null)
			udpListenSocket.close();
		super.windowClosing(e);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void preDebugHooks()
	{
		super.preDebugHooks();
		this.getContentPane().add(new DebuggerPanel());
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private class UDPListenWorker implements Runnable
	{
		@Override
		public void run()
		{
			byte[] buffer = new byte[1024];
			
			while (!abortThreads)
			{
				//wait for incoming data
				DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
				try
				{
					udpListenSocket.receive(receivePacket);
				}
				catch (SocketTimeoutException e)
				{
					if (abortThreads)
						return;
					continue;
				}
				catch (IOException e)
				{
					Debugger.ex(e);
					continue;
				}
				
				//parse data out into packets
				ParsedUDPPacket parsedPacket = new ParsedUDPPacket(receivePacket);
				String messageType = parsedPacket.getData().get("type");
				if (messageType == null)
					continue;
				messageType = messageType.toLowerCase();
				
				//handle packet types
				switch (messageType)
				{
					case "NODE": processNodePacket(parsedPacket); break;
					case "DEVICE": processDevicePacket(parsedPacket); break;
				}
			}
		}
		
		private void processNodePacket(ParsedUDPPacket packet)
		{
			//sanity checks
			if (packet == null)
				return;
			String hash = getPacketHash(packet);
			if (hash == null)
				return;
		}
		
		private void processDevicePacket(ParsedUDPPacket packet)
		{
			if (packet == null)
				return;
			String hash = getPacketHash(packet);
			if (hash == null)
				return;
		}
		
		private String getPacketHash(ParsedUDPPacket packet)
		{
			if (packet == null)
				return null;
			String hash = packet.getData().get("hash");
			if (hash == null || !Hash.isValid(hash))
				return null;
			return hash;
		}
	};
	
	/////////////////////////////////////////////////////////////////////
	// MAIN - DO NOT MODIFY
	/////////////////////////////////////////////////////////////////////
	
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
