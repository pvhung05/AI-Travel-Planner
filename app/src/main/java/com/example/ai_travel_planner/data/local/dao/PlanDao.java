package com.example.ai_travel_planner.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ai_travel_planner.data.local.entities.PlanEntity;

import java.util.List;

@Dao
public interface PlanDao {

    @Insert
    long insert(PlanEntity plan);

    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    List<PlanEntity> getAll();

    @Query("DELETE FROM plans WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM plans")
    void deleteAll();

    @Query("SELECT * FROM plans WHERE id = :id LIMIT 1")
    PlanEntity getById(long id);

    @Query("UPDATE plans SET imageUrl = :url WHERE id = :id")
    void updateImage(long id, String url);
}