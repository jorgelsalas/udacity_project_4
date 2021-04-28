package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.utils.DataProvider
import com.udacity.project4.locationreminders.utils.DataProvider.Companion.getSampleReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.internal.matchers.Null

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {


    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun remindersCanBeSavedAndRetrieved() = runBlocking {
        // Given a valid reminder
        val reminder = getSampleReminderDTO()

        // When it is saved to the repository
        localRepository.saveReminder(reminder)

        // Then it can be retrieved
        val loaded = localRepository.getReminder(reminder.id)

        // And all data is properly preserved
        loaded as Success
        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.title, `is`(reminder.title))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun anErrorIsReturnedWhenFetchingANonExistentReminder() = runBlocking {
        // Given a non existent reminder id
        val nonExistentReminderId = "non_existent_reminder_id"

        // When the repository is queried with such an id
        val loaded = localRepository.getReminder(nonExistentReminderId)

        // Then an error message is returned with a null status code
        loaded as Result.Error
        assertThat(loaded.message, `is`("Reminder not found!"))
        assertThat(loaded.statusCode, `is`(Matchers.nullValue()))
    }

    @Test
    fun aListOfAllSavedRemindersCanBeRetrieved() = runBlocking {
        // Given an empty repository to which 3 reminders are added
        assertThat((localRepository.getReminders() as Success).data.size, `is`(0))
        insertSampleReminders(3)

        // When a list of all reminders is retrieved
        val reminderList = (localRepository.getReminders() as Success).data

        // Then the list contains the same number of reminders as the repository
        assertThat(reminderList, Matchers.notNullValue())
        assertThat(reminderList.isEmpty(), `is`(false))
        assertThat(reminderList.size, `is`(3))
    }

    @Test
    fun savedRemindersCanBeDeleted() = runBlocking {
        // Given a repository with 3 reminders
        insertSampleReminders(3)

        // When the repository reminders are deleted
        localRepository.deleteAllReminders()

        // Then retrieving the list of reminders returns a non null but empty list
        val reminderListAfterDeletion = (localRepository.getReminders() as Success).data
        assertThat(reminderListAfterDeletion, Matchers.notNullValue())
        assertThat(reminderListAfterDeletion.isEmpty(), `is`(true))
    }

    private suspend fun insertSampleReminders(reminderCount: Int) {
        for (i in 1 .. reminderCount) {
            localRepository.saveReminder(getSampleReminderDTO())
        }
    }

}