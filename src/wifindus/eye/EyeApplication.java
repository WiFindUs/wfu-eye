package wifindus.eye;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import wifindus.ConfigFile;
import wifindus.Debugger;
import wifindus.MySQLConnection;

/**
 * A general base for Eye applications that need to maintain a connection
 * to a MySQL database and respond to changes made to Users, Devices, etc.
 * @author Mark 'marzer' Gillard
 */
public abstract class EyeApplication extends JFrame implements DeviceEventListener, NodeEventListener, UserEventListener, IncidentEventListener, WindowListener
{
	//properties
	private static final long serialVersionUID = -3410394016911856177L;
	private static final Pattern PATTERN_VERBOSITY = Pattern.compile( "^-([0-4])$" );
	private volatile ConfigFile config = null;
	private volatile boolean abortThreads = false;
	private volatile MySQLUpdateWorker mysqlWorker = null;
	private EyeMySQLConnection mysql = new EyeMySQLConnection();
	//database structures
	private volatile ConcurrentHashMap<String,Device> devices = new ConcurrentHashMap<>();
	private volatile ConcurrentHashMap<String,Node> nodes = new ConcurrentHashMap<>();
	private volatile ConcurrentHashMap<Integer,Incident> incidents = new ConcurrentHashMap<>();
	private volatile ConcurrentHashMap<Integer,User> users = new ConcurrentHashMap<>();
	

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new EyeApplication.
	 * @param args The command-line arguments used to launch the application, as provided by main.
	 * The following parameters are currently accepted:
	 * <ul>
	 * <li><code>-conf <em>filename</em></code>: specifies a configuration file to load.
	 * This argument may be provided more than once, with each listed config file being loaded into 
	 * one {@link wifindus.ConfigFile} instance. If this argument is omitted, <em>eye.conf</em> is assumed.</li>
	 * <li><code>-0 <em>to</em> -4</code>: specifies the minimum verbosity of {@link wifindus.Debugger} output,
	 * from <code>Verbose (0)</code>, to <code>Exception (4)</code>.
	 * If this parameter is omitted, -1 is assumed.</li>
	 * </ul>
	 * @throws NullPointerException if <code>args</code> is null
	 */
	public EyeApplication(String[] args)
	{
		if (args == null)
			throw new NullPointerException("Parameter 'args' cannot be null.");
		
		//set frame properties
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setVisible(true);
		
		//check for debugger verbosity flags & start debugger
		Debugger.Verbosity verbosity = Debugger.Verbosity.Information;
		for (int i = 0; i < args.length; i++)
		{
			Matcher match = PATTERN_VERBOSITY.matcher(args[i]);
			if (!match.matches())
				continue;
			verbosity = Debugger.Verbosity.values()[Integer.parseInt(match.group(1))];
		}
		Debugger.open(verbosity);
		
		//parse command line arguments for config parameters
		Debugger.i("Parsing command line arguments for config files...");
		List<File> configFiles = new ArrayList<>();
		for (int i = 0; i < args.length-1; i++)
		{
			if (!args[i].substring(0, 1).equals("-"))
				continue;
			if (args[i].equalsIgnoreCase("-conf"))
			{
				configFiles.add(new File(args[++i]));
				Debugger.i("    Found '"+args[i]+"'.");
				continue;
			}
		}
		
		//use eye.conf as default if none were supplied
		if (configFiles.size() == 0)
		{
			Debugger.i("    None found, using default 'eye.conf'.");
			configFiles.add(new File("eye.conf"));
		}
		
		//try loading files
		config = new ConfigFile(configFiles);
		Debugger.v("Parsed configuration: " + config);
		
		//ensure required keys are present and valid, enforce defaults if not
		//mysql
		config.defaultString("mysql.username", "root");
		config.defaultString("mysql.address", "localhost");
		config.defaultInt("mysql.port", 3306, 1024, 65535);
		config.defaultString("mysql.database", "wfu_eye_db");
		config.defaultInt("mysql.update_interval", 3000, 1000, 30000);
		//server
		config.defaultInt("server.udp_port", 33339, 1024, 65535);
		config.defaultInt("server.tcp_port", 33340, 1024, 65535);
		config.defaultInt("server.tcp_count", 33340, 1, 5);
		//dispatcher
		config.defaultInt("dispatcher.tcp_port", 33340, 1024, 65535);
		
		//connect to mysql
		Debugger.i("Connecting to MySQL database '" + config.getString("mysql.database") + "@"
				+ config.getString("mysql.address") + ":" + config.getInt("mysql.port") + "...");
		try
		{			
			mysql.connect(config.getString("mysql.address"),
					config.getInt("mysql.port"),
					config.getString("mysql.database"),
					config.getString("mysql.username"),
					config.getString("mysql.password"));
			Debugger.i("    Connected OK.");
		}
		catch (Exception e)
		{
			Debugger.ex(e);
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		
		//create and launch mysql worker task
		mysqlWorker = new MySQLUpdateWorker(config.getInt("mysql.update_interval"));
		mysqlWorker.execute();
		

	}

	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	//WindowListener
	@Override public void windowClosing(WindowEvent e)
	{
		Debugger.i("Cleaning up...");
		mysql.disconnect();
		abortThreads = true;
		Debugger.close();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS (UNIMPLEMENTED INTERFACE METHODS)
	/////////////////////////////////////////////////////////////////////

	//DeviceEventListener
	@Override public void deviceCreated(Device device) { }
	@Override public void deviceTimedOut(Device device) { }
	@Override public void deviceInUse(Device device, User user) { }
	@Override public void deviceNotInUse(Device device, User user) { }
	@Override public void deviceLocationChanged(Device device) { }
	@Override public void deviceAtmosphereChanged(Device device) { }
	@Override public void deviceAddressChanged(Device device) { }
	@Override public void deviceUpdated(Device device) { }
	@Override public void deviceAssignedIncident(Device device, Incident incident) { }
	@Override public void deviceUnassignedIncident(Device device, Incident incident) { }
	
	//IncidentEventListener
	@Override public void incidentCreated(Incident incident) { }
	@Override public void incidentArchived(Incident incident) { }
	
	//NodeEventListener
	@Override public void nodeCreated(Node node) { }
	@Override public void nodeTimedOut(Node node) { }
	@Override public void nodeLocationChanged(Node node) { }
	@Override public void nodeVoltageChanged(Node node) { }
	@Override public void nodeUpdated(Node node) { }
	@Override public void nodeAddressChanged(Node node) { }
	
	//UserEventListener
	@Override public void userCreated(User user) { }
	
	//WindowListener
	@Override public void windowOpened(WindowEvent e) { }
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }

	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private class MySQLUpdateWorker extends SwingWorker<Void, Object[]>
	{
		private int interval = 1000;
		
		public MySQLUpdateWorker(int interval)
		{
			this.interval = interval;
		}
		
		@Override
		protected Void doInBackground() throws Exception
		{
			while (!abortThreads)
			{
				//users
				try
				{
					publish(new Object[] { "Users", mysql.fetchUsers() });
					Thread.sleep(100);
				}
				catch (SQLException e)
				{
					Debugger.ex(e);
				}
				if (abortThreads)
					break;
				
				//devices
				try
				{
					publish(new Object[] { "Devices", mysql.fetchDevices() });
					Thread.sleep(100);
				}
				catch (SQLException e)
				{
					Debugger.ex(e);
				}
				if (abortThreads)
					break;
				
				//device users
				try
				{
					publish(new Object[] { "DeviceUsers", mysql.fetchDeviceUsers() });
					Thread.sleep(100);
				}
				catch (SQLException e)
				{
					Debugger.ex(e);
				}
				if (abortThreads)
					break;
				
				//nodes
				try
				{
					publish(new Object[] { "Nodes", mysql.fetchNodes() });
					Thread.sleep(100);
				}
				catch (SQLException e)
				{
					Debugger.ex(e);
				}
				if (abortThreads)
					break;
				
				//incidents
				try
				{
					publish(new Object[] { "Incidents", mysql.fetchIncidents() });
					Thread.sleep(100);
				}
				catch (SQLException e)
				{
					Debugger.ex(e);
				}
				if (abortThreads)
					break;
				
				//sleep for the interval, but do so in short chunks
				//so that we can terminate quickly if necessary
				int counter = 0;
				while (!abortThreads && counter < interval)
				{
					Thread.sleep(100);
					counter += 100;
				}
				
			}
			return null;
		}
		
		@Override
		protected void process(List< Object[] > chunks)
		{
			for (Object[] kvp : chunks)
			{
				String table = (String)kvp[0];
				ResultSet results = (ResultSet)kvp[1];
				
				switch (table)
				{
					case "Users": processUsers(results); break;
					case "Devices": processDevices(results); break;
					case "DeviceUsers": processDeviceUsers(results); break;
					case "Nodes": processNodes(results); break;
					case "Incidents": processIncidents(results); break;
					
				}
			}
		}
	};
	
	private void processUsers(ResultSet results)
	{
		try
		{
			while (results.next())
			{
				Debugger.v("Database: User["+results.getInt("userID")+"]");
				
			}
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
		}
		mysql.release(results);
	}
	
	private void processDevices(ResultSet results)
	{
		try
		{
			while (results.next())
			{
				Debugger.v("Database: Device[\""+results.getString("hash")+"\"]");
				
			}
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
		}
		mysql.release(results);
	}
	
	private void processDeviceUsers(ResultSet results)
	{
		try
		{
			while (results.next())
			{
				Debugger.v("Database: DeviceUser[\""+results.getString("deviceHash")+"\", "+results.getInt("userID")+"]");
				
			}
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
		}
		mysql.release(results);
	}
	
	private void processNodes(ResultSet results)
	{
		try
		{
			while (results.next())
			{
				Debugger.v("Database: Node[\""+results.getString("hash")+"\"]");
				
			}
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
		}
		mysql.release(results);
	}
	
	private void processIncidents(ResultSet results)
	{
		try
		{
			while (results.next())
			{
				Debugger.v("Database: Incident["+results.getInt("incidentID")+"]");
				
			}
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
		}
		mysql.release(results);
	}
	
	
	/*
	@SuppressWarnings("unused")
	private class TCPListenThread implements Runnable
	{
		private int port = 33340;
		
		public TCPListenThread(int port)
		{
			this.port = port;
		}
		
		@Override
		public void run()
		{
			if (abortThreads)
				return;
		}		
	}
	*/
}
