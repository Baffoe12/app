package com.example.accidentsys.api;

import com.example.accidentsys.models.AccidentReport;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AccidentReportService {
    @GET("accidents/")
    Call<List<AccidentReport>> getAccidentReports(@Header("Authorization") String authToken);
    
    @GET("accidents/{id}/")
    Call<AccidentReport> getAccidentReport(@Header("Authorization") String authToken, @Path("id") int id);
    
    @POST("accidents/")
    Call<AccidentReport> createAccidentReport(@Header("Authorization") String authToken, @Body AccidentReport accidentReport);
}
