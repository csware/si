DROP TABLE IF EXISTS `groups`;
CREATE TABLE  `groups` (
  `gid` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `lectureid` int(11) NOT NULL,
  PRIMARY KEY (`gid`),
  KEY `FKB63DD9D4AF18EDD1` (`lectureid`),
  CONSTRAINT `FKB63DD9D4AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `lectures`;
CREATE TABLE  `lectures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `semester` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `participations`;
CREATE TABLE  `participations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role` varchar(255) NOT NULL,
  `groupid` int(11) DEFAULT NULL,
  `lectureid` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `lectureid` (`lectureid`,`uid`),
  KEY `FKA301B52E28A1D21` (`uid`),
  KEY `FKA301B527F3A8A13` (`groupid`),
  KEY `FKA301B52AF18EDD1` (`lectureid`),
  CONSTRAINT `FKA301B527F3A8A13` FOREIGN KEY (`groupid`) REFERENCES `groups` (`gid`),
  CONSTRAINT `FKA301B52AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKA301B52E28A1D21` FOREIGN KEY (`uid`) REFERENCES `users` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `similarities`;
CREATE TABLE  `similarities` (
  `similarityid` int(11) NOT NULL AUTO_INCREMENT,
  `percentage` int(11) NOT NULL,
  `similarityTest_similarityTestId` int(11) NOT NULL,
  `submissionOne_submissionid` int(11) NOT NULL,
  `submissionTwo_submissionid` int(11) NOT NULL,
  PRIMARY KEY (`similarityid`),
  KEY `FKB31AC117AAD798` (`submissionTwo_submissionid`),
  KEY `FKB31AC193B8B275` (`similarityTest_similarityTestId`),
  KEY `FKB31AC1B470E5BE` (`submissionOne_submissionid`),
  CONSTRAINT `FKB31AC117AAD798` FOREIGN KEY (`submissionTwo_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE,
  CONSTRAINT `FKB31AC193B8B275` FOREIGN KEY (`similarityTest_similarityTestId`) REFERENCES `similaritytests` (`similarityTestId`) ON DELETE CASCADE,
  CONSTRAINT `FKB31AC1B470E5BE` FOREIGN KEY (`submissionOne_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=164348 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `similaritytests`;
CREATE TABLE  `similaritytests` (
  `similarityTestId` int(11) NOT NULL AUTO_INCREMENT,
  `taskid` int(11) NOT NULL,
  `basis` varchar(255) NOT NULL,
  `needsToRun` bit(1) NOT NULL,
  `normalizeCapitalization` bit(1) NOT NULL,
  `tabsSpacesNewlinesNormalization` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `minimumDifferenceInPercent` int(11) NOT NULL,
  `excludeFiles` text NOT NULL,
  PRIMARY KEY (`similarityTestId`),
  KEY `FK86B2AD1EAE0697EB` (`taskid`),
  CONSTRAINT `FK86B2AD1EAE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=165 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `submissions`;
CREATE TABLE  `submissions` (
  `submissionid` int(11) NOT NULL AUTO_INCREMENT,
  `compiles` bit(1) DEFAULT NULL,
  `points` int(11) DEFAULT NULL,
  `stderr` longtext,
  `passedTest` bit(1) DEFAULT NULL,
  `testOutput` longtext,
  `issuedBy_id` int(11) DEFAULT NULL,
  `submitter` int(11) NOT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`submissionid`),
  UNIQUE KEY `submitter` (`submitter`,`taskid`),
  KEY `FK2912EA7F27BD004` (`submitter`),
  KEY `FK2912EA7AE0697EB` (`taskid`),
  KEY `issuedby` (`issuedBy_id`),
  CONSTRAINT `FK2912EA7AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE,
  CONSTRAINT `FK2912EA7F27BD004` FOREIGN KEY (`submitter`) REFERENCES `participations` (`id`),
  CONSTRAINT `issuedby` FOREIGN KEY (`issuedBy_id`) REFERENCES `participations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `tasks`;
CREATE TABLE  `tasks` (
  `taskid` int(11) NOT NULL AUTO_INCREMENT,
  `deadline` datetime NOT NULL,
  `description` longtext,
  `maxPoints` int(11) NOT NULL,
  `showPoints` datetime DEFAULT NULL,
  `start` datetime NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `lectureid` int(11) NOT NULL,
  PRIMARY KEY (`taskid`),
  KEY `FK6907B8EAF18EDD1` (`lectureid`),
  CONSTRAINT `FK6907B8EAF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `test`;
CREATE TABLE  `test` (
  `DTYPE` varchar(31) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `visibleToStudents` bit(1) NOT NULL,
  `commandLineParameter` varchar(255) DEFAULT NULL,
  `mainClass` varchar(255) DEFAULT NULL,
  `regularExpression` varchar(255) DEFAULT NULL,
  `taskid` int(11) NOT NULL,
  `timeout` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `taskid` (`taskid`),
  KEY `FK364492AE0697EB` (`taskid`),
  CONSTRAINT `FK364492AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `users`;
CREATE TABLE  `users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `firstName` varchar(255) NOT NULL,
  `lastName` varchar(255) NOT NULL,
  `superUser` bit(1) NOT NULL,
  `matrikelno` int(11) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=latin1;
