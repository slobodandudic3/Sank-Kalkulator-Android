package com.example.dnevniceapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.dnevniceapp.model.MonthEntity;

import java.util.List;

@Dao
public interface MonthDao {

    @Insert
    long insertMonth(MonthEntity month);

    @Query("SELECT * FROM months ORDER BY id DESC")
    List<MonthEntity> getAllMonths();

    @Query("UPDATE months SET name = :newName WHERE id = :monthId")
    void updateMonthName(int monthId, String newName);

    @Query("DELETE FROM months WHERE id = :monthId")
    void deleteMonth(int monthId);

    @Query("DELETE FROM months")
    void deleteAllMonths();
}