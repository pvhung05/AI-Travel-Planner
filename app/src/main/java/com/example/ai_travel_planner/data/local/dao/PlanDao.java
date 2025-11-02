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

    @Query("SELECT * FROM plans ORDER BY id DESC LIMIT 1")
    PlanEntity getLatest();

    //@Query("UPDATE plans SET tripPlanJson = :json WHERE id = :id")
    //void updateTripPlanJson(long id, String json);

    @Query("SELECT * FROM plans WHERE destination = :dest AND days = :days AND style = :style ORDER BY createdAt DESC LIMIT 1")
    PlanEntity getLatestByMeta(String dest, int days, String style);

}