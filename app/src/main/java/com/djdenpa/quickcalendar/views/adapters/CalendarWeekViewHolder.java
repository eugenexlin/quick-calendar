package com.djdenpa.quickcalendar.views.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarWeekViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.cl_calendar_week)
  ConstraintLayout clCalendarWeeks;

  public CalendarWeekViewHolder(View itemView) {
    super(itemView);

    ButterKnife.bind(this, itemView);
  }

  public ConstraintLayout getParentLayout(){
    return clCalendarWeeks;
  }

}
