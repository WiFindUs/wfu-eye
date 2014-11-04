/*=========================================================== 
WiFindUs Festival Eye - Database test data population script
Mark Gillard, Hussein Al Hammad, Mitchell Templeton for WiFindUs
  -----------------------------------------------------------*/
CREATE DATABASE IF NOT EXISTS wfu_eye_db;
USE wfu_eye_db;

/*=========================================================== 
  User	
  -----------------------------------------------------------*/
INSERT INTO
	Users (id, nameFirst, nameMiddle, nameLast, personnelType)
VALUES 
	/* wfu personnel */
	(1,'Mark', 		'Stephen', 	'Gillard', 		'WFU'),
	(2,'Hussein', 	'', 		'Al Hammad', 	'WFU'),
	(3,'Mitchell', 	'', 		'Templeton', 	'WFU'),
	(4,'Greg', 		'', 		'Stevens', 		'WFU'),
	(5,'Mateus', 	'', 		'Nolasco', 		'WFU'),
	(6,'Ben', 		'', 		'Quast', 		'WFU'),
	(7,'Travis', 	'', 		'Grund', 		'WFU'),
	
	/* medical personnel at morphettville */
	(8,	'Kieran', '', 'Speck', 'MED'),
	(9,	'Med', '', 'Test 2', 'MED'),
	(10,'Med', '', 'Test 3', 'MED'),
	(11,'Med', '', 'Test 4', 'MED'),
	(12,'Med', '', 'Test 5', 'MED');

/*=========================================================== 
  Device	- AUTO ADDED USING EYE CLIENT APP! ^_^
  -----------------------------------------------------------
INSERT INTO
	Devices (hash, userID, deviceType, lastUpdate, latitude, longitude, accuracy)
VALUES 
	('TYRZz3u0', 1,    'PHO', NOW(), -34.911282, 138.578099, 20.0),
	('AbUNZ0xl', 2,    'PHO', NOW(), -34.912356, 138.580546, 30.0),
	('WuQkhzZB', 3,    'PHO', NOW(), -34.918109, 138.582713, 40.0),
	('NsiBN8kt', 8,    'PHO', NOW(), -34.915928, 138.584043, 100.0),
	('xkkk7tzT', 9,    'PHO', NOW(), -34.919306, 138.580524, 10.0),
	('DH1527J9', NULL, 'PHO', NOW(), -34.914696, 138.583099, 50.0),
	('RLvlP1Sp', 14,   'TAB', NOW(), -34.911142, 138.580803, 100.0),
	('nopGICOT', NULL, 'TAB', NOW(), -34.911282, 138.578099, 200.0),
	('Bj4952Gg', NULL, 'PHO', NOW(), -34.918109, 138.582348, 30.0),
	('8SL4oA6X', NULL, 'WAT', NOW(), -34.915716, 138.581318, 20.0);
	*/
	
/*=========================================================== 
  Node	
  -----------------------------------------------------------*/
INSERT INTO
	Nodes (hash, latitude, longitude, lastUpdate)
VALUES 
	('wfumesh1', -34.914850, 138.578958, NOW()),
	('wfumesh2', -34.917093, 138.580020, NOW()),
	/* ('wfumesh3', -34.916838, 138.582359, NOW()), */
	('wfumesh4', -34.918008, 138.583196, NOW()),
	('wfumesh5', -34.917815, 138.579226, NOW()),
	('wfumesh6', -34.916126, 138.581028, NOW());
    
/*=========================================================== 
  Incidents		- AUTO ADDED USING EYE DISPATCHER! ^_^
  -----------------------------------------------------------
INSERT INTO
	Incidents (id, incidentType, latitude, longitude, created)
VALUES 
	(1, 'MED', -34.911282, 138.578099, NOW()),
	(2, 'SEC', -34.919306, 138.580524, NOW());
	
/*=========================================================== 
  Assign an incidents
  -----------------------------------------------------------
UPDATE
	Devices
SET
	respondingIncidentID=1
WHERE
	hash='xkkk7tzT';
	*/




