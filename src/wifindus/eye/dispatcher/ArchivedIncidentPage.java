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
		
		location = location.replaceAll("°", "&deg;").replaceFirst(" ", ", ");
		reportTable = "<table id=\"report\">\n"
							+"<tr>\n"
								+"<th colspan=\"2\">Report</th>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Date</th>\n"
								+"<td>"+dateCreated+"</td>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Time</th>\n"
								+"<td>"+timeCreated+"</td>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>By</th>\n"
								+"<td>"+reporter+"</td>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Code</th>\n"
								+"<td>"+emergencyCode+"</td>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Location</th>\n"
								+"<td>"+location+"</td>\n"
							+"</tr>\n"
						+"</table>\n";		
	}
	
	public void createResolvedTable(String dateResolved, String timeResolved, String[] resolvedIn) {
		resolvedTable = "<table id=\"resolved\">\n"
							+"<tr>\n"
								+"<th colspan=\"4\">Resolved</th>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Date</th>\n"
								+"<td colspan=\"3\">"+dateResolved+"</td>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Time</th>\n"
								+"<td colspan=\"3\">"+timeResolved+"</td>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th colspan=\"4\">Resolved In</th>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<th>Days</th>\n"
								+"<th>Hours</th>\n"
								+"<th>Minutes</th>\n"
								+"<th>Seconds</th>\n"
							+"</tr>\n"
							+"<tr>\n"
								+"<td>"+resolvedIn[0]+"</td>\n"
								+"<td>"+resolvedIn[1]+"</td>\n"
								+"<td>"+resolvedIn[2]+"</td>\n"
								+"<td>"+resolvedIn[3]+"</td>\n"
							+"</tr>\n"
						+"</table>\n";
	}
	
	public void createRespondentsTable(List<String> respondents) {
		String respondentsRows = "";
		for(int i=0; i<respondents.size(); i++)
		{
			respondentsRows= respondentsRows
							+"<tr>\n"
								+"<td>"+respondents.get(i)+"</td>\n"
							+"</tr>\n";
		}
		
		respondentsTable = "<table id=\"respondents\">\n"
							+"<tr>\n"
								+"<th>Respondents</th>\n"
							+"</tr>\n"
							+respondentsRows
						+"</table>\n";
	}
	
	public void createDesc(String incidentDesc) {
		descDiv ="<div class=\"desc\">\n"
							+"<p>"+incidentDesc+"</p>\n"
						+"</div>\n";
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
		
		
		htmlCode = "<html>\n"
								+"<head>\n"
									+"<link rel=\"stylesheet\" href=\"css/styles.css\">\n"
									+"<script type=\"text/javascript\" src=\"js/script.js\"></script>\n"
								+"</head>\n"
								
								+"<body onload=\"getIncidents()\">\n"
									+"<div class=\"header\">\n"
										+"<img id=\"logo\" src=\"../images/wfu_logo.png\"/>\n"
										
										+"<ul class=\"selectIncident\">\n"
											+"<li>Incident Number:</li>\n"
											+"<li>\n"
												+"<select id=\"incidents\" onchange=\"changeIncident()\">\n"
													+"<option value=\"\"></option>\n"
												+"</select>\n"
											+"</li>\n"
										+"</ul>\n"
									+"</div>\n"
									
									+"<ul class=\"incident\">\n"
										+"<li><img id=\"incidentType\" src=\"../images/"+iconPath+"\"/></li>\n"
										+ "<li id=\"incidentID\">Incident #"+incidentID+"</li>\n"
									+"</ul>\n"
										
									+"<div class=\"tables\">\n"
										+reportTable
										+resolvedTable
										+respondentsTable
									+"</div>\n"
									
									+descDiv
									
								+"</body>\n"
							+"</html>\n";
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
		String getIncident = "function getIncidents(){\n"
								+"var selectOptions = \"\";\n"
								+"for(var i=0; i<incident.length; i++)\n"
								+"{selectOptions = selectOptions + \"<option value=\"+incident[i]+\">\"+incident[i]+\"</option>\";}\n"
								+"document.getElementById(\"incidents\").innerHTML = selectOptions;}\n";
		
		String changeIncident = "function changeIncident(){\n"
									+"var incidentNum = document.getElementById(\"incidents\").value;\n"
									+"window.location = \"incident_\"+incidentNum+\".html\";}\n";
		
	    
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
