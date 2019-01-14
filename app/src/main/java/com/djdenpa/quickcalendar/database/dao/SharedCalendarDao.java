package com.djdenpa.quickcalendar.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.djdenpa.quickcalendar.models.SharedCalendar;

import java.util.List;

@Dao
public interface SharedCalendarDao {

  @Query("SELECT * FROM SharedCalendar ORDER BY lastAccess DESC")
  LiveData<List<SharedCalendar>> loadAllSharedCalendars();

  @Query("SELECT * FROM SharedCalendar WHERE hash = :hash")
  SharedCalendar getSharedCalendar(String hash);

  @Insert
  long insertSharedCalendar(SharedCalendar sharedCalendar);

  @Update(onConflict = OnConflictStrategy.REPLACE)
  void updateSharedCalendar(SharedCalendar sharedCalendar);

  @Delete
  void deleteSharedCalendar(SharedCalendar sharedCalendar);
}
