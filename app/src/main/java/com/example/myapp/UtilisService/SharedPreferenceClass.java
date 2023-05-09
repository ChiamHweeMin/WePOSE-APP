package com.example.myapp.UtilisService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {
    //Initialize variable
    private static final String USER_PREF = "user_wepose";
    private SharedPreferences appShared;
    private SharedPreferences.Editor prefsEditor;

    public SharedPreferenceClass(Context context) {
        //Initialize shared preferences
        appShared = context.getSharedPreferences(USER_PREF, Activity.MODE_PRIVATE);
        //Initialize editor
        this.prefsEditor = appShared.edit();
        //Apply editor
        this.prefsEditor.apply();
    }

    //int
    public int getValue_int(String key) {
        return appShared.getInt(key, 0);
    }

    public void setValue_int(String key, int value) {
        prefsEditor.putInt(key, value).commit();
    }

    //string
    public String getValue_string(String key) {
        return appShared.getString(key, "");
    }

    public void setValue_string(String key, String value) {
        prefsEditor.putString(key, value).commit();
    }

    //boolean
    public boolean getValue_boolean(String key) {
        return appShared.getBoolean(key, false);
    }

    public void setValue_boolean(String key, boolean value) {
        prefsEditor.putBoolean(key, value).commit();
    }

    //clear the saved data
    public void clear() {
        prefsEditor.clear().commit();
    }


}
