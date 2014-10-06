package wifindus.eye.dispatcher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import wifindus.eye.Device;
import wifindus.Debugger;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.Location;
import wifindus.eye.Incident.Type;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;


public class Dispatcher extends EyeApplication
{
	private static final long serialVersionUID = 12094147960785467L;
	private JPanel menuPanel, queryPanel, incidentPanel, devicePanel;
	//ArrayList<Device> deviceList;
	private Deque<Device> deviceStack;
	private final JComboBox<String> sortComboBox;
	private String sortType = "ID";
	private ButtonGroup filterButtonGroup;
	private JToggleButton allFilterButton, medicalFilterButton, securityFilterButton, techFilterButton;
	

	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Dispatcher(String[] args)
	{
		super(args);
		deviceStack = new ArrayDeque<Device>();
		getClientPanel().setLayout(new BoxLayout(getClientPanel(), BoxLayout.Y_AXIS));
		getClientPanel().setBackground(Color.white);
		menuPanel = new JPanel();
		menuPanel.setBackground(Color.white);
		menuPanel.setPreferredSize(new Dimension(800, 70));
		menuPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0 , new Color(0x618197)));
		ItemListener listener = new itemListener(); 
		MapFrame map = new MapFrame();
		map.setVisible(true);
		
		/////////////////////////////////////////////////////////////////////
		// query Panel
		/////////////////////////////////////////////////////////////////////
		queryPanel = new JPanel();
		queryPanel.setBackground(new Color(0xedf4fb));
		//int queryPanelWidth = scrollbarWidth+380;
		//System.out.println(scrollbarWidth);
		queryPanel.setMinimumSize(new Dimension(397, 125));
		queryPanel.setMaximumSize(new Dimension(397, 125));
		GroupLayout queryPanelLayout = new GroupLayout(queryPanel);
		queryPanel.setLayout(queryPanelLayout);
        GroupLayout.SequentialGroup queryPanelLayoutHorizontal = queryPanelLayout.createSequentialGroup();
        GroupLayout.SequentialGroup queryPanelLayoutVertical = queryPanelLayout.createSequentialGroup();
        queryPanelLayout.setAutoCreateGaps(true);
        queryPanelLayout.setAutoCreateContainerGaps(true);
		
        ImageIcon medicalIcon = new ImageIcon("images/medical_logo_small-30.png");
        filterButtonGroup = new ButtonGroup();
        allFilterButton = new JToggleButton("All");
        medicalFilterButton = new JToggleButton(medicalIcon);
        securityFilterButton = new JToggleButton(Incident.getIcon(Incident.Type.Security, true));
        techFilterButton = new JToggleButton(Incident.getIcon(Incident.Type.WiFindUs, true));
        
        allFilterButton.setActionCommand("All");
        medicalFilterButton.setActionCommand("Medical");
        securityFilterButton.setActionCommand("Security");
        techFilterButton.setActionCommand("WiFindUs");
        
        allFilterButton.addItemListener(listener);
        medicalFilterButton.addItemListener(listener);
        securityFilterButton.addItemListener(listener);
        techFilterButton.addItemListener(listener);
        
        filterButtonGroup.add(allFilterButton);
        filterButtonGroup.add(medicalFilterButton);
        filterButtonGroup.add(securityFilterButton);
        filterButtonGroup.add(techFilterButton);
        filterButtonGroup.getSelection();
        allFilterButton.setSelected(true);
        
        JLabel sortLabel = new JLabel("Sort by:");
        JLabel searchLabel = new JLabel("Search:");
        
        
		
		// search
		final JTextField search = new JTextField();		 
		search.getDocument().addDocumentListener(new DocumentListener() 
		{
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				searchName(search.getText());
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				searchName(search.getText());
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				searchName(search.getText());
				
			}
		});
		
		
		
		// sort
		String[] choices = { "ID", "First Name", "Last Name", "Availible First", "Currently Responding First", "Unused Devices First"};
		sortComboBox = new JComboBox<String>(choices);
		sortComboBox.setVisible(true);
		sortComboBox.setSelectedIndex(0);
		sortComboBox.addItemListener(listener);
		
		/*sortComboBox.addActionListener (new ActionListener () 
		{
		    public void actionPerformed(ActionEvent e) 
		    {
		    	sortType = sortComboBox.getSelectedItem().toString();
		    	if(!deviceStack.isEmpty())
		    	{
		    		updateDevicePanel(sortFromMenu(sortType, deviceStack));
		    	}
		    }
		});*/
				
		sortComboBox.setMaximumSize(new Dimension(295,25));
		sortComboBox.setMinimumSize(new Dimension(295,25));
        search.setMaximumSize(new Dimension(295,25));
        search.setMinimumSize(new Dimension(295,25));
		
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
        columnSortSearch.addComponent(search);
        
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
        rowSearchParallel.addComponent(search);
        
        queryPanelLayoutVertical.addGroup(rowButtonsParallel);
        queryPanelLayoutVertical.addGroup(rowSortParallel);
        queryPanelLayoutVertical.addGroup(rowSearchParallel);
        
        
        queryPanelLayout.setHorizontalGroup(queryPanelLayoutHorizontal);
        queryPanelLayout.setVerticalGroup(queryPanelLayoutVertical);
        
        
        
        /////////////////////////////////////////////////////////////////////
        // device Panel
        /////////////////////////////////////////////////////////////////////
        devicePanel = new JPanel();
		devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
		JScrollPane devicePanelScroll = new JScrollPane(devicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		int scrollbarWidth = devicePanelScroll.getWidth();
        
        
		
        /////////////////////////////////////////////////////////////////////
        // device control Panel: contains query and device panels
        /////////////////////////////////////////////////////////////////////
        JPanel deviceControlPanel = new JPanel();
		
		//Layout
        GroupLayout layout = new GroupLayout(deviceControlPanel);
        deviceControlPanel.setLayout(layout);
        GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
        //layout.setAutoCreateGaps(true);
        //layout.setAutoCreateContainerGaps(true);
		
		//deviceControlPanel horizontal
        GroupLayout.ParallelGroup column = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		
        column.addComponent(queryPanel);
        column.addComponent(devicePanelScroll);
        
        horizontal.addGroup(column);
        
        //deviceControlPanel vertical
        vertical.addComponent(queryPanel);
        vertical.addComponent(devicePanelScroll);
        
        
        layout.setHorizontalGroup(horizontal);
        layout.setVerticalGroup(vertical);
		
		
		incidentPanel = new JPanel();
        incidentPanel.setLayout(new BoxLayout(incidentPanel, BoxLayout.Y_AXIS));
        
        
        JScrollPane incidentPanelScroll = new JScrollPane(incidentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        incidentPanel.setBackground(Color.WHITE);
        
        getClientPanel().setLayout(new BorderLayout());
        getClientPanel().add(menuPanel, BorderLayout.NORTH);
        getClientPanel().add(deviceControlPanel, BorderLayout.WEST);
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
		deviceStack.push(device);
		devicePanel.revalidate();
		
		
		
		//updateDevicePanel(deviceStack);
		//Not working (Users not assigned to devices yet?)
		//updateDevicePanel(sortFromMenu(sortType,deviceStack));
		sort(deviceStack);
	}
	
	@Override
	public void deviceLocationChanged(Device device, Location oldLocation, Location newLocation)
	{
		super.deviceCreated(device);
		
		//Update Location on the map
		MapImagePanel.deviceLocationChanged( device,  oldLocation,  newLocation);
		
	}
	
	
	
	
	@Override
	public void incidentCreated(Incident incident)
	{
		super.incidentCreated(incident);
		incidentPanel.add(new IncidentPanel(incident));
		incidentPanel.revalidate();
	}
	
	
	public void searchName(String searchText)
	{
		devicePanel.removeAll();

		Deque<Device> searchedDeviceStack  = new ArrayDeque<Device>();

		for(Device obj : deviceStack)
		{
			if(obj.getCurrentUser() != null)
			{
				if(obj.getCurrentUser().getNameFull().toLowerCase().contains(searchText.toLowerCase()))
					searchedDeviceStack.push(obj);
			}
		}
			
		for(Device obj : searchedDeviceStack)
		{
			devicePanel.add(new DevicePanel(obj));
		}
		devicePanel.revalidate();
		
		Debugger.i("Users searched by text.");
	}
	
	
	
	
	public Deque<Device> sortFromMenu(String sortType, Deque<Device> stack)
	{
		devicePanel.removeAll();
		Deque<Device> sortedDeviceStack  = new ArrayDeque<Device>();
		
		
		if(sortType == "ID")
		{
				Map<Integer, Device> sortedID = new TreeMap<Integer, Device>();
				
				//add records without names
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
				
				//add records with names
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() != null)
					{
						sortedID.put(obj.getCurrentUser().getID() , obj);
					}
				}
			
				//put sorted tree map into an array list
				ArrayList<Device> reversedDeviceList = new ArrayList<Device>();
				for(Map.Entry<Integer,Device> entry : sortedID.entrySet()) 
				{
					  Device device = entry.getValue();
					  reversedDeviceList.add(device);
				}
				
				for (int i = reversedDeviceList.size()-1; i >= 0; i--)
				{
					sortedDeviceStack.push(reversedDeviceList.get(i));
				}
					
				Debugger.i("Users sorted by ID.");
			}
			
		
		
			if(sortType == "Availible First")
			{
		
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
		
				for(Device obj : stack)
				{
					if(obj.getCurrentIncident() != null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
		
				for(Device obj : stack)
				{
					if(obj.getCurrentIncident() == null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
		
				
				Debugger.i("Users sorted by Available First.");
			}
			
			
			

			if(sortType == "Currently Responding First")
			{
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
				
				for(Device obj : stack)
				{
					if(obj.getCurrentIncident() == null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
		
		
				for(Device obj : stack)
				{
					if(obj.getCurrentIncident() != null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
						
				
				Debugger.i("Users sorted by Currently Responding First.");
			}
			
			
			if(sortType == "Unused Devices First")
			{
				
				for(Device obj : stack)
				{
					if(obj.getCurrentIncident() != null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
				
				for(Device obj : stack)
				{
					if(obj.getCurrentIncident() == null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
				
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
			
				
				Debugger.i("Users sorted by Unused Devices First.");
			}
			
			// sort by first name
			if(sortType == "First Name" || sortType == "Last Name")
			{
				Map<String, Device> sortedNames = new TreeMap<String, Device>();
				
				//add records without names
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
				
				//add records with names
				for(Device obj : stack)
				{
					if(obj.getCurrentUser() != null)
					{
						sortedNames.put(obj.getCurrentUser().getNameFull() , obj);
					}
				}
			
				//put sorted tree map into an array list
				ArrayList<Device> reversedDeviceList = new ArrayList<Device>();
				for(Map.Entry<String,Device> entry : sortedNames.entrySet()) 
				{
					  Device device = entry.getValue();
					  reversedDeviceList.add(device);
				}
					// sort by first name
					if(sortType == "First Name")
					{
						for (int i = reversedDeviceList.size()-1; i >= 0; i--)
						{
							sortedDeviceStack.push(reversedDeviceList.get(i));
						}
						Debugger.i("Users sorted by First Name.");
					}
					// sort by last name
					else
					{
						for (int i = 0; i < reversedDeviceList.size(); i++)
						{
							sortedDeviceStack.push(reversedDeviceList.get(i));
						}
						Debugger.i("Users sorted by Last Name.");
					}
			}
			
			return sortedDeviceStack;
		}
	
	
	
		public Deque<Device> sortByUserType(String type, Deque<Device> stack)
		{
			devicePanel.removeAll();
			Deque<Device> sortedDeviceStack  = new ArrayDeque<Device>();
			
			if(type == "Medical")
			{
				for(Device obj : stack)
				{
					if(obj.getCurrentUser()!=null && obj.getCurrentUser().getType().toString().equals("Medical"))
					{
						sortedDeviceStack.push(obj);
						Debugger.i("Type: "+obj.getCurrentUser().getType().toString());
					}
				}
				Debugger.i("Users sorted by Medical type.");
			}
			
			if(type == "Security")
			{
				for(Device obj : stack)
				{
					if(obj.getCurrentUser()!=null && obj.getCurrentUser().getType().toString().equals("Security"))
					{
						sortedDeviceStack.push(obj);
						Debugger.i("Type: "+obj.getCurrentUser().getType().toString());
					}
				}
				Debugger.i("Users sorted by Security type.");
			}
			
			if(type == "WiFindUs")
			{
				for(Device obj : stack)
				{
					if(obj.getCurrentUser()!=null && obj.getCurrentUser().getType().toString().equals("WiFindUs"))
					{
						sortedDeviceStack.push(obj);
						Debugger.i("Type: "+obj.getCurrentUser().getType().toString());
					}
				}
				Debugger.i("Users sorted by Wifindus/Tech type.");
			}
			
			if(type == "All")
			{
				sortedDeviceStack = stack;
				Debugger.i("Users sorted by no type (all).");
			}
			
			return sortedDeviceStack;
		}
		
		
		public void sort(Deque<Device> stack){
			String userType = filterButtonGroup.getSelection().getActionCommand();
			String sortCriteria = sortComboBox.getSelectedItem().toString();
			Deque<Device> sortedStack  = new ArrayDeque<Device>();
			sortedStack = sortByUserType(userType, stack);
			sortedStack = sortFromMenu(sortCriteria, sortedStack);
			updateDevicePanel(sortedStack);
		}
		
		public void updateDevicePanel(Deque<Device> stack){
			devicePanel.removeAll();
			for(Device obj : stack)
			{
				devicePanel.add(new DevicePanel(obj));
			}
			devicePanel.revalidate();
		}
	
		
	class itemListener implements ItemListener{
	      public void itemStateChanged(ItemEvent e) {
	    	  if(!deviceStack.isEmpty())
	    	  {
	    		  sort(deviceStack);
	    	  }
	      }
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




