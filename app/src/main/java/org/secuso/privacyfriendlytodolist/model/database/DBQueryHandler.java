package org.secuso.privacyfriendlytodolist.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoList;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoSubTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;

import java.util.ArrayList;

/**
 * Created by dominik on 19.05.16.
 *
 * This class encapsulates all sql statements and returns
 */
public class DBQueryHandler {

    private static final String TAG = DBQueryHandler.class.getSimpleName();

    public static final long NO_CHANGES = -2;

    public enum ObjectStates {
        INSERT_TO_DB,
        UPDATE_DB,
        NO_DB_ACTION
    }

    public static void deleteTodoList(SQLiteDatabase db, TodoList todoList) {

        long id = todoList.getId();

        int deletedTasks = 0;
        for(TodoTask task : todoList.getTasks())
            deletedTasks+=deleteTodoTask(db, task);
        Log.i(TAG, deletedTasks + " tasks removed from the database.");

        String where = TTodoList.COLUMN_ID + "=?";
        String whereArgs[] = {String.valueOf(id)};
        db.delete(TTodoList.TABLE_NAME, where, whereArgs);
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

    public static ArrayList<TodoList> getAllToDoLists (SQLiteDatabase db) {

        ArrayList<TodoList> todoLists = new ArrayList<>();
        Cursor cursor = db.query(TTodoList.TABLE_NAME, null, null, null, null, null, null);


        try {
            if(cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(TTodoList.COLUMN_ID));
                    String listName = cursor.getString(cursor.getColumnIndex(TTodoList.COLUMN_NAME));
                    String listDescription = cursor.getString(cursor.getColumnIndex(TTodoList.COLUMN_DESCRIPTION));

                    TodoList currentList = new TodoList();
                    currentList.setName(listName);
                    currentList.setDescription(listDescription);
                    currentList.setId(id);
                    currentList.setTasks(getTasksByListId(db, id));
                    todoLists.add(currentList);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        
        return todoLists;

    }

    private static ArrayList<TodoTask> getTasksByListId(SQLiteDatabase db, int listId) {

        ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();
        String where = TTodoTask.COLUMN_TODO_LIST_ID + " = " + listId;
        Cursor cursor = db.query(TTodoTask.TABLE_NAME, null, where, null, null, null, null);

        try {
            if(cursor.moveToFirst()) {
                do {

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

                    TodoTask currentTask = new TodoTask();
                    currentTask.setName(title);
                    currentTask.setDeadline(deadline);
                    currentTask.setDescription(description);
                    currentTask.setPriority(TodoTask.Priority.fromInt(priority));
                    currentTask.setReminderTime(reminderTime);
                    currentTask.setId(id);
                    currentTask.setPositionInList(listPosition);
                    currentTask.setSubTasks(getSubTasksByTaskId(db, id));
                    currentTask.setProgress(progress);
                    currentTask.setDone(done);
                    currentTask.setListId(listID);
                    tasks.add(currentTask);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return tasks;
    }

    private static ArrayList<TodoSubTask> getSubTasksByTaskId(SQLiteDatabase db, int taskId) {

        ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();
        String where = TTodoSubTask.COLUMN_TASK_ID + " = " + taskId;
        Cursor cursor = db.query(TTodoSubTask.TABLE_NAME, null, where, null, null, null, null);
        cursor.moveToFirst();

        try {
            if(cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(TTodoSubTask.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(TTodoSubTask.COLUMN_TITLE));
                    boolean done = cursor.getInt(cursor.getColumnIndex(TTodoSubTask.COLUMN_DONE)) > 0;

                    TodoSubTask currentSubTask = new TodoSubTask();
                    currentSubTask.setId(id);
                    currentSubTask.setName(title);
                    currentSubTask.setDone(done);
                    currentSubTask.setTaskId(taskId);
                    subTasks.add(currentSubTask);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return subTasks;
    }

    public static long saveTodoSubTaskInDb(SQLiteDatabase db, TodoSubTask subTask) {

        long returnCode;

        ContentValues values = new ContentValues();
        values.put(TTodoSubTask.COLUMN_TITLE, subTask.getName());
        values.put(TTodoSubTask.COLUMN_DONE, subTask.getDone());
        values.put(TTodoSubTask.COLUMN_TASK_ID, subTask.getTaskId());

        if(subTask.getDBState() == ObjectStates.INSERT_TO_DB) {
            returnCode = db.insert(TTodoSubTask.TABLE_NAME, null, values);
        } else if(subTask.getDBState() == ObjectStates.UPDATE_DB) {
            String whereClause = TTodoSubTask.COLUMN_ID + "=?";
            String[] whereArgs = {String.valueOf(subTask.getId())};
            db.update(TTodoSubTask.TABLE_NAME, values, whereClause, whereArgs);
            returnCode = subTask.getId();
        } else
            returnCode = NO_CHANGES;

        subTask.setDbState(ObjectStates.NO_DB_ACTION);

        return returnCode;

    }

    public static long saveTodoTaskInDb(SQLiteDatabase db, TodoTask todoTask) {

        long returnCode;

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

        if(todoTask.getDBState() == ObjectStates.INSERT_TO_DB) {
            returnCode = db.insert(TTodoTask.TABLE_NAME, null, values);
        } else if(todoTask.getDBState() == ObjectStates.UPDATE_DB) {
            String whereClause = TTodoTask.COLUMN_ID + "=?";
            String[] whereArgs = {String.valueOf(todoTask.getId())};
            db.update(TTodoTask.TABLE_NAME, values, whereClause, whereArgs);
            returnCode = todoTask.getId();
        } else
            returnCode = NO_CHANGES;

        todoTask.setDbState(ObjectStates.NO_DB_ACTION);

        return returnCode;
    }

    // returns the id of the todolist
    public static long saveTodoListInDb(SQLiteDatabase db, TodoList todoList) {

        long returnCode;

        ContentValues values = new ContentValues();
        values.put(TTodoList.COLUMN_NAME, todoList.getName());
        values.put(TTodoList.COLUMN_DESCRIPTION, todoList.getDescription());

        if(todoList.getDBState() == ObjectStates.INSERT_TO_DB) {
            returnCode = db.insert(TTodoList.TABLE_NAME, null, values);
        } else if(todoList.getDBState() == ObjectStates.UPDATE_DB) {
            String whereClause = TTodoList.COLUMN_ID + "=?";
            String[] whereArgs = {String.valueOf(todoList.getId())};
            db.update(TTodoList.TABLE_NAME, values, whereClause, whereArgs);
            returnCode =  todoList.getId();
        } else
            returnCode = NO_CHANGES;

        todoList.setDbState(ObjectStates.NO_DB_ACTION);

        return returnCode;
    }

    public static int deleteTodoSubTask(SQLiteDatabase db, TodoSubTask subTask) {
        long id = subTask.getId();

        String where = TTodoSubTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};
        return db.delete(TTodoSubTask.TABLE_NAME, where, whereArgs);
    }
}
