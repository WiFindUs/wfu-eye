package wifindus.eye.dispatcher;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import wifindus.Debugger;

public class IncidentPanel extends JPanel 
{
	private static final long serialVersionUID = -7397843910420550797L;
	int width;
    ArrayList<IncidentRecordPanel> incidents;
    ArrayList<Incident> incidentRecords;
    IncidentRecordPanel incident1;
    int incidentNo = 1;
    static boolean create = false;
     
    public IncidentPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        incidents = new ArrayList<>();
	incidentRecords = new ArrayList<>();
    }

    public void newIncident(int id, int latitude, int longitude)
    {
         incidentRecords.add(new Incident(incidentNo));
                
                try 
                {
                    incidents.add(new IncidentRecordPanel(incidentNo, id, latitude, longitude));
                    add(Box.createRigidArea(new Dimension(0, 20)));
                } 
                catch (InterruptedException ex) 
                {
                    Debugger.ex(ex);
                }
                incidentNo--;
                add(incidents.get(incidentNo));
                incidentNo+=2;
                revalidate();
                repaint();
    }
}

