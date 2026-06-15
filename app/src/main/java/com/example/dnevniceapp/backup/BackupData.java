package com.example.dnevniceapp.backup;

import com.example.dnevniceapp.model.DayEntryEntity;
import com.example.dnevniceapp.model.MonthEntity;

import java.util.List;

public class BackupData {
    public List<MonthEntity> months;
    public List<DayEntryEntity> days;

    public BackupData(List<MonthEntity> months, List<DayEntryEntity> days) {
        this.months = months;
        this.days = days;
    }
}