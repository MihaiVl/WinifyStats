package com.android.winifystats.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.android.winifystats.R;
import com.android.winifystats.model.EmployeeCredentials;

/**
 * Created by izaya_orihara on 7/10/17.
 */

public class Cache {
    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;
    private Context context;


    public Cache(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("CACHE",Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }
    public void saveUsername(String mDefaultUsername){
        editor.putString("USERNAME",mDefaultUsername).apply();
    }

    public void savePassword(String mDefaultPassword){
        editor.putString("PASSWORD", mDefaultPassword).apply();
    }
    public String getUsername(){
       return sharedPref.getString("USERNAME",null);
    }
    public String getPassword(){
        return sharedPref.getString("PASSWORD", null);
    }
    public void saveCredentials(boolean value){
        editor.putBoolean("CHECKBOXSTATE",value).apply();
    }

    public boolean isPasswordCheckBoxSaved(){
        return sharedPref.getBoolean("CHECKBOXSTATE", false);
    }
    public void saveToken(String token){
        editor.putString("TOKEN", token).apply();
    }
    public String getToken(){
       return sharedPref.getString("TOKEN", null);
    }

    public boolean isLogged(){
        return getToken()!= null;
    }

    public void logout(){
        saveToken(null);
    }

    public void setTimerStarted(boolean value){
        editor.putBoolean("TIMER_STARTED", value).apply();

    }

    public boolean isTimerStarted(){
        return sharedPref.getBoolean("TIMER_STARTED", false);
    }
}
