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
	Device (hash, deviceType)
VALUES 
	('TYRZz3u0','PHO'),
	('AbUNZ0xl','PHO'),
	('WuQkhzZB','PHO'),
	('NsiBN8kt','PHO'),
	('xkkk7tzT','PHO'),
	('DH1527J9','PHO'),
	('RLvlP1Sp','PHO'),
	('nopGICOT','PHO'),
	('Bj4952Gg','PHO'),
	('8SL4oA6X','PHO');

/*=========================================================== 
  User	
  -----------------------------------------------------------*/
INSERT INTO
	`User` (userID, nameFirst, nameMiddle, nameLast, personnelType)
VALUES 
	(1,'Mark', 'Stephen', 'Gillard', 'WFU'),
	(2,'Hussein', '', 'Al Hammad', 'WFU'),
	(3,'Mitchell', '', 'Templeton', 'WFU'),
	(4,'Ciaran', '', 'Dodson', 'MED'),
	(5,'Conal', '', 'Melendez', 'MED'),
	(6,'Abar', '', 'Blevins', 'MED'),
	(7,'Roman', '', 'Camacho', 'MED'),
	(8,'Oscar', '', 'Nicholson', 'MED'),
	(9,'Ramsay', '', 'Wilkerson', 'SEC'),
	(10,'Samuel', '', 'Henry', 'SEC'),
	(11,'Sandy', '', 'Schultz', 'SEC'),
	(12,'Ame', '', 'Raymond', 'SEC'),
	(13,'Syed', '', 'Atkins', 'SEC');
	
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
	
	




