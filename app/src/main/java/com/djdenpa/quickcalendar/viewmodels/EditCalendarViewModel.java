package com.djdenpa.quickcalendar.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.EventSet;

public class EditCalendarViewModel extends ViewModel {
  public MutableLiveData<Calendar> activeCalendar = new MutableLiveData<>();
  public int activeEventSetLocalId;
  public int previousActiveEventSetLocalId = -1;
  public MutableLiveData<EventSet> activeEventSet = new MutableLiveData<>();
  public String identity = "";
  public String uid = "";
  public String idToken = "";

  public MutableLiveData<Boolean> isFirebaseShareOn = new MutableLiveData<>();

  public LiveData<Boolean> getIsFirebaseShareOn(){
    return isFirebaseShareOn;
  }
  public void setIsFirebaseShareOn(boolean isOn){
    isFirebaseShareOn.setValue(isOn);
  }

  public void init() {
    Calendar calendar = new Calendar();
    activeCalendar.setValue(calendar);
  }

  public void setEntireCalendar(Calendar calendar){
    activeCalendar.setValue(calendar);
    // re fetch the active event set
    activeEventSet.setValue(null);
    getActiveEventSet();
  }

  public LiveData<Calendar> getActiveCalendar(){
    if(activeCalendar.getValue() == null){
      init();
    }
    return activeCalendar;
  }

  public LiveData<EventSet> getActiveEventSet(){
    if (activeEventSet.getValue() == null || previousActiveEventSetLocalId != activeEventSetLocalId) {
      EventSet eventSet = activeCalendar.getValue().getEventSetById(activeEventSetLocalId);
      activeEventSetLocalId = eventSet.localId;
      previousActiveEventSetLocalId = activeEventSetLocalId;
      activeEventSet.setValue(eventSet);
    }
    return activeEventSet;
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
