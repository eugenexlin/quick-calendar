package com.djdenpa.quickcalendar.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class EventListRemoteViewsService extends RemoteViewsService {
  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new EventListRemoteViewsFactory(this.getApplicationContext());
  }
}
