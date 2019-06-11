ALTER TABLE submission_template
MODIFY COLUMN description varchar(1024) DEFAULT NULL,
MODIFY COLUMN summary varchar(1024) DEFAULT NULL;