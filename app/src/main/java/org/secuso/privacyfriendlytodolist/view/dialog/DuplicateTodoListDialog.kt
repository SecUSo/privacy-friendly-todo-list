/*
Privacy Friendly To-Do List
Copyright (C) 2026  Christian Adams

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlytodolist.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoList

/**
 * Business logic for the duplicate-to-do-list-dialog.
 */
class DuplicateTodoListDialog(context: Context, private val todoList: TodoList) :
    FullScreenDialog<ResultCallback<TodoList>>(context, R.layout.list_duplicate_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val etName = findViewById<EditText>(R.id.et_todo_list_name)
        val cbResetTasks = findViewById<CheckBox>(R.id.cb_reset_tasks)
        val cbSubtasksToo = findViewById<CheckBox>(R.id.cb_subtasks_too)
        val buttonOkay = findViewById<Button>(R.id.bt_todo_list_ok)
        val buttonCancel = findViewById<Button>(R.id.bt_todo_list_cancel)

        // Request focus for first input field.
        etName.requestFocus()
        // Show soft-keyboard
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val originalListName = todoList.getName()
        etName.setText(originalListName)
        etName.setSelection(originalListName.length)

        buttonOkay.setOnClickListener {
            val newListName = etName.text.toString()
            if (newListName.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.list_name_must_not_be_empty),
                    Toast.LENGTH_SHORT).show()
            } else {
                val newTodoList = todoList.deepCopy(cbResetTasks.isChecked, cbSubtasksToo.isChecked)
                newTodoList.setName(newListName)
                getDialogCallback().onFinish(newTodoList)
                dismiss()
            }
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }
    }
}
