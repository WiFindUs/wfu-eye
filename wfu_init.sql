/*
WiFindUs Festival Eye
SQL Generated by VP UML 22/09/2014 11:30:14 PM
Mark Gillard, Hussein Al Hammad, Mitchell Templeton for WiFindUs
*/
CREATE DATABASE IF NOT EXISTS wfu_eye_db;
USE wfu_eye_db;

SET foreign_key_checks = 0;
DROP TABLE IF EXISTS Incident;
DROP TABLE IF EXISTS Device;
DROP TABLE IF EXISTS `User`;
DROP TABLE IF EXISTS DeviceUserLink;
DROP TABLE IF EXISTS Node;
SET foreign_key_checks = 1;
CREATE TABLE Device (
  deviceID             int(10) NOT NULL AUTO_INCREMENT, 
  hash                 char(8) NOT NULL UNIQUE, 
  deviceType           char(3) DEFAULT 'PHO' NOT NULL CHECK (deviceType IN('PHO','TAB','WAT','COM','OTH')), 
  address              varchar(255) DEFAULT NULL, 
  latitude             decimal(18, 16) DEFAULT NULL CHECK (latitude BETWEEN -90.00000000000000 AND 90.00000000000000), 
  longitude            decimal(18, 15) DEFAULT NULL CHECK (longitude BETWEEN -180.00000000000000 AND 180.00000000000000), 
  altitude             decimal(9, 2) DEFAULT NULL, 
  accuracy             decimal(8, 4) DEFAULT NULL CHECK (accuracy BETWEEN 0.0000 AND 9999.9999), 
  humidity             decimal(9, 2) DEFAULT NULL, 
  airPressure          decimal(9, 2) DEFAULT NULL, 
  temperature          decimal(9, 2) DEFAULT NULL, 
  lightLevel           decimal(9, 2) DEFAULT NULL, 
  lastUpdate           datetime DEFAULT '1000-01-01 00:00:00' NOT NULL, 
  respondingIncidentID int(10) DEFAULT NULL, 
  PRIMARY KEY (deviceID)) ENGINE=InnoDB;
CREATE TABLE `User` (
  userID        int(10) NOT NULL AUTO_INCREMENT, 
  nameFirst     varchar(32) NOT NULL CHECK (LEN(nameFirst) > 0), 
  nameLast      varchar(32) NOT NULL CHECK (LEN(nameLast) > 0), 
  personnelType char(3) NOT NULL CHECK (personnelType IN('MED','SEC','WFU')), 
  PRIMARY KEY (userID)) ENGINE=InnoDB;
CREATE TABLE Incident (
  incidentID   int(10) NOT NULL AUTO_INCREMENT, 
  incidentType char(3) NOT NULL CHECK (incidentType IN('MED','SEC','WFU')), 
  latitude     decimal(18, 16) NOT NULL CHECK (latitude BETWEEN -90.00000000000000 AND 90.00000000000000), 
  longitude    decimal(18, 15) NOT NULL CHECK (longitude BETWEEN -180.00000000000000 AND 180.00000000000000), 
  altitude     decimal(9, 2), 
  PRIMARY KEY (incidentID)) ENGINE=InnoDB;
CREATE TABLE DeviceUserLink (
  userID   int(10) NOT NULL, 
  deviceID int(10) NOT NULL, 
  PRIMARY KEY (userID, 
  deviceID)) ENGINE=InnoDB;
CREATE TABLE Node (
  nodeID     int(10) NOT NULL AUTO_INCREMENT, 
  hash       char(8) NOT NULL UNIQUE, 
  address    varchar(255) DEFAULT NULL, 
  latitude   decimal(18, 16) DEFAULT NULL CHECK (latitude BETWEEN -90.00000000000000 AND 90.00000000000000), 
  longitude  decimal(18, 15) DEFAULT NULL CHECK (longitude BETWEEN -180.00000000000000 AND 180.00000000000000), 
  voltage    decimal(6, 4) DEFAULT NULL, 
  lastUpdate datetime DEFAULT '1000-01-01 00:00:00' NOT NULL, 
  PRIMARY KEY (nodeID)) ENGINE=InnoDB;
ALTER TABLE Device ADD INDEX `Responded to by` (respondingIncidentID), ADD CONSTRAINT `Responded to by` FOREIGN KEY (respondingIncidentID) REFERENCES Incident (incidentID);
ALTER TABLE DeviceUserLink ADD INDEX `Uses a Device according to` (userID), ADD CONSTRAINT `Uses a Device according to` FOREIGN KEY (userID) REFERENCES `User` (userID);
ALTER TABLE DeviceUserLink ADD INDEX `In use by user according to` (deviceID), ADD CONSTRAINT `In use by user according to` FOREIGN KEY (deviceID) REFERENCES Device (deviceID);
