package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.comparer.CalendarComparator;
import com.djdenpa.quickcalendar.comparer.CalendarTileComparator;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.CalendarThumbnail;
import com.djdenpa.quickcalendar.models.CalendarTile;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;
import com.djdenpa.quickcalendar.views.activities.EditCalendarActivity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.djdenpa.quickcalendar.views.activities.EditCalendarActivity.EXTRA_CALENDAR_ID;
import static com.djdenpa.quickcalendar.views.activities.EditCalendarActivity.EXTRA_SHARE_HASH;

public class CalendarTileListItemAdapter extends RecyclerView.Adapter<CalendarTileListItemViewHolder> {

  QuickCalendarDatabase mDB;

  Context mContext;

  public final LinkedList<CalendarTile> mCalendarData = new LinkedList<>();

  @NonNull
  @Override
  public CalendarTileListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    mContext = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(mContext);
    View view = inflater.inflate(R.layout.calendar_tile_item, parent, false);

    mDB = QuickCalendarDatabase.getInstance(mContext.getApplicationContext());

    return new CalendarTileListItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarTileListItemViewHolder holder, int position) {
    CalendarTile calendarTile = getItem(position);
    holder.tvName.setText(calendarTile.name);

    QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
      CalendarThumbnail thumb = mDB.calendarThumbnailDao().loadCalendarThumbnails(calendarTile.thumbnailId);

      if (thumb != null) {
        Drawable drawable;
        Bitmap bitmap = thumb.getBitmap();
        drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
          holder.ivThumbnail.setImageDrawable(drawable);
        });
      }

    });

    if (calendarTile.isShare) {
      holder.clTileItem.setOnClickListener(v -> {
        Intent intent = new Intent(mContext,  EditCalendarActivity.class);
        intent.putExtra(EXTRA_SHARE_HASH, calendarTile.shareHash);
        mContext.startActivity(intent);
      });
    } else {
      // standard calendar load.
      holder.clTileItem.setOnClickListener(v -> {
        Intent intent = new Intent(mContext,  EditCalendarActivity.class);
        intent.putExtra(EXTRA_CALENDAR_ID, calendarTile.calendarId);
        mContext.startActivity(intent);
      });
    }
  }

  @Override
  public int getItemCount() {
    return mCalendarData.size();
  }

  private CalendarTile getItem(int position) {
    return mCalendarData.get(position);
  }

  private CalendarTileComparator calendarComparator = new CalendarTileComparator();

  public void setData(List<CalendarTile> data){
    mCalendarData.clear();

    // MEMO TO ME, Collections.copy does not auto resize collection,
    // requires destination to at least be as long...
    // not very friendly.
    // Collections.copy(mCalendarData, data);

    for (CalendarTile c : data){
      mCalendarData.add(c);
    }
    Collections.sort(mCalendarData, calendarComparator);
    Collections.reverse(mCalendarData);
  }
}
