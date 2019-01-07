package com.djdenpa.quickcalendar.utils;

import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.models.EventSet;

import java.util.LinkedList;
import java.util.Random;

public class MockCalendarDataGenerator {
  enum DistributionFormula {
    LINEAR,
    SIN,
    GAUSSIAN_3SD,
    GAUSSIAN_5SD
  }

  // when rolling event length, it first rolls 1/2 chance into left or right
  // and then it uses distribution formula for that time range.
  // that way if we want a rare long event, it wont skew our distribution.
  public long minEventMs = 1000*60*60;
  public long medianEventMs = 1000*60*60*12;
  public long maxEventMs = 1000*60*60*24*7;
  public DistributionFormula eventMsDistribution = DistributionFormula.GAUSSIAN_3SD;

  public long minEventStartUTC = System.currentTimeMillis();
  public long maxEventStartUTC = System.currentTimeMillis() + maxEventMs*30;

  public int minEventCount = 10;
  public int maxEventCount = 250;

  public int minEventSetCount = 1;
  public int maxEventSetCount = 5;

  public long roundToMs = 1000*60*30;

  public Random rand = new Random();

  public LinkedList<Calendar> getMockCalendars(){
    LinkedList<Calendar> result = new LinkedList<>();
    for (int i = 0; i < 1 + rand.nextInt(20); i++){
      Calendar calendar = new Calendar();
      calendar.name = "Test Calendar " + i;
      calendar.id = i;
      result.add(calendar);
    }
    return result;
  }

  public Calendar getMockCalendar(){
    Calendar result = new Calendar();

    result.name = "Test Calendar " + rand.nextInt(99999);
    result.creatorIdentity = "Test Mom";

    int eventCount = rand.nextInt(maxEventSetCount - minEventSetCount) + minEventSetCount;

    for (int i = 0; i < eventCount; i++){
      result.saveEventSet(getMockEventSet());
    }
    return result;
  }
  public EventSet getMockEventSet(){
    EventSet result = new EventSet();

    maxEventStartUTC = System.currentTimeMillis() + maxEventMs*rand.nextInt(50);

    result.name = "Test Set + " + rand.nextInt(99999);
    int eventCount = rand.nextInt(maxEventCount - minEventCount) + minEventCount;

    for (int i = 0; i < eventCount; i++){
      result.saveEvent(getMockEvent());
    }
    return result;
  }

  public String getRandomColor(){
    String red = String.format("%02x", 0x66 + rand.nextInt(0x88 + 1));
    String green = String.format("%02x", 0x66 + rand.nextInt(0x88 + 1));
    String blue = String.format("%02x", 0x66 + rand.nextInt(0x88 + 1));
    return "#"+red+green+blue;
  }


  public Event getMockEvent(){
    Event result = new Event();
    result.name = "event" + rand.nextInt(99999);;
    result.eventStartUTC = (long) (rand.nextDouble()*(maxEventStartUTC - minEventStartUTC) + minEventStartUTC);
    result.eventDurationMs = getMockDuration();
    result.color = getRandomColor();
    return result;
  }

  public long getMockDuration(){
    long value;
    if (rand.nextDouble() < 0.5) {
      value = generateRandomFromDistribution(medianEventMs, minEventMs-medianEventMs);
    }else{
      value = generateRandomFromDistribution(medianEventMs, maxEventMs-medianEventMs);
    }
    value = (value / roundToMs) * roundToMs;
    return value;
  }

  // put in negative distance for reverse side
  public long generateRandomFromDistribution(long start, long distance){

    double multiplier = 0.0;
    switch (eventMsDistribution) {
      case LINEAR:
        return (long) (rand.nextDouble() * (distance) + start);
      case SIN:
        return (long) (Math.sin(rand.nextDouble()*Math.PI)* (distance) + start);

      // both gaussian cases are sharing the same body
      case GAUSSIAN_3SD:
        multiplier = 1.0 / 3.0;
        break;
      case GAUSSIAN_5SD:
        multiplier = 1.0 / 5.0;
        break;
    }
    double halfGaussian = Math.abs(rand.nextGaussian()) * multiplier;
    return (long) (halfGaussian * distance + start);
  }
}
