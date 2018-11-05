package com.djdenpa.quickcalendar.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.LinkedList;

public class Calendar implements Parcelable {
  public int id;
  public String name;
  public Date lastAccess;
  public String creatorIdentity;

  public LinkedList<Event> events = new LinkedList<>();

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

  public Date getEarliestDateUTC(){
    if (events.size() == 0){
      return new Date();
    }
    long earliestStartUTC = events.get(0).eventStartUTC;
    for (int i = 1; i < events.size(); i++) {
      long startUTC = events.get(i).eventStartUTC;
      if (startUTC < earliestStartUTC) {
        startUTC = earliestStartUTC;
      }
    }
    return new Date(earliestStartUTC);
  }
}
