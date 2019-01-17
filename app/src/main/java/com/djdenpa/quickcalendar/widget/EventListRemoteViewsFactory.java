package com.djdenpa.quickcalendar.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.CoreDataLayer;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class EventListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

  private Context mContext;
  private QuickCalendarDatabase mDB;
  private List<Event> mData = new LinkedList<>();
  private SimpleDateFormat mDateFormat;
  private SimpleDateFormat mTimeFormat;

  public EventListRemoteViewsFactory(Context applicationContext) {
    mContext = applicationContext;
    mDB = QuickCalendarDatabase.getInstance(mContext);

    mDateFormat = new SimpleDateFormat(mContext.getString(R.string.widget_date_format), Locale.getDefault());
    mTimeFormat = new SimpleDateFormat(mContext.getString(R.string.widget_time_format), Locale.getDefault());
  }

  @Override
  public void onCreate() {

  }

  @Override
  public void onDataSetChanged() {
    mData = mDB.eventDao().loadEventsAfter(System.currentTimeMillis());
  }

  @Override
  public void onDestroy() {

  }

  @Override
  public int getCount() {
    return mData.size();
  }

  @Override
  public RemoteViews getViewAt(int position) {
    RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.event_list_item_widget);

    if (mData.size() <= position) {
      return rv;
    }

    Event event = mData.get(position);

    Calendar javaCal = Calendar.getInstance();
    javaCal.setTimeInMillis(event.eventStartUTC);

    String name = event.name;
    if (name == null || event.name.length() <= 0) {
      name = mContext.getString(R.string.unnamed_event);
    }
    rv.setTextViewText(R.id.tv_calendar_event_name, name);
    int color;
    try{
      color = Color.parseColor(event.color);

    } catch (Exception ex) {
      color = mContext.getColor(R.color.primaryColor);
    }
    rv.setInt(
            R.id.iv_calendar_event_block,
            "setBackgroundColor",
            color );

    rv.setTextViewText(R.id.tv_calendar_event_time, mTimeFormat.format(javaCal.getTime()));

    boolean needsSeparator = false;
    if (position > 0) {
      Event otherEvent = mData.get(position-1);
      Calendar otherCal = Calendar.getInstance();
      otherCal.setTimeInMillis(otherEvent.eventStartUTC);

      if (javaCal.get(Calendar.DATE) != otherCal.get(Calendar.DATE)) {
        needsSeparator = true;
      } else if (javaCal.get(Calendar.MONTH) != otherCal.get(Calendar.MONTH)) {
        needsSeparator = true;
      }else if (javaCal.get(Calendar.YEAR) != otherCal.get(Calendar.YEAR)) {
        needsSeparator = true;
      }
    } else{
      needsSeparator = true;
    }

    if (!needsSeparator) {
      rv.setViewVisibility(R.id.tv_date_separator, View.GONE);
    }else{
      rv.setViewVisibility(R.id.tv_date_separator, View.VISIBLE);
      rv.setTextViewText(R.id.tv_date_separator, mDateFormat.format(javaCal.getTime()));
    }


    return rv;
  }

  @Override
  public RemoteViews getLoadingView() {
    return null;
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  public long getItemId(int position) {
    if (mData.size() > position) {
      return mData.get(position).id;
    }
    return 0;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }
}
