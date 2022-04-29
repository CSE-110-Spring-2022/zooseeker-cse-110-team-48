package com.example.zooseeker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOError;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class LocationDatabaseTest {
    private LocationsListItemDao dao;
    private LocationsDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, LocationsDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.locationsListItemDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testInsert() {
        LocationsListItem item1 = new LocationsListItem("Pizza time", 2.0, 0);
        LocationsListItem item2 = new LocationsListItem("Photos of Spider-Man", 2.0, 1);

        long id1 = dao.insert(item1);
        long id2 = dao.insert(item2);

        assertNotEquals(id1, id2);
    }

    @Test
    public void testGet() {
        LocationsListItem insertedItem = new LocationsListItem("Pizza time", 2.0, 0);
        long id = dao.insert(insertedItem);

        LocationsListItem item = dao.get(id);
        assertEquals(id, item.id);
        assertEquals(insertedItem.text, item.text);
        assertEquals(insertedItem.order, item.order);
    }
    @Test
    public void testUpdate() {
        LocationsListItem item = new LocationsListItem("Pizza time", 2.0, 0);
        long id = dao.insert(item);

        item = dao.get(id);
        item.text = "Photos of Spider-Man";
        int itemsUpdated = dao.update(item);
        assertEquals(1, itemsUpdated);

        item = dao.get(id);
        assertNotNull(item);
        assertEquals("Photos of Spider-Man", item.text);
    }

    @Test
    public void testDelete() {
        LocationsListItem item = new LocationsListItem("Pizza time", 2.0, 0);
        long id = dao.insert(item);

        item = dao.get(id);
        int itemsDeleted = dao.delete(item);
        assertEquals(1, itemsDeleted);
        assertNull(dao.get(id));
    }

}
