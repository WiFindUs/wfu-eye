package wifindus.eye;

/**
 * An object which listens for changes in the state of a Incident object. 
 * @author Mark 'marzer' Gillard
 */
public interface IncidentEventListener
{
	/**
	 * Event fired when an Incident is first created.
	 * @param incident The new incident object.
	 */
	public void incidentCreated(Incident incident);

	/**
	 * Event fired when an Incident is archived.
	 * @param incident The archived incident object.
	 */
	public void incidentArchived(Incident incident);
}
