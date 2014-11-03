package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import wifindus.HighResolutionTimerListener;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;
import wifindus.eye.Location;
import wifindus.eye.MapFrame;
import wifindus.eye.User;

public class IncidentPanel extends IncidentParentPanel implements IncidentEventListener, ActionListener, HighResolutionTimerListener
{
	private static final long serialVersionUID = -7397843910420550797L;
	private transient JLabel idLabel, onTaskLabel, descriptionLabel, incidentIconLabel, incidentTime;
	private transient JButton locateBtn, addRespondentBtn, removeRespondentBtn,
	deleteBtn, codeBtn, archiveBtn, saveDescriptionBtn;
	private transient JTextArea incidentDescription;

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
		
    	ResourcePool.loadImage("cog_inverted_themed", "images/cog_inverted_themed.png");
    	ResourcePool.loadImage("cross_inverted_themed", "images/cross_inverted_themed.png");
    	ResourcePool.loadImage("shield_inverted_themed", "images/shield_inverted_themed.png");
		
		ResourcePool.loadImage("delete", "images/delete.png");
		ResourcePool.loadImage("archive", "images/archive.png");
		ResourcePool.loadImage("save", "images/save.png");
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
		setMaximumSize(new Dimension(1000,230));
		setMinimumSize(new Dimension(600,230));
		Font font, rightColumnFont, idFont, btnFont;
		font = getFont().deriveFont(Font.BOLD, 15.0f);
		idFont = getFont().deriveFont(Font.BOLD, 17.0f);
		btnFont = getFont().deriveFont(Font.BOLD, 11.0f);
		rightColumnFont = getFont().deriveFont(Font.BOLD, 16.0f);

		//Layout
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
		GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		(idLabel = new JLabel("Incident #" + incident.getID())).setFont(idFont);
		incidentIconLabel = new JLabel();
		incidentIconLabel.setBorder(emptyBorder);
		incidentIconLabel.setBackground(lightBlue);
		updateButtonState();

		
		descriptionLabel = new JLabel ("Description:");
		descriptionLabel.setFont(font);
				
		incidentDescription = new JTextArea("", 5, 5);
		incidentDescription.setMinimumSize(new Dimension(270, 110));
		incidentDescription.setLineWrap(true);
		incidentDescription.setWrapStyleWord(true);
		incidentDescription.setText(incident.getDescription());
		
		incidentDescription.getDocument()
    	.addDocumentListener(new DocumentListener() 
		{
			@Override
			public void changedUpdate(DocumentEvent arg0)
			{

			}
			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				saveDescriptionBtn.setEnabled(true);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				saveDescriptionBtn.setEnabled(true);
			}
		});
		

		JScrollPane descriptionScroll = new JScrollPane(incidentDescription, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		descriptionScroll.setPreferredSize(new Dimension(270,110));
		descriptionScroll.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK), 
	            BorderFactory.createEmptyBorder(1, 1, 1, 1)));

		saveDescriptionBtn = new JButton("Save");
		saveDescriptionBtn.addActionListener(this);
		saveDescriptionBtn.setBackground(lightBlue);
		saveDescriptionBtn.setIcon(ResourcePool.getIcon("save"));
		saveDescriptionBtn.setBorder(emptyBorder);
		saveDescriptionBtn.setFont(btnFont);
		saveDescriptionBtn.setHorizontalAlignment(SwingConstants.LEFT);
		saveDescriptionBtn.setEnabled(false);
			
		locateBtn = new JButton("Locate");
		locateBtn.setBackground(lightBlue);
		locateBtn.setIcon(ResourcePool.getIcon("locate_small"));
		locateBtn.setBorder(emptyBorder);
		locateBtn.setFont(btnFont);
		locateBtn.setMinimumSize(new Dimension(40,50));
		locateBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		locateBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		locateBtn.addActionListener(this);

		addRespondentBtn = new JButton("Add");
		addRespondentBtn.setBackground(lightBlue);
		addRespondentBtn.setIcon(ResourcePool.getIcon("plus_small"));
		addRespondentBtn.setBorder(emptyBorder);
		addRespondentBtn.setFont(btnFont);
		addRespondentBtn.setHorizontalAlignment(SwingConstants.LEFT);
		addRespondentBtn.addActionListener(this);

		removeRespondentBtn = new JButton("Remove");
		removeRespondentBtn.setBackground(lightBlue);
		removeRespondentBtn.setIcon(ResourcePool.getIcon("minus_small"));
		removeRespondentBtn.setBorder(emptyBorder);
		removeRespondentBtn.setFont(btnFont);
		removeRespondentBtn.setHorizontalAlignment(SwingConstants.LEFT);
		removeRespondentBtn.addActionListener(this);

		deleteBtn = new JButton("Delete");
		deleteBtn.setBackground(lightBlue);
		deleteBtn.setIcon(ResourcePool.getIcon("delete"));
		deleteBtn.setBorder(emptyBorder);
		deleteBtn.setFont(btnFont);
		deleteBtn.setMinimumSize(new Dimension(40,50));
		deleteBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		deleteBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		deleteBtn.addActionListener(this);

		archiveBtn = new JButton("Archive");
		archiveBtn.setBackground(lightBlue);
		archiveBtn.setIcon(ResourcePool.getIcon("archive"));
		archiveBtn.setBorder(emptyBorder);
		archiveBtn.setFont(btnFont);
		archiveBtn.setMinimumSize(new Dimension(40,50));
		archiveBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		archiveBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		archiveBtn.addActionListener(this);

		onTaskLabel = new JLabel ("On Task:");
		onTaskLabel.setFont(font);

		assignedDevicesTable = new JTable(deviceTableModel);
		TableColumn distanceCol = assignedDevicesTable.getColumnModel().getColumn(COLUMN_DISTANCE);
		distanceCol.setMaxWidth(85);
		JTableHeader header = assignedDevicesTable.getTableHeader();
		header.setBackground(new Color(0xc8ddf2));
		
		JScrollPane onTaskListScroll = new JScrollPane(assignedDevicesTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		onTaskListScroll.setPreferredSize(new Dimension(270,110));
		onTaskListScroll.getViewport().setBackground(new Color(0xedf4fb));

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

		codeBtn = new JButton("code");
		codeBtn.setBackground(Color.gray);
		codeBtn.setForeground(Color.white);
		codeBtn.setFont(rightColumnFont);
		codeBtn.setBorder(emptyBorder);
		codeBtn.setMinimumSize(new Dimension(115,35));
		codeBtn.setMaximumSize(new Dimension(115,35));
		codeBtn.addActionListener(this);


		/* MODIFY ACCORDING TO CHANGES
          The panel is divided into 4 columns and 2 main rows.

          column(1): incident id and incident type icon
          column(2): locateBtn, addRespondentBtn, deleteBtn buttons
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

		
		//horizontal layout
		//horizontal layout: top row
		GroupLayout.SequentialGroup topRowSequential = layout.createSequentialGroup();
		GroupLayout.ParallelGroup columnIncidentType = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup columnTimer = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup columnButtons = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		
		//horizontal layout: bottom row
		GroupLayout.SequentialGroup bottomRowSequential = layout.createSequentialGroup();
		GroupLayout.ParallelGroup columnDescription = layout.createParallelGroup();
		GroupLayout.ParallelGroup columnList = layout.createParallelGroup();
		
		
		//horizontal layout: incident type column (top row)
		GroupLayout.SequentialGroup IncidentTypeRowSequential = layout.createSequentialGroup();
		IncidentTypeRowSequential.addComponent(incidentIconLabel, 0, GroupLayout.DEFAULT_SIZE, 40);
		IncidentTypeRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, 0);
		IncidentTypeRowSequential.addComponent(codeBtn, 115, GroupLayout.DEFAULT_SIZE, 115);
		columnIncidentType.addGroup(IncidentTypeRowSequential);
		
		//horizontal layout: timer column (top row)
		GroupLayout.SequentialGroup timerRowSequential = layout.createSequentialGroup();
		timerRowSequential.addComponent(timeIconLabel, 0, GroupLayout.DEFAULT_SIZE, 50);
		timerRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, 0);
		timerRowSequential.addComponent(incidentTime, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnTimer.addGroup(timerRowSequential);
		
		//horizontal layout: buttons column (top row)
		GroupLayout.SequentialGroup buttonsRowSequential = layout.createSequentialGroup();
		buttonsRowSequential.addComponent(locateBtn, 0, GroupLayout.DEFAULT_SIZE, 45);
		buttonsRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, 0);
		buttonsRowSequential.addComponent(archiveBtn, 0, GroupLayout.DEFAULT_SIZE, 45);
		buttonsRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, 0);
		buttonsRowSequential.addComponent(deleteBtn, 0, GroupLayout.DEFAULT_SIZE, 45);
		columnButtons.addGroup(buttonsRowSequential);
		
		//horizontal layout: description column (bottom row)
		GroupLayout.SequentialGroup descSequential = layout.createSequentialGroup();
		descSequential.addComponent(idLabel);
		descSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE);
		descSequential.addComponent(saveDescriptionBtn,  0, GroupLayout.DEFAULT_SIZE, 70);
		columnDescription.addGroup(descSequential);
		columnDescription.addComponent(descriptionScroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		//horizontal layout: on task column (bottom row)
		GroupLayout.SequentialGroup onTaskSequential = layout.createSequentialGroup();
		onTaskSequential.addComponent(onTaskLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		onTaskSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE);
		onTaskSequential.addComponent(addRespondentBtn, 0, GroupLayout.DEFAULT_SIZE, 50);
		onTaskSequential.addComponent(removeRespondentBtn, 0, GroupLayout.DEFAULT_SIZE, 80);
		columnList.addGroup(onTaskSequential);
		columnList.addComponent(onTaskListScroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		
		topRowSequential.addGroup(columnIncidentType);
		topRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 80, Short.MAX_VALUE);
		topRowSequential.addGroup(columnTimer);
		topRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 80, Short.MAX_VALUE);
		topRowSequential.addGroup(columnButtons);
		
		bottomRowSequential.addGroup(columnDescription);
		bottomRowSequential.addGroup(columnList);
		
		GroupLayout.ParallelGroup horizontalParallel = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		horizontalParallel.addGroup(topRowSequential);
		horizontalParallel.addGroup(bottomRowSequential);
		
		horizontal.addGroup(horizontalParallel);
		
		
		
		
		
		//vertical layout: main rows
		GroupLayout.ParallelGroup rowTop = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		GroupLayout.ParallelGroup rowBottom = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		
		//vertical layout: incident type column (top row)
		GroupLayout.ParallelGroup incidentTypeParallel = layout.createParallelGroup();
		incidentTypeParallel.addComponent(incidentIconLabel, 40, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		incidentTypeParallel.addComponent(codeBtn, 35, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		//vertical layout: timer column (top row)
		GroupLayout.ParallelGroup timeRowParallel = layout.createParallelGroup();
		timeRowParallel.addComponent(timeIconLabel, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		timeRowParallel.addComponent(incidentTime, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		//vertical layout: buttons column (top row)
		GroupLayout.ParallelGroup buttonRowParallel = layout.createParallelGroup();
		buttonRowParallel.addComponent(locateBtn, 40, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		buttonRowParallel.addComponent(archiveBtn, 40, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		buttonRowParallel.addComponent(deleteBtn, 40, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		rowTop.addGroup(incidentTypeParallel);
		rowTop.addGroup(timeRowParallel);
		rowTop.addGroup(buttonRowParallel);
	
		
		
		//vertical layout: description column (bottom row)
		GroupLayout.SequentialGroup descriptionGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup descRowParallel = layout.createParallelGroup();
		descRowParallel.addComponent(idLabel, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		descRowParallel.addComponent(saveDescriptionBtn, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		descriptionGroup.addGroup(descRowParallel);
		descriptionGroup.addComponent(descriptionScroll);
				
		//vertical layout: on task column (bottom row)
		GroupLayout.SequentialGroup onTaskGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup onTaskRowParallel = layout.createParallelGroup();
		onTaskRowParallel.addComponent(onTaskLabel, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		onTaskRowParallel.addComponent(addRespondentBtn, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		onTaskRowParallel.addComponent(removeRespondentBtn, 25, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		onTaskGroup.addGroup(onTaskRowParallel);
		onTaskGroup.addComponent(onTaskListScroll);
		
		//vertical: bottom row
		rowBottom.addGroup(descriptionGroup);
		rowBottom.addGroup(onTaskGroup);
		
		vertical.addGroup(rowTop);
		vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 50, 50);
		vertical.addGroup(rowBottom);

		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);

		//eye listeners
		incident.addEventListener(this);
		if (!incident.isArchived())
			EyeApplication.get().addTimerListener(this);
	}

	public void setPanelDescription(String description)
	{
		incidentDescription.setText(description);
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
		deviceTableModel.addRow(new Object[] {device, (int)device.getLocation().distanceTo(incident.getLocation()) + " m"});
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
	public void actionPerformed(ActionEvent e) 
	{
		//once the incident is archived, we should not be able to manipulate it (wouldn't make sense)
		if (getIncident().isArchived())
			return;
		if(e.getSource() == saveDescriptionBtn)
		{
			if (EyeApplication.get().db_setIncidentDescription(getIncident(), incidentDescription.getText()))
				saveDescriptionBtn.setEnabled(false);
		}
		else if(e.getSource() == addRespondentBtn)
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
		else if(e.getSource() == removeRespondentBtn)
		{
			for (Device device : getSelectedDevices())
				EyeApplication.get().db_setDeviceIncident(device, null); //null incident for 'unassigning'
		}
		else if(e.getSource() == archiveBtn)
			EyeApplication.get().db_archiveIncident(getIncident());
		else if (e.getSource() == locateBtn)
			locateObjectOnMap(getIncident());
		else if (e.getSource() == codeBtn)
    	{
    		ColourCodeFrame colourCodeFrame = new ColourCodeFrame(this);
    		colourCodeFrame.setLocation(codeBtn.getLocationOnScreen());
    		colourCodeFrame.setLocation(colourCodeFrame.getX()+40, colourCodeFrame.getY()+40);
    	}
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
			case Medical: icon = ResourcePool.getIcon("cross_inverted_themed"); break;
			case Security: icon = ResourcePool.getIcon("shield_inverted_themed"); break;
			case WiFindUs: icon = ResourcePool.getIcon("cog_inverted_themed"); break;
			default: icon = ResourcePool.getIcon("question_inverted_themed"); break;
		}
		incidentIconLabel.setIcon(icon);
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
	
	public void setCode(String code, Color color){
		codeBtn.setText(code);
		codeBtn.setBackground(color);
		if(code.equals("Yellow") || code.equals("Green"))
		{
			codeBtn.setForeground(Color.black);
		}
		else
		{
			codeBtn.setForeground(Color.white);
		}
	}
}





