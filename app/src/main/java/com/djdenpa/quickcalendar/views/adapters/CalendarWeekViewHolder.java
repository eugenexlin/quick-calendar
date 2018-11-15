package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;

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

  Context mContext;


  public CalendarWeekViewHolder(View itemView, Context context) {
    super(itemView);

    ButterKnife.bind(this, itemView);

    mContext = context;

    //here create a horizontal divider that will be permanent for the view holder
    ImageView divider = new ImageView(mContext);
    int lineThickness = (int) (mContext.getResources().getDimension(R.dimen.divider_line_thickness));
    divider.setLayoutParams(new ViewGroup.LayoutParams(0, lineThickness));
    divider.setId(View.generateViewId());
    divider.setBackgroundColor(mContext.getColor(R.color.gray_75_a_50));
    clCalendarWeeks.addView(divider);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);

    constraintSet.connect(divider.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);

    constraintSet.connect(divider.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
    constraintSet.connect(divider.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);

    constraintSet.applyTo(clCalendarWeeks);
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

  public void setDayTextField(int position, String date){
    switch(position) {
      case 0:
        tvDay0.setText(date);
        return;
      case 1:
        tvDay1.setText(date);
        return;
      case 2:
        tvDay2.setText(date);
        return;
      case 3:
        tvDay3.setText(date);
        return;
      case 4:
        tvDay4.setText(date);
        return;
      case 5:
        tvDay5.setText(date);
        return;
      case 6:
        tvDay6.setText(date);
        return;
      default:
        return;
    }
  }

  public ConstraintLayout getParentLayout(){
    return clCalendarWeeks;
  }

}
