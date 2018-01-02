package org.secuso.privacyfriendlytodolist.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;

import java.util.ArrayList;

/**
 * Created by Sebastian Lutz on 20.12.2017.
 */

public class RecyclerActivity extends AppCompatActivity {

    private DatabaseHelper dbhelper;
    private ExpandableListView expandableListView;
    private ExpandableTodoTaskAdapter taskAdapter;

    private ArrayList<TodoTask> todoTasks = new ArrayList<>();

    private TodoList currentList;

    private MainActivity containingActivity;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);
        dbhelper = DatabaseHelper.getInstance(this);
        ArrayList<TodoTask> tasks = new ArrayList<>();

        String selection = TTodoTask.COLUMN_TRASH + " = ?";
        String[] selectionArgs = {"1"};
        tasks = DBQueryHandler.getBin(dbhelper.getReadableDatabase());


    }

}
