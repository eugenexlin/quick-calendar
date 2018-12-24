package com.djdenpa.quickcalendar.utils;

import java.util.HashSet;
import java.util.LinkedList;

public class EventCollisionChecker {
  LinkedList<EventCollisionInfo> events = new LinkedList<>();
  public int currentMax;

  public boolean findAvailableSlotAndInsert(EventCollisionInfo newEvent){
    if (events.contains(newEvent)) {
      return false;
    }
    HashSet<Integer> takenSlots = new HashSet<>();
    for (EventCollisionInfo event: events) {
      if (event.beginPosition < newEvent.endPosition && event.endPosition > newEvent.beginPosition) {
        takenSlots.add(event.layer);
      }
    }
    for(int i = 0; i < 100; i++){
      if (!takenSlots.contains(i)){
        newEvent.layer = i;
        events.add(newEvent);

        if (currentMax < i) {
          currentMax = i;
        }
        return true;
      }
    }
    return false;
  }
}

