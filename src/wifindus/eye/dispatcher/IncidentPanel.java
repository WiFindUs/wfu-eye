package wifindus.eye.dispatcher;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import wifindus.eye.Device;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;

public class IncidentPanel extends JPanel implements IncidentEventListener
{
	private static final long serialVersionUID = -7397843910420550797L;
    private transient Incident incident = null;
    private transient JLabel incidentTime, typeLabel, statusDisplay, incidentTypeTitle;
    private transient JButton showOnMap, addRespondent;
     
    public IncidentPanel(Incident incident)
    {
		if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		this.incident = incident;
    	
		//cosmetic properties
    	setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(3,2,2,2,Color.black));
        Border blackLine = BorderFactory.createLineBorder(Color.black);

        
        //time/status panels
        JPanel timeAndStatus = new JPanel(); 
        timeAndStatus.setLayout(new GridLayout(2,1));
        timeAndStatus.add(statusDisplay = new JLabel("Status: Active"));
        statusDisplay.setFont(statusDisplay.getFont().deriveFont(18.0f));
        timeAndStatus.add(incidentTime = new JLabel("Time: 00 : 00 : 00"));
        incidentTime.setFont(incidentTime.getFont().deriveFont(18.0f));
        
        //north panel
        JPanel northPanel = new JPanel(); 
        northPanel.setBorder(BorderFactory.createMatteBorder(0,0,2,0,Color.black));
        northPanel.setLayout(new BoxLayout (northPanel, BoxLayout.X_AXIS));
        northPanel.add(typeLabel = new JLabel("Incident #" + incident.getID()));
        typeLabel.setFont(typeLabel.getFont().deriveFont(30.0f));
        northPanel.add(Box.createHorizontalGlue());
        northPanel.add(timeAndStatus);
        northPanel.add(showOnMap = new JButton("Show on Map"));
        add(northPanel, BorderLayout.NORTH);
        
        //respondent type panel
        JPanel respondentTypeSelectionPanel = new JPanel();
        respondentTypeSelectionPanel.setLayout(new GridLayout(3,1));
        
        //west panel
        JPanel westPanel = new JPanel();
        westPanel.setLayout (new BoxLayout (westPanel, BoxLayout.Y_AXIS));
        westPanel.add(incidentTypeTitle = new JLabel("Incident Type"));
        incidentTypeTitle.setFont(incidentTypeTitle.getFont().deriveFont(21.0f));
        westPanel.setBorder(blackLine);
        westPanel.add(new JLabel(Incident.getIcon(incident.getType(), false)));
        add(westPanel, BorderLayout.WEST);
        
        /*
         * 
         * 
         * TODO: finish importing the rest of the code here...
         * 
         * 
         * 
         */
        
        incident.addEventListener(this);
    }

	@Override
	public void incidentArchived(Incident incident)
	{
		
	}
	
	@Override
	public void incidentAssignedDevice(Incident incident, Device device)
	{
		
	}
	
	@Override
	public void incidentUnassignedDevice(Incident incident, Device device)
	{
		
	}
	
	//do not implement this
	@Override public void incidentCreated(Incident incident) { }
}

