package com.djdenpa.quickcalendar.views.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CalendarTileListItemViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.tv_calendar_tile_name)
  TextView tvName;
  @BindView(R.id.iv_calendar_tile_thumbnail)
  ImageView ivThumbnail;
  @BindView(R.id.cl_tile_item)
  ConstraintLayout clTileItem;



  public CalendarTileListItemViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }


}
