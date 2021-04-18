package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.BaseViewModel.AuthenticationState.AUTHENTICATED
import com.udacity.project4.base.BaseViewModel.AuthenticationState.UNAUTHENTICATED
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */

private const val SIGN_IN_REQUEST_CODE = 1001

private val TAG = AuthenticationActivity::class.java.simpleName

class AuthenticationActivity : AppCompatActivity() {

    lateinit var binding : ActivityAuthenticationBinding
    lateinit var viewModel : AuthenticationActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        viewModel = ViewModelProvider(this).get(AuthenticationActivityViewModel::class.java)

        binding.loginButton.setOnClickListener { launchSignInFlow() }

        viewModel.authenticationState.observe(this, Observer {
            onAuthenticationStateChange(it)
        })
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        // TODO: Figure out why google account sign in did not work, but email and password did
    }

    private fun onAuthenticationStateChange(authenticationState: BaseViewModel.AuthenticationState) {
        when (authenticationState) {
            AUTHENTICATED -> openRemindersActivity()
            UNAUTHENTICATED -> Log.i(TAG, "Unauthenticated")
            else -> Log.e(TAG, "Unexpected AuthenticationState: $authenticationState")
        }
    }

    private fun openRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun launchSignInFlow() {
        startActivityForResult(getSignInIntent(), SIGN_IN_REQUEST_CODE)
    }

    private fun getSignInIntent() : Intent {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(getProviders())
                .setAuthMethodPickerLayout(getAuthMethodPickerLayout())
                .build()
    }


    private fun getProviders() : List<AuthUI.IdpConfig> {
        return arrayListOf(EmailBuilder().build(), GoogleBuilder().build())
    }

    private fun getAuthMethodPickerLayout(): AuthMethodPickerLayout {
        return AuthMethodPickerLayout.Builder(R.layout.auth_method_picker_layout)
                .setEmailButtonId(R.id.email_auth_button)
                .setGoogleButtonId(R.id.google_auth_button)
                .build()
    }
}
