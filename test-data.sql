SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `abgabesystem`
--

--
-- Daten für Tabelle `groups`
--

INSERT INTO `groups` (`gid`, `allowStudentsToQuit`, `allowStudentsToSignup`, `maxStudents`, `name`, `submissionGroup`, `lectureid`) VALUES
(1, b'0', b'0', 2, 'Group 1 (fixed)', b'0', 1),
(2, b'1', b'1', 2, 'Group 2 (full)', b'0', 1),
(3, b'1', b'1', 2, 'Group 3', b'0', 1),
(4, b'1', b'1', 2, 'Group 4 (empty)', b'0', 1),
(5, b'0', b'0', 2, 'Group 5 (no join)', b'0', 1);

--
-- Daten für Tabelle `groups_tutors`
--

INSERT INTO `groups_tutors` (`groups_gid`, `tutors_id`) VALUES
(1, 20),
(2, 1),
(2, 20),
(3, 1),
(4, 1);

--
-- Daten für Tabelle `javaadvancedioteststep`
--

INSERT INTO `javaadvancedioteststep` (`teststepid`, `expect`, `testcode`, `title`, `testid`) VALUES
(1, 'Hello World!', 'HelloWorld.main(null);', 'Hello World', 2);

--
-- Daten für Tabelle `lectures`
--

INSERT INTO `lectures` (`id`, `gradingMethod`, `name`, `requiresAbhnahme`, `semester`) VALUES
(1, 'taskWise', 'Lecture 1', b'1', 20201),
(2, 'groupWise', 'Lecture 2 Groupwise', b'0', 20201);

--
-- Daten für Tabelle `logs`
--

INSERT INTO `logs` (`id`, `action`, `result`, `testOutput`, `timeStamp`, `upload`, `uploadFilename`, `taskId`, `testId`, `userId`, `additionaldata`) VALUES
(1, 0, NULL, NULL, '2020-12-21 10:35:21', 0x36307c7c0a3131313130303d3630, '!textsolution!', 1, NULL, 8, '{\"taskNumbers\":[{\"number\":\"111100\",\"origNumber\":\"60\"}],\"userResponses\":[\"60\"]}'),
(2, 2, NULL, NULL, '2020-12-21 10:35:31', NULL, 'MathFunctions.java', 1, NULL, 8, '{\"filename\":\"MathFunctions.java\"}'),
(3, 0, NULL, NULL, '2020-12-21 10:36:17', 0x337c7c, '!mc!', 2, NULL, 8, '{\"mc\":[\"3\"]}'),
(4, 0, NULL, NULL, '2020-12-21 10:37:12', 0x7075626c696320636c6173732048656c6c6f576f726c64207b0d0a097075626c69632073746174696320766f6964206d61696e28537472696e675b5d206172677329207b0d0a090953797374656d2e6f75742e7072696e746c6e282248656c6c6f20576f726c642122293b0d0a097d0d0a7d, 'HelloWorld.java', 3, NULL, 8, '{\"filename\":\"HelloWorld.java\"}'),
(5, 1, b'1', '', '2020-12-21 10:37:19', NULL, NULL, 3, 1, 8, NULL),
(6, 1, b'1', '', '2020-12-21 10:37:29', NULL, NULL, 3, 1, 8, NULL),
(7, 1, b'1', '{\"stdout\":\"Hello World!\\n\",\"separator\":\"#<GATE@-2790265235529257908#@>#\\n\",\"exitedCleanly\":true,\"steps\":[{\"id\":1,\"got\":\"Hello World!\\n\",\"expected\":\"Hello World!\",\"ok\":true}]}', '2020-12-21 10:37:39', NULL, NULL, 3, 2, 8, NULL),
(8, 1, b'1', '{\"stdout\":\"Hello World!\\n\",\"separator\":\"#<GATE@-1526931535110961630#@>#\\n\",\"exitedCleanly\":true,\"steps\":[{\"id\":1,\"got\":\"Hello World!\\n\",\"expected\":\"Hello World!\",\"ok\":true}]}', '2020-12-21 10:37:47', NULL, NULL, 3, 2, 8, NULL),
(9, 0, NULL, NULL, '2020-12-21 10:43:00', 0x32367c7c0a6e6978206973, '!textsolution!', 1, NULL, 9, '{\"taskNumbers\":[{\"number\":\"10001\",\"origNumber\":\"17\"}],\"userResponses\":[\"26\"]}'),
(10, 0, NULL, NULL, '2020-12-21 10:43:08', 0x337c7c, '!mc!', 2, NULL, 9, '{\"mc\":[\"3\"]}'),
(11, 0, NULL, NULL, '2020-12-21 10:43:46', 0x7075626c696320636c6173732047617573737363686553756d6d656e466f726d656c0d0a7b0d0a092f2a0d0a09202a20446965206d61696e204d6574686f64652e0d0a09202a20486965722073696e64207363686f6e2065696e207061617220546573746661656c6c6520766f726765676562656e2e0d0a09202a2054726f747a64656d206461726673742064752061756368206e6f636820656967656e6520546573746661656c6c652064617a752073636872656962656e2e0d0a09202a2f0d0a097075626c69632073746174696320766f6964206d61696e28537472696e675b5d2061726773290d0a097b0d0a090953797374656d2e6f75742e7072696e746c6e28224469652053756d6d6520766f6e20312b32206973743a2022293b0d0a090953797374656d2e6f75742e7072696e746c6e286265726563686e53756d6d65283229293b0d0a090953797374656d2e6f75742e7072696e746c6e28224469652053756d6d6520766f6e20312b322b33206973743a2022293b0d0a090953797374656d2e6f75742e7072696e746c6e286265726563686e53756d6d65283329293b0d0a090953797374656d2e6f75742e7072696e746c6e28224469652053756d6d6520766f6e20312b322b332b34206973743a2022293b0d0a090953797374656d2e6f75742e7072696e746c6e286265726563686e53756d6d65283429293b0d0a090953797374656d2e6f75742e7072696e746c6e28224469652053756d6d6520766f6e20312b322b332b342b35206973743a2022293b0d0a090953797374656d2e6f75742e7072696e746c6e286265726563686e53756d6d65283529293b0d0a097d0d0a090d0a092f2a0d0a09202a20496e20646965736572204175666761626520736f6c6c2065696e652053756d6d65206265726563686e65742077657264656e2e0d0a09202a20446162656920736f6c6c656e20616c6c65205a61686c656e20766f6e203120626973206e2073756d6d696572742077657264656e2e0d0a09202a20426569737069656c3a0d0a09202a206265726563686e6553756d6d65283529202d2d3e20312b322b332b342b35203d2031352e0d0a09202a20416c7320556562756e6720736f6c6c746520646965732072656b7572736976206265726563686e65742077657264656e2e0d0a09202a2044617a75206b616e6e206d616e20616e6e65686d656e2c2064617373206469657365204d6574686f6465206e7572206d69740d0a09202a20706f7369746976656e205a61686c656e20676574657374657420776972642e0d0a09202a2044616e6e20697374206265726563686e6553756d6d65286e29203d206265726563686e6553756d6d65286e2d31292b6e2c2066616c6c73206e203e20310d0a09202a2009090909090909203d2031202020202020202020202020202020202020202c20736f6e73740d0a09202a2048696e776569733a204e61747565726c696368206b616e6e206d616e2064696573652046756e6b74696f6e20617563682069746572617469760d0a09202a206265726563686e656e2c20756e6420657320697374206562656e66616c6c7320616c6c67656d65696e2062656b616e6e742c20646173730d0a09202a20312b322b332b2e2e2e2b6e203d20286e2a286e2b3129292f32206973742e0d0a09202a2044656e6e6f6368206973742065732065696e65206775746520556562756e672c20646965736520466f726d656c0d0a09202a206165686e6c69636820776965206469652046616b756c7461657420617563682072656b7572736976207a75206265726563686e656e2e0d0a09202a2f0d0a097075626c69632073746174696320696e74206265726563686e53756d6d6528696e74206e290d0a097b0d0a0909696e742073c3bc6d6d65203d2031323b0d0a090972657475726e2073c3bc6d6d653b0d0a097d0d0a7d, 'HelloWorld.java', 3, NULL, 9, '{\"filename\":\"HelloWorld.java\"}'),
(12, 1, b'0', 'HelloWorld.java:1: error: class GaussscheSummenFormel is public, should be declared in a file named GaussscheSummenFormel.java\r\npublic class GaussscheSummenFormel\r\n       ^\r\n1 error\r\n', '2020-12-21 10:43:52', NULL, NULL, 3, 1, 9, NULL),
(13, 1, b'0', '{\"stdout\":\"\",\"separator\":\"#<GATE@-5785040581425448127#@>#\\n\",\"exitedCleanly\":false,\"steps\":[{\"id\":1,\"got\":\"\",\"expected\":\"Hello World!\",\"ok\":false}]}', '2020-12-21 10:44:00', NULL, NULL, 3, 2, 9, NULL),
(14, 0, NULL, NULL, '2020-12-21 10:44:36', 0x34377c7c0a3130313131313d3437, '!textsolution!', 1, NULL, 3, '{\"taskNumbers\":[{\"number\":\"101111\",\"origNumber\":\"47\"}],\"userResponses\":[\"47\"]}'),
(15, 0, NULL, NULL, '2020-12-21 10:44:44', 0x337c7c, '!mc!', 2, NULL, 3, '{\"mc\":[\"3\"]}'),
(16, 0, NULL, NULL, '2020-12-21 10:45:23', 0x7075626c696320636c6173732048656c6c6f576f726c64207b0d0a09737461746963207075626c696320766f6964206d61696e28537472696e675b5d206172677329207b0d0a090953797374656d2e6f75742e7072696e746c6e282248656c6c6f20576f726c642122293b0d0a097d0d0a7d, 'HelloWorld.java', 3, NULL, 3, '{\"filename\":\"HelloWorld.java\"}'),
(17, 1, b'1', '', '2020-12-21 10:45:32', NULL, NULL, 3, 1, 3, NULL),
(18, 1, b'1', '{\"stdout\":\"Hello World!\\n\",\"separator\":\"#<GATE@7902065626236426519#@>#\\n\",\"exitedCleanly\":true,\"steps\":[{\"id\":1,\"got\":\"Hello World!\\n\",\"expected\":\"Hello World!\",\"ok\":true}]}', '2020-12-21 10:45:42', NULL, NULL, 3, 2, 3, NULL),
(19, 0, NULL, NULL, '2020-12-21 10:46:44', 0x7075626c696320636c6173732048656c6c6f576f726c64207b0d0a09737461746963207075626c696320766f6964206d61696e28537472696e675b5d206172677329207b0d0a090953797374656d2e6f75742e7072696e746c6e282248656c6c6f20576f726c642122293b0d0a097d0d0a7d, 'HelloWorld.java', 3, NULL, 5, '{\"filename\":\"HelloWorld.java\"}'),
(20, 1, b'1', '', '2020-12-21 10:46:51', NULL, NULL, 3, 1, 5, NULL),
(21, 1, b'1', '', '2020-12-21 10:46:57', NULL, NULL, 3, 1, 5, NULL),
(22, 2, NULL, NULL, '2020-12-21 10:47:11', NULL, 'HelloWorld.java', 3, NULL, 5, '{\"filename\":\"HelloWorld.java\"}'),
(23, 0, NULL, NULL, '2020-12-21 10:47:39', 0x7075626c696320636c6173732048656c6c6f576f726c64207b0d0a09737461746963207075626c696320766f6964206d61696e28537472696e675b5d206172677329207b0d0a090953797374656d2e6f75742e7072696e746c6e282248656c6c6f20576f726c642122293b0d0a097d0d0a7d, 'HelloWorld.java', 3, NULL, 6, '{\"filename\":\"HelloWorld.java\"}'),
(24, 0, NULL, NULL, '2020-12-21 10:50:58', 0x337c7c, '!mc!', 2, NULL, 10, '{\"mc\":[\"3\"]}'),
(25, 0, NULL, NULL, '2020-12-23 14:19:14', 0x33347c7c0a, '!textsolution!', 1, NULL, 5, '{\"taskNumbers\":[{\"number\":\"100010\",\"origNumber\":\"34\"}],\"userResponses\":[\"34\"]}');

--
-- Daten für Tabelle `mcoptions`
--

INSERT INTO `mcoptions` (`id`, `correct`, `title`, `taskid`) VALUES
(1, b'0', 'Wrong 1', 2),
(2, b'0', 'Wrong 2', 2),
(3, b'1', 'Correct', 2),
(4, b'0', 'Wrong 3', 2);

--
-- Daten für Tabelle `participations`
--

INSERT INTO `participations` (`id`, `role`, `groupid`, `lectureid`, `uid`) VALUES
(1, 'ADVISOR', NULL, 1, 1),
(2, 'ADVISOR', NULL, 2, 1),
(4, 'NORMAL', 1, 1, 3),
(5, 'NORMAL', NULL, 2, 3),
(6, 'NORMAL', 1, 1, 4),
(7, 'NORMAL', NULL, 2, 4),
(8, 'NORMAL', 2, 1, 5),
(9, 'NORMAL', NULL, 2, 5),
(10, 'NORMAL', 2, 1, 6),
(11, 'NORMAL', NULL, 2, 6),
(12, 'NORMAL', 3, 1, 7),
(13, 'NORMAL', NULL, 2, 7),
(14, 'NORMAL', NULL, 1, 8),
(15, 'NORMAL', NULL, 2, 8),
(16, 'NORMAL', NULL, 2, 9),
(17, 'NORMAL', NULL, 1, 9),
(18, 'NORMAL', NULL, 2, 10),
(19, 'NORMAL', 1, 1, 10),
(20, 'TUTOR', NULL, 1, 11),
(21, 'NORMAL', NULL, 2, 11);

--
-- Daten für Tabelle `pointcategories`
--

INSERT INTO `pointcategories` (`pointcatid`, `description`, `optional`, `points`, `taskid`) VALUES
(1, 'Correct solution', b'0', 100, 1),
(2, 'Very nice explanation', b'1', 50, 1),
(3, 'Compiles', b'0', 50, 3),
(4, 'Output correct', b'0', 100, 3);

--
-- Daten für Tabelle `pointgiven`
--

INSERT INTO `pointgiven` (`pointgivenid`, `points`, `categoryid`, `submissionid`) VALUES
(1, 100, 1, 1),
(2, 50, 2, 1),
(3, 100, 1, 8),
(4, 50, 3, 3),
(5, 100, 4, 3),
(6, 50, 3, 10),
(7, 100, 4, 10);

--
-- Daten für Tabelle `pointhistory`
--

INSERT INTO `pointhistory` (`id`, `added`, `date`, `field`, `removed`, `submission_submissionid`, `who_id`) VALUES
(1, 'ABGENOMMEN', '2020-12-21 10:52:14', 'status', '', 14, 1),
(2, '1', '2020-12-21 10:52:14', 'points', '', 14, 1),
(3, '1', '2020-12-21 10:55:14', 'Correct solution', '0', 1, 20),
(4, '0,5', '2020-12-21 10:55:14', 'Very nice explanation', '0', 1, 20),
(5, 'ABGENOMMEN', '2020-12-21 10:55:14', 'status', '', 1, 20),
(6, '1,5', '2020-12-21 10:55:14', 'points', '', 1, 20),
(7, 'internal comment', '2020-12-21 10:55:14', 'internalComment', '', 1, 20),
(8, 'really good', '2020-12-21 10:55:14', 'publicComment', '', 1, 20),
(9, '1', '2020-12-21 10:55:40', 'Correct solution', '0', 8, 20),
(10, 'ABGENOMMEN', '2020-12-21 10:55:40', 'status', '', 8, 20),
(11, '1', '2020-12-21 10:55:40', 'points', '', 8, 20),
(12, 'ok', '2020-12-21 10:55:40', 'publicComment', '', 8, 20),
(13, '0,5', '2020-12-21 10:56:49', 'Compiles', '0', 3, 20),
(14, '1', '2020-12-21 10:56:49', 'Output correct', '0', 3, 20),
(15, 'NICHT_ABGENOMMEN', '2020-12-21 10:56:49', 'status', '', 3, 20),
(16, '0', '2020-12-21 10:56:49', 'duplicate', '', 3, 20),
(17, '1,5', '2020-12-21 10:56:49', 'points', '', 3, 20),
(18, 'ist plagiat', '2020-12-21 10:56:49', 'internalComment', '', 3, 20),
(19, 'ne, so nicht', '2020-12-21 10:56:49', 'publicComment', '', 3, 20),
(20, 'ABGENOMMEN_FAILED', '2020-12-21 10:57:15', 'status', '', 7, 20),
(21, '0', '2020-12-21 10:57:15', 'points', '', 7, 20),
(22, 'falsch und abnahme nicht bestanden', '2020-12-21 10:57:15', 'publicComment', '', 7, 20),
(23, '0,5', '2020-12-21 10:57:32', 'Compiles', '0', 10, 20),
(24, 'NICHT_BEWERTET', '2020-12-21 10:57:32', 'status', '', 10, 20),
(25, '0,5', '2020-12-21 10:57:32', 'points', '', 10, 20),
(26, '1', '2020-12-21 10:57:43', 'Output correct', '0', 10, 20),
(27, '1,5', '2020-12-21 10:57:43', 'points', '0,5', 10, 20),
(28, 'nochmal geändert', '2020-12-21 10:57:43', 'publicComment', '', 10, 20),
(29, 'ABGENOMMEN', '2020-12-21 10:57:48', 'status', 'NICHT_BEWERTET', 10, 20),
(30, 'jetzt final', '2020-12-21 10:57:48', 'publicComment', 'nochmal geändert', 10, 20);

--
-- Daten für Tabelle `similarities`
--

INSERT INTO `similarities` (`similarityid`, `percentage`, `similarityTest_similarityTestId`, `submissionOne_submissionid`, `submissionTwo_submissionid`) VALUES
(1, 100, 3, 10, 12),
(2, 91, 3, 3, 10),
(3, 91, 3, 10, 3),
(4, 100, 3, 12, 10),
(5, 91, 3, 3, 12),
(6, 91, 3, 12, 3),
(7, 42, 2, 7, 10),
(8, 42, 2, 10, 7);

--
-- Daten für Tabelle `similaritytests`
--

INSERT INTO `similaritytests` (`similarityTestId`, `basis`, `excludeFiles`, `minimumDifferenceInPercent`, `normalizeCapitalization`, `status`, `tabsSpacesNewlinesNormalization`, `type`, `taskid`) VALUES
(1, 'code', '.classpath,.project,META-INF,.settings', 80, b'1', 0, 'all', 'levenshtein', 1),
(2, '', '', 80, b'0', 0, '', 'plaggie', 3),
(3, 'code', '.classpath,.project,META-INF,.settings', 80, b'1', 0, 'all', 'levenshtein', 3);

--
-- Daten für Tabelle `submissions`
--

INSERT INTO `submissions` (`submissionid`, `closedTime`, `lastModified`, `duplicate`, `internalComment`, `pointStatus`, `points`, `publicComment`, `closedBy`, `issuedBy_id`, `taskid`) VALUES
(1, NULL, '2020-12-21 10:35:31', NULL, 'internal comment', 3, 150, 'really good', NULL, 20, 1),
(2, NULL, '2020-12-21 10:36:17', NULL, '', 1, 200, '', NULL, NULL, 2),
(3, NULL, '2020-12-21 10:37:12', 0, 'ist plagiat', 1, 150, 'ne, so nicht', NULL, 20, 3),
(4, NULL, '2020-12-21 10:43:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(5, NULL, '2020-12-21 10:43:08', NULL, '', 1, 200, '', NULL, NULL, 2),
(7, NULL, '2020-12-21 10:43:46', NULL, '', 2, 0, 'falsch und abnahme nicht bestanden', NULL, 20, 3),
(8, NULL, '2020-12-21 10:44:36', NULL, '', 3, 100, 'ok', NULL, 20, 1),
(9, NULL, '2020-12-21 10:44:44', NULL, '', 1, 200, '', NULL, NULL, 2),
(10, NULL, '2020-12-21 10:45:23', NULL, '', 3, 150, 'jetzt final', NULL, 20, 3),
(12, NULL, '2020-12-21 10:47:39', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3),
(13, NULL, '2020-12-21 10:50:58', NULL, '', 1, 200, '', NULL, NULL, 2),
(14, NULL, NULL, NULL, '', 3, 100, '', NULL, 1, 2),
(15, NULL, '2020-12-23 14:19:14', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1);

--
-- Daten für Tabelle `submissions_participations`
--

INSERT INTO `submissions_participations` (`submitters_id`, `submissions_submissionid`) VALUES
(14, 1),
(14, 2),
(14, 3),
(17, 4),
(17, 5),
(17, 7),
(4, 8),
(6, 8),
(4, 9),
(6, 9),
(4, 10),
(6, 10),
(8, 12),
(10, 12),
(19, 13),
(12, 14),
(8, 15),
(10, 15);

--
-- Daten für Tabelle `submissions_results`
--

INSERT INTO `submissions_results` (`resultid`, `result`, `submissionid`) VALUES
(1, '60', 1),
(2, '3', 2),
(3, '26', 4),
(4, '3', 5),
(5, '47', 8),
(6, '3', 9),
(7, '3', 13),
(8, '34', 15);

--
-- Daten für Tabelle `submissions_tasknumbers`
--

INSERT INTO `submissions_tasknumbers` (`tasknumberid`, `number`, `origNumber`, `participationid`, `submissionid`, `taskid`) VALUES
(1, '111100', '60', 14, 1, 1),
(2, '10001', '17', 17, 4, 1),
(3, '101111', '47', 4, 8, 1),
(4, '100010', '34', 8, 15, 1),
(5, '1100001', '97', 10, NULL, 1),
(6, '110', '6', 12, NULL, 1);

--
-- Daten für Tabelle `taskgroups`
--

INSERT INTO `taskgroups` (`taskGroupId`, `title`, `lectureid`) VALUES
(1, 'Taskgroup 1', 1),
(2, 'Java', 1);

--
-- Daten für Tabelle `tasks`
--

INSERT INTO `tasks` (`taskid`, `allowPrematureSubmissionClosing`, `allowSubmittersAcrossGroups`, `archiveFilenameRegexp`, `deadline`, `description`, `dynamicTask`, `featuredFiles`, `filenameRegexp`, `maxPoints`, `maxSubmitters`, `maxsize`, `minPointStep`, `showPoints`, `showTextArea`, `start`, `type`, `title`, `tutorsCanUploadFiles`, `taskgroupid`, `modelSolutionProvision`) VALUES
(1, b'0', b'0', '-', '2020-12-21 10:40:39', '<p>Berechnen Sie die Dezimaldarstellung des Bin&auml;r-Wertes $Var0$.</p>', 'bin2dec', '', '-', 100, 2, 10485760, 50, '2020-12-28 10:26:50', b'1', '2020-12-21 10:26:50', 'dynamicTask', 'Dynamic Task binary numbers', b'0', 1, NULL),
(2, b'0', b'0', '-', '2020-12-21 10:40:39', '<p>Choose wisely!</p>', NULL, '', '-', 200, 2, 10485760, 50, '2020-12-28 10:30:39', b'0', '2020-12-21 10:30:39', 'mc', 'MC Task', b'0', 1, NULL),
(3, b'0', b'0', '-', '2020-12-21 10:40:39', '<p>Write \"Hello World\" (class name <em>HelloWorld</em>)</p>', NULL, '', 'HelloWorld\\.java', 150, 2, 10485760, 50, '2020-12-28 10:52:01', b'0', '2020-12-21 10:32:01', '', 'Hello World', b'0', 2, NULL),
(4, b'1', b'0', '-', '2100-12-28 10:53:05', '<p>Upload some Java</p>', NULL, '', '[A-Z][A-Za-z0-9_]+\\.java', 500, 1, 10485760, 50, '2120-12-28 10:53:05', b'0', '2020-12-21 10:53:05', '', 'Something', b'0', 2, NULL);

--
-- Daten für Tabelle `testresults`
--

INSERT INTO `testresults` (`id`, `passedTest`, `testOutput`, `submission_submissionid`, `test_id`) VALUES
(1, b'1', '', 10, 1),
(2, b'1', '', 3, 1),
(3, b'1', '', 12, 1),
(4, b'0', 'HelloWorld.java:1: error: class GaussscheSummenFormel is public, should be declared in a file named GaussscheSummenFormel.java\r\npublic class GaussscheSummenFormel\r\n       ^\r\n1 error\r\n', 7, 1),
(5, b'0', '{\"stdout\":\"\",\"separator\":\"#<GATE@-6314383504834687576#@>#\\n\",\"exitedCleanly\":false,\"steps\":[{\"id\":1,\"got\":\"\",\"expected\":\"Hello World!\",\"ok\":false}]}', 7, 2),
(6, b'1', '{\"stdout\":\"Hello World!\\n\",\"separator\":\"#<GATE@8512651552286650067#@>#\\n\",\"exitedCleanly\":true,\"steps\":[{\"id\":1,\"got\":\"Hello World!\\n\",\"expected\":\"Hello World!\",\"ok\":true}]}', 3, 2),
(7, b'1', '{\"stdout\":\"Hello World!\\n\",\"separator\":\"#<GATE@-597728958179510121#@>#\\n\",\"exitedCleanly\":true,\"steps\":[{\"id\":1,\"got\":\"Hello World!\\n\",\"expected\":\"Hello World!\",\"ok\":true}]}', 12, 2),
(8, b'1', '{\"stdout\":\"Hello World!\\n\",\"separator\":\"#<GATE@7497054804966300849#@>#\\n\",\"exitedCleanly\":true,\"steps\":[{\"id\":1,\"got\":\"Hello World!\\n\",\"expected\":\"Hello World!\",\"ok\":true}]}', 10, 2);

--
-- Daten für Tabelle `tests`
--

INSERT INTO `tests` (`DTYPE`, `id`, `forTutors`, `giveDetailsToStudents`, `needsToRun`, `testDescription`, `testTitle`, `timeout`, `timesRunnableByStudents`, `mainClass`, `commandLineParameter`, `regularExpression`, `excludedFiles`, `minProzent`, `taskid`) VALUES
('CompileTest', 1, b'1', b'1', b'0', '', 'Syntax-Test', 5, 2, NULL, NULL, NULL, NULL, NULL, 3),
('JavaAdvancedIOTest', 2, b'1', b'1', b'0', NULL, 'Testen', 15, 2, NULL, NULL, NULL, NULL, NULL, 3),
('RegExpTest', 3, b'1', b'0', b'1', '', 'Funktionstest (Tutoren only)', 15, 0, 'HelloWorld', '', 'Hello World!', NULL, NULL, 3),
('CommentsMetricTest', 4, b'0', b'1', b'1', '', 'Kommentar-Metrik (Students only)', 5, 1, NULL, NULL, NULL, '', 5, 3);

--
-- Daten für Tabelle `testscounts`
--

INSERT INTO `testscounts` (`id`, `timesExecuted`, `test_id`, `user_uid`) VALUES
(1, 2, 1, 8),
(2, 2, 2, 8),
(3, 1, 1, 9),
(4, 1, 2, 9),
(5, 1, 1, 3),
(6, 1, 1, 4),
(7, 1, 2, 3),
(8, 1, 2, 4),
(9, 2, 1, 5);

--
-- Daten für Tabelle `users`
--

INSERT INTO `users` (`uid`, `username`, `email`, `firstName`, `lastName`, `superUser`, `matrikelno`, `studiengang`, `lastLoggedIn`) VALUES
(1, 'admin', 'admin@localhost', 'Admin', 'User', b'1', NULL, NULL, '2020-12-21 11:46:13'),
(2, 'user0', 'user0', 'Firstname0', 'Lastname0', b'0', NULL, NULL, NULL),
(3, 'user1', 'user1', 'Firstname1', 'Lastname1', b'0', 1, 'Ägyptologie und Koptologie (Promotion)', '2020-12-21 10:54:04'),
(4, 'user2', 'user2', 'Firstname2', 'Lastname2', b'0', NULL, NULL, '2020-12-21 10:22:30'),
(5, 'user3', 'user3', 'Firstname3', 'Lastname3', b'0', NULL, NULL, '2020-12-21 10:46:31'),
(6, 'user4', 'user4', 'Firstname4', 'Lastname4', b'0', NULL, NULL, '2020-12-21 10:47:28'),
(7, 'user5', 'user5', 'Firstname5', 'Lastname5', b'0', 5, NULL, '2020-12-21 10:54:15'),
(8, 'user6', 'user6', 'Firstname6', 'Lastname6', b'0', NULL, NULL, '2020-12-21 10:34:39'),
(9, 'user7', 'user7', 'Firstname7', 'Lastname7', b'0', NULL, NULL, '2020-12-21 10:42:50'),
(10, 'user8', 'user8', 'Firstname8', 'Lastname8', b'0', NULL, NULL, '2020-12-21 10:50:50'),
(11, 'user9', 'user9', 'Firstname9', 'Lastname9', b'0', NULL, NULL, '2020-12-21 10:54:23'),
(12, 'user10', 'user10', 'Firstname10', 'Lastname10', b'0', NULL, NULL, '2020-12-21 10:24:16'),
(13, 'user11', 'user11', 'Firstname11', 'Lastname11', b'0', NULL, NULL, NULL),
(14, 'user12', 'user12', 'Firstname12', 'Lastname12', b'0', NULL, NULL, NULL),
(15, 'user13', 'user13', 'Firstname13', 'Lastname13', b'0', NULL, NULL, NULL),
(16, 'user14', 'user14', 'Firstname14', 'Lastname14', b'0', NULL, NULL, NULL);
SET FOREIGN_KEY_CHECKS=1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
