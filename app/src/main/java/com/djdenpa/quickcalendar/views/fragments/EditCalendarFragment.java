package com.djdenpa.quickcalendar.views.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter;
import com.djdenpa.quickcalendar.views.adapters.CalendarWeekViewHolder;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarEventDialog;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarNameDialog;
import com.djdenpa.quickcalendar.views.dialogs.GenericSingleSelectListDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditCalendarFragment extends Fragment
        implements EditCalendarNameDialog.EditCalendarNameListener,
        GenericSingleSelectListDialog.GenericSpinnerDialogListener,
        EditCalendarEventDialog.EditCalendarNameListener {

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

  // this is for throttling
  private long previousRVScrollTime = System.currentTimeMillis();
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
      mAdapter.setData(viewModel.getActiveEventSet());
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
    long earliestEventMillis = viewModel.getActiveCalendar().getValue().getFirstEventSet().getEarliestMillisUTC();
    mAdapter.setMidpointDateMillis(earliestEventMillis);
    java.util.Calendar earliestEventCal = java.util.Calendar.getInstance();
    earliestEventCal.setTimeInMillis(earliestEventMillis);
    updateCalendarFocusMonth(earliestEventCal);

    rvCalendarWeeks.setAdapter(mAdapter);

    if (viewModel.isFirstEntry) {
      rvCalendarWeeks.scrollToPosition(CalendarWeekAdapter.START_POSITION );
    }

    rvCalendarWeeks.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (Math.abs(previousRVScrollTime - System.currentTimeMillis()) > 100) {
          previousRVScrollTime = System.currentTimeMillis();

          checkCalendarFocusMonth();
        }
      }
    });

    viewModel.isFirstEntry = false;

    return rootView;
  }

  // here we use a funny algorithm of if the number becomes
  // a bit too far from the current month,
  // set it to a new value
  private void checkCalendarFocusMonth() {
    int start = mLayoutManager.findFirstVisibleItemPosition();
    int end = mLayoutManager.findLastVisibleItemPosition();
    int midPosition = (start + end)/2;

    if (tvCalendarFloatingTag.getText().length() == 0 ||
        Math.abs(mAdapter.getAverageMonthValue(start, end) - (double)mCurrentMonth) > 0.7) {
      java.util.Calendar jCal = mAdapter.getMonthYear(midPosition);
      updateCalendarFocusMonth(jCal);
    }
  }

  private void updateCalendarFocusMonth(java.util.Calendar jCal){
    boolean hasChange = false;
    hasChange |= (mCurrentYear != jCal.get(Calendar.YEAR));
    hasChange |= (mCurrentMonth != jCal.get(Calendar.MONTH));
    mCurrentYear = jCal.get(Calendar.YEAR);
    mCurrentMonth = jCal.get(Calendar.MONTH);
    if (hasChange) {
      SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.month_year_format_string));
      tvCalendarFloatingTag.setText(sdf.format(jCal.getTime()));
      mAdapter.setHighlightMonth(mCurrentMonth);
    }
  }

  private void PromptChangeName(){
    EditCalendarNameDialog dialog = new EditCalendarNameDialog();
    Bundle args = new Bundle();
    args.putString(EditCalendarNameDialog.BUNDLE_CALENDAR_NAME,
            viewModel.getActiveCalendar().getValue().name);
    dialog.setArguments(args);
    dialog.setTargetFragment(this, 0);
    dialog.show(getFragmentManager(), "EDIT_NAME");
  }


  public void PromptChangeGranularityFactor(){

    GenericSingleSelectListDialog dialog = new GenericSingleSelectListDialog();
    Bundle args = new Bundle();
    args.putString(GenericSingleSelectListDialog.BUNDLE_STRING_CURRENT_VALUE,
            String.valueOf(mAdapter.getEventGranularityFactor()));
    args.putInt(GenericSingleSelectListDialog.BUNDLE_INT_STRING_TITLE,
            R.string.menu_change_granularity);
    args.putInt(GenericSingleSelectListDialog.BUNDLE_INT_ARRAY_ID,
            R.array.week_granularity);
    dialog.setArguments(args);
    dialog.setTargetFragment(this, DIALOG_CODE_CHANGE_WEEK_GRANULARITY);

    dialog.show(getFragmentManager(), "CHANGE_GRANULARITY");

  }

  public void PromptEditEvent(Event event){

    EditCalendarEventDialog dialog = new EditCalendarEventDialog();
    Bundle args = new Bundle();
    if (event != null){
      args.putParcelable(EditCalendarEventDialog.BUNDLE_CALENDAR_EVENT,
              event);
      dialog.setArguments(args);
    }
    dialog.setTargetFragment(this, DIALOG_CODE_CHANGE_WEEK_GRANULARITY);

    dialog.show(getFragmentManager(), "CHANGE_GRANULARITY");

  }

  @Override
  public void setCalendarName(String name) {
    String cleanedName = name.trim();
    viewModel.setCalendarName(cleanedName);
  }

  private static final int DIALOG_CODE_CHANGE_WEEK_GRANULARITY = 0;

  @Override
  public void handleSpinnerDialog(int code, String value) {
    switch(code){
      case DIALOG_CODE_CHANGE_WEEK_GRANULARITY:
        int numVal = Integer.parseInt(value);
        mAdapter.setEventGranularityFactor(numVal);
        return;
    }

  }

  @Override
  public void saveEve nt(Event event) {

  }
}
