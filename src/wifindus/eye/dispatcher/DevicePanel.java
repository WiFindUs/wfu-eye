package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import wifindus.ResourcePool;
import wifindus.eye.Atmosphere;
import wifindus.eye.Device;
import wifindus.eye.DeviceEventListener;
import wifindus.eye.Incident;
import wifindus.eye.Location;
import wifindus.eye.MapFrame;
import wifindus.eye.User;

public class DevicePanel extends MapFrameLinkedPanel
	implements ActionListener, DeviceEventListener
{
	private static final long serialVersionUID = -953467312117311967L;
    private transient volatile Device device = null;
    private transient JButton newIncidentButton, locateOnMapButton;
    private transient JLabel logo, name, location, status, deviceHash;
    
    static
    {
    	ResourcePool.loadImage("plus_small",  "images/plus_small.png");
    	ResourcePool.loadImage("locate_small", "images/locate_small.png");
    	ResourcePool.loadImage("none", "images/none.png");
    	ResourcePool.loadImage("medical", "images/medical.png");
    	ResourcePool.loadImage("security", "images/security.png");
    	ResourcePool.loadImage("wfu", "images/wfu.png");
    }

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a new DevicePanel, binding it to a particular device and responding to its events.
     * @param device The Device to bind to.
     * @throws NullPointerException if device is null.
     */
    public DevicePanel(Device device, MapFrame mapFrame)
    {
		super(mapFrame);
    	if (device == null)
			throw new NullPointerException("Parameter 'device' cannot be null.");
		this.device = device;
		boolean opaqueControls = true;
		Color opaqueColour = Color.white;
		
		//panel properties
		setLayout(null);
		setPreferredSize(new Dimension(380,90));
        setBorder(BorderFactory.createMatteBorder(1,0,1,0 , new Color(0x618197)));
        setMaximumSize(new Dimension(380,95));
        setMinimumSize(new Dimension(380,95));
        setBackground(Color.white);
        
        //user name
        name = new JLabel();
        name.setFont(getFont().deriveFont(Font.BOLD, 15.0f));
        name.setOpaque(opaqueControls);
        name.setBackground(opaqueColour);
        
        //device hash
        deviceHash = new JLabel();
        deviceHash.setHorizontalAlignment(SwingConstants.RIGHT);
        deviceHash.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 12));
        deviceHash.setForeground(Color.DARK_GRAY);
        deviceHash.setOpaque(opaqueControls);
        deviceHash.setBackground(opaqueColour);
        deviceHash.setText(device.getHash());
        
        //logo
        (logo = new JLabel()).setBackground(Color.white);
               
        //create a new incident button
        Font font = getFont().deriveFont(Font.BOLD, 13.0f);
        Border emptyBorder = BorderFactory.createEmptyBorder();
        newIncidentButton = new JButton("New Incident");
        newIncidentButton.setIcon(ResourcePool.getIcon("plus_small"));
        newIncidentButton.setBackground(opaqueColour);
        newIncidentButton.addActionListener(this);
        newIncidentButton.setHorizontalAlignment(SwingConstants.LEFT);
        newIncidentButton.setMargin(new Insets(0,0,0,0));
        newIncidentButton.setBorder(emptyBorder);
        newIncidentButton.setFont(font);
        
        //locate a device/user on the map button
        locateOnMapButton = new JButton("Locate on map");
        locateOnMapButton.setIcon(ResourcePool.getIcon("locate_small"));
        locateOnMapButton.setBackground(opaqueColour);
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
        status.setForeground(opaqueColour);
        status.setOpaque(opaqueControls);
        status.setBorder(BorderFactory.createEmptyBorder(0,15,0,15));
        
        //add controls
        add(name);
        add(logo);
        add(newIncidentButton);
        add(locateOnMapButton);
        add(status);
        add(deviceHash);
        
        //set control bounds
        name.setBounds(20, 3, 270, 20);
        logo.setBounds(20,30,60,60);
        newIncidentButton.setBounds(90,37,120,20);
        locateOnMapButton.setBounds(90,60,120,20);
        status.setBounds(250,45,100,25);
        deviceHash.setBounds(297,3,80,20);
        
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
   
    /**
     * Gets the Device associated with this DevicePanel. 
     * @return A reference to a Device object.
     */
    public final Device getDevice()
    {
    	return device;
    }
    
    // Listener for new incident, locate on map, archive and delete incident buttons
    @Override
    public void actionPerformed(ActionEvent e) 
    {
    	//everything this button does requires the device to have a valid location
    	if (!device.getLocation().hasLatLong())
    		return;
    	
    	//Listener for new incident
    	if (e.getSource() == newIncidentButton && device.getCurrentUser() != null)
    	{
    		IncidentTypeFrame selectIncidentType = new IncidentTypeFrame(device);
    		
    		selectIncidentType.addWindowListener( new WindowAdapter() {
                  @Override
                  public void windowClosing(WindowEvent we) {
                	  Dispatcher.get().setEnabled(true);
                  }
              } );
    		
    		selectIncidentType.setLocationRelativeTo(null);
    		selectIncidentType.setLocation(newIncidentButton.getLocationOnScreen());
    		selectIncidentType.setLocation(selectIncidentType.getX()+20, selectIncidentType.getY()+20);

    	}
		else if (e.getSource() == locateOnMapButton)
			locateObjectOnMap(device);
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
	
	public static final Comparator<DevicePanel> COMPARATOR_DEVICE_TYPE = new Comparator<DevicePanel>()
	{
		@Override
		public int compare(DevicePanel o1, DevicePanel o2)
		{
			int comparison = userComparatorCheck(o1,o2);
			if (comparison >= -1)
				return comparison;
			return o1.getDevice().getCurrentUserType().compareTo(o2.getDevice().getCurrentUserType());
		}
	};
	
	public static final Comparator<DevicePanel> COMPARATOR_USER_ID = new Comparator<DevicePanel>()
	{
		@Override
		public int compare(DevicePanel o1, DevicePanel o2)
		{
			int comparison = userComparatorCheck(o1,o2);
			if (comparison >= -1)
				return comparison;
			return Integer.compare(o1.getDevice().getCurrentUser().getID(),o2.getDevice().getCurrentUser().getID());
		}
	};
	
	public static final Comparator<DevicePanel> COMPARATOR_USER_NAME_FIRST = new Comparator<DevicePanel>()
	{
		@Override
		public int compare(DevicePanel o1, DevicePanel o2)
		{
			int comparison = userComparatorCheck(o1,o2);
			if (comparison >= -1)
				return comparison;
			return o1.getDevice().getCurrentUser().getNameFirst().compareTo(o2.getDevice().getCurrentUser().getNameFirst());
		}
	};
	
	public static final Comparator<DevicePanel> COMPARATOR_USER_NAME_LAST = new Comparator<DevicePanel>()
	{
		@Override
		public int compare(DevicePanel o1, DevicePanel o2)
		{
			int comparison = userComparatorCheck(o1,o2);
			if (comparison >= -1)
				return comparison;
			return o1.getDevice().getCurrentUser().getNameLast().compareTo(o2.getDevice().getCurrentUser().getNameLast());
		}
	};
	
	public static final Comparator<DevicePanel> COMPARATOR_ASSIGNED_FIRST = new Comparator<DevicePanel>()
	{
		@Override
		public int compare(DevicePanel o1, DevicePanel o2)
		{
			int comparison = userComparatorCheck(o1,o2);
			if (comparison >= -1)
				return comparison;

			Incident i1 = o1.getDevice().getCurrentIncident();
			Incident i2 = o2.getDevice().getCurrentIncident();
			if (i1 == i2)
				return 0;
			if (i1 == null)
				return 1;
			if (i2 == null)
				return -1;	
			return Integer.compare(i1.getID(), i2.getID());
		}
	};
	
	public static final Comparator<DevicePanel> COMPARATOR_UNASSIGNED_FIRST = new Comparator<DevicePanel>()
	{
		@Override
		public int compare(DevicePanel o1, DevicePanel o2)
		{
			int comparison = userComparatorCheck(o1,o2);
			if (comparison >= -1)
				return comparison;

			Incident i1 = o1.getDevice().getCurrentIncident();
			Incident i2 = o2.getDevice().getCurrentIncident();
			if (i1 == i2)
				return 0;
			if (i1 == null)
				return -1;
			if (i2 == null)
				return 1;	
			return Integer.compare(i1.getID(), i2.getID());
		}
	};

	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private static final int userComparatorCheck(DevicePanel o1, DevicePanel o2)
	{
		if (o1 == o2)
			return 0;
		if (o1 == null)
			return 1;
		if (o2 == null)
			return -1;	
		
		User u1 = o1.getDevice().getCurrentUser();
		User u2 = o2.getDevice().getCurrentUser();
		if (u1 == u2)
			return 0;
		if (u1 == null)
			return 1;
		if (u2 == null)
			return -1;
		
		return -1000;		
	}
	
	private void updateLabelState()
	{
		//TODO: some sort of visual change when the device times out
		
		if (device.getCurrentUser() != null)
		{
			User user = device.getCurrentUser();
			ImageIcon icon = null;
			switch (user.getType())
			{
				case Medical: icon = ResourcePool.getIcon("medical"); break;
				case Security: icon = ResourcePool.getIcon("security"); break;
				case WiFindUs: icon = ResourcePool.getIcon("wfu"); break;
				default: icon = ResourcePool.getIcon("none"); break;
			}
			logo.setIcon(icon);
			
			name.setText("[" + user.getID() + "] " + user.getNameFull());
			status.setVisible(true);
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
			
			logo.setIcon(ResourcePool.getIcon("none"));
			name.setText("Unused device");
			status.setVisible(false);
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
		newIncidentButton.setVisible(device.getCurrentUser() != null && latlong);
		locateOnMapButton.setVisible(latlong);
	}
}