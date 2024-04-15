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
import org.secuso.privacyfriendlytodolist.model.Model.createNewTodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoSubtask

/**
 * This class shows a dialog that lets the user create/edit a subtask.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ProcessTodoSubtaskDialog : FullScreenDialog<ResultCallback<TodoSubtask>> {
    private var subtask: TodoSubtask
    private lateinit var dialogTitleNew: TextView
    private lateinit var dialogTitleEdit: TextView
    private var isTitleEdit = false

    constructor(context: Context) :
            super(context, R.layout.add_subtask_dialog) {
        subtask = createNewTodoSubtask()
    }

    constructor(context: Context, todoSubtask: TodoSubtask) :
            super(context, R.layout.add_subtask_dialog) {
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

        //initialize titles of the dialog
        dialogTitleNew = findViewById(R.id.dialog_subtitle)
        dialogTitleEdit = findViewById(R.id.dialog_edit_sub)
        if (isTitleEdit) {
            isTitleEdit = false
            titleEdit()
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

    fun titleEdit() {
        if (::dialogTitleNew.isInitialized) {
            dialogTitleNew.visibility = View.GONE
            dialogTitleEdit.visibility = View.VISIBLE
        } else {
            isTitleEdit = true
        }
    }
}
