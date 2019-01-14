package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;

import com.google.common.hash.Hashing;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity
public class Calendar {

  @PrimaryKey (autoGenerate = true)
  public int id;
  public String name;
  public long lastAccess;
  public String creatorIdentity;

  public int thumbnailId;

  public int shareCode;

  @Ignore
  private int nextEventSetId = 1;
  @Ignore
  private Object nextEventSetIdLock = new Object();
  @Ignore
  private HashMap<Integer, EventSet> eventSetHash = new HashMap<>();

  public Calendar(){
    name = "";
    lastAccess =  System.currentTimeMillis();
    creatorIdentity = "";
  }

  protected Calendar(Parcel in) {
    id = in.readInt();
    name = in.readString();
    lastAccess = in.readLong();
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
      eventSet.localId = claimNextLocalId();
    }
    // now we see if this event id is already there. if so, save over it.
    if (eventSetHash.containsKey(eventSet.localId )){
      EventSet existingEventSet = eventSetHash.get(eventSet.localId);
      existingEventSet.copyFrom(eventSet);
    } else {
      eventSetHash.put(eventSet.localId, eventSet);
    }
  }

  private int claimNextLocalId() {
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

  public EventSet getEventSetByLocalId(int id){
    if (eventSetHash.containsKey(id)){
      return eventSetHash.get(id);
    }
    return getFirstEventSet();
  }

  public JSONArray exportAllEventSetsJson() {
    JSONArray array = new JSONArray();
    for (EventSet set : eventSetHash.values()){
      array.put(set.exportJson());
    }
    return array;
  }

  public void EnsureShareCodeInitialized(){
    if (shareCode == 0) {
      shareCode = new Random().nextInt();
    }
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

  public void importJson(JSONObject jObj) {
    try {
      name = jObj.getString("name");
      lastAccess = jObj.getLong("lastAccess");
      creatorIdentity = jObj.getString("creatorIdentity");
      JSONArray jArr = jObj.getJSONArray("eventSets");
      for (int i=0; i < jArr.length(); i++) {
        JSONObject jEventSet = jArr.getJSONObject(i);
        int localId = jEventSet.getInt("localId");
        EventSet eventSet = getEventSetByLocalId(localId);
        if (eventSet == null) {
          eventSet = new EventSet();
        }
        eventSet.importJson(jEventSet);
        saveEventSet(eventSet);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void importFirebaseSerialization(String data) {
    try {
      JSONObject jObj = new JSONObject(data);
      importJson(jObj);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public String getFirebaseSerialization() {
    try {
      JSONObject jObj = new JSONObject();
      jObj.put("name", name);
      jObj.put("lastAccess", lastAccess);
      jObj.put("creatorIdentity", creatorIdentity);
      jObj.put("eventSets", exportAllEventSetsJson());
      return jObj.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    }
  }
}
