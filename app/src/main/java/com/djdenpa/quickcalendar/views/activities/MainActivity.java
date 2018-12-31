package com.djdenpa.quickcalendar.views.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;
import com.djdenpa.quickcalendar.views.fragments.CalendarTileListFragment;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

  CalendarTileListFragment mRecentCalendarFragment;
  CalendarTileListFragment mRecentSharedCalendarFragment;

  QuickCalendarDatabase mDB;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mDB = QuickCalendarDatabase.getInstance(getApplicationContext());

    // TEST LOCALE
//    Resources resources = getResources();
//    Configuration configuration = resources.getConfiguration();
//    configuration.setLocale(Locale.JAPAN);
//    resources.updateConfiguration(configuration, resources.getDisplayMetrics());

    mRecentCalendarFragment = (CalendarTileListFragment)
            getSupportFragmentManager().findFragmentById(R.id.f_recent_calendars);
    mRecentSharedCalendarFragment = (CalendarTileListFragment)
            getSupportFragmentManager().findFragmentById(R.id.f_recent_shared_calendars);

    mRecentCalendarFragment.setEmptyStateHelperText(
            getString(R.string.no_calendars_sub));
    mRecentSharedCalendarFragment.setEmptyStateHelperText(
            getString(R.string.no_shared_calendars_sub));

    // mRecentCalendarFragment.bindTestData();


    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_create_new);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, EditCalendarActivity.class);
        startActivity(intent);
      }
    });

  }

  @Override
  protected void onResume() {
    super.onResume();

    BindCalendarData();
  }

  private void BindCalendarData() {
    mRecentCalendarFragment.setLoading(true);
    QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
      mDB.calendarDao().loadAllCalendars().observe(this, calendarList -> {
        QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
          mRecentCalendarFragment.setAdapterData(calendarList);
          mRecentCalendarFragment.setLoading(false);
        });
      });
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

}
