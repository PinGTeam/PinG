USE `ping`;
DROP procedure IF EXISTS `InsertUser`;

DELIMITER $$
USE `ping`$$
CREATE PROCEDURE `InsertUser`(parmUserName varchar(20), parmPassword varchar(60), parmFirstName varchar(25), parmLastName varchar(25), parmEmail varchar(256))
BEGIN
		IF NOT exists(SELECT * FROM users where userName = parmUserName OR email = parmEmail) THEN
        	insert into users
				(userName, password, firstName, lastName, email)
			VALUES
				(parmUserName,parmPassword, parmFirstName, parmLastName, parmEmail);

        END IF;

        SELECT
			userID
        FROM
			users
		where
			userName = parmUserName;

END$$

DELIMITER ;
