package org.secuso.privacyfriendlytodolist.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public enum ObjectStates {
        INSERT_TO_DB,
        UPDATE_DB,
        NO_DB_ACTION
    }

    public static void deleteTodoList(SQLiteDatabase db, TodoList todoList) {

        long id = todoList.getId();

        // TODO make one transaction

        for(TodoTask task : todoList.getTasks())
            deleteTodoTask(db, task);

        String where = TTodoList.COLUMN_ID + "=?";
        String whereArgs[] = {String.valueOf(id)};
        db.delete(TTodoList.TABLE_NAME, where, whereArgs);
    }

    public static void deleteTodoTask(SQLiteDatabase db, TodoTask todoTask) {

        long id = todoTask.getId();

        for(TodoSubTask subTask : todoTask.getSubTasks())
            deleteTodoSubTask(db, subTask);

        String where = TTodoTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};
        db.delete(TTodoTask.TABLE_NAME, where, whereArgs);
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
                    long deadline = cursor.getLong(cursor.getColumnIndex(TTodoList.COLUMN_DEADLINE));

                    TodoList currentList = new TodoList(listName, listDescription, deadline);
                    currentList.setId(id);
                    currentList.setTasks(getTasksByList(db, id));
                    todoLists.add(currentList);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        
        return todoLists;

    }

    private static ArrayList<TodoTask> getTasksByList(SQLiteDatabase db, int listId) {

        ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();
        String where = TTodoTask.COLUMN_TODO_LIST_ID + " = " + listId;
        Cursor cursor = db.query(TTodoTask.TABLE_NAME, null, where, null, null, null, null);

        try {
            if(cursor.moveToFirst()) {
                do {

                    int id = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_ID));
                    int listPosition = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_LIST_POSITION));
                    String title = cursor.getString(cursor.getColumnIndex(TTodoTask.COLUMN_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(TTodoTask.COLUMN_TITLE));
                    boolean done = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_DONE)) > 0;
                    int progress = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_PROGRESS));
                    int deadline = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_DEADLINE));
                    int reminderTime = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_DEADLINE_WARNING_TIME));
                    int priority = cursor.getInt(cursor.getColumnIndex(TTodoTask.COLUMN_PRIORITY));

                    TodoTask currentTask = new TodoTask(title, description, progress, TodoTask.Priority.fromInt(priority), deadline, reminderTime);
                    currentTask.setId(id);
                    currentTask.setPositionInList(listPosition);
                    currentTask.setSubTasks(getSubTasksByTask(db, id));
                    currentTask.setDone(done);
                    tasks.add(currentTask);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return tasks;
    }

    private static ArrayList<TodoSubTask> getSubTasksByTask(SQLiteDatabase db, int taskId) {

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

                    TodoSubTask currentSubTask = new TodoSubTask(id, title, done);
                    subTasks.add(currentSubTask);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }

        return subTasks;
    }

    public static long insertNewTask(SQLiteDatabase db, TodoTask newTask) {
        ContentValues values = new ContentValues();
        values.put(TTodoTask.COLUMN_TITLE, newTask.getName());
        values.put(TTodoTask.COLUMN_DESCRIPTION, newTask.getDescription());
        values.put(TTodoTask.COLUMN_PROGRESS, newTask.getProgress());
        values.put(TTodoTask.COLUMN_DEADLINE, newTask.getDeadlineTs());
        values.put(TTodoTask.COLUMN_DEADLINE_WARNING_TIME, newTask.getReminderTime());
        values.put(TTodoTask.COLUMN_PRIORITY, newTask.getPriority().getValue());
        values.put(TTodoTask.COLUMN_TODO_LIST_ID, newTask.getListId());
        values.put(TTodoTask.COLUMN_LIST_POSITION, newTask.getListPosition());

        if(newTask.getSubTasks() != null) {
            // TODO
        }

        return db.insert(TTodoTask.TABLE_NAME, null, values);
    }

    // returns the id of the todolist
    public static long saveTodoListInDb(SQLiteDatabase db, TodoList todoList) {

        ContentValues values = new ContentValues();
        values.put(TTodoList.COLUMN_NAME, todoList.getName());
        values.put(TTodoList.COLUMN_DESCRIPTION, todoList.getDescription());
        values.put(TTodoList.COLUMN_DEADLINE, todoList.getDeadline());

        if(todoList.getDBState() == ObjectStates.INSERT_TO_DB) {

            if(todoList.getTasks() != null) {
                // TODO insert new tasks and subtasks (make sure to do this in one transaction)
            }
            return db.insert(TTodoList.TABLE_NAME, null, values);
        } else if(todoList.getDBState() == ObjectStates.UPDATE_DB) {
            String whereClause = TTodoList.COLUMN_ID + "=?";
            String[] whereArgs = {String.valueOf(todoList.getId())};
            db.update(TTodoList.TABLE_NAME, values, whereClause, whereArgs);
            return todoList.getId();
        }

        return -1;
    }

    public static void deleteTodoSubTask(SQLiteDatabase db, TodoSubTask subTask) {
        long id = subTask.getId();

        String where = TTodoSubTask.COLUMN_ID + " = ?";
        String whereArgs[] = {String.valueOf(id)};
        db.delete(TTodoSubTask.TABLE_NAME, where, whereArgs);
    }
}
