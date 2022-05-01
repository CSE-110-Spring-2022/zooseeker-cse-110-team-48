package com.example.zooseeker;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LocationsListViewModel extends AndroidViewModel {
    private LiveData<List<LocationsListItem>> locationsListItems;
    private final LocationsListItemDao locationsListItemDao;

    public LocationsListViewModel(@NonNull Application application) {
        super(application);
        Context context = getApplication().getApplicationContext();
        LocationsDatabase db = LocationsDatabase.getSingleton(context);
        locationsListItemDao = db.locationsListItemDao();
    }

    public LiveData<List<LocationsListItem>> getLocationsListItems() {
        if (locationsListItems == null) {
            loadUsers();
        }
        return locationsListItems;
    }

    private void loadUsers() {
        locationsListItems = locationsListItemDao.getAllLive();
    }

    public void createLocation(String text) {
        int endOfListOrder = locationsListItemDao.getOrderForAppend();
        LocationsListItem newItem = new LocationsListItem(text, 0.0, endOfListOrder);
        locationsListItemDao.insert(newItem);
    }
    public void deleteLocation(LocationsListItem locationsListItem) {
        locationsListItemDao.delete(locationsListItem);
    }
}
