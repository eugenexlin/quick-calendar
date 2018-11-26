package com.djdenpa.quickcalendar.models;


public enum DisplayMode {
  ROW_PER_WEEK,
  ROW_PER_DAY;

  public static DisplayMode fromInt(int num) {
    switch (num) {
      case 0:
        return ROW_PER_WEEK;
      case 1:
        return ROW_PER_DAY;
      default:
        return ROW_PER_WEEK;
    }
  }

  public static Integer toInt(DisplayMode mode) {
    switch (mode) {
      case ROW_PER_WEEK:
        return 0;
      case ROW_PER_DAY:
        return 1;
      default:
        return 0;
    }
  }
}