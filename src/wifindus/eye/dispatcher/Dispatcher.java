package wifindus.eye.dispatcher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;


public class Dispatcher extends EyeApplication
{
	private static final long serialVersionUID = 12094147960785467L;
	private JPanel menuPanel, incidentPanel, devicePanel;
    

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Dispatcher(String[] args)
	{
		super(args);
		getClientPanel().setLayout(new BoxLayout(getClientPanel(), BoxLayout.Y_AXIS));
		getClientPanel().setBackground(Color.white);
		menuPanel = new JPanel();
		menuPanel.setBackground(Color.white);
		menuPanel.setPreferredSize(new Dimension(800, 70));
		menuPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0 , new Color(0x618197)));
		
		devicePanel = new JPanel();
		devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
		
		JScrollPane devicePanelScroll = new JScrollPane(devicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
        incidentPanel = new JPanel();
        incidentPanel.setLayout(new BoxLayout(incidentPanel, BoxLayout.Y_AXIS));
        
        JScrollPane incidentPanelScroll = new JScrollPane(incidentPanel);
        incidentPanel.setBackground(Color.WHITE);
        
        getClientPanel().setLayout(new BorderLayout());
        getClientPanel().add(menuPanel, BorderLayout.NORTH);
        getClientPanel().add(devicePanelScroll, BorderLayout.WEST);
        getClientPanel().add(incidentPanelScroll, BorderLayout.CENTER);
        
        
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