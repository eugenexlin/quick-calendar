package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;

@Entity
public class Calendar implements Parcelable {

  @PrimaryKey (autoGenerate = true)
  public int id;
  public String name;
  public Date lastAccess;
  public String creatorIdentity;

  @Ignore
  private int nextEventSetId;
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

  protected Calendar(int pId, String pName, Date pLastAccess, String pCreatorIdentity) {
    id = pId;
    name = pName;
    lastAccess = pLastAccess;
    creatorIdentity = pCreatorIdentity;
  }

  public static final Creator<Calendar> CREATOR = new Creator<Calendar>() {
    @Override
    public Calendar createFromParcel(Parcel in) {
      return new Calendar(in);
    }

    @Override
    public Calendar[] newArray(int size) {
      return new Calendar[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(id);
    parcel.writeString(name);
    parcel.writeLong(lastAccess.getTime());
    parcel.writeString(creatorIdentity);
  }

  public void saveEventSet(EventSet eventSet) {
    // if they add an event with an id greater than the next event id,
    // hek set our next id to greater than that one
    if (eventSet.id > nextEventSetId) {
      synchronized (nextEventSetIdLock){
        nextEventSetId = eventSet.id + 1;
      }
    }
    // if no id, assign one.
    if (eventSet.id == 0){
      eventSet.id = claimNextId();
    }
    // now we see if this event id is already there. if so, save over it.
    if (eventSetHash.containsKey(eventSet.id )){
      EventSet existingEventSet = eventSetHash.get(eventSet.id);
      existingEventSet.copyFrom(eventSet);
    } else {
      eventSetHash.put(eventSet.id, eventSet);
    }
  }

  public int claimNextId() {
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

}
