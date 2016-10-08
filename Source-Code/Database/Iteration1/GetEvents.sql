CREATE DEFINER=`richard`@`localhost` PROCEDURE `GetEvents`(topLatitude double,  topLongitude double,  bottomLatitude double,  bottomLongitude double)
BEGIN

	SELECT 
		eventID, FirstName, LastName, latitude, longitude, eventName, time
	FROM 
		eventtable
	inner join 
		users
	on
		eventtable.userid = users.userid
	where
		(latitude <= topLatitude AND
        latitude >= bottomLatitude) AND
        (
        CASE
			WHEN 
			((bottomLongitude <= 180.0000 AND bottomLongitude > 0.0000) AND 
            (topLongitude > -180.0000 AND topLongitude < 0.0000) AND
            (longitude <= topLongitude AND longitude > -180.0000) AND
            (longitude <= 180.0000 and longitude >= bottomLongitude)) THEN 1
			WHEN
			((bottomLongitude <= 180.0000  and topLongitude <= 180.0000) AND
            (longitude >= topLongitude AND longitude <= bottomLongitude)) then 1
            ELSE
				0
		END) = 1;
END
