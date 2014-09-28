package wifindus.eye.dispatcher;

import javax.swing.SwingUtilities;
import wifindus.eye.EyeApplication;
import java.awt.Color;
import javax.swing.*;


public class Dispatcher extends EyeApplication
{
	private static final long serialVersionUID = 12094147960785467L;

	public JFrame frame;
    public static JScrollPane incidentScrollPanel;
    public static IncidentPanel incidentPanel;

	public Dispatcher(String[] args)
	{
		super(args);
		
        // Incident Display Panel
        incidentPanel = new IncidentPanel();
        incidentScrollPanel = new JScrollPane(incidentPanel);
        incidentPanel.setBackground(Color.WHITE);
        
        // Personnel Display Panel
        PersonnelPanel personnelPanel = new PersonnelPanel();
               
        //Add Each Panel to a Split Pane
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, personnelPanel, incidentScrollPanel);
        
        // Specify location of pane split
        sp.setResizeWeight(0.01);
        sp.setOneTouchExpandable(true);
        getContentPane().add(sp);
	}

	//do not modify main :)
	public static void main(final String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	new Dispatcher(args)
		    	.setTitle("WiFindUs Dispatcher");
		    }
		});
	}
}