package wifindus.eye;

/**
 * An object which listens for changes in the state of a Node object. 
 * @author Mark 'marzer' Gillard
 */
public interface NodeEventListener
{
	/**
	 * Event fired when a Node is first created.
	 * Corresponds to the event key "created".
	 * @param node The new node object.
	 */
	public void nodeCreated(Node node, Location nodeLocation);
	
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
	 */
	public void nodeLocationChanged(Node node, Location nodeLocation);
	
	/**
	 * Event fired when a Node's power source voltage reading changes.
	 * Corresponds to the event key "voltage".
	 * @param node The node object.
	 */
	public void nodeVoltageChanged(Node node);
	
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
	 */
	public void nodeAddressChanged(Node node);
}
