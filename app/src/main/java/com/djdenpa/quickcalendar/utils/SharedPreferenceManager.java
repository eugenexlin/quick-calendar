package com.djdenpa.quickcalendar.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.DisplayMode;

public class SharedPreferenceManager {

  private Context mContext;
  private final String PREF_FILE_STRING;


  private static final String USER_EMAIL_KEY = "USER_EMAIL_KEY";
  private static final String USER_NAME_KEY = "USER_NAME_KEY";
  private static final String USER_ID_TOKEN_KEY = "USER_ID_TOKEN_KEY";
  private static final String USER_ID_KEY = "USER_ID_KEY";

  public SharedPreferenceManager(Context context) {
    mContext = context;
    PREF_FILE_STRING = mContext.getString(R.string.preference_file_key);
  }

  /* TODO this class could be the central repository for all the shared preferences
      Will refactor existing shared preference stuff later.
    */

  public String getUserEmail() {
    return getStringValueByKey(USER_EMAIL_KEY, "");
  }
  public void setUserEmail(String email) {
    setStringValueByKey(USER_EMAIL_KEY, email);
  }

  public String getUserIdToken() {
    return getStringValueByKey(USER_ID_TOKEN_KEY, "");
  }
  public void setUserIdToken(String token) {
    setStringValueByKey(USER_ID_TOKEN_KEY, token);
  }

  public String getUserId() {
    return getStringValueByKey(USER_ID_KEY, "");
  }
  public void setUserId(String uid) {
    setStringValueByKey(USER_ID_KEY, uid);
  }

  public String getUserName() {
    return getStringValueByKey(USER_NAME_KEY, "");
  }
  public void setUserName(String name) {
    setStringValueByKey(USER_NAME_KEY, name);
  }

  private String getStringValueByKey(String key, String defaultValue) {
    SharedPreferences sharedPref =
            mContext.getSharedPreferences(PREF_FILE_STRING, Context.MODE_PRIVATE);
    return sharedPref.getString(key, defaultValue);
  }
  private void setStringValueByKey(String key, String value) {
    SharedPreferences sharedPref =
            mContext.getSharedPreferences(PREF_FILE_STRING, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(key, value);
    editor.apply();
    // not using apply, since not critical
  }
}
