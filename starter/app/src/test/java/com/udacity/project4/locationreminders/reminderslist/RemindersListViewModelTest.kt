package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var repository: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() = runBlockingTest {
        repository = FakeDataSource(mutableListOf())
        val reminder1 = ReminderDTO(
            title = "Example Location Reminder 1",
            description = "This is an example location reminder",
            location = "1600 Pennsylvania Avenue",
            latitude = 38.89760902552842,
            longitude = -77.03655244235057
        )
        val reminder2 = ReminderDTO(
            title = "Example Location Reminder 2",
            description = "This is an example location reminder (2)",
            location = "123 Random Street",
            latitude = 10.89760902552842,
            longitude = -10.03655244235057
        )
        val reminder3 = ReminderDTO(
            title = "Example Location Reminder 3",
            description = "This is an example location reminder (3)",
            location = "123 Random Road",
            latitude = 20.89760902552842,
            longitude = -20.03655244235057
        )
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)
    }

    @After
    fun tearDown() {
        stopKoin()
    }


    @Test
    fun testRemindersSize() = runBlockingTest {
        val reminders = getDataFromResult(repository.getReminders())
        assertEquals(3, reminders?.size)
    }

    @Test
    fun testDeletingAllReminders() = runBlockingTest {
        repository.deleteAllReminders()
        val reminders = getDataFromResult(repository.getReminders())
        assertEquals(0, reminders?.size)
    }

    @Test
    fun testAddingOneReminder() = runBlockingTest {
        val newReminder = ReminderDTO(
            title = "New Reminder",
            description = "This is an example location reminder",
            location = "1600 Pennsylvania Avenue",
            latitude = 38.89760902552842,
            longitude = -77.03655244235057
        )
        repository.saveReminder(newReminder)
        val reminders = getDataFromResult(repository.getReminders())
        assertEquals(4, reminders?.size)
    }

    @Test
    fun testGettingOneReminder() = runBlockingTest {
        val firstId = getDataFromResult(repository.getReminders())?.first()?.id
        val oneReminder = getDataFromResult(repository.getReminder(firstId ?: ""))
        assertNotEquals(null, oneReminder)
    }

    private fun getDataFromResult(result: Result<List<ReminderDTO>>): List<ReminderDTO>? {
        return when (result) {
            is Result.Success<List<ReminderDTO>> -> {
                result.data
            }
            else -> {
                null
            }
        }
    }

    private fun getDataFromResult(result: Result<ReminderDTO>): ReminderDTO? {
        return when (result) {
            is Result.Success<ReminderDTO> -> {
                result.data
            }
            else -> {
                null
            }
        }
    }
}