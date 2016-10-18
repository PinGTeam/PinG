USE `ping`;
DROP procedure IF EXISTS `InsertUser`;

DELIMITER $$
CREATE PROCEDURE `InsertUser`(parmUserName varchar(20), parmFirstName varchar(25), parmLastName varchar(25))
BEGIN
		IF NOT exists(SELECT * FROM users where userName = parmUserName) THEN
        	insert into users
				(userName, firstName, lastName)
			VALUES
				(parmUserName, parmFirstName, parmLastName);
        
        END IF;
        
        SELECT 
			userID
        FROM
			users 
		where 
			userName = parmUserName;  

END$$

DELIMITER ;

