package com.djdenpa.quickcalendar.views.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.CoreDataLayer;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.CalendarThumbnail;
import com.djdenpa.quickcalendar.models.DisplayMode;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;
import com.djdenpa.quickcalendar.models.SharedCalendar;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.activities.EditCalendarActivity;
import com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarEventDialog;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarNameDialog;
import com.djdenpa.quickcalendar.views.dialogs.GenericSingleSelectListDialog;
import com.djdenpa.quickcalendar.views.dialogs.GenericSingleSelectListTextMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditCalendarFragment extends Fragment
        implements EditCalendarNameDialog.EditCalendarNameListener,
        GenericSingleSelectListDialog.GenericSpinnerDialogListener,
        EditCalendarEventDialog.EditCalendarNameListener,
        CalendarWeekAdapter.CursorStateHandler {

  QuickCalendarDatabase mDB;

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
//  @BindView(R.id.iv_test)
//  ImageView ivTest;


  // this is for opening dialog
  private AppCompatActivity mActivity;
  private MenuEnabledHandler mMenuEnabledHandler;

  private int mCurrentYear = -1;
  private int mCurrentMonth = -1;

  CalendarWeekAdapter mAdapter;
  LinearLayoutManager mLayoutManager;

  // this is for throttling
  private long previousRVScrollTime = System.currentTimeMillis();
  private EditCalendarViewModel mViewModel;

  boolean mIsLargeLayout;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mDB = QuickCalendarDatabase.getInstance(getActivity().getApplicationContext());

    mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

    mViewModel = ViewModelProviders.of(getActivity()).get(EditCalendarViewModel.class);

    mViewModel.getActiveCalendar().observe(this, calendar -> {
      if (calendar.name.length() == 0) {
        tvCalendarName.setText(getString(R.string.calendar_untitled_name));
      } else {
        tvCalendarName.setText(calendar.name);
      }
      if (mMenuEnabledHandler != null) {
        mMenuEnabledHandler.toggleDeleteButton(mViewModel.activeCalendar.getValue().id != 0);
      }

      if (mViewModel.getIsFirebaseShareOn()) {
        QuickCalendarExecutors.getInstance().networkIO().execute(() -> pushToFirebase());
      }
    });
    mViewModel.getActiveEventSet().observe(this, eventSet -> {
      mAdapter.setData(eventSet);

      if (mViewModel.getIsFirebaseShareOn()) {
        QuickCalendarExecutors.getInstance().networkIO().execute(() -> pushToFirebase());
      }
    });

  }

  public void setActivity(EditCalendarActivity activity) {
    mActivity = activity;
    mMenuEnabledHandler = activity;
  }

  @Override
  public void onResume() {
    super.onResume();
    fabAddEvent.setEnabled(true);
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

    rvCalendarWeeks.setAdapter(mAdapter);

    llHeader.setOnClickListener(v -> mAdapter.handleTouchDate(-1, -1));

    fabAddEvent.setOnClickListener(v -> {
      fabAddEvent.setEnabled(false);
      openEditOnCursor();
    });

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

    initializeAdapterSettings();

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
    mMenuEnabledHandler.toggleSaveButton(true);

    EditCalendarNameDialog dialog = new EditCalendarNameDialog();
    Bundle args = new Bundle();
    args.putString(EditCalendarNameDialog.BUNDLE_CALENDAR_NAME,
            mViewModel.getActiveCalendar().getValue().name);
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

  public void handleClickEvent(Event event) {
    // on second click, this should edit
    if (mAdapter.setEventCursorLocalId(event.localId)) {
      PromptEditEvent(event);
    }
  }

  private long previousEditEventMillis = 0;
  public void PromptEditEvent(Event event){
    long currentMillis = System.currentTimeMillis();
    if (currentMillis - 1000 < previousEditEventMillis) {
      // clicked too fast. eat this input lol
      // spamming to open multiple dialogs is not great.
      return;
    }
    previousEditEventMillis = currentMillis;

    mMenuEnabledHandler.toggleSaveButton(true);

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
    mViewModel.setCalendarName(cleanedName);
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
    mViewModel.saveEventToActiveSet(event);
  }

  public void deleteEvent(int localId) {
    if (mViewModel.deleteEventFromActiveSet(localId)){
      Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void finishDialog() {
    fabAddEvent.setEnabled(true);
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

  @Override
  public void onCursorAlreadySelectedClick() {
    openEditOnCursor();
  }

  public void openEditOnCursor() {
    long timeUtc = mAdapter.getCursorDate().getTime().getTime();
    Event event = new Event();
    event.eventStartUTC = timeUtc;
    event.eventDurationMs = 1000*60*60*24;
    PromptEditEvent(event);
  }

  public void deleteCalendar() {
    QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
      mDB.calendarDao().deleteCalendar(mViewModel.activeCalendar.getValue());
      QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
        Toast.makeText(getContext(), "Calendar Deleted", Toast.LENGTH_SHORT).show();
        getActivity().finish();
      });
    });
  }

  public void setEntireCalendar(com.djdenpa.quickcalendar.models.Calendar calendar) {
    mViewModel.setEntireCalendar(calendar);
    mAdapter.notifyDataSetChanged();
    initializeAdapterSettings();
  }

  public void initializeAdapterSettings(){
    //fetch earliest event, it will be base scroll
    long earliestEventMillis = mViewModel.getActiveEventSet().getValue().getEarliestMillisUTC();
    mAdapter.setMidpointDateMillis(earliestEventMillis);
    java.util.Calendar earliestEventCal = java.util.Calendar.getInstance();
    earliestEventCal.setTimeInMillis(earliestEventMillis);
    updateCalendarFocusMonth(earliestEventCal);

    rvCalendarWeeks.scrollToPosition(CalendarWeekAdapter.START_POSITION );
  }

  public void toggleSaveButton(boolean isEnabled) {

    mMenuEnabledHandler.toggleSaveButton(isEnabled);
  }

  public void selectAdjacentEvent(int polarity) {
    Event event = mAdapter.selectAdjacentEvent(polarity);
    if (event == null) {
      return;
    }
    int position = mAdapter.getPositionOfDate(event.eventStartUTC);
    Log.i("test", ""+position);
    rvCalendarWeeks.scrollToPosition(position);
    mAdapter.setEventCursorLocalId(event.localId);
  }

  public void setEventSet(EventSet eventSet) {
    mAdapter.setTestEventSet(eventSet);
    mAdapter.notifyDataSetChanged();
  }

  private int setShareDataCount = 0;
  public void setShareData(String data){
    mViewModel.setCalendarShareData(data);
    mAdapter.notifyDataSetChanged();
    if (setShareDataCount <= 0) {
      saveSharedCalendar();
      setShareDataCount = 5;
    }
  }

  public void enableFirebaseShare() {
    mViewModel.setIsFirebaseShareOn(true);
    com.djdenpa.quickcalendar.models.Calendar calendar = mViewModel.getActiveCalendar().getValue();
    String hash = calendar.getFirebaseHash();
    String name = mViewModel.userName;
    if (name.length() <= 0) {
      name = mViewModel.identity;
    }

    pushToFirebase();

    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quick Calendar");
    String shareMessage= name
            + " wants to share their calendar. Open in Quick Calendar\n\n"
            + "https://raw.githubusercontent.com/eugenexlin/quick-calendar/master/index.html?hash=" + hash;
    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
    startActivity(Intent.createChooser(shareIntent, "choose one"));
  }

  private String mShareHash;
  public void setHash(String hash) {
    toggleSaveButton(true);
    mShareHash = hash;
  }

  public interface MenuEnabledHandler {
    void toggleSaveButton(boolean isEnabled);
    void toggleDeleteButton(boolean isEnabled);
  }

  public void saveCalendar() {
    saveCalendar(false);
  }
  public void saveCalendar(boolean startShare) {

    QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
      // set last access to now
      com.djdenpa.quickcalendar.models.Calendar calendar = mViewModel.activeCalendar.getValue();
      if (calendar.creatorIdentity == "") {
        calendar.creatorIdentity = mViewModel.identity;
      }
      if (calendar.id == 0) {
        calendar.EnsureShareCodeInitialized();
      }
      CoreDataLayer.saveCalendar(mDB, calendar);
      QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
        mMenuEnabledHandler.toggleSaveButton(false);
        Toast.makeText(getContext(), "Calendar Saved", Toast.LENGTH_SHORT).show();
      });
    });
  }

  public void saveSharedCalendar() {
    QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
      com.djdenpa.quickcalendar.models.Calendar calendar = mViewModel.activeCalendar.getValue();
      CoreDataLayer.saveSharedCalendar(mDB, calendar, mShareHash);
    });
  }

  public void pushToFirebase() {
    new pushToFirebaseTask().execute();
  }

  // AsyncTask for rubric yay.
  private class pushToFirebaseTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {

      com.djdenpa.quickcalendar.models.Calendar calendar = mViewModel.getActiveCalendar().getValue();
      FirebaseDatabase database = FirebaseDatabase.getInstance();
      DatabaseReference myRef = database.getReference("calendars/" + calendar.getFirebaseHash());
      myRef.setValue(calendar.getFirebaseSerialization());

      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      // here you can maybe provide feedback,
      // like play some indicator pulse animation
      // that means data was pushed.
    }
  }
}
