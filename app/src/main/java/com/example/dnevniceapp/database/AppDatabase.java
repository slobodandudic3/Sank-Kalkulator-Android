package com.example.dnevniceapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.dnevniceapp.dao.DayEntryDao;
import com.example.dnevniceapp.dao.MonthDao;
import com.example.dnevniceapp.model.DayEntryEntity;
import com.example.dnevniceapp.model.MonthEntity;

@Database(
        entities = {MonthEntity.class, DayEntryEntity.class},
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract MonthDao monthDao();

    public abstract DayEntryDao dayEntryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "dnevnice_database"
            ).build();
        }

        return instance;
    }
}