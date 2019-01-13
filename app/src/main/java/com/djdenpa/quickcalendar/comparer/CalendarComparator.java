package com.djdenpa.quickcalendar.comparer;

import com.djdenpa.quickcalendar.models.Calendar;

import java.util.Comparator;

public class CalendarComparator implements Comparator<Calendar> {
  @Override
  public int compare(Calendar o1, Calendar o2) {
    return Long.compare( o1.lastAccess, o2.lastAccess);
  }
}
