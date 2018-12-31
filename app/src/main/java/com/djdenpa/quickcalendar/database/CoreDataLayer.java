package com.djdenpa.quickcalendar.database;

import android.widget.Toast;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;

import java.util.Date;

public class CoreDataLayer {

  public static void saveCalendar(QuickCalendarDatabase db, Calendar calendar) {

    if (calendar.id == 0) {
      db.calendarDao().insertCalendar(calendar);
    } else{
      db.calendarDao().updateCalendar(calendar);
    }



  }

  public static void saveEventSet() {

  }
}
