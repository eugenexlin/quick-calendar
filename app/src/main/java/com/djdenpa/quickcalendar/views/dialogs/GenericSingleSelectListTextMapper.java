package com.djdenpa.quickcalendar.views.dialogs;

import java.util.LinkedList;

public class GenericSingleSelectListTextMapper {

  public LinkedList<MapperItem> mMapperItems = new LinkedList<>();

  public String mapValueToText(String value){
    for (MapperItem item : mMapperItems) {
      if (item.value.equals(value)) {
        return item.displayText;
      }
    }
    if (mMapperItems.size() > 0) {
      return mMapperItems.get(0).displayText;
    }
    return "null";
  }
//  public String mapTextToValue(String displayText){
//    for (MapperItem item : mMapperItems) {
//      if (item.displayText == displayText) {
//        return item.value;
//      }
//    }
//    if (mMapperItems.size() > 0) {
//      return mMapperItems.get(0).value;
//    }
//    return "null";
//  }

  public static class MapperItem {
    public String value;
    public String displayText;
    public MapperItem(String v, String t) {
      value = v;
      displayText = t;
    }
  }
}
