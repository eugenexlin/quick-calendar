package com.djdenpa.quickcalendar.database;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import com.djdenpa.quickcalendar.database.dao.CalendarDao;
import com.djdenpa.quickcalendar.database.dao.EventDao;
import com.djdenpa.quickcalendar.database.dao.EventSetDao;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;

@Database(entities = {
        Calendar.class,
        EventSet.class,
        Event.class
  }, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class QuickCalendarDatabase extends RoomDatabase {

  private static final String DATABASE_NAME = "quick_calendar_db";
  private static final Object instantiationLock = new Object();
  private static QuickCalendarDatabase sInstance;

  public static QuickCalendarDatabase getInstance(Context context) {
    if (sInstance == null) {
      synchronized(instantiationLock) {
        if (sInstance == null) {
          sInstance = Room.databaseBuilder(context.getApplicationContext(),
                  QuickCalendarDatabase.class,
                  QuickCalendarDatabase.DATABASE_NAME
                  )
                  .build();
//                   .allowMainThreadQueries()
        }
      }
    }
    return sInstance;
  }

  public abstract CalendarDao calendarDao();
  public abstract EventSetDao eventSetDao();
  public abstract EventDao eventDao();

  @NonNull
  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
    return null;
  }

  @NonNull
  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return null;
  }

  @Override
  public void clearAllTables() {

  }


}
