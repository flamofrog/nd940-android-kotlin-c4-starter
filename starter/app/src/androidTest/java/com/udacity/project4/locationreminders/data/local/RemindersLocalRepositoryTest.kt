package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSourceAndroid
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private val reminder = ReminderDTO(
        title = "Example Location Reminder 1",
        description = "This is an example location reminder",
        location = "1600 Pennsylvania Avenue",
        latitude = 38.89760902552842,
        longitude = -77.03655244235057
    )

    //    Done: Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderTest() = runBlocking {
        repository.saveReminder(reminder)

        val allRemindersRes = repository.getReminders()

        Assert.assertTrue(allRemindersRes is com.udacity.project4.locationreminders.data.dto.Result.Success)
        Assert.assertEquals(
            (allRemindersRes as com.udacity.project4.locationreminders.data.dto.Result.Success).data.size,
            1
        )
    }

    @Test
    fun getReminderTest() = runBlocking {
        repository.saveReminder(reminder)

        val reminderRes = repository.getReminder(reminder.id)

        Assert.assertTrue(reminderRes is com.udacity.project4.locationreminders.data.dto.Result.Success)
        val savedReminder =
            (reminderRes as com.udacity.project4.locationreminders.data.dto.Result.Success).data

        Assert.assertEquals(savedReminder.id, reminder.id)
        Assert.assertEquals(savedReminder.description, reminder.description)
        Assert.assertEquals(savedReminder.latitude, reminder.latitude)
        Assert.assertEquals(savedReminder.longitude, reminder.longitude)
        Assert.assertEquals(savedReminder.title, reminder.title)
    }

    @Test
    fun getReminderDoesntExistTest() = runBlocking {
        val reminderRes = repository.getReminder(reminder.id)

        Assert.assertTrue(reminderRes is com.udacity.project4.locationreminders.data.dto.Result.Error)
        Assert.assertEquals("Reminder not found!", (reminderRes as com.udacity.project4.locationreminders.data.dto.Result.Error).message)
    }

    fun deleteAllTest() = runBlocking {
        repository.saveReminder(reminder)

        var allRemindersRes = repository.getReminders()
        Assert.assertTrue(allRemindersRes is com.udacity.project4.locationreminders.data.dto.Result.Success)
        Assert.assertEquals(
            (allRemindersRes as com.udacity.project4.locationreminders.data.dto.Result.Success).data.size,
            1
        )

        repository.deleteAllReminders()

        allRemindersRes = repository.getReminders()
        Assert.assertTrue(allRemindersRes is com.udacity.project4.locationreminders.data.dto.Result.Success)

        Assert.assertEquals(
            (allRemindersRes as com.udacity.project4.locationreminders.data.dto.Result.Success).data.size,
            0
        )
    }

}