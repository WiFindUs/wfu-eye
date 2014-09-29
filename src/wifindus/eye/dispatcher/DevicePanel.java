package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetAddress;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import wifindus.eye.Atmosphere;
import wifindus.eye.Device;
import wifindus.eye.DeviceEventListener;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.Location;
import wifindus.eye.User;
import wifindus.eye.Incident.Type;

public class DevicePanel extends JPanel implements ActionListener, ItemListener, DeviceEventListener
{
	private static final long serialVersionUID = -953467312117311967L;
    private transient volatile Device device = null;
    private transient JButton newMedicalButton, newSecurityButton, newWifibutton;
    private transient JCheckBox selectedCheckBox;
    private transient JPanel personnelDetailsPanel;
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
        setBorder(BorderFactory.createLineBorder(Color.black));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        Color trans = new Color(0f,0f,0f,0f);
      
        // Selected check box
        selectedCheckBox = new JCheckBox();
        selectedCheckBox.addItemListener(this);
        add(selectedCheckBox);
  
        //logo
        add(logo = new JLabel(Incident.getIcon(Type.None, false)));
        add(Box.createRigidArea(new Dimension(5,0)));
        logo.setBackground(trans);
        
        //personnel details
        add(personnelDetailsPanel = new JPanel());
        personnelDetailsPanel.setBackground(trans);
        personnelDetailsPanel.setLayout(new BoxLayout (personnelDetailsPanel, BoxLayout.X_AXIS));        
        Font font; 
        name = new JLabel();
        name.setFont(font = name.getFont().deriveFont(13.0f));
        name.setOpaque(true);
        name.setBackground(new Color(0, 0, 0, 10));
        location = new JLabel();
        location.setFont(font);
        status = new JLabel();
        status.setFont(font);
        
        JPanel panel = new JPanel();
        panel.setBackground(trans);
        panel.setLayout(new GridLayout (3,1));
        panel.add(name);
        panel.add(status);
        panel.add(location);
        personnelDetailsPanel.add(panel);
        personnelDetailsPanel.add(Box.createRigidArea(new Dimension(5,0)));
        
        //'create incident' panel and button
        personnelDetailsPanel.add(panel = new JPanel());
        panel.setBackground(trans);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("New Incident:"));
        panel.add(panel = new JPanel());
        panel.setBackground(trans);
        panel.setLayout(new GridLayout(1,3));
        panel.add(newMedicalButton = new JButton(Incident.getIcon(Type.Medical, true)));
        newMedicalButton.addActionListener(this);
        newMedicalButton.setMaximumSize(new Dimension(32,32));
        panel.add(newSecurityButton = new JButton(Incident.getIcon(Type.Security, true)));
        newSecurityButton.addActionListener(this);
        newSecurityButton.setMaximumSize(new Dimension(32,32));
        panel.add(newWifibutton = new JButton(Incident.getIcon(Type.WiFindUs, true)));
        newWifibutton.addActionListener(this);
        newWifibutton.setMaximumSize(new Dimension(32,32));
        
        personnelDetailsPanel.add(Box.createRigidArea(new Dimension(5,0)));
        
        deviceNotInUse(device,null);
        device.addEventListener(this);
    } 
    
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////

    @Override
    public void itemStateChanged(ItemEvent e) 
    {
    	setBackground(selectedCheckBox.isSelected() ? new Color(0xCCFFFF) : Color.WHITE);
    }
    
    // Listener for New Incident button 
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if (device.getCurrentUser() == null
        	|| device.getCurrentIncident() != null
        	|| !device.getLocation().hasLatLong())
        	return;
    	
    	if (e.getSource() == newMedicalButton)
        	EyeApplication.get().db_createIncident(Type.Medical, device.getLocation());
    	else if (e.getSource() == newSecurityButton)
        	EyeApplication.get().db_createIncident(Type.Security, device.getLocation());
    	else if (e.getSource() == newWifibutton)
        	EyeApplication.get().db_createIncident(Type.WiFindUs, device.getLocation());
    }

    // Return if a person is selected
    public final boolean isSelected()
    {
        return selectedCheckBox.isSelected();
    }

    @Override
    public void deviceInUse(Device device, User user)
    {
    	updateLabelState();
    	updateButtonState();
    }

	@Override
	public void deviceNotInUse(Device device, User user)
	{
		updateLabelState();
		updateButtonState();
	}

	@Override
	public void deviceTimedOut(Device device)
	{

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
		if (device.getCurrentUser() != null)
		{
			logo.setIcon(Incident.getIcon(device.getCurrentUser().getType(),false));
			name.setText(device.getCurrentUser().toString() + ": " + device.getCurrentUser().getNameFull());
			if (device.getCurrentIncident() != null)
			{
				status.setText("Assigned to " + device.getCurrentIncident().toString());
				status.setForeground(Color.red);
			}
			else
			{
				status.setText("Patrolling");
				status.setForeground(new Color(0x006600));
			}
			location.setText(device.getLocation().hasLatLong() ? device.getLocation().toShortString() : "");
		}
		else
		{
			logo.setIcon(Incident.getIcon(Type.None,false));
			name.setText(device.toString());
			status.setText("No user.");
			status.setForeground(Color.gray);
			location.setText("");
		}
		repaint();
	}
	
	private void updateButtonState()
	{
		//set the button to visible/enabled only when it has a user assigned,
		//it's not currently responding to an event,
		//and it has lat/long components
		boolean enabled = 
			device.getCurrentUser() != null
			&& device.getCurrentIncident() == null
			&& device.getLocation().hasLatLong();
		
		newMedicalButton.setEnabled(enabled);
		newSecurityButton.setEnabled(enabled);
		newWifibutton.setEnabled(enabled);
	}
}