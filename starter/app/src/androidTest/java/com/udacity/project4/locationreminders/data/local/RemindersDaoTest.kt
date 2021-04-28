package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.DataProvider.Companion.getSampleReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun remindersCanBeSavedAndRetrieved() = runBlockingTest {
        // Given a valid reminder
        val reminder = getSampleReminderDTO()

        // When it is saved to the database
        database.reminderDao().saveReminder(reminder)

        // Then it can be retrieved
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat(loaded as ReminderDTO, Matchers.notNullValue())

        // And all data is properly preserved
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun aListOfAllSavedRemindersCanBeRetrieved() = runBlockingTest {
        // Given an empty database to which 3 reminders are added
        assertThat(database.reminderDao().getReminders().size, `is`(0))
        insertSampleReminders(3)

        // When a list of all reminders is retrieved
        val reminderList = database.reminderDao().getReminders()

        // Then the list contains the same number of reminders as the database
        assertThat(reminderList, Matchers.notNullValue())
        assertThat(reminderList.isEmpty(), `is`(false))
        assertThat(reminderList.size, `is`(3))
    }

    @Test
    fun savedRemindersCanBeDeleted() = runBlockingTest {
        // Given a database with 3 reminders
        insertSampleReminders(3)

        // When the database reminders are deleted
        database.reminderDao().deleteAllReminders()

        // Then retrieving the list of reminders returns a non null but empty list
        val reminderListAfterDeletion = database.reminderDao().getReminders()
        assertThat(reminderListAfterDeletion, Matchers.notNullValue())
        assertThat(reminderListAfterDeletion.isEmpty(), `is`(true))
    }

    private suspend fun insertSampleReminders(reminderCount: Int) {
        for (i in 1 .. reminderCount) {
            database.reminderDao().saveReminder(getSampleReminderDTO())
        }
    }

}