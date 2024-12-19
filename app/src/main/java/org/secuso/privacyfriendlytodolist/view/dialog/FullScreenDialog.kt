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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
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

        /*
        Needed at least at API 21 to hide the title bar.
        At API 34 the title bar does not appear even without the call of requestWindowFeature().
        Important: Call requestWindowFeature() before calling setContentView().
        */
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(layoutId)

        // required to make the dialog use the full screen width
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.horizontalMargin = 40f
        window!!.setAttributes(lp)
    }
}
