/*=========================================================== 
WiFindUs Festival Eye - Database test data population script
Mark Gillard, Hussein Al Hammad, Mitchell Templeton for WiFindUs
  -----------------------------------------------------------*/
CREATE DATABASE IF NOT EXISTS wfu_eye_db;
USE wfu_eye_db;

/*=========================================================== 
  Device	
  -----------------------------------------------------------*/
INSERT INTO
	Devices (hash, deviceType, lastUpdate)
VALUES 
	('TYRZz3u0','PHO', NOW()),
	('AbUNZ0xl','PHO', NOW()),
	('WuQkhzZB','PHO', NOW()),
	('NsiBN8kt','PHO', NOW()),
	('xkkk7tzT','PHO', NOW()),
	('DH1527J9','PHO', NOW()),
	('RLvlP1Sp','TAB', NOW()),
	('nopGICOT','TAB', NOW()),
	('Bj4952Gg','PHO', NOW()),
	('8SL4oA6X','WAT', NOW());

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
	
	/* fake 'test' personnel */
	(8,	'Med', '', 'Test 1', 'MED'),
	(9,	'Med', '', 'Test 2', 'MED'),
	(10,'Med', '', 'Test 3', 'MED'),
	(11,'Med', '', 'Test 4', 'MED'),
	(12,'Med', '', 'Test 5', 'MED'),
	
	(13,'Sec', '', 'Test 1', 'SEC'),
	(14,'Sec', '', 'Test 2', 'SEC'),
	(15,'Sec', '', 'Test 3', 'SEC'),
	(16,'Sec', '', 'Test 4', 'SEC'),
	(17,'Sec', '', 'Test 5', 'SEC');
	
/*=========================================================== 
  Node	
  -----------------------------------------------------------*/
INSERT INTO
	Nodes (hash, latitude, longitude, lastUpdate)
VALUES 
	('nmkhFNT6', -34.914850, 138.578958, NOW()),
	('AZN9SEhT', -34.917093, 138.580020, NOW()),
	('4t8GKzPB', -34.916838, 138.582359, NOW()),
	('ScJQCjOm', -34.918008, 138.583196, NOW()),
	('km8rsPfh', -34.917815, 138.579226, NOW()),
	('hMFoVTyg', -34.916126, 138.581028, NOW());
	
/*=========================================================== 
  Incident	
  -----------------------------------------------------------*/
INSERT INTO
	Incidents (id, incidentType, latitude, longitude, created)
VALUES 
	(1, 'SEC', -34.914850, 138.578958, NOW()),
	(2, 'MED', -34.917093, 138.580020, NOW());



