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
    private transient JLabel incidentTime, idLabel, onTaskLabel;
    private transient JButton locateOnMap, addRespondent, removeIncident, codeButton, statusButton, incidentIconButton;
	private transient JList onTaskList;
     
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
        
        idLabel = new JLabel("Incident #" + incident.getID());
        //JLabel incidentIcon = new JLabel(Incident.getIcon(incident.getType(), false));
        incidentIconButton = new JButton(Incident.getIcon(incident.getType(), false));
        incidentIconButton.setBorder(emptyBorder);
        incidentIconButton.setBackground(lightBlue);
        idLabel.setFont(idFont);
        
        //Locate on map icon
        ImageIcon locateIcon = new ImageIcon("images/locate.png");
        Image locateIconImage = locateIcon.getImage() ; 
        Image scaledLocateIcon = locateIconImage.getScaledInstance( 12, 20,  java.awt.Image.SCALE_SMOOTH );  
        locateIcon = new ImageIcon(scaledLocateIcon);
        
        //Add respondent (plus) icon
        ImageIcon plusIcon = new ImageIcon("images/plus.png");
        Image plusIconImage = plusIcon.getImage() ; 
        Image scaledPlusIcon = plusIconImage.getScaledInstance( 12, 12,  java.awt.Image.SCALE_SMOOTH );  
        plusIcon = new ImageIcon(scaledPlusIcon);
        
        //Remove Incident (x) Icon [will be changed]
        ImageIcon minusIcon = new ImageIcon("images/minus.png");
        Image minusIconImage = minusIcon.getImage() ; 
        Image scaledMinusIcon = minusIconImage.getScaledInstance( 12, 12,  java.awt.Image.SCALE_SMOOTH );  
        minusIcon = new ImageIcon(scaledMinusIcon);
        
        locateOnMap = new JButton("Locate on Map");
        locateOnMap.setBackground(lightBlue);
        locateOnMap.setIcon(locateIcon);
        locateOnMap.setBorder(emptyBorder);
        locateOnMap.setFont(font);
        locateOnMap.setHorizontalAlignment(SwingConstants.LEFT);
        
        addRespondent = new JButton("Add Respondent");
        addRespondent.setBackground(lightBlue);
        addRespondent.setIcon(plusIcon);
        addRespondent.setBorder(emptyBorder);
        addRespondent.setFont(font);
        addRespondent.setHorizontalAlignment(SwingConstants.LEFT);
        
        removeIncident = new JButton("Remove Incident");
        removeIncident.setBackground(lightBlue);
        removeIncident.setIcon(minusIcon);
        removeIncident.setBorder(emptyBorder);
        removeIncident.setFont(font);
        removeIncident.setHorizontalAlignment(SwingConstants.LEFT);
        
        onTaskLabel = new JLabel ("On Task:");
        onTaskLabel.setFont(font);
        String	testList[] =
    		{
    			"Mark Gillard",
    			"Mitchell Templeton",
    			"Peter Griffin",
    			"Chandler Muriel Bing",
    			"Adam West",
    			"Random Name",
    			"Joseph Francis Tribbiani ",
    			"Hussein Al Hammad"
    		};
        onTaskList = new JList(testList);
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

