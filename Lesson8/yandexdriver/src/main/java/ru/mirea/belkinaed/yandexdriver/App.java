package ru.mirea.belkinaed.yandexdriver;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

public class App extends Application {
    private final String MAPKIT_API_KEY = "2c287ead-ba3f-4716-9ef0-0a6a73091944";
    @Override
    public void onCreate() {
        super.onCreate();
        // Set the api key before calling initialize on MapKitFactory.
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
    }
}
