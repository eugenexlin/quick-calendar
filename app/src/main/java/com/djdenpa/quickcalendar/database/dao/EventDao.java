package com.djdenpa.quickcalendar.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;

import java.util.List;

@Dao
public interface EventDao {

  @Query("SELECT * FROM Event WHERE eventSetId = :eventSetId")
  List<Event> loadEventSetEvents(int eventSetId);

  @Query("SELECT * FROM Event")
  List<Event> loadAllEvents();


  @Query("SELECT * FROM Event WHERE eventStartUTC > :utcTime ORDER BY eventStartUTC LIMIT 25")
  List<Event> loadEventsAfter(long utcTime);

//  @Query("SELECT * FROM Event WHERE id = :id")
//  EventSet loadEvent(int id);

  @Insert
  long insertEvent(Event event);

  @Update(onConflict = OnConflictStrategy.REPLACE)
  void updateEvent(Event event);

  @Delete
  void deleteEvent(Event event);

  @Query("DELETE FROM Event WHERE id=:id")
  void deleteEventById(int id);

}
