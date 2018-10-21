package com.djdenpa.quickcalendar.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.views.fragments.CalendarTileListFragment;

public class MainActivity extends AppCompatActivity {

  CalendarTileListFragment mRecentCalendarFragment;
  CalendarTileListFragment mRecentSharedCalendarFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mRecentCalendarFragment = (CalendarTileListFragment)
            getSupportFragmentManager().findFragmentById(R.id.f_recent_calendars);
    mRecentSharedCalendarFragment = (CalendarTileListFragment)
            getSupportFragmentManager().findFragmentById(R.id.f_recent_shared_calendars);

    mRecentCalendarFragment.setEmptyStateHelperText(
            getString(R.string.no_calendars_sub));
    mRecentSharedCalendarFragment.setEmptyStateHelperText(
            getString(R.string.no_shared_calendars_sub));

    mRecentCalendarFragment.bindTestData();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
