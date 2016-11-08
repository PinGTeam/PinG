USE `test_middle`;
DROP procedure IF EXISTS `InsertUser`;

DELIMITER $$
USE `test_middle`$$
CREATE PROCEDURE `InsertUser`(parmUserName varchar(20), parmPassword varchar(60), parmFirstName varchar(25), parmLastName varchar(25), parmEmail varchar(320))
BEGIN
    IF exists(SELECT * FROM users where email = parmEmail) AND exists(SELECT * FROM users where userName = parmUserName) THEN
      SELECT -3;
    ELSEIF exists(SELECT * FROM users where userName = parmUserName) THEN
      SELECT -2;
    ELSEIF exists(SELECT * FROM users where email = parmEmail) THEN
      SELECT -1;
    ELSE
      insert into users (userName, password, firstName, lastName, email)
      VALUES (parmUserName,parmPassword, parmFirstName, parmLastName, parmEmail);
      SELECT 1;
    END IF;

END$$

DELIMITER ;
