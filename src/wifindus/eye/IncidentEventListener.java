package wifindus.eye;

public interface IncidentEventListener
{
	/**
	 * Event fired when an Incident is first created.
	 * @param incident The new incident object.
	 */
	public void incidentCreated(Incident incident);
	
	/**
	 * Event fired when an Incident is deleted.
	 * @param incident The about-to-be-deleted incident object.
	 */
	public void incidentDeleted(Incident incident);
	
	/**
	 * Event fired when an Incident is archived.
	 * @param incident The archived incident object.
	 */
	public void incidentArchived(Incident incident);
}
