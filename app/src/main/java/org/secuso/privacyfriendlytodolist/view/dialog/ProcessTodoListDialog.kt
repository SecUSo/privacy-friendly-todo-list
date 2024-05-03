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
package org.secuso.privacyfriendlytodolist.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.TodoList

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Defines the dialog that lets the user create a list
 */
class ProcessTodoListDialog(context: Context) :
    FullScreenDialog<ResultCallback<TodoList?>>(context, R.layout.add_todolist_dialog) {
    private lateinit var todoList: TodoList
    private var editExistingList = false

    constructor(context: Context, todoList: TodoList) : this(context) {
        this.todoList = todoList
        editExistingList = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tvTitle = findViewById<TextView>(R.id.tv_todo_list_title)
        val etName = findViewById<EditText>(R.id.et_todo_list_name)
        val buttonOkay = findViewById<Button>(R.id.bt_todo_list_ok)
        val buttonDelete = findViewById<Button>(R.id.bt_todo_list_delete)
        val buttonCancel = findViewById<Button>(R.id.bt_todo_list_cancel)

        // Request focus for first input field.
        etName.requestFocus()
        // Show soft-keyboard
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        if (editExistingList) {
            tvTitle.text = context.getString(R.string.edit_todo_list)
            etName.setText(todoList.getName())
            etName.selectAll()
        } else {
            todoList = Model.createNewTodoList()
            buttonDelete.visibility = View.GONE
        }

        buttonOkay.setOnClickListener {
            // prepare list data
            val listName = etName.getText().toString()
            if (listName.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.list_name_must_not_be_empty),
                    Toast.LENGTH_SHORT).show()
            } else {
                // check if real changes were made
                if (todoList.getName() != listName) {
                    todoList.setName(listName)
                    getDialogCallback().onFinish(todoList)
                }
                dismiss()
            }
        }

        buttonDelete.setOnClickListener {
            getDialogCallback().onFinish(null)
            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }
    }
}
