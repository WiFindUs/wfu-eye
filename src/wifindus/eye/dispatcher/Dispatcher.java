package wifindus.eye.dispatcher;

import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import java.awt.Color;
import javax.swing.*;


public class Dispatcher extends EyeApplication
{
	private static final long serialVersionUID = 12094147960785467L;
	private JPanel incidentPanel, devicePanel;
    

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Dispatcher(String[] args)
	{
		super(args);
		
		//create two panels with a splitter
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        	new JScrollPane(devicePanel = new JPanel()),
        	new JScrollPane(incidentPanel = new JPanel()));
        
        devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
        incidentPanel.setLayout(new BoxLayout(incidentPanel, BoxLayout.Y_AXIS));
        incidentPanel.setBackground(Color.WHITE);
        
        // Specify location of pane split
        sp.setResizeWeight(0.25);
        sp.setOneTouchExpandable(true);
        getClientPanel().add(sp);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////

	@Override
	public void deviceCreated(Device device)
	{
		super.deviceCreated(device);
		devicePanel.add(new DevicePanel(device));
		devicePanel.revalidate();
	}
	
	@Override
	public void incidentCreated(Incident incident)
	{
		super.incidentCreated(incident);
		incidentPanel.add(new IncidentPanel(incident));
		incidentPanel.revalidate();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	
	
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