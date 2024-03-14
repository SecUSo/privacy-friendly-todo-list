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

package org.secuso.privacyfriendlytodolist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoTask

class ViewModelServices(application: Application) : AndroidViewModel(application) {
    private val model = Model.getServices(application)

    fun getAllToDoLists(): LiveData<List<TodoList>> {
        val liveData = MutableLiveData<List<TodoList>>()
        viewModelScope.launch(Dispatchers.IO) {
            liveData.postValue(model.getAllToDoLists())
        }
        return liveData
    }

    fun getAllToDoTasks(): LiveData<List<TodoTask>> {
        val liveData = MutableLiveData<List<TodoTask>>()
        viewModelScope.launch(Dispatchers.IO) {
            liveData.postValue(model.getAllToDoTasks())
        }
        return liveData
    }

    fun getTasksToRemind(today: Long, lockedIds: Set<Int>): LiveData<List<TodoTask>> {
        val liveData = MutableLiveData<List<TodoTask>>()
        viewModelScope.launch(Dispatchers.IO) {
            liveData.postValue(model.getTasksToRemind(today, lockedIds))
        }
        return liveData
    }
}