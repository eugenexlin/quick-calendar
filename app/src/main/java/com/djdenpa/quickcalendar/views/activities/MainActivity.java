package com.djdenpa.quickcalendar.views.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
  protected void onDestroy() {
    super.onDestroy();
  }
}
