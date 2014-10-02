package wifindus.eye.dispatcher;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import wifindus.eye.Device;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;

public class IncidentPanel extends JPanel implements IncidentEventListener
{
	private static final long serialVersionUID = -7397843910420550797L;
    private transient Incident incident = null;
    private transient JLabel incidentTime, idLabel, statusLabel, codeLabel, incidentTypeTitle, onTaskLabel;
    private transient JButton locateOnMap, addRespondent, removeIncident;
    private transient JList onTaskList;
     
    public IncidentPanel(Incident incident)
    {
		if (incident == null)
			throw new NullPointerException("Parameter 'incident' cannot be null.");
		this.incident = incident;
    	
		
		//cosmetic properties
		setBackground(new Color(0xf6f9fc));
		setBorder(BorderFactory.createMatteBorder(0,1,1,0,new Color(0x618197)));
        Border emptyBorder = BorderFactory.createEmptyBorder();

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        
        GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
        GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
        
        idLabel = new JLabel("Incident #" + incident.getID());
        JLabel incidentIcon = new JLabel(Incident.getIcon(incident.getType(), false));
        
        ImageIcon locateIcon = new ImageIcon("images/locate.png");
        locateOnMap = new JButton("Locate on Map");
        ImageIcon plusIcon = new ImageIcon("images/plus.png");
        addRespondent = new JButton("Add Respondent");
        ImageIcon minusIcon = new ImageIcon("images/minus.png");
        removeIncident = new JButton("Remove Incident");
        
        onTaskLabel = new JLabel ("On Task:");
        String	testList[] =
    		{
    			"Item 1",
    			"Item 2",
    			"Item 3",
    			"Item 4",
    			"Item 5",
    			"Item 6",
    			"Item 7",
    			"Item 8"
    		};
        onTaskList = new JList(testList);
        JScrollPane onTaskListScroll = new JScrollPane(onTaskList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        onTaskListScroll.setPreferredSize(new Dimension(100,60));
        
        incidentTime = new JLabel("Time: 00 : 00 : 00");
        codeLabel = new JLabel("Code");
        statusLabel = new JLabel("Active");
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
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
        
        columnLeft.addComponent(idLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnLeft.addComponent(incidentIcon, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        columnButtons.addComponent(locateOnMap, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnButtons.addComponent(addRespondent, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnButtons.addComponent(removeIncident, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        columnList.addComponent(onTaskLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnList.addComponent(onTaskListScroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        columnRight.addComponent(incidentTime, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnRight.addComponent(codeLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        columnRight.addComponent(statusLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        horizontal.addGroup(columnLeft);
        horizontal.addGroup(columnButtons);
        horizontal.addGroup(columnList);
        horizontal.addGroup(columnRight);
        
        //vertical
        GroupLayout.ParallelGroup rowBottom = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        GroupLayout.SequentialGroup buttonGroup = layout.createSequentialGroup();
        GroupLayout.SequentialGroup onTaskGroup = layout.createSequentialGroup();
        GroupLayout.SequentialGroup rightGroup = layout.createSequentialGroup();
        
        buttonGroup.addComponent(locateOnMap);
        buttonGroup.addComponent(addRespondent);
        buttonGroup.addComponent(removeIncident);
        
        onTaskGroup.addComponent(onTaskLabel);
        onTaskGroup.addComponent(onTaskListScroll);
        
        rightGroup.addComponent(incidentTime);
        rightGroup.addComponent(codeLabel);
        rightGroup.addComponent(statusLabel);
        
        rowBottom.addComponent(incidentIcon);
        rowBottom.addGroup(buttonGroup);
        rowBottom.addGroup(onTaskGroup);
        rowBottom.addGroup(rightGroup);
        
        vertical.addComponent(idLabel);
        vertical.addGroup(rowBottom);
        
        layout.setHorizontalGroup(horizontal);
        layout.setVerticalGroup(vertical);
        
        
        /*
         * 
         * 
         * TODO: finish importing the rest of the code here...
         * 
         * 
         * 
         */
        
        incident.addEventListener(this);
    }

	@Override
	public void incidentArchived(Incident incident)
	{
		
	}
	
	@Override
	public void incidentAssignedDevice(Incident incident, Device device)
	{
		
	}
	
	@Override
	public void incidentUnassignedDevice(Incident incident, Device device)
	{
		
	}
	
	//do not implement this
	@Override public void incidentCreated(Incident incident) { }
}

