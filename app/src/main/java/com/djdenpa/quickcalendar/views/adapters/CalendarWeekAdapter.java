package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.DisplayMode;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;
import com.djdenpa.quickcalendar.utils.EventCollisionChecker;
import com.djdenpa.quickcalendar.utils.EventCollisionInfo;
import com.djdenpa.quickcalendar.views.fragments.EditCalendarFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

/*
 I want something that scrolls forever, but also take advantage of the
 recycling properties of this.
 scrolling will be implemented by using some large number of items,
 and starting at the mid point.
 and some re-syncing of offset method when straying too far from the midpoint
* */
public class CalendarWeekAdapter
        extends RecyclerView.Adapter<CalendarWeekViewHolder>
        implements CalendarWeekViewHolder.TouchDateHandler {

  public static final int ITEM_COUNT = 1000000;
  private static final String DIVIDER_VIEW_TAG = "DIVIDER_VIEW_TAG";
  public static int START_POSITION = ITEM_COUNT/2;

  private static final long MILLIS_PER_DAY = (1000 * 60 * 60 * 24);

  public static int FIRST_EVENT_LAYER_OFFSET = 70;

  public static int EVENT_BAR_HEIGHT = 64;
  public static int EVENT_BAR_MARGIN = 5;
  public static int getEventCursorThickness() {
    return EVENT_BAR_MARGIN*2/5;
  }
  public static int getEventCursorHeight() {
    return EVENT_BAR_MARGIN*4/5;
  }

  private int mEventGranularityFactor;

  // this is the cursor so if they want to visually select a date on the calendar,
  // they can just poke it.
  private java.util.Calendar mDateCursor;
  private int mDateCursorPosition;
  private int mDateCursorIndex;

  private int mEventCursorLocalId = -1;
  public int getEventCursorLocalId() {
    return mEventCursorLocalId;
  }

  public boolean setEventCursorLocalId (int cursorLocalId) {
    boolean requestFocus = true;
    if (mEventCursorLocalId == cursorLocalId) {
      return true;
    }
    mEventCursorLocalId = cursorLocalId;
    for (CalendarWeekViewHolder holder: mAllViewHolders) {
      if (holder.setEventCursor(cursorLocalId, false)){
        requestFocus = false;
      }
    }
    return false;
  }

  private DisplayMode mDisplayMode;

  public int getEventGranularityFactor(){
    return mEventGranularityFactor;
  }
  public void setEventGranularityFactor(int value){
    mEventGranularityFactor = value;
    SharedPreferences sharedPref = mContext.getSharedPreferences(
            mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt(getGranularityPreferenceKey(), value);
    editor.commit();
    notifyDataSetChanged();
  }
  public DisplayMode getDisplayMode(){
    return mDisplayMode;
  }
  public void setDisplayMode(DisplayMode mode){
    mDisplayMode = mode;
    SharedPreferences sharedPref = mContext.getSharedPreferences(
            mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    mEventGranularityFactor = sharedPref.getInt(getGranularityPreferenceKey(), 4);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt(mContext.getString(R.string.preference_calendar_display_mode), DisplayMode.toInt(mode));
    editor.commit();
    notifyDataSetChanged();
  }

  private String getGranularityPreferenceKey(){
    if (mDisplayMode == DisplayMode.ROW_PER_DAY){
      return mContext.getString(R.string.preference_calendar_day_granularity);
    }
    return mContext.getString(R.string.preference_calendar_week_granularity);
  }

  private java.util.Calendar mMidpointDate = Calendar.getInstance();
  private EventSet mEventSet;

  private int mHighlightMonth;

  private Context mContext;
  private EditCalendarFragment mFragment;
  private LayoutInflater mLayoutInflater;

  public CalendarWeekAdapter(EditCalendarFragment fragment){
    mFragment = fragment;
    mContext = fragment.getContext();
    mLayoutInflater = LayoutInflater.from(mContext);
    SharedPreferences sharedPref = mContext.getSharedPreferences(
            mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    mDisplayMode = DisplayMode.fromInt(sharedPref.getInt(mContext.getString(R.string.preference_calendar_display_mode), 0));
    int defaultGranularity;
    if (mDisplayMode == DisplayMode.ROW_PER_WEEK) {
      defaultGranularity = 1;
    } else {
      defaultGranularity = 24;
    }
    FIRST_EVENT_LAYER_OFFSET = mContext.getResources().getDimensionPixelSize(R.dimen.event_item_first_offset);
    EVENT_BAR_HEIGHT = mContext.getResources().getDimensionPixelSize(R.dimen.event_item_height);
    EVENT_BAR_MARGIN = mContext.getResources().getDimensionPixelSize(R.dimen.event_item_margin);
    mEventGranularityFactor = sharedPref.getInt(getGranularityPreferenceKey(), defaultGranularity);
  }

  LinkedList<CalendarWeekViewHolder> mAllViewHolders = new LinkedList<>();

  @NonNull
  @Override
  public CalendarWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    View view = mLayoutInflater.inflate(R.layout.calendar_edit_week_item, parent, false);
    CalendarWeekViewHolder holder = new CalendarWeekViewHolder(view, context);
    holder.setTouchHandler(this);
    mAllViewHolders.add(holder);
    return holder;
  }

  public java.util.Calendar getItemBaseDate(int position) {

    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate.getTime());
    if (mDisplayMode == DisplayMode.ROW_PER_DAY) {
      javaCal.add(java.util.Calendar.DAY_OF_YEAR, position - START_POSITION);
    } else {
      javaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);
    }

    return javaCal;
  }

  // returns a decimal that represents how close it is to the month integer
  public double getAverageMonthValue(int startPos, int endPos) {

    java.util.Calendar javaCal = getItemBaseDate(startPos);
    int positionDiff = endPos - startPos;
    double sum = 0;
    double count = positionDiff;
    if (mDisplayMode == DisplayMode.ROW_PER_WEEK) {
      count *= 7.0;
    }
    for (int i = 0; i < count; i++) {
      javaCal.add(java.util.Calendar.DATE, 1);
      sum += javaCal.get(java.util.Calendar.MONTH);
    }

    return sum/count;
  }


  @Override
  public void onBindViewHolder(@NonNull CalendarWeekViewHolder holder, int position) {

    switch (mDisplayMode){
      case ROW_PER_WEEK:
        BindDataByWeek(holder, position);
        break;
      case ROW_PER_DAY:
        BindDataByDay(holder, position);
        break;
    }

    holder.setCurrentPosition(position);
    holder.resetEventCursor();
    holder.setEventCursor(mEventCursorLocalId, false);
    holder.resynchronizeDateNumberVisuals(
            mHighlightMonth,
            mDisplayMode,
            mDateCursorPosition,
            mDateCursorIndex
    );
  }

  private void BindDataByDay(CalendarWeekViewHolder holder, int position) {

    java.util.Calendar baseDate = getItemBaseDate(position);

    int verticalLinePosition = 0;
    ConstraintLayout parent = holder.getParentLayout();
    ConstraintSet constraintSet = new ConstraintSet();

    int dateNumber = baseDate.get(java.util.Calendar.DATE);
    int dayOfWeek = baseDate.get(Calendar.DAY_OF_WEEK);
    int month = baseDate.get(java.util.Calendar.MONTH);

    TextView view = holder.getDayTextField(0);
//    view.setText(String.format(mContext.getString(R.string.day_mode_date_string_format), String.valueOf(dateNumber), getDayModeDayOfWeek(dayOfWeek)));
    view.setText(String.valueOf(dateNumber));
    view.setTag(R.id.tag_tv_month_key, month);

    // clear out all texts except the first
    for (int i = 1; i < 7; i++) {
      TextView otherView = holder.getDayTextField(i);
      otherView.setText("");
    }
    holder.setVerticalDividerPosition(verticalLinePosition);

    long dayStartMillis = baseDate.getTimeInMillis();
    EventCollisionChecker oECC = new EventCollisionChecker();
    // bind the events
    for (Event event : mEventSet.getAllEvents()){
      // does event fall on this week
      // default to inverted left right position
      // they must return to neutral for us to render the event.
      long beginOffsetMillis = event.eventStartUTC - dayStartMillis;
      int beginPosition = (int) Math.floor(
              (double)beginOffsetMillis/MILLIS_PER_DAY*mEventGranularityFactor);
      int endOffsetPositions = (int) (Math.ceil(event.eventDurationMs/MILLIS_PER_DAY)*mEventGranularityFactor);
      int endPosition = beginPosition + endOffsetPositions;
      //if out of bound, do not draw
      if (beginPosition > mEventGranularityFactor){
        continue;
      }
      // we say less than equal because
      // if it ends on midnight,
      // we probably don't want a dink to get on the next day
      if (endPosition <= 0){
        continue;
      }
      // otherwise clip to the limits of this row.
      if (beginPosition < 0){
        beginPosition = 0;
      }
      if (endPosition > mEventGranularityFactor){
        endPosition = mEventGranularityFactor;
      }

      //make sure the event is never less than a unit size
      if (beginPosition == endPosition){
        endPosition += 1;
      }

      EventCollisionInfo eventInfo = new EventCollisionInfo();
      eventInfo.beginPosition = beginPosition;
      eventInfo.endPosition = endPosition;
      eventInfo.event = event;
      oECC.findAvailableSlotAndInsert(eventInfo);

      CalendarEventViewManager eventItem = holder.getViewFromPoolOrCreate(mContext, holder);
      eventItem.setEventData(event, mFragment);
      constraintSet.clone(parent);

      int offsetTop = (EVENT_BAR_HEIGHT + EVENT_BAR_MARGIN) * eventInfo.layer;

      Guideline guidelineStart = holder.getHighResDayGuideline(beginPosition, mEventGranularityFactor);
      Guideline guidelineEnd = holder.getHighResDayGuideline(endPosition, mEventGranularityFactor);

      constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, FIRST_EVENT_LAYER_OFFSET + offsetTop);
      constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.START, guidelineStart.getId(), ConstraintSet.START, 2);
      constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.END, guidelineEnd.getId(), ConstraintSet.START, 3);

      eventItem.marginTop = FIRST_EVENT_LAYER_OFFSET + offsetTop;
      eventItem.guidelineStart = guidelineStart;
      eventItem.guidelineEnd = guidelineEnd;

      constraintSet.applyTo(parent);

    }

    CreatePlaceHolderEventItem(constraintSet, holder,oECC.currentMax+1);
    holder.setEventCursor(mEventCursorLocalId,false);
  }

  private void CreatePlaceHolderEventItem(ConstraintSet constraintSet, CalendarWeekViewHolder holder, int layer) {
    ConstraintLayout parent = holder.getParentLayout();
    CalendarEventViewManager eventItem = holder.getViewFromPoolOrCreate(mContext, holder);
    eventItem.setAsPlaceholder();
    constraintSet.clone(parent);
    int offsetTop = (EVENT_BAR_HEIGHT + EVENT_BAR_MARGIN) * layer;
    constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, FIRST_EVENT_LAYER_OFFSET + offsetTop);
    constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.START, holder.getGuideline(0).getId(), ConstraintSet.START, 0);
    constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.END, holder.getGuideline(0).getId(), ConstraintSet.START, 0);
    constraintSet.applyTo(parent);
  }

  private void BindDataByWeek(CalendarWeekViewHolder holder, int position){

    java.util.Calendar baseDate = getItemBaseDate(position);

    int verticalLinePosition = 0;
    ConstraintLayout parent = holder.getParentLayout();
    ConstraintSet constraintSet = new ConstraintSet();
    // first bind the days of the week
    for (int i = 0; i < 7; i++) {
      java.util.Calendar dayOfWeek = (java.util.Calendar) baseDate.clone();
      dayOfWeek.add(java.util.Calendar.DAY_OF_YEAR, i);
      int dateNumber = dayOfWeek.get(java.util.Calendar.DATE);
      int month = dayOfWeek.get(java.util.Calendar.MONTH);

      TextView view = holder.getDayTextField(i);
      view.setText(String.valueOf(dateNumber));
      view.setTag(R.id.tag_tv_month_key, month);

      // special check for if date is 1, so we can draw a line
      if (dateNumber == 1) {
        verticalLinePosition = i;
      }

    }
    holder.setVerticalDividerPosition(verticalLinePosition);

    long weekStartMillis = baseDate.getTimeInMillis();
    EventCollisionChecker oECC = new EventCollisionChecker();
    // bind the events
    for (Event event : mEventSet.getAllEvents()){
      // does event fall on this week
      // default to inverted left right position
      // they must return to neutral for us to render the event.
      long beginOffsetMillis = event.eventStartUTC - weekStartMillis;
      int beginPosition = (int) Math.floor(
              (double)beginOffsetMillis/MILLIS_PER_DAY*mEventGranularityFactor);
      int endOffsetPositions = (int) (Math.ceil(event.eventDurationMs/MILLIS_PER_DAY)*mEventGranularityFactor);
      int endPosition = beginPosition + endOffsetPositions;
      //if out of bound, do not draw
      if (beginPosition > 7*mEventGranularityFactor){
        continue;
      }
      if (endPosition <= 0){
        continue;
      }
      // otherwise clip to the limits of this row.
      if (beginPosition < 0){
        beginPosition = 0;
      }
      if (endPosition > 7*mEventGranularityFactor){
        endPosition = 7*mEventGranularityFactor;
      }

      //make sure the event is never less than a unit size
      if (beginPosition == endPosition){
        endPosition += 1;
      }

      EventCollisionInfo eventInfo = new EventCollisionInfo();
      eventInfo.beginPosition = beginPosition;
      eventInfo.endPosition = endPosition;
      eventInfo.event = event;
      oECC.findAvailableSlotAndInsert(eventInfo);

      CalendarEventViewManager eventItem = holder.getViewFromPoolOrCreate(mContext, holder);
      eventItem.setEventData(event, mFragment);
      constraintSet.clone(parent);

      int offsetTop = (EVENT_BAR_HEIGHT + EVENT_BAR_MARGIN) * eventInfo.layer;

      Guideline guidelineStart = holder.getHighResWeekGuideline(beginPosition, mEventGranularityFactor);
      Guideline guidelineEnd = holder.getHighResWeekGuideline(endPosition, mEventGranularityFactor);

      constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, FIRST_EVENT_LAYER_OFFSET + offsetTop);
      constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.START, guidelineStart.getId(), ConstraintSet.START, 2);
      constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.END, guidelineEnd.getId(), ConstraintSet.START, 3);

      eventItem.marginTop = FIRST_EVENT_LAYER_OFFSET + offsetTop;
      eventItem.guidelineStart = guidelineStart;
      eventItem.guidelineEnd = guidelineEnd;

      constraintSet.applyTo(parent);

    }
    CreatePlaceHolderEventItem(constraintSet, holder,oECC.currentMax+1);
    holder.setEventCursor(mEventCursorLocalId,false);
  }

  // this takes into account the display mode.
  // used to preserve and restore scroll date position when changing display modes.
  public int getPositionOfDate(java.util.Calendar date){
    return getPositionOfDate(date.getTime().getTime());
  }
  public int getPositionOfDate(long UTC){
    long diffInMillis = UTC - mMidpointDate.getTime().getTime();
    boolean isNegative = diffInMillis < 0;

    long positionShift;
    if (mDisplayMode == DisplayMode.ROW_PER_DAY) {
      positionShift = diffInMillis / MILLIS_PER_DAY;
    } else {
      positionShift = diffInMillis / MILLIS_PER_DAY / 7;
    }
    return (int) (START_POSITION + positionShift - (isNegative?1:0));
  }

  @Override
  public void onViewRecycled(@NonNull CalendarWeekViewHolder holder) {
    super.onViewRecycled(holder);
    ConstraintLayout parent = holder.getParentLayout();
    for (int i = 0; i < parent.getChildCount(); i++) {
      View view = parent.getChildAt(i);
      if (view.getTag(R.id.tag_gv_view_manager) != null) {
        CalendarEventViewManager managedView = (CalendarEventViewManager) view.getTag(R.id.tag_gv_view_manager);
        holder.RecycleViewToPool(managedView);
        continue;
      }
      if (view.getTag() == DIVIDER_VIEW_TAG) {
        parent.removeView(view);
        i--;
        continue;
      }
    }
  }

  public void setData(EventSet eventSet){
    mEventSet = eventSet;
  }

  public void setMidpointDateMillis(long millis){
    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTimeInMillis(millis);
    javaCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    javaCal.set(java.util.Calendar.MINUTE, 0);
    javaCal.set(java.util.Calendar.SECOND, 0);
    javaCal.set(java.util.Calendar.MILLISECOND, 0);
    javaCal.add(java.util.Calendar.DAY_OF_WEEK, -(javaCal.get(java.util.Calendar.DAY_OF_WEEK)-1));
    mMidpointDate = javaCal;
  }

  @Override
  public int getItemCount() {
    return ITEM_COUNT;
  }

  public void setHighlightMonth(int month) {
    mHighlightMonth = month;
    resynchronizeAllDateNumberVisuals();
  }

  public String getDayModeDayOfWeek(int position){
    switch(position) {
      case 0:
        return mContext.getString(R.string.day_mode_day_of_week_0);
      case 1:
        return mContext.getString(R.string.day_mode_day_of_week_1);
      case 2:
        return mContext.getString(R.string.day_mode_day_of_week_2);
      case 3:
        return mContext.getString(R.string.day_mode_day_of_week_3);
      case 4:
        return mContext.getString(R.string.day_mode_day_of_week_4);
      case 5:
        return mContext.getString(R.string.day_mode_day_of_week_5);
      case 6:
        return mContext.getString(R.string.day_mode_day_of_week_6);
      default:
        return mContext.getString(R.string.day_mode_day_of_week_0);
    }
  }

  public java.util.Calendar getCursorDate() {
    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    if (mDateCursor != null) {
      javaCal.setTime(mDateCursor.getTime());
    } else {
      javaCal.setTime(mMidpointDate.getTime());
    }
    return javaCal;
  }

  @Override
  public void handleTouchDate(int position, int index) {
    if (position < 0) {
      clearCursor();
      return;
    }
    setEventCursorLocalId(-1);

    if (mDateCursorPosition == position && mDateCursorIndex == index) {
      // no change. do nothing;
      return;
    }
    mDateCursorPosition = position;
    mDateCursorIndex = index;

    Calendar date = getItemBaseDate(position);
    switch (mDisplayMode){
      case ROW_PER_WEEK:
        //only add index days if it is in week item mode
        date.add(Calendar.DAY_OF_MONTH, index);
        break;
      case ROW_PER_DAY:
        break;
    }

    mDateCursor = date;
    mCursorStateHandler.onSetCursorVisibility(true);
    resynchronizeAllDateNumberVisuals();
  }

  public void clearCursor() {
    mDateCursorPosition = -1;
    mCursorStateHandler.onSetCursorVisibility(false);
    resynchronizeAllDateNumberVisuals();
  }

  public void resynchronizeAllDateNumberVisuals() {
    for (CalendarWeekViewHolder holder: mAllViewHolders) {
      holder.resynchronizeDateNumberVisuals(
              mHighlightMonth,
              mDisplayMode,
              mDateCursorPosition,
              mDateCursorIndex
      );
    }
  }


  public CursorStateHandler mCursorStateHandler;

  public Event selectAdjacentEvent(int polarity) {
    Event event = mEventSet.getEventByLocalId(mEventCursorLocalId);
    if (event == null) {
      long cursorUTC = getCursorDate().getTime().getTime();
      event = mEventSet.getEventNearDate(cursorUTC, polarity);
      if (event != null){
        setEventCursorLocalId(event.localId);
      }
      clearCursor();
      return event;
    }
    Event nextEvent = mEventSet.getAdjacentEvent(event, polarity);
    if (nextEvent.localId != event.localId){
      setEventCursorLocalId(nextEvent.localId);
      clearCursor();
    }
    return nextEvent;
  }

  public interface CursorStateHandler {
    void onSetCursorVisibility(boolean isVisible);
  }
  public void setCursorStateHandler(CursorStateHandler handler) {
    mCursorStateHandler = handler;
  }

  public void setTestEventSet(EventSet eventSet) {
    mEventSet.replaceAllEvents(eventSet.getAllEvents());
  }

}

