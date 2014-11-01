package wifindus.eye;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import wifindus.ConfigFile;
import wifindus.Debugger;
import wifindus.DebuggerFrame;
import wifindus.HighResolutionTimerListener;
import wifindus.MySQLResultRow;
import wifindus.MySQLResultSet;

/**
 * A general base for Eye applications that need to maintain a connection
 * to a MySQL database and respond to changes made to Users, Devices, etc.
 * @author Mark 'marzer' Gillard
 */
public abstract class EyeApplication extends JFrame
	implements EyeApplicationListener, DeviceEventListener, NodeEventListener,
	UserEventListener, IncidentEventListener, WindowListener
{
	//properties
	private static final long serialVersionUID = -3410394016911856177L;
	private transient static final Pattern PATTERN_VERBOSITY = Pattern.compile( "^-([0-4])$" );
	private transient static final Pattern PATTERN_CONSOLE = Pattern.compile( "^-console?$", Pattern.CASE_INSENSITIVE);
	private volatile ConfigFile config = null;
	private transient volatile boolean abortThreads = false;
	private transient volatile EyeMySQLConnection mysql = new EyeMySQLConnection();
	private transient static EyeApplication singleton;
	private transient volatile CopyOnWriteArrayList<EyeApplicationListener> listeners = new CopyOnWriteArrayList<>();
	private transient volatile CopyOnWriteArrayList<HighResolutionTimerListener> timerListeners = new CopyOnWriteArrayList<>();
	private transient volatile Timer timer;
	private volatile long lastNanoTime;
	
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
	 * @param forceNoConsole Setting this to FALSE prevents the console window from being created, regardless of the
	 * verbosity level passed at the command-line.
	 * @param spawnSQLThread Setting this to FALSE prevents the background SQL scraper daemon thread form being launched. 
	 * @throws NullPointerException if <code>args</code> is null
	 * @throws IllegalStateException if an existing EyeApplication instance exists.
	 */
	public EyeApplication(String[] args, boolean forceNoConsole, boolean spawnSQLThread)
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
		int verbosity = 1; //info
		boolean spawnConsole = false;
		for (int i = 0; i < args.length; i++)
		{
			Matcher match = PATTERN_VERBOSITY.matcher(args[i]);
			if (match.matches())
			{
				verbosity = Integer.parseInt(match.group(1));
				continue;
			}
			
			if (!forceNoConsole)
			{
				match = PATTERN_CONSOLE.matcher(args[i]);
				if (match.matches())
				{
					spawnConsole = true;
					continue;
				}
			}
		}
		if (spawnConsole)
		{
			DebuggerFrame debuggerFrame = new DebuggerFrame();
			debuggerFrame.setBounds(20, 20, 800, 300);
			debuggerFrame.setVisible(true);
		}
		preDebugHooks();
		Debugger.open(verbosity);
		
		//parse command line arguments for config parameters
		Debugger.i("Parsing command line arguments for config files...");
		List<File> configFiles = new ArrayList<>();
		configFiles.add(new File("eye.conf"));
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
	
		//try loading files
		config = new ConfigFile(configFiles);
		//ensure required keys are present and valid, enforce defaults if not
		//mysql
		config.defaultString("mysql.username", "root");
		config.defaultString("mysql.address", "localhost");
		config.defaultInt("mysql.port", 3306, 1024, 65535);
		config.defaultString("mysql.database", "wfu_eye_db");
		config.defaultInt("mysql.update_interval", 1000, 100, 30000);
		//map
		config.defaultInt("map.grid_rows", 10);
		config.defaultInt("map.grid_columns", 10);
		config.defaultDouble("map.center_latitude", Location.GPS_MARKS_HOUSE.getLatitude().doubleValue(), -90.0, 90.0);
		config.defaultDouble("map.center_longitude", Location.GPS_MARKS_HOUSE.getLongitude().doubleValue(), -180.0, 180.0);
		config.defaultBoolean("map.high_res", true);
		config.defaultInt("map.zoom", 16, 15, 21);
		config.defaultString("map.type", new String[]{"satellite","roadmap","terrain","hybrid"});
		
		
		//ui stuff
		config.defaultInt("ui.update_fps", 60);
		//output config
		Debugger.i("Parsed configuration: " + config);
		
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
		addEyeListener(this);
		
		//create and launch mysql worker task
		if (spawnSQLThread)
		{
			MySQLUpdateWorker mysqlWorker = new MySQLUpdateWorker(config.getInt("mysql.update_interval"));
			mysqlWorker.execute();
		}
		
		//start ui timer
		startTimer();
	}
	
	/**
	 * Creates a new EyeApplication.
	 * @see wifindus.eye.EyeApplication#EyeApplication(String[], boolean)
	 */
	public EyeApplication(String[] args)
	{
		this(args,false,true);
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
	
	/**
	 * Gets a list of all Devices.
	 * @return An ArrayList containing all Devices.
	 * Since the backing collection is a mutable structure, the returned list is a clone,
	 * and thus calling any mutators will have no effect on the backing collection data.
	 */
	public final ArrayList<Device> getDevices()
	{
		return new ArrayList<Device>(devices.values());
	}
	
	/**
	 * Gets a list of all Incidents.
	 * @return An ArrayList containing all Incidents.
	 * Since the backing collection is a mutable structure, the returned list is a clone,
	 * and thus calling any mutators will have no effect on the backing collection data.
	 */
	public final ArrayList<Incident> getIncidents()
	{
		return new ArrayList<Incident>(incidents.values());
	}
	
	/**
	 * Gets a list of all Nodes.
	 * @return An ArrayList containing all Nodes.
	 * Since the backing collection is a mutable structure, the returned list is a clone,
	 * and thus calling any mutators will have no effect on the backing collection data.
	 */
	public final ArrayList<Node> getNodes()
	{
		return new ArrayList<Node>(nodes.values());
	}
	
	/**
	 * Gets a list of all Users.
	 * @return An ArrayList containing all Users.
	 * Since the backing collection is a mutable structure, the returned list is a clone,
	 * and thus calling any mutators will have no effect on the backing collection data.
	 */
	public final ArrayList<User> getUsers()
	{
		return new ArrayList<User>(users.values());
	}
	
	//WindowListener
	@Override
	public void windowClosing(WindowEvent e)
	{
		Debugger.i("Cleaning up...");
		abortThreads = true;
		
		stopTimer();
		clearEyeListeners();
		clearTimerListeners();
		mysql.disconnect();
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
		if (!location.hasLatLong())
			throw new IllegalArgumentException("Parameter 'location' is missing horizontal positioning data.");
		
		//manipulate database
		int generatedID = -1;
		Statement statement = null;
		ResultSet resultSet = null;
		try
		{
			statement = mysql.createStatement();
			statement.executeUpdate("INSERT INTO Incidents "
					+ "(incidentType, latitude, longitude, created) "
					+ "VALUES ("
						+"'"+Incident.getDatabaseKeyFromType(type)+"', "
						+location.getLatitude()+", "
						+location.getLongitude()+", "
						+"NOW())"
					, Statement.RETURN_GENERATED_KEYS);
			
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next())
				generatedID = resultSet.getInt(1);
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
			return null;
		}
		finally
		{
			mysql.release(resultSet);
			mysql.release(statement);
		}
		
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
	 * @throws IllegalArgumentException if responderType is Type.None
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
		if (responderType == Incident.Type.None)
			throw new IllegalArgumentException("Parameter 'responderType' cannot be None.");
		
		//manipulate database
		int generatedID = -1;
		Statement statement = null;
		ResultSet resultSet = null;
		try
		{
			statement = mysql.createStatement();
			statement.executeUpdate("INSERT INTO Users "
					+ "(nameFirst, nameMiddle, nameLast, personnelType) "
					+ "VALUES ("
						+"'"+nameFirst+"', "
						+"'"+nameMiddle+"', "
						+"'"+nameLast+"', "
						+"'"+Incident.getDatabaseKeyFromType(responderType)+"')"
					, Statement.RETURN_GENERATED_KEYS);
			
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next())
				generatedID = resultSet.getInt(1);
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
			return null;
		}
		finally
		{
			mysql.release(resultSet);
			mysql.release(statement);
		}
	
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
			if (incident.isArchived())
				throw new IllegalArgumentException("You cannot assign a Device to an archived incident.");
		}
		
		//manipulate database
		Statement statement = null;
		try
		{
			statement = mysql.createStatement();
			statement.executeUpdate("UPDATE Devices SET "
					+ "respondingIncidentID="+(incident==null ? "NULL" : incident.getID())+" "
					+ "WHERE hash='"+device.getHash()+"'");
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
			return false;
		}
		finally
		{
			mysql.release(statement);
		}
		
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
		
		//get currently responding devices and their associated users
		List<Device> respondingDevices = incident.getRespondingDevices();
		List<User> respondingUsers = new ArrayList<User>();
		for (Device device : respondingDevices)
		{
			User user = device.getCurrentUser();
			if (user != null)
				respondingUsers.add(user);
		}
		
		//manipulate database
		Statement statement = null;
		try
		{
			statement = mysql.createStatement();
			
			if (respondingUsers.size() > 0)
			{
				String archivedRespondersQuery = "INSERT INTO PastIncidentResponders (userID, incidentID) VALUES ";
				for (int i = 0; i < respondingUsers.size(); i++)
					archivedRespondersQuery += (i > 0 ? ",":"") + "(" + respondingUsers.get(i).getID() +","+incident.getID()+")";
				statement.executeUpdate(archivedRespondersQuery);
			}
			
			statement.executeUpdate("UPDATE Devices SET "
					+ "respondingIncidentID=NULL "
					+ "WHERE respondingIncidentID="+incident.getID());
			statement.executeUpdate("UPDATE Incidents SET "
					+ "archived=1,archivedTime=NOW() "
					+ "WHERE id="+incident.getID());
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
			return false;
		}
		finally
		{
			mysql.release(statement);
		}
		
		//manipulate data structures
		for (Device device : respondingDevices)
			device.updateIncident(null);
		incident.archive(new Timestamp(new Date().getTime()));
		for (User responder : respondingUsers)
			incident.assignArchivedResponder(responder);
		return true;
	}
	
	/**
	 * Updates an incident's description in the database.
	 * @param incident The incident to change.
	 * @param description The description to set.
	 * @return false if an error occurred, true otherwise.
	 * @throws NullPointerException if the incident was null.
	 */
	public final boolean db_setIncidentDescription(Incident incident, String description)
	{
		//sanity checks
		if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		if (description == null)
			description = "";
		description = description.trim();
		if (description.equals(incident.getDescription()))
			return true;
		
		//manipulate database
		Statement statement = null;
		try
		{
			statement = mysql.createStatement();
			statement.executeUpdate("UPDATE Incidents SET "
					+ "description='"+description+"' "
					+ "WHERE id="+incident.getID());
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
			return false;
		}
		finally
		{
			mysql.release(statement);
		}
		
		//manipulate data structures
		incident.updateDescription(description);
		
		return true;
	}
	
	/**
	 * Sets an incident's reporting user in the database.
	 * @param incident The incident to change.
	 * @param reportingUser The user to set (null for no user).
	 * @return false if an error occurred, true otherwise.
	 * @throws NullPointerException if the incident was null.
	 */
	public final boolean db_setIncidentReportingUser(Incident incident, User reportingUser)
	{
		//sanity checks
		if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		if (incident.getReportingUser() == reportingUser)
			return true;
		
		//manipulate database
		Statement statement = null;
		try
		{
			statement = mysql.createStatement();
			statement.executeUpdate("UPDATE Incidents SET "
					+ "reportingUserID="+(reportingUser == null ? "NULL" : reportingUser.getID())+" "
					+ "WHERE id="+incident.getID());
		}
		catch (SQLException e)
		{
			Debugger.ex(e);
			return false;
		}
		finally
		{
			mysql.release(statement);
		}
		
		//manipulate data structures
		incident.updateReportingUser(reportingUser);
		
		return true;
	}
	
		
	/**
	 * Adds a new EyeApplicationListener.
	 * @param listener subscribes an EyeApplicationListener to this application's state events.
	 */
	public final void addEyeListener(EyeApplicationListener listener)
	{
		if (listener == null || listeners.contains(listener))
			return;
		
		synchronized(listeners)
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Adds a new HighResolutionTimerListener.
	 * @param timerListener subscribes an HighResolutionTimerListener to this application's timerTick() events.
	 */
	public final void addTimerListener(HighResolutionTimerListener timerListener)
	{
		if (timerListener == null || timerListeners.contains(timerListener))
			return;
		
		synchronized(timerListeners)
		{
			timerListeners.add(timerListener);
		}
	}
	
	
	
	/**
	 * Removes an existing EyeApplicationListener. 
	 * @param listener unsubscribes an EyeApplicationListener from this application's state events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public final void removeEyeListener(EyeApplicationListener listener)
	{
		if (listener == null)
			return;
		synchronized(listeners)
		{
			listeners.remove(listener);
		}
	}
	
	/**
	 * Removes an existing HighResolutionTimerListener. 
	 * @param timerListener unsubscribes an HighResolutionTimerListener from this application's timerTick() events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public final void removeTimerListener(HighResolutionTimerListener timerListener)
	{
		if (timerListener == null)
			return;
		synchronized(timerListeners)
		{
			timerListeners.remove(timerListener);
		}
	}
	
	/**
	 * Unsubscribes all EyeApplicationListeners from this application's state events.
	 */
	public final void clearEyeListeners()
	{
		synchronized(listeners)
		{
			listeners.clear();
		}
	}
	
	/**
	 * Unsubscribes all HighResolutionTimerListeners from this application's state events.
	 */
	public final void clearTimerListeners()
	{
		synchronized(timerListeners)
		{
			timerListeners.clear();
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
	@Override public void incidentArchivedResponderAdded(Incident incident, User user) { }
	@Override public void incidentSeverityChanged(Incident incident, int oldSeverity, int newSeverity) { }
	@Override public void incidentCodeChanged(Incident incident, String oldCode, String newCode) { }
	@Override public void incidentReportingUserChanged(Incident incident, User oldUser,	User newUser) { }
	@Override public void incidentDescriptionChanged(Incident incident) { }
	
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
	
	/**
	 * Called during initialization, before the initial call to Debugger.open().
	 * Use this to initialize any debugger-based controls so that they'll receive all
	 * Debugger events fired during launch. 
	 */
	protected void preDebugHooks()
	{
		;
	}
	
	/**
	 * Gets the mysql connection.
	 * @return A reference to the EyeMySQLConnection object in use by this application.
	 */
	protected final EyeMySQLConnection getMySQL()
	{
		return mysql;
	}
	
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
					Thread.sleep(50);
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
					Thread.sleep(50);
				}
				catch (SQLException e)
				{
					Debugger.ex(e);
				}
				if (abortThreads)
					break;
				
				//archived incident users
				try
				{
					publish(new Object[] { "PastIncidentResponders", mysql.fetchPastIncidentResponders() });
					Thread.sleep(50);
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
					Thread.sleep(50);
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
					Thread.sleep(50);
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
					Thread.sleep(50);
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
					Thread.sleep(50);
					counter += 50;
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
					case "PastIncidentResponders": processPastIncidentResponders(results); break;
					
				}
			}
		}
	};
	
	private void processPastIncidentResponders(MySQLResultSet results)
	{
		//check and cache ID's for performance if we have a lot
		//from the same incident in sequence
		int id = -1;
		Incident incident = null;
		
		//process entries from database
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			//get integer
			Integer newID = (Integer)entry.getValue().get("incidentID");
			if (newID == null)
				continue;
			if (id == -1 || incident == null || newID.intValue() != id)
			{
				id = newID.intValue();
				incident = incidents.get(newID);
			}
			if (incident == null || !incident.isArchived())
				continue;
			
			//get the user
			Integer userID = (Integer)entry.getValue().get("userID");
			if (userID == null)
				continue;
			User responder = users.get(userID);
			
			//assign user to incident
			if (responder != null)
				incident.assignArchivedResponder(responder);
		}
	}
	
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
		//update devices
		for (Map.Entry< Object, MySQLResultRow > entry : results.entrySet())
		{
			//properties
			String hash = (String)entry.getKey();
			Device device = devices.get(hash);
			if (device == null)
				addNewDevice(hash, device = new Device(hash, (Device.Type)(entry.getValue().get("deviceType")), this));
			device.updateFromMySQL(entry.getValue());	
			
			//linked incident
			Integer incidentKey = (Integer)(entry.getValue().get("respondingIncidentID"));
			device.updateIncident(incidentKey == null ? null : incidents.get(incidentKey));
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
				addNewIncident(id, incident = new Incident(id.intValue(),
					(Incident.Type)(entry.getValue().get("incidentType")),
					new Location(
						(Double)entry.getValue().get("latitude"),
						(Double)entry.getValue().get("longitude"),
						(Double)entry.getValue().get("accuracy"),
						(Double)entry.getValue().get("altitude")
					),
					(Timestamp)(entry.getValue().get("created")),
					this));

			}
			incident.updateFromMySQL(entry.getValue());
			
			//reporting user
			Integer userKey = (Integer)(entry.getValue().get("reportingUserID"));
			incident.updateReportingUser(userKey == null ? null : users.get(userKey));
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
	
	private void startTimer()
	{
		if (timer != null)
			return;

		//store first time
		lastNanoTime = System.nanoTime();
		
		//init timer
		int rate = (int)(1000.0 / (double)config.getInt("ui.update_fps"));
		timer = new Timer();
    	timer.scheduleAtFixedRate(
    		new TimerTask()
    		{
    			@Override
    			public void run()
    			{
    				synchronized(timerListeners)
    				{
    					//work out delta
    					long currentNanoTime = System.nanoTime();
    					double deltaTime = (double)(currentNanoTime - lastNanoTime) / 1000000000.0;
    					lastNanoTime = currentNanoTime;
    					
    					//call subscribers
    					ListIterator<HighResolutionTimerListener> iterator = timerListeners.listIterator();
    					while(iterator.hasNext())
    						iterator.next().timerTick(deltaTime);
    				}
    			}
    		}, rate, rate
    	);
    	
    	Debugger.i("High-resolution timer started.");
	}
	
	private void stopTimer()
	{		
		if (timer == null)
			return;
		
		timer.cancel();
		timer = null;
		Debugger.i("High-resolution timer terminated.");
	}
}
