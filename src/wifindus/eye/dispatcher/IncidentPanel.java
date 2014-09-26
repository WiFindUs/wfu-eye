package wifindus.eye.dispatcher;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class IncidentPanel extends JPanel 
{
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
                    Logger.getLogger(IncidentPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                incidentNo--;
                add(incidents.get(incidentNo));
                incidentNo+=2;
                revalidate();
                repaint();
    }
}

