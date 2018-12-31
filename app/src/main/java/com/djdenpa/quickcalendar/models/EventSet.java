package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static com.google.gson.internal.$Gson$Types.arrayOf;

// this is a single snapshot of possible event arrangements of one calendar.
@Entity(foreignKeys = @ForeignKey(entity = Calendar.class,
          parentColumns = "id",
          childColumns = "calendarId",
          onDelete = CASCADE),
        indices = {@Index("calendarId")}
)
public class EventSet {

  @PrimaryKey(autoGenerate = true)
  public int id;
  public int calendarId;
  public String name;
  public String creatorIdentity;

  // in this program, event id 0 means not yet defined
  @Ignore
  private int nextLocalEventId = 1;
  @Ignore
  private Object nextLocalEventIdLock = new Object();
  //these two must hold the same data,
  @Ignore
  private HashMap<Integer, Event> eventHash = new HashMap<>();

  @Ignore
  public int localId;

  public EventSet(){
  }
//
//  protected EventSet(int id, int calendarId, String name, String creatorIdentity){
//    this.id = id;
//    this.calendarId = calendarId;
//    this.name = name;
//    this.creatorIdentity = creatorIdentity;
//  }

  public void saveEvent(Event event) {
    // if they add an event with an id greater than the next event id,
    // hek set our next id to greater than that one
    if (event.localId > nextLocalEventId) {
      synchronized (nextLocalEventIdLock){
        nextLocalEventId = event.localId + 1;
      }
    }
    // if no id, assign one.
    if (event.localId == 0){
      event.localId = claimNextId();
    }
    // now we see if this event id is already there. if so, save over it.
    if (eventHash.containsKey(event.localId )){
      Event existingEvent = eventHash.get(event.localId);
      existingEvent.copyFrom(event);
    } else {
      eventHash.put(event.localId, event);
    }

  }

  public Collection<Event> getAllEvents() {
    return eventHash.values();
  }

  // probably only use this for loading from db
  public void replaceAllEvents(Collection<Event> events){
    eventHash.clear();
    for (Event event : events) {
      saveEvent(event);
    }
  }

  public int claimNextId() {
    int result;
    synchronized (nextLocalEventIdLock){
      result = nextLocalEventId;
      nextLocalEventId +=1;
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
