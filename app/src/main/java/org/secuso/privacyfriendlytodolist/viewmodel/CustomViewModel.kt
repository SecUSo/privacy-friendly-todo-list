/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

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
package org.secuso.privacyfriendlytodolist.viewmodel

import android.content.Context
import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.secuso.privacyfriendlytodolist.model.Model

/**
 * CustomViewModel for classes that do not implement ViewModelStoreOwner and therefore can't get
 * LifecycleViewModel from ViewModelProvider.
 * If possible do not use this class but LifecycleViewModel.
 */
class CustomViewModel(context: Context) {
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    /**
     * Model services that work in the Coroutine scope created by this view model.
     * Results from the model services get posted to the 'mainLooper' of the given context.
     */
    val model = Model.createServices(context, coroutineScope, Handler(context.mainLooper))

    /**
     * Shall be called if containing view gets destroyed. It cancels potentially ongoing jobs.
     */
    fun destroy() {
        job.cancel()
    }
}