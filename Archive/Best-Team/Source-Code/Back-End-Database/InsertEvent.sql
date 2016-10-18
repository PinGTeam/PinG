DROP procedure IF EXISTS `InsertEvent`;

DELIMITER $$
USE `ping`$$
CREATE PROCEDURE `InsertEvent` (
parmUserID bigint, parmEventName varchar(255), parmLatitude double, parmLongitude double, 
parmWhen datetime, parmDescription varchar(1024))
BEGIN
	INSERT INTO eventtable
	(userID, latitude, longitude, eventName, time, description)
	VALUES
	(parmUserId, parmLatitude, parmLongitude, parmEventName, parmWhen, parmDescription);

END$$
DELIMITER ;
