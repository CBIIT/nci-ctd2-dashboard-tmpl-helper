ALTER TABLE submission_template
MODIFY COLUMN description varchar(1024) DEFAULT NULL,
MODIFY COLUMN project varchar(1024) DEFAULT NULL,
MODIFY COLUMN storyTitle varchar(1024) DEFAULT NULL,
MODIFY COLUMN piName varchar(64) DEFAULT NULL,
MODIFY COLUMN summary varchar(1024) DEFAULT NULL;