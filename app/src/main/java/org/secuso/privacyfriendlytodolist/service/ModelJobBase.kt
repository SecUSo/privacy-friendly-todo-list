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
package org.secuso.privacyfriendlytodolist.service

import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel

/**
 * Job base class for all jobs that need access to the data model.
 */
abstract class ModelJobBase(jobName: String): JobBase(jobName) {
    private var viewModel: CustomViewModel? = null
    private var modelRef: ModelServices? = null
    protected val model: ModelServices
        get() = modelRef!!

    override fun onCreate() {
        super.onCreate()

        viewModel = CustomViewModel(applicationContext)
        modelRef = viewModel!!.model
    }

    override fun onDestroy() {
        super.onDestroy()

        modelRef = null
        viewModel!!.destroy()
        viewModel = null
    }
}