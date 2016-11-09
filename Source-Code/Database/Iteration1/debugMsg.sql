USE `ping`;
DROP procedure IF EXISTS `debug_msg`;


DELIMITER $$
USE `ping`$$
CREATE PROCEDURE debug_msg(enabled INTEGER, msg VARCHAR(255))
BEGIN
  IF enabled THEN BEGIN
    select concat("** ", msg) AS '** DEBUG:';
  END; END IF;
END $$
DELIMITER;
