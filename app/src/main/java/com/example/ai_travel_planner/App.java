package com.example.ai_travel_planner;

import android.app.Application;
import com.google.android.libraries.places.api.Places;
import com.example.ai_travel_planner.BuildConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
    }
}