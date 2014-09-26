package wifindus.eye.dispatcher;

import javax.swing.SwingUtilities;
import wifindus.eye.EyeApplication;
import java.awt.Color;
import java.awt.Dimension;
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
		
		
		// Dispatcher Frame
        frame = new JFrame("WFU Dispatch");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
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
        
        frame.getContentPane().add(sp);
        frame.pack();
        frame.setVisible(true);

		

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