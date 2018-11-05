package com.djdenpa.quickcalendar.views.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.views.fragments.EditCalendarFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditCalendarActivity extends AppCompatActivity {

  EditCalendarFragment mEditCalendarFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_calendar);

    final Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

  }


  @Override
  protected void onStart() {
    super.onStart();

    FragmentManager fm = getSupportFragmentManager();
    mEditCalendarFragment = (EditCalendarFragment) fm.findFragmentById(R.id.edit_calendar_fragment);

  }
}
