package wifindus.eye;

/**
 * An object which listens for changes in the state of a Incident object. 
 * @author Mark 'marzer' Gillard
 */
public interface IncidentEventListener
{
	/**
	 * Event fired when an Incident is archived.
	 * Corresponds to the event key "archived".
	 * @param incident The archived incident object.
	 */
	public void incidentArchived(Incident incident);
	
	/**
	 * Event fired when a Device is assigned to an Incident.
	 * Corresponds to the event key "assigned".
	 * @param incident The incident object.
	 * @param device The device object.
	 */
	public void incidentAssignedDevice(Incident incident, Device device);
	
	/**
	 * Event fired when a Device is unassigned from an Incident.
	 * Corresponds to the event key "unassigned".
	 * @param incident The incident object.
	 * @param device The device object.
	 */
	public void incidentUnassignedDevice(Incident incident, Device device);
}
