package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = EventSet.class,
          parentColumns = "id",
          childColumns = "eventSetId",
          onDelete = CASCADE),
        indices = {@Index("eventSetId")}
        )
public class Event implements Parcelable {

  @PrimaryKey(autoGenerate = true)
  public int id;
  public int eventSetId;
  public String name;
  public long eventStartUTC;
  public long eventDurationMs;

  // hex color string
  // color class is not well supported in sdk 24 :(
  public String color;

  @Ignore
  public int localId;
  @Ignore
  public boolean isMarkedForDeletion = false;

  public Event(){
    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTimeInMillis(new Date().getTime());
    javaCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    javaCal.set(java.util.Calendar.MINUTE, 0);
    javaCal.set(java.util.Calendar.SECOND, 0);
    javaCal.set(java.util.Calendar.MILLISECOND, 0);
    eventStartUTC = javaCal.getTime().getTime();
    eventDurationMs = 1000;
  }

  protected Event(Parcel in) {
    id = in.readInt();
    eventStartUTC = in.readLong();
    eventDurationMs = in.readLong();
    name = in.readString();
    color = in.readString();
  }

  public static final Creator<Event> CREATOR = new Creator<Event>() {
    @Override
    public Event createFromParcel(Parcel in) {
      return new Event(in);
    }

    @Override
    public Event[] newArray(int size) {
      return new Event[size];
    }
  };

  public void copyFrom(Event event){
    this.name = event.name;
    this.eventStartUTC = event.eventStartUTC;
    this.eventDurationMs = event.eventDurationMs;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeLong(eventStartUTC);
    dest.writeLong(eventDurationMs);
    dest.writeString(name);
    dest.writeString(color);
  }

  public void importJson(JSONObject jObj) {
    try {
      name = jObj.optString("name");
      eventStartUTC = jObj.getLong("eventStartUTC");
      eventDurationMs = jObj.getLong("eventDurationMs");
      color = jObj.optString("color");
      localId = jObj.getInt("localId");
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public JSONObject exportJson() {
    try {
      JSONObject jObj = new JSONObject();
      jObj.put("name", name);
      jObj.put("eventStartUTC", eventStartUTC);
      jObj.put("eventDurationMs", eventDurationMs);
      jObj.put("color", color);
      jObj.put("localId", localId);
      return jObj;
    } catch (JSONException e) {
      e.printStackTrace();
      return new JSONObject();
    }
  }
}
