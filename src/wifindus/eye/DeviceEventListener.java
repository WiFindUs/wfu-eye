package wifindus.eye;

public interface DeviceEventListener
{
	/**
	 * Event fired when a Device is first created.
	 * @param device The new device object.
	 */
	public void deviceCreated(Device device);
	
	/**
	 * Event fired when a Device 'times out' (i.e. when it is deemed inactive
	 * as a result of no updates being received within a set interval).
	 * @param device The inactive device object.
	 */
	public void deviceTimedOut(Device device);
	
	/**
	 * Event fired when a User is attached to (or 'logs in') to a Device.
	 * @param device The device object.
	 * @param user The user logged into the device.
	 */
	public void deviceUserLoggedIn(Device device, User user);
	
	/**
	 * Event fired when a User is detached from (or 'logs out of') a Device.
	 * @param device The device object.
	 * @param user The user that logged out of the device.
	 */
	public void deviceUserLoggedOut(Device device, User user);
	
	/**
	 * Event fired when a Device's location details change.
	 * @param device The device object.
	 */
	public void deviceLocationChanged(Device device);
	
	/**
	 * Event fired when a Device's atmosphere details change.
	 * @param device The device object.
	 */
	public void deviceAtmosphereChanged(Device device);
	
	/**
	 * Event fired when a Device's network address changes.
	 * @param device The device object.
	 */
	public void deviceAddressChanged(Device device);
	
	/**
	 * Event fired when any aspect of a Device is updated.
	 * @param device The device object.
	 */
	public void deviceUpdated(Device device);
	
	/**
	 * Event fired when a Device is assigned to an Incident.
	 * @param device The device object.
	 * @param incident The incident the device is now assigned to.
	 */
	public void deviceAssignedIncident(Device device, Incident incident);
	
	/**
	 * Event fired when a Device is unassigned from an Incident.
	 * @param device The device object.
	 * @param incident The incident the device was previously assigned to.
	 */
	public void deviceUnassignedIncident(Device device, Incident incident);
}
