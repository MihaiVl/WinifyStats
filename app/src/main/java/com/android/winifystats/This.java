package com.android.winifystats;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.android.winifystats.cache.Cache;

/**
 * Created by izaya_orihara on 7/10/17.
 */

public class This extends MultiDexApplication {
    private static Cache cache;
    @Override
    public void onCreate() {
        super.onCreate();
        cache = new Cache(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



    public static Cache getCache(){
        return cache;
    }


}
