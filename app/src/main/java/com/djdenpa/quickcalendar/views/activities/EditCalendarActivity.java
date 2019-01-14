package com.djdenpa.quickcalendar.views.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.CoreDataLayer;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;
import com.djdenpa.quickcalendar.utils.SharedPreferenceManager;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.fragments.EditCalendarFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class EditCalendarActivity extends AppCompatActivity implements EditCalendarFragment.MenuEnabledHandler {

  public static final String EXTRA_CALENDAR_ID = "EXTRA_CALENDAR_ID";
  public static final String EXTRA_SHARE_HASH = "EXTRA_SHARE_HASH";

  EditCalendarFragment mEditCalendarFragment;

  QuickCalendarDatabase mDB;
  private static final int DEFAULT_TASK_ID = 0;
  private int mCalendarId = DEFAULT_TASK_ID;

  private boolean mCanSave = false;
  private boolean mCanDelete = false;

  private EditCalendarViewModel mViewModel;

  DatabaseReference mShareDbRef;

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
    String shareHash = "";
    if (intent != null && intent.hasExtra(EXTRA_SHARE_HASH)) {
      shareHash = intent.getStringExtra(EXTRA_SHARE_HASH);
    }
    if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      Uri uri = intent.getData();
      shareHash = uri.getQueryParameter("hash");
    }

    if (!shareHash.equals("")){
      FirebaseDatabase database = FirebaseDatabase.getInstance();
      mShareDbRef = database.getReference("calendars/" + shareHash);
      ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          String data = (String) dataSnapshot.getValue();
          mEditCalendarFragment.setShareData(data);
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
          Toast.makeText(getApplicationContext(), "Failed to get calendar data. " + databaseError.getMessage(),
                  Toast.LENGTH_SHORT).show();
        }
      };
      mShareDbRef.addValueEventListener(postListener);
    }

    mViewModel = ViewModelProviders.of(this).get(EditCalendarViewModel.class);
    mViewModel.init();
    if (mCalendarId == DEFAULT_TASK_ID) {
      // test data
//      mViewModel.setEntireCalendar(
//              new MockCalendarDataGenerator().getMockCalendar());
    } else {
      QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
        Calendar calendar = CoreDataLayer.loadCalendar(mDB, mCalendarId);
        QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
          mEditCalendarFragment.setEntireCalendar(calendar);
        });
      });
    }

    SharedPreferenceManager prefMan = new SharedPreferenceManager(this);
    mViewModel.idToken = prefMan.getUserIdToken();
    mViewModel.uid = prefMan.getUserId();
    mViewModel.identity= prefMan.getUserEmail();
    mViewModel.userName = prefMan.getUserName();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_calendar_edit, menu);

    for(int i = 0; i < menu.size(); i++){
      Drawable drawable = menu.getItem(i).getIcon();
      if(drawable != null) {
        drawable.setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        drawable.setAlpha(255);
      }
    }

    if (!mCanSave) {
      MenuItem item = menu.findItem(R.id.action_save_calendar);
      item.setEnabled(false);
      Drawable drawable = item.getIcon();
      drawable.setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
      drawable.setAlpha(50);
    }
    if (!mCanDelete) {
      MenuItem item = menu.findItem(R.id.action_delete_calendar);
      item.setEnabled(false);
    }

    if (mViewModel.uid == "") {
      MenuItem item1 = menu.findItem(R.id.action_create_share);
      item1.setEnabled(false);
      MenuItem item2 = menu.findItem(R.id.action_disable_share);
      item2.setEnabled(false);
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
    if (id == R.id.action_next_event) {
      mEditCalendarFragment.selectAdjacentEvent(1);
      return true;
    }
    if (id == R.id.action_prev_event) {
      mEditCalendarFragment.selectAdjacentEvent(-1);
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

    if (id == R.id.m_real_time_share){
      if (mViewModel.uid == "") {
        Toast.makeText(getApplicationContext(), "Sign in to access real-time share.", Toast.LENGTH_SHORT).show();
        return true;
      }
    }

    if (id == R.id.action_create_share) {
      if(mViewModel.getActiveCalendar().getValue().id == 0){
        new AlertDialog.Builder(this)
                .setTitle("Share Calendar")
                .setMessage("Save and share calendar?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> mEditCalendarFragment.saveCalendar(true))
                .setNegativeButton(android.R.string.no, null)
                .show();
      } else {
        mEditCalendarFragment.enableFirebaseShare();
      }

      return true;
    }
    if (id == R.id.action_disable_share) {
      mViewModel.setIsFirebaseShareOn(false);
      return true;
    }

    if (id == R.id.actions_generate_test_events){
      mEditCalendarFragment.setEventSet(
              new MockCalendarDataGenerator().getMockEventSet());
      mEditCalendarFragment.toggleSaveButton(true);
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
  }

}

