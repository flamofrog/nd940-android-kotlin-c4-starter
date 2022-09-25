package com.udacity.project4

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSourceAndroid(private val locationReminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {
//    Done: Create a fake data source to act as a double to the real data source

    override suspend fun saveReminder(reminder: ReminderDTO) {
        locationReminders.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        locationReminders.clear()
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            Result.Success(locationReminders)
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            val reminder = locationReminders.firstOrNull { it.id == id }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
}