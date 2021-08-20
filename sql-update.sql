-- switch to utf8mb4
alter database `submissionsystem` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `groups` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `groups_tutors` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `lectures` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `logs` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `participations` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `pointcategories` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `pointgiven` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `pointhistory` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `similarities` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `similaritytests` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `submissions` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `submissions_participations` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `submissions_results` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `submissions_tasknumbers` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `taskgroups` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `tasks` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `testresults` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `tests` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `testscounts` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
alter table `users` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin;
ALTER TABLE `groups` CHANGE `name` `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `lectures` CHANGE `gradingMethod` `gradingMethod` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `lectures` CHANGE `name` `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `logs` CHANGE `testOutput` `testOutput` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL;
ALTER TABLE `participations` CHANGE `role` `role` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `pointcategories` CHANGE `description` `description` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `pointhistory` CHANGE `added` `added` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `field` `field` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `removed` `removed` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `similaritytests` CHANGE `basis` `basis` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `excludeFiles` `excludeFiles` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `tabsSpacesNewlinesNormalization` `tabsSpacesNewlinesNormalization` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `type` `type` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `submissions` CHANGE `internalComment` `internalComment` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `publicComment` `publicComment` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL;
ALTER TABLE `submissions_results` CHANGE `result` `result` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `submissions_tasknumbers` CHANGE `number` `number` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `origNumber` `origNumber` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `taskgroups` CHANGE `title` `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `tasks` CHANGE `archiveFilenameRegexp` `archiveFilenameRegexp` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `description` `description` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `dynamicTask` `dynamicTask` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `featuredFiles` `featuredFiles` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `filenameRegexp` `filenameRegexp` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `title` `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL;
ALTER TABLE `testresults` CHANGE `testOutput` `testOutput` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `tests` CHANGE `DTYPE` `DTYPE` VARCHAR(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, CHANGE `testDescription` `testDescription` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `testTitle` `testTitle` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `mainClass` `mainClass` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `commandLineParameter` `commandLineParameter` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `regularExpression` `regularExpression` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL, CHANGE `excludedFiles` `excludedFiles` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL;
ALTER TABLE `users` CHANGE `email` `email` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
ALTER TABLE `users` CHANGE `lastName` `lastName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `users` CHANGE `firstName` `firstName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL;
ALTER TABLE `users` CHANGE `studiengang` `studiengang` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NULL DEFAULT NULL;

-- 05d34b44a358972099f8f5aa30b060d1b6a6fc55 and e1793a9c9aa944c37135dde82d8dab5e93132def
ALTER TABLE `logs` ADD `uploadFilename` varchar(255) DEFAULT NULL AFTER `userId`, ADD `upload` longblob DEFAULT NULL AFTER `uploadfilename`;

-- b527092749a8f5e2b718c267d43ef8ff919a8772
ALTER TABLE `similaritytests` CHANGE `status` `status` TINYINT(4) NOT NULL; 
ALTER TABLE `submissions` CHANGE `pointStatus` `pointStatus` TINYINT(4) DEFAULT NULL;

-- Allow to upload model solution and provide it to students
ALTER TABLE `tasks` ADD `modelSolutionProvision` VARCHAR(255) AFTER `taskgroupid`;

-- Add new advanced Java IO tests
CREATE TABLE IF NOT EXISTS `javaadvancedioteststep` (
  `teststepid` int(11) NOT NULL AUTO_INCREMENT,
  `expect` longtext NOT NULL,
  `testcode` longtext NOT NULL,
  `title` varchar(255) NOT NULL,
  `testid` int(11) NOT NULL,
  PRIMARY KEY (`teststepid`),
  KEY `FK1DB21A80AE3F26C5` (`testid`)
) ENGINE=InnoDB;
ALTER TABLE `javaadvancedioteststep` ADD CONSTRAINT `FK1DB21A80AE3F26C5` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE;

-- Add support for multiple choice questions
ALTER TABLE `tasks` ADD `type` VARCHAR(255) NOT NULL AFTER `tutorsCanUploadFiles`;
CREATE TABLE IF NOT EXISTS `mcoptions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `correct` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  `taskid` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2BE2448AE0697EB` (`taskid`)
) ENGINE=InnoDB;
ALTER TABLE `mcoptions` ADD CONSTRAINT `FK2BE2448AE0697EB` FOREIGN KEY (`taskid`) REFERENCES `tasks` (`taskid`) ON DELETE CASCADE;

-- Allow to prematurely close submissions
ALTER TABLE `tasks` ADD `allowPrematureSubmissionClosing` BIT NOT NULL AFTER `modelSolutionProvision`; 
ALTER TABLE `submissions` ADD `closedTime` DATETIME NULL AFTER `taskid`, ADD `closedBy` INT NULL AFTER `closedTime`, ADD INDEX `FK2912EA71ED6A9BE` (`closedBy`); 
ALTER TABLE `submissions` ADD CONSTRAINT `FK2912EA71ED6A9BE` FOREIGN KEY (`closedBy`) REFERENCES `participations` (`id`);

-- Extra username
ALTER TABLE `users` ADD `username` varchar(255) NOT NULL AFTER `uid`;
UPDATE `users` SET `username`=`email`,`email`=concat(`email`,"@tu-clausthal.de");
ALTER TABLE `users` ADD UNIQUE KEY `username` (`username`);
ALTER TABLE `users` DROP KEY `email`;

-- store last logged in time
ALTER TABLE `users` ADD `lastLoggedIn` DATETIME NULL AFTER `studiengang`;

-- Make showing group members to students configurable
ALTER TABLE `groups` ADD `membersvisibletostudents` bit(1) NOT NULL;

-- don't store uploaded files in the database
-- Execute ExtractUploadData from commit fca631b8d487811e5c665ef8bb0b7834d397887d
ALTER TABLE `logs` ADD `additionaldata` LONGTEXT;
ALTER TABLE `logs` DROP `uploadFilename` , DROP `upload`;

-- update collations
ALTER TABLE `javaadvancedioteststep` CHANGE `title` `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `lectures` CHANGE `name` `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `logs` CHANGE `testOutput` `testOutput` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL, CHANGE `additionaldata` `additionaldata` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL;
ALTER TABLE `mcoptions` CHANGE `title` `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `pointcategories` CHANGE `description` `description` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `pointhistory` CHANGE `added` `added` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL, CHANGE `removed` `removed` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `submissions` CHANGE `internalComment` `internalComment` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL, CHANGE `publicComment` `publicComment` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL;
ALTER TABLE `submissions_results` CHANGE `result` `result` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `tasks` CHANGE `description` `description` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL, CHANGE `title` `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `testresults` CHANGE `testOutput` `testOutput` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `tests` CHANGE `testDescription` `testDescription` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL, CHANGE `testTitle` `testTitle` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL;
ALTER TABLE `users` CHANGE `username` `username` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL, CHANGE `email` `email` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;
ALTER TABLE `taskgroups` CHANGE `title` `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL;

-- similarities are unique
alter table similarities add constraint UKdtkfair4dgx3r6e6utbf1fuu1 unique (similaritytest_similaritytestid, submissionone_submissionid, submissiontwo_submissionid), DROP INDEX `FKB31AC193B8B275`;

-- docker test
CREATE TABLE `dockerteststep` (
  `teststepid` int(11) NOT NULL,
  `expect` longtext NOT NULL,
  `testcode` longtext NOT NULL,
  `title` varchar(255) NOT NULL,
  `testid` int(11) NOT NULL,
  PRIMARY KEY (`teststepid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
ALTER TABLE `dockerteststep` ADD CONSTRAINT `FK4tbopcx0wiytwom7cs13924no` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE;
alter table tests add column `preparationshellcode` LONGTEXT DEFAULT NULL;

-- checklist test
CREATE TABLE `checklisttestcheckitem` (
  `checkitemid` int(11) NOT NULL AUTO_INCREMENT,
  `title` longtext NOT NULL,
  `testid` int(11) NOT NULL,
  PRIMARY KEY (`checkitemid`),
  KEY `FK81pc66uelq448v1na0u5ryetf` (`testid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
ALTER TABLE `checklisttestcheckitem` ADD CONSTRAINT `FK81pc66uelq448v1na0u5ryetf` FOREIGN KEY (`testid`) REFERENCES `tests` (`id`) ON DELETE CASCADE;

-- add description to lecture
ALTER TABLE `lectures` ADD `description` TEXT NOT NULL;

-- allow to toggle self subscribe
ALTER TABLE `lectures` ADD `allowselfsubscribe` BIT(1) NOT NULL DEFAULT b'1';
