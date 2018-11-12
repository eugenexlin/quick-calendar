package com.djdenpa.quickcalendar.utils;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CalendarDateUtils {
  public static java.util.Calendar getCalendarFromUTCMillis (long millis) {
    java.util.Calendar jCal = java.util.Calendar.getInstance();
    jCal.setTimeInMillis(millis);
    jCal.setTimeZone(TimeZone.getTimeZone("UTC"));
    return jCal;
  }

  public static long getSystemCurrentUTCMillis(){
    java.util.Calendar jCal = new GregorianCalendar(TimeZone.getDefault());
    jCal.setTimeInMillis(System.currentTimeMillis());

    java.util.Calendar utcJCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    utcJCal.setTimeInMillis(jCal.getTimeInMillis());
    return utcJCal.getTimeInMillis();
  }
}
