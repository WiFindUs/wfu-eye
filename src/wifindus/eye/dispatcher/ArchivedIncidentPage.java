package wifindus.eye.dispatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;




public class ArchivedIncidentPage {

	private int incidentID;
	private String reportTable, resolvedTable, respondentsTable, descDiv, htmlCode, medicalIcon, securityIcon, wfuIcon, unknownIcon, jsArray;
	private List<Integer> incidentIDs;
	

	public ArchivedIncidentPage(){
		medicalIcon = "cross_inverted_themed.png";
		securityIcon = "shield_inverted_themed.png";
		wfuIcon = "cog_inverted_themed.png";
		unknownIcon = "question_inverted_themed.png";
		
	}
	
	public void createReportTable(String dateCreated, String timeCreated, String reporter, String emergencyCode, String location) {
		String[] coordinates = location.split(" ");
		
		String coordinateHtml1 = coordinates[0].substring(0, coordinates[0].indexOf("°"));
		coordinateHtml1 = coordinateHtml1+"&deg;"+coordinates[0].substring(coordinates[0].indexOf("°") + 1);
		
		String coordinateHtml2 = coordinates[1].substring(0, coordinates[1].indexOf("°"));
		coordinateHtml2 = coordinateHtml2+"&deg;"+coordinates[1].substring(coordinates[1].indexOf("°") + 1);
		
		location = coordinateHtml1 + ", " + coordinateHtml2;

		reportTable = "<table id=\"report\">"
							+"<tr>"
								+"<th colspan=\"2\">Report</th>"
							+"</tr>"
							+"<tr>"
								+"<th>Date</th>"
								+"<td>"+dateCreated+"</td>"
							+"</tr>"
							+"<tr>"
								+"<th>Time</th>"
								+"<td>"+timeCreated+"</td>"
							+"</tr>"
							+"<tr>"
								+"<th>By</th>"
								+"<td>"+reporter+"</td>"
							+"</tr>"
							+"<tr>"
								+"<th>Code</th>"
								+"<td>"+emergencyCode+"</td>"
							+"</tr>"
							+"<tr>"
								+"<th>Location</th>"
								+"<td>"+location+"</td>"
							+"</tr>"
						+"</table> ";		
	}
	
	public void createResolvedTable(String dateResolved, String timeResolved, String[] resolvedIn) {
		resolvedTable = "<table id=\"resolved\">"
							+"<tr>"
								+"<th colspan=\"4\">Resolved</th>"
							+"</tr>"
							+"<tr>"
								+"<th>Date</th>"
								+"<td colspan=\"3\">"+dateResolved+"</td>"
							+"</tr>"
							+"<tr>"
								+"<th>Time</th>"
								+"<td colspan=\"3\">"+timeResolved+"</td>"
							+"</tr>"
							+"<tr>"
								+"<th colspan=\"4\">Resolved In</th>"
							+"</tr>"
							+"<tr>"
								+"<th>Days</th>"
								+"<th>Hours</th>"
								+"<th>Minutes</th>"
								+"<th>Seconds</th>"
							+"</tr>"
							+"<tr>"
								+"<td>"+resolvedIn[0]+"</td>"
								+"<td>"+resolvedIn[1]+"</td>"
								+"<td>"+resolvedIn[2]+"</td>"
								+"<td>"+resolvedIn[3]+"</td>"
							+"</tr>"
						+"</table> ";
	}
	
	public void createRespondentsTable(List<String> respondents) {
		String respondentsRows = "";
		for(int i=0; i<respondents.size(); i++)
		{
			respondentsRows= respondentsRows
							+"<tr>"
								+"<td>"+respondents.get(i)+"</td>"
							+"</tr>";
		}
		
		respondentsTable = "<table id=\"respondents\">"
							+"<tr>"
								+"<th>Respondents</th>"
							+"</tr>"
							+respondentsRows
						+"</table>";
	}
	
	public void createDesc(String incidentDesc) {
		descDiv ="<div class=\"desc\">"
							+"<p>"+incidentDesc+"</p>"
						+"</div>";
	}
	
	public void createPage(String incidentType, int incidentID) {
		this.incidentID = incidentID;
		String iconPath = null;
		switch (incidentType)
		{
			case "Medical": iconPath = medicalIcon; break;
			case "Security": iconPath = securityIcon; break;
			case "WiFindUs": iconPath = wfuIcon; break;
			default: iconPath = unknownIcon; break;
		}
		
		
		htmlCode = "<html>"
								+"<head>"
									+"<link rel=\"stylesheet\" href=\"css/styles.css\">"
									+"<script type=\"text/javascript\" src=\"js/script.js\"></script>"
								+"</head>"
								
								+"<body onload=\"getIncidents()\">"
									+"<div class=\"header\">"
										+"<img id=\"logo\" src=\"../images/wfu_logo.png\"/>"
										
										+"<ul class=\"selectIncident\">"
											+"<li>Incident Number:</li>"
											+"<li>"
												+"<select id=\"incidents\" onchange=\"changeIncident()\">"
													+"<option value=\"\"></option>"
												+"</select>"
											+"</li>"
										+"</ul>"
									+"</div>"
									
									+"<ul class=\"incident\">"
										+"<li><img id=\"incidentType\" src=\"../images/"+iconPath+"\"/></li><li id=\"incidentID\">Incident #"+incidentID+"</li>"
									+"</ul>"
										
									+"<div class=\"tables\">"
										+reportTable
										+resolvedTable
										+respondentsTable
									+"</div>"
									
									+descDiv
									
								+"</body>"
							+"</html>";
		writeFile();
		getIncidentList();
		updateScript();
	}
	
	public void writeFile() {
		String fileType =  ".html";
		String filePath = "reports/incident_"+incidentID+fileType;
		BufferedWriter writer = null;
		
		try
		{
			writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(htmlCode);
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
	            }
	            catch (Exception ex) 
	            {
	            }
	        }
	}
	
	
	public void getIncidentList(){
		File folder = new File("reports");
		File[] listOfFiles = folder.listFiles();
		incidentIDs = new ArrayList<Integer>();
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{
		      if (listOfFiles[i].isFile()) 
		      {
		    	  String fileName = listOfFiles[i].getName();
		    	  if(fileName.contains("incident_"))
		    	  {
		    		  String sub = fileName.substring(fileName.indexOf("_") + 1);
		    		  sub =sub.substring(0, sub.indexOf("."));
		    		  incidentIDs.add(Integer.parseInt(sub));
		    	  }
		      } 
		}
		
		Collections.sort(incidentIDs);
		String jsArrayPopulate ="";
		for(int i=0; i<incidentIDs.size(); i++){
			if(i==incidentIDs.size()-1)
			{
				jsArrayPopulate = jsArrayPopulate + "\""+ incidentIDs.get(i) + "\"";
			}
			else
			{
				jsArrayPopulate = jsArrayPopulate + "\""+ incidentIDs.get(i) +"\",";
			}
		}
		jsArray = "var incident = new Array(\"\" ,"+ jsArrayPopulate+ ");";
	}
	
	public void updateScript() {
		String getIncident = "function getIncidents(){"
								+"var selectOptions = \"\";"
								+"for(var i=0; i<incident.length; i++)"
								+"{selectOptions = selectOptions + \"<option value=\"+incident[i]+\">\"+incident[i]+\"</option>\";}"
								+"document.getElementById(\"incidents\").innerHTML = selectOptions;}";
		
		String changeIncident = "function changeIncident(){"
									+"var incidentNum = document.getElementById(\"incidents\").value;"
									+"window.location = \"incident_\"+incidentNum+\".html\";}";
		
	    
	    String filePath = "reports/js/script.js";
		BufferedWriter writer = null;
		
		try
		{
			writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(jsArray);
			writer.write(System.getProperty("line.separator"));
			writer.write(getIncident);
			writer.write(System.getProperty("line.separator"));
			writer.write(changeIncident);
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
	            }
	            catch (Exception ex) 
	            {
	            }
	        }
	}
}
