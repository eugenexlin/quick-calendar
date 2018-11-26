package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/*
 I want something that scrolls forever, but also take advantage of the
 recycling properties of this.
 scrolling will be implemented by using some large number of items,
 and starting at the mid point.
 and some re-syncing of offset method when straying too far from the midpoint
* */
public class CalendarWeekAdapter extends RecyclerView.Adapter<CalendarWeekViewHolder> {

  public static final int ITEM_COUNT = 1000000;
  private static final String DIVIDER_VIEW_TAG = "DIVIDER_VIEW_TAG";
  public static int START_POSITION = ITEM_COUNT/2;

  private static final double MILLIS_PER_DAY = (1000 * 60 * 60 * 24);
  public static final int EVENT_BAR_HEIGHT = 56;
  public static final int EVENT_BAR_MARGIN = 5;

  private int mEventGranularityFactor;

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

  private java.util.Calendar mMidpointDate;
  private EventSet mEventSet;

  private int mHighlightMonth;

  private Context mContext;
  private LayoutInflater mLayoutInflater;

  public CalendarWeekAdapter(Context context){
    mContext = context;
    mLayoutInflater = LayoutInflater.from(context);
    SharedPreferences sharedPref = mContext.getSharedPreferences(
            mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    mDisplayMode = DisplayMode.fromInt(sharedPref.getInt(mContext.getString(R.string.preference_calendar_display_mode), 0));
    mEventGranularityFactor = sharedPref.getInt(getGranularityPreferenceKey(), 4);
  }

  LinkedList<CalendarWeekViewHolder> mAllViewHolders = new LinkedList<>();

  @NonNull
  @Override
  public CalendarWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    View view = mLayoutInflater.inflate(R.layout.calendar_edit_week_item, parent, false);
    CalendarWeekViewHolder holder = new CalendarWeekViewHolder(view, context);
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

    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate.getTime());
    javaCal.add(java.util.Calendar.WEEK_OF_YEAR, startPos - START_POSITION);
    int weeksDifference = endPos - startPos;
    double sum = 0;
    double count = weeksDifference * 7.0;
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

    holder.resynchronizeHighlightMonth(mHighlightMonth);
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
    view.setText(String.valueOf(dateNumber) + " " + getDayModeDayOfWeek(dayOfWeek));
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
      long endOffsetMillis = beginOffsetMillis + event.eventDurationMs;
      int beginPosition = (int) Math.floor(
              (double)beginOffsetMillis/MILLIS_PER_DAY*mEventGranularityFactor);
      int endPosition = (int) Math.ceil(
              (double)endOffsetMillis/MILLIS_PER_DAY*mEventGranularityFactor);
      //if out of bound, do not draw
      if (beginPosition > mEventGranularityFactor){
        continue;
      }
      if (endPosition < 0){
        continue;
      }
      // otherwise clip to the limits of this row.
      if (beginPosition < 0){
        beginPosition = 0;
      }
      if (endPosition > mEventGranularityFactor){
        endPosition = mEventGranularityFactor;
      }
      // sanity check
      if (beginPosition < endPosition) {
        EventCollisionInfo eventInfo = new EventCollisionInfo();
        eventInfo.beginPosition = beginPosition;
        eventInfo.endPosition = endPosition;
        eventInfo.event = event;
        oECC.findAvailableSlotAndInsert(eventInfo);

        CalendarEventViewManager eventItem = holder.getViewFromPoolOrCreate(mContext, holder);
        eventItem.setEventData(event);
        constraintSet.clone(parent);

        int offsetTop = (EVENT_BAR_HEIGHT + EVENT_BAR_MARGIN) * eventInfo.layer;

        constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 60 + offsetTop);
        constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.START, holder.getHighResDayGuideline(beginPosition, mEventGranularityFactor).getId(), ConstraintSet.START, 2);
        constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.END, holder.getHighResDayGuideline(endPosition, mEventGranularityFactor).getId(), ConstraintSet.START, 3);

        constraintSet.applyTo(parent);

      }
    }
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
      long endOffsetMillis = beginOffsetMillis + event.eventDurationMs;
      int beginPosition = (int) Math.floor(
              (double)beginOffsetMillis/MILLIS_PER_DAY*mEventGranularityFactor);
      int endPosition = (int) Math.ceil(
              (double)endOffsetMillis/MILLIS_PER_DAY*mEventGranularityFactor);
      //if out of bound, do not draw
      if (beginPosition > 7*mEventGranularityFactor){
        continue;
      }
      if (endPosition < 0){
        continue;
      }
      // otherwise clip to the limits of this row.
      if (beginPosition < 0){
        beginPosition = 0;
      }
      if (endPosition > 7*mEventGranularityFactor){
        endPosition = 7*mEventGranularityFactor;
      }
      // sanity check
      if (beginPosition < endPosition) {
        EventCollisionInfo eventInfo = new EventCollisionInfo();
        eventInfo.beginPosition = beginPosition;
        eventInfo.endPosition = endPosition;
        eventInfo.event = event;
        oECC.findAvailableSlotAndInsert(eventInfo);

        CalendarEventViewManager eventItem = holder.getViewFromPoolOrCreate(mContext, holder);
        eventItem.setEventData(event);
        constraintSet.clone(parent);

        int offsetTop = (EVENT_BAR_HEIGHT + EVENT_BAR_MARGIN) * eventInfo.layer;

        constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 60 + offsetTop);
        constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.START, holder.getHighResWeekGuideline(beginPosition, mEventGranularityFactor).getId(), ConstraintSet.START, 2);
        constraintSet.connect(eventItem.clRoot.getId(), ConstraintSet.END, holder.getHighResWeekGuideline(endPosition, mEventGranularityFactor).getId(), ConstraintSet.START, 3);

        constraintSet.applyTo(parent);

      }
    }
  }

  // this takes into account the display mode.
  // used to preserve and restore scroll date position when changing display modes.
  public int getPositionOfDate(java.util.Calendar date){
    java.util.Calendar baseDate = java.util.Calendar.getInstance();
    baseDate.setTime(mMidpointDate.getTime());

    double diffInMillis = date.getTime().getTime() - baseDate.getTime().getTime();

    if (mDisplayMode == DisplayMode.ROW_PER_DAY) {
      double diffDays = diffInMillis/ MILLIS_PER_DAY;
      return (int) (START_POSITION + Math.ceil(diffDays));
    } else {
      double diffWeeks = diffInMillis/ MILLIS_PER_DAY / 7;
      return (int) (START_POSITION + Math.ceil(diffWeeks));
    }
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

    for (CalendarWeekViewHolder holder: mAllViewHolders) {

      for (int i = 0; i < 7; i++) {
        TextView view = holder.getDayTextField(i);
        if (view == null) {
          continue;
        }
        if (view.getTag(R.id.tag_tv_month_key) == null){
          continue;
        }
        int viewMonth = (int) view.getTag(R.id.tag_tv_month_key);
        if (viewMonth == mHighlightMonth) {
          view.setTextColor(mContext.getColor(R.color.darker_gray));
          view.setTypeface(null, Typeface.BOLD);
        }else{
          view.setTextColor(mContext.getColor(R.color.lighter_gray));
          view.setTypeface(null, Typeface.NORMAL);
        }
      }

    }
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

}
