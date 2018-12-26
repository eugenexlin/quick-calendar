package com.djdenpa.quickcalendar.views.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.views.fragments.EditCalendarFragment;


public class EditCalendarActivity extends AppCompatActivity implements EditCalendarFragment.SaveEnabledHandler {

  EditCalendarFragment mEditCalendarFragment;

  QuickCalendarDatabase mDB;

  private boolean mCanSave = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_calendar);

    mDB = QuickCalendarDatabase.getInstance(getApplicationContext());

    final Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_calendar_edit, menu);

    if (!mCanSave) {
      MenuItem item = menu.findItem(R.id.action_save_calendar);
      item.setEnabled(false);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_change_granularity) {
      mEditCalendarFragment.PromptChangeGranularityFactor();
      return true;
    }
    if (id == R.id.action_add_event) {
      mEditCalendarFragment.PromptEditEvent(new Event());
      return true;
    }
    if (id == R.id.action_change_calendar_view) {
      mEditCalendarFragment.PromptChangeView();
      return true;
    }
    if (id == R.id.action_save_calendar) {
      mEditCalendarFragment.saveCalendar();
      return true;
    }


    return super.onOptionsItemSelected(item);
  }

  public void toggleSaveButton( boolean isEnabled) {
    mCanSave = isEnabled;
    invalidateOptionsMenu();
  }

  @Override
  protected void onStart() {
    super.onStart();

    FragmentManager fm = getSupportFragmentManager();
    mEditCalendarFragment = (EditCalendarFragment) fm.findFragmentById(R.id.edit_calendar_fragment);

    mEditCalendarFragment.setActivity(this);

  }
}

