/*
Privacy Friendly To-Do List
Copyright (C) 2021-2025  Christopher Beckmann

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
package org.secuso.privacyfriendlytodolist

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager
import org.secuso.privacyfriendlytodolist.backup.BackupCreator
import org.secuso.privacyfriendlytodolist.backup.BackupRestorer
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.observer.PreferenceObserver
import org.secuso.privacyfriendlytodolist.observer.TaskChangeObserver
import org.secuso.privacyfriendlytodolist.service.JobManager
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget

class PFAApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()

    override fun onCreate() {
        super.onCreate()
        BackupManager.backupCreator = BackupCreator()
        BackupManager.backupRestorer = BackupRestorer()
        TodoListWidget.registerAsModelObserver(this)
        // When the application exits, its alarms get cancelled by the OS.
        // So ensure here that an alarm is set for the next due task:
        JobManager.startUpdateAlarmJob(this)
        Model.registerModelObserver(TaskChangeObserver)
        PreferenceObserver.initialize(this)
        val appTheme = PreferenceMgr.getAppTheme(this)
        PreferenceObserver.applyAppTheme(appTheme)
    }
}