/*=========================================================== 
WiFindUs Festival Eye - Database initialization script
Mark Gillard, Hussein Al Hammad, Mitchell Templeton for WiFindUs
  -----------------------------------------------------------*/
CREATE DATABASE IF NOT EXISTS wfu_eye_db;
USE wfu_eye_db;

SET foreign_key_checks = 0;
DROP TABLE IF EXISTS PastIncidentResponders;
DROP TABLE IF EXISTS DeviceUsers;
DROP TABLE IF EXISTS Incidents;
DROP TABLE IF EXISTS Devices;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Nodes;
SET foreign_key_checks = 1;

CREATE TABLE Devices (
  hash                 char(8) NOT NULL, 
  deviceType           char(3) DEFAULT 'PHO' NOT NULL CHECK (deviceType IN('PHO','TAB','WAT','COM','OTH')), 
  address              varchar(255), 
  latitude             decimal(18, 16) CHECK (latitude IS NULL OR latitude BETWEEN -90.00000000000000 AND 90.00000000000000), 
  longitude            decimal(18, 15) CHECK (longitude IS NULL OR longitude BETWEEN -180.00000000000000 AND 180.00000000000000), 
  altitude             decimal(9, 2), 
  accuracy             decimal(8, 4) CHECK (accuracy IS NULL OR accuracy BETWEEN 0.0000 AND 9999.9999), 
  humidity             decimal(9, 2), 
  airPressure          decimal(9, 2), 
  temperature          decimal(9, 2), 
  lightLevel           decimal(9, 2), 
  lastUpdate           datetime DEFAULT '1970-01-01 00:00:00' NOT NULL, 
  respondingIncidentID int(10), 
  PRIMARY KEY (hash)) ENGINE=InnoDB;
  
CREATE TABLE Users (
  id	        int(10) NOT NULL AUTO_INCREMENT, 
  nameFirst     varchar(32) NOT NULL CHECK (LEN(nameFirst) > 0), 
  nameMiddle    varchar(32) NOT NULL CHECK (LEN(nameMiddle) > 0), 
  nameLast      varchar(32) NOT NULL CHECK (LEN(nameLast) > 0), 
  personnelType char(3) NOT NULL CHECK (personnelType IN('MED','SEC','WFU')), 
  PRIMARY KEY (id)) ENGINE=InnoDB;
  
CREATE TABLE Incidents (
  id   int(10) NOT NULL AUTO_INCREMENT, 
  incidentType char(3) NOT NULL CHECK (incidentType IN('MED','SEC','WFU')), 
  latitude     decimal(18, 16) NOT NULL CHECK (latitude BETWEEN -90.00000000000000 AND 90.00000000000000), 
  longitude    decimal(18, 15) NOT NULL CHECK (longitude BETWEEN -180.00000000000000 AND 180.00000000000000), 
  altitude     decimal(9, 2), 
  accuracy     decimal(8, 4) CHECK (accuracy IS NULL OR accuracy BETWEEN 0.0000 AND 9999.9999), 
  created      datetime DEFAULT '1970-01-01 00:00:00' NOT NULL, 
  archived     tinyint DEFAULT 0 NOT NULL, 
  archivedTime datetime, 
  severity	   int(10),
  code		   varchar(32), 
  reportingUserID int(10), 
  PRIMARY KEY (id)) ENGINE=InnoDB;
  
CREATE TABLE PastIncidentResponders (
  userID     int(10) NOT NULL, 
  incidentID int(10) NOT NULL, 
  PRIMARY KEY (userID, incidentID)) ENGINE=InnoDB;
  
CREATE TABLE DeviceUsers (
  userID     int(10) NOT NULL, 
  deviceHash char(8) NOT NULL, 
  PRIMARY KEY (userID, deviceHash)) ENGINE=InnoDB;
  
CREATE TABLE Nodes (
  hash       char(8) NOT NULL, 
  address    varchar(255), 
  latitude   decimal(18, 16) CHECK (latitude IS NULL OR latitude BETWEEN -90.00000000000000 AND 90.00000000000000), 
  longitude  decimal(18, 15) CHECK (longitude IS NULL OR longitude BETWEEN -180.00000000000000 AND 180.00000000000000), 
  altitude   decimal(9, 2), 
  accuracy   decimal(8, 4) CHECK (accuracy IS NULL OR accuracy BETWEEN 0.0000 AND 9999.9999), 
  voltage    decimal(6, 4), 
  lastUpdate datetime DEFAULT '1970-01-01 00:00:00' NOT NULL, 
  PRIMARY KEY (hash)) ENGINE=InnoDB;
  
ALTER TABLE Devices ADD INDEX `Responded to by` (respondingIncidentID), ADD CONSTRAINT `Responded to by` FOREIGN KEY (respondingIncidentID) REFERENCES Incidents (id);
ALTER TABLE DeviceUsers ADD INDEX `Uses a Device according to` (userID), ADD CONSTRAINT `Uses a Device according to` FOREIGN KEY (userID) REFERENCES Users (id);
ALTER TABLE DeviceUsers ADD INDEX `In use by user according to` (deviceHash), ADD CONSTRAINT `In use by user according to` FOREIGN KEY (deviceHash) REFERENCES Devices (hash);
ALTER TABLE Incidents ADD INDEX `Reported by` (reportingUserID), ADD CONSTRAINT `Reported by` FOREIGN KEY (reportingUserID) REFERENCES Incidents (id);
ALTER TABLE PastIncidentResponders ADD INDEX `Responded to an incident` (userID), ADD CONSTRAINT `Responded to an incident` FOREIGN KEY (userID) REFERENCES Users (id);
ALTER TABLE PastIncidentResponders ADD INDEX `Was reponded to by user` (incidentID), ADD CONSTRAINT `Was reponded to by user` FOREIGN KEY (incidentID) REFERENCES Incidents (id);
