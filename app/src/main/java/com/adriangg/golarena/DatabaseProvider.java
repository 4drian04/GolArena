package com.adriangg.golarena;

import android.content.Context;

import androidx.room.Room;

public class DatabaseProvider {
    private static AppDatabase instance;

    public static synchronized AppDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "golArenaDb"
            ).build();
        }
        return instance;
    }
}
