/*
Privacy Friendly To-Do List
Copyright (C) 2022-2025  Christopher Beckmann

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
package org.secuso.privacyfriendlytodolist.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.secuso.privacyfriendlytodolist.view.dialog.PinCallback
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog

class PinActivity : AppCompatActivity() {
    companion object {
        var isAuthenticated : Boolean? = null
            private set

        fun reset() {
            isAuthenticated = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // reset result and start dialog
        reset()
        startDialog()
    }

    private fun startDialog() {
        PinDialog(this, false).apply {
            setDialogCallback(object : PinCallback {
                override fun accepted() {
                    isAuthenticated = true
                    this@PinActivity.finish()
                }

                override fun declined() {
                    isAuthenticated = false
                    this@PinActivity.finish()
                }

                override fun resetApp() {
                    isAuthenticated = false
                    this@PinActivity.finish()
                }
            })
            setOnCancelListener {
                isAuthenticated = false
                this@PinActivity.finish()
            }
            setOnDismissListener {
                isAuthenticated = false
                this@PinActivity.finish()
            }
            show()
        }
    }
}