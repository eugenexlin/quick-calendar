package com.djdenpa.quickcalendar.views.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.DisplayMode;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.activities.EditCalendarActivity;
import com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarEventDialog;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarNameDialog;
import com.djdenpa.quickcalendar.views.dialogs.GenericSingleSelectListDialog;
import com.djdenpa.quickcalendar.views.dialogs.GenericSingleSelectListTextMapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditCalendarFragment extends Fragment
        implements EditCalendarNameDialog.EditCalendarNameListener,
        GenericSingleSelectListDialog.GenericSpinnerDialogListener,
        EditCalendarEventDialog.EditCalendarNameListener,
        CalendarWeekAdapter.CursorStateHandler {

  private Unbinder unbinder;
  @BindView(R.id.tv_calendar_name)
  TextView tvCalendarName;
  @BindView(R.id.rv_calendar_weeks)
  RecyclerView rvCalendarWeeks;
  @BindView(R.id.tv_calendar_floating_tag)
  TextView tvCalendarFloatingTag;
  @BindView(R.id.cl_calendar_week_header)
  ConstraintLayout clCalendarWeekHeader;
  @BindView(R.id.fab_add_event)
  FloatingActionButton fabAddEvent;
  @BindView(R.id.ll_calendar_header)
  LinearLayout llHeader;

  // this is for opening dialog
  private AppCompatActivity mActivity;

  private int mCurrentYear = -1;
  private int mCurrentMonth = -1;

  CalendarWeekAdapter mAdapter;
  LinearLayoutManager mLayoutManager;

  // this is for throttling
  private long previousRVScrollTime = System.currentTimeMillis();
  private EditCalendarViewModel viewModel;

  boolean mIsLargeLayout;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = ViewModelProviders.of(getActivity()).get(EditCalendarViewModel.class);
    // test data
    if (viewModel.isFirstEntry) {
      viewModel.init();
      viewModel.setEntireCalendar(
              new MockCalendarDataGenerator().getMockCalendar());
    }

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

    mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
  }

  public void setActivity(EditCalendarActivity activity) {
    mActivity = activity;
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

    mAdapter = new CalendarWeekAdapter(this);
    mAdapter.setCursorStateHandler(this);
    //fetch earliest event, it will be base scroll
    long earliestEventMillis = viewModel.getActiveCalendar().getValue().getFirstEventSet().getEarliestMillisUTC();
    mAdapter.setMidpointDateMillis(earliestEventMillis);
    java.util.Calendar earliestEventCal = java.util.Calendar.getInstance();
    earliestEventCal.setTimeInMillis(earliestEventMillis);
    updateCalendarFocusMonth(earliestEventCal);

    rvCalendarWeeks.setAdapter(mAdapter);

    llHeader.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mAdapter.handleTouchDate(-1, -1);
      }
    });

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

    if (mAdapter.getDisplayMode() == DisplayMode.ROW_PER_WEEK) {
      clCalendarWeekHeader.setVisibility(View.VISIBLE);
    } else {
      clCalendarWeekHeader.setVisibility(View.GONE);
    }

    setWeekHeaderVisibilityBasedOnMode();
    viewModel.isFirstEntry = false;

    return rootView;
  }

  private void setWeekHeaderVisibilityBasedOnMode(){
    if (mAdapter.getDisplayMode() == DisplayMode.ROW_PER_WEEK) {
      clCalendarWeekHeader.setVisibility(View.VISIBLE);
    } else {
      clCalendarWeekHeader.setVisibility(View.GONE);
    }
  }

  // here we use a funny algorithm of if the number becomes
  // a bit too far from the current month,
  // set it to a new value
  private void checkCalendarFocusMonth() {
    int start = mLayoutManager.findFirstVisibleItemPosition();
    int end = mLayoutManager.findLastVisibleItemPosition();
    int midPosition = (start + end)/2;

    if (tvCalendarFloatingTag.getText().length() == 0 ||
        Math.abs(mAdapter.getAverageMonthValue(start, end) - (double)mCurrentMonth) > 0.60) {
      java.util.Calendar jCal = mAdapter.getItemBaseDate(midPosition);
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

  public void PromptChangeView(){

    GenericSingleSelectListDialog dialog = new GenericSingleSelectListDialog();
    Bundle args = new Bundle();
    args.putString(GenericSingleSelectListDialog.BUNDLE_STRING_CURRENT_VALUE,
            String.valueOf(DisplayMode.toInt(mAdapter.getDisplayMode())));
    args.putInt(GenericSingleSelectListDialog.BUNDLE_INT_STRING_TITLE,
            R.string.menu_change_view);
    args.putInt(GenericSingleSelectListDialog.BUNDLE_INT_ARRAY_ID,
            R.array.calendar_modes);
    dialog.setArguments(args);
    dialog.setTargetFragment(this, DIALOG_CODE_CHANGE_DISPLAY_MODE);

    dialog.mapper = new GenericSingleSelectListTextMapper();
    dialog.mapper.mMapperItems.add(
            new GenericSingleSelectListTextMapper.MapperItem(
                    String.valueOf(DisplayMode.toInt(DisplayMode.ROW_PER_WEEK)),
                    getString(R.string.calendar_modes_row_per_week))
    );
    dialog.mapper.mMapperItems.add(
            new GenericSingleSelectListTextMapper.MapperItem(
                    String.valueOf(DisplayMode.toInt(DisplayMode.ROW_PER_DAY)),
                    getString(R.string.calendar_modes_row_per_day))
    );

    dialog.show(getFragmentManager(), "CHANGE_VIEW");

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

    FragmentManager fragmentManager = mActivity.getSupportFragmentManager();

    if (mIsLargeLayout) {
      dialog.show(fragmentManager, "EDIT_EVENT");
    } else {
      // The device is smaller, so show the fragment fullscreen
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      // For a little polish, specify a transition animation
      transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
      // To make it fullscreen, use the 'content' root view as the container
      // for the fragment, which is always the root view for the activity
      transaction.replace(android.R.id.content, dialog, "EDIT_EVENT")
              .addToBackStack("EDIT_EVENT").commit();
    }
  }

  @Override
  public void setCalendarName(String name) {
    String cleanedName = name.trim();
    viewModel.setCalendarName(cleanedName);
  }

  private static final int DIALOG_CODE_CHANGE_WEEK_GRANULARITY = 0;
  private static final int DIALOG_CODE_CHANGE_DISPLAY_MODE = 1;

  @Override
  public void handleSpinnerDialog(int code, String value) {
    int numVal;
    switch(code){
      case DIALOG_CODE_CHANGE_WEEK_GRANULARITY:
        numVal = Integer.parseInt(value);
        mAdapter.setEventGranularityFactor(numVal);
        return;
      case DIALOG_CODE_CHANGE_DISPLAY_MODE:
        numVal = Integer.parseInt(value);

        java.util.Calendar saveDate = mAdapter.getItemBaseDate(mLayoutManager.findFirstCompletelyVisibleItemPosition());

        DisplayMode mode = DisplayMode.fromInt(numVal);
        mAdapter.setDisplayMode(mode);

        setWeekHeaderVisibilityBasedOnMode();

        int newPosition = mAdapter.getPositionOfDate(saveDate);
        rvCalendarWeeks.scrollToPosition(newPosition);

        return;
    }

  }

  @Override
  public void saveEvent(Event event) {
    viewModel.getActiveEventSet().saveEvent(event);
    mAdapter.notifyDataSetChanged();
  }


  public boolean isFABVisible = false;
  @Override
  public void onSetCursorVisibility(boolean isVisible) {
    if (isVisible) {
      if (!isFABVisible){
        isFABVisible = true;
        fabAddEvent.setVisibility(View.VISIBLE);
        fabAddEvent.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.crispy_show));
      }
    }else{
      if (isFABVisible){
        isFABVisible = false;
        fabAddEvent.setVisibility(View.INVISIBLE);
        fabAddEvent.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.crispy_hide));
      }
    }
  }
}
