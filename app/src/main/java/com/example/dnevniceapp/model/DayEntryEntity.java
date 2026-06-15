package com.example.dnevniceapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "day_entries")
public class DayEntryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int monthId;

    public String date;

    public double hours;

    public double barTotal;

    public int peopleCount;

    public double earned;

    public DayEntryEntity(int monthId, String date, double hours, double barTotal, int peopleCount, double earned) {
        this.monthId = monthId;
        this.date = date;
        this.hours = hours;
        this.barTotal = barTotal;
        this.peopleCount = peopleCount;
        this.earned = earned;
    }
}