/*
 This file is part of Privacy Friendly To-Do List.

 Privacy Friendly To-Do List is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do List is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do List. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytodolist.model.database.tables;

/**
 * This class is responsible for creating sql table of To-Do lists.
 */

public final class TTodoList {

	private static final String TAG = TTodoList.class.getSimpleName();

	// columns + tablename
	public static final String TABLE_NAME = "todo_list";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";

	// sql table creation
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
	 	" INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT NOT NULL);";

}
