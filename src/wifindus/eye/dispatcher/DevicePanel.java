package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import wifindus.Debugger;
import wifindus.eye.Atmosphere;
import wifindus.eye.Device;
import wifindus.eye.DeviceEventListener;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.Incident.Type;
import wifindus.eye.Location;
import wifindus.eye.User;

public class DevicePanel extends JPanel implements ActionListener, DeviceEventListener
{
	private static final long serialVersionUID = -953467312117311967L;
    private transient volatile Device device = null;
    private transient JButton newIncidentButton, locateOnMapButton;
    private transient JLabel logo, name, location, status;

    /**
     * Creates a new DevicePanel, binding it to a particular device and responding to its events.
     * @param device The Device to bind to.
     * @throws NullPointerException if device is null.
     */
    public DevicePanel(Device device)
    {
		if (device == null)
			throw new NullPointerException("Parameter 'device' cannot be null.");
		this.device = device;
		
		//cosmetic properties
        setBorder(BorderFactory.createMatteBorder(1,0,1,0 , new Color(0x618197)));
        setBackground(Color.white);
        setMaximumSize(new Dimension(380,95));
        setMinimumSize(new Dimension(380,95));
        
        Font font, nameFont;
        nameFont = getFont().deriveFont(Font.BOLD, 15.0f);
        font = getFont().deriveFont(Font.BOLD, 13.0f);
        
        setBackground(Color.white);
        setLayout(null);
        setPreferredSize(new Dimension(380,90));
        
        //user number&name OR device ID
        name = new JLabel();
        name.setFont(nameFont);
        name.setOpaque(true);
        name.setBackground(Color.white);
        
        //logo
        logo = new JLabel(Incident.getIcon(Type.None, true));
        logo.setBackground(Color.white);
               
        //icons for buttons
        ImageIcon newIncidentLogo = new ImageIcon("images/plus.png");
        ImageIcon showOnMapLogo = new ImageIcon("images/locate.png");
        
        //resize images icon to fit button
        Image plusImg = newIncidentLogo.getImage();  
        Image scaledPlus = plusImg.getScaledInstance( 13, 13,  java.awt.Image.SCALE_SMOOTH );
        newIncidentLogo = new ImageIcon(scaledPlus);
        
        Image showMapImg = showOnMapLogo.getImage();  
        Image scaledShowMap = showMapImg.getScaledInstance( 12, 20,  java.awt.Image.SCALE_SMOOTH );  
        showOnMapLogo = new ImageIcon(scaledShowMap);
        
        Border emptyBorder = BorderFactory.createEmptyBorder();
        
        // buttons for creating incidents & locating users
        newIncidentButton = new JButton("New Incident");
        newIncidentButton.setIcon(newIncidentLogo);
        newIncidentButton.setBackground(Color.white);
        newIncidentButton.addActionListener(this);
        newIncidentButton.setHorizontalAlignment(SwingConstants.LEFT);
        newIncidentButton.setMargin(new Insets(0,0,0,0));
        newIncidentButton.setBorder(emptyBorder);
        newIncidentButton.setFont(font);
        
        locateOnMapButton = new JButton("Locate on map");
        locateOnMapButton.setIcon(showOnMapLogo);
        locateOnMapButton.setBackground(Color.white);
        locateOnMapButton.addActionListener(this);
        locateOnMapButton.setHorizontalAlignment(SwingConstants.LEFT);
        locateOnMapButton.setMargin(new Insets(0,0,0,0));
        locateOnMapButton.setBorder(emptyBorder);
        locateOnMapButton.setFont(font);
        
        //user coordinates - DO NOT DELETE: data is not retrieved if not declared!
        location = new JLabel();
        
        //status of user: Patrolling/Incident#/No User
        status = new JLabel();
        status.setFont(font);
        status.setPreferredSize(new Dimension(116,30));
        status.setForeground(Color.white);
        status.setOpaque(true);
        Border paddingBorder = BorderFactory.createEmptyBorder(0,15,0,15);
        status.setBorder(paddingBorder);
        
        //add controls
        add(name);
        add(logo);
        add(newIncidentButton);
        add(locateOnMapButton);
        add(status);
        
        //set control bounds
        name.setBounds(20, 3, 300, 20);
        logo.setBounds(20,30,60,60);
        newIncidentButton.setBounds(90,37,120,20);
        locateOnMapButton.setBounds(90,60,120,20);
        status.setBounds(250,45,100,25);
        
        //fire usage event to set the initial state
        if (device.getCurrentUser() == null)
        	deviceNotInUse(device,null);
        else
        	deviceInUse(device,device.getCurrentUser());
        
        //attach event listener
        device.addEventListener(this);
    } 
    
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
    
    // Listener for New Incident button 
    @Override
    public void actionPerformed(ActionEvent e) 
    {
    	//everything this button does requires the device to have a valid location
    	if (!device.getLocation().hasLatLong())
    		return;
    	
    	//Listener for new incident
    	if (e.getSource() == newIncidentButton && device.getCurrentUser() != null)
    	{
    		EyeApplication.get().db_createIncident(Type.Security, device.getLocation());
    		Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    	}
    	else if (e.getSource() == locateOnMapButton)
    	{
    		User user  = device.getCurrentUser();
    		Debugger.i("Locate "+ (user == null ? device.toString() : user.getNameFull()) +" on map.");
    	}
    }

    @Override
    public void deviceInUse(Device device, User user)
    {
    	updateLabelState();
    	updateButtonState();
    }

	@Override
	public void deviceNotInUse(Device device, User oldUser)
	{
		updateLabelState();
		updateButtonState();
	}

	@Override
	public void deviceTimedOut(Device device)
	{
		updateLabelState();
		updateButtonState();
	}

	@Override
	public void deviceLocationChanged(Device device, Location oldLocation,
			Location newLocation)
	{
		updateLabelState();
		updateButtonState();
	}

	@Override
	public void deviceAssignedIncident(Device device, Incident incident)
	{
		updateLabelState();
		updateButtonState();
	}

	@Override
	public void deviceUnassignedIncident(Device device, Incident incident)
	{
		updateLabelState();
		updateButtonState();
	}
	@Override public void deviceAtmosphereChanged(Device device,
			Atmosphere oldAtmosphere, Atmosphere newAtmosphere) { }
	@Override public void deviceAddressChanged(Device device, InetAddress oldAddress,
			InetAddress newAddress) { }
	@Override public void deviceUpdated(Device device) { }
	
	//do not implement this
	@Override public void deviceCreated(Device device) { }
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private void updateLabelState()
	{
		//TODO: some sort of visual change when the device times out
		
		if (device.getCurrentUser() != null)
		{
			logo.setIcon(Incident.getIcon(device.getCurrentUser().getType(),true));
			name.setText(device.getCurrentUser().toString() + ": " + device.getCurrentUser().getNameFull());
			if (device.getCurrentIncident() != null)
			{
				status.setText(device.getCurrentIncident().toString());
				status.setBackground(new Color(0xfd0b15));
			}
			else
			{
				status.setText("Patrolling");
				status.setBackground(new Color(0x0a9a06));
			}
			location.setText(device.getLocation().hasLatLong() ? device.getLocation().toShortString() : "");
		}
		else
		{
			logo.setIcon(Incident.getIcon(Type.None,false));
			name.setText(device.toString());
			status.setText("No user.");
			status.setBackground(Color.gray);
			location.setText("");
		}
		repaint();
	}
	
	private void updateButtonState()
	{
		//set the button to visible/enabled only when it has a user assigned,
		//it's not currently responding to an event,
		//and it has lat/long components
		boolean latlong = device.getLocation().hasLatLong();
		newIncidentButton.setEnabled(device.getCurrentUser() != null && latlong);
		locateOnMapButton.setEnabled(latlong);
	}
}