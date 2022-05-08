package com.example.zooseeker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationsListItemDao {
    @Insert
    long insert(LocationsListItem locationsListItem);

    @Insert
    List<Long> insertAll(List<LocationsListItem> locationsListItem);

    @Query("SELECT * from `locations_list_items` WHERE `id`=:id")
    LocationsListItem get(long id);

    @Query("SELECT * from `locations_list_items` WHERE `textId`=:textId")
    LocationsListItem get(String textId);

    @Query("SELECT * from `locations_list_items` ORDER BY `order`")
    List<LocationsListItem> getAll();

    @Query("SELECT * from `locations_list_items` ORDER BY `order`")
    LiveData<List<LocationsListItem>> getAllLive();

    @Query("DELETE from `locations_list_items`")
    void deleteAll();

    @Update
    int update(LocationsListItem locationsListItem);

    @Delete
    int delete(LocationsListItem locationsListItem);

    @Query("SELECT `order` + 1 FROM `locations_list_items` ORDER BY `order` DESC LIMIT 1")
    int getOrderForAppend();

    @Query("SELECT COUNT(*) FROM `locations_list_items`")
    int getDataCount();
}
