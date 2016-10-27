USE `ping`;

DELIMITER $$

DROP TRIGGER IF EXISTS eventtable_BEFORE_INSERT$$
USE `ping`$$
CREATE TRIGGER `eventtable_BEFORE_INSERT` BEFORE INSERT ON `eventtable` FOR EACH ROW
BEGIN
  IF NEW.latitude <-90 THEN
    SET NEW.latitude = -90;
  ELSEIF NEW.latitude > 90 THEN
    SET NEW.latitude = 90;
  END IF;

    IF NEW.longitude <= -180 OR NEW.longitude > 180 THEN
    SET NEW.longitude = 180;

    END IF;
END$$
DELIMITER ;
USE `ping`;

DELIMITER $$

DROP TRIGGER IF EXISTS eventtable_BEFORE_UPDATE$$
USE `ping`$$
CREATE TRIGGER `eventtable_BEFORE_UPDATE` BEFORE UPDATE ON `eventtable` FOR EACH ROW
BEGIN
  IF NEW.latitude <-90 THEN
    SET NEW.latitude = -90;
  ELSEIF NEW.latitude > 90 THEN
    SET NEW.latitude = 90;
  END IF;

    IF NEW.longitude <= -180 OR NEW.longitude > 180 THEN
    SET NEW.longitude = 180;

    END IF;
END$$
DELIMITER ;
