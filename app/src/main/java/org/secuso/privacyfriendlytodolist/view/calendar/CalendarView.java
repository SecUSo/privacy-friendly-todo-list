package org.secuso.privacyfriendlytodolist.view.calendar;

import android.content.Context;
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
import java.util.Calendar;
import java.util.Date;


public class CalendarView extends LinearLayout {

    private static final int MAX_DAY_COUNT = 42;
    private static final String DATE_FORMAT = "MMM yyyy";

    private Calendar currentDate;

    private CalendarGridAdapter gridAdapter;

    private ImageView buttonPrevMonth, buttonNextMonth;
    private TextView tvCurrentMonth;
    private GridView calendarGrid;


    public CalendarView(Context context) {
        super(context);
        initGui(context);
    }


    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGui(context);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGui(context);
    }

    public void setGridAdapter(CalendarGridAdapter adapter) {
        this.gridAdapter = adapter;
        calendarGrid.setAdapter(gridAdapter);
        refresh();
    }

    public void setDayOnClickListener(AdapterView.OnItemClickListener listener) {
        calendarGrid.setOnItemClickListener(listener);
    }

    public void setNextMonthOnClickListener(OnClickListener listener) {
        buttonNextMonth.setOnClickListener(listener);
    }



    public void setPrevMontOnClickListener(OnClickListener listener) {
        buttonPrevMonth.setOnClickListener(listener);

    }


    private void initGui(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.calendar, this);

        currentDate = Calendar.getInstance();


        buttonPrevMonth = (ImageView) findViewById(R.id.iv_prev_month);
        buttonNextMonth = (ImageView) findViewById(R.id.iv_next_month);
        tvCurrentMonth = (TextView) findViewById(R.id.tv_current_month);
        calendarGrid = (GridView)findViewById(R.id.gv_calendargrid);
    }



    public void refresh() {
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

    private String getMonth(int month) {

        switch(month) {
            case 0:
                return getResources().getString(R.string.january);
            case 1:
                return getResources().getString(R.string.february);
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

    public void incMonth(int i) {
        currentDate.add(Calendar.MONTH, i);
    }
}
