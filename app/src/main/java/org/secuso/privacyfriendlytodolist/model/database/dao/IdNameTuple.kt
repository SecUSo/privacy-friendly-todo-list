package org.secuso.privacyfriendlytodolist.model.database.dao

import androidx.room.ColumnInfo

data class IdNameTuple(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String
)