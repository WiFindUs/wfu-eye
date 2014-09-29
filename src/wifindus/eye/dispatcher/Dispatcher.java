package wifindus.eye.dispatcher;

import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import java.awt.Color;
import javax.swing.*;


public class Dispatcher extends EyeApplication
{
	private static final long serialVersionUID = 12094147960785467L;
	private JPanel incidentPanel, personnelPanel;

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Dispatcher(String[] args)
	{
		super(args,true);
		
		//create two panels with a splitter
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        	new JScrollPane(personnelPanel = new JPanel()),
        	new JScrollPane(incidentPanel = new JPanel()));
        
        personnelPanel.setLayout(new BoxLayout(personnelPanel, BoxLayout.Y_AXIS));
        incidentPanel.setLayout(new BoxLayout(incidentPanel, BoxLayout.Y_AXIS));
        incidentPanel.setBackground(Color.WHITE);
        
        // Specify location of pane split
        sp.setResizeWeight(0.15);
        sp.setOneTouchExpandable(true);
        getContentPane().add(sp);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////

	@Override
	public void deviceCreated(Device device)
	{
		super.deviceCreated(device);
	}
	

	/////////////////////////////////////////////////////////////////////
	// MAIN - DO NOT MODIFY
	/////////////////////////////////////////////////////////////////////
	
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