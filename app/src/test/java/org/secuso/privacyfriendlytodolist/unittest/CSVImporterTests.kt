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
package org.secuso.privacyfriendlytodolist.unittest

import org.junit.Test
import org.secuso.privacyfriendlytodolist.model.impl.CSVImporter

class CSVImporterTests {
    @Test
    fun importTest() {
        val importer = CSVImporter()
        importer.import(CSV.reader())
    }

    companion object {
        const val CSV = """ListId,ListName,TaskId,TaskName,TaskCreationTime,TaskDoneTime,TaskDescription,TaskDeadline,TaskReminderTime,TaskRecurrencePattern,TaskRecurrenceInterval,TaskProgress,TaskPriority,SubtaskId,SubtaskName,SubtaskDoneTime
4,List 4,1,Task A,2024-08-02 15:02:53,2024-08-11 08:36:35,"The description",,,NONE,100,0,LOW,,,
,,2,Task B,2024-08-11 08:35:49,,,,,NONE,1234234,0,MEDIUM,1,Subtask A,
,,2,Task B,2024-08-11 08:35:49,,,,,NONE,1,0,MEDIUM,2,Subtask B,2024-08-21 18:36:35
,,2,Task B,2024-08-11 08:35:49,,,,,NONE,123,0,MEDIUM,3,Subtask C,
1,List 1,3,Task C,2024-08-11 08:35:49,2024-08-11 14:35:49,The task description.,2024-12-24 18:00:00,2024-12-20 12:00:00,YEARLY,10,72,HIGH,,,
2,List 2,,,,,,,,,,,,,,
"""
    }
}