package wifindus.eye.dispatcher;

import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;
import wifindus.eye.MapFrame;

public abstract class IncidentParentPanel extends MapFrameLinkedPanel implements IncidentEventListener
{
	private static final long serialVersionUID = -49268178751804004L;
	private transient volatile Incident incident = null;
	
    public IncidentParentPanel(Incident incident, MapFrame mapFrame)
    {
		super(mapFrame);
    	if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		this.incident = incident;
    }
    
	public final Incident getIncident()
	{
		return incident;
	}
}
