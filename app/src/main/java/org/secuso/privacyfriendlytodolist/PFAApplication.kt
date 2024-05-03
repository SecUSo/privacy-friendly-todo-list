package org.secuso.privacyfriendlytodolist

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager
import org.secuso.privacyfriendlytodolist.backup.BackupCreator
import org.secuso.privacyfriendlytodolist.backup.BackupRestorer
import org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget

class PFAApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()

    override fun onCreate() {
        super.onCreate()
        BackupManager.backupCreator = BackupCreator()
        BackupManager.backupRestorer = BackupRestorer()
        TodoListWidget.registerAsModelObserver(this)
    }
}