package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;

public class IncidentPanel extends JPanel implements IncidentEventListener, ActionListener
{
	private static final long serialVersionUID = -7397843910420550797L;
    private transient Incident incident = null;
    private transient JLabel incidentTime, idLabel, onTaskLabel;
    private transient JButton locateOnMap, addRespondent, removeRespondent,
    	removeIncident, codeButton, statusButton, incidentIconButton;
	private transient JList<Device> onTaskList;
	private transient DefaultListModel<Device> onTaskListModel;
	
    static
    {
    	ResourcePool.loadImage("plus_small",  "images/plus_small.png");
    	ResourcePool.loadImage("minus_small",  "images/minus_small.png");
    	ResourcePool.loadImage("locate_small", "images/locate_small.png");
    	ResourcePool.loadImage("none", "images/none.png");
    	ResourcePool.loadImage("medical", "images/medical.png");
    	ResourcePool.loadImage("security", "images/security.png");
    	ResourcePool.loadImage("wfu", "images/wfu.png");
    }
     
    public IncidentPanel(Incident incident)
    {
		if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		this.incident = incident;
    	
		//cosmetic properties
		Color lightBlue = new Color(0xf6f9fc);
		Color red = new Color(0xfd0b15);
		setBackground(lightBlue);
		setBorder(BorderFactory.createMatteBorder(0,1,1,0,new Color(0x618197)));
        Border emptyBorder = BorderFactory.createEmptyBorder();
        setMaximumSize(new Dimension(1000,150));
        setMinimumSize(new Dimension(500,150));
        
        Font font, rightColumnFont, idFont;
        font = getFont().deriveFont(Font.BOLD, 15.0f);
        idFont = getFont().deriveFont(Font.BOLD, 17.0f);
        rightColumnFont = getFont().deriveFont(Font.BOLD, 16.0f);

        //Layout
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        (idLabel = new JLabel("Incident #" + incident.getID())).setFont(idFont);
        incidentIconButton = new JButton();
        incidentIconButton.setBorder(emptyBorder);
        incidentIconButton.setBackground(lightBlue);
        updateButtonState();
       
        locateOnMap = new JButton("Locate on Map");
        locateOnMap.setBackground(lightBlue);
        locateOnMap.setIcon(ResourcePool.getIcon("locate_small"));
        locateOnMap.setBorder(emptyBorder);
        locateOnMap.setFont(font);
        locateOnMap.setHorizontalAlignment(SwingConstants.LEFT);
        
        addRespondent = new JButton("Add Respondent");
        addRespondent.setBackground(lightBlue);
        addRespondent.setIcon(ResourcePool.getIcon("plus_small"));
        addRespondent.setBorder(emptyBorder);
        addRespondent.setFont(font);
        addRespondent.setHorizontalAlignment(SwingConstants.LEFT);
        addRespondent.addActionListener(this);
        
        removeRespondent = new JButton("Remove Respondent");
        removeRespondent.setBackground(lightBlue);
        removeRespondent.setIcon(ResourcePool.getIcon("plus_small"));
        removeRespondent.setBorder(emptyBorder);
        removeRespondent.setFont(font);
        removeRespondent.setHorizontalAlignment(SwingConstants.LEFT);
        removeRespondent.addActionListener(this);
        
        removeIncident = new JButton("Remove Incident");
        removeIncident.setBackground(lightBlue);
        removeIncident.setIcon(ResourcePool.getIcon("minus_small"));
        removeIncident.setBorder(emptyBorder);
        removeIncident.setFont(font);
        removeIncident.setHorizontalAlignment(SwingConstants.LEFT);
        removeIncident.addActionListener(this);
        
        onTaskLabel = new JLabel ("On Task:");
        onTaskLabel.setFont(font);
        onTaskList = new JList<Device>(onTaskListModel = new DefaultListModel<Device>());
        JScrollPane onTaskListScroll = new JScrollPane(onTaskList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        onTaskListScroll.setPreferredSize(new Dimension(200,70));
        
        //time
        ImageIcon timeIcon = new ImageIcon("images/time.png");
        Image timeIconImage = timeIcon.getImage() ; 
        Image scaledTimeIcon = timeIconImage.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );  
        timeIcon = new ImageIcon(scaledTimeIcon);
        JLabel timeIconLabel = new JLabel(timeIcon);
        timeIconLabel.setMinimumSize(new Dimension(50,100));
        incidentTime = new JLabel("00 : 00 : 00");
        incidentTime.setFont(idFont);
        
        codeButton = new JButton("code");
        codeButton.setBackground(Color.gray);
        codeButton.setForeground(Color.white);
        codeButton.setFont(rightColumnFont);
        codeButton.setBorder(emptyBorder);
        codeButton.setMinimumSize(new Dimension(186,30));
        
        statusButton = new JButton("Active");
        statusButton.setBackground(red);
        statusButton.setForeground(Color.white);
        statusButton.setFont(rightColumnFont);
        statusButton.setBorder(emptyBorder);
        statusButton.setMinimumSize(new Dimension(186,30));
        
        /*
          The panel is divided into 4 columns and 2 main rows.
          
          column(1): incident id and incident type icon
          column(2): locateOnMap, addRespondent, removeIncident buttons
          column(3): on task label and list
          column(4): time, colour code, status
          
          row(1): incident id
          row(2): incident type icon and column(2)-column(4)
          
          Horizontal sequential group:
          	contains 4 parallel groups; column(1)-column(4)
          			 2 sequential groups:
          			 	(a)indenting gap + icon button
          			 	(b)timer icon + timer
          
          Vertical sequential group:
          	contains incident id and 1 parallel group
          	This parallel group contains:
          		incident type icon and 3 sequential groups; column(2)-column(4)
         */
        
        //horizontal
        GroupLayout.ParallelGroup columnLeft = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup columnButtons = layout.createParallelGroup();
        GroupLayout.ParallelGroup columnList = layout.createParallelGroup();
        GroupLayout.ParallelGroup columnRight = layout.createParallelGroup();
        GroupLayout.SequentialGroup timeRowSequential = layout.createSequentialGroup();
        GroupLayout.SequentialGroup iconSequential = layout.createSequentialGroup();
        
        iconSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 50);
        iconSequential.addComponent(incidentIconButton, 0, GroupLayout.DEFAULT_SIZE, 100);
        columnLeft.addComponent(idLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnLeft.addGroup(iconSequential);
        
        columnButtons.addComponent(locateOnMap, 0, GroupLayout.DEFAULT_SIZE, 150);
        columnButtons.addComponent(addRespondent, 0, GroupLayout.DEFAULT_SIZE, 150);
        columnButtons.addComponent(removeIncident, 0, GroupLayout.DEFAULT_SIZE, 150);
        
        columnButtons.addComponent(removeRespondent, 0, GroupLayout.DEFAULT_SIZE, 150);
        
        columnList.addComponent(onTaskLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnList.addComponent(onTaskListScroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        timeRowSequential.addComponent(timeIconLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        timeRowSequential.addComponent(incidentTime, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        columnRight.addGroup(timeRowSequential);
        columnRight.addComponent(codeButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnRight.addComponent(statusButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        horizontal.addGroup(columnLeft);
        horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 30, 40);
        horizontal.addGroup(columnButtons);
        horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 40, Short.MAX_VALUE);
        horizontal.addGroup(columnList);
        horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 40, Short.MAX_VALUE);
        horizontal.addGroup(columnRight);
        
        //vertical
        GroupLayout.ParallelGroup rowBottom = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        GroupLayout.SequentialGroup buttonGroup = layout.createSequentialGroup();
        GroupLayout.SequentialGroup onTaskGroup = layout.createSequentialGroup();
        GroupLayout.SequentialGroup rightGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup timeRowParallel = layout.createParallelGroup();
        
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 10);
        buttonGroup.addComponent(locateOnMap);
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 10);
        buttonGroup.addComponent(addRespondent);
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 10);
        buttonGroup.addComponent(removeRespondent);
        buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 10);
        buttonGroup.addComponent(removeIncident);
        
        onTaskGroup.addComponent(onTaskLabel);
        onTaskGroup.addComponent(onTaskListScroll);
        
        timeRowParallel.addComponent(timeIconLabel, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        timeRowParallel.addComponent(incidentTime, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        rightGroup.addGroup(timeRowParallel);
        rightGroup.addComponent(codeButton);
        rightGroup.addComponent(statusButton);
        
        rowBottom.addComponent(incidentIconButton, 0, GroupLayout.DEFAULT_SIZE, 100);
        rowBottom.addGroup(buttonGroup);
        rowBottom.addGroup(onTaskGroup);
        rowBottom.addGroup(rightGroup);
        
        vertical.addComponent(idLabel);
        vertical.addGroup(rowBottom);
        
        layout.setHorizontalGroup(horizontal);
        layout.setVerticalGroup(vertical);
   
        incident.addEventListener(this);
    }
    
	@Override
	public void incidentArchived(Incident incident)
	{
		onTaskListModel.clear();
	}
	
	@Override
	public void incidentAssignedDevice(Incident incident, Device device)
	{
		if (device != null && !onTaskListModel.contains(device))
			onTaskListModel.addElement(device);
	}
	
	@Override
	public void incidentUnassignedDevice(Incident incident, Device device)
	{
		if (device != null && onTaskListModel.contains(device))
			onTaskListModel.removeElement(device);
	}
	
	@Override
	public void incidentSelectionChanged(Incident incident)
	{
		//TODO: visually reflect selection state in some way
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == addRespondent)
		{
			//start out with no choice, set initial smallest distance to something massive
			double minDist = Double.MAX_VALUE;
			Device closestAvailableDevice = null;

			for (Device device : EyeApplication.get().getDevices())
			{
				//skip invalid options
				if (device.getCurrentIncident() != null
						|| !device.getLocation().hasLatLong()
						|| device.getCurrentUser() == null
						|| device.getCurrentUser().getType() != incident.getType())
					continue;

				//if this is the first, assign it immediately as 'closest'
				if (closestAvailableDevice == null)
				{
					closestAvailableDevice = device;
					minDist = closestAvailableDevice.getLocation().distanceTo(incident.getLocation());
				}
				else //check against previous closest
				{
					//get test distance
					double currDist = device.getLocation().distanceTo(incident.getLocation());

					//if it's smaller, this one is closer
					if (currDist < minDist)
					{
						minDist = currDist;
						closestAvailableDevice = device;
					}
				}
			}

			//if we found a valid, available device, assign it, yo
			if (closestAvailableDevice != null)
				EyeApplication.get().db_setDeviceIncident(closestAvailableDevice, incident);
		}
		else if(e.getSource() == removeRespondent)
		{
			for (Device device : getSelectedDevices())
				EyeApplication.get().db_setDeviceIncident(device, null); //null incident for 'unassigning'
		}
	}
	
	/**
	 * Gets all assigned devices currently selected in the incident's device/user list. 
	 * @return A List<Device> containing all selected devices. The returned value is a copy of the original list, so altering it will have no effect on the panel's list. 
	 */
	public List<Device> getSelectedDevices()
	{
		return new ArrayList<Device>(onTaskList.getSelectedValuesList());
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
      
	private void updateButtonState()
	{
		ImageIcon icon = null;
		switch (incident.getType())
		{
			case Medical: icon = ResourcePool.getIcon("medical"); break;
			case Security: icon = ResourcePool.getIcon("security"); break;
			case WiFindUs: icon = ResourcePool.getIcon("wfu"); break;
			default: icon = ResourcePool.getIcon("none"); break;
		}
		incidentIconButton.setIcon(icon);
	}



}



    

