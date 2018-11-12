package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
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
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.CalendarDateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

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

  private Date mMidpointDate;
  private Calendar mCalendar;

  private Context mContext;

  @BindView(R.id.g_day_0)
  Guideline gDay0;
  @BindView(R.id.g_day_1)
  Guideline gDay1;
  @BindView(R.id.g_day_2)
  Guideline gDay2;
  @BindView(R.id.g_day_3)
  Guideline gDay3;
  @BindView(R.id.g_day_4)
  Guideline gDay4;
  @BindView(R.id.g_day_5)
  Guideline gDay5;
  @BindView(R.id.g_day_6)
  Guideline gDay6;
  @BindView(R.id.g_day_7)
  Guideline gDay7;

  // private HashSet<View> managedViews = new HashSet<>();
  private static final String MANAGED_VIEW_TAG = "MANAGED_VIEW_TAG";

  public CalendarWeekAdapter(Context context){
    mContext = context;
  }

  @NonNull
  @Override
  public CalendarWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.calendar_edit_week_item, parent, false);

    ButterKnife.bind(this, view);

    return new CalendarWeekViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarWeekViewHolder holder, int position) {

    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate);
    javaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);

    ConstraintLayout parent = holder.getParentLayout();
    ConstraintSet constraintSet = new ConstraintSet();
    // first bind the days of the week
    for (int i = 0; i < 7; i++) {
      java.util.Calendar dayOfWeek = (java.util.Calendar) javaCal.clone();
      dayOfWeek.add(java.util.Calendar.DAY_OF_YEAR, i);
      int dateNumber = dayOfWeek.get(java.util.Calendar.DATE);
      Guideline guideline = getGuideline(i);

      TextView view = createTextViewHelper();
      view.setText(String.valueOf(dateNumber));
      parent.addView(view);

      constraintSet.clone(parent);

      constraintSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
      constraintSet.connect(view.getId(), ConstraintSet.START, guideline.getId(), ConstraintSet.END, 8);

      constraintSet.applyTo(parent);
    }

    // bind the events
    for (Event event : mCalendar.events){
      // does event fall on this week
      java.util.Calendar eventJavaCal = CalendarDateUtils.getCalendarFromUTCMillis(event.eventStartUTC);
      eventJavaCal.setTime(new Date(System.currentTimeMillis()));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      eventJavaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);


    }

//
//    TextView view = createTextViewHelper();
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//    view.setText(sdf.format(javaCal.getTime()));
//    parent.addView(view);
//
//    constraintSet.clone(parent);
//
//    constraintSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
//    constraintSet.connect(view.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 32);
//
//    constraintSet.applyTo(parent);

  }



  private TextView createTextViewHelper() {
    TextView view = new TextView(mContext);
    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    view.setId(View.generateViewId());
    view.setTag(MANAGED_VIEW_TAG);
    return view;
  }

  private Guideline getGuideline(int position){
    switch(position) {
      case 0:
        return gDay0;
      case 1:
        return gDay1;
      case 2:
        return gDay2;
      case 3:
        return gDay3;
      case 4:
        return gDay4;
      case 5:
        return gDay5;
      case 6:
        return gDay6;
      case 7:
        return gDay7;
      default:
        return gDay0;
    }
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

  public void setMidpointDate(Date date){
    mMidpointDate = date;
  }

  @Override
  public int getItemCount() {
    return ITEM_COUNT;
  }
}
