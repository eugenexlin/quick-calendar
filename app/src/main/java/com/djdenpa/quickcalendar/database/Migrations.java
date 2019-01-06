package com.djdenpa.quickcalendar.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;

public class Migrations {
  static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE `CalendarThumbnailDao` " +
              "(`id` INTEGER, `calendarId` INTEGER, " +
              "`width` INTEGER, `height` INTEGER, " +
              "`data` BLOB, " +
              "PRIMARY KEY(`id`))");
    }
  };


}
