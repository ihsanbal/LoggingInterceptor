package ihsanbal.com.logginginterceptor.ui;

import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ihsanbal.com.logginginterceptor.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class InterceptorsTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void interceptorsPostTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_post), withText("POST"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

    @Test
    public void interceptorsZipTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_zip), withText("ZIP"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

    @Test
    public void interceptorsGetTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_get), withText("GET"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

    @Test
    public void interceptorsDeleteTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_delete), withText("DELETE"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

    @Test
    public void interceptorsTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_patch), withText("PATCH"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

    @Test
    public void interceptorsPutTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_put), withText("PUT"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

    @Test
    public void interceptorsFileTest() {
        ViewInteraction buttonPost = onView(
                allOf(withId(R.id.button_pdf), withText("FILE"), isDisplayed()));
        buttonPost.perform(click());
        buttonPost.check(matches(isDisplayed()));
    }

}
