package com.ayhanunal.foursquarecloneparse;

import android.app.Application;

import com.parse.Parse;

public class ParseStarterClass extends Application {


    @Override
    public void onCreate() {
        super.onCreate();


        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("jgHCqTJda3H9S6AaW2W1cXlL0bqerFMfiW1tsS3a")
                .clientKey("sQsCW6FbbH93Gp1gsBuGZPKPMf8OAtM0nzWZbrzd")
                .server("https://parseapi.back4app.com/")
                .build());



    }
}
