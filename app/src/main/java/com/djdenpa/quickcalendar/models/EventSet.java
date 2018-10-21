package com.djdenpa.quickcalendar.models;

import java.util.LinkedList;

// this is a single snapshot of possible event arrangements of one calendar.
public class EventSet {
  public String name;
  public String creatorIdentity;

  public LinkedList<Event> events = new LinkedList<>();


}
