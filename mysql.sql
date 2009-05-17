alter table groups drop foreign key FKB63DD9D4AF18EDD1
alter table participations drop foreign key FKA301B52DDB37416
alter table participations drop foreign key FKA301B52E28A1D21
alter table participations drop foreign key FKA301B527F3A8A13
alter table participations drop foreign key FKA301B52AF18EDD1
alter table similarities drop foreign key FKB31AC117AAD798
alter table similarities drop foreign key FKB31AC193B8B275
alter table similarities drop foreign key FKB31AC1B470E5BE
alter table similaritytests drop foreign key FK86B2AD1EAE0697EB
alter table submissions drop foreign key FK2912EA7F27BD004
alter table submissions drop foreign key FK2912EA7AE0697EB
alter table submissions drop foreign key issuedby
alter table tasks drop foreign key FK6907B8EAF18EDD1
alter table test drop foreign key FK364492AE0697EB
drop table if exists groups
drop table if exists lectures
drop table if exists participations
drop table if exists similarities
drop table if exists similaritytests
drop table if exists submissions
drop table if exists tasks
drop table if exists test
drop table if exists users
create table groups (gid integer not null auto_increment, name varchar(255), lectureid integer not null, primary key (gid)) type=InnoDB
create table lectures (id integer not null auto_increment, name varchar(255), semester integer not null, primary key (id)) type=InnoDB
create table participations (id integer not null auto_increment, role varchar(255) not null, groupid integer, lectureid integer not null, uid integer not null, primary key (id), unique (lectureid, uid)) type=InnoDB
create table similarities (similarityid integer not null auto_increment, percentage integer not null, similarityTest_similarityTestId integer not null, submissionOne_submissionid integer not null, submissionTwo_submissionid integer not null, primary key (similarityid)) type=InnoDB
create table similaritytests (similarityTestId integer not null auto_increment, basis varchar(255) not null, minimumDifferenceInPercent integer not null, needsToRun bit not null, normalizeCapitalization bit not null, tabsSpacesNewlinesNormalization varchar(255) not null, type varchar(255) not null, taskid integer not null, primary key (similarityTestId)) type=InnoDB
create table submissions (submissionid integer not null auto_increment, compiles bit not null, points integer, stderr longtext, passedTest bit, testOutput longtext, issuedBy_id integer, submitter integer not null, taskid integer not null, primary key (submissionid), unique (submitter, taskid)) type=InnoDB
create table tasks (taskid integer not null auto_increment, deadline datetime not null, description longtext, maxPoints integer not null, showPoints datetime, start datetime not null, title varchar(255), lectureid integer not null, primary key (taskid)) type=InnoDB
create table test (DTYPE varchar(31) not null, id integer not null auto_increment, visibleToStudents bit not null, commandLineParameter varchar(255), mainClass varchar(255), regularExpression varchar(255), taskid integer not null unique, primary key (id)) type=InnoDB
create table users (uid integer not null auto_increment, email varchar(255) not null unique, firstName varchar(255) not null, lastName varchar(255) not null, password varchar(255), superUser bit not null, matrikelno integer, primary key (uid)) type=InnoDB
alter table groups add index FKB63DD9D4AF18EDD1 (lectureid), add constraint FKB63DD9D4AF18EDD1 foreign key (lectureid) references lectures (id) on delete cascade
alter table participations add index FKA301B52DDB37416 (id), add constraint FKA301B52DDB37416 foreign key (id) references participations (id)
alter table participations add index FKA301B52E28A1D21 (uid), add constraint FKA301B52E28A1D21 foreign key (uid) references users (uid)
alter table participations add index FKA301B527F3A8A13 (groupid), add constraint FKA301B527F3A8A13 foreign key (groupid) references groups (gid)
alter table participations add index FKA301B52AF18EDD1 (lectureid), add constraint FKA301B52AF18EDD1 foreign key (lectureid) references lectures (id) on delete cascade
alter table similarities add index FKB31AC117AAD798 (submissionTwo_submissionid), add constraint FKB31AC117AAD798 foreign key (submissionTwo_submissionid) references submissions (submissionid)
alter table similarities add index FKB31AC193B8B275 (similarityTest_similarityTestId), add constraint FKB31AC193B8B275 foreign key (similarityTest_similarityTestId) references similaritytests (similarityTestId)
alter table similarities add index FKB31AC1B470E5BE (submissionOne_submissionid), add constraint FKB31AC1B470E5BE foreign key (submissionOne_submissionid) references submissions (submissionid) on delete cascade
alter table similaritytests add index FK86B2AD1EAE0697EB (taskid), add constraint FK86B2AD1EAE0697EB foreign key (taskid) references tasks (taskid) on delete cascade
alter table submissions add index FK2912EA7F27BD004 (submitter), add constraint FK2912EA7F27BD004 foreign key (submitter) references participations (id)
alter table submissions add index FK2912EA7AE0697EB (taskid), add constraint FK2912EA7AE0697EB foreign key (taskid) references tasks (taskid) on delete cascade
alter table submissions add index issuedby (issuedBy_id), add constraint issuedby foreign key (issuedBy_id) references participations (id)
alter table tasks add index FK6907B8EAF18EDD1 (lectureid), add constraint FK6907B8EAF18EDD1 foreign key (lectureid) references lectures (id) on delete cascade
alter table test add index FK364492AE0697EB (taskid), add constraint FK364492AE0697EB foreign key (taskid) references tasks (taskid)
