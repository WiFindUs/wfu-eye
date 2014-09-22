package wifindus.eye;

public interface IncidentEventListener
{
	public void incidentCreated(Incident incident);
	public void incidentDeleted(Incident incident);
	public void incidentArchived(Incident incident);
}
