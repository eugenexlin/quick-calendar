package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
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

  private java.util.Calendar mMidpointDate;
  private Calendar mCalendar;

  private int mHighlightMonth;

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

  @BindView(R.id.tv_day_0)
  TextView tvDay0;
  @BindView(R.id.tv_day_1)
  TextView tvDay1;
  @BindView(R.id.tv_day_2)
  TextView tvDay2;
  @BindView(R.id.tv_day_3)
  TextView tvDay3;
  @BindView(R.id.tv_day_4)
  TextView tvDay4;
  @BindView(R.id.tv_day_5)
  TextView tvDay5;
  @BindView(R.id.tv_day_6)
  TextView tvDay6;

  // private HashSet<View> managedViews = new HashSet<>();
  private static final String MANAGED_VIEW_TAG = "MANAGED_VIEW_TAG";

  private class ManagedViewTag {
    public int month;
  }

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

    return new CalendarWeekViewHolder(view, context);
  }

  public java.util.Calendar getMonthYear(int position) {

    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate.getTime());
    javaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);

    return javaCal;
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
      Guideline guideline = getGuideline(i);

      TextView view;


//      view = createTextViewHelper();
//      parent.addView(view);
//      constraintSet.clone(parent);
//      constraintSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
//      constraintSet.connect(view.getId(), ConstraintSet.START, guideline.getId(), ConstraintSet.END, 16);
//      constraintSet.applyTo(parent);


      view = getDayTextField(i);


      view.setText(String.valueOf(dateNumber));

      if (i == 0) {
        view.setText(String.valueOf(position));
      }

      // special check for if date is 1, so we can draw a line
      if (dateNumber == 1) {
        ImageView divider = createVerticalDividerHelper();
        parent.addView(divider);

        constraintSet.clone(parent);

        constraintSet.connect(divider.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        constraintSet.connect(divider.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        constraintSet.connect(divider.getId(), ConstraintSet.START, guideline.getId(), ConstraintSet.END, 0);
        constraintSet.connect(divider.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.START, 0);

        constraintSet.applyTo(parent);
      }

    }

    // bind the events
    for (Event event : mCalendar.events){
      // does event fall on this week
      java.util.Calendar eventJavaCal = CalendarDateUtils.getCalendarFromUTCMillis(event.eventStartUTC);
      eventJavaCal.setTime(new Date(System.currentTimeMillis()));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      eventJavaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);

    }

  }



  private TextView createTextViewHelper() {
    TextView view = new TextView(mContext);
    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    view.setId(View.generateViewId());
    view.setTag(MANAGED_VIEW_TAG);
    return view;
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
  private TextView getDayTextField(int position){
    switch(position) {
      case 0:
        return tvDay0;
      case 1:
        return tvDay1;
      case 2:
        return tvDay2;
      case 3:
        return tvDay3;
      case 4:
        return tvDay4;
      case 5:
        return tvDay5;
      case 6:
        return tvDay6;
      default:
        return tvDay0;
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
