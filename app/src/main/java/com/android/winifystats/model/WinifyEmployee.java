package com.android.winifystats.model;

import android.media.session.MediaSession;

import com.android.winifystats.cache.Cache;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by izaya_orihara on 7/7/17.
 */

public interface WinifyEmployee {


    @POST("login")
    Call<TokenDTO>postCredentials(@Body EmployeeCredentials employeeCredentials);

    @POST("start")
    Call<Void>startTimer(@Body TokenDTO tokenDTO);

    @POST("stop")
    Call<Void>stopTimer (@Body TokenDTO tokenDTO);

    @POST("statistics")
    Call<StatisticsDTO> getStatistics(@Body TokenDTO tokenDTO);
}
