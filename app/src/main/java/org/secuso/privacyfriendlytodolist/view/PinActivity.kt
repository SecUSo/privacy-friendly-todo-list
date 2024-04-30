package org.secuso.privacyfriendlytodolist.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
            setDialogCallback(object : PinDialog.PinCallback {
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