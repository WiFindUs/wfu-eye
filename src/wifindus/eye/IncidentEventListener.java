package wifindus.eye;

public interface IncidentEventListener
{
	public void incidentCreated();
	public void incidentDeleted();
	public void incidentArchived();
	public void incidentUserAssigned();
}
