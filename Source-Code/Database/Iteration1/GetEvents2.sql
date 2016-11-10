USE `ping`;
DROP procedure IF EXISTS `GetEvents2`;

DELIMITER $$
USE `ping`$$
CREATE PROCEDURE `GetEvents2`(paramLatitude double,  paramLongitude double)
BEGIN

/*
#DECLARE distMiles INT;
#DECLARE lon2miles INT;
#DECLARE lat2miles INT;
DECLARE rlon1 DOUBLE;
DECLARE rlat1 DOUBLE;
DECLARE rlon2 DOUBLE;
DECLARE rlat2 DOUBLE;
SET distMiles = 25;
SET lon2miles = abs(cos(radians(paramLatitude))*69);
SET lat2miles = 69;
SET rlon1 = paramLongitude-distMiles/lon2miles;
SET rlat1 = paramLatitude-distMiles/lat2miles;
SET rlon1 = paramLongitude+distMiles/lon2miles;
SET rlat1 = paramLatitude+distMiles/lat2miles;
SELECT
st_astext(g)
FROM
geom
where
st_within(g,st_envelope(linestring(
point(rlon1, rlat1),
point(rlon2, rlat2))));
END$$
*/

SELECT
st_astext(g)
FROM
geom
where
st_within(g,st_envelope(linestring(
point(paramLongitude-25/abs(cos(radians(paramLatitude))*69), paramLatitude-25/69),
point(paramLongitude+25/abs(cos(radians(paramLatitude))*69), paramLatitude+25/69))));
END$$

DELIMITER ;
