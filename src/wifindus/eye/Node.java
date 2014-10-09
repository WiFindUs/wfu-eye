package wifindus.eye;

import java.net.InetAddress;
import java.sql.Timestamp;

import wifindus.EventObject;
import wifindus.MySQLResultRow;
import wifindus.MySQLUpdateTarget;

/**
 * A mesh node device forming part of the WFU client network route.
 * @author Mark 'marzer' Gillard
 */
public class Node extends EventObject<NodeEventListener> implements MySQLUpdateTarget
{
	//properties
	private String hash = "";
	private InetAddress address = null;
	private Location location = Location.EMPTY;
	private Double voltage = null;
	private Timestamp lastUpdate = new Timestamp(0);
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new Node, representing a Node entry from the database.
	 * @param hash The randomly 8-character hash key of this Node.
	 * @param listeners A variable-length list of event listeners that will watch this node's state.
	 * @throws NullPointerException if <code>hash</code> is null.
	 * @throws IllegalArgumentException if <code>hash</code> is not valid.
	 */
	public Node(String hash, NodeEventListener... listeners)
	{
		super(listeners);
		
		if (hash == null)
			throw new NullPointerException("Parameter 'hash' cannot be null.");
		if (!Hash.isValid(hash))
			throw new IllegalArgumentException("Parameter 'hash' is not a valid WFU device hash ("+hash+").");
		
		this.hash = hash;
		
		fireEvent("created");
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets this Node's Hash.
	 * @return An 8-character String containing this Node's unique Hash ID.
	 */
	public final String getHash()
	{
		return hash;
	}
	
	/**
	 * Gets this Node's IP Address.
	 * @return An InetAddress object containing information about the last-known address this Node connected from.
	 */
	public final InetAddress getAddress()
	{
		return address;
	}
	
	/**
	 * Gets this Node's Location.
	 * @return A Location object containing the last-known location information this Node reported. 
	 */
	public final Location getLocation()
	{
		return location;
	}
	
	/**
	 * Gets this Node's last update timestamp.
	 * @return An sql.Timestamp object representing the date/time the last known update was received.
	 * Since Timestamp is a mutable structure, the returned object is a clone, and thus calling any
	 * mutators will have no effect on the Node's timestamp data.
	 */
	public final Timestamp getLastUpdate()
	{
		return (Timestamp)lastUpdate.clone();
	}
	
	/**
	 * Gets the voltage of the node's DC power source. 
	 * @return A double value indicating the DC voltage of the node's power source as measured
	 * by an external sensor, or null (for no data).
	 */
	public final Double getVoltage()
	{
		return voltage;
	}
	
	@Override
	public String toString()
	{
		return "Node[\""+getHash()+"\"]";
	}
	
	@Override
	public void update(MySQLResultRow resultRow)
	{
		if (resultRow == null)
			throw new NullPointerException("Parameter 'resultRow' cannot be null.");
		if (!((String)resultRow.get("hash")).equals(getHash()))
			throw new IllegalArgumentException("Parameter 'resultRow' does not have the same primary key as this object.");
		

		//update location data
		Location loc = new Location(
				(Double)resultRow.get("latitude"),
				(Double)resultRow.get("longitude"),
				(Double)resultRow.get("accuracy"),
				(Double)resultRow.get("altitude"));
		if (!loc.equals(location))
		{
			Location old = location;
			location = loc;
			fireEvent("location", old, location);
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, NodeEventListener listener, Object... data)
	{
		switch(event)
		{
			case "created":
				listener.nodeCreated(this, location);
				break;
			case "timedout":
				listener.nodeTimedOut(this);
				break;
			case "location":
				listener.nodeLocationChanged(this, location);
				break;
			case "voltage":
				listener.nodeVoltageChanged(this);
				break;
			case "address":
				listener.nodeAddressChanged(this);
				break;
			case "updated":
				listener.nodeUpdated(this);
				break;	
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
