package wifindus.eye.dispatcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.Location;
import wifindus.eye.MapFrame;
import wifindus.eye.MapRenderer;
import wifindus.eye.User;

/**
 * A specialized form of {@link EyeApplication} that provides controls for
 * a dispatch control center to monitor personnel state, create and respond
 * to incidents, etc. 
 * @author Mark 'marzer' Gillard, Hussein Al Hammad, Mitchell Templeton
 */
public class Dispatcher extends EyeApplication 
{
	private static final long serialVersionUID = 12094147960785467L;
	private static final String[] sortModes = {
		"User's type",
		"User's ID",
		"User's First Name",
		"User's Last Name",
		"Available for assignment",
		"Currently on assignment",
		"Devices without a user"
	};
	private transient JPanel queryPanel, incidentPanel, archivedIncidentPanel, devicePanel;
	private transient JComboBox<String> sortComboBox;
	private final transient ArrayList<DevicePanel> devicePanels = new ArrayList<>();
	private final transient Map<Integer,IncidentPanel> incidentPanels = new TreeMap<>();
	private final transient Map<Integer,ArchivedIncidentPanel> archivedIncidentPanels = new TreeMap<>();
	private transient JTextField searchTextField;
	private transient MapRenderer mapRenderer;
	private transient MapFrame mapFrame;
	private final transient List<Incident.Type> filteredTypes = new ArrayList<Incident.Type>();
	private final transient JToggleButton unusedFilterButton, medicalFilterButton, securityFilterButton, techFilterButton;
	
    static
    {
    	ResourcePool.loadImage("cog_inverted", "images/cog_inverted.png");
    	ResourcePool.loadImage("cross_inverted", "images/cross_inverted.png");
    	ResourcePool.loadImage("shield_inverted", "images/shield_inverted.png");
    	ResourcePool.loadImage("question_inverted", "images/question_inverted.png");
    	ResourcePool.loadImage("cog_inverted_themed", "images/cog_inverted_themed.png");
    	ResourcePool.loadImage("cross_inverted_themed", "images/cross_inverted_themed.png");
    	ResourcePool.loadImage("shield_inverted_themed", "images/shield_inverted_themed.png");
    	ResourcePool.loadImage("question_inverted_themed", "images/question_inverted_themed.png");
    }

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Dispatcher(String[] args)
	{
		//call parent ctor
		super(args);
		
		//get parent content pane
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(Color.white);
	
		// query Panel
		(queryPanel = new JPanel()).setBackground(new Color(0xedf4fb));
		queryPanel.setMinimumSize(new Dimension(397, 150));
		queryPanel.setMaximumSize(new Dimension(397, 150));
		GroupLayout queryPanelLayout = new GroupLayout(queryPanel);
		queryPanel.setLayout(queryPanelLayout);
        GroupLayout.SequentialGroup queryPanelLayoutHorizontal = queryPanelLayout.createSequentialGroup();
        GroupLayout.SequentialGroup queryPanelLayoutVertical = queryPanelLayout.createSequentialGroup();
        queryPanelLayout.setAutoCreateGaps(true);
        queryPanelLayout.setAutoCreateContainerGaps(true);
		
        unusedFilterButton = new JToggleButton(ResourcePool.getIcon("question_inverted"));
        medicalFilterButton = new JToggleButton(ResourcePool.getIcon("cross_inverted"));
        securityFilterButton = new JToggleButton(ResourcePool.getIcon("shield_inverted"));
        techFilterButton = new JToggleButton(ResourcePool.getIcon("cog_inverted"));
        
        ItemListener filterButtonListener = new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				JToggleButton source = (JToggleButton)e.getSource();
				boolean add = source.isSelected();
				Incident.Type type = Incident.Type.None;
				if (source == medicalFilterButton)
				{
					type = Incident.Type.Medical;
					medicalFilterButton.setIcon(ResourcePool.getIcon("cross_inverted" + (add ? "_themed" : "")));
				}
				else if (source == securityFilterButton)
				{
					type = Incident.Type.Security;
					securityFilterButton.setIcon(ResourcePool.getIcon("shield_inverted" + (add ? "_themed" : "")));
				}
				else if (source == techFilterButton)
				{
					type = Incident.Type.WiFindUs;
					techFilterButton.setIcon(ResourcePool.getIcon("cog_inverted" + (add ? "_themed" : "")));
				}
				else if (source == unusedFilterButton)
				{
					type = Incident.Type.None;
					unusedFilterButton.setIcon(ResourcePool.getIcon("question_inverted" + (add ? "_themed" : "")));
				}
				
				if (add)
				{
					if (!filteredTypes.contains(type))
						filteredTypes.add(type);
				}
				else
					filteredTypes.remove(type);
				
				updateDeviceFilter();
			}
		};
		unusedFilterButton.addItemListener(filterButtonListener);
        medicalFilterButton.addItemListener(filterButtonListener);
        securityFilterButton.addItemListener(filterButtonListener);
        techFilterButton.addItemListener(filterButtonListener);
        
        unusedFilterButton.setSelected(true);
        medicalFilterButton.setSelected(true);
        securityFilterButton.setSelected(true);
        techFilterButton.setSelected(true);
        
		// search
        JLabel searchLabel = new JLabel("Search:");
        (searchTextField = new JTextField()).getDocument()
        	.addDocumentListener(new DocumentListener() 
			{
				@Override
				public void changedUpdate(DocumentEvent arg0)
				{

				}
				@Override
				public void insertUpdate(DocumentEvent arg0)
				{
					updateDeviceFilter();
					devicePanel.revalidate();
				}
				@Override
				public void removeUpdate(DocumentEvent arg0)
				{
					updateDeviceFilter();
					devicePanel.revalidate();
				}
			});
		
		// sort
		JLabel sortLabel = new JLabel("Sort by:");
		sortComboBox = new JComboBox<String>(sortModes);
		sortComboBox.setVisible(true);
		sortComboBox.setSelectedIndex(0);
		sortComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				updateDeviceSort();
				devicePanel.revalidate();
			}
		});
		
		sortComboBox.setMaximumSize(new Dimension(295,25));
		sortComboBox.setMinimumSize(new Dimension(295,25));
		searchTextField.setMaximumSize(new Dimension(295,25));
		searchTextField.setMinimumSize(new Dimension(295,25));
		
		//queryPanelLayoutHorizontal
        GroupLayout.ParallelGroup queryColumn = queryPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER);
        GroupLayout.SequentialGroup rowButtons = queryPanelLayout.createSequentialGroup();
        GroupLayout.SequentialGroup rowBottom = queryPanelLayout.createSequentialGroup();
        GroupLayout.ParallelGroup columnLabels = queryPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup columnSortSearch = queryPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        
        rowButtons.addComponent(medicalFilterButton);
        rowButtons.addComponent(securityFilterButton);
        rowButtons.addComponent(techFilterButton);
        rowButtons.addComponent(unusedFilterButton);
        
        columnLabels.addComponent(sortLabel);
        columnLabels.addComponent(searchLabel);
        columnSortSearch.addComponent(sortComboBox);
        columnSortSearch.addComponent(searchTextField);
        
        rowBottom.addGroup(columnLabels);
        rowBottom.addGroup(columnSortSearch);
        
        queryColumn.addGroup(rowButtons);
        queryColumn.addGroup(rowBottom);
        
        queryPanelLayoutHorizontal.addGroup(queryColumn);
        
        //queryPanelLayoutVertical
        GroupLayout.ParallelGroup rowButtonsParallel = queryPanelLayout.createParallelGroup();
        GroupLayout.ParallelGroup rowSortParallel = queryPanelLayout.createParallelGroup();
        GroupLayout.ParallelGroup rowSearchParallel = queryPanelLayout.createParallelGroup();
        
        rowButtonsParallel.addComponent(unusedFilterButton);
        rowButtonsParallel.addComponent(medicalFilterButton);
        rowButtonsParallel.addComponent(securityFilterButton);
        rowButtonsParallel.addComponent(techFilterButton);
        
        rowSortParallel.addComponent(sortLabel);
        rowSortParallel.addComponent(sortComboBox);
        
        rowSearchParallel.addComponent(searchLabel);
        rowSearchParallel.addComponent(searchTextField);
        
        queryPanelLayoutVertical.addGroup(rowButtonsParallel);
        queryPanelLayoutVertical.addGroup(rowSortParallel);
        queryPanelLayoutVertical.addGroup(rowSearchParallel);
        
        queryPanelLayout.setHorizontalGroup(queryPanelLayoutHorizontal);
        queryPanelLayout.setVerticalGroup(queryPanelLayoutVertical);
        
        // device Panel
        devicePanel = new JPanel();
		devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
		JScrollPane devicePanelScroll = new JScrollPane(devicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // device control Panel: contains query and device panels
        JPanel deviceControlPanel = new JPanel();
		
		//Layout
        GroupLayout layout = new GroupLayout(deviceControlPanel);
        deviceControlPanel.setLayout(layout);
        GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
		
		//deviceControlPanel horizontal
        GroupLayout.ParallelGroup column = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        column.addComponent(queryPanel);
        column.addComponent(devicePanelScroll);
        horizontal.addGroup(column);
        layout.setHorizontalGroup(horizontal);
        
        //deviceControlPanel vertical
        vertical.addComponent(queryPanel);
        vertical.addComponent(devicePanelScroll);
        layout.setVerticalGroup(vertical);
        
		incidentPanel = new JPanel();
        incidentPanel.setLayout(new BoxLayout(incidentPanel, BoxLayout.Y_AXIS));
        incidentPanel.setBackground(Color.WHITE);
        JScrollPane incidentPanelScroll = new JScrollPane(incidentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        archivedIncidentPanel = new JPanel();
        archivedIncidentPanel.setLayout(new BoxLayout(archivedIncidentPanel, BoxLayout.Y_AXIS));
        archivedIncidentPanel.setBackground(Color.WHITE);
        JScrollPane archivedIncidentPanelScroll = new JScrollPane(archivedIncidentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JTabbedPane incidentTabs = new JTabbedPane();
        incidentTabs.add("Active Incidents", incidentPanelScroll);
        incidentTabs.add("Archived Incidents", archivedIncidentPanelScroll);
        
        contentPane.add(deviceControlPanel, BorderLayout.WEST);
        contentPane.add(incidentTabs, BorderLayout.CENTER);

		incidentPanel.revalidate();
		incidentPanel.repaint();
		
		//create map renderer
		mapRenderer = new MapRenderer(
			getConfig().getDouble("map.center_latitude", Location.GPS_MARKS_HOUSE.getLatitude().doubleValue()),
			getConfig().getDouble("map.center_longitude", Location.GPS_MARKS_HOUSE.getLongitude().doubleValue()),
			getConfig().getString("map.api_key"),
			getConfig().getInt("map.grid_rows", 10),
			getConfig().getInt("map.grid_columns", 10),
			getConfig().getDouble("map.grid_scale_x", 1.0),
			getConfig().getDouble("map.grid_scale_y", 1.0));

		//spawn map window
		(mapFrame = new MapFrame(mapRenderer)).setVisible(true);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
   
	@Override
	public void deviceCreated(Device device)
	{
		super.deviceCreated(device);
		DevicePanel newPanel = new DevicePanel(device, mapFrame);
		devicePanels.add(newPanel);
		
		updateDeviceSort();
		updateDeviceFilter();
		devicePanel.revalidate();
	}
	
	@Override
	public void incidentCreated(Incident incident)
	{
		super.incidentCreated(incident);
		
		IncidentPanel newPanel = new IncidentPanel(incident, mapFrame);
		incidentPanels.put(Integer.valueOf(incident.getID()), newPanel);
		incidentPanel.add(newPanel);
		incidentPanel.revalidate();
		incidentPanel.repaint();
	}
	
	@Override
	public void incidentArchived(Incident incident)
	{
		super.incidentArchived(incident);
		//remove old panel
		IncidentPanel oldPanel = incidentPanels.remove(Integer.valueOf(incident.getID()));
		if (oldPanel != null)
		{
			incidentPanel.remove(oldPanel);
			incidentPanel.revalidate();
			incidentPanel.repaint();
		}
		
		
		
		
		//add new panel
		ArchivedIncidentPanel archivedPanel = new ArchivedIncidentPanel(incident, mapFrame);
		archivedIncidentPanels.put(Integer.valueOf(incident.getID()), archivedPanel);
		archivedIncidentPanel.add(archivedPanel);
		
		archivedIncidentPanel.revalidate();
	}
	
	@Override
	public void deviceLocationChanged(Device device, Location oldLocation, Location newLocation)
	{	
		super.deviceLocationChanged(device, oldLocation, newLocation);

		//updates the device location for panels
		// TODO move this to an event-based function in the panels themselves
		for(Map.Entry<Integer,IncidentPanel> entry : incidentPanels.entrySet())
		{
			if(entry.getValue().getIncident().getRespondingDevices().contains(device))
			{
				entry.getValue().setDeviceLocation(device, newLocation);
				break;
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		if (mapRenderer != null)
			mapRenderer.dispose();
		super.windowClosing(e);
	}
	
	@Override
	public void incidentDescriptionChanged(Incident incident) 
	{ 
		super.incidentDescriptionChanged(incident);
		incidentPanels.get(incident.getID()).setPanelDescription(incident.getDescription());
	}
	
	@Override
	public void deviceSelectionChanged(Device device)
	{
		super.deviceSelectionChanged(device);
	}
	
	@Override
	public void deviceInUse(Device device, User user)
	{
		updateDeviceSort();
		updateDeviceFilter();
		revalidate();
	}
	
	@Override
	public void deviceNotInUse(Device device, User oldUser)
	{
		updateDeviceSort();
		updateDeviceFilter();
		revalidate();
	}
	
	@Override
	public void deviceAssignedIncident(Device device, Incident incident)
	{
		updateDeviceSort();
		updateDeviceFilter();
		revalidate();
	}
	
	@Override
	public void deviceUnassignedIncident(Device device, Incident incident)
	{
		updateDeviceSort();
		updateDeviceFilter();
		revalidate();
	}
	


	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////

	private void updateDeviceSort()
	{
		int selectionIndex = sortComboBox.getSelectedIndex();
		if (selectionIndex == -1)
			return;

		Comparator<DevicePanel> comparator = null;
		boolean reverse = false;
		switch (selectionIndex)
		{
			case 0: comparator = DevicePanel.COMPARATOR_DEVICE_TYPE; break;	
			case 1: comparator = DevicePanel.COMPARATOR_USER_ID; break;
			case 2: comparator = DevicePanel.COMPARATOR_USER_NAME_FIRST; break;
			case 3: comparator = DevicePanel.COMPARATOR_USER_NAME_LAST; break;
			case 4: comparator = DevicePanel.COMPARATOR_UNASSIGNED_FIRST; break;
			case 5: comparator = DevicePanel.COMPARATOR_ASSIGNED_FIRST; break;
			case 6: comparator = DevicePanel.COMPARATOR_USER_ID; reverse = true; break;
		}
		Collections.sort(devicePanels, comparator);
		devicePanel.removeAll();
		int i = reverse ? devicePanels.size()-1 : 0;
		while (reverse ? i >= 0 : i < devicePanels.size())
		{
			devicePanel.add(devicePanels.get(i));			
			i = reverse ? i-1 : i+1;
		}
		
	}
	
	private void updateDeviceFilter()
	{
		String search;
		try
		{
			search = searchTextField.getText().trim().toLowerCase();
		}
		catch (Exception e)
		{
			search = "";
		}

		for (int i = 0; i < devicePanels.size(); i++)
		{
			DevicePanel panel = devicePanels.get(i);
			Device device = panel.getDevice();
			User user = device.getCurrentUser();
			boolean visible = (filteredTypes.contains(device.getCurrentUserType())
				&& (search.isEmpty() || (user != null && user.getNameFull().toLowerCase().contains(search))));
			if (panel.isVisible() != visible)
				panel.setVisible(visible);
		}
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




