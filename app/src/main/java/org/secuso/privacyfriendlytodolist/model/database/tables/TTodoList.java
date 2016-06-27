package org.secuso.privacyfriendlytodolist.model.database.tables;

public final class TTodoList {

	private static final String TAG = TTodoList.class.getSimpleName();

	// columns + tablename
	public static final String TABLE_NAME = "todo_list";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESCRIPTION = "description";

	// sql table creation
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
	 	" INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT NOT NULL, " + COLUMN_DESCRIPTION + " TEXT NOT NULL);";

}
