package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.comparer.CalendarComparator;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.CalendarInfo;
import com.djdenpa.quickcalendar.views.activities.EditCalendarActivity;
import com.djdenpa.quickcalendar.views.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.djdenpa.quickcalendar.views.activities.EditCalendarActivity.EXTRA_CALENDAR_ID;

public class CalendarTileListItemAdapter extends RecyclerView.Adapter<CalendarTileListItemViewHolder> {

  Context mContext;

  public final LinkedList<Calendar> mCalendarData = new LinkedList<>();

  @NonNull
  @Override
  public CalendarTileListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    mContext = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(mContext);
    View view = inflater.inflate(R.layout.calendar_tile_item, parent, false);

    return new CalendarTileListItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarTileListItemViewHolder holder, int position) {
    Calendar calendar = getItem(position);
    holder.tvName.setText(calendar.name);
    Picasso.get().load(R.drawable.ic_calendar).into(holder.ivThumbnail);
    holder.clTileItem.setOnClickListener(v -> {
      Intent intent = new Intent(mContext,  EditCalendarActivity.class);
      intent.putExtra(EXTRA_CALENDAR_ID, calendar.id);
      mContext.startActivity(intent);
    });
  }

  @Override
  public int getItemCount() {
    return mCalendarData.size();
  }

  private Calendar getItem(int position) {
    return mCalendarData.get(position);
  }

  private CalendarComparator calendarComparator = new CalendarComparator();

  public void setData(List<Calendar> data){
    mCalendarData.clear();

    // MEMO TO ME, Collections.copy does not auto resize collection,
    // requires destination to at least be as long...
    // not very friendly.
    // Collections.copy(mCalendarData, data);

    for (Calendar c : data){
      mCalendarData.add(c);
    }
    Collections.sort(mCalendarData, calendarComparator);
    Collections.reverse(mCalendarData);
  }
}
