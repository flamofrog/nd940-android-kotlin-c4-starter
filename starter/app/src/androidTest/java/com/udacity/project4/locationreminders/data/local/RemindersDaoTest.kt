package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private val reminder = ReminderDTO(
        title = "Example Location Reminder 1",
        description = "This is an example location reminder",
        location = "1600 Pennsylvania Avenue",
        latitude = 38.89760902552842,
        longitude = -77.03655244235057
    )

    //    Done: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {

        database.reminderDao().saveReminder(reminder)

        val savedReminder = database.reminderDao().getReminderById(reminder.id)

        checkNotNull(savedReminder as ReminderDTO)
        assertThat(savedReminder.id , `is`(reminder.id))
        assertThat(savedReminder.description , `is`(reminder.description))
        assertThat(savedReminder.latitude , `is`(reminder.latitude))
        assertThat(savedReminder.longitude , `is`(reminder.longitude))
        assertThat(savedReminder.title , `is`(reminder.title))
        assertThat(savedReminder.location , `is`(reminder.location))
    }

    @Test
    fun saveThenDeleteReminder() = runBlockingTest {

        database.reminderDao().saveReminder(reminder)
        var allReminders = database.reminderDao().getReminders()

        assertThat(allReminders.size, `is`(1))

        database.reminderDao().deleteAllReminders()
        allReminders = database.reminderDao().getReminders()

        assertThat(allReminders.size, `is`(0))
    }

}