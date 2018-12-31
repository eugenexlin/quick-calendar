package com.djdenpa.quickcalendar.comparer;

import com.djdenpa.quickcalendar.models.Calendar;

import java.util.Comparator;

public class CalendarComparator implements Comparator<Calendar> {
  @Override
  public int compare(Calendar o1, Calendar o2) {
    if (o1.lastAccess == null && o2.lastAccess == null){
      return 0;
    }
    if (o1.lastAccess == null){
      return -1;
    }
    if (o2.lastAccess == null){
      return 1;
    }
    return o1.lastAccess.compareTo(o2.lastAccess);
  }
}