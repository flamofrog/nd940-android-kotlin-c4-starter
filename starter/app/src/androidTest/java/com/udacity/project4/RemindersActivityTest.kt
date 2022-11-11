package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
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

    private val reminder = ReminderDTO(
        title = "Example Location Reminder 1",
        description = "This is an example location reminder",
        location = "1600 Pennsylvania Avenue",
        latitude = 38.89760902552842,
        longitude = -77.03655244235057
    )

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
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

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

//    Done: add End to End testing to the app

    @Test
    fun testShowMessageOnSaveWithNoTitleOrLocation() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.reminderDescription)).perform(
            ViewActions.typeText("test description"),
            ViewActions.closeSoftKeyboard()
        )
        runBlocking { delay(100) }

        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        val viewModel: SaveReminderViewModel = get()

        viewModel.latitude.postValue(0.0)// = 0.0
        viewModel.longitude.postValue(0.0)//.value = 0.0
        viewModel.reminderSelectedLocationStr.postValue("some location")
        viewModel.selectedPOI.postValue(PointOfInterest(LatLng(0.0, 0.0), "", ""))

        Espresso.onView(withId(R.id.save_button)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun testShowToastOnSuccessfulSave() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        runBlocking { delay(100) }

        Espresso.onView(withId(R.id.reminderTitle))
            .perform(
                ViewActions.typeText("test"),
                ViewActions.closeSoftKeyboard()
            )

        Espresso.onView(withId(R.id.reminderDescription)).perform(
            ViewActions.typeText("test description"),
            ViewActions.closeSoftKeyboard()
        )
        runBlocking { delay(100) }

        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        val viewModel: SaveReminderViewModel = get()

        viewModel.latitude.postValue(0.0)// = 0.0
        viewModel.longitude.postValue(0.0)//.value = 0.0
        viewModel.reminderSelectedLocationStr.postValue("some location")
        viewModel.selectedPOI.postValue(PointOfInterest(LatLng(0.0, 0.0), "", ""))

        Espresso.onView(withId(R.id.save_button)).perform(ViewActions.click())
        runBlocking { delay(300) }

        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        runBlocking { delay(300) }

        Espresso.onView(withText(R.string.reminder_saved))
            .inRoot(
                withDecorView(
                    not(
                        `is`(
                            getActivity(appContext)?.window?.decorView
                        )
                    )
                )
            )
            .check(ViewAssertions.matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun fullSaveReminderTest() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.reminderDescription))
            .check(ViewAssertions.matches(ViewMatchers.withHint("Description")))
        Espresso.onView(withId(R.id.reminderTitle))
            .check(ViewAssertions.matches(ViewMatchers.withHint("Reminder Title")))

        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        val viewModel: SaveReminderViewModel = get()

        viewModel.latitude.postValue(0.0)// = 0.0
        viewModel.longitude.postValue(0.0)//.value = 0.0
        viewModel.reminderSelectedLocationStr.postValue("some location")
        viewModel.selectedPOI.postValue(PointOfInterest(LatLng(0.0, 0.0), "", ""))

        Espresso.onView(withId(R.id.save_button))
            .check(ViewAssertions.matches(ViewMatchers.withText("Save")))
        Espresso.onView(withId(R.id.save_button))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
        Espresso.onView(withId(R.id.save_button)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("test"), ViewActions.closeSoftKeyboard())

        Espresso.onView(withId(R.id.reminderDescription)).perform(
            ViewActions.typeText("test description"),
            ViewActions.closeSoftKeyboard()
        )
        runBlocking { delay(100) }

        Espresso.onView(withId(R.id.saveReminder))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.logout)).check(ViewAssertions.matches(ViewMatchers.withText("LOGOUT")))

        Espresso.onView(withId(R.id.logout))
            .check(ViewAssertions.matches(ViewMatchers.withText("Logout")))
            .perform(ViewActions.click())

        Espresso.onView(withId(com.firebase.ui.auth.R.id.email_button))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText("Sign in with email")))

        activityScenario.close()
    }
}
