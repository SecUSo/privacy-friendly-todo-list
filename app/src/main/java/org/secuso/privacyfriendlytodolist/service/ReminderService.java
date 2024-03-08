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

package org.secuso.privacyfriendlytodolist.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * This service implements the following alarm policies:
 *
 * - On startup it sets alarms for all tasks fulfilling all of the subsequent conditions:
 *          1. The reminding time is in the past.
 *          2. The deadline is in the future.
 *          3. The task is not yet done.
 * - On startup the service sets an alarm for next due task in the future.
 * - Whenever an alarm is triggered the service sets the alarm for the next due task in the future. It is possible
 *   that this alarm is already set. In that case is just gets overwritten.
 * - A WorkerManager gets used to do the actual work outside the main thread. This is required by Room library.
 */

public class ReminderService extends Service {

    private static final String TAG = ReminderService.class.getSimpleName();

    public class ReminderServiceBinder extends Binder {
        public ReminderService getService() {
            return ReminderService.this;
        }
    }

    private final IBinder mBinder = new ReminderServiceBinder();
    private boolean isAlreadyRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()" + (intent.getExtras() != null ? " with extra" : ""));

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

        Bundle extras = intent.getExtras();
        if (extras == null) {
            //  service was started for the first time
            if (!isAlreadyRunning) {
                Log.i(TAG, "Service was started the first time.");
                // If this service gets killed, alreadyRunning will be false the next time.
                // However, the service is only killed when the resources are scarce. So we
                // deliberately set the alarms again after restarting the service.
                isAlreadyRunning = true;
                startWorker(ReminderWorker.KEY_START_UP, 1);
            } else {
                Log.i(TAG, "Service was already running.");
            }
        } else {
            startWorker(ReminderWorker.KEY_ALARM_TASK_ID, extras.getInt(ReminderWorker.KEY_ALARM_TASK_ID));
        }

        return START_NOT_STICKY; // do not recreate service if the phone runs out of memory
    }

    public void processTodoTask(int todoTaskId) {
        startWorker(ReminderWorker.KEY_TASK_ID, todoTaskId);
    }

    private void startWorker(String workKey, int workArg) {
        Data.Builder inputData = new Data.Builder();
        inputData.putInt(workKey, workArg);
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(ReminderWorker.class);
        builder.setInputData(inputData.build());
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(builder.build());
    }
}
