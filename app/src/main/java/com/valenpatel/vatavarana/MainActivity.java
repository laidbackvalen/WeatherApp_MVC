package com.valenpatel.vatavarana;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieListener;
import com.valenpatel.vatavarana.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    WeatherModel model;
    int sunriseHourFromAPI;
    int sunsetHourFromAPI;
    int currentFrame;
    ImageView moreImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        moreImage = findViewById(R.id.moreImageView);
        moreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MainActivity2.class));

            }
        });
        //default city
        fetchWeatherData("pune");

        binding.searchViewMain.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    fetchWeatherData(query.trim());

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

    }

    private void fetchWeatherData(String cityname) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Retrofit_API networkcall = retrofit.create(Retrofit_API.class);
        Call<WeatherModel> response = networkcall.getWeatherData(cityname, Constants.API_KEY, "metric");

        response.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful()) {
                    model = response.body();
                    if (model != null) {
                        binding.cityMain.setText(model.getName());
                        binding.dateMain.setText(datetoday());

                        binding.temperatureReadingMain.setText(String.valueOf(Math.round(model.getMain().getTemp())));
//                        binding.minTempMain.setText(String.valueOf(model.getMain().getTempMin()));
//                        binding.maxTempMain.setText(String.valueOf(model.getMain().getTempMax()));
                        binding.tempReading.setText(model.getMain().getHumidity() + " %");
                        binding.dayTxt.setText(dayToday().toUpperCase());
                        binding.windSpeedReading.setText(String.valueOf(model.getWind().getSpeed()));
                        binding.todaysConditionbelowDay.setText(String.valueOf(model.getWeather().get(0).getDescription()));
                        binding.conditionType.setText(String.valueOf(model.getWeather().get(0).getMain()));
                        binding.sunRiseReading.setText(String.valueOf(timetoday(model.getSys().getSunrise())));
                        binding.sunSetReading.setText(String.valueOf(timetoday(model.getSys().getSunset())));
//                        binding.seaReading.setText(String.valueOf(model.getMain().getSeaLevel()));

                        changeBackgroundAsWeatherCondition(String.valueOf(model.getWeather().get(0).getMain()));

                        String a = String.valueOf(timetoday(model.getSys().getSunrise()));
                        sunriseHourFromAPI = Integer.parseInt(a.substring(0, 2));
                        String b = String.valueOf(timetoday(model.getSys().getSunset()));
                        sunsetHourFromAPI = Integer.parseInt(b.substring(0, 2));

                        //Function for sunpostion in lottie animation
                        lottieSetSunsCurrentPosition(sunriseHourFromAPI, sunsetHourFromAPI);

                    } else {
                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No records found : " + cityname, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable throwable) {
            }
        });

    }

    private void lottieSetSunsCurrentPosition(int sunriseHourFromAP, int sunsetHourFromAP) {
        LottieCompositionFactory.fromRawRes(getApplicationContext(), R.raw.sunrisesetlottie).addListener(new LottieListener<LottieComposition>() {
            @Override
            public void onResult(LottieComposition result) {
                int totalFrames = (int) result.getDurationFrames();

                // Define sunrise and sunset times
                int sunriseHour = sunriseHourFromAP;
                int sunsetHour = sunsetHourFromAP;

                //Get the current time
                Calendar now = Calendar.getInstance();
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);
                int currentSecond = now.get(Calendar.SECOND);
                int currentMillisecond = now.get(Calendar.MILLISECOND);

                // Convert current time to total seconds since midnight
                int currentTimeInSeconds = currentHour * 3600 + currentMinute * 60 + currentSecond + currentMillisecond / 1000;

                // Convert sunrise and sunset times to total seconds since midnight
                int sunriseTimeInSeconds = sunriseHour * 3600;
                int sunsetTimeInSeconds = sunsetHour * 3600;

                // Calculate the duration between sunrise and sunset in seconds
                int dayDurationInSeconds = sunsetTimeInSeconds - sunriseTimeInSeconds;

                // Check if the current time is within the day duration

                if (currentTimeInSeconds >= sunriseTimeInSeconds && currentTimeInSeconds <= sunsetTimeInSeconds) {
                    // Calculate the current time relative to sunrise
                    int timeSinceSunriseInSeconds = currentTimeInSeconds - sunriseTimeInSeconds;

                    // Calculate the fraction of the day that has passed
                    double timeFraction = (double) timeSinceSunriseInSeconds / dayDurationInSeconds;

                    // Map the time fraction to the total frames of the animation
                    currentFrame = (int) (timeFraction * totalFrames);

                    //functrion for animation to current frame
                    lottieToCurrentFramesAnimation(currentFrame);
                    //setting Sun's current position on lottie
                    binding.lottieAnimationViewSunRiseSet.setFrame(currentFrame);
                } else {
                    // If the current time is outside the sunrise to sunset period, pause the animation
                    binding.lottieAnimationViewSunRiseSet.pauseAnimation();

                }
            }
        });
    }


    private void changeBackgroundAsWeatherCondition(String condition) {
        if (condition.matches("Clouds")) {
            binding.main.setBackground(getDrawable(R.drawable.raining_gradient));
            binding.lottieAnimationView2.setAnimation(R.raw.cloudslottie);
            binding.lottieAnimationView2.playAnimation();
        } else if (condition.matches("Clear")) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 18) {
                binding.main.setBackground(getDrawable(R.drawable.rect_main_bg_bottom_purple_gradient));
                binding.lottieAnimationView2.setAnimation(R.raw.night_moon_lottie);
                binding.lottieAnimationView2.playAnimation();
            } else {
                binding.main.setBackground(getDrawable(R.drawable.rect_main_bg_bottom_purple_gradient));
                binding.lottieAnimationView2.setAnimation(R.raw.sun_other);
                binding.lottieAnimationView2.playAnimation();
            }
        } else if (condition.matches("Rain")) {
            binding.main.setBackground(getDrawable(R.drawable.raining_gradient));
            binding.lottieAnimationView2.setAnimation(R.raw.raininglottie);
            binding.lottieAnimationView2.playAnimation();
        } else if (condition.matches("Snow")) {
            binding.main.setBackground(getDrawable(R.drawable.snow_fall_gradient_mian_bg));
            binding.lottieAnimationView2.setAnimation(R.raw.snowfalllottie);
            binding.lottieAnimationView2.playAnimation();
        }
    }

    String datetoday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    String timetoday(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(timestamp * 1000));
    }

    String dayToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void lottieToCurrentFramesAnimation(int targetFrame) {
        // Set the initial frame to 0
        binding.lottieAnimationViewSunRiseSet.setFrame(0);
        // Create a ValueAnimator to animate from 0 to targetFrame
        ValueAnimator animator = ValueAnimator.ofInt(0, targetFrame);
        animator.setDuration(2000); // Set the duration of the animation (e.g., 2000ms or 2 seconds)
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                binding.lottieAnimationViewSunRiseSet.setFrame(animatedValue);
            }
        });
        // Start the animation
        animator.start();
    }

}