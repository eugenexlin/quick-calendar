package com.djdenpa.quickcalendar.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.djdenpa.quickcalendar.comparer.EventDurationComparator;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Calendar.class,
        parentColumns = "id",
        childColumns = "calendarId",
        onDelete = CASCADE),
        indices = {@Index("calendarId")}
)
public class CalendarThumbnail {

  @PrimaryKey(autoGenerate = true)
  public int id;

  public int calendarId;
  public int width;
  public int height;
  public byte[] data;

  public CalendarThumbnail() {
    width = 16;
    height = 16;
  }

  // generates a thumbnail without caring about event overlap..
  // last come wins the pixel.
  public void generateThumbnail(Calendar calendar) {
    int totalLength = width * height; // one byte per rgb
    byte[] result = new byte[totalLength * 4];
    EventSet eventSet = calendar.getFirstEventSet();

    List<Event> sortedEvents = new LinkedList<>();
    for (Event event : eventSet.getAllEvents()) {
      sortedEvents.add(event);
    }
    Collections.sort(sortedEvents, new EventDurationComparator());
    Collections.reverse(sortedEvents);

    // first get the range of the data set
    long minEventUTC = Long.MAX_VALUE;
    long maxEventUTC = Long.MIN_VALUE;
    for (Event event : sortedEvents) {
      if (event.eventStartUTC < minEventUTC) {
        minEventUTC = event.eventStartUTC;
      }
      if (event.eventStartUTC + event.eventDurationMs > maxEventUTC) {
        maxEventUTC = event.eventStartUTC + event.eventDurationMs;
      }
    }

    // calc the important numbers
    long range = maxEventUTC - minEventUTC;
    // add 1 to avoid array overflow from rounding.
    long unitLength = range / totalLength;


    // now with the min and max, fill in the relative position in the array
    for (Event event : sortedEvents) {
      int color = Color.parseColor(event.color);
      byte a = (byte) Color.alpha((color));
      byte r = (byte) Color.red((color));
      byte g = (byte) Color.green((color));
      byte b = (byte) Color.blue((color));
      int startPos = (int) ((event.eventStartUTC - minEventUTC) / unitLength);
      int endPos = (int) ((event.eventStartUTC + event.eventDurationMs - minEventUTC) / unitLength);
      if ( endPos > result.length/4) {
        endPos = result.length/4;
      }
      for (int i = startPos; i < endPos; i++) {
        result[i * 4] = r;
        result[i * 4 + 1] = g;
        result[i * 4 + 2] = b;
        result[i * 4 + 3] = a;
      }
    }

    data = result;
    calendarId = calendar.id;
  }

  public Bitmap getBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    ByteBuffer buffer = ByteBuffer.wrap(data);
    bitmap.copyPixelsFromBuffer(buffer);
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width*16, height*16, false);
    return scaledBitmap;
  }
}
