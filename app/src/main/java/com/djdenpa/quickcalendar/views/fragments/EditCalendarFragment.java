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
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarNameDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
  @BindView(R.id.tv_calendar_floating_tag)
  TextView tvCalendarFloatingTag;

  private int mCurrentYear = -1;
  private int mCurrentMonth = -1;

  CalendarWeekAdapter mAdapter;
  LinearLayoutManager mLayoutManager;

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

    tvCalendarName.setOnClickListener(v -> {
      PromptChangeName();
    });

    mLayoutManager
            = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);

    rvCalendarWeeks.setLayoutManager(mLayoutManager);

    mAdapter = new CalendarWeekAdapter(getContext());
    //fetch earliest event, it will be base scroll
    mAdapter.setMidpointDateMillis(viewModel.getActiveCalendar().getValue().getEarliestMillisUTC());

    rvCalendarWeeks.setAdapter(mAdapter);

    rvCalendarWeeks.scrollToPosition(CalendarWeekAdapter.START_POSITION);

    rvCalendarWeeks.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        updateCalendarFloatingTag();
      }
    });

    updateCalendarFloatingTag();

    return rootView;
  }

  private void updateCalendarFloatingTag() {
    int start = mLayoutManager.findFirstVisibleItemPosition();
    int last = mLayoutManager.findLastVisibleItemPosition();
    int position = (start + last)/2;

    java.util.Calendar jCal = mAdapter.getMonthYear(position);
    boolean hasChange = false;
    hasChange |= (mCurrentYear != jCal.get(Calendar.YEAR));
    hasChange |= (mCurrentMonth != jCal.get(Calendar.MONTH));
    mCurrentYear = jCal.get(Calendar.YEAR);
    mCurrentMonth = jCal.get(Calendar.MONTH);
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
    tvCalendarFloatingTag.setText(sdf.format(jCal.getTime()));
    if (hasChange) {
      mAdapter.setHighlightMonth(mCurrentMonth);
    }
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
