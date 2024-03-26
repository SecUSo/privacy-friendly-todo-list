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

package org.secuso.privacyfriendlytodolist.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog

class PinActivity : AppCompatActivity() {
    companion object {
        var result : Boolean? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // reset result and start dialog
        result = null
        startDialog()
    }

    private fun startDialog() {
        PinDialog(this).apply {
            setDialogCallback(object : PinDialog.PinCallback {
                override fun accepted() {
                    result = true
                    this@PinActivity.finish()
                }
                override fun declined() {
                    result = false
                    this@PinActivity.finish()
                }
                override fun resetApp() {
                    result = false
                    this@PinActivity.finish()
                }
            })
            setDisallowReset(true)
            setOnCancelListener {
                result = false
                this@PinActivity.finish() }
            setOnDismissListener {
                result = false
                this@PinActivity.finish()
            }
            show()
        }
    }
}