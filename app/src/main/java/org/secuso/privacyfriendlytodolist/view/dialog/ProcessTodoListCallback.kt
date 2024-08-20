package org.secuso.privacyfriendlytodolist.view.dialog

import org.secuso.privacyfriendlytodolist.model.TodoList

fun interface ProcessTodoListCallback {
    enum class Action {
        APPLY, EXPORT, DELETE
    }
    fun onFinish(todoList: TodoList, action: Action)
}
