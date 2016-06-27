package org.secuso.privacyfriendlytodolist.model;

import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;

public abstract class BaseTodo {

    protected String name, description;
    protected DBQueryHandler.ObjectStates dbState;


    public DBQueryHandler.ObjectStates getDBState() {
        return dbState;
    }

    public void setDbState(DBQueryHandler.ObjectStates dbState) {
        this.dbState = dbState;
    }

    public void setName(String name) {
        this.name = name;
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
