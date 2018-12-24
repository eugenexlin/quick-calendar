package com.djdenpa.quickcalendar.utils;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class EventDragShadow extends View.DragShadowBuilder {
  View greyBox;

  public EventDragShadow(View view) {
    super(view);
    greyBox = view;
  }

  @Override
  public void onDrawShadow(Canvas canvas) {
    greyBox.draw(canvas);
  }

  @Override
  public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
    View v = getView();
    int height = v.getHeight();
    int width = v.getWidth();
    shadowSize.set(width, height);
    shadowTouchPoint.set((width / 2), (height / 2));
  }
}
