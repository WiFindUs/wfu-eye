package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;
import wifindus.eye.MapFrame;
import wifindus.eye.User;

public class ArchivedIncidentPanel extends IncidentParentPanel implements
		IncidentEventListener, ActionListener {
	private static final long serialVersionUID = -7397843910420550797L;
	private transient JLabel idLabel, reportedLabel, resolvedLabel, resolvedTimeLabel, incidentIconLabel, codeLabel;
	private transient JButton locateBtn, saveBtn;
	private transient JTextArea incidentDescription;
	private transient String  reporterName, reportedDate, reportedTime, resolvedDate, resolvedTime;
	private transient long dayDifference, hourDifference, minuteDifference, secondDifference, timeDifference;
	private transient String[] resolvedIn;
	private transient List<String> respondents;
	 JComboBox<String> fileTypeSelect;
	private DefaultTableCellRenderer centerRenderer;

	@SuppressWarnings("unused")
	private static final int COLUMN_RESPONDENT = 0;
	private transient DefaultTableModel respondentsTableModel = new DefaultTableModel(
			new Object[][]{},
			new String[] {"Respondents"}
			);
	private transient JTable respondentsTable;
	
	
	private static final int COLUMN_DAYS = 0;
	private static final int COLUMN_HOURS = 1;
	private static final int COLUMN_MINUTES = 2;
	private static final int COLUMN_SECONDS = 3;
	private transient DefaultTableModel resolvedTimeTableModel = new DefaultTableModel(
			new Object[][]{},
			new String[] {"Days", "Hours", "Minutes", "Seconds"}
			);
	private transient JTable resolvedTimeTable;
	
	
	private static final int COLUMN_DATE_REPORTED = 0;
	private static final int COLUMN_TIME_REPORTED = 1;
	private transient DefaultTableModel reportedTableModel = new DefaultTableModel(
			new Object[][]{},
			new String[] {"Date", "Time"}
			);
	private transient JTable reportedTable;
	
	private static final int COLUMN_DATE_RESOLVED = 0;
	private static final int COLUMN_TIME_RESOLVED = 1;
	private transient DefaultTableModel resolvedTableModel = new DefaultTableModel(
			new Object[][]{},
			new String[] {"Date", "Time"}
			);
	private transient JTable resolvedTable;
	
	
	private static final int COLUMN_REPORTED_BY = 0;
	private transient DefaultTableModel reporterTableModel = new DefaultTableModel(
			new Object[][]{},
			new String[] {"Reported By"}
			);
	private transient JTable reporterTable;
	

	static {
		ResourcePool.loadImage("save_report_icon","images/save_report.png");
		ResourcePool.loadImage("locate_small", "images/locate_small.png");
    	ResourcePool.loadImage("cog_inverted", "images/cog_inverted.png");
    	ResourcePool.loadImage("cross_inverted", "images/cross_inverted.png");
    	ResourcePool.loadImage("shield_inverted", "images/shield_inverted.png");
    	ResourcePool.loadImage("question_inverted", "images/question_inverted.png");
	}

	public ArchivedIncidentPanel(Incident incident, MapFrame mapFrame)
	{
		super(incident, mapFrame);

		// cosmetic properties
		Color lightBlue = new Color(0xf6f9fc);
		setBackground(lightBlue);
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, new Color(0x618197)));
		Border emptyBorder = BorderFactory.createEmptyBorder();
		setMaximumSize(new Dimension(1000,230));
		setMinimumSize(new Dimension(600,230));
		Font headerFont, codeLabelFont, btnFont;
		btnFont = getFont().deriveFont(Font.BOLD, 11.0f);
		headerFont = getFont().deriveFont(Font.BOLD, 15.0f);
		codeLabelFont = getFont().deriveFont(Font.BOLD, 16.0f);
		respondents = new ArrayList<String>();
		
		// Layout
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
		GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		(idLabel = new JLabel("Incident #" + incident.getID())).setFont(headerFont);
		incidentIconLabel = new JLabel();
		incidentIconLabel.setBorder(emptyBorder);
		incidentIconLabel.setBackground(lightBlue);
		updateButtonState();
				
		incidentDescription = new JTextArea("", 10, 4);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		incidentDescription.setLineWrap(true);
		incidentDescription.setWrapStyleWord(true);
		incidentDescription.setEditable(false);
		incidentDescription.setText(incident.getDescription());
		incidentDescription.setMinimumSize(new Dimension (265, 110));
		
		
		JScrollPane descriptionScroll = new JScrollPane(incidentDescription, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		descriptionScroll.setPreferredSize(new Dimension(265,110));
		descriptionScroll.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(1, 1, 1, 1)));

		
		
		// Times
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		String createdDateString = dateFormat.format(incident.getCreated());
		String archivedDateString = dateFormat.format(incident.getArchivedTime());

		String[] reporteddDateTime = createdDateString.split(" ");
		reportedDate = reporteddDateTime[0];
		reportedTime = reporteddDateTime[1];
		
		String[] archivedDateTime = archivedDateString.split(" ");
		resolvedDate = archivedDateTime[0];
		resolvedTime = archivedDateTime[1];
		
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 
		Date createdDate = null;
		Date archivedDate = null;

		try {
			createdDate = format.parse(createdDateString);
			archivedDate = format.parse(archivedDateString);
			timeDifference = archivedDate.getTime() - createdDate.getTime();
 			secondDifference = timeDifference / 1000 % 60;
			minuteDifference = timeDifference / (60 * 1000) % 60;
			hourDifference = timeDifference / (60 * 60 * 1000) % 24;
			dayDifference = timeDifference / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		resolvedIn = new String[4];
		resolvedIn[0] = String.valueOf(dayDifference);
		resolvedIn[1] = String.valueOf(hourDifference);
		resolvedIn[2] = String.valueOf(minuteDifference);
		resolvedIn[3] = String.valueOf(secondDifference);
		
		

		centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		
		reporterTable = new JTable(reporterTableModel);
		JTableHeader headerReporter = reporterTable.getTableHeader();
		headerReporter.setBackground(new Color(0xc8ddf2));
		headerReporter.setFont(getFont().deriveFont(Font.BOLD));
		reporterTable.getColumnModel().getColumn(COLUMN_REPORTED_BY).setCellRenderer(centerRenderer);
		
		respondentsTable = new JTable(respondentsTableModel);
		JTableHeader headerRespondents = respondentsTable.getTableHeader();
		headerRespondents.setBackground(new Color(0xc8ddf2));
		headerRespondents.setFont(headerFont.deriveFont(headerFont.getStyle() | Font.BOLD));
		
		reportedTable = new JTable(reportedTableModel);
		reportedTableModel.addRow(new Object[] {reportedDate, reportedTime});
		JTableHeader headerReported = reportedTable.getTableHeader();
		headerReported.setBackground(new Color(0xc8ddf2));
		headerReported.setFont(getFont().deriveFont(Font.BOLD));
		reportedTable.getColumnModel().getColumn(COLUMN_DATE_REPORTED).setCellRenderer(centerRenderer);
		reportedTable.getColumnModel().getColumn(COLUMN_TIME_REPORTED).setCellRenderer(centerRenderer);
		
		resolvedTable = new JTable(resolvedTableModel);
		resolvedTableModel.addRow(new Object[] {resolvedDate, resolvedTime});
		JTableHeader headerResolved = resolvedTable.getTableHeader();
		headerResolved.setBackground(new Color(0xc8ddf2));
		headerResolved.setFont(getFont().deriveFont(Font.BOLD));
		resolvedTable.getColumnModel().getColumn(COLUMN_DATE_RESOLVED).setCellRenderer(centerRenderer);
		resolvedTable.getColumnModel().getColumn(COLUMN_TIME_RESOLVED).setCellRenderer(centerRenderer);
		
		resolvedTimeTable = new JTable(resolvedTimeTableModel);
		resolvedTimeTableModel.addRow(new Object[] {dayDifference, hourDifference, minuteDifference, secondDifference});
		JTableHeader headerResolvedTime = resolvedTimeTable.getTableHeader();
		headerResolvedTime.setBackground(new Color(0xc8ddf2));
		headerResolvedTime.setFont(getFont().deriveFont(Font.BOLD));
		resolvedTimeTable.getColumnModel().getColumn(COLUMN_DAYS).setCellRenderer(centerRenderer);
		resolvedTimeTable.getColumnModel().getColumn(COLUMN_HOURS).setCellRenderer(centerRenderer);
		resolvedTimeTable.getColumnModel().getColumn(COLUMN_MINUTES).setCellRenderer(centerRenderer);
		resolvedTimeTable.getColumnModel().getColumn(COLUMN_SECONDS).setCellRenderer(centerRenderer);
		
		JScrollPane respondentsScroll = new JScrollPane(respondentsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		respondentsScroll.setPreferredSize(new Dimension(245,110));
		respondentsScroll.getViewport().setBackground(new Color(0xedf4fb));
		
		JScrollPane reporterScroll = new JScrollPane(reporterTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		reporterScroll.setPreferredSize(new Dimension(200,40));
		
		JScrollPane reportedScroll = new JScrollPane(reportedTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		reportedScroll.setPreferredSize(new Dimension(135,35));
		
		JScrollPane resolvedScroll = new JScrollPane(resolvedTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resolvedScroll.setPreferredSize(new Dimension(135,35));
		
		JScrollPane resolvedTimeScroll = new JScrollPane(resolvedTimeTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resolvedTimeScroll.setPreferredSize(new Dimension(270,35));
		
		
		
		reportedLabel = new JLabel("Reported:");
		reportedLabel.setFont(headerFont);
		
		resolvedLabel = new JLabel("Resolved:");
		resolvedLabel.setFont(headerFont);
		
		resolvedTimeLabel = new JLabel("Resolved In:");
		resolvedTimeLabel.setFont(headerFont);
		

		locateBtn = new JButton("Locate");
		locateBtn.setBackground(lightBlue);
		locateBtn.setIcon(ResourcePool.getIcon("locate_small"));
		locateBtn.setBorder(emptyBorder);
		locateBtn.setFont(btnFont);
		locateBtn.setMinimumSize(new Dimension(40,50));
		locateBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		locateBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		locateBtn.addActionListener(this);
		
		saveBtn = new JButton("Save");
		saveBtn.setBackground(lightBlue);
		saveBtn.setIcon(ResourcePool.getIcon("save_report_icon"));
		saveBtn.setBorder(emptyBorder);
		saveBtn.setFont(btnFont);
		saveBtn.setMinimumSize(new Dimension(40,50));
		saveBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		saveBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		saveBtn.addActionListener(this);

		codeLabel = new JLabel("code");
		codeLabel.setBackground(Color.gray);
		codeLabel.setForeground(Color.white);
		codeLabel.setFont(codeLabelFont);
		codeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		codeLabel.setOpaque(true);
		codeLabel.setBorder(emptyBorder);
		codeLabel.setMinimumSize(new Dimension(115,35));
		codeLabel.setMaximumSize(new Dimension(115,35));


		 String[] fileTypes = {"Plain Text","HTML"};
		    fileTypeSelect = new JComboBox<String>(fileTypes);
		    fileTypeSelect.setMaximumSize(new Dimension(30, 10));
		
		    
		    
		//horizontal layout
		//horizontal layout: top row
		GroupLayout.SequentialGroup topRowSequential = layout.createSequentialGroup();
		GroupLayout.ParallelGroup columnIncidentType = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup columnButtons = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
			
		//horizontal layout: bottom row
		GroupLayout.SequentialGroup bottomRowSequential = layout.createSequentialGroup();
		GroupLayout.ParallelGroup columnDescription = layout.createParallelGroup();
		GroupLayout.ParallelGroup columnTimes = layout.createParallelGroup();
		
		//horizontal layout: incident type column (top row)
		GroupLayout.SequentialGroup IncidentTypeRowSequential = layout.createSequentialGroup();
		IncidentTypeRowSequential.addComponent(incidentIconLabel, 0, GroupLayout.DEFAULT_SIZE, 50);
		IncidentTypeRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, 0);
		IncidentTypeRowSequential.addComponent(codeLabel, 115, GroupLayout.DEFAULT_SIZE, 115);
		columnIncidentType.addGroup(IncidentTypeRowSequential);
		
		//horizontal layout: buttons column (top row)
		GroupLayout.SequentialGroup buttonsRowSequential = layout.createSequentialGroup();
		buttonsRowSequential.addComponent(locateBtn, 0, GroupLayout.DEFAULT_SIZE, 45);
		buttonsRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, 0);
		buttonsRowSequential.addComponent(saveBtn, 0, GroupLayout.DEFAULT_SIZE, 45);
		columnButtons.addGroup(buttonsRowSequential);
		
		//horizontal layout: description column (bottom row)
		columnDescription.addComponent(idLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDescription.addComponent(descriptionScroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		//horizontal layout: Times column (top row)
		GroupLayout.SequentialGroup timesColumnTopRow = layout.createSequentialGroup();
		GroupLayout.ParallelGroup columnReported = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup columnResolved = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		
		//Times column: Reported column
		columnReported.addComponent(reportedLabel, 0, GroupLayout.DEFAULT_SIZE, 145);
		columnReported.addComponent(reportedScroll, 0, GroupLayout.DEFAULT_SIZE, 145);
		
		//Times column: Resolved column
		columnResolved.addComponent(resolvedLabel, 0, GroupLayout.DEFAULT_SIZE, 145);
		columnResolved.addComponent(resolvedScroll, 0, GroupLayout.DEFAULT_SIZE, 145);
				
		timesColumnTopRow.addGroup(columnReported);
		timesColumnTopRow.addGroup(columnResolved);
		
		//horizontal layout: Times column (bottom row)
		GroupLayout.ParallelGroup timesColumnBottomRow = layout.createParallelGroup();
		timesColumnBottomRow.addComponent(resolvedTimeLabel);
		timesColumnBottomRow.addComponent(resolvedTimeScroll, 0, GroupLayout.DEFAULT_SIZE, 300);
		
		columnTimes.addGroup(timesColumnTopRow);
		columnTimes.addGroup(timesColumnBottomRow);
		
		topRowSequential.addGroup(columnIncidentType);
		topRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 80, Short.MAX_VALUE);
		topRowSequential.addComponent(reporterScroll, 0, GroupLayout.DEFAULT_SIZE, 200);
		topRowSequential.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 80, Short.MAX_VALUE);
		topRowSequential.addGroup(columnButtons);
		
		bottomRowSequential.addGroup(columnDescription);
		bottomRowSequential.addComponent(respondentsScroll);
		bottomRowSequential.addGroup(columnTimes);
		
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
		incidentTypeParallel.addComponent(codeLabel, 35, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
				
		//vertical layout: buttons column (top row)
		GroupLayout.ParallelGroup buttonRowParallel = layout.createParallelGroup();
		buttonRowParallel.addComponent(locateBtn, 40, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		buttonRowParallel.addComponent(saveBtn, 40, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		//vertical: top row
		rowTop.addGroup(incidentTypeParallel);
		rowTop.addComponent(reporterScroll);
		rowTop.addGroup(buttonRowParallel);
		
		
		//vertical layout: description column (bottom row)
		GroupLayout.SequentialGroup descriptionGroup = layout.createSequentialGroup();
		descriptionGroup.addComponent(idLabel);
		descriptionGroup.addComponent(descriptionScroll);
		
		//Vertical layout: respondents column
		GroupLayout.SequentialGroup respondentsColumn = layout.createSequentialGroup();
		respondentsColumn.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE);
		respondentsColumn.addComponent(respondentsScroll, 110, GroupLayout.DEFAULT_SIZE, 100);
		
		//vertical layout: times column (bottom row)
		GroupLayout.SequentialGroup timesGroup = layout.createSequentialGroup();
		
		GroupLayout.ParallelGroup timesTopRowParallel = layout.createParallelGroup();
		GroupLayout.SequentialGroup reportedColumn = layout.createSequentialGroup();
		reportedColumn.addComponent(reportedLabel);
		reportedColumn.addComponent(reportedScroll);
		GroupLayout.SequentialGroup resolvedColumn = layout.createSequentialGroup();
		resolvedColumn.addComponent(resolvedLabel);
		resolvedColumn.addComponent(resolvedScroll);
		timesTopRowParallel.addGroup(reportedColumn);
		timesTopRowParallel.addGroup(resolvedColumn);
		
		timesGroup.addGroup(timesTopRowParallel);
		timesGroup.addComponent(resolvedTimeLabel);
		timesGroup.addComponent(resolvedTimeScroll);
		
		//vertical: bottom row
		rowBottom.addGroup(descriptionGroup);
		rowBottom.addGroup(respondentsColumn);
		rowBottom.addGroup(timesGroup);
		
		vertical.addGroup(rowTop);
		vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 50, 50);
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
	public void actionPerformed(ActionEvent e) {

		Incident incident = getIncident();
		//Save report to text file
		if(e.getSource() == saveBtn)
		{
			ArchivedIncidentPage htmlPage = new ArchivedIncidentPage();
			htmlPage.createReportTable(reportedDate, reportedTime, reporterName, codeLabel.getText(), incident.getLocation().toString());
			htmlPage.createResolvedTable(resolvedDate, resolvedTime, resolvedIn);
			htmlPage.createRespondentsTable(respondents);
			htmlPage.createDesc(incident.getDescription());
			htmlPage.createPage(incident.getType().toString(), incident.getID());
		}
		
		if (e.getSource() == locateBtn)
			locateObjectOnMap(getIncident());
			
		
	}
	
	public void setReportingUserText(String user)
	{
		reporterTableModel.addRow(new Object[] {user});
		reporterName = user;
	}
	
	public void saveReport(Incident incident)
	{
		ArchivedIncidentPage htmlPage = new ArchivedIncidentPage();
		htmlPage.createReportTable(reportedDate, reportedTime, reporterName, codeLabel.getText(), incident.getLocation().toString());
		htmlPage.createResolvedTable(resolvedDate, resolvedTime, resolvedIn);
		htmlPage.createRespondentsTable(respondents);
		htmlPage.createDesc(incident.getDescription());
		htmlPage.createPage(incident.getType().toString(), incident.getID());
	}

	// ///////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// ///////////////////////////////////////////////////////////////////

	private void updateButtonState()
	{
		ImageIcon icon = null;
		switch (getIncident().getType())
		{
			case Medical: icon = ResourcePool.getIcon("cross_inverted"); break;
			case Security: icon = ResourcePool.getIcon("shield_inverted"); break;
			case WiFindUs: icon = ResourcePool.getIcon("cog_inverted"); break;
			default: icon = ResourcePool.getIcon("question_inverted"); break;
		}
		incidentIconLabel.setIcon(icon); 
	}


	@Override
	public void incidentArchivedResponderAdded(Incident incident, User user) {
		// TODO Auto-generated method stub
		respondentsTableModel.addRow(new Object[] {user.getNameFull()});
		respondents.add(user.getNameFull());
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
	
	@Override public void incidentDescriptionChanged(Incident incident) 
	{ 

	}
}








