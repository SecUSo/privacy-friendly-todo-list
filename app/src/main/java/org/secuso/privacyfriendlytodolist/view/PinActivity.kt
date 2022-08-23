package org.secuso.privacyfriendlytodolist.view

import android.os.Bundle
import android.os.PersistableBundle
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
            setCallback(object : PinDialog.PinCallback {
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