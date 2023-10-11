SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `submissionsystem`
--

-- create database submissionsystem CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;

-- --------------------------------------------------------
-- Tabellenstruktur für Tabelle `checklisttestcheckitem`
--

DROP TABLE IF EXISTS `checklisttestcheckitem`;
CREATE TABLE `checklisttestcheckitem` (
  `checkitemid` int(11) NOT NULL AUTO_INCREMENT,
  `correct` bit(1) NOT NULL,
  `feedback` longtext NOT NULL,
  `title` longtext NOT NULL,
  `testid` int(11) NOT NULL,
  PRIMARY KEY (`checkitemid`),
  KEY `FK81pc66uelq448v1na0u5ryetf` (`testid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------
-- Tabellenstruktur für Tabelle `dockerteststep`
--

DROP TABLE IF EXISTS `dockerteststep`;
CREATE TABLE `dockerteststep` (
  `teststepid` int(11) NOT NULL AUTO_INCREMENT,
  `expect` longtext NOT NULL,
  `testcode` longtext NOT NULL,
  `title` varchar(255) NOT NULL,
  `testid` int(11) NOT NULL,
  PRIMARY KEY (`teststepid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------
-- Tabellenstruktur für Tabelle `groups`
--

DROP TABLE IF EXISTS `groups`;
CREATE TABLE IF NOT EXISTS `groups` (
  `gid` int(11) NOT NULL AUTO_INCREMENT,
  `allowStudentsToQuit` bit(1) NOT NULL,
  `allowStudentsToSignup` bit(1) NOT NULL,
  `maxStudents` int(11) NOT NULL,
  `name` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `submissionGroup` bit(1) NOT NULL,
  `lectureid` int(11) NOT NULL,
  `membersvisibletostudents` bit(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`gid`),
  KEY `FKB63DD9D4AF18EDD1` (`lectureid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `javaadvancedioteststep`
--

DROP TABLE IF EXISTS `javaadvancedioteststep`;
CREATE TABLE IF NOT EXISTS `javaadvancedioteststep` (
  `teststepid` int(11) NOT NULL AUTO_INCREMENT,
  `expect` longtext NOT NULL,
  `testcode` longtext NOT NULL,
  `title` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `testid` int(11) NOT NULL,
  PRIMARY KEY (`teststepid`),
  KEY `FK1DB21A80AE3F26C5` (`testid`),
  KEY `FK1DB21A80DB681CA1` (`testid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `lectures`
--

DROP TABLE IF EXISTS `lectures`;
CREATE TABLE IF NOT EXISTS `lectures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gradingMethod` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `requiresAbhnahme` bit(1) NOT NULL,
  `semester` int(11) NOT NULL,
  `description` TEXT NOT NULL DEFAULT '',
  `allowselfsubscribe` BIT(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `logs`
--

DROP TABLE IF EXISTS `logs`;
CREATE TABLE IF NOT EXISTS `logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action` int(11) NOT NULL,
  `result` bit(1) DEFAULT NULL,
  `testOutput` longtext DEFAULT NULL COLLATE utf8mb4_unicode_ci,
  `timeStamp` datetime DEFAULT NULL,
  `taskId` int(11) NOT NULL,
  `testId` int(11) DEFAULT NULL,
  `userId` int(11) NOT NULL,
  `additionaldata` LONGTEXT COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `FK32C5AFAE3F26C5` (`testId`),
  KEY `FK32C5AFB0B38AF7` (`userId`),
  KEY `FK32C5AFAE0697EB` (`taskId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mcoptions`
--

DROP TABLE IF EXISTS `mcoptions`;
CREATE TABLE IF NOT EXISTS `mcoptions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `correct` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2BE2448AE0697EB` (`taskid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

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
  KEY `FKA301B52DDB37416` (`id`),
  KEY `FKA301B52AF18EDD1` (`lectureid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `pointcategories`
--

DROP TABLE IF EXISTS `pointcategories`;
CREATE TABLE IF NOT EXISTS `pointcategories` (
  `pointcatid` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `optional` bit(1) NOT NULL,
  `points` int(11) NOT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`pointcatid`),
  KEY `FK2623E7ACAE0697EB` (`taskid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

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
  KEY `FK4BE59BED39FBF139` (`submissionid`),
  KEY `FK4BE59BED638E4301` (`categoryid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `pointhistory`
--

DROP TABLE IF EXISTS `pointhistory`;
CREATE TABLE IF NOT EXISTS `pointhistory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `added` longtext NOT NULL COLLATE utf8mb4_unicode_ci,
  `date` datetime NOT NULL,
  `field` varchar(255) NOT NULL,
  `removed` longtext NOT NULL COLLATE utf8mb4_unicode_ci,
  `submission_submissionid` int(11) NOT NULL,
  `who_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1DB12D04AEB18C37` (`who_id`),
  KEY `FK1DB12D046B74DB4C` (`submission_submissionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

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
  KEY `FKB31AC193B8B275` (`similarityTest_similarityTestId`),
  KEY `FKB31AC1B470E5BE` (`submissionOne_submissionid`),
  KEY `FKB31AC117AAD798` (`submissionTwo_submissionid`),
  UNIQUE KEY `UKdtkfair4dgx3r6e6utbf1fuu1` (`similarityTest_similarityTestId`, `submissionOne_submissionid`, `submissionTwo_submissionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

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
  `normalizeCapitalization` bit(1) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `tabsSpacesNewlinesNormalization` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`similarityTestId`),
  KEY `FK86B2AD1EAE0697EB` (`taskid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions`
--

DROP TABLE IF EXISTS `submissions`;
CREATE TABLE IF NOT EXISTS `submissions` (
  `submissionid` int(11) NOT NULL AUTO_INCREMENT,
  `closedTime` datetime DEFAULT NULL,
  `lastModified` datetime DEFAULT NULL,
  `duplicate` int(11) DEFAULT NULL,
  `internalComment` longtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pointStatus` tinyint(4) DEFAULT NULL,
  `points` int(11) DEFAULT NULL,
  `publicComment` longtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `closedBy` int(11) DEFAULT NULL,
  `issuedBy_id` int(11) DEFAULT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`submissionid`),
  KEY `FK2912EA71ED6A9BE` (`closedBy`),
  KEY `issuedby` (`issuedBy_id`),
  KEY `FK2912EA7AE0697EB` (`taskid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions_results`
--

DROP TABLE IF EXISTS `submissions_results`;
CREATE TABLE IF NOT EXISTS `submissions_results` (
  `resultid` int(11) NOT NULL AUTO_INCREMENT,
  `result` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `submissionid` int(11) NOT NULL,
  PRIMARY KEY (`resultid`),
  KEY `FKB4227A5E39FBF139` (`submissionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `submissions_tasknumbers`
--

DROP TABLE IF EXISTS `submissions_tasknumbers`;
CREATE TABLE IF NOT EXISTS `submissions_tasknumbers` (
  `tasknumberid` int(11) NOT NULL AUTO_INCREMENT,
  `number` varchar(255) NOT NULL,
  `origNumber` varchar(255) NOT NULL,
  `participationid` int(11) NOT NULL,
  `submissionid` int(11) DEFAULT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`tasknumberid`),
  KEY `FK44B4D38D39FBF139` (`submissionid`),
  KEY `FK44B4D38DAE0697EB` (`taskid`),
  KEY `FK44B4D38D1986B517` (`participationid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `taskgroups`
--

DROP TABLE IF EXISTS `taskgroups`;
CREATE TABLE IF NOT EXISTS `taskgroups` (
  `taskGroupId` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `lectureid` int(11) NOT NULL,
  PRIMARY KEY (`taskGroupId`),
  KEY `FK5BD51799AF18EDD1` (`lectureid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tasks`
--

DROP TABLE IF EXISTS `tasks`;
CREATE TABLE IF NOT EXISTS `tasks` (
  `taskid` int(11) NOT NULL AUTO_INCREMENT,
  `allowPrematureSubmissionClosing` bit(1) NOT NULL,
  `allowSubmittersAcrossGroups` bit(1) NOT NULL,
  `archiveFilenameRegexp` varchar(255) NOT NULL,
  `deadline` datetime NOT NULL,
  `description` longtext NOT NULL COLLATE utf8mb4_unicode_ci,
  `dynamicTask` varchar(255) DEFAULT NULL,
  `featuredFiles` text NOT NULL,
  `filenameRegexp` varchar(255) NOT NULL,
  `maxPoints` int(11) NOT NULL,
  `maxSubmitters` int(11) NOT NULL,
  `maxsize` int(11) NOT NULL,
  `minPointStep` int(11) NOT NULL,
  `showPoints` datetime DEFAULT NULL,
  `showTextArea` VARCHAR(255) NOT NULL,
  `start` datetime NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `title` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `tutorsCanUploadFiles` bit(1) NOT NULL,
  `taskgroupid` int(11) NOT NULL,
  `modelSolutionProvision` varchar(255),
  PRIMARY KEY (`taskid`),
  KEY `FK6907B8E1B0B1D69` (`taskgroupid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `testresults`
--

DROP TABLE IF EXISTS `testresults`;
CREATE TABLE IF NOT EXISTS `testresults` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `passedTest` bit(1) NOT NULL,
  `testOutput` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `submission_submissionid` int(11) NOT NULL,
  `test_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC6CC0F246B74DB4C` (`submission_submissionid`),
  KEY `FKC6CC0F248DBEBD80` (`test_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tests`
--

DROP TABLE IF EXISTS `tests`;
CREATE TABLE IF NOT EXISTS `tests` (
  `DTYPE` varchar(31) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `forTutors` bit(1) NOT NULL,
  `giveDetailsToStudents` bit(1) NOT NULL,
  `needsToRun` bit(1) NOT NULL,
  `testDescription` varchar(255) DEFAULT NULL COLLATE utf8mb4_unicode_ci,
  `testTitle` varchar(255) DEFAULT NULL COLLATE utf8mb4_unicode_ci,
  `timeout` int(11) NOT NULL,
  `timesRunnableByStudents` int(11) NOT NULL,
  `mainClass` varchar(255) DEFAULT NULL,
  `commandLineParameter` varchar(255) DEFAULT NULL,
  `regularExpression` varchar(255) DEFAULT NULL,
  `preparationshellcode` longtext DEFAULT NULL,
  `excludedFiles` varchar(255) DEFAULT NULL,
  `minProzent` int(11) DEFAULT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6924E21AE0697EB` (`taskid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `testscounts`
--

DROP TABLE IF EXISTS `testscounts`;
CREATE TABLE IF NOT EXISTS `testscounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timesExecuted` int(11) NOT NULL,
  `test_id` int(11) NOT NULL,
  `user_uid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKF81042A58DBEBD80` (`test_id`),
  KEY `FKF81042A5D2AB5AAD` (`user_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL COLLATE utf8mb4_general_ci,
  `email` varchar(255) NOT NULL COLLATE utf8mb4_general_ci,
  `firstName` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `lastName` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `superUser` bit(1) NOT NULL,
  `matrikelno` int(11) DEFAULT NULL,
  `studiengang` varchar(255) DEFAULT NULL COLLATE utf8mb4_unicode_ci,
  `lastLoggedIn` datetime DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `checklisttestcheckitem`
--
ALTER TABLE `checklisttestcheckitem`
  ADD CONSTRAINT `FK81pc66uelq448v1na0u5ryetf` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `dockerteststep`
--
ALTER TABLE `dockerteststep`
  ADD CONSTRAINT `FK4tbopcx0wiytwom7cs13924no` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `groups`
--
ALTER TABLE `groups`
  ADD CONSTRAINT `FKB63DD9D4AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `groups_tutors`
--
ALTER TABLE `groups_tutors`
  ADD CONSTRAINT `FK8EAE7CC842D82B98` FOREIGN KEY (`tutors_id`) REFERENCES `participations` (`id`),
  ADD CONSTRAINT `FK8EAE7CC8BB3EB910` FOREIGN KEY (`groups_gid`) REFERENCES `groups` (`gid`);

--
-- Constraints der Tabelle `javaadvancedioteststep`
--
ALTER TABLE `javaadvancedioteststep`
  ADD CONSTRAINT `FK1DB21A80AE3F26C5` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK1DB21A80DB681CA1` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `logs`
--
ALTER TABLE `logs`
  ADD CONSTRAINT `FK32C5AFAE0697EB` FOREIGN KEY (`taskId`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK32C5AFAE3F26C5` FOREIGN KEY (`testId`) REFERENCES `tests` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK32C5AFB0B38AF7` FOREIGN KEY (`userId`) REFERENCES `users` (`uid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `mcoptions`
--
ALTER TABLE `mcoptions`
  ADD CONSTRAINT `FK2BE2448AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `participations`
--
ALTER TABLE `participations`
  ADD CONSTRAINT `FKA301B527F3A8A13` FOREIGN KEY (`groupid`) REFERENCES `groups` (`gid`),
  ADD CONSTRAINT `FKA301B52AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FKA301B52DDB37416` FOREIGN KEY (`id`) REFERENCES `participations` (`id`),
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
  ADD CONSTRAINT `FK1DB12D046B74DB4C` FOREIGN KEY (`submission_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK1DB12D04AEB18C37` FOREIGN KEY (`who_id`) REFERENCES `participations` (`id`);

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
  ADD CONSTRAINT `FK2912EA71ED6A9BE` FOREIGN KEY (`closedBy`) REFERENCES `participations` (`id`),
  ADD CONSTRAINT `FK2912EA7AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE,
  ADD CONSTRAINT `issuedby` FOREIGN KEY (`issuedBy_id`) REFERENCES `participations` (`id`);

--
-- Constraints der Tabelle `submissions_participations`
--
ALTER TABLE `submissions_participations`
  ADD CONSTRAINT `FK27F157EA16D3DBEB` FOREIGN KEY (`submitters_id`) REFERENCES `participations` (`id`),
  ADD CONSTRAINT `FK27F157EA5F9373D1` FOREIGN KEY (`submissions_submissionid`) REFERENCES `submissions` (`submissionid`);

--
-- Constraints der Tabelle `submissions_results`
--
ALTER TABLE `submissions_results`
  ADD CONSTRAINT `FKB4227A5E39FBF139` FOREIGN KEY (`submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `submissions_tasknumbers`
--
ALTER TABLE `submissions_tasknumbers`
  ADD CONSTRAINT `FK44B4D38D1986B517` FOREIGN KEY (`participationid`) REFERENCES `participations` (`id`),
  ADD CONSTRAINT `FK44B4D38D39FBF139` FOREIGN KEY (`submissionid`) REFERENCES `submissions` (`submissionid`),
  ADD CONSTRAINT `FK44B4D38DAE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `taskgroups`
--
ALTER TABLE `taskgroups`
  ADD CONSTRAINT `FK5BD51799AF18EDD1` FOREIGN KEY (`lectureid`) REFERENCES `lectures` (`id`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `tasks`
--
ALTER TABLE `tasks`
  ADD CONSTRAINT `FK6907B8E1B0B1D69` FOREIGN KEY (`taskgroupid`) REFERENCES `taskgroups` (`taskGroupId`) ON DELETE CASCADE;

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
  ADD CONSTRAINT `FK6924E21AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

--
-- Constraints der Tabelle `testscounts`
--
ALTER TABLE `testscounts`
  ADD CONSTRAINT `FKF81042A58DBEBD80` FOREIGN KEY (`test_id`) REFERENCES `tests` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FKF81042A5D2AB5AAD` FOREIGN KEY (`user_uid`) REFERENCES `users` (`uid`) ON DELETE CASCADE;

-- Manual additions
ALTER TABLE `groups_tutors` DROP FOREIGN KEY `FK8EAE7CC842D82B98`; ALTER TABLE `groups_tutors` ADD CONSTRAINT `FK8EAE7CC842D82B98` FOREIGN KEY (`tutors_id`) REFERENCES `participations`(`id`) ON DELETE CASCADE; 
ALTER TABLE `groups_tutors` DROP FOREIGN KEY `FK8EAE7CC8BB3EB910`; ALTER TABLE `groups_tutors` ADD CONSTRAINT `FK8EAE7CC8BB3EB910` FOREIGN KEY (`groups_gid`) REFERENCES `groups`(`gid`) ON DELETE CASCADE; 
ALTER TABLE `submissions_participations` DROP FOREIGN KEY `FK27F157EA5F9373D1`; ALTER TABLE `submissions_participations` ADD CONSTRAINT `FK27F157EA5F9373D1` FOREIGN KEY (`submissions_submissionid`) REFERENCES `submissions` (`submissionid`) ON DELETE CASCADE;
ALTER TABLE `submissions_tasknumbers` DROP FOREIGN KEY `FK44B4D38D39FBF139`; ALTER TABLE `submissions_tasknumbers` add constraint `FK44B4D38D39FBF139` foreign key (submissionid) references submissions (submissionid) ON DELETE SET NULL;

-- no need to have two indeces on same column
ALTER TABLE `groups_tutors` DROP INDEX `FK8EAE7CC8BB3EB910`;
ALTER TABLE `participations` DROP INDEX `FKA301B52DDB37416`;
ALTER TABLE `participations` DROP INDEX `FKA301B52AF18EDD1`;
ALTER TABLE `participations` DROP FOREIGN KEY `FKA301B52DDB37416`;
ALTER TABLE `submissions_participations` DROP INDEX `FK27F157EA5F9373D1`;
ALTER TABLE `similarities` DROP INDEX `FKB31AC193B8B275`;

-- use German sort ordering for names
ALTER TABLE `groups` CHANGE `name` `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `users` CHANGE `lastName` `lastName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `users` CHANGE `firstName` `firstName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
