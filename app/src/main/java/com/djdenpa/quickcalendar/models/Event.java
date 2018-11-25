package com.djdenpa.quickcalendar.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
  public long eventStartUTC;
  public long eventDurationMs;
  public String name;
  public int id;

  public Event(){
  }

  protected Event(Parcel in) {
    eventStartUTC = in.readLong();
    eventDurationMs = in.readLong();
    name = in.readString();
    id = in.readInt();
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
    dest.writeLong(eventStartUTC);
    dest.writeLong(eventDurationMs);
    dest.writeString(name);
    dest.writeInt(id);
  }
}
