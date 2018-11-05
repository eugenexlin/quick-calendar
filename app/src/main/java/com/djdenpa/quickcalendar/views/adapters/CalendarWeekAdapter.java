package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 I want something that scrolls forever, but also take advantage of the
 recycling properties of this.
 scrolling will be implemented by using some large number of items,
 and starting at the mid point.
 and some re-syncing of offset method when straying too far from the midpoint
* */
public class CalendarWeekAdapter extends RecyclerView.Adapter<CalendarWeekViewHolder> {

  public static final int ITEM_COUNT = 200;
  public static int START_POSITION = ITEM_COUNT/2;

  private Date mMidpointDate;
  private Calendar mCalendar;

  private Context mContext;

  public CalendarWeekAdapter(Context context){
    mContext = context;
  }

  @NonNull
  @Override
  public CalendarWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.calendar_edit_week_item, parent, false);

    return new CalendarWeekViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarWeekViewHolder holder, int position) {
    java.util.Calendar javaCal = java.util.Calendar.getInstance();
    javaCal.setTime(mMidpointDate);
    javaCal.add(java.util.Calendar.WEEK_OF_YEAR, position - START_POSITION);

    ConstraintLayout parent = holder.getParentLayout();

    TextView view = new TextView(mContext);
    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    view.setId(View.generateViewId());
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    view.setText(sdf.format(javaCal.getTime()));
    parent.addView(view);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(parent);
    constraintSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
    constraintSet.connect(view.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 32);
    constraintSet.applyTo(parent);

  }

  @Override
  public void onViewRecycled(@NonNull CalendarWeekViewHolder holder) {
    super.onViewRecycled(holder);
    ConstraintLayout parent = holder.getParentLayout();
    parent.removeAllViews();
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
