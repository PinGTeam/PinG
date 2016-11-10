USE `ping`;
DROP procedure IF EXISTS `GetEvents3`;

DELIMITER $$
USE `ping`$$
CREATE PROCEDURE `GetEvents3`(paramLatitude double,  paramLongitude double)
BEGIN

DECLARE distMiles INT;
DECLARE lon2miles DOUBLE;
DECLARE lat2miles DOUBLE;
DECLARE topLatitude DOUBLE;
DECLARE topLongitude DOUBLE;
DECLARE bottomLatitude DOUBLE;
DECLARE bottomLongitude DOUBLE;

SET distMiles = 25;
SET lon2miles = abs(cos(radians(paramLatitude))*69);
SET lat2miles = 69;
SET topLongitude = paramLongitude-distMiles/lon2miles;
SET topLatitude = paramLatitude+distMiles/lat2miles;
SET bottomLongitude = paramLongitude+distMiles/lon2miles;
SET bottomLatitude = paramLatitude-distMiles/lat2miles;

SELECT
longitude, latitude, eventtable.userID, eventID, firstName, lastName, eventName, startTime, endTime, description
FROM
eventtable
inner join
users
on
eventtable.userid = users.userid
where
(endTime > NOW()) AND

(latitude <= topLatitude AND
        latitude >= bottomLatitude) AND
        (
        CASE
WHEN(
(bottomLongitude <= 180.0000 AND bottomLongitude > 0.0000)
AND
(topLongitude > -180.0000 AND topLongitude < 0.0000) AND
            ((longitude <= topLongitude AND longitude > -180.0000 ) OR
            (longitude <= 180.0000 and longitude >= bottomLongitude))) THEN 1
WHEN
  ((bottomLongitude > topLongitude) AND
  (longitude >= topLongitude AND longitude <= bottomLongitude)) then 1
ELSE
0
END) = 1;
END$$

DELIMITER ;
