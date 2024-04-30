package org.secuso.privacyfriendlytodolist.view

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.BuildConfig
import org.secuso.privacyfriendlytodolist.R

/**
 * Created by Sebastian Lutz on 31.01.2018
 *
 * Activity that shows information of the app development.
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        findViewById<TextView>(R.id.secusoWebsite).movementMethod = LinkMovementMethod.getInstance()
        findViewById<TextView>(R.id.githubURL).movementMethod = LinkMovementMethod.getInstance()
        val toolbar: Toolbar = findViewById(R.id.toolbar_about)
        toolbar.setTitle(R.string.menu_about)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setHomeAsUpIndicator(R.drawable.arrow)
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
        }
        findViewById<TextView>(R.id.textFieldVersion).text =
            getString(R.string.version_number, BuildConfig.VERSION_NAME)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
