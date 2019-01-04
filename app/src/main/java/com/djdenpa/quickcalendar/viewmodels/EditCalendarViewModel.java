package com.djdenpa.quickcalendar.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.EventSet;

public class EditCalendarViewModel extends ViewModel {
  public MutableLiveData<Calendar> activeCalendar = new MutableLiveData<>();
  public int activeEventSetId;
  public String identity = "";
  public String uid = "";
  public String idToken = "";

  private Application mApplication;

  public void init() {
    Calendar calendar = new Calendar();
    activeCalendar.setValue(calendar);
  }
  // todo init with id

  public void setEntireCalendar(Calendar calendar){
    activeCalendar.setValue(calendar);
  }

  public LiveData<Calendar> getActiveCalendar(){
    if(activeCalendar == null){
      init();
    }
    return activeCalendar;
  }

  public EventSet getActiveEventSet(){
    Calendar calendar = activeCalendar.getValue();
    EventSet eventSet = calendar.getEventSetById(activeEventSetId);
    activeEventSetId = eventSet.id;
    return eventSet;
  }

  public void setCalendarName(String name){
    Calendar calendar = getActiveCalendar().getValue();
    String cleanedName = name.trim();
    if (cleanedName == null || cleanedName == "") {
      cleanedName = "";
    }
    calendar.name = cleanedName;
    activeCalendar.setValue(calendar);
  }
}
