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
        const val CSV = """ListId,ListName,TaskId,TaskName,TaskCreationTime,TaskDoneTime,TaskDescription,TaskDeadline,TaskReminderTime,TaskRecurrencePattern,TaskProgress,TaskPriority,SubtaskId,SubtaskName,SubtaskDoneTime
4,List 4,1,Task A,2024-08-02 15:02:53,2024-08-11 08:36:35,"The description",,,NONE,0,LOW,,,
,,2,Task B,2024-08-11 08:35:49,,,,,NONE,0,MEDIUM,1,Subtask A,
,,2,Task B,2024-08-11 08:35:49,,,,,NONE,0,MEDIUM,2,Subtask B,2024-08-21 18:36:35
,,2,Task B,2024-08-11 08:35:49,,,,,NONE,0,MEDIUM,3,Subtask C,
1,List 1,3,Task C,2024-08-11 08:35:49,2024-08-11 14:35:49,The task description.,2024-12-24 18:00:00,2024-12-20 12:00:00,YEARLY,72,HIGH,,,
2,List 2,,,,,,,,,,,,,
"""
    }
}