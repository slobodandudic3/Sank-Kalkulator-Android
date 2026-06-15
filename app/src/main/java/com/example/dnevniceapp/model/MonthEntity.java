package com.example.dnevniceapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "months")
public class MonthEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public MonthEntity(String name) {
        this.name = name;
    }
}