package wifindus.eye.server;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import wifindus.Debugger;
import wifindus.DebuggerPanel;
import wifindus.MySQLResultRow;
import wifindus.ParsedUDPPacket;
import wifindus.eye.Device;
import wifindus.eye.DeviceEventListener;
import wifindus.eye.EyeApplication;
import wifindus.eye.EyeMySQLConnection;
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
		
		//open udp socket
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
		
		//launch udp listener thread
		Thread udpListenThread = new Thread(new UDPListenWorker());
		udpListenThread.start();
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
			while (!abortThreads)
			{
				//wait for incoming data
				byte[] buffer = new byte[1024];
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
				catch (SocketException e)
				{
					if (!abortThreads)
						Debugger.ex(e);
					return;
				}
				catch (IOException e)
				{
					Debugger.ex(e);
					continue;
				}
				
				//parse data out into packets
				Debugger.v("UDP message received: " + new String(buffer).trim());
				ParsedUDPPacket parsedPacket = new ParsedUDPPacket(receivePacket);
				String messageType = parsedPacket.getData().get("type");
				if (messageType == null)
					continue;
				messageType = messageType.toLowerCase();
				
				//handle packet types
				switch (messageType)
				{
					case "node": processNodePacket(parsedPacket); break;
					case "device": processDevicePacket(parsedPacket); break;
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
			//sanity checks
			if (packet == null)
				return;
			String hash = getPacketHash(packet);
			if (hash == null)
				return;
			
			//build temporary dataset
			Map <String, String> tempMap = new HashMap<>(); 
			tempMap.put("hash", "'" + hash + "'");
			if (packet.getData().containsKey("devicetype"))
				tempMap.put("deviceType", "'" + packet.getData().get("devicetype").toUpperCase() + "'");
			if (packet.getData().containsKey("latitude"))
				tempMap.put("latitude", packet.getData().get("latitude"));
			if (packet.getData().containsKey("longitude"))
				tempMap.put("longitude", packet.getData().get("longitude"));
			if (packet.getData().containsKey("altitude"))
				tempMap.put("altitude", packet.getData().get("altitude"));
			if (packet.getData().containsKey("accuracy"))
				tempMap.put("accuracy", packet.getData().get("accuracy"));
			if (packet.getData().containsKey("humidity"))
				tempMap.put("humidity", packet.getData().get("humidity"));
			if (packet.getData().containsKey("airpressure"))
				tempMap.put("airPressure", packet.getData().get("airpressure"));
			if (packet.getData().containsKey("temperature"))
				tempMap.put("temperature", packet.getData().get("temperature"));
			if (packet.getData().containsKey("lightlevel"))
				tempMap.put("lightLevel", packet.getData().get("lightLevel"));
			tempMap.put("address", "'" + packet.getSourceAddress().getHostAddress() + "'");
			tempMap.put("lastUpdate", "NOW()");
			
			//check if record exists, execute appropriate query
			EyeMySQLConnection mysql = getMySQL();
			try
			{
				MySQLResultRow deviceRow = mysql.fetchSingleDevice(hash);
				String query;
				boolean first = true;
				boolean update = false;

				//if not, use an INSERT query
				if (deviceRow == null)
				{
					//build query string
					query = "INSERT INTO Devices (";
					String values = "VALUES (";

					for (Map.Entry< String, String > entry : tempMap.entrySet())
					{
						query += (!first ? ", " : "") + entry.getKey();
						values += (!first ? ", " : "") + entry.getValue();
						first = false;
					}
					query += ") " + values + ")"; 
				}
				else //UPDATE query
				{
					query = "UPDATE Devices SET ";
					
					for (Map.Entry< String, String > entry : tempMap.entrySet())
					{
						if (!first)
							query += ", ";
						query += entry.getKey() + "=" + entry.getValue();							
						first = false;
					}
					query += " WHERE hash='" + hash + "'";
					
					update = true;
				}
				
				//execute sql
				PreparedStatement statement = mysql.prepareStatement(query);
				if (update)
					statement.executeUpdate();
				else
					statement.execute();
				
				//release statement
				mysql.release(statement);
				
			}
			catch (SQLException e)
			{
				Debugger.ex(e);
			}
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
		
		private String nullable(String value)
		{
			if (!value.toUpperCase().equals("NULL"))
				value = "'" + value + "'";
			return value;
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
