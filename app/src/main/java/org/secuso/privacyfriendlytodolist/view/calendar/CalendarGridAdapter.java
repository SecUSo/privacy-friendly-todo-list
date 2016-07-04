package org.secuso.privacyfriendlytodolist.view.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarGridAdapter extends ArrayAdapter<Date>{

    private ArrayList<TodoTask> todoTasks;
    private int currentMonth;
    private LayoutInflater inflater;
    private DateFormat dateFormat = new SimpleDateFormat("d");
    private ColorStateList oldColors;

    public CalendarGridAdapter(Context context, int resource) {
        super(context, resource, new ArrayList<Date>());

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        CalendarDayViewHolder dayViewHolder;

        Date dateAtPos = getItem(position);

        Date todayDate = new Date();
        Calendar todayCal = Calendar.getInstance();
        Calendar posCal = Calendar.getInstance();
        todayCal.setTime(todayDate);
        posCal.setTime(dateAtPos);

        if(view == null) {
            dayViewHolder = new CalendarDayViewHolder();

            view = inflater.inflate(R.layout.calendar_day, parent, false);
            dayViewHolder.dayStatus = view.findViewById(R.id.v_calendar_todo_status);
            dayViewHolder.dayText = (TextView) view.findViewById(R.id.tv_calendar_day_content);
            oldColors = dayViewHolder.dayText.getTextColors();

            view.setTag(dayViewHolder);

        } else {
            dayViewHolder = (CalendarDayViewHolder) view.getTag();
        }

        // grey day out if it is outside the current month

        if(posCal.get(Calendar.MONTH) != currentMonth) {
            dayViewHolder.dayText.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
        } else if(sameDay(posCal, todayCal)){
            dayViewHolder.dayText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        } else {
            dayViewHolder.dayText.setTextColor(oldColors);
        }

        dayViewHolder.dayText.setText(dateToStr(posCal));
        dayViewHolder.dayStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));


        return view;

    }

    private boolean sameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private String dateToStr(Calendar c) {
        return dateFormat.format(c.getTime());
    }


    public void setMonth(int month) {
        this.currentMonth = month;
    }

    public void setTodos(ArrayList<TodoTask> todos) {
        this.todoTasks = todos;
    }

    private class CalendarDayViewHolder {
        public View dayStatus;
        public TextView dayText;
    }
}
