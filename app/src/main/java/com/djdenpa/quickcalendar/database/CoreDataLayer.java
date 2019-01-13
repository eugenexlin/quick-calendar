package com.djdenpa.quickcalendar.database;

import android.util.Log;
import android.widget.Toast;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;

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

