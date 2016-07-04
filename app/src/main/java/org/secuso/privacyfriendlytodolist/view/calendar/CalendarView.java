package org.secuso.privacyfriendlytodolist.view.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CalendarView extends LinearLayout {

    private static final int MAX_DAY_COUNT = 42;
    private static final String DATE_FORMAT = "MMM yyyy";

    private ArrayList todoTasks;

    private Calendar currentDate;

    private CalendarGridAdapter gridAdapter;

    private LinearLayout dayLine;
    private ImageView buttonPrevMonth, buttonNextMonth;
    private TextView tvCurrentMonth;
    private GridView calendarGrid;


    public CalendarView(Context context) {
        super(context);

        currentDate = Calendar.getInstance();

        initGui(context);
        initValues();
        updateCalendar();
    }


    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGui(context);
        initValues();
        updateCalendar();
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initGui(context);
        initValues();
        updateCalendar();
    }

    private void initValues() {
        currentDate = Calendar.getInstance();

    }


    private void initGui(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.calendar, this);

        dayLine = (LinearLayout) findViewById(R.id.ll_day_line);
        buttonPrevMonth = (ImageView) findViewById(R.id.iv_prev_month);
        buttonNextMonth = (ImageView) findViewById(R.id.iv_next_month);
        tvCurrentMonth = (TextView) findViewById(R.id.tv_current_month);
        calendarGrid = (GridView)findViewById(R.id.gv_calendargrid);


        buttonPrevMonth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();

            }
        });

        buttonNextMonth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        calendarGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Date selectedDate = gridAdapter.getItem(position);
                Toast.makeText(getContext(), selectedDate.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        gridAdapter = new CalendarGridAdapter(getContext(), R.layout.calendar_day);
        calendarGrid.setAdapter(gridAdapter);
    }


    private void updateCalendar() {
        Calendar calendar = (Calendar) currentDate.clone();

        int selectedMonth = calendar.get(Calendar.MONTH);

         // determine cell for the current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        gridAdapter.clear();
        int dayCounter = 0;
        while(dayCounter < MAX_DAY_COUNT) {
            gridAdapter.insert(calendar.getTime(), dayCounter);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            dayCounter++;
        }
        gridAdapter.setMonth(selectedMonth);
        gridAdapter.notifyDataSetChanged();

        // update title
        tvCurrentMonth.setText(getMonth(currentDate.get(Calendar.MONTH)) + " " + currentDate.get(Calendar.YEAR));

    }

    public void setTodoTasks(ArrayList<TodoTask> todoTasks) {
        gridAdapter.setTodos(todoTasks);
    }

    private String getMonth(int month) {

        switch(month) {
            case 0:
                return getResources().getString(R.string.january);
            case 1:
                return getResources().getString(R.string.februrary);
            case 2:
                return getResources().getString(R.string.march);
            case 3:
                return getResources().getString(R.string.april);
            case 4:
                return getResources().getString(R.string.mai);
            case 5:
                return getResources().getString(R.string.june);
            case 6:
                return getResources().getString(R.string.july);
            case 7:
                return getResources().getString(R.string.august);
            case 8:
                return getResources().getString(R.string.september);
            case 9:
                return getResources().getString(R.string.october);
            case 10:
                return getResources().getString(R.string.november);
            case 11:
                return getResources().getString(R.string.december);
            default:
                return "UNKNOWN MONTH";
        }
    }
}
