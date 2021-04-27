package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.firebase.FirebaseApp
import com.udacity.project4.MyApp
import com.udacity.project4.R.string.*
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var dataSource: FakeDataSource

    @Before
    fun setup() {
        val appContext: MyApp = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(appContext)
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(appContext, dataSource)
    }

    @After
    fun shutDown() {
        stopKoin()
    }

    @Test
    fun `onClear nulls all live data`() {
        // Given all live data is set
        setLiveData()

        // When
        saveReminderViewModel.onClear()

        // Then all live data are null
        assert(verifyAllLiveDataIsClear())
    }

    @Test
    fun `When the title is empty, the snack bar live data is updated to err_enter_title and the data is considered invalid`() {
        // Given title is empty
        val reminderDataItem = getSampleReminderData()
        reminderDataItem.title = ""

        // When
        val validData = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then the snack bar message is set to the enter title resource
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(err_enter_title))

        // And the function returns false
        assertThat(validData, `is`(false))
    }

    @Test
    fun `When the title is null, the snack bar live data is updated to err_enter_title and the data is considered invalid`() {
        // Given title is null
        val reminderDataItem = getSampleReminderData()
        reminderDataItem.title = null

        // When
        val validData = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then the snac kbar message is set to the enter title resource
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(err_enter_title))

        // And the function returns false
        assertThat(validData, `is`(false))
    }

    @Test
    fun `When the location is empty, the snack bar live data is updated to err_select_location and the data is considered invalid`() {
        // Given location is empty
        val reminderDataItem = getSampleReminderData()
        reminderDataItem.location = ""

        // When
        val validData = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then the snack bar message is set to the select location resource
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(err_select_location))

        // And the function returns false
        assertThat(validData, `is`(false))
    }

    @Test
    fun `When the location is null, the snack bar live data is updated to err_select_location and the data is considered invalid`() {
        // Given location is null
        val reminderDataItem = getSampleReminderData()
        reminderDataItem.location = null

        // When
        val validData = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then the snack bar message is set to the select location resource
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(err_select_location))

        // And the function returns false
        assertThat(validData, `is`(false))
    }

    @Test
    fun `The missing title error takes precedence over missing location error`() {
        // Given title and location are empty
        val reminderDataItem = getSampleReminderData()
        reminderDataItem.title = ""
        reminderDataItem.location = ""

        // When
        val validData = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then the snackbar message is set to the enter title resource
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(err_enter_title))

        // And the function returns false
        assertThat(validData, `is`(false))
    }

    @Test
    fun `validateEnteredData returns true when the reminderData is valid`() {
        // Given title and location are empty
        val reminderDataItem = getSampleReminderData()

        // When
        val validData = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then the function returns true
        assertThat(validData, `is`(true))
    }

    @Test
    fun `saveReminder sets the showLoading value to true while saving and once done sets it to false and sets the showToast value to the expected text and the navigation command to Back`() {
        // Given a valid reminderDataItem
        val reminderDataItem = getSampleReminderData()
        val reminderSavedText = ApplicationProvider.getApplicationContext<MyApp>().getString(reminder_saved)

        // Pause dispatcher to verify initial values
        mainCoroutineRule.pauseDispatcher()

        // When
        saveReminderViewModel.saveReminder(reminderDataItem)

        // Then show loading value is set to true
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Resume dispatcher to verify final values
        mainCoroutineRule.resumeDispatcher()

        // Then
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(reminderSavedText))
        assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun `validateAndSaveReminder saves the reminder data if it is valid`() {
        // Given a valid reminderDataItem
        val reminderDataItem = getSampleReminderData()

        // Expect
        assertEquals(dataSource.reminders?.size, 0)

        // When
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        // Then
        assertEquals(dataSource.reminders?.size, 1)
        assertEquals(dataSource.reminders?.get(0)?.id, reminderDataItem.id)
    }

    @Test
    fun `validateAndSaveReminder does not save the reminder data if it is invalid`() {
        // Given an invalid reminderDataItem
        val reminderDataItem = getSampleReminderData()
        reminderDataItem.title = null

        // Expect
        assertEquals(dataSource.reminders?.size, 0)

        // When
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        // Then
        assertEquals(dataSource.reminders?.size, 0)
    }

    private fun setLiveData() {
        saveReminderViewModel.reminderTitle.value = "title"
        saveReminderViewModel.reminderDescription.value = "description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Selected Loc String"
        saveReminderViewModel.selectedPOI.value = PointOfInterest(LatLng(0.0, 0.0), "", "")
        saveReminderViewModel.latitude.value = 0.0
        saveReminderViewModel.longitude.value = 0.0
    }

    private fun verifyAllLiveDataIsClear(): Boolean {
        return saveReminderViewModel.reminderTitle.getOrAwaitValue() == null
                && saveReminderViewModel.reminderDescription.getOrAwaitValue() == null
                && saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue() == null
                && saveReminderViewModel.selectedPOI.getOrAwaitValue() == null
                && saveReminderViewModel.latitude.getOrAwaitValue() == null
                && saveReminderViewModel.longitude.getOrAwaitValue() == null
    }


    private fun getSampleReminderData() : ReminderDataItem {
        return ReminderDataItem("title",
                "description",
                "location",
                0.0,
                0.0)
    }
}