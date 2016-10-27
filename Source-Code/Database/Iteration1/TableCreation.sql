CREATE TABLE `users` (
  `userID` BIGINT NOT NULL AUTO_INCREMENT,
  `userName` VARCHAR(20) NOT NULL,
  `firstName` VARCHAR(25) NOT NULL,
  `lastName` VARCHAR(25) NOT NULL,
  PRIMARY KEY (`userID`));

  CREATE TABLE `eventtable` (
  `eventID` BIGINT NOT NULL AUTO_INCREMENT,
  `userID` BIGINT NOT NULL,
  `latitude` DOUBLE NOT NULL,
  `longitude` DOUBLE NOT NULL,
  `eventName` VARCHAR(255) NOT NULL,
  `startTime` DATETIME NOT NULL,
  `endTime` DATETIME NOT NULL,
  `description` VARCHAR(1024) NULL,
  PRIMARY KEY (`eventID`),
  INDEX `userID_idx` (`userID` ASC),
  CONSTRAINT `userID`
    FOREIGN KEY (`userID`)
    REFERENCES `users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
