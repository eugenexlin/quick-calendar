package com.djdenpa.quickcalendar.views.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.fragments.EditCalendarFragment;


public class EditCalendarActivity extends AppCompatActivity implements EditCalendarFragment.MenuEnabledHandler {

  public static final String EXTRA_CALENDAR_ID = "EXTRA_CALENDAR_ID";

  EditCalendarFragment mEditCalendarFragment;

  QuickCalendarDatabase mDB;
  private static final int DEFAULT_TASK_ID = 0;
  private int mCalendarId = DEFAULT_TASK_ID;

  private boolean mCanSave = false;
  private boolean mCanDelete = false;

  private EditCalendarViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_calendar);

    mDB = QuickCalendarDatabase.getInstance(getApplicationContext());

    final Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(EXTRA_CALENDAR_ID)) {
      if (mCalendarId == DEFAULT_TASK_ID) {
        mCalendarId = intent.getIntExtra(EXTRA_CALENDAR_ID, 0);
      }
    }


    mViewModel = ViewModelProviders.of(this).get(EditCalendarViewModel.class);
    mViewModel.init();
    if (mCalendarId == DEFAULT_TASK_ID) {
      // test data
      mViewModel.setEntireCalendar(
              new MockCalendarDataGenerator().getMockCalendar());
    } else {
      QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
        Calendar calendar = mDB.calendarDao().loadCalendar(mCalendarId);
        QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
          mViewModel.setEntireCalendar(calendar);
        });
      });
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_calendar_edit, menu);

    if (!mCanSave) {
      MenuItem item = menu.findItem(R.id.action_save_calendar);
      item.setEnabled(false);
    }
    if (!mCanDelete) {
      MenuItem item = menu.findItem(R.id.action_delete_calendar);
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
    if (id == R.id.action_delete_calendar) {
      new AlertDialog.Builder(this)
              .setTitle("Delete Calendar")
              .setMessage("Do you really want to delete?")
              .setIcon(android.R.drawable.ic_dialog_alert)
              .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> mEditCalendarFragment.deleteCalendar())
              .setNegativeButton(android.R.string.no, null)
              .show();

      return true;
    }


    return super.onOptionsItemSelected(item);
  }

  public void toggleSaveButton( boolean isEnabled) {
    mCanSave = isEnabled;
    invalidateOptionsMenu();
  }

  @Override
  public void toggleDeleteButton(boolean isEnabled) {
    mCanDelete = isEnabled;
    invalidateOptionsMenu();
  }

  @Override
  protected void onStart() {
    super.onStart();

    FragmentManager fm = getSupportFragmentManager();
    mEditCalendarFragment = (EditCalendarFragment) fm.findFragmentById(R.id.edit_calendar_fragment);

    mEditCalendarFragment.setActivity(this);
    mEditCalendarFragment.setViewModel(mViewModel);

  }

//
//  @Override
//  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    super.onActivityResult(requestCode, resultCode, data);
//
//    Intent refresh = new Intent(this, MainActivity.class);
//    startActivity(refresh);
//    this.finish();
//  }
}

