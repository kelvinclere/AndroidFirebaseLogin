package com.example.androidfirebaselogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth.AuthStateListener


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private var signInButton: SignInButton? = null
    private var googleApiClient: GoogleApiClient? = null
    var name: String? = null
    var email: String? = null
    var idToken: String? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = AuthStateListener { firebaseAuth ->
            // Get signedIn user
            val user = firebaseAuth.currentUser

            //if user is signed in, we call a helper method to save the user details to Firebase
            if (user != null) {
                // User is signed in
                // you could place other firebase code
                //logic to save the user details to Firebase
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) //you can also use R.string.default_web_client_id
            .requestEmail()
            .build()
        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener(View.OnClickListener {
            val intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(intent, RC_SIGN_IN)
        })
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        if (result!!.isSuccess) {
            val account = result.signInAccount
            idToken = account!!.idToken
            name = account.displayName
            email = account.email
            // you can store user data to SharedPreference
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuthWithGoogle(credential)
        } else {
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Login Unsuccessful. $result")
            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(credential: AuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                Log.d(
                    TAG,
                    "signInWithCredential:onComplete:" + task.isSuccessful
                )
                if (task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    gotoProfile()
                } else {
                    Log.w(
                        TAG,
                        "signInWithCredential" + task.exception!!.message
                    )
                    task.exception!!.printStackTrace()
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun gotoProfile() {
        val intent = Intent(this@MainActivity, profileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (authStateListener != null) {
            FirebaseAuth.getInstance().signOut()
        }
        firebaseAuth!!.addAuthStateListener(authStateListener!!)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null) {
            firebaseAuth!!.removeAuthStateListener(authStateListener!!)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 1
    }
}

class profileActivity {

}
