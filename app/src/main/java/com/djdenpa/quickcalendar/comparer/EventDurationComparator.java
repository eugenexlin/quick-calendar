package com.djdenpa.quickcalendar.comparer;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;

import java.util.Comparator;

public class EventDurationComparator implements Comparator<Event> {
  @Override
  public int compare(Event o1, Event o2) {
    if (o1.eventDurationMs != o2.eventDurationMs ) {
      return Long.compare(o1.eventDurationMs, o2.eventDurationMs);
    }
    return Integer.compare(o1.id, o2.id);
  }
}