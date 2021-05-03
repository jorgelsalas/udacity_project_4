package com.udacity.project4

import androidx.annotation.IdRes
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.DataProvider
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class ReminderDescriptionActivityTest {

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Test
    fun reminderDescriptionActivityProperlyDisplaysReminderData() {
        // Given a sample ReminderDataItem
        val reminderDataItem = DataProvider.getSampleReminderData()

        // When the app is launched to the ReminderDescriptionActivity with the reminderDataItem
        val scenario = launchApp(reminderDataItem)

        // Then the reminderDataItem data is displayed
        verifyReminderDataItems(reminderDataItem)

        scenario.close()
    }

    private fun launchApp(reminderDataItem: ReminderDataItem): ActivityScenario<ReminderDescriptionActivity> {
        val intent = ReminderDescriptionActivity.newIntent(getApplicationContext(), reminderDataItem)
        val scenario = ActivityScenario.launch<ReminderDescriptionActivity>(intent)

        dataBindingIdlingResource.monitorActivity(scenario)

        return scenario
    }

    private fun verifyReminderDataItems(reminderDataItem: ReminderDataItem) {
        isDisplayed(R.id.reminder_details_header)
        isDisplayed(R.id.title)
        isDisplayed(R.id.description)
        isDisplayed(R.id.location)
        isDisplayed(R.id.latitude)
        isDisplayed(R.id.longitude)

        isViewWithPartialTextDisplayed(R.id.title, reminderDataItem.title!!)
        isViewWithPartialTextDisplayed(R.id.description, reminderDataItem.description!!)
        isViewWithPartialTextDisplayed(R.id.location, reminderDataItem.location!!)
        isViewWithPartialTextDisplayed(R.id.latitude, reminderDataItem.latitude.toString())
        isViewWithPartialTextDisplayed(R.id.longitude, reminderDataItem.longitude.toString())
    }

    private fun isDisplayed(@IdRes id : Int) {
        onView(withId(id)).check(matches(isDisplayed()))
    }

    private fun isViewWithPartialTextDisplayed(@IdRes id : Int, text: String) {
        onView(withId(id)).check(matches(withText(containsString(text))));
    }
}
