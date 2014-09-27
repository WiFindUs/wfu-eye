package wifindus.eye;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import wifindus.MySQLResultRow;
import wifindus.MySQLResultSet;

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
				Debugger.i("Found '"+args[i]+"'.");
				continue;
			}
		}
		
		//use eye.conf as default if none were supplied
		if (configFiles.size() == 0)
		{
			Debugger.i("None found, using default 'eye.conf'.");
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
			Debugger.i("Connected OK.");
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
	
	@Override
	public void deviceCreated(Device device)
	{
		Debugger.v(device + " created");
	}
	
	@Override
	public void userCreated(User user)
	{
		Debugger.v(user + " created");
	}
	
	@Override
	public void incidentCreated(Incident incident)
	{
		Debugger.v(incident + " created");
	}
	
	@Override
	public void nodeCreated(Node node)
	{
		Debugger.v(node + " created");
	}
	
	@Override
	public void deviceInUse(Device device, User user)
	{
		Debugger.v(user + " logged into " + device);
	}
	
	@Override
	public void deviceNotInUse(Device device, User user)
	{
		Debugger.v(user + " logged out of " + device);
	}
	
	@Override
	public void deviceLocationChanged(Device device)
	{
		Debugger.v(device + " location data changed: " + device.getLocation());
	}
	
	@Override
	public void deviceAtmosphereChanged(Device device)
	{
		Debugger.v(device + " atmospheric data changed: " + device.getAtmosphere());
	}
	
	@Override
	public void deviceUpdated(Device device)
	{
		Debugger.v(device + " updated: " + device.getLastUpdate());
	}
	
	/////////////////////////////////////////////////////////////////////
	// UNIMPLEMENTED INTERFACE METHODS
	/////////////////////////////////////////////////////////////////////

	//DeviceEventListener
	@Override public void deviceTimedOut(Device device) { }
	@Override public void deviceAddressChanged(Device device) { }
	@Override public void deviceAssignedIncident(Device device, Incident incident) { }
	@Override public void deviceUnassignedIncident(Device device, Incident incident) { }
	
	//IncidentEventListener
	@Override public void incidentArchived(Incident incident) { }
	
	//NodeEventListener
	@Override public void nodeTimedOut(Node node) { }
	@Override public void nodeLocationChanged(Node node) { }
	@Override public void nodeVoltageChanged(Node node) { }
	@Override public void nodeUpdated(Node node) { }
	@Override public void nodeAddressChanged(Node node) { }

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
				MySQLResultSet results = (MySQLResultSet)kvp[1];
				
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
	
	private void processUsers(MySQLResultSet results)
	{
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			Integer id = (Integer)entry.getKey();
			User user = users.get(id);
			if (user == null)
			{
				user = new User(entry.getValue(), this);
				users.put(id, user);
			}
			user.update(entry.getValue());
		}
	}
	
	
	private void processDevices(MySQLResultSet results)
	{
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			String hash = (String)entry.getKey();
			Device device = devices.get(hash);
			if (device == null)
			{
				device = new Device(hash, (Device.Type)(entry.getValue().get("deviceType")), this);
				devices.put(hash, device);
			}
			device.update(entry.getValue());	
		}
	}

	private void processDeviceUsers(MySQLResultSet results)
	{
		//create a list of all the devices
		ArrayList<Device> userlessDevices = new ArrayList<Device>(
				devices.values());
		
		//process entries from database (logins)
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			Device device = devices.get((String)entry.getValue().get("deviceHash"));
			if (device != null)
			{
				userlessDevices.remove(device);
				device.updateUser(users.get((Integer)entry.getValue().get("userID")));
			}
		}
		
		//handle unused devices (logouts)
		for (Device d : userlessDevices)
			d.updateUser(null);
	}

	
	private void processNodes(MySQLResultSet results)
	{
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			String hash = (String)entry.getKey();
			Node node = nodes.get(hash);
			if (node == null)
			{
				node = new Node(hash, this);
				nodes.put(hash, node);
			}
			node.update(entry.getValue());	
		}
	}
	
	private void processIncidents(MySQLResultSet results)
	{
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			Integer id = (Integer)entry.getKey();
			Incident incident = incidents.get(id);
			if (incident == null)
			{
				Double accuracy = entry.getValue().get("accuracy") == null ? null : (Double)entry.getValue().get("accuracy");
				Double altitude = entry.getValue().get("altitude") == null ? null : (Double)entry.getValue().get("altitude");
				incident = new Incident(id.intValue(),
					(Incident.Type)(entry.getValue().get("incidentType")),
					new Location(
						(Double)entry.getValue().get("latitude"),
						(Double)entry.getValue().get("longitude"),
						accuracy,
						altitude
					),
					(Timestamp)(entry.getValue().get("created")),
					this);
				incidents.put(id, incident);
			}
			incident.update(entry.getValue());
		}
	}
}
