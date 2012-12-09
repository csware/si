SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `submissionsystem`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `groups`
--

DROP TABLE IF EXISTS `groups`;
CREATE TABLE IF NOT EXISTS `groups` (
  `gid` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET latin1 COLLATE latin1_german2_ci DEFAULT NULL,
  `lectureid` int(11) NOT NULL,
  `allowStudentsToSignup` bit(1) NOT NULL,
  `allowStudentsToQuit` bit(1) NOT NULL,
  `submissionGroup` bit(1) NOT NULL,
  `maxStudents` int(11) unsigned NOT NULL DEFAULT '20',
  PRIMARY KEY (`gid`),
  KEY `FKB63DD9D4AF18EDD1` (`lectureid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `groups_tutors`
--

DROP TABLE IF EXISTS `groups_tutors`;
CREATE TABLE IF NOT EXISTS `groups_tutors` (
  `groups_gid` int(11) NOT NULL,
  `tutors_id` int(11) NOT NULL,
  PRIMARY KEY (`groups_gid`,`tutors_id`),
  KEY `FK8EAE7CC8BB3EB910` (`groups_gid`),
  KEY `FK8EAE7CC842D82B98` (`tutors_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `lectures`
--

DROP TABLE IF EXISTS `lectures`;
CREATE TABLE IF NOT EXISTS `lectures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET latin1 COLLATE latin1_german2_ci DEFAULT NULL,
  `semester` int(11) NOT NULL,
  `requiresAbhnahme` bit(1) NOT NULL,
  `gradingMethod` varchar(25) CHARACTER SET latin1 COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `logs`
--

DROP TABLE IF EXISTS `logs`;
CREATE TABLE IF NOT EXISTS `logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action` int(11) NOT NULL,
  `result` bit(1) DEFAULT NULL,
  `testOutput` text,
  `timeStamp` datetime DEFAULT NULL,
  `taskId` int(11) NOT NULL,
  `testId` int(11) DEFAULT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`),
  KEY `testId` (`testId`),
  KEY `taskId` (`taskId`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `participations`
--

DROP TABLE IF EXISTS `participations`;
CREATE TABLE IF NOT EXISTS `participations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role` varchar(255) NOT NULL,
  `groupid` int(11) DEFAULT NULL,
  `lectureid` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `lectureid` (`lectureid`,`uid`),
  KEY `FKA301B52E28A1D21` (`uid`),
  KEY `FKA301B527F3A8A13` (`groupid`),
  KEY `FKA301B52AF18EDD1` (`lectureid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `pointcategories`
--

DROP TABLE IF EXISTS `pointcategories`;
CREATE TABLE IF NOT EXISTS `pointcategories` (
  `pointcatid` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `optional` bit(1) NOT NULL,
  `points` int(11) NOT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`pointcatid`),
  KEY `FK2623E7ACAE0697EB` (`taskid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `pointgiven`
--

DROP TABLE IF EXISTS `pointgiven`;
CREATE TABLE IF NOT EXISTS `pointgiven` (
  `pointgivenid` int(11) NOT NULL AUTO_INCREMENT,
  `points` int(11) NOT NULL,
  `categoryid` int(11) NOT NULL,
  `submissionid` int(11) NOT NULL,
  PRIMARY KEY (`pointgivenid`),
  KEY `FK4BE59BED638E4301` (`categoryid`),
  KEY `FK4BE59BED39FBF139` (`submissionid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `pointhistory`
--

DROP TABLE IF EXISTS `pointhistory`;
CREATE TABLE IF NOT EXISTS `pointhistory` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `added` longtext NOT NULL,
  `date` datetime NOT NULL,
  `field` varchar(255) NOT NULL,
  `removed` longtext NOT NULL,
  `submission_submissionid` int(11) NOT NULL,
  `who_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1DB12D04AEB18C37` (`who_id`),
  KEY `FK1DB12D046B74DB4C` (`submission_submissionid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `similarities`
--

DROP TABLE IF EXISTS `similarities`;
CREATE TABLE IF NOT EXISTS `similarities` (
  `similarityid` int(11) NOT NULL AUTO_INCREMENT,
  `percentage` int(11) NOT NULL,
  `similarityTest_similarityTestId` int(11) NOT NULL,
  `submissionOne_submissionid` int(11) NOT NULL,
  `submissionTwo_submissionid` int(11) NOT NULL,
  PRIMARY KEY (`similarityid`),
  KEY `FKB31AC117AAD798` (`submissionTwo_submissionid`),
  KEY `FKB31AC193B8B275` (`similarityTest_similarityTestId`),
  KEY `FKB31AC1B470E5BE` (`submissionOne_submissionid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `similaritytests`
--

DROP TABLE IF EXISTS `similaritytests`;
CREATE TABLE IF NOT EXISTS `similaritytests` (
  `similarityTestId` int(11) NOT NULL AUTO_INCREMENT,
  `basis` varchar(255) NOT NULL,
  `excludeFiles` varchar(255) DEFAULT NULL,
  `minimumDifferenceInPercent` int(11) NOT NULL,
  `status` tinyint(1) NOT NULL,
  `normalizeCapitalization` bit(1) NOT NULL,
  `tabsSpacesNewlinesNormalization` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`similarityTestId`),
  KEY `FK86B2AD1EAE0697EB` (`taskid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions`
--

DROP TABLE IF EXISTS `submissions`;
CREATE TABLE IF NOT EXISTS `submissions` (
  `submissionid` int(11) NOT NULL AUTO_INCREMENT,
  `points` int(11) DEFAULT NULL,
  `issuedBy_id` int(11) DEFAULT NULL,
  `taskid` int(11) NOT NULL,
  `publicComment` longtext,
  `pointStatus` tinyint(1) unsigned DEFAULT NULL,
  `duplicate` int(11) DEFAULT NULL,
  `internalComment` longtext,
  `lastModified` datetime DEFAULT NULL,
  PRIMARY KEY (`submissionid`),
  KEY `FK2912EA7AE0697EB` (`taskid`),
  KEY `issuedby` (`issuedBy_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions_participations`
--

DROP TABLE IF EXISTS `submissions_participations`;
CREATE TABLE IF NOT EXISTS `submissions_participations` (
  `submitters_id` int(11) NOT NULL,
  `submissions_submissionid` int(11) NOT NULL,
  PRIMARY KEY (`submissions_submissionid`,`submitters_id`),
  KEY `FK27F157EA16D3DBEB` (`submitters_id`),
  KEY `FK27F157EA5F9373D1` (`submissions_submissionid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions_results`
--

DROP TABLE IF EXISTS `submissions_results`;
CREATE TABLE IF NOT EXISTS `submissions_results` (
  `resultid` int(11) NOT NULL AUTO_INCREMENT,
  `submissionid` int(11) NOT NULL,
  `result` varchar(255) NOT NULL,
  PRIMARY KEY (`resultid`),
  KEY `submissionid` (`submissionid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions_tasknumbers`
--

DROP TABLE IF EXISTS `submissions_tasknumbers`;
CREATE TABLE IF NOT EXISTS `submissions_tasknumbers` (
  `tasknumberid` int(11) NOT NULL AUTO_INCREMENT,
  `taskid` int(11) NOT NULL,
  `participationid` int(11) NOT NULL,
  `submissionid` int(11) DEFAULT NULL,
  `number` varchar(255) NOT NULL,
  `origNumber` varchar(255) NOT NULL,
  PRIMARY KEY (`tasknumberid`),
  KEY `taskid` (`taskid`),
  KEY `submissionid` (`submissionid`),
  KEY `participationid` (`participationid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `taskgroups`
--

DROP TABLE IF EXISTS `taskgroups`;
CREATE TABLE IF NOT EXISTS `taskgroups` (
  `taskgroupid` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `lectureid` int(11) NOT NULL,
  PRIMARY KEY (`taskgroupid`),
  KEY `FK5BD51799AF18EDD1` (`lectureid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tasks`
--

DROP TABLE IF EXISTS `tasks`;
CREATE TABLE IF NOT EXISTS `tasks` (
  `taskid` int(11) NOT NULL AUTO_INCREMENT,
  `deadline` datetime NOT NULL,
  `description` longtext,
  `filenameRegexp` varchar(255) DEFAULT NULL,
  `archiveFilenameRegexp` varchar(255) DEFAULT NULL,
  `maxPoints` int(11) NOT NULL,
  `minPointStep` int(11) NOT NULL,
  `maxSubmitters` int(11) NOT NULL,
  `allowSubmittersAcrossGroups` bit(1) NOT NULL,
  `showPoints` datetime DEFAULT NULL,
  `showTextArea` bit(1) NOT NULL,
  `start` datetime NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `taskgroupid` int(11) NOT NULL,
  `featuredFiles` text NOT NULL,
  `tutorsCanUploadFiles` bit(1) NOT NULL,
  `dynamicTask` varchar(255) DEFAULT NULL,
  `maxsize` int(11) NOT NULL DEFAULT '10485760',
  PRIMARY KEY (`taskid`),
  KEY `taskgroupid` (`taskgroupid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `testresults`
--

DROP TABLE IF EXISTS `testresults`;
CREATE TABLE IF NOT EXISTS `testresults` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `passedTest` bit(1) DEFAULT NULL,
  `testOutput` longtext,
  `submission_submissionid` int(11) DEFAULT NULL,
  `test_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC6CC0F246B74DB4C` (`submission_submissionid`),
  KEY `FKC6CC0F248DBEBD80` (`test_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tests`
--

DROP TABLE IF EXISTS `tests`;
CREATE TABLE IF NOT EXISTS `tests` (
  `DTYPE` varchar(31) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `forTutors` bit(1) NOT NULL,
  `needsToRun` bit(1) NOT NULL,
  `testDescription` varchar(255) DEFAULT NULL,
  `testTitle` varchar(255) DEFAULT NULL,
  `timeout` int(11) NOT NULL,
  `timesRunnableByStudents` int(11) NOT NULL,
  `commandLineParameter` varchar(255) DEFAULT NULL,
  `mainClass` varchar(255) DEFAULT NULL,
  `regularExpression` varchar(255) DEFAULT NULL,
  `taskid` int(11) NOT NULL,
  `minProzent` int(11) DEFAULT NULL,
  `excludedFiles` varchar(255) DEFAULT NULL,
  `giveDetailsToStudents` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6924E21AE0697EB` (`taskid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `testscounts`
--

DROP TABLE IF EXISTS `testscounts`;
CREATE TABLE IF NOT EXISTS `testscounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timesExecuted` int(11) NOT NULL,
  `test_id` int(11) DEFAULT NULL,
  `user_uid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKF81042A5D2AB5AAD` (`user_uid`),
  KEY `FKF81042A58DBEBD80` (`test_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `firstName` varchar(255) CHARACTER SET latin1 COLLATE latin1_german2_ci NOT NULL,
  `lastName` varchar(255) CHARACTER SET latin1 COLLATE latin1_german2_ci NOT NULL,
  `superUser` bit(1) NOT NULL,
  `matrikelno` int(11) DEFAULT NULL,
  `studiengang` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `groups`
--
ALTER TABLE `groups`
  ADD CONSTRAINT `FKB63DD9D4AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `groups_tutors`
--
ALTER TABLE `groups_tutors`
  ADD CONSTRAINT `groups_tutors_ibfk_1` FOREIGN KEY (`groups_gid`) REFERENCES `groups` (`gid`) ON DELETE CASCADE,
  ADD CONSTRAINT `groups_tutors_ibfk_2` FOREIGN KEY (`tutors_id`) REFERENCES `participations` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `logs`
--
ALTER TABLE `logs`
  ADD CONSTRAINT `logs_ibfk_1` FOREIGN KEY (`taskId`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE,
  ADD CONSTRAINT `logs_ibfk_2` FOREIGN KEY (`testId`) REFERENCES `tests` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `logs_ibfk_3` FOREIGN KEY (`userId`) REFERENCES `users` (`uid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `participations`
--
ALTER TABLE `participations`
  ADD CONSTRAINT `FKA301B527F3A8A13` FOREIGN KEY (`groupid`) REFERENCES `groups` (`gid`),
  ADD CONSTRAINT `FKA301B52AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FKA301B52E28A1D21` FOREIGN KEY (`uid`) REFERENCES `users` (`uid`);

--
-- Constraints der Tabelle `pointcategories`
--
ALTER TABLE `pointcategories`
  ADD CONSTRAINT `FK2623E7ACAE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `pointgiven`
--
ALTER TABLE `pointgiven`
  ADD CONSTRAINT `FK4BE59BED39FBF139` FOREIGN KEY (`submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK4BE59BED638E4301` FOREIGN KEY (`categoryid`) REFERENCES `pointcategories` (`pointcatid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `pointhistory`
--
ALTER TABLE `pointhistory`
  ADD CONSTRAINT `FK1DB12D04AEB18C37` FOREIGN KEY (`who_id`) REFERENCES `participations` (`id`),
  ADD CONSTRAINT `pointhistory_ibfk_1` FOREIGN KEY (`submission_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `similarities`
--
ALTER TABLE `similarities`
  ADD CONSTRAINT `FKB31AC117AAD798` FOREIGN KEY (`submissionTwo_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE,
  ADD CONSTRAINT `FKB31AC193B8B275` FOREIGN KEY (`similarityTest_similarityTestId`) REFERENCES `similaritytests` (`similarityTestId`) ON DELETE CASCADE,
  ADD CONSTRAINT `FKB31AC1B470E5BE` FOREIGN KEY (`submissionOne_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `similaritytests`
--
ALTER TABLE `similaritytests`
  ADD CONSTRAINT `FK86B2AD1EAE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `submissions`
--
ALTER TABLE `submissions`
  ADD CONSTRAINT `FK2912EA7AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE,
  ADD CONSTRAINT `issuedby` FOREIGN KEY (`issuedBy_id`) REFERENCES `participations` (`id`);

--
-- Constraints der Tabelle `submissions_participations`
--
ALTER TABLE `submissions_participations`
  ADD CONSTRAINT `FK27F157EA16D3DBEB` FOREIGN KEY (`submitters_id`) REFERENCES `participations` (`id`),
  ADD CONSTRAINT `FK27F157EA5F9373D1` FOREIGN KEY (`submissions_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `submissions_results`
--

ALTER TABLE `submissions_results`
  ADD FOREIGN KEY (`submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `submissions_tasknumbers`
--

ALTER TABLE `submissions_tasknumbers`
  ADD FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE,
  ADD FOREIGN KEY (`participationid`) REFERENCES `participations` (`id`),
  ADD FOREIGN KEY (`submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE SET NULL;

--
-- Constraints der Tabelle `taskgroups`
--
ALTER TABLE `taskgroups`
  ADD CONSTRAINT `FK5BD51799AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `tasks`
--
ALTER TABLE `tasks`
  ADD CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`taskgroupid`) REFERENCES `taskgroups` (`taskgroupid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `testresults`
--
ALTER TABLE `testresults`
  ADD CONSTRAINT `FKC6CC0F246B74DB4C` FOREIGN KEY (`submission_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE,
  ADD CONSTRAINT `FKC6CC0F248DBEBD80` FOREIGN KEY (`test_id`) REFERENCES `tests` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `tests`
--
ALTER TABLE `tests`
  ADD CONSTRAINT `tests_ibfk_1` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `testscounts`
--
ALTER TABLE `testscounts`
  ADD CONSTRAINT `testscounts_ibfk_1` FOREIGN KEY (`test_id`) REFERENCES `tests` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `testscounts_ibfk_2` FOREIGN KEY (`user_uid`) REFERENCES `users` (`uid`) ON DELETE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
