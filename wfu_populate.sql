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
	
	(8,	'Med', '', 'Test 1', 'MED'),
	(9,	'Med', '', 'Test 2', 'MED'),
	(10,'Med', '', 'Test 3', 'MED'),
	(11,'Med', '', 'Test 4', 'MED'),
	(12,'Med', '', 'Test 5', 'MED'),
	
	(13,'Security', '', 'Test 1', 'SEC'),
	(14,'Security', '', 'Test 2', 'SEC'),
	(15,'Security', '', 'Test 3', 'SEC'),
	(16,'Security', '', 'Test 4', 'SEC'),
	(17,'Security', '', 'Test 5', 'SEC');

/*=========================================================== 
  Devices
  ----------------------------------------------------------*/
INSERT INTO
	Devices (hash, userID, deviceType, lastUpdate, latitude, longitude, accuracy)
VALUES 
	('TYRZz3u0', 1,  'TAB', NOW(), -34.97502, 138.540119, 10.0),
	('AbUNZ0xl', 2,  'PHO', NOW(), -34.976842, 138.539199, 20.0),
	('WuQkhzZB', 3,  'PHO', NOW(), -34.976534, 138.538601, 10.0),
	('NsiBN8kt', 8,  'PHO', NOW(), -34.976063, 138.538632, 20.0),
	('xkkk7tzT', 9,  'PHO', NOW(), -34.975824, 138.539107, 20.0),
	('DH1527J9', 10, 'PHO', NOW(), -34.976076, 138.539713,  40.0),
	('RLvlP1Sp', 11, 'PHO', NOW(), -34.976277, 138.540073,  40.0),
	('nopGICOT', 12, 'PHO', NOW(), -34.976006, 138.540441,  20.0),
	('Bj4952Gg', 13, 'PHO', NOW(), -34.975818, 138.540188,  10.0),
	('nsjrigje', 14, 'PHO', NOW(), -34.975416, 138.539812,  10.0),
	('mjejrejm', 15, 'PHO', NOW(), -34.97507, 138.540157,  10.0),
	('smfj3wnd', 16, 'PHO', NOW(), -34.975064, 138.540878,  10.0),
	('msnrj3lw', 17, 'PHO', NOW(), -34.975403, 138.541315,  10.0);

/*=========================================================== 
  Node	
  -----------------------------------------------------------*/
INSERT INTO
	Nodes (hash, latitude, longitude, lastUpdate)
VALUES 
	('wfumesh1', -34.97502, 138.540119, NOW()),
	('wfumesh2', -34.975705, 138.538716, NOW()),
	('wfumesh3', -34.975969, 138.539912, NOW()),
	('wfumesh4', -34.975296, 138.540847, NOW()),
	('wfumesh5', -34.974549, 138.541115, NOW()),
	('wfumesh6', -34.9745048, 138.542288, NOW());
	
/*=========================================================== 
  Incidents
  ----------------------------------------------------------*/
INSERT INTO
	Incidents (id, incidentType, latitude, longitude, created, reportingUserID)
VALUES 
	(1, 'MED', -34.974643, 138.54169, NOW(), 16),
	(2, 'SEC', -34.975353, 138.539398, NOW(), 5);


/*=========================================================== 
  Assign an incidents
  ----------------------------------------------------------*/
UPDATE
	Devices
SET
	respondingIncidentID=1
WHERE
	hash='NsiBN8kt' OR hash='xkkk7tzT';
	
UPDATE
	Devices
SET
	respondingIncidentID=2
WHERE
	hash='Bj4952Gg' OR hash='nsjrigje';





