package wifindus.eye;

/**
 * An object which listens for the creation of one of the core Eye platform objects.
 * @author Mark 'marzer' Gillard
 */
public interface EyeApplicationListener
{
	/**
	 * Event fired when a Device is first created.
	 * @param device The new device object.
	 */
	public void deviceCreated(Device device);

	/**
	 * Event fired when an Incident is first created.
	 * @param incident The new incident object.
	 */
	public void incidentCreated(Incident incident);

	/**
	 * Event fired when a Node is first created.
	 * @param node The new node object.
	 */
	public void nodeCreated(Node node);
	
	/**
	 * Event fired when an User is first created.
	 * @param user The new user object.
	 */
	public void userCreated(User user);
}
