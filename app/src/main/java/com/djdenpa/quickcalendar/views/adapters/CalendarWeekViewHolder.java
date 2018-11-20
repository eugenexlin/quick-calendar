package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;

import java.util.HashMap;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarWeekViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.cl_calendar_week)
  ConstraintLayout clCalendarWeeks;

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

//  @BindView(R.id.iv_week_divider)
//  ImageView ivHorizontalDivider;
  @BindView(R.id.iv_month_divider)
  ImageView ivVerticalDivider;

  Context mContext;

  private HashMap<String, Guideline> mDynamicGuidelines = new HashMap<>();

  // this list shall hold hidden constraint layouts that
  private LinkedList<CalendarEventViewManager> mRecycledEventViews = new LinkedList<>();


  public CalendarWeekViewHolder(View itemView, Context context) {
    super(itemView);

    ButterKnife.bind(this, itemView);

    mContext = context;
  }

  public Guideline getGuideline(int position){
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

  public void setVerticalDividerPosition(int position) {
    Guideline guideline = getGuideline(position);
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);
    constraintSet.connect(ivVerticalDivider.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.START, 0);
    constraintSet.applyTo(clCalendarWeeks);
  }

  public Guideline getHighResGuideline(int scaledPosition, int multiplier) {
    // first check if it divides well
    double simplifiedPosition = (double) scaledPosition / (double) multiplier;
    //close enough to an integer.
    if (Math.abs(simplifiedPosition - Math.floor(simplifiedPosition)) < 0.01) {
      return (getGuideline((int)simplifiedPosition));
    }

    String key = "" + scaledPosition + "_" + multiplier;

    if (!mDynamicGuidelines.containsKey(key)){
      Guideline guideline = new Guideline(mContext);
      guideline.setId(View.generateViewId());
      guideline.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
      guideline.setGuidelinePercent((float)scaledPosition / (float) multiplier / 7.0f);
      clCalendarWeeks.addView(guideline);

      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(clCalendarWeeks);

      constraintSet.create(guideline.getId(), ConstraintSet.VERTICAL_GUIDELINE);

      constraintSet.applyTo(clCalendarWeeks);
      mDynamicGuidelines.put(key, guideline);
    }

    return mDynamicGuidelines.get(key);
  }

  // hide a view's visibility and
  public void RecycleViewToPool(CalendarEventViewManager eventItem) {
    eventItem.hide();
    mRecycledEventViews.push(eventItem);

  }

  public CalendarEventViewManager getViewFromPoolOrCreate(Context context, @NonNull CalendarWeekViewHolder holder) {
    if (mRecycledEventViews.size() > 0) {
      CalendarEventViewManager result = mRecycledEventViews.poll();
      return result;
    }
    return new CalendarEventViewManager(context, holder);
  }


  public TextView getDayTextField(int position){
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

  public ConstraintLayout getParentLayout(){
    return clCalendarWeeks;
  }

  public void resynchronizeHighlightMonth(int mCurrentMonth) {
    for (int i = 0; i <=6; i++) {
      TextView tv = getDayTextField(i);
      if ((int)tv.getTag(R.id.tag_tv_month_key) == mCurrentMonth){
        tv.setTextColor(mContext.getColor(R.color.darker_gray));
        tv.setTypeface(null, Typeface.BOLD);
      }else{
        tv.setTextColor(mContext.getColor(R.color.lighter_gray));
        tv.setTypeface(null, Typeface.NORMAL);
      }
    }
  }
}
