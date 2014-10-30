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
	
	/**
	 * Event fired when a User is marked as having responded to this incident during archival.
	 * Corresponds to the event key "reponderadded".
	 * @param incident The incident object.
	 * @param user The User object.
	 */
	public void incidentArchivedResponderAdded(Incident incident, User user);
	
	/**
	 * Event fired when the severity of an incident is changed.
	 * Corresponds to the event key "severity".
	 * @param incident The incident object.
	 * @param oldSeverity The incident's previous Severity.
	 * @param newSeverity The incident's new Severity.
	 */
	public void incidentSeverityChanged(Incident incident, int oldSeverity, int newSeverity);
	
	/**
	 * Event fired when the code of an incident is changed.
	 * Corresponds to the event key "code".
	 * @param incident The incident object.
	 * @param oldCode The incident's previous Code.
	 * @param newCode The incident's new Code.
	 */
	public void incidentCodeChanged(Incident incident, String oldCode, String newCode);
	
	/**
	 * Event fired when the reporting user of an incident is changed.
	 * Corresponds to the event key "reportinguser".
	 * @param incident The incident object.
	 * @param oldUser The incident's previous responding user.
	 * @param newUser The incident's new responding user.
	 */
	public void incidentReportingUserChanged(Incident incident, User oldUser, User newUser);
	
	/**
	 * Event fired when the general description field of an incident is changed.
	 * Corresponds to the event key "description".
	 * @param incident The incident object.
	 */
	public void incidentDescriptionChanged(Incident incident);
}
