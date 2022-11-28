package ua.edu.znu.hitonoriol.aweather

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
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

    private lateinit var googleSignInButton: SignInButton

    /* Facebook sign in activity result callback dispatcher and a simple callback
     * for sign in result indication. */
    private lateinit var facebookCallbackManager: CallbackManager
    private val facebookSignInCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            performFacebookSignIn(result)
        }

        override fun onCancel() {
            signInFailed("Facebook (cancelled)")
        }

        override fun onError(error: FacebookException) {
            signInFailed("Facebook")
        }
    }

    /* Google auth client and activity result callback for sign in result indication. */
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            performGoogleSignIn(task.getResult(ApiException::class.java))
        } catch (e: ApiException) {
            signInFailed("Google")
            e.printStackTrace()
        }
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
        binding.facebookBtn.registerCallback(facebookCallbackManager, facebookSignInCallback)
    }

    private fun initGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.google_auth_client_id))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        googleSignInButton = binding.googleBtn
        googleSignInButton.setOnClickListener {
            googleSignInCallback.launch(googleSignInClient.signInIntent)
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
}