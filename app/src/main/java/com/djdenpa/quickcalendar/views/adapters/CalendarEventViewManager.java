package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.EventCollisionInfo;
import com.djdenpa.quickcalendar.views.fragments.EditCalendarFragment;

import static com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter.EVENT_BAR_HEIGHT;

// not quite a view holder, but it is to help manage references to views
// and reduce calls to findViewById
// and avoid inflating.
public class CalendarEventViewManager {

  public Context mContext;
  public ConstraintLayout clRoot;
  public ImageView ivEventBlock;
  public TextView tvEventName;

  public CalendarEventViewManager(Context context, @NonNull CalendarWeekViewHolder holder){
    mContext = context;
    LayoutInflater inflater = LayoutInflater.from(mContext);
    clRoot = (ConstraintLayout) inflater.inflate(R.layout.calendar_event_item, holder.getParentLayout(),false );
    ivEventBlock = clRoot.findViewById(R.id.iv_calendar_event_block);
    tvEventName = clRoot.findViewById(R.id.tv_calendar_event_name);
    clRoot.setTag(R.id.tag_gv_view_manager, this);
    ViewGroup.LayoutParams params = clRoot.getLayoutParams();
    params.height = EVENT_BAR_HEIGHT;
    clRoot.setLayoutParams(params);
    clRoot.setId(View.generateViewId());
    holder.clCalendarWeeks.addView(clRoot);
  }

  public void hide() {
    clRoot.setVisibility(View.GONE);
  }

  public void setEventData(Event event, EditCalendarFragment fragment) {
    clRoot.setVisibility(View.VISIBLE);
//    ivEventBlock.setColorFilter(mContext.getColor(R.color.primaryLightColor), PorterDuff.Mode.MULTIPLY);
    int color;
    try {
      color = Color.parseColor(event.color);
    } catch (Exception ex) {
      // invalid color...
      color = mContext.getColor(R.color.primaryLightColor);
    }
    ivEventBlock.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    tvEventName.setText(event.name);

    clRoot.setOnClickListener(v -> fragment.PromptEditEvent(event));

  }

  public void setAsPlaceholder() {
    clRoot.setVisibility(View.VISIBLE);
    tvEventName.setText("");
    clRoot.setOnClickListener(null);
  }

}
