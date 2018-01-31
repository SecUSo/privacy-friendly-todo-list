package org.secuso.privacyfriendlytodolist.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoList;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoSubTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by dominik on 19.05.16.
 *
 * This class encapsulates all sql statements and returns
 */
public class DBQueryHandler {

    private static final String TAG = DBQueryHandler.class.getSimpleName();

    public static final int NO_CHANGES = -2;

    public DatabaseHelper dbhelper;

    public static TodoTask getNextDueTask(SQLiteDatabase db, long today) {

        String rawQuery = "SELECT * FROM " + TTodoTask.TABLE_NAME + " WHERE " + TTodoTask.COLUMN_DONE + "=0 AND " + TTodoTask.COLUMN_DEADLINE_WARNING_TIME + " > 0 AND " + TTodoTask.COLUMN_TRASH + "=0 AND " + TTodoTask.COLUMN_DEADLINE_WARNING_TIME + "-? > 0 ORDER BY ABS(" + TTodoTask.COLUMN_DEADLINE_WARNING_TIME + " -?) LIMIT 1;";
        String selectionArgs[] = {String.valueOf(today)};

        TodoTask nextDueTask = null;

        try {
            Cursor cursor = db.rawQuery(rawQuery, selectionArgs);

            try {
                if (cursor.moveToFirst()) {
                    nextDueTask = extractTodoTask(cursor);
                }
            } finally {
                cursor.close();
            }
        } catch (Exception ex) {
        }

        return nextDueTask;
    }


    /**
     * returns a list of tasks
     *
     *  -   which are not fulfilled and whose reminder time is prior to the current time
     *  -   the task which is next due
     */
    public static ArrayList<TodoTask> getTasksToRemind(SQLiteDatabase db, long today, HashSet<Integer> lockedIds) {

        ArrayList<TodoTask> tasks = new ArrayList<>();

        // do not request tasks for which the user was just notified (these tasks are locked)
        StringBuilder excludedIDs = new StringBuilder();
        String and = " AND ";
        if (lockedIds != null && lockedIds.size() > 0) {
            excludedIDs.append(" AND ");
            for (Integer lockedTaskID : lockedIds) {
                excludedIDs.append(TTodoTask.COLUMN_ID + " <> " + String.valueOf(lockedTaskID));
                excludedIDs.append(" AND ");
            }
            excludedIDs.setLength(excludedIDs.length() - and.length());
        }
        excludedIDs.append(";");

        String rawQuery = "SELECT * FROM " + TTodoTask.TABLE_NAME + " WHERE " + TTodoTask.COLUMN_DONE + " = 0 AND " + TTodoTask.COLUMN_TRASH + "=0 AND " + TTodoTask.COLUMN_DEADLINE_WARNING_TIME + " > 0 AND " + TTodoTask.COLUMN_DEADLINE_WARNING_TIME + " <= ? " + excludedIDs.toString();
        String selectionArgs[] = {String.valueOf(today)};
        try {
            Cursor cursor = db.rawQuery(rawQuery, selectionArgs);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        TodoTask taskForNotification = extractTodoTask(cursor);
                        tasks.add(taskForNotification);

                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        } catch (Exception e) {
        }

        // get task that is next due
        TodoTask nextDueTask = getNextDueTask(db, today);
        if (nextDueTask != null)
            tasks.add(nextDueTask);

        return tasks;
    }

    private static TodoTask extractTodoTask(Cursor cursor) {

        int id = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_ID));
        int listPosition = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_LIST_POSITION));
        String title = cursor.getString(cursor.getColumnIndex(TTodoTask.COLUMN_NAME));
        String description = cursor.getString(cursor.getColumnIndex(TTodoTask.COLUMN_DESCRIPTION));
        boolean done = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_DONE)) > 0;
        int progress = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_PROGRESS));
        int deadline = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_DEADLINE));
        int reminderTime = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_DEADLINE_WARNING_TIME));
        int priority = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_PRIORITY));
        int listID = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_TODO_LIST_ID));
        boolean inTrash = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_TRASH)) > 0;

        TodoTask task = new TodoTask();
        task.setName(title);
        task.setDeadline(deadline);
        task.setDescription(description);
        task.setPriority(TodoTask.Priority.fromInt(priority));
        task.setReminderTime(reminderTime);
        task.setId(id);
        task.setPositionInList(listPosition);
        task.setProgress(progress);
        task.setDone(done);
        task.setListId(listID);
        task.setInTrash(inTrash);

        return task;
    }

    public enum ObjectStates {
        INSERT_TO_DB,
        UPDATE_DB,
        NO_DB_ACTION
    }

    public static void deleteTodoList(SQLiteDatabase db, TodoList todoList) {

        long id = todoList.getId();

        int deletedTasks = 0;
        for(TodoTask task : todoList.getTasks())
            deletedTasks+=putTaskInTrash(db, task);
        Log.i(TAG, deletedTasks + " tasks put into trash");

        String where = TTodoList.COLUMN_ID + "=?";
        String whereArgs[] = {String.valueOf(id)};
        int deletedLists = db.delete(TTodoList.TABLE_NAME, where, whereArgs);

        Log.i(TAG, deletedLists + " lists removed from database");
    }

    public static int deleteTodoTask(SQLiteDatabase db, TodoTask todoTask) {

        long id = todoTask.getId();

        int removedSubTask = 0;
        for(TodoSubTask subTask : todoTask.getSubTasks())
            removedSubTask += deleteTodoSubTask(db, subTask);

        Log.i(TAG, removedSubTask + " subtasks removed from database");

        String where = TTodoTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};
        return db.delete(TTodoTask.TABLE_NAME, where, whereArgs);
    }

    public static ArrayList<TodoTask> getAllToDoTasks (SQLiteDatabase db) {
        ArrayList<TodoTask> todo = new ArrayList<>();

        String where = TTodoTask.COLUMN_TRASH + " =0";

        try {
            Cursor c = db.query(TTodoTask.TABLE_NAME, null, where, null, null, null, null);
            try {
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex(TTodoTask.COLUMN_ID));
                        String taskName = c.getString(c.getColumnIndex(TTodoTask.COLUMN_NAME));
                        String taskDescription = c.getString(c.getColumnIndex(TTodoTask.COLUMN_DESCRIPTION));
                        int progress = c.getInt(c.getColumnIndex(TTodoTask.COLUMN_PROGRESS));
                        long deadline = c.getLong(c.getColumnIndex(TTodoTask.COLUMN_DEADLINE));

                        TodoTask currentTask = new TodoTask();
                        currentTask.setName(taskName);
                        currentTask.setDescription(taskDescription);
                        currentTask.setId(id);
                        currentTask.setSubTasks(getSubTasksByTaskId(db, id));
                        currentTask.setProgress(progress);
                        currentTask.setDeadline(deadline);
                        //currentTask.setPriority(priority);
                        //currentTask.setDone(done);
                        todo.add(currentTask);
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
                }
            } catch (Exception ex) {
        }
        return todo;
    }


    public static ArrayList<TodoTask> getBin (SQLiteDatabase db) {
        ArrayList<TodoTask> todo = new ArrayList<>();

        String where = TTodoTask.COLUMN_TRASH + " =1";

        try {
            Cursor c = db.query(TTodoTask.TABLE_NAME, null, where, null, null, null, null);
            try {
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex(TTodoTask.COLUMN_ID));
                        String taskName = c.getString(c.getColumnIndex(TTodoTask.COLUMN_NAME));
                        String taskDescription = c.getString(c.getColumnIndex(TTodoTask.COLUMN_DESCRIPTION));

                        TodoTask currentTask = new TodoTask();
                        currentTask.setName(taskName);
                        currentTask.setDescription(taskDescription);
                        currentTask.setId(id);
                        currentTask.setSubTasks(getSubTasksByTaskId(db, id));
                        todo.add(currentTask);
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        } catch (Exception ex) {

        }
        return todo;
    }


    public static ArrayList<TodoList> getAllToDoLists (SQLiteDatabase db) {

        ArrayList<TodoList> todoLists = new ArrayList<>();

        try {
            Cursor cursor = db.query(TTodoList.TABLE_NAME, null, null, null, null, null, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(TTodoList.COLUMN_ID));
                        String listName = cursor.getString(cursor.getColumnIndex(TTodoList.COLUMN_NAME));
                        String listDescription = cursor.getString(cursor.getColumnIndex(TTodoList.COLUMN_DESCRIPTION));

                        TodoList currentList = new TodoList();
                        currentList.setName(listName);
                        currentList.setDescription(listDescription);
                        currentList.setId(id);
                        currentList.setTasks(getTasksByListId(db, id, listName));
                        todoLists.add(currentList);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        } catch (Exception ex) {
        }

        return todoLists;
    }

    private static ArrayList<TodoTask> getTasksByListId(SQLiteDatabase db, int listId, String listName) {

        ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();
        String where = TTodoTask.COLUMN_TODO_LIST_ID + " = " + listId + " AND " + TTodoTask.COLUMN_TRASH + "=0";
        Cursor cursor = db.query(TTodoTask.TABLE_NAME, null, where, null, null, null, null);

        try {
            if(cursor.moveToFirst()) {
                do {

                    TodoTask currentTask = extractTodoTask(cursor);
                    currentTask.setListName(listName);
                    currentTask.setSubTasks(getSubTasksByTaskId(db, currentTask.getId()));
                    tasks.add(currentTask);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return tasks;
    }

    private static ArrayList<TodoSubTask> getSubTasksByTaskId(SQLiteDatabase db, long taskId) {

        ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();
        String where = TTodoSubTask.COLUMN_TASK_ID + " = " + taskId + " AND " + TTodoSubTask.COLUMN_TRASH + "=0";
        Cursor cursor = db.query(TTodoSubTask.TABLE_NAME, null, where, null, null, null, null);
        cursor.moveToFirst();

        try {
            if(cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(TTodoSubTask.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(TTodoSubTask.COLUMN_TITLE));
                    boolean done = cursor.getInt(cursor.getColumnIndex(TTodoSubTask.COLUMN_DONE)) > 0;
                    boolean trash = cursor.getInt(cursor.getColumnIndex(TTodoSubTask.COLUMN_TRASH)) > 0;

                    TodoSubTask currentSubTask = new TodoSubTask();
                    currentSubTask.setId(id);
                    currentSubTask.setName(title);
                    currentSubTask.setDone(done);
                    currentSubTask.setTaskId(taskId);
                    currentSubTask.setInTrash(trash);
                    subTasks.add(currentSubTask);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return subTasks;
    }

    public static int saveTodoSubTaskInDb(SQLiteDatabase db, TodoSubTask subTask) {

        int returnCode;

        if(subTask.getDBState() != ObjectStates.NO_DB_ACTION) {
            ContentValues values = new ContentValues();
            values.put(TTodoSubTask.COLUMN_TITLE, subTask.getName());
            values.put(TTodoSubTask.COLUMN_DONE, subTask.getDone());
            values.put(TTodoSubTask.COLUMN_TASK_ID, subTask.getTaskId());
            values.put(TTodoSubTask.COLUMN_TRASH, subTask.isInTrash());

            if(subTask.getDBState() == ObjectStates.INSERT_TO_DB) {
                returnCode = (int) db.insert(TTodoSubTask.TABLE_NAME, null, values);
                Log.d(TAG, "Todo subtask " + subTask.getName() + " was inserted into the database (return code: "+returnCode+").");
            } else if(subTask.getDBState() == ObjectStates.UPDATE_DB) {
                String whereClause = TTodoSubTask.COLUMN_ID + "=?";
                String[] whereArgs = {String.valueOf(subTask.getId())};
                db.update(TTodoSubTask.TABLE_NAME, values, whereClause, whereArgs);
                returnCode = subTask.getId();
                Log.d(TAG, "Todo subtask " + subTask.getName() + " was updated (return code: "+returnCode+").");
            } else
                returnCode = NO_CHANGES;

            subTask.setUnchanged();
            //subTask.setDbState(ObjectStates.NO_DB_ACTION);
        } else {
            returnCode = NO_CHANGES;
        }

        return returnCode;
    }

    public static int saveTodoTaskInDb(SQLiteDatabase db, TodoTask todoTask) {

        int returnCode;

        if(todoTask.getDBState() != ObjectStates.NO_DB_ACTION) {

            ContentValues values = new ContentValues();
            values.put(TTodoTask.COLUMN_NAME, todoTask.getName());
            values.put(TTodoTask.COLUMN_DESCRIPTION, todoTask.getDescription());
            values.put(TTodoTask.COLUMN_PROGRESS, todoTask.getProgress());
            values.put(TTodoTask.COLUMN_DEADLINE, todoTask.getDeadline());
            values.put(TTodoTask.COLUMN_DEADLINE_WARNING_TIME, todoTask.getReminderTime());
            values.put(TTodoTask.COLUMN_PRIORITY, todoTask.getPriority().getValue());
            values.put(TTodoTask.COLUMN_TODO_LIST_ID, todoTask.getListId());
            values.put(TTodoTask.COLUMN_LIST_POSITION, todoTask.getListPosition());
            values.put(TTodoTask.COLUMN_DONE, todoTask.getDone());
            values.put(TTodoTask.COLUMN_TRASH, todoTask.isInTrash());

            if(todoTask.getDBState() == ObjectStates.INSERT_TO_DB) {
                returnCode = (int) db.insert(TTodoTask.TABLE_NAME, null, values);
                Log.d(TAG, "Todo task " + todoTask.getName() + " was inserted into the database (return code: "+returnCode+").");
            } else if(todoTask.getDBState() == ObjectStates.UPDATE_DB) {
                String whereClause = TTodoTask.COLUMN_ID + "=?";
                String[] whereArgs = {String.valueOf(todoTask.getId())};
                db.update(TTodoTask.TABLE_NAME, values, whereClause, whereArgs);
                returnCode = todoTask.getId();
                Log.d(TAG, "Todo task " + todoTask.getName() + " was updated (return code: "+returnCode+").");
            } else
                returnCode = NO_CHANGES;

            todoTask.setUnchanged();
            //todoTask.setDbState(ObjectStates.NO_DB_ACTION);

        } else
          returnCode = NO_CHANGES;

        return returnCode;
    }

    // returns the id of the todolist
    public static int saveTodoListInDb(SQLiteDatabase db, TodoList todoList) {

        int returnCode;

        // Log.i(TAG, "Changes of list " + currentList.getName() + " were stored in the database.");

        if(todoList.getDBState() != ObjectStates.NO_DB_ACTION) {
            ContentValues values = new ContentValues();
            values.put(TTodoList.COLUMN_NAME, todoList.getName());
            values.put(TTodoList.COLUMN_DESCRIPTION, todoList.getDescription());

            if(todoList.getDBState() == ObjectStates.INSERT_TO_DB) {
                returnCode = (int) db.insert(TTodoList.TABLE_NAME, null, values);
                Log.d(TAG, "Todo list " + todoList.getName() + " was inserted into the database (return code: "+returnCode+").");
            } else if(todoList.getDBState() == ObjectStates.UPDATE_DB) {
                String whereClause = TTodoList.COLUMN_ID + "=?";
                String[] whereArgs = {String.valueOf(todoList.getId())};
                db.update(TTodoList.TABLE_NAME, values, whereClause, whereArgs);
                returnCode =  todoList.getId();
                Log.d(TAG, "Todo list " + todoList.getName() + " was updated (return code: "+returnCode+").");
            } else
                returnCode = NO_CHANGES;

            todoList.setUnchanged();
            //todoList.setDbState(ObjectStates.NO_DB_ACTION);
        } else {
            returnCode = NO_CHANGES;
        }

        return returnCode;
    }

    public static int deleteTodoSubTask(SQLiteDatabase db, TodoSubTask subTask) {
        long id = subTask.getId();

        String where = TTodoSubTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};
        return db.delete(TTodoSubTask.TABLE_NAME, where, whereArgs);
    }

    public static int putTaskInTrash(SQLiteDatabase db, TodoTask todoTask) {
        long id = todoTask.getId();
        ContentValues args = new ContentValues();
        args.put(TTodoTask.COLUMN_TRASH, 1);

        int removedSubTask = 0;
        for(TodoSubTask subTask : todoTask.getSubTasks())
            removedSubTask += putSubtaskInTrash(db, subTask);

        Log.i(TAG, removedSubTask + " subtasks put into bin");

        String where = TTodoTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};

        return db.update(TTodoTask.TABLE_NAME, args, where, whereArgs);
    }

    public static int putSubtaskInTrash(SQLiteDatabase db, TodoSubTask subTask) {
        long id = subTask.getId();

        ContentValues args = new ContentValues();
        args.put(TTodoSubTask.COLUMN_TRASH, 1);

        String where = TTodoSubTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};
        return db.update(TTodoSubTask.TABLE_NAME, args, where, whereArgs);
    }
}
