package wifindus.eye;

import java.net.InetAddress;

/**
 * An object which listens for changes in the state of a Device object. 
 * @author Mark 'marzer' Gillard
 */
public interface DeviceEventListener
{
	/**
	 * Event fired when a Device is first created.
	 * Corresponds to the event key "created".
	 * @param device The new device object.
	 */
	public void deviceCreated(Device device);
	
	/**
	 * Event fired when a Device 'times out' (i.e. when it is deemed inactive
	 * as a result of no updates being received within a set interval).
	 * Corresponds to the event key "timedout".
	 * @param device The inactive device object.
	 */
	public void deviceTimedOut(Device device);
	
	/**
	 * Event fired when a User is attached to (or 'logs in') to a Device.
	 * Corresponds to the event key "loggedin".
	 * @param device The device object.
	 * @param user The user logged into the device.
	 */
	public void deviceInUse(Device device, User user);
	
	/**
	 * Event fired when a User is detached from (or 'logs out of') a Device.
	 * Corresponds to the event key "loggedout".
	 * @param device The device object.
	 * @param user The user that logged out of the device.
	 */
	public void deviceNotInUse(Device device, User user);
	
	/**
	 * Event fired when a Device's location details change.
	 * Corresponds to the event key "location".
	 * @param device The device object.
	 * @param oldLocation The device's previous Location.
	 * @param newLocation The device's new Location.
	 */
	public void deviceLocationChanged(Device device, Location oldLocation, Location newLocation);
	
	/**
	 * Event fired when a Device's atmosphere details change.
	 * Corresponds to the event key "atmosphere".
	 * @param device The device object.
	 * @param oldAtmosphere The device's previous Atmosphere.
	 * @param newAtmosphere The device's new Atmosphere.
	 */
	public void deviceAtmosphereChanged(Device device, Atmosphere oldAtmosphere, Atmosphere newAtmosphere);
	
	/**
	 * Event fired when a Device's network address changes.
	 * Corresponds to the event key "address".
	 * @param device The device object.
	 */
	public void deviceAddressChanged(Device device, InetAddress oldAddress, InetAddress newAddress);
	
	/**
	 * Event fired when the <code>lastUpdate</code> property of the device is changed.
	 * Corresponds to the event key "updated".
	 * @param device The device object.
	 */
	public void deviceUpdated(Device device);
	
	/**
	 * Event fired when a Device is assigned to an Incident.
	 * Corresponds to the event key "assigned".
	 * @param device The device object.
	 * @param incident The incident the device is now assigned to.
	 */
	public void deviceAssignedIncident(Device device, Incident incident);
	
	/**
	 * Event fired when a Device is unassigned from an Incident.
	 * Corresponds to the event key "unassigned".
	 * @param device The device object.
	 * @param incident The incident the device was previously assigned to.
	 */
	public void deviceUnassignedIncident(Device device, Incident incident);
}
