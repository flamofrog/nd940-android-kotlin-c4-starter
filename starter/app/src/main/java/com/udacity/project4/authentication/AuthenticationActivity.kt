package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
const val SIGN_IN_REQUEST_CODE = 1001

private const val TAG = "AuthenticationActivity"
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
//          Done: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        signInFlow()
        if (FirebaseAuth.getInstance().currentUser != null) launchRemindersActivity()
//          Done: If the user was authenticated, send him to RemindersActivity
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    private fun signInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                launchRemindersActivity()
            } else {
                Toast.makeText(applicationContext, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchRemindersActivity() {
        val activityIntent = Intent(this, RemindersActivity::class.java)
        Log.d(TAG, "Launching Reminders Activity")
        startActivity(activityIntent)
    }
}
