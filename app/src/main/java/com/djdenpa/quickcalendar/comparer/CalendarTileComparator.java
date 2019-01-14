package com.djdenpa.quickcalendar.comparer;

import com.djdenpa.quickcalendar.models.CalendarTile;

import java.util.Comparator;

public class CalendarTileComparator implements Comparator<CalendarTile> {
  @Override
  public int compare(CalendarTile o1, CalendarTile o2) {
    return Long.compare( o1.lastAccess, o2.lastAccess);
  }
}
