package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

// this is a single snapshot of possible event arrangements of one calendar.
@Entity
public class EventSet {

  @PrimaryKey(autoGenerate = true)
  public int id;
  public String name;
  public String creatorIdentity;

  // in this program, event id 0 means not yet defined
  @Ignore
  private int nextEventId = 1;
  @Ignore
  private Object nextEventIdLock = new Object();
  //these two must hold the same data,
  @Ignore
  private HashMap<Integer, Event> eventHash = new HashMap<>();

  public void saveEvent(Event event) {
    // if they add an event with an id greater than the next event id,
    // hek set our next id to greater than that one
    if (event.id > nextEventId) {
      synchronized (nextEventIdLock){
        nextEventId = event.id + 1;
      }
    }
    // if no id, assign one.
    if (event.id == 0){
      event.id = claimNextId();
    }
    // now we see if this event id is already there. if so, save over it.
    if (eventHash.containsKey(event.id )){
      Event existingEvent = eventHash.get(event.id);
      existingEvent.copyFrom(event);
    } else {
      eventHash.put(event.id, event);
    }

  }

  public Collection<Event> getAllEvents() {
    return eventHash.values();
  }

  public int claimNextId() {
    int result;
    synchronized (nextEventIdLock){
      result = nextEventId;
      nextEventId+=1;
    }
    return result;
  }

  public long getEarliestMillisUTC(){
    if (eventHash.size() <= 0){
      return new Date().getTime();
    }
    Event first = eventHash.get(0);
    long earliestStartUTC;
    if (first == null){
      earliestStartUTC = new Date().getTime();
    } else {
      earliestStartUTC = eventHash.get(0).eventStartUTC;
    }
    for (Event event : eventHash.values()) {
      long startUTC = event.eventStartUTC;
      if (startUTC < earliestStartUTC) {
        earliestStartUTC = startUTC;
      }
    }
    return earliestStartUTC;
  }

  public void copyFrom(EventSet eventSet) {
    this.name = eventSet.name;
  }
}
