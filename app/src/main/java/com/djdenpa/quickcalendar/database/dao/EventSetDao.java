package com.djdenpa.quickcalendar.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.djdenpa.quickcalendar.models.EventSet;

import java.util.List;

@Dao
public interface EventSetDao {

  @Query("SELECT * FROM EventSet WHERE calendarId = :calendarId")
  List<EventSet> loadCalendarEventSets(int calendarId);

  @Query("SELECT * FROM EventSet")
  List<EventSet> loadAllEventSets();

//  @Query("SELECT * FROM EventSet WHERE id = :id")
//  EventSet loadEventSet(int id);

  @Insert
  long insertEventSet(EventSet eventSet);

  @Update(onConflict = OnConflictStrategy.REPLACE)
  void updateEventSet(EventSet eventSet);

  @Delete
  void deleteEventSet(EventSet eventSet);

  @Query("DELETE FROM EventSet WHERE id=:id")
  void deleteEventSetById(int id);

}
