package com.valenpatel.vatavarana;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Retrofit_API {

    //Overall, this interface defines a method getWeatherData that, when called,
    // will make an HTTP GET request to the specified endpoint "weather" with the provided query parameters (city, appid, units).
    // The response is expected to be of type WeatherJson, and Retrofit will handle the serialization of the response into this type.
    @GET("weather")
    Call<WeatherModel> getWeatherData(
            @Query("q") String city,
            @Query("appid") String appid,
            @Query("units") String units);
}
//https://api.openweathermap.org/data/2.5/weather?q=valsad&appid=1be9c93fa620d80a5ee6559d7a8652a6&units=metric

//Here’s a summary of how your Retrofit setup works:
//Base URL: https://api.openweathermap.org/data/2.5/
//Endpoint: weather
//Parameters: q, appid, and units