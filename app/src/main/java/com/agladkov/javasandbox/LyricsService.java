package com.agladkov.javasandbox;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LyricsService {

    @GET("./search/titles/results/")
    Call<JsonObject> listLyrics(@Query("terms") String state,
                                @Query("format") String format);
}
