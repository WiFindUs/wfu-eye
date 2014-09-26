package wifindus.eye.dispatcher;

import java.util.ArrayList;

public class Incident {

    int id = 0;

    String status;
    int time;

    ArrayList<PersonnelRecordPanel> respondingMedical;

    ArrayList<PersonnelRecordPanel> respondingSecurity;

    String type;
    String description;
    int severity;
    int latitude;
    int longitude;

    public Incident(int id) 
    {
        this.id = id;
        respondingMedical = new ArrayList<>();
        respondingSecurity = new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    
      public void setId(int id) {
         this.id = id;
    }
      
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public ArrayList<PersonnelRecordPanel> getRespondingMedical() {
        return respondingMedical;
    }


    
    public void setRespondingMedical(ArrayList<PersonnelRecordPanel> respondingMedical) {
        this.respondingMedical = respondingMedical;
    }

    
    
    
    
    public ArrayList<PersonnelRecordPanel> getRespondingSecurity() {
        return respondingSecurity;
    }

    public void setRespondingSecurity(ArrayList<PersonnelRecordPanel> respondingSecurity) {
        this.respondingSecurity = respondingSecurity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

}
