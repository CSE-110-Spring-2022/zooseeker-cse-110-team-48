package com.example.zooseeker;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import android.app.Application;
import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * ViewModel to interface with database of locations/exhibits
 */
public class LocationsListViewModel extends AndroidViewModel {
    private LiveData<List<LocationsListItem>> locationsListItems;
    private final LocationsListItemDao locationsListItemDao;

    /**
     * Ctor for LocationsListViewModel: creates database and DAO
     * @param application
     */
    public LocationsListViewModel(@NonNull Application application) {
        super(application);
        Context context = getApplication().getApplicationContext();
        LocationsDatabase db = LocationsDatabase.getSingleton(context);
        locationsListItemDao = db.locationsListItemDao();
    }

    /**
     * Gets live list of database contents
     * @return
     */
    public LiveData<List<LocationsListItem>> getLocationsListItems() {
        if (locationsListItems == null) {
            loadUsers();
        }
        return locationsListItems;
    }

    private void loadUsers() {
        locationsListItems = locationsListItemDao.getAllLive();
    }

    /**
     * Creates new entry in database
     * @param text - Proper name of location (e.g. "Arctic Foxes")
     * @param textId - ID of location (e.g. "arctic_foxes")
     * @param distance - Distance to store of location
     */
    public void createLocation(String text, String textId, double distance) {
        int endOfListOrder = locationsListItemDao.getOrderForAppend();
        LocationsListItem newItem = new LocationsListItem(text, textId, distance, endOfListOrder);
        locationsListItemDao.insert(newItem);
    }

    public void deleteLocation(LocationsListItem locationsListItem) {
        locationsListItemDao.delete(locationsListItem);
    }
}
