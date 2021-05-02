package com.udacity.project4

import android.app.Application
import androidx.annotation.IdRes
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.R.id.*
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun clearDb() {
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun remindersCanBeSaved() {
        // Given the application loads with no reminders saved
        val scenario = launchApp()
        verifyReminderListIsShown()

        // When a new reminder is saved
        saveNewReminder()

        // Then it is displayed in the list
        ensureSavedReminderIsDisplayedInList()

        scenario.close()
    }

    private fun launchApp() : ActivityScenario<RemindersActivity> {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        return scenario
    }

    private fun verifyReminderListIsShown() {
        isDisplayed(addReminderFAB)
        isDisplayed(noDataTextView)
        isDisplayed(reminderssRecyclerView)
    }

    private fun saveNewReminder() {
        openSaveReminderScreen()
        verifySaveReminderScreenIsShown()
        enterReminderData()
        openLocationSelectionScreen()
        verifyLocationSelectionScreenIsShown()
        selectLocation()
        ensureLocationDataWasUpdated()
        saveReminder()
    }

    private fun openSaveReminderScreen() {
        click(addReminderFAB)
    }

    private fun verifySaveReminderScreenIsShown() {
        isDisplayed(reminderTitle)
        isDisplayed(reminderDescription)
        isDisplayed(selectLocation)
        isDisplayed(saveReminder)
        isNotDisplayed(selectedLocation)
    }

    private fun enterReminderData() {
        enterText(reminderTitle, "title")
        enterText(reminderDescription, "description")
    }

    private fun openLocationSelectionScreen() {
        click(selectLocation)
    }

    private fun verifyLocationSelectionScreenIsShown() {
        isDisplayed(mapFragment)
        isDisplayed(save_button)
    }

    private fun selectLocation() {
        longClick(mapFragment)
        click(save_button)
    }

    private fun ensureLocationDataWasUpdated() {
        isDisplayed(selectedLocation)
    }

    private fun saveReminder() {
        click(saveReminder)
    }

    private fun ensureSavedReminderIsDisplayedInList() {
        isViewWithTextDisplayed("title")
        isViewWithTextDisplayed("description")
        isNotDisplayed(noDataTextView)
    }

    private fun isDisplayed(@IdRes id : Int) {
        onView(withId(id)).check(matches(isDisplayed()))
    }

    private fun isNotDisplayed(@IdRes id : Int) {
        onView(withId(id)).check(matches(IsNot.not(isDisplayed())))
    }

    private fun isViewWithTextDisplayed(text: String) {
        onView(withText(text)).check(matches(isDisplayed()))
    }

    private fun click(@IdRes id : Int) {
        onView(withId(id)).perform(click())
    }

    private fun enterText(@IdRes id : Int, text: String) {
        onView(withId(id)).perform(replaceText(text))
    }

    private fun longClick(@IdRes id : Int) {
        onView(withId(id)).perform(longClick())
    }
}
