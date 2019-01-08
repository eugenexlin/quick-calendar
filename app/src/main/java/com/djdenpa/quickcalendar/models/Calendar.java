package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Utf8;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Entity
public class Calendar {

  @Expose
  @PrimaryKey (autoGenerate = true)
  public int id;
  @Expose
  public String name;
  @Expose
  public Date lastAccess;
  @Expose
  public String creatorIdentity;
  @Expose
  public int shareCode;

  @Ignore
  private int nextEventSetId = 1;
  @Ignore
  private Object nextEventSetIdLock = new Object();
  @Ignore
  private HashMap<Integer, EventSet> eventSetHash = new HashMap<>();

  public Calendar(){
    name = "";
    lastAccess = new Date();
    creatorIdentity = "";
  }

  protected Calendar(Parcel in) {
    id = in.readInt();
    name = in.readString();
    lastAccess = new Date(in.readLong());
    creatorIdentity = in.readString();
  }

  public Collection<EventSet> getAllEventSets() {
    return eventSetHash.values();
  }

  // probably only use this for loading from db
  public void replaceAllEventSets(Collection<EventSet> eventSets){
    eventSetHash.clear();
    for (EventSet eventSet : eventSets) {
      saveEventSet(eventSet);
    }
  }

  public void saveEventSet(EventSet eventSet) {
    // if they add an event with an id greater than the next event id,
    // hek set our next id to greater than that one
    if (eventSet.localId > nextEventSetId) {
      synchronized (nextEventSetIdLock){
        nextEventSetId = eventSet.localId + 1;
      }
    }
    // if no id, assign one.
    if (eventSet.localId == 0){
      eventSet.localId = claimNextId();
    }
    // now we see if this event id is already there. if so, save over it.
    if (eventSetHash.containsKey(eventSet.localId )){
      EventSet existingEventSet = eventSetHash.get(eventSet.localId);
      existingEventSet.copyFrom(eventSet);
    } else {
      eventSetHash.put(eventSet.localId, eventSet);
    }
  }

  private int claimNextId() {
    int result;
    synchronized (nextEventSetIdLock){
      result = nextEventSetId;
      nextEventSetId+=1;
    }
    return result;
  }


  public EventSet getFirstEventSet(){
    EventSet result = null;
    //return the first that exists.
    for (EventSet set : eventSetHash.values()){
      result = set;
      break;
    }
    if (result == null) {
      result = new EventSet();
      result.localId = 1;
      eventSetHash.put(1, result);
    }
    return result;
  }

  public EventSet getEventSetById(int id){
    if (eventSetHash.containsKey(id)){
      return eventSetHash.get(id);
    }
    return getFirstEventSet();
  }

  public String getFirebaseHash(){
    if (shareCode == 0) {
      shareCode = new Random().nextInt();
    }
    String starter = creatorIdentity + "_" + id + "_" + shareCode;

    return Hashing.sha256()
            .hashString(starter, StandardCharsets.UTF_8)
            .toString();
  }

  public String getFirebaseSerialization() {
    Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    String initialJson = gson.toJson(this);
    try {
      JSONObject jObj = new JSONObject(initialJson);
      jObj.put("eventSets", new int[0]);
      return jObj.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return "{}";
    }
  }
}
