package wifindus.eye;

/**
 * An object which listens for changes in the state of a Node object. 
 * @author Mark 'marzer' Gillard
 */
public interface NodeEventListener
{
	/**
	 * Event fired when a Node is first created.
	 * @param node The new node object.
	 */
	public void nodeCreated(Node node);
	
	/**
	 * Event fired when a Node 'times out' (i.e. when it is deemed inactive
	 * as a result of no updates being received within a set interval).
	 * @param node The inactive node object.
	 */
	public void nodeTimedOut(Node node);
	
	/**
	 * Event fired when a Node's location details change.
	 * @param node The node object.
	 */
	public void nodeLocationChanged(Node node);
	
	/**
	 * Event fired when a Node's power source voltage reading changes.
	 * @param node The node object.
	 */
	public void nodeVoltageChanged(Node node);
	
	/**
	 * Event fired when the <code>lastUpdate</code> property of the node is changed.
	 * @param node The node object.
	 */
	public void nodeUpdated(Node node);
	
	/**
	 * Event fired when a Node's network address changes.
	 * @param node The node object.
	 */
	public void nodeAddressChanged(Node node);
}
