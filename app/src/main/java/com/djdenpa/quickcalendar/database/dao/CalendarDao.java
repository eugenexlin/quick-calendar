package com.djdenpa.quickcalendar.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.djdenpa.quickcalendar.models.Calendar;

import java.util.List;

@Dao
public interface CalendarDao {

  @Query("SELECT * FROM Calendar ORDER BY lastAccess DESC")
  LiveData<List<Calendar>> loadAllCalendars();

  @Query("SELECT * FROM Calendar WHERE id = :id")
  Calendar loadCalendar(int id);

  @Insert
  long insertCalendar(Calendar calendar);

  @Update(onConflict = OnConflictStrategy.REPLACE)
  void updateCalendar(Calendar calendar);

  @Delete
  void deleteCalendar(Calendar calendar);

}
