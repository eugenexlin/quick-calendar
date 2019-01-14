package com.djdenpa.quickcalendar.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;

public class EditCalendarViewModel extends ViewModel {
  public MutableLiveData<Calendar> activeCalendar = new MutableLiveData<>();
  public int activeEventSetLocalId;
  public int previousActiveEventSetLocalId = -1;
  public MutableLiveData<EventSet> activeEventSet = new MutableLiveData<>();
  public String identity = "";
  public String uid = "";
  public String idToken = "";
  public String userName;

  private boolean isFirebaseShareOn = false;
  private boolean hasImportedShare = false;

  public boolean getIsFirebaseShareOn(){
    return isFirebaseShareOn;
  }
  public void setIsFirebaseShareOn(boolean isOn){
    isFirebaseShareOn = (isOn);
  }

  public void init() {
    Calendar calendar = new Calendar();
    activeCalendar.setValue(calendar);
  }

  public void setEntireCalendar(Calendar calendar){
    activeCalendar.setValue(calendar);
    // re fetch the active event set
    previousActiveEventSetLocalId = -1;
    setActiveEventSetLocalId(1);
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
      EventSet eventSet = activeCalendar.getValue().getEventSetByLocalId(activeEventSetLocalId);
      activeEventSetLocalId = eventSet.localId;
      previousActiveEventSetLocalId = activeEventSetLocalId;
      activeEventSet.setValue(eventSet);
    }
    return activeEventSet;
  }

  public void setActiveEventSetLocalId(int id) {
    activeEventSetLocalId = id;
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

  public void setCalendarShareData(String data){
    Calendar calendar = getActiveCalendar().getValue();
    calendar.importFirebaseSerialization(data);
    activeCalendar.setValue(calendar);
    // trigger data rebind also..
    if (!hasImportedShare){
      hasImportedShare = true;
      previousActiveEventSetLocalId = -1;
    }
    EventSet eventset = getActiveEventSet().getValue();
    activeEventSet.setValue(eventset);
  }

  public void saveEventToActiveSet(Event event) {
    EventSet eventset = getActiveEventSet().getValue();
    eventset.saveEvent(event);
    activeEventSet.setValue(eventset);
  }
  public boolean deleteEventFromActiveSet(int localId) {
    EventSet eventset = getActiveEventSet().getValue();
    boolean result = eventset.deleteEvent(localId);
    activeEventSet.setValue(eventset);
    return result;
  }
}
