package com.djdenpa.quickcalendar.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.djdenpa.quickcalendar.models.CalendarThumbnail;
import com.djdenpa.quickcalendar.models.EventSet;

import java.util.List;


@Dao
public interface CalendarThumbnailDao {

  @Query("SELECT * FROM CalendarThumbnail WHERE id = :id")
  CalendarThumbnail loadCalendarThumbnails(int id);

  @Insert
  long insertCalendarThumbnail(CalendarThumbnail calendarThumbnail);

  @Update(onConflict = OnConflictStrategy.REPLACE)
  void updateCalendarThumbnail(CalendarThumbnail calendarThumbnail);

  @Delete
  void deleteCalendarThumbnail(CalendarThumbnail calendarThumbnail);


}
