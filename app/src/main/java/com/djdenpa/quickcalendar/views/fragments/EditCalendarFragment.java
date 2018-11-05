package com.djdenpa.quickcalendar.views.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarNameDialog;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditCalendarFragment extends Fragment
        implements EditCalendarNameDialog.EditCalendarNameListener {

  private Unbinder unbinder;
  @BindView(R.id.tv_calendar_name)
  TextView tvCalendarName;
  @BindView(R.id.rv_calendar_weeks)
  RecyclerView rvCalendarWeeks;

  CalendarWeekAdapter mAdapter;

  private EditCalendarViewModel viewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = ViewModelProviders.of(this.getActivity()).get(EditCalendarViewModel.class);
    viewModel.init();

    // test data
    viewModel.setEntireCalendar(
            new MockCalendarDataGenerator().getMockCalendar());

    try{
      if (viewModel.getActiveCalendar().getValue().name.length() == 0){
        tvCalendarName.setText(getString(R.string.calendar_untitled_name));
      }
    } catch(Exception ex) {
    }

    viewModel.getActiveCalendar().observe(this, calendar -> {
      if (calendar.name.length() == 0){
        tvCalendarName.setText(getString(R.string.calendar_untitled_name));
      } else {
        tvCalendarName.setText(calendar.name);
      }
      mAdapter.setData(calendar);
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    final View rootView = inflater.inflate(R.layout.fragment_edit_calendar, container, false);


    unbinder = ButterKnife.bind(this, rootView);

    tvCalendarName.setOnClickListener(v -> PromptChangeName());

    final LinearLayoutManager layoutManager
            = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

    rvCalendarWeeks.setLayoutManager(layoutManager);

    mAdapter = new CalendarWeekAdapter(getContext());
    //fetch earliest event, it will be base scroll
    Date startDate = viewModel.getActiveCalendar().getValue().getEarliestDateUTC();
    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(startDate);
    javaCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    javaCal.set(java.util.Calendar.MINUTE, 0);
    javaCal.set(java.util.Calendar.SECOND, 0);
    javaCal.set(java.util.Calendar.MILLISECOND, 0);
    javaCal.add(java.util.Calendar.DAY_OF_WEEK, -(javaCal.get(java.util.Calendar.DAY_OF_WEEK)-1));
    mAdapter.setMidpointDate(javaCal.getTime());

    rvCalendarWeeks.setAdapter(mAdapter);

    rvCalendarWeeks.scrollToPosition(CalendarWeekAdapter.START_POSITION);


    return rootView;
  }

  private void PromptChangeName(){
    EditCalendarNameDialog dialog = new EditCalendarNameDialog();
    Bundle args = new Bundle();
    args.putString(EditCalendarNameDialog.BUNDLE_CALENDAR_NAME, viewModel.getActiveCalendar().getValue().name);
    dialog.setArguments(args);
    dialog.setTargetFragment(this, 0);
    dialog.show(getFragmentManager(), "EDIT_NAME");
  }

  @Override
  public void setCalendarName(String name) {
    String cleanedName = name.trim();
    viewModel.setCalendarName(name);
  }

  public void checkCalendarName() {

  }
}
