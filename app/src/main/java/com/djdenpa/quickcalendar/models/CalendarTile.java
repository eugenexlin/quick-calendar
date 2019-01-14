package com.djdenpa.quickcalendar.models;

public class CalendarTile {
  public String name;
  public int thumbnailId;
  public long lastAccess;

  public int calendarId;
  public boolean isShare;
  public String shareHash;


  public CalendarTile(Calendar calendar){
    isShare = false;
    calendarId = calendar.id;
    name = calendar.name;
    thumbnailId = calendar.thumbnailId;
    lastAccess = calendar.lastAccess;

  }

  public CalendarTile(SharedCalendar sharedCalendar){
    isShare = true;
    shareHash = sharedCalendar.hash;
    name = sharedCalendar.name;
    thumbnailId = sharedCalendar.thumbnailId;
    lastAccess = sharedCalendar.lastAccess;
  }


}
