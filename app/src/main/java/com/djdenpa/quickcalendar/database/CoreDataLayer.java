package com.djdenpa.quickcalendar.database;

import android.appwidget.AppWidgetManager;
import android.util.Log;
import android.widget.Toast;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.CalendarThumbnail;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;
import com.djdenpa.quickcalendar.models.SharedCalendar;

import java.util.List;

public class CoreDataLayer {

  public static Calendar loadCalendar(QuickCalendarDatabase db, int id) {
    Calendar calendar = db.calendarDao().loadCalendar(id);

    List<EventSet> eventSets = db.eventSetDao().loadCalendarEventSets(calendar.id);

    calendar.replaceAllEventSets(eventSets);
    for (EventSet eventSet : eventSets){
      List<Event> events = db.eventDao().loadEventSetEvents(eventSet.id);

      eventSet.replaceAllEvents(events);
    }

    return calendar;
  }

  public static void saveCalendar(QuickCalendarDatabase db, Calendar calendar) {

    int thumbnailId = saveThumbnail(db, calendar);
    calendar.thumbnailId = thumbnailId;

    calendar.lastAccess = System.currentTimeMillis();

    if (calendar.id == 0) {
      long id = db.calendarDao().insertCalendar(calendar);
      calendar.id = (int) id;
    } else{
      db.calendarDao().updateCalendar(calendar);
    }

    for (EventSet eventSet : calendar.getAllEventSets()){
      eventSet.calendarId = calendar.id;
      saveEventSet(db, eventSet);
    }
  }

  public static void saveSharedCalendar(QuickCalendarDatabase db, Calendar calendar, String hash) {

    int thumbnailId = saveThumbnail(db, calendar);

    boolean isNew = false;
    SharedCalendar sharedCalendar = db.sharedCalendarDao().getSharedCalendar(hash);
    if (sharedCalendar == null){
      isNew = true;
      sharedCalendar = new SharedCalendar();
      sharedCalendar.hash = hash;
    }

    sharedCalendar.name = calendar.name;
    sharedCalendar.thumbnailId = thumbnailId;
    sharedCalendar.lastAccess = System.currentTimeMillis();

    if (isNew) {
      db.sharedCalendarDao().insertSharedCalendar(sharedCalendar);
    } else {
      db.sharedCalendarDao().updateSharedCalendar(sharedCalendar);
    }
  }

  // return thumbnail id
  public static int saveThumbnail(QuickCalendarDatabase db, Calendar calendar) {
    CalendarThumbnail thumbnail = new CalendarThumbnail();
    thumbnail.generateThumbnail(calendar);
    int thumbnailId = calendar.thumbnailId;
    if (calendar.thumbnailId != 0) {
      thumbnail.id = thumbnailId;
      db.calendarThumbnailDao().updateCalendarThumbnail(thumbnail);
    } else {
      thumbnailId = (int) db.calendarThumbnailDao().insertCalendarThumbnail(thumbnail);
    }
    return thumbnailId;
  }

  public static void saveEventSet(QuickCalendarDatabase db, EventSet eventSet) {

    if (eventSet.id == 0) {
      if (!eventSet.isMarkedForDeletion) {
        long id = db.eventSetDao().insertEventSet(eventSet);
        eventSet.id = (int) id;
      }
    } else{
      if (eventSet.isMarkedForDeletion) {
        db.eventSetDao().deleteEventSetById(eventSet.id);
      } else {
        db.eventSetDao().updateEventSet(eventSet);
      }
    }

    for (Event event : eventSet.getAllEvents(false)){
      event.eventSetId = eventSet.id;
      saveEvent(db, event);
    }

  }

  public static void saveEvent(QuickCalendarDatabase db, Event event) {

    if (event.id == 0) {
      if (!event.isMarkedForDeletion) {
        long id = db.eventDao().insertEvent(event);
        event.id = (int) id;
      }
    } else{
      if (event.isMarkedForDeletion) {
        db.eventDao().deleteEventById(event.id);
      } else {
        db.eventDao().updateEvent(event);
      }
    }

  }

}

