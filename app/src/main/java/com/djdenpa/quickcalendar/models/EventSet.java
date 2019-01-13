package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.djdenpa.quickcalendar.comparer.EventComparator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
  @Expose
  public int id;
  @Expose
  public int calendarId;
  @Expose
  public String name;
  @Expose
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
  @Expose
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
    ArrayList<Event> sorted = new ArrayList<>();
    for (Event event : eventHash.values()) {
      sorted.add(event);
    }
    sorted.sort(new EventComparator());
    return sorted;
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

  public Event getEventByLocalId(int localId) {
    for (Event event : eventHash.values()) {
      if (event.localId == localId) {
        return event;
      }
    }
    return null;
  }

//  public Event getChronologicalNext(Event event) {
//    return getAdjacentEvent(event, 1);
//  }
//  public Event getChronologicalPrev(Event event ) {
//    return getAdjacentEvent(event, -1);
//  }
  // 1 means next, -1 means previous
  public Event getAdjacentEvent(Event event, int polarity){
    long minDT = Long.MAX_VALUE;
    int minDId = Integer.MAX_VALUE;
    Event result = null;
    for (Event otherEvent : eventHash.values()) {
      if (event.localId == otherEvent.localId) {
        continue;
      }
      long dT = (otherEvent.eventStartUTC - event.eventStartUTC) * polarity;
      if (dT < 0) {
        // this is on the other side of polarity, skip
        continue;
      }
      if (dT > minDT) {
        continue;
      }
      if (dT <= minDT) {

        if (dT == 0) {
          //since distances are tied, find the one with the closer ID.. hah
          int dId = (otherEvent.localId - event.localId) * polarity;
          if (dId < 0) {
            // this is on the other side of polarity, skip
            continue;
          }
          if (dId < minDId) {
            minDId = dId;
            result = otherEvent;
          }
        } else {
          minDId = Integer.MAX_VALUE;
          minDT = dT;
          result = otherEvent;
        }
      }
    }
    if (result == null) {
      return event;
    }
    return result;
  }

  public Event getEventNearDate(long timeUtc, int polarity){
    long minDT = Long.MAX_VALUE;
    int minId = Integer.MAX_VALUE;
    Event result = null;
    for (Event otherEvent : eventHash.values()) {
      long dT = (otherEvent.eventStartUTC - timeUtc) * polarity;
      if (dT < 0) {
        // this is on the other side of polarity, skip
        continue;
      }
      if (dT > minDT) {
        continue;
      }
      if (dT < minDT) {
        // reset ID because entering new chronological bracket,
        minId = Integer.MAX_VALUE;
        minDT = dT;
        result = otherEvent;
        continue;
      }
      minDT = dT;

      //since distances are tied, find the one with the closer ID.. hah
      if (otherEvent.localId < minId) {
        minId = otherEvent.localId;
        result = otherEvent;
      }
    }
    return result;
  }

  public JSONArray getAllEventsJson() {
    JSONArray array = new JSONArray();
    for (Event event : eventHash.values()){
      array.put(event.getJson());
    }
    return array;
  }
  public JSONObject getJson() {
    Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    String initialJson = gson.toJson(this);
    try {
      JSONObject jObj = new JSONObject(initialJson);
      jObj.put("events", getAllEventsJson());
      return jObj;
    } catch (JSONException e) {
      e.printStackTrace();
      return new JSONObject();
    }
  }
}
