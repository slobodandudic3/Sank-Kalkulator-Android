package com.example.dnevniceapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.dnevniceapp.model.DayEntryEntity;

import java.util.List;

@Dao
public interface DayEntryDao {

    @Insert
    void insertDayEntry(DayEntryEntity dayEntry);

    @Query("SELECT * FROM day_entries WHERE monthId = :monthId ORDER BY id ASC")
    List<DayEntryEntity> getDaysForMonth(int monthId);

    @Query("SELECT SUM(earned) FROM day_entries WHERE monthId = :monthId")
    Double getTotalForMonth(int monthId);

    @Query("DELETE FROM day_entries WHERE id = :dayId")
    void deleteDayEntry(int dayId);

    @Query("DELETE FROM day_entries WHERE monthId = :monthId")
    void deleteAllDaysForMonth(int monthId);

    @Query("UPDATE day_entries SET date = :date, hours = :hours, barTotal = :barTotal, peopleCount = :peopleCount, earned = :earned WHERE id = :dayId")
    void updateDayEntry(int dayId, String date, double hours, double barTotal, int peopleCount, double earned);

    @Query("SELECT SUM(earned) FROM day_entries")
    Double getTotalAllMonths();

    @Query("SELECT COUNT(*) FROM day_entries")
    int getTotalWorkDays();

    @Query("SELECT SUM(hours) FROM day_entries")
    Double getTotalHoursAllMonths();

    @Query("SELECT * FROM day_entries")
    List<DayEntryEntity> getAllDayEntries();

    @Query("DELETE FROM day_entries")
    void deleteAllDayEntries();
}