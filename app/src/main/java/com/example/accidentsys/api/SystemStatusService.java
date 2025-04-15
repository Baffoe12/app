package com.example.accidentsys.api;

import com.example.accidentsys.models.SystemStatus;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SystemStatusService {
    @GET("system-status/")
    Call<SystemStatus> getSystemStatus(@Header("Authorization") String authToken);
    
    @POST("system-status/")
    Call<SystemStatus> updateSystemStatus(@Header("Authorization") String authToken, @Body SystemStatus systemStatus);
}
