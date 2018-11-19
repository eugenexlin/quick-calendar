package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.EventCollisionChecker;
import com.djdenpa.quickcalendar.utils.EventCollisionInfo;

/*
 I want something that scrolls forever, but also take advantage of the
 recycling properties of this.
 scrolling will be implemented by using some large number of items,
 and starting at the mid point.
 and some re-syncing of offset method when straying too far from the midpoint
* */
public class CalendarWeekAdapter extends RecyclerView.Adapter<CalendarWeekViewHolder> {

  public static final int ITEM_COUNT = 5000;
  public static int START_POSITION = ITEM_COUNT/2;

  private static final double MILLIS_PER_DAY = (1000 * 60 * 60 * 24);
  public static final int EVENT_BAR_HEIGHT = 40;
  public static final int EVENT_BAR_MARGIN = 5;

  private int mEventGranularityFactor;

  public int getEventGranularityFactor(){
    return mEventGranularityFactor;
  }
  public void setEventGranularityFactor(int value){
    mEventGranularityFactor = value;
    SharedPreferences sharedPref = mContext.getSharedPreferences(
            mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt(mContext.getString(R.string.preference_calendar_week_granularity), value);
    editor.commit();
    notifyDataSetChanged();
  }

  private java.util.Calendar mMidpointDate;
  private Calendar mCalendar;

  private int mHighlightMonth;

  private Context mContext;
  private LayoutInflater mLayoutInflater;

  // private HashSet<View> managedViews = new HashSet<>();
  private static final String MANAGED_VIEW_TAG = "MANAGED_VIEW_TAG";

  private class ManagedViewTag {
    public int month;
  }

  public CalendarWeekAdapter(Context context){
    mContext = context;
    mLayoutInflater = LayoutInflater.from(context);
    SharedPreferences sharedPref = mContext.getSharedPreferences(
            mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    mEventGranularityFactor = sharedPref.getInt(mContext.getString(R.string.preference_calendar_week_granularity), 4);

  }

  @NonNull
  @Override
  public CalendarWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    View view = mLayoutInflater.inflate(R.layout.calendar_edit_week_item, parent, false);

    return new CalendarWeekViewHolder(view, context);
  }

  public java.util.Calendar getMonthYear(int position) {

    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate.getTime());
    javaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);

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

    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate.getTime());
    javaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);

    ConstraintLayout parent = holder.getParentLayout();
    ConstraintSet constraintSet = new ConstraintSet();
    // first bind the days of the week
    for (int i = 0; i < 7; i++) {
      java.util.Calendar dayOfWeek = (java.util.Calendar) javaCal.clone();
      dayOfWeek.add(java.util.Calendar.DAY_OF_YEAR, i);
      int dateNumber = dayOfWeek.get(java.util.Calendar.DATE);
      int month = dayOfWeek.get(java.util.Calendar.MONTH);

      TextView view = holder.getDayTextField(i);
      view.setText(String.valueOf(dateNumber));
      view.setTag(R.id.tag_tv_month_key, month);
      if (month == mHighlightMonth) {
        view.setTextColor(mContext.getColor(R.color.darker_gray));
        view.setTypeface(null, Typeface.BOLD);
      }else{
        view.setTextColor(mContext.getColor(R.color.lighter_gray));
        view.setTypeface(null, Typeface.NORMAL);
      }

      // special check for if date is 1, so we can draw a line
      if (dateNumber == 1) {
        ImageView divider = createVerticalDividerHelper();
        parent.addView(divider);

        Guideline guideline = holder.getGuideline(i);

        constraintSet.clone(parent);

        constraintSet.connect(divider.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        constraintSet.connect(divider.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        constraintSet.connect(divider.getId(), ConstraintSet.START, guideline.getId(), ConstraintSet.END, 0);
        constraintSet.connect(divider.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.START, 0);

        constraintSet.applyTo(parent);
      }

    }

    long weekStartMillis = javaCal.getTimeInMillis();
    EventCollisionChecker oECC = new EventCollisionChecker();
    // bind the events
    for (Event event : mCalendar.events){
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
        oECC.findAvailableSlotAndInsert(eventInfo);

        int offsetTop = (EVENT_BAR_HEIGHT + EVENT_BAR_MARGIN) * eventInfo.layer;

        ConstraintLayout eventBlock = createEventBlockHelper(holder, event);
        eventBlock.setBackgroundColor(mContext.getColor(R.color.primaryColor));

        parent.addView(eventBlock);

        constraintSet.clone(parent);

        constraintSet.connect(eventBlock.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 60 + offsetTop);
        constraintSet.connect(eventBlock.getId(), ConstraintSet.START, holder.getHighResGuideline(beginPosition, mEventGranularityFactor).getId(), ConstraintSet.START, 2);
        constraintSet.connect(eventBlock.getId(), ConstraintSet.END, holder.getHighResGuideline(endPosition, mEventGranularityFactor).getId(), ConstraintSet.START, 3);

        constraintSet.applyTo(parent);

      }
    }

    holder.resynchronizeHighlightMonth(mHighlightMonth);
  }

  private ConstraintLayout createEventBlockHelper(@NonNull CalendarWeekViewHolder holder, Event event) {

    ConstraintLayout rootBlock = (ConstraintLayout) mLayoutInflater.inflate(R.layout.calendar_event_item, holder.getParentLayout(),false );
    ImageView eventBlock = rootBlock.findViewById(R.id.iv_calendar_event_block);
    eventBlock.setColorFilter(mContext.getColor(R.color.primaryLightColor), PorterDuff.Mode.MULTIPLY);
    TextView eventName = rootBlock.findViewById(R.id.tv_calendar_event_name);
    eventName.setText(event.name);
    rootBlock.setTag(MANAGED_VIEW_TAG);
    ViewGroup.LayoutParams params = rootBlock.getLayoutParams();
    params.height = EVENT_BAR_HEIGHT;
    rootBlock.setLayoutParams(params);
    rootBlock.setId(View.generateViewId());
    return rootBlock;
  }

  private ImageView createVerticalDividerHelper() {
    ImageView view = new ImageView(mContext);
    int lineThickness = (int) (mContext.getResources().getDimension(R.dimen.divider_line_thickness));
    view.setLayoutParams(new ViewGroup.LayoutParams(lineThickness, 0));
    view.setId(View.generateViewId());
    view.setBackgroundColor(mContext.getColor(R.color.gray_75_a_50));
    view.setTag(MANAGED_VIEW_TAG);
    return view;
  }

  @Override
  public void onViewRecycled(@NonNull CalendarWeekViewHolder holder) {
    super.onViewRecycled(holder);
    ConstraintLayout parent = holder.getParentLayout();
    for (int i = 0; i < parent.getChildCount(); i++) {
      View view = parent.getChildAt(i);
      if (view.getTag() == MANAGED_VIEW_TAG) {
        parent.removeView(view);
        i--;
      }
    }
  }

  public void setData(Calendar calendar){
    mCalendar = calendar;
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
  }
}
