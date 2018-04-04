-- drop column description from todo_list
PRAGMA foreign_keys=off;
BEGIN TRANSACTION;
ALTER TABLE todo_list RENAME TO temp_table;
CREATE TABLE todo_list (
	_id	INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL
);
INSERT INTO todo_list (_id, name)
  SELECT _id, name
  FROM temp_table;
DROP TABLE temp_table;
COMMIT;
PRAGMA foreign_keys=on;

ALTER TABLE todo_task ADD in_trash INTEGER NOT NULL DEFAULT 0;

ALTER TABLE todo_subtask ADD in_trash INTEGER NOT NULL DEFAULT 0;