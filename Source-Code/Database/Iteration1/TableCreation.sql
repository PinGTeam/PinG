CREATE TABLE `users` (
  `userID` BIGINT NOT NULL AUTO_INCREMENT,
  `userName` VARCHAR(20) NOT NULL,
  `password` VARCHAR(60) NOT NULL,
  `firstName` VARCHAR(25) NOT NULL,
  `lastName` VARCHAR(25) NOT NULL,
  `email` VARCHAR(320) NOT NULL,

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
/*
CREATE TABLE `requesttable` (
  `userID_sender` BIGINT NOT NULL,
  `userID_receiver` BIGINT NOT NULL,
  `status` ENUM(`accepted`,`rejected`,`pending`) NOT NULL,
  PRIMARY KEY (`userID_sender`,`userID_receiver`),
  CONSTRAINT `userID_sender`
    FOREIGN KEY (`userID_sender`)
    REFERENCES `users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `userID_receiver`
    FOREIGN KEY (`userID_receiver`)
    REFERENCES `users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

CREATE TABLE `friendshiptable` (
  `userID_1` BIGINT NOT NULL,
  `userID_2` BIGINT NOT NULL,
  PRIMARY KEY (`userID_1`,`userID_2`),
  CONSTRAINT `userID_1`
    FOREIGN KEY (`userID_sender`)
    REFERENCES `users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `userID_2`
    FOREIGN KEY (`userID_receiver`)
    REFERENCES `users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
*/
CREATE TABLE `attendancetable` (
  `eventID` BIGINT NOT NULL,
  `userID` BIGINT NOT NULL,
  PRIMARY KEY (`userID`,`eventID`),
  INDEX `userID_idx` (`userID` ASC),
  CONSTRAINT `userID_att`
    FOREIGN KEY (`userID`)
    REFERENCES `users` (`userID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `eventID_att`
    FOREIGN KEY (`eventID`)
    REFERENCES `eventtable` (`eventID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
