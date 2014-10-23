package wifindus.eye.dispatcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
		"ID",
		"First Name",
		"Last Name",
		"Available First",
		"Currently Responding First",
		"Unused Devices First"
	};
	private transient JPanel menuPanel, queryPanel, incidentPanel, archivedIncidentPanel, devicePanel;
	private transient JComboBox<String> sortComboBox;
	private transient ButtonGroup filterButtonGroup;
	private transient ArrayList<DevicePanel> devicePanels = new ArrayList<>();
	private transient Map<Integer,IncidentPanel> incidentPanels = new TreeMap<>();
	private transient JTextField searchTextField;
	
    static
    {
    	ResourcePool.loadImage("medical_small", "images/medical_small.png");
    	ResourcePool.loadImage("security_small", "images/security_small.png");
    	ResourcePool.loadImage("wfu_small", "images/wfu_small.png");
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
		
		(menuPanel = new JPanel()).setBackground(Color.white);
		menuPanel.setPreferredSize(new Dimension(800, 70));
		menuPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0 , new Color(0x618197)));

		(new MapFrame()).setVisible(true);
		
		// query Panel
		(queryPanel = new JPanel()).setBackground(new Color(0xedf4fb));
		queryPanel.setMinimumSize(new Dimension(397, 125));
		queryPanel.setMaximumSize(new Dimension(397, 125));
		GroupLayout queryPanelLayout = new GroupLayout(queryPanel);
		queryPanel.setLayout(queryPanelLayout);
        GroupLayout.SequentialGroup queryPanelLayoutHorizontal = queryPanelLayout.createSequentialGroup();
        GroupLayout.SequentialGroup queryPanelLayoutVertical = queryPanelLayout.createSequentialGroup();
        queryPanelLayout.setAutoCreateGaps(true);
        queryPanelLayout.setAutoCreateContainerGaps(true);
		
        filterButtonGroup = new ButtonGroup();
        JToggleButton allFilterButton = new JToggleButton("All");
        JToggleButton medicalFilterButton = new JToggleButton(ResourcePool.getIcon("medical_small"));
        JToggleButton securityFilterButton = new JToggleButton(ResourcePool.getIcon("security_small"));
        JToggleButton techFilterButton = new JToggleButton(ResourcePool.getIcon("wfu_small"));
        
        allFilterButton.setActionCommand("All");
        medicalFilterButton.setActionCommand("Medical");
        securityFilterButton.setActionCommand("Security");
        techFilterButton.setActionCommand("WiFindUs");
        
        ItemListener filterButtonListener = new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				updateDeviceFilter();
			}
		};
        allFilterButton.addItemListener(filterButtonListener);
        medicalFilterButton.addItemListener(filterButtonListener);
        securityFilterButton.addItemListener(filterButtonListener);
        techFilterButton.addItemListener(filterButtonListener);
        
        filterButtonGroup.add(allFilterButton);
        filterButtonGroup.add(medicalFilterButton);
        filterButtonGroup.add(securityFilterButton);
        filterButtonGroup.add(techFilterButton);
        filterButtonGroup.getSelection();
        allFilterButton.setSelected(true);
        
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
        
        rowButtons.addComponent(allFilterButton);
        rowButtons.addComponent(medicalFilterButton);
        rowButtons.addComponent(securityFilterButton);
        rowButtons.addComponent(techFilterButton);
        
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
        
        rowButtonsParallel.addComponent(allFilterButton);
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
        
        contentPane.add(menuPanel, BorderLayout.NORTH);
        contentPane.add(deviceControlPanel, BorderLayout.WEST);
        contentPane.add(incidentTabs, BorderLayout.CENTER);

		incidentPanel.revalidate();
		incidentPanel.repaint();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
   
	@Override
	public void deviceCreated(Device device)
	{
		super.deviceCreated(device);
		devicePanels.add(new DevicePanel(device));
		updateDeviceSort();
		updateDeviceFilter();
		devicePanel.revalidate();
	}
	
	@Override
	public void incidentCreated(Incident incident)
	{
		super.incidentCreated(incident);
		
		IncidentPanel newPanel = new IncidentPanel(incident); 
		incidentPanels.put(Integer.valueOf(incident.getID()), newPanel);
		incidentPanel.add(newPanel);
		incidentPanel.revalidate();
		incidentPanel.repaint();
	}
	
	@Override
	public void incidentArchived(Incident incident)
	{
		super.incidentArchived(incident);
		
		IncidentPanel oldPanel = incidentPanels.get(Integer.valueOf(incident.getID()));
		incidentPanel.remove(oldPanel);
		archivedIncidentPanel.add(oldPanel);
		incidentPanel.revalidate();
		incidentPanel.repaint();
		archivedIncidentPanel.revalidate();
		archivedIncidentPanel.repaint();
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
			case 0: comparator = DevicePanel.COMPARATOR_USER_ID; break;
			case 1: comparator = DevicePanel.COMPARATOR_USER_NAME_FIRST; break;
			case 2: comparator = DevicePanel.COMPARATOR_USER_NAME_LAST; break;
			case 3: comparator = DevicePanel.COMPARATOR_UNASSIGNED_FIRST; break;
			case 4: comparator = DevicePanel.COMPARATOR_ASSIGNED_FIRST; break;
			case 5: comparator = DevicePanel.COMPARATOR_USER_ID; reverse = true; break;
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
		String groupFilter = filterButtonGroup.getSelection().getActionCommand();
		for (int i = 0; i < devicePanels.size(); i++)
		{
			DevicePanel panel = devicePanels.get(i);
			Device device = panel.getDevice();
			User user = device.getCurrentUser();
			boolean visible = (groupFilter.equalsIgnoreCase("All") || (user != null && user.getType().toString().equalsIgnoreCase(groupFilter)))
				&& (search.isEmpty() || (user != null && user.getNameFull().toLowerCase().contains(search)));
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




