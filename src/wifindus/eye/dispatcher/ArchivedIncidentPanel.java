package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import wifindus.Debugger;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;
import wifindus.eye.MapFrame;
import wifindus.eye.User;

public class ArchivedIncidentPanel extends IncidentParentPanel implements
		IncidentEventListener, ActionListener {
	private static final long serialVersionUID = -7397843910420550797L;
	private transient JLabel incidentTime;
	private transient JLabel idLabel;
	private transient JLabel onTaskLabel;

	// For Archived Incidents
	private transient JLabel reportedBy, respondedToBy, timeToResolve,
			reportedAt, resolvedAt, timeIconLabel, reportedName, descriptionLabel;
	private transient JButton locateOnMap, codeButton, saveButton,
			incidentIconButton, saveIconButton;
	private transient JTextArea incidentDescription;
	
	ImageIcon timeIcon, saveIcon;

	String timeDifferenceReport; 
	
	JList list;

	DefaultListModel model;

	int counter = 15;

	JScrollPane pane;

	static {
		ResourcePool.loadImage("plus_small", "images/plus_small.png");
		ResourcePool.loadImage("minus_small", "images/minus_small.png");
		ResourcePool.loadImage("locate_small", "images/locate_small.png");
		ResourcePool.loadImage("none", "images/none.png");
		ResourcePool.loadImage("medical", "images/medical.png");
		ResourcePool.loadImage("security", "images/security.png");
		ResourcePool.loadImage("wfu", "images/wfu.png");

		ResourcePool.loadImage("save_report_icon",
				"images/save_report_icon.png");
		ResourcePool.loadImage("time", "images/time.png");
	}

	public ArchivedIncidentPanel(Incident incident, MapFrame mapFrame)
	{
		super(incident, mapFrame);

		// cosmetic properties
		Color lightBlue = new Color(0xf6f9fc);
		Color red = new Color(0xfd0b15);
		setBackground(lightBlue);
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, new Color(
				0x618197)));
		Border emptyBorder = BorderFactory.createEmptyBorder();
		setMaximumSize(new Dimension(1000, 200));
		setMinimumSize(new Dimension(500, 200));
		Font font, rightColumnFont, idFont, timeFont;
		timeFont = getFont().deriveFont(Font.PLAIN, 14.0f);
		font = getFont().deriveFont(Font.BOLD, 15.0f);
		idFont = getFont().deriveFont(Font.BOLD, 17.0f);
		rightColumnFont = getFont().deriveFont(Font.BOLD, 16.0f);

		// Layout
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

		
		descriptionLabel = new JLabel ("Description:");
		descriptionLabel.setFont(font);
				
		incidentDescription = new JTextArea("", 5, 5);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		incidentDescription.setBorder(BorderFactory.createCompoundBorder(border, 
		            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		incidentDescription.setLineWrap(true);
		incidentDescription.setWrapStyleWord(true);
		incidentDescription.setEditable(false);
		 
		// Second Column
		reportedName = new JLabel("reporter name");
		if(incident.getReportingUser() != null)
			reportedName.setText(incident.getReportingUser().toString());

		reportedName.setFont(timeFont);
		
		reportedBy = new JLabel("Reported By: ");
		reportedBy.setBackground(lightBlue);
		reportedBy.setBorder(emptyBorder);
		reportedBy.setFont(font);
		reportedBy.setHorizontalAlignment(SwingConstants.LEFT);

		respondedToBy = new JLabel("Responded to By: ");
		respondedToBy.setBackground(lightBlue);
		respondedToBy.setBorder(emptyBorder);
		respondedToBy.setFont(font);
		respondedToBy.setHorizontalAlignment(SwingConstants.LEFT);

		// List
		model = new DefaultListModel<User>();
		list = new JList(model);
		pane = new JScrollPane(list);
		pane.setMaximumSize(new Dimension(500, 50));

		
		// Times (Third Column)
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		
		String date1 = dateFormat.format(incident.getCreated());
		String date2 = dateFormat.format(incident.getArchivedTime());

		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 
		Date createdDate = null;
		Date archivedDate = null;

		timeDifferenceReport = "";
		
		try {
			createdDate = format.parse(date1);
			archivedDate = format.parse(date2);
			long timeDifference = archivedDate.getTime() - createdDate.getTime();
 			long secondDifference = timeDifference / 1000 % 60;
			long minuteDifference = timeDifference / (60 * 1000) % 60;
			long hourDifference = timeDifference / (60 * 60 * 1000) % 24;
			long dayDifference = timeDifference / (24 * 60 * 60 * 1000);
			timeDifferenceReport = dayDifference + " days, " + hourDifference+ " hours, " + minuteDifference+ " minutes, " + secondDifference+ " seconds";
 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		timeIconLabel = new JLabel("Times");
		timeIconLabel.setFont(font);
		timeIcon = ResourcePool.getIcon("time");
		Image timeIconImage = timeIcon.getImage();
		Image scaledTimeIcon = timeIconImage.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		timeIcon = new ImageIcon(scaledTimeIcon);
		timeIconLabel.setIcon(timeIcon);
		timeToResolve = new JLabel("Time to Resolve - "+ timeDifferenceReport);
		timeToResolve.setFont(timeFont);
		reportedAt = new JLabel("Reported At - "+ dateFormat.format(incident.getCreated()));
		reportedAt.setFont(timeFont);
		resolvedAt = new JLabel("Resolved At - " + dateFormat.format(incident.getArchivedTime()));
		resolvedAt.setFont(timeFont);

		locateOnMap = new JButton("Locate on Map");
		locateOnMap.setBackground(lightBlue);
		locateOnMap.setIcon(ResourcePool.getIcon("locate_small"));
		locateOnMap.setBorder(emptyBorder);
		locateOnMap.setFont(font);
		locateOnMap.setHorizontalAlignment(SwingConstants.LEFT);
		locateOnMap.addActionListener(this);

		codeButton = new JButton("code");
		codeButton.setBackground(Color.gray);
		codeButton.setForeground(Color.white);
		codeButton.setFont(rightColumnFont);
		codeButton.setBorder(emptyBorder);
		codeButton.setMinimumSize(new Dimension(186, 30));

		saveIcon = ResourcePool.getIcon("save_report_icon");
		Image saveIconImage = saveIcon.getImage();
		Image scaledSaveIcon = saveIconImage.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
		saveIcon = new ImageIcon(scaledSaveIcon);

		saveButton = new JButton("Save Report");
		saveButton.setBackground(lightBlue);
		saveButton.setIcon(saveIcon);
		saveButton.setBorder(emptyBorder);
		saveButton.setFont(font);
		saveButton.setHorizontalAlignment(SwingConstants.LEFT);
		saveButton.addActionListener(this);

		// horizontal
		GroupLayout.ParallelGroup columnLeft = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup columnButtons = layout.createParallelGroup();
		GroupLayout.ParallelGroup columnList = layout.createParallelGroup();
		GroupLayout.ParallelGroup columnDescription = layout.createParallelGroup();
		GroupLayout.ParallelGroup columnRight = layout.createParallelGroup();
		GroupLayout.SequentialGroup timeRowSequential = layout.createSequentialGroup();
		GroupLayout.SequentialGroup iconSequential = layout.createSequentialGroup();

		iconSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, 50);
		iconSequential.addComponent(incidentIconButton, 0,GroupLayout.DEFAULT_SIZE, 100);
		columnLeft.addComponent(idLabel, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		columnLeft.addGroup(iconSequential);

		// ////////////////////////////////////
		columnButtons.addComponent(reportedBy, 0, GroupLayout.DEFAULT_SIZE, 150);
		columnButtons.addComponent(reportedName, 0, GroupLayout.DEFAULT_SIZE, 150);
		columnButtons.addComponent(respondedToBy, 0, GroupLayout.DEFAULT_SIZE,150);
		columnButtons.addComponent(pane, 0, GroupLayout.DEFAULT_SIZE, 150);

		// ////////////////////////////////////
		columnList.addComponent(timeIconLabel, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		columnList.addComponent(timeToResolve, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		columnList.addComponent(reportedAt, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		columnList.addComponent(resolvedAt, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);

		// ////////////////////////////////////

		columnRight.addComponent(codeButton, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		columnRight.addComponent(locateOnMap, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);
		columnRight.addComponent(saveButton, 0, GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE);

		// ////////////////////////////////////
		horizontal.addGroup(columnLeft);
		horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 30, 40);
		horizontal.addGroup(columnDescription);
		horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 30,40);
		horizontal.addGroup(columnButtons);
		horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,40, Short.MAX_VALUE);
		horizontal.addGroup(columnList);
		horizontal.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,40, Short.MAX_VALUE);
		horizontal.addGroup(columnRight);

		columnDescription.addComponent(incidentDescription, 0, GroupLayout.DEFAULT_SIZE, 150);
		columnDescription.addComponent(descriptionLabel, 0, GroupLayout.DEFAULT_SIZE, 150);
		
		// vertical
		GroupLayout.ParallelGroup rowBottom = layout
				.createParallelGroup(GroupLayout.Alignment.BASELINE);
		GroupLayout.SequentialGroup buttonGroup = layout
				.createSequentialGroup();
		GroupLayout.SequentialGroup onTaskGroup = layout
				.createSequentialGroup();
		GroupLayout.SequentialGroup rightGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup timeRowParallel = layout
				.createParallelGroup();
		
		GroupLayout.SequentialGroup descriptionGroup = layout.createSequentialGroup();

		buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, 10);
		buttonGroup.addComponent(reportedBy);
		buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, 10);
		buttonGroup.addComponent(reportedName);
		buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, 10);
		buttonGroup.addComponent(respondedToBy);
		buttonGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, 10);
		buttonGroup.addComponent(pane);

		onTaskGroup.addComponent(timeIconLabel);
		onTaskGroup.addComponent(timeToResolve);
		onTaskGroup.addComponent(reportedAt);
		onTaskGroup.addComponent(resolvedAt);
		
		
		descriptionGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 10);
		descriptionGroup.addComponent(descriptionLabel);
		descriptionGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 10);
		descriptionGroup.addComponent(incidentDescription);
		
		

		rightGroup.addComponent(codeButton);
		rightGroup.addComponent(locateOnMap);

		rightGroup.addComponent(saveButton);

		rowBottom.addComponent(incidentIconButton, 0, GroupLayout.DEFAULT_SIZE,
				100);
		
		rowBottom.addGroup(onTaskGroup);
		rowBottom.addGroup(buttonGroup);
		rowBottom.addGroup(rightGroup);

		vertical.addComponent(idLabel);
		rowBottom.addGroup(descriptionGroup);
		vertical.addGroup(rowBottom);

		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);

		// eye listeners
		incident.addEventListener(this);

	}

	@Override
	public void incidentArchived(Incident incident) {

	}

	@Override
	public void incidentAssignedDevice(Incident incident, Device device) {

	}

	@Override
	public void incidentUnassignedDevice(Incident incident, Device device) {

	}

	@Override
	public void incidentSelectionChanged(Incident incident) {
		// TODO: visually reflect selection state in some way
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Incident incident = getIncident();
		//Save report to text file
		if(e.getSource() == saveButton)
		{
	        BufferedWriter writer = null;
	        try 
	        {
	            String path = "reports/incident_"+incident.getID()+".txt";
	            
	            writer = new BufferedWriter(new FileWriter(path));
	            writer.write("Incident #" + incident.getID());
	            writer.newLine();
	            writer.newLine();
	            
	            writer.write("Type: " + incident.getType());
	            writer.newLine();
	            writer.newLine();
	            
	            writer.write("Reported By:" + incident.getReportingUser());
	            writer.newLine();
	            writer.newLine();
	            
	            writer.write("Responded to By:");
	            writer.newLine();
	        	for (User user : incident.getArchivedResponders()) {
	        		writer.write(user.getNameFull().toString());
	        		writer.newLine();
	    		}
	            writer.newLine();
	            writer.newLine();
	           
	            writer.write("Times:");
	            writer.newLine();
	            writer.write("\tTime Taken to Resolve: "+ timeDifferenceReport);
	            writer.newLine();
	            writer.write("\tReported At: "+incident.getCreated());
	            writer.newLine();
	            writer.write("\tResolved At: "+incident.getArchivedTime());
	            writer.newLine();
	            writer.newLine();
	       	            
	            if(incident.getCode() != null)
	            writer.write("Code: " + incident.getCode());
	            writer.newLine();
	            writer.newLine();
	            
	            writer.write("Location: " + incident.getLocation());
	        } 
	        catch (Exception ex) 
	        {
	            ex.printStackTrace();
	        } 
	        finally 
	        {
	            try 
	            {
	                writer.close();
	            } catch (Exception ex) 
	            {
	            }
	        }
		}
		else if (e.getSource() == locateOnMap)
			locateObjectOnMap(getIncident());
	}

	// ///////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// ///////////////////////////////////////////////////////////////////

	private void updateButtonState() {
		ImageIcon icon = null;
		switch (getIncident().getType()) {
		case Medical:
			icon = ResourcePool.getIcon("medical");
			break;
		case Security:
			icon = ResourcePool.getIcon("security");
			break;
		case WiFindUs:
			icon = ResourcePool.getIcon("wfu");
			break;
		default:
			icon = ResourcePool.getIcon("none");
			break;
		}
		incidentIconButton.setIcon(icon);
	}

	private void updateTimerLabel(long endTimestamp) {
		if (endTimestamp <= 0)
			endTimestamp = System.currentTimeMillis();

		long seconds = (endTimestamp - getIncident().getCreated().getTime()) / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		incidentTime.setText(String.format("%02d : %02d : %02d", hours % 24,
				minutes % 60, seconds % 60));
	}

	@Override
	public void incidentArchivedResponderAdded(Incident incident, User user) {
		// TODO Auto-generated method stub
		model.addElement(user.getNameFull());
	}

	@Override
	public void incidentSeverityChanged(Incident incident, int oldSeverity,
			int newSeverity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void incidentCodeChanged(Incident incident, String oldCode,
			String newCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void incidentReportingUserChanged(Incident incident, User oldUser,
			User newUser) {
		// TODO Auto-generated method stub
	}
	
	@Override public void incidentDescriptionChanged(Incident incident) { }
}








