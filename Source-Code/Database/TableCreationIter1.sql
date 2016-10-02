CREATE TABLE `sirotto_db`.`users` (
  `userID` BIGINT NOT NULL AUTO_INCREMENT,
  `userName` VARCHAR(20) NOT NULL,
  `firstName` VARCHAR(25) NOT NULL,
  `lastName` VARCHAR(25) NOT NULL,
  PRIMARY KEY (`userID`));
  
  CREATE TABLE `sirotto_db`.`eventtable` (
  `eventID` BIGINT NOT NULL AUTO_INCREMENT,
  `userID` BIGINT NULL,
  `latitude` DOUBLE NULL,
  `longitude` DOUBLE NULL,
  `eventName` VARCHAR(255) NULL,
  `time` DATETIME NULL,
  `description` VARCHAR(1024) NULL,
  PRIMARY KEY (`eventID`),
  INDEX `userID_idx` (`userID` ASC),
  CONSTRAINT `userID`
    FOREIGN KEY (`userID`)
    REFERENCES `sirotto`.`users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
