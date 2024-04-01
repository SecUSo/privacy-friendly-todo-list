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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager

abstract class FullScreenDialog<T>(context: Context, private val layoutId: Int) : Dialog(context) {
    private var dialogCallback: T? = null

    fun setDialogCallback(dialogCallback: T) {
        this.dialogCallback = dialogCallback
    }

    fun getDialogCallback(): T {
        return dialogCallback!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)

        // required to make the dialog use the full screen width
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.horizontalMargin = 40f
        window!!.setAttributes(lp)
    }
}
