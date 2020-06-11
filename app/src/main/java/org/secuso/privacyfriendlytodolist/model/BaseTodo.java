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

package org.secuso.privacyfriendlytodolist.model;

import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;

public abstract class BaseTodo {

    protected int id;

    protected String name, description;
    protected DBQueryHandler.ObjectStates dbState;

    public BaseTodo() {
        dbState = DBQueryHandler.ObjectStates.NO_DB_ACTION;
    }

    public DBQueryHandler.ObjectStates getDBState() {
        return dbState;
    }

    public void setDBState(DBQueryHandler.ObjectStates newState) {
        dbState = newState;
    }

    public void setCreated() {
        this.dbState = DBQueryHandler.ObjectStates.INSERT_TO_DB;
    }

    public void setChanged() {
        if(this.dbState == DBQueryHandler.ObjectStates.NO_DB_ACTION)
            this.dbState = DBQueryHandler.ObjectStates.UPDATE_DB;
    }

    public void setUnchanged() {
        this.dbState = DBQueryHandler.ObjectStates.NO_DB_ACTION;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
