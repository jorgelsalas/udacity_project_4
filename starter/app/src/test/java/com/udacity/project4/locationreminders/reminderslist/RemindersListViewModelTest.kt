package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.utils.DataProvider
import com.udacity.project4.locationreminders.utils.DataProvider.Companion.getSampleReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is.*
import org.hamcrest.core.IsNot.not
import org.hamcrest.core.IsNull.nullValue
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var dataSource: FakeDataSource

    @Before
    fun setup() {
        val appContext: MyApp = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(appContext)
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(appContext, dataSource)
    }

    @After
    fun shutDown() {
        stopKoin()
    }

    @Test
    fun `loadReminders - sets showLoading to true while retrieving the reminders and once retrieved sets it to false`() {
        // Pause dispatcher to verify initial values
        mainCoroutineRule.pauseDispatcher()

        // When
        remindersListViewModel.loadReminders()

        // Then show loading value is set to true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Resume dispatcher to verify final values
        mainCoroutineRule.resumeDispatcher()

        // Then
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `loadReminders - sets showNoData to true if the remindersList is null`() {
        // Given
        remindersListViewModel.remindersList.value = null

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun `loadReminders - sets showNoData to true if the remindersList is empty`() {
        // Given
        remindersListViewModel.remindersList.value = mutableListOf()

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun `loadReminders - sets showNoData to false if the remindersList is not empty`() = runBlockingTest {
        // Given
        dataSource.saveReminder(getSampleReminderDTO())

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `loadReminders - when the data source is empty, sets remindersList to an empty list`() = runBlockingTest {
        // Given
        remindersListViewModel.remindersList.value = null

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), not(nullValue()))
        assertEquals(remindersListViewModel.remindersList.getOrAwaitValue().size, 0)
    }

    @Test
    fun `loadReminders - when the data source contains reminders, sets remindersList to the corresponding list of stored values`() = runBlockingTest {
        // Given
        val sampleReminderDTO = getSampleReminderDTO()
        remindersListViewModel.remindersList.value = null
        dataSource.saveReminder(sampleReminderDTO)

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), not(nullValue()))
        assertEquals(remindersListViewModel.remindersList.getOrAwaitValue().size, 1)
        assertEquals(remindersListViewModel.remindersList.getOrAwaitValue()[0].id, sampleReminderDTO.id)
    }

    @Test
    fun `loadReminders - when there's an error loading the data, sets the showSnackBar to the message provided by the error instance`() = runBlockingTest {
        // Given
        dataSource.setReturnError(true)

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test Exception"))
    }

}