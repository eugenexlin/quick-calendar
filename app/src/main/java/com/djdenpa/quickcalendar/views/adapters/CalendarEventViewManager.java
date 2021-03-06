package com.djdenpa.quickcalendar.views.adapters;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.EventDragShadow;
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

  public int mEventLocalId;
  public int marginTop;
  public Guideline guidelineStart;
  public Guideline guidelineEnd;

  public CalendarEventViewManager(Context context, @NonNull CalendarWeekViewHolder holder){
    mContext = context;
    LayoutInflater inflater = LayoutInflater.from(mContext);
    clRoot = (ConstraintLayout) inflater.inflate(R.layout.calendar_event_item, holder.getParentLayout(),false );
    ivEventBlock = clRoot.findViewById(R.id.iv_calendar_event_block);
    tvEventName = clRoot.findViewById(R.id.tv_calendar_event_name);
    clRoot.setTag(R.id.tag_gv_view_manager, this);
    clRoot.setId(View.generateViewId());
    holder.clCalendarWeeks.addView(clRoot);
  }

  public void recycleAndHide() {
    clRoot.setVisibility(View.GONE);
    mEventLocalId = -1;
    marginTop = 0;
    guidelineStart = null;
    guidelineEnd = null;
  }

  public void setEventData(Event event, EditCalendarFragment fragment) {
    mEventLocalId = event.localId;
    clRoot.setVisibility(View.VISIBLE);
//    ivEventBlock.setColorFilter(mContext.getColor(R.color.primaryLightColor), PorterDuff.Mode.MULTIPLY);
    int color;
    try {
      color = Color.parseColor(event.color);
    } catch (Exception ex) {
      // invalid color...
      color = mContext.getColor(R.color.primaryLightColor);
    }
    int colorSum = getColorSum(color);
    if (colorSum < 100) {
      tvEventName.setTextColor(mContext.getColor(R.color.light_gray));
    } else if (colorSum < 200) {
      tvEventName.setTextColor(mContext.getColor(R.color.lighter_gray));
    } else if (colorSum < 300) {
      tvEventName.setTextColor(mContext.getColor(R.color.lightest_gray));
    } else if (colorSum < 400) {
      tvEventName.setTextColor(mContext.getColor(R.color.white));
    } else {
      tvEventName.setTextColor(mContext.getColor(R.color.black));
    }
    ivEventBlock.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    tvEventName.setText(event.name);

    clRoot.setOnClickListener(v -> fragment.handleClickEvent(event));
    clRoot.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        EventDragShadow ds = new EventDragShadow(v);
        ClipData data = ClipData.newPlainText("","");
        v.startDrag(data,ds,v,0);
//        v.setVisibility(View.INVISIBLE);
        return false;
      }
    });

  }

  public int getColorSum(int color) {
    int sum = 0;
    sum += Color.red(color);
    sum += Color.green(color);
    sum += Color.blue(color);
    return sum;
  }


  public void setAsPlaceholder() {
    clRoot.setVisibility(View.VISIBLE);
    tvEventName.setText("");
    clRoot.setOnClickListener(null);
  }

}
