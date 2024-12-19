/*
Privacy Friendly To-Do List
Copyright (C) 2016-2024  Dominik Puellen

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
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model.createNewTodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoSubtask

/**
 * This class shows a dialog that lets the user create/edit a subtask.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ProcessTodoSubtaskDialog : FullScreenDialog<ResultCallback<TodoSubtask>> {
    private val editExistingSubtask: Boolean
    private val subtask: TodoSubtask

    constructor(context: Context) :
            super(context, R.layout.subtask_dialog) {
        editExistingSubtask = false
        subtask = createNewTodoSubtask()
    }

    constructor(context: Context, todoSubtask: TodoSubtask) :
            super(context, R.layout.subtask_dialog) {
        editExistingSubtask = true
        subtask = todoSubtask
        subtask.setChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initGui()
    }

    private fun initGui() {
        val etSubtaskName: EditText = findViewById(R.id.et_new_subtask_name)
        val okButton: Button = findViewById(R.id.bt_new_subtask_ok)
        val cancelButton: Button = findViewById(R.id.bt_new_subtask_cancel)

        // initialize title of the dialog
        if (editExistingSubtask) {
            findViewById<Toolbar>(R.id.subtask_dialog_title).setTitle(R.string.edit_subtask)
        }

        // Request focus for first input field.
        etSubtaskName.requestFocus()
        // Show soft-keyboard
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        etSubtaskName.setText(subtask.getName())
        okButton.setOnClickListener { v: View? ->
            val name = etSubtaskName.getText().toString()
            if (name.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.todo_name_must_not_be_empty),
                    Toast.LENGTH_SHORT).show()
            } else {
                subtask.setName(name)
                getDialogCallback().onFinish(subtask)
                dismiss()
            }
        }
        cancelButton.setOnClickListener { dismiss() }
    }
}
