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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private JPanel menuPanel, incidentPanel, devicePanel;
	//ArrayList<Device> deviceList;
	private Deque<Device> deviceStack;
	private String sortType = "ID";
	

	
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
		
		JPanel deviceControlPanel = new JPanel();
		deviceControlPanel.setLayout(new BoxLayout(deviceControlPanel, BoxLayout.Y_AXIS));
		
		devicePanel = new JPanel();
		devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
		
		
		
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
		final JComboBox<String> sort = new JComboBox<String>(choices);
		sort.setVisible(true);
		 
		sort.addActionListener (new ActionListener () 
		{
		    public void actionPerformed(ActionEvent e) 
		    {
		    	sortType = sort.getSelectedItem().toString();
		        sortDeviceList(sortType);
		    }
		});
		
		deviceControlPanel.add(sort);
		deviceControlPanel.add(search);
		deviceControlPanel.add(devicePanel);
		
		
		
		JScrollPane devicePanelScroll = new JScrollPane(deviceControlPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
        incidentPanel = new JPanel();
        incidentPanel.setLayout(new BoxLayout(incidentPanel, BoxLayout.Y_AXIS));
        
        
        
        JScrollPane incidentPanelScroll = new JScrollPane(incidentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        incidentPanel.setBackground(Color.WHITE);
        
        getClientPanel().setLayout(new BorderLayout());
        getClientPanel().add(menuPanel, BorderLayout.NORTH);
        getClientPanel().add(devicePanelScroll, BorderLayout.WEST);
        getClientPanel().add(incidentPanelScroll, BorderLayout.CENTER);
        
        
        deviceStack = new ArrayDeque<Device>();
        
        
        
        
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
		
		//Not working (Users not assigned to devices yet?)
		sortDeviceList(sortType);
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
	}
	
	
	
	
	public void sortDeviceList(String sortType)
	{
		devicePanel.removeAll();
		Deque<Device> sortedDeviceStack  = new ArrayDeque<Device>();

		if(sortType == "ID")
		{
				Map<Integer, Device> sortedID = new TreeMap<Integer, Device>();
				
				//add records without names
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
				
				//add records with names
				for(Device obj : deviceStack)
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
					
				for(Device obj : sortedDeviceStack)
				{
						devicePanel.add(new DevicePanel(obj));
				}
			}
			
		
		
			if(sortType == "Availible First")
			{
		
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
		
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentIncident() != null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
		
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentIncident() == null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
		
				for(Device obj : sortedDeviceStack)
				{
					devicePanel.add(new DevicePanel(obj));
				}
				
			}
			
			
			

			if(sortType == "Currently Responding First")
			{
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
				
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentIncident() == null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
		
				
		
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentIncident() != null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
						
				for(Device obj : sortedDeviceStack)
				{
					devicePanel.add(new DevicePanel(obj));
				}
			}
			
			
			if(sortType == "Unused Devices First")
			{
				
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentIncident() != null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
				
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentIncident() == null && obj.getCurrentUser() != null)
						sortedDeviceStack.push(obj);
				}
				
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
			
				
				for(Device obj : sortedDeviceStack)
				{
					devicePanel.add(new DevicePanel(obj));
				}
			}
			
			// sort by first name
			if(sortType == "First Name" || sortType == "Last Name")
			{
				Map<String, Device> sortedNames = new TreeMap<String, Device>();
				
				//add records without names
				for(Device obj : deviceStack)
				{
					if(obj.getCurrentUser() == null)
						sortedDeviceStack.push(obj);
				}
				
				//add records with names
				for(Device obj : deviceStack)
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
					}
					// sort by last name
					else
					{
						for (int i = 0; i < reversedDeviceList.size(); i++)
						{
							sortedDeviceStack.push(reversedDeviceList.get(i));
						}
					}
					
					
				for(Device obj : sortedDeviceStack)
				{
					devicePanel.add(new DevicePanel(obj));
				}
			}
			devicePanel.revalidate();
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




