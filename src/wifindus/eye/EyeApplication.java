package wifindus.eye;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import wifindus.ConfigFile;
import wifindus.Debugger;
import wifindus.DebuggerFrame;
import wifindus.MySQLResultRow;
import wifindus.MySQLResultSet;

/**
 * A general base for Eye applications that need to maintain a connection
 * to a MySQL database and respond to changes made to Users, Devices, etc.
 * @author Mark 'marzer' Gillard
 */
public abstract class EyeApplication extends JFrame
	implements EyeApplicationListener, DeviceEventListener, NodeEventListener, UserEventListener, IncidentEventListener, WindowListener
{
	//properties
	private static final long serialVersionUID = -3410394016911856177L;
	private transient static final Pattern PATTERN_VERBOSITY = Pattern.compile( "^-([0-4])$" );
	private transient static final Pattern PATTERN_CONSOLE = Pattern.compile( "^-console?$", Pattern.CASE_INSENSITIVE);
	private volatile ConfigFile config = null;
	private transient volatile boolean abortThreads = false;
	private transient volatile MySQLUpdateWorker mysqlWorker = null;
	private transient EyeMySQLConnection mysql = new EyeMySQLConnection();
	private transient static EyeApplication singleton;
	private transient volatile CopyOnWriteArrayList<EyeApplicationListener> listeners = new CopyOnWriteArrayList<>();
	//database structures
	private transient volatile ConcurrentHashMap<String,Device> devices = new ConcurrentHashMap<>();
	private transient volatile ConcurrentHashMap<String,Node> nodes = new ConcurrentHashMap<>();
	private transient volatile ConcurrentHashMap<Integer,Incident> incidents = new ConcurrentHashMap<>();
	private transient volatile ConcurrentHashMap<Integer,User> users = new ConcurrentHashMap<>();
	

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
	 * <li><code>-console</code>: causes a debug console window to be spawned at launch.</li>
	 * </ul>
	 * @throws NullPointerException if <code>args</code> is null
	 * @throws IllegalStateException if an existing EyeApplication instance exists.
	 */
	public EyeApplication(String[] args)
	{
		if (args == null)
			throw new NullPointerException("Parameter 'args' cannot be null.");
		if (singleton != null)
			throw new IllegalStateException("An EyeApplication object has already been instantiated..");
		singleton = this;
		
		//set frame properties
		Dimension screenBounds = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize((int)(screenBounds.width * 0.6), (int)(screenBounds.height * 0.6));
		setLocation((int)(screenBounds.width * 0.2),(int)(screenBounds.height * 0.2));
		setVisible(true);
		
		//check for debugger verbosity flags & start debugger
		Debugger.Verbosity verbosity = Debugger.Verbosity.Information;
		boolean spawnConsole = false;
		for (int i = 0; i < args.length; i++)
		{
			Matcher match = PATTERN_VERBOSITY.matcher(args[i]);
			if (match.matches())
			{
				verbosity = Debugger.Verbosity.values()[Integer.parseInt(match.group(1))];
				continue;
			}
			
			match = PATTERN_CONSOLE.matcher(args[i]);
			if (match.matches())
			{
				spawnConsole = true;
				continue;
			}
			
		}
		if (spawnConsole)
		{
			DebuggerFrame debuggerFrame = new DebuggerFrame();
			debuggerFrame.setBounds(20, 20, 800, 300);
			debuggerFrame.setVisible(true);
		}
		Debugger.open(verbosity);
		
		//parse command line arguments for config parameters
		Debugger.i("Parsing command line arguments for config files...");
		List<File> configFiles = new ArrayList<>();
		for (int i = 0; i < args.length-1; i++)
		{
			//skip arguments that do not begin with a dash
			if (!args[i].substring(0, 1).equals("-"))
				continue;
			
			//find config file arguments
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
		//ensure required keys are present and valid, enforce defaults if not
		//mysql
		config.defaultString("mysql.username", "root");
		config.defaultString("mysql.address", "localhost");
		config.defaultInt("mysql.port", 3306, 1024, 65535);
		config.defaultString("mysql.database", "wfu_eye_db");
		config.defaultInt("mysql.update_interval", 1000, 100, 30000);
		//server
		config.defaultInt("server.udp_port", 33339, 1024, 65535);
		config.defaultInt("server.tcp_port", 33340, 1024, 65535);
		config.defaultInt("server.tcp_count", 33340, 1, 5);
		//dispatcher
		config.defaultInt("dispatcher.tcp_port", 33340, 1024, 65535);
		//map
		config.defaultString("map.image", "maps/base.png");
		config.defaultDouble("map.latitude_start", -34.908591);
		config.defaultDouble("map.longitude_start", 138.576943);
		config.defaultDouble("map.latitude_end", -34.919506);
		config.defaultDouble("map.longitude_end", 138.593057);
		//output config
		Debugger.v("Parsed configuration: " + config);
		
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
		
		//attach self as creation listener
		addEventListener(this);
		
		//create and launch mysql worker task
		mysqlWorker = new MySQLUpdateWorker(config.getInt("mysql.update_interval"));
		mysqlWorker.execute();
	}

	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Get the currently instanced EyeApplication.
	 * @return a reference to the currently instantiated EyeApplication object.
	 */
	public static final EyeApplication get()
	{
		return singleton;
	}
	
	/**
	 * Gets the config file object loaded with the application.
	 * @return A reference to the EyeApplication's ConfigFile object.
	 */
	public final ConfigFile getConfig()
	{
		return config;		
	}
	
	//WindowListener
	@Override
	public void windowClosing(WindowEvent e)
	{
		Debugger.i("Cleaning up...");
		mysql.disconnect();
		abortThreads = true;
		Debugger.clearEventListeners();
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
	public void incidentArchived(Incident incident)
	{
		Debugger.v(incident + " archived");
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
	public void deviceNotInUse(Device device, User oldUser)
	{
		Debugger.v(oldUser + " logged out of " + device);
	}
	
	@Override
	public void deviceLocationChanged(Device device, Location oldLocation, Location newLocation)
	{
		Debugger.v(device + " location data changed: " + newLocation);
	}
	
	@Override
	public void deviceAtmosphereChanged(Device device, Atmosphere oldAtmosphere, Atmosphere newAtmosphere)
	{
		Debugger.v(device + " atmospheric data changed: " + newAtmosphere);
	}
	
	@Override
	public void deviceUpdated(Device device)
	{
		Debugger.v(device + " updated: " + device.getLastUpdate());
	}
	
	@Override
	public void deviceAddressChanged(Device device, InetAddress oldAddress, InetAddress newAddress)
	{
		Debugger.v(device + " address changed: " + newAddress);
	}
	
	@Override
	public void deviceAssignedIncident(Device device, Incident incident)
	{
		Debugger.v(device + " assigned to " + incident);
	}
	
	@Override	
	public void deviceUnassignedIncident(Device device, Incident incident)
	{
		Debugger.v(device + " unassigned from " + incident);		
	}
	
	@Override
	public void nodeLocationChanged(Node node, Location oldLocation, Location newLocation)
	{
		Debugger.v(node + " location data changed: " + newLocation);
	}
	
	@Override
	public void nodeVoltageChanged(Node node, Double oldVoltage, Double newVoltage)
	{
		Debugger.v(node + " input voltage changed: " + newVoltage);
	}
	
	@Override
	public void nodeUpdated(Node node)
	{
		Debugger.v(node + " updated: " + node.getLastUpdate());
	}
	
	@Override
	public void nodeAddressChanged(Node node, InetAddress oldAddress, InetAddress newAddress)
	{
		Debugger.v(node + " address changed: " + newAddress);
	}

	/**
	 * Creates a new incident in the MySQL database, handling errors and firing events accordingly.
	 * @param type The incident's type
	 * @param location The location of the incident (ideally this will be the reporting User's Device.getLocation()) 
	 * @return the new Incident object, or null if an error occurred.
	 * @throws NullPointerException if location is null.
	 */
	public final Incident db_createIncident(Incident.Type type, Location location)
	{
		//sanity checks
		if (location == null)
			throw new NullPointerException("Parameter 'location' cannot be null.");
		
		//manipulate database
		//TODO: execute database INSERT query, store auto-generated id
		int generatedID = incidents.size()+1;
		
		//manipulate data structures
		Incident incident = new Incident(generatedID,
			type,
			location,
			new Timestamp(new Date().getTime()),
			this);
		addNewIncident(Integer.valueOf(generatedID), incident);
		
		//return incident
		return incident;
	}
	
	/**
	 * Creates a new user in the MySQL database, handling errors and firing events accordingly.
	 * @param responderType The user's type (i.e. what sort of Incident they can respond to).
	 * @param nameFirst The user's first (given) name.
	 * @param nameMiddle The user's middle (given) name(s).
	 * @param nameLast The user's last (family) name.
	 * @return the new User object, or null if an error occurred.
	 * @throws NullPointerException if any of the name parameters are null.
	 */
	public final User db_createUser(Incident.Type responderType, String nameFirst, String nameMiddle, String nameLast)
	{
		//sanity checks
		if (nameFirst == null)
			throw new NullPointerException("Parameter 'nameFirst' cannot be null.");
		if (nameMiddle == null)
			throw new NullPointerException("Parameter 'nameMiddle' cannot be null.");
		if (nameLast == null)
			throw new NullPointerException("Parameter 'nameLast' cannot be null.");
		
		//manipulate database
		//TODO: execute database INSERT query, store auto-generated id
		int generatedID = users.size()+1;
		
		//manipulate data structures
		User user = new User(generatedID,
			responderType,
			nameFirst,
			nameMiddle,
			nameLast,
			this);
		addNewUser(Integer.valueOf(generatedID), user);
		
		//return user
		return user;
	}
	
	/**
	 * Assigns a user to a device in the MySQL database, handling errors and firing events accordingly.
	 * @param device The device to which the user will be assigned.
	 * @param user The user to assign (pass null to 'unassign').
	 * @return false if an error occurred, true otherwise.
	 * @throws NullPointerException if the device was null.
	 */
	public final boolean db_setDeviceUser(Device device, User user)
	{
		//sanity checks
		if (device == null)
			throw new NullPointerException("Parameter 'device' cannot be null.");
		if (user == device.getCurrentUser())
			return true;
		
		//manipulate database
		//TODO: SQL query for deletion of existing user/device link
		if (user != null)
		{
			//TODO: SQL query for insert of new user/device link
		}
		
		//manipulate data structures
		device.updateUser(user);
		
		//return result
		return true;
	}
	
	/**
	 * Assigns a device to an incident in the MySQL database, handling errors and firing events accordingly.
	 * @param device The device to assign to the given incident.
	 * @param incident The incident to assign (pass null to 'unassign').
	 * @return false if an error occurred, true otherwise.
	 * @throws NullPointerException if the device was null.
	 * @throws IllegalArgumentException if incident is not null and the device does not have a user, or the incident's type does not match the user's type.
	 */
	public final boolean db_setDeviceIncident(Device device, Incident incident)
	{
		//sanity checks
		if (device == null)
			throw new NullPointerException("Parameter 'device' cannot be null.");
		if (incident != null)
		{
			if (incident == device.getCurrentIncident())
				return true;
			if (device.getCurrentUser() == null)
				throw new IllegalArgumentException("The given Device does not currently have an assigned User.");
			if (device.getCurrentUser().getType().compareTo(incident.getType()) != 0)
				throw new IllegalArgumentException("User assigned to the given device does not respond to Incidents of the given type.");
		}
		
		//manipulate database
		//TODO: SQL update query to set respondingIncidentID
		
		//manipulate data structures
		device.updateIncident(incident);
		
		//return result
		return true;
	}
	
	/**
	 * Flags an incident as being archived in the MySQL database, handling errors and firing events accordingly. All assigned devices will be unassigned.
	 * @param incident The incident to archive.
	 * @return false if an error occurred, true otherwise.
	 * @throws NullPointerException if the incident was null.
	 */
	public final boolean db_archiveIncident(Incident incident)
	{
		//sanity checks
		if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		if (incident.isArchived())
			return true;
		
		//manipulate database
		//TODO: SQL query to un-assign all devices from this incident
		//TODO: SQL query to set incident archived flag to TRUE
		
		//manipulate data structures
		for (Device device : incident.getRespondingDevices())
			device.updateIncident(null);
		incident.archive();
		
		return true;
	}
	
	/**
	 * Adds a new EyeApplicationListener.
	 * @param listener subscribes an EyeApplicationListener to this application's state events.
	 */
	public final void addEventListener(EyeApplicationListener listener)
	{
		if (listener == null || listeners.contains(listener))
			return;
		
		synchronized(listeners)
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes an existing EyeApplicationListener. 
	 * @param listener unsubscribes an EyeApplicationListener from this application's state events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public final void removeEventListener(EyeApplicationListener listener)
	{
		if (listener == null)
			return;
		synchronized(listeners)
		{
			listeners.remove(listener);
		}
	}
	
	/**
	 * Unsubscribes all EyeApplicationListeners from this application's state events.
	 */
	public final void clearEventListeners()
	{
		synchronized(listeners)
		{
			listeners.clear();
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// UNIMPLEMENTED INTERFACE METHODS
	/////////////////////////////////////////////////////////////////////

	//DeviceEventListener
	@Override public void deviceTimedOut(Device device) { }
	
	//IncidentEventListener
	@Override public void incidentAssignedDevice(Incident incident, Device device) { }
	@Override public void incidentUnassignedDevice(Incident incident, Device device) { }
	
	//NodeEventListener
	@Override public void nodeTimedOut(Node node) { }

	//WindowListener
	@Override public void windowOpened(WindowEvent e) { }
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
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
				addNewUser(id, user = new User(entry.getValue(), this));
			user.updateFromMySQL(entry.getValue());
		}
	}
	
	
	private void processDevices(MySQLResultSet results)
	{
		//create a list of all the devices
		ArrayList<Device> incidentlessDevices = new ArrayList<Device>(
			devices.values());
		
		//update devices
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			//properties
			String hash = (String)entry.getKey();
			Device device = devices.get(hash);
			if (device == null)
			{
				addNewDevice(hash, device = new Device(hash, (Device.Type)(entry.getValue().get("deviceType")), this));
				incidentlessDevices.add(device);
			}
			device.updateFromMySQL(entry.getValue());	
			
			//linked incident
			Integer incidentKey = (Integer)(entry.getValue().get("respondingIncidentID"));
			if (incidentKey != null)
			{
				Incident incident = incidents.get(incidentKey);
				if (incident != null)
				{
					incidentlessDevices.remove(device);
					device.updateIncident(incident);
				}
			}
		}
		
		//incident-less devices
		for (Device device : incidentlessDevices)
			device.updateIncident(null);
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
				addNewNode(hash, node = new Node(hash, this));
			node.updateFromMySQL(entry.getValue());	
		}
	}
	
	private void processIncidents(MySQLResultSet results)
	{
		//process incidents
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			Integer id = (Integer)entry.getKey();
			Incident incident = incidents.get(id);
			if (incident == null)
			{
				Double accuracy = entry.getValue().get("accuracy") == null ? null : (Double)entry.getValue().get("accuracy");
				Double altitude = entry.getValue().get("altitude") == null ? null : (Double)entry.getValue().get("altitude");
				addNewIncident(id, incident = new Incident(id.intValue(),
					(Incident.Type)(entry.getValue().get("incidentType")),
					new Location(
						(Double)entry.getValue().get("latitude"),
						(Double)entry.getValue().get("longitude"),
						accuracy,
						altitude
					),
					(Timestamp)(entry.getValue().get("created")),
					this));

			}
			incident.updateFromMySQL(entry.getValue());
		}
	}
	
	private void addNewIncident(Integer id, Incident incident)
	{
		//add to array
		incidents.put(id, incident);
		
		//fire creation event
		synchronized(listeners)
		{
			ListIterator<EyeApplicationListener> iterator = listeners.listIterator();
			while(iterator.hasNext())
				iterator.next().incidentCreated(incident);
		}
	}
	
	private void addNewUser(Integer id, User user)
	{
		//add to array
		users.put(id, user);
		
		//fire creation event
		synchronized(listeners)
		{
			ListIterator<EyeApplicationListener> iterator = listeners.listIterator();
			while(iterator.hasNext())
				iterator.next().userCreated(user);
		}
	}
	
	private void addNewDevice(String hash, Device device)
	{
		//add to array
		devices.put(hash, device);
		
		//fire creation event
		synchronized(listeners)
		{
			ListIterator<EyeApplicationListener> iterator = listeners.listIterator();
			while(iterator.hasNext())
				iterator.next().deviceCreated(device);
		}
	}
	
	private void addNewNode(String hash, Node node)
	{
		//add to array
		nodes.put(hash, node);
		
		//fire creation event
		synchronized(listeners)
		{
			ListIterator<EyeApplicationListener> iterator = listeners.listIterator();
			while(iterator.hasNext())
				iterator.next().nodeCreated(node);
		}
	}
}
