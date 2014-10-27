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
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import wifindus.Debugger;
import wifindus.HighResolutionTimerListener;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;
import wifindus.eye.Location;
import wifindus.eye.MapFrame;
import wifindus.eye.MapPanel;
import wifindus.eye.User;

public class IncidentPanel extends IncidentParentPanel implements IncidentEventListener, ActionListener, HighResolutionTimerListener
{
	private static final long serialVersionUID = -7397843910420550797L;
	private transient  JLabel incidentTime;
	private transient JLabel idLabel;
	private transient JLabel onTaskLabel;
	private transient JButton locateOnMap, addRespondent, removeRespondent,
	removeIncident, codeButton, statusButton, incidentIconButton, archiveIncident;

	private double timerCounter = 0.0;
	private static final int COLUMN_DEVICE = 0;
	private static final int COLUMN_DISTANCE = 1;
	private transient DefaultTableModel deviceTableModel = new DefaultTableModel(
			new Object[][]{},
			new String[] {"Device", "Distance"}
			);
	private transient JTable assignedDevicesTable;

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

	public IncidentPanel(Incident incident, MapFrame mapFrame)
	{
		super(incident, mapFrame);

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
		locateOnMap.addActionListener(this);

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

		archiveIncident = new JButton("Archive Incident");
		archiveIncident.setBackground(lightBlue);
		archiveIncident.setIcon(ResourcePool.getIcon("minus_small"));
		archiveIncident.setBorder(emptyBorder);
		archiveIncident.setFont(font);
		archiveIncident.setHorizontalAlignment(SwingConstants.LEFT);
		archiveIncident.addActionListener(this);

		onTaskLabel = new JLabel ("On Task:");
		onTaskLabel.setFont(font);

		assignedDevicesTable = new JTable(deviceTableModel);

		JScrollPane onTaskListScroll = new JScrollPane(assignedDevicesTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		onTaskListScroll.setPreferredSize(new Dimension(200,70));

		//time
		ImageIcon timeIcon = new ImageIcon("images/time.png");
		Image timeIconImage = timeIcon.getImage() ; 
		Image scaledTimeIcon = timeIconImage.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );  
		timeIcon = new ImageIcon(scaledTimeIcon);
		JLabel timeIconLabel = new JLabel(timeIcon);
		timeIconLabel.setMinimumSize(new Dimension(50,100));
		incidentTime = new JLabel();
		incidentTime.setFont(idFont);
		updateTimerLabel(0);

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

		columnButtons.addComponent(archiveIncident, 0, GroupLayout.DEFAULT_SIZE, 150);
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
		buttonGroup.addComponent(archiveIncident);
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

		//eye listeners
		incident.addEventListener(this);
		if (!incident.isArchived())
			EyeApplication.get().addTimerListener(this);
	}

	@Override
	public void incidentArchived(Incident incident)
	{
		incident.removeEventListener(this);
		deviceTableModel.setNumRows(0);
		EyeApplication.get().removeTimerListener(this);
		updateTimerLabel(0);
	}

	@Override
	public void incidentAssignedDevice(Incident incident, Device device)
	{
		if (device == null)
			return;
		
		for(int i = 0; i < deviceTableModel.getRowCount(); i++)
			if (deviceTableModel.getValueAt(i,COLUMN_DEVICE) == device)
				return;
		deviceTableModel.addRow(new Object[] {device, (int)device.getLocation().distanceTo(incident.getLocation()) + " meters"});
	}

	@Override
	public void incidentUnassignedDevice(Incident incident, Device device)
	{
		if (device == null)
			return;

		for(int i = 0; i < deviceTableModel.getRowCount(); i++)
		{
			if (device != null && deviceTableModel.getValueAt(i,COLUMN_DEVICE) == device)
			{
				deviceTableModel.removeRow(i);
				break;
			}
		}
	}


	@Override
	public void incidentSelectionChanged(Incident incident)
	{
		//TODO: visually reflect selection state in some way
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		//once the incident is archived, we should not be able to manipulate it (wouldn't make sense)
		if (getIncident().isArchived())
			return;

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
						|| device.getCurrentUser().getType() != getIncident().getType())
					continue;

				//if this is the first, assign it immediately as 'closest'
				if (closestAvailableDevice == null)
				{
					closestAvailableDevice = device;
					minDist = closestAvailableDevice.getLocation().distanceTo(getIncident().getLocation());
				}
				else //check against previous closest
				{
					//get test distance
					double currDist = device.getLocation().distanceTo(getIncident().getLocation());

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
				EyeApplication.get().db_setDeviceIncident(closestAvailableDevice, getIncident());
		}
		else if(e.getSource() == removeRespondent)
		{
			for (Device device : getSelectedDevices())
				EyeApplication.get().db_setDeviceIncident(device, null); //null incident for 'unassigning'
		}
		else if(e.getSource() == archiveIncident)
			EyeApplication.get().db_archiveIncident(getIncident());
		else if (e.getSource() == locateOnMap)
			locateObjectOnMap(getIncident());
	}

	/**
	 * Gets all assigned devices currently selected in the incident's device/user list. 
	 * @return A List<Device> containing all selected devices. The returned value is a copy of the original list, so altering it will have no effect on the panel's list. 
	 */
	public List<Device> getSelectedDevices()
	{
		List<Device> deviceList = new ArrayList<Device>();
		for (int row : assignedDevicesTable.getSelectedRows())
			deviceList.add((Device)deviceTableModel.getValueAt(row,COLUMN_DEVICE));
		return deviceList;
	}

	//TODO: implement DeviceEventListener on this class when a device is added, and respond to location that way
	public void setDeviceLocation(Device device, Location newLocation)
	{
		for(int i = 0; i < deviceTableModel.getRowCount(); i++)
		{
			if (deviceTableModel.getValueAt(i,COLUMN_DEVICE) == device)
			{
				deviceTableModel.setValueAt((int)device.getLocation().distanceTo(getIncident().getLocation()) + " meters", i, COLUMN_DISTANCE);
				break;
			}
		}
	}
	
	@Override
	public void timerTick(double deltaTime)
	{
		if (getIncident().isArchived())
			return;
		
		timerCounter += deltaTime;
		if (timerCounter >= 1.0)
		{
			timerCounter -= 1.0;
			updateTimerLabel(System.currentTimeMillis());			
		}
	}
	
	@Override public void incidentArchivedResponderAdded(Incident incident, User user) { }
	@Override public void incidentSeverityChanged(Incident incident, int oldSeverity, int newSeverity) { }
	@Override public void incidentCodeChanged(Incident incident, String oldCode, String newCode) { }
	@Override public void incidentReportingUserChanged(Incident incident, User oldUser, User newUser) {	}
	@Override public void incidentDescriptionChanged(Incident incident) { }

	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////

	private void updateButtonState()
	{
		ImageIcon icon = null;
		switch (getIncident().getType())
		{
			case Medical: icon = ResourcePool.getIcon("medical"); break;
			case Security: icon = ResourcePool.getIcon("security"); break;
			case WiFindUs: icon = ResourcePool.getIcon("wfu"); break;
			default: icon = ResourcePool.getIcon("none"); break;
		}
		incidentIconButton.setIcon(icon);
	}
	
	private void updateTimerLabel(long endTimestamp)
	{
		if (endTimestamp <= 0)
			endTimestamp = System.currentTimeMillis();
		
		long seconds = (endTimestamp - getIncident().getCreated().getTime()) / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		
		incidentTime.setText(String.format("%02d : %02d : %02d", hours % 24, minutes % 60, seconds % 60));
	}
}





