package com.example.zooseeker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StepBackwardInDirectionsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule = new ActivityScenarioRule<>(
            MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Test
    public void testUserIsAtFirstExhibit() {
        UserLocationTrackerSingleton.getTracker().useGPSLocations = false;
        ViewInteraction materialSearchView = onView(allOf(withId(R.id.search_field)));
        materialSearchView.perform(click());
        materialSearchView.perform(replaceText("koi"), closeSoftKeyboard());

        // Source: https://stackoverflow.com/a/54307601
        onView(withText("Koi Fish")).inRoot(RootMatchers.isPlatformPopup()).perform(click());

        ViewInteraction materialListButton = onView(allOf(withId(R.id.view_list_btn)));
        materialListButton.perform(click());
        ViewInteraction materialPlanButton = onView(allOf(withId(R.id.plan_route_btn)));
        materialPlanButton.perform(click());
        ViewInteraction materialPrevButton = onView(allOf(withId(R.id.prev_route_button)));
        materialPrevButton.perform(click());
        ViewInteraction messageView = onView(allOf(withId(android.R.id.message)));
        messageView.check(matches(withText("No exhibit to go back to!")));
    }
}
