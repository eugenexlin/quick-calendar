package com.djdenpa.quickcalendar.comparer;

import com.djdenpa.quickcalendar.models.CalendarInfo;

import java.util.Comparator;
import java.util.Date;

public class CalendarInfoComparator implements Comparator<CalendarInfo> {
  @Override
  public int compare(CalendarInfo o1, CalendarInfo o2) {
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
