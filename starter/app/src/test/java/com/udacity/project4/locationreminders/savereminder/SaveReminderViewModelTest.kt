package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.google.common.truth.Truth.assertThat

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //Done: provide testing to the SaveReminderView and its live data objects

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val goodReminderDataItem = ReminderDataItem(
        title = "Example Location Reminder 1",
        description = "This is an example location reminder",
        location = "1600 Pennsylvania Avenue",
        latitude = 38.89760902552842,
        longitude = -77.03655244235057
    )

    private val noLocationReminderDataItem = ReminderDataItem(
        title = "Example Location Reminder 1",
        description = "This is an example location reminder",
        location = "1600 Pennsylvania Avenue",
        latitude = null,
        longitude = null
    )

    private val emptyTitleReminderDataItem = ReminderDataItem(
        title = "",
        description = "This is an example location reminder",
        location = "1600 Pennsylvania Avenue",
        latitude = 38.89760902552842,
        longitude = -77.03655244235057
    )

    @Before
    fun setupTests(){
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun testValidationLogicWithGoodData_shouldReturnNoError() {
        val result = viewModel.validateAndSaveReminder(goodReminderDataItem)
        assertThat(result).isTrue()
    }

    @Test
    fun testValidationLogicWithNoLocation_shouldReturnError() {
        val result = viewModel.validateAndSaveReminder(noLocationReminderDataItem)
        assertThat(result).isFalse()
    }


    @Test
    fun testValidationLogicWithEmptyTitle_shouldReturnError() {
        val result = viewModel.validateAndSaveReminder(emptyTitleReminderDataItem)
        assertThat(result).isFalse()
    }

    fun check_loading() {
        mainCoroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(goodReminderDataItem)
        assertThat(viewModel.showLoading.value).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.value).isFalse()
    }
}