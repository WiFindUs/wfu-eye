var incident = new Array("" ,"1","2","3","39");
function getIncidents(){
var selectOptions = "";
for(var i=0; i<incident.length; i++)
{selectOptions = selectOptions + "<option value="+incident[i]+">"+incident[i]+"</option>";}
document.getElementById("incidents").innerHTML = selectOptions;}

function changeIncident(){
var incidentNum = document.getElementById("incidents").value;
window.location = "incident_"+incidentNum+".html";}
