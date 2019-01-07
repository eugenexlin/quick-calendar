package com.djdenpa.quickcalendar.comparer;

import com.djdenpa.quickcalendar.models.Event;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {
  @Override
  public int compare(Event o1, Event o2) {
    if (o1.eventStartUTC != o2.eventStartUTC ) {
      return Long.compare(o1.eventStartUTC, o2.eventStartUTC);
    }
    return Integer.compare(o1.localId, o2.localId);
  }
}