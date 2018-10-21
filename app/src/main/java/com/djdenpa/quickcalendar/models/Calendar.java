package com.djdenpa.quickcalendar.models;

import java.util.Date;
import java.util.LinkedList;

public class Calendar {
  public int id;
  public String name;
  public Date lastAccess;
  public String creatorIdentity;

  public LinkedList<Event> events = new LinkedList<>();
}
