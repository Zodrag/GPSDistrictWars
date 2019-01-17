package com.example.home.gpsdistrictwars;

import android.content.Context;
import android.content.SharedPreferences;

class UserLocalStore {

    private static final String SP_NAME = "userDetails";
    private SharedPreferences userLocalDataBase;

    UserLocalStore(Context context){
        userLocalDataBase = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    String getUserDistrict() {
        return userLocalDataBase.getString("district", "DistrictName");
    }
}
