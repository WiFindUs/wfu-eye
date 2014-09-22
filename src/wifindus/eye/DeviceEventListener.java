package wifindus.eye;

public interface DeviceEventListener
{
	public void deviceCreated(Device device);
	public void deviceTimedOut(Device device);
	public void deviceUserLoggedIn(Device device, User user);
	public void deviceUserLoggedOut(Device device, User user);
	public void deviceLocationChanged(Device device);
	public void deviceAtmosphereChanged(Device device);
	public void deviceAddressChanged(Device device);
	public void deviceUpdated(Device device);
	public void deviceAssignedIncident(Device device, Incident incident);
	public void deviceUnassignedIncident(Device device, Incident incident);
}
