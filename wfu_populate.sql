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
	Device (hash, deviceType, lastUpdate)
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
	`User` (userID, nameFirst, nameMiddle, nameLast, personnelType)
VALUES 
	/* wfu personnel */
	(1,'Mark', 'Stephen', 'Gillard', 'WFU'),
	(2,'Hussein', '', 'Al Hammad', 'WFU'),
	(3,'Mitchell', '', 'Templeton', 'WFU'),
	(4,'Greg', '', 'Stevens', 'WFU'),
	(5,'Mateus', '', 'Nolasco', 'WFU'),
	(6,'Ben', '', 'Quast', 'WFU'),
	(7,'Travis', '', 'Grund', 'WFU'),
	
	/* fake 'test' personnel */
	(8,'Ciaran', '', 'Dodson', 'MED'),
	(9,'Conal', '', 'Melendez', 'MED'),
	(10,'Abar', '', 'Blevins', 'MED'),
	(11,'Roman', '', 'Camacho', 'MED'),
	(12,'Oscar', '', 'Nicholson', 'MED'),
	
	(13,'Ramsay', '', 'Wilkerson', 'SEC'),
	(14,'Samuel', '', 'Henry', 'SEC'),
	(15,'Sandy', '', 'Schultz', 'SEC'),
	(16,'Ame', '', 'Raymond', 'SEC'),
	(17,'Syed', '', 'Atkins', 'SEC');
	
/*=========================================================== 
  Node	
  -----------------------------------------------------------*/
INSERT INTO
	Node (hash, latitude, longitude, lastUpdate)
VALUES 
	('nmkhFNT6', -34.914850, 138.578958, NOW()),
	('AZN9SEhT', -34.917093, 138.580020, NOW()),
	('4t8GKzPB', -34.916838, 138.582359, NOW()),
	('ScJQCjOm', -34.918008, 138.583196, NOW()),
	('km8rsPfh', -34.917815, 138.579226, NOW()),
	('hMFoVTyg', -34.916126, 138.581028, NOW());
	
	




