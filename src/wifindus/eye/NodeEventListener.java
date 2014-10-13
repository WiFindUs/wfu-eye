 package wifindus.eye;

import java.net.InetAddress;

/**
 * An object which listens for changes in the state of a Node object. 
 * @author Mark 'marzer' Gillard
 */
public interface NodeEventListener
{
	/**
	 * Event fired when a Node 'times out' (i.e. when it is deemed inactive
	 * as a result of no updates being received within a set interval).
	 * Corresponds to the event key "timedout".
	 * @param node The inactive node object.
	 */
	public void nodeTimedOut(Node node);
	
	/**
	 * Event fired when a Node's location details change.
	 * Corresponds to the event key "location".
	 * @param node The node object.
	 * @param oldLocation The node's previous Location.
	 * @param newLocation The node's new Location.
	 */
	public void nodeLocationChanged(Node node, Location oldLocation, Location newLocation);
	
	/**
	 * Event fired when a Node's power source voltage reading changes.
	 * Corresponds to the event key "voltage".
	 * @param node The node object.
	 * @param oldVoltage The node's previous Voltage.
	 * @param newVoltage The node's new Voltage.
	 */
	public void nodeVoltageChanged(Node node, Double oldVoltage, Double newVoltage);
	
	/**
	 * Event fired when the <code>lastUpdate</code> property of the node is changed.
	 * Corresponds to the event key "updated".
	 * @param node The node object.
	 */
	public void nodeUpdated(Node node);
	
	/**
	 * Event fired when a Node's network address changes.
	 * Corresponds to the event key "address".
	 * @param node The node object.
	 * @param oldAddress The node's previous Address.
	 * @param newAddress The node's new Address.
	 */
	public void nodeAddressChanged(Node node, InetAddress oldAddress, InetAddress newAddress);
	
	public void nodeSelectionChanged(Node node);
}
