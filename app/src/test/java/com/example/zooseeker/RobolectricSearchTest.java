package com.example.zooseeker;

import static org.junit.Assert.assertEquals;

import android.widget.AutoCompleteTextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

// Maybe we can only test search w/ instrumented tests
@RunWith(AndroidJUnit4.class)
public class RobolectricSearchTest {

    @Test
    public void test_search_elephants() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                AutoCompleteTextView searchField = (AutoCompleteTextView) activity.findViewById(R.id
                        .search_field);
                searchField.setText("Elephant");

                // TODO: Get suggestions, check for Elephant Odyssey
                // null?
//                assertEquals("Elephant Odyssey", searchField.getCompletionHint());
            });
        }
    }
}
