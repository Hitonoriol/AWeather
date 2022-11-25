package ua.edu.znu.hitonoriol.aweather

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import ua.edu.znu.hitonoriol.aweather.databinding.ActivityLoginBinding

/**
 * Proof of concept demonstration of Facebook and Google OAuth sign-in mechanisms.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInButton: SignInButton

    private lateinit var facebookCallbackManager: CallbackManager
    private lateinit var facebookSignInButton: LoginButton

    companion object {
        private const val RC_SIGN_IN = 1337
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.loginToolbar)

        initFacebookSignIn()
        initGoogleSignIn()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun initFacebookSignIn() {
        facebookCallbackManager = CallbackManager.Factory.create()
        facebookSignInButton = binding.facebookBtn
        facebookSignInButton.registerCallback(
            facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    if (result != null)
                        performFacebookSignIn(result)
                    else
                        onError(null)
                }

                override fun onCancel() {
                    onError(null)
                }

                override fun onError(error: FacebookException?) {
                    signInFailed("Facebook")
                }
            })
    }

    private fun initGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.google_auth_client_id))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        googleSignInButton = binding.googleBtn
        googleSignInButton.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    private fun performGoogleSignIn(account: GoogleSignInAccount) {
        signInResult("Google", account.idToken!!)
    }

    private fun performFacebookSignIn(result: LoginResult) {
        signInResult("Facebook", result.accessToken.token)
    }

    private fun signInResult(method: String, token: String) {
        binding.loginCard.visibility = View.GONE
        binding.resultCard.isVisible = true
        binding.resultText.text = "Logged in via $method. Your token: $token."
    }

    private fun signInFailed(method: String) {
        Snackbar.make(
            binding.root,
            "Failed to sign in via $method.",
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (facebookCallbackManager.onActivityResult(requestCode, resultCode, data))
            return

        when (requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    performGoogleSignIn(task.getResult(ApiException::class.java))
                } catch (e: ApiException) {
                    signInFailed("Google")
                    e.printStackTrace()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}