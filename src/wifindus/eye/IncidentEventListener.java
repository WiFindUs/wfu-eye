package wifindus.eye;

/**
 * An object which listens for changes in the state of a Incident object. 
 * @author Mark 'marzer' Gillard
 */
public interface IncidentEventListener
{
	/**
	 * Event fired when an Incident is first created.
	 * Corresponds to the event key "created".
	 * @param incident The new incident object.
	 */
	public void incidentCreated(Incident incident);

	/**
	 * Event fired when an Incident is archived.
	 * Corresponds to the event key "archived".
	 * @param incident The archived incident object.
	 */
	public void incidentArchived(Incident incident);
}
