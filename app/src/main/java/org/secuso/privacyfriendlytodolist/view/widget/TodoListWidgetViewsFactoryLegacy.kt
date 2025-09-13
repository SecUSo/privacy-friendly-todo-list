/*
Privacy Friendly To-Do List
Copyright (C) 2018-2025  Sebastian Lutz

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
package org.secuso.privacyfriendlytodolist.view.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import java.util.ArrayList

/**
 * Created by Sebastian Lutz on 15.02.2018.
 *
 * This class sets to-do tasks to show up in the widget.
 *
 * This class is only in use if SDK_INT is below Build.VERSION_CODES.S. See [TodoListWidget].
 */
class TodoListWidgetViewsFactoryLegacy(private val context: Context, private val appWidgetId: Int):
    TodoListWidgetViewsFactory(), RemoteViewsFactory {

    private var items: List<Pair<Int, RemoteViews>> = ArrayList<Pair<Int, RemoteViews>>()
    private var lastListTitle: String? = null

    override fun onCreate() {
    }

    override fun onDestroy() {
        destroy()
    }

    override fun onDataSetChanged() {
        items = createItems(context, appWidgetId)

        if (lastListTitle != listName) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val bundle: Bundle = appWidgetManager.getAppWidgetOptions(appWidgetId)
            bundle.putString(TodoListWidget.OPTION_TODO_WIDGET_TITLE, listName)
            appWidgetManager.updateAppWidgetOptions(appWidgetId, bundle)
            lastListTitle = listName
        }
    }

    override fun getViewTypeCount(): Int {
        return numberOfViewTypes
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewAt(position: Int): RemoteViews? {
        return if (position in items.indices) items[position].second else null
    }

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) items[position].first.toLong() else 0
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
