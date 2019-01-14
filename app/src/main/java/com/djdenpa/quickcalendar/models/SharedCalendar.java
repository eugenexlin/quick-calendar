package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class SharedCalendar {

  @NonNull
  @PrimaryKey
  public String hash;
  public String name;
  public long lastAccess;
  public int thumbnailId;

}
