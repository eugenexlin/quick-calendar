package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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

  public ConstraintLayout getParentLayout(){
    return clCalendarWeeks;
  }

}
