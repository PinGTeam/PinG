USE `test_middle`;
DROP procedure IF EXISTS `InsertEvent`;

DELIMITER $$
USE `test_middle`$$
CREATE PROCEDURE `InsertEvent` (
  parmUserID bigint, parmEventName varchar(255), parmLatitude double, parmLongitude double,
    parmWhenStart datetime, parmWhenEnd datetime, parmDescription varchar(1024))
BEGIN
  INSERT INTO eventtable
    (userID, latitude, longitude, eventName, startTime, endTime, description)
  VALUES
    (parmUserId, parmLatitude, parmLongitude, parmEventName, parmWhenStart, parmWhenEnd, parmDescription);

END$$

DELIMITER ;
