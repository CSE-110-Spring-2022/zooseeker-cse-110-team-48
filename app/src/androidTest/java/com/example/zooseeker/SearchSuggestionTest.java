package com.example.zooseeker;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchSuggestionTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity
            .class);

    @Test
    public void searchNonExistentExhibitTest() {
        ViewInteraction materialAutoCompleteTextView = onView(
                allOf(withId(R.id.search_field),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        materialAutoCompleteTextView.perform(typeText("Bir"), closeSoftKeyboard());

        ViewInteraction materialAutoCompleteTextView2 = onView(
                allOf(withId(R.id.search_field), withText("Bir"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        materialAutoCompleteTextView2.perform(pressImeActionButton());

//        onView(withText("Bird"))
//                .inRoot(RootMatchers.isPlatformPopup())
//                .perform(click());
    }

    @Test
    public void searchNonExistentExhibitTest2() {
        ViewInteraction materialAutoCompleteTextView = onView(allOf(withId(R.id.search_field),
                isDisplayed()));
        materialAutoCompleteTextView.perform(typeText("dog"), closeSoftKeyboard());
        ViewInteraction autoCompleteTextView = onView(allOf(withId(R.id.search_field), withText
                ("Hot Dogs"), isDisplayed()));
        autoCompleteTextView.check(doesNotExist());
    }

    @Test
    public void searchExistentExhibitTest() {
        ViewInteraction materialAutoCompleteTextView = onView(allOf(withId(R.id.search_field),
                isDisplayed()));
        materialAutoCompleteTextView.perform(typeText("fox"), closeSoftKeyboard());
        ViewInteraction autoCompleteTextView = onView(allOf(withId(R.id.search_field), withText
                ("fox"), isDisplayed()));
        autoCompleteTextView.check(matches(isDisplayed()));
    }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
