package com.tiparo.tripway.login.ui

import android.app.Application
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.tiparo.tripway.R
import com.tiparo.tripway.login.domain.AuthRepository
import timber.log.Timber
import javax.inject.Inject


class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val applicationContext: Application
) : ViewModel() {

    val authenticationState = MediatorLiveData<SignInState>()

    private val mGoogleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()

    init {
        authenticationState.value =
            SignInState.UNAUTHENTICATED

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(applicationContext.getString(R.string.server_client_id))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
    }

//    private fun authGoogleBackend(idToken: String) {
//        val progressAuthLiveData = authRepository.authUser(idToken)
//        authenticationState.addSource(progressAuthLiveData) { response ->
//            when (response.status) {
//                Resource.Status.SUCCESS -> {
//                    authenticationState.value = SignInState.AUTHENTICATED
//                    authenticationState.removeSource(progressAuthLiveData)
//                }
//                Resource.Status.LOADING -> {
//                    authenticationState.value = SignInState.LOADING
//                }
//                Resource.Status.ERROR -> {
//                    authenticationState.value = SignInState.FAILED_AUTHENTICATION
//                    authenticationState.removeSource(progressAuthLiveData)
//
//                    Log.e(
//                        TAG,
//                        "[ERROR] AuthBackend: error = ${response.message ?: "Unknown error"}"
//                    )
//                }
//            }
//        }
//    }

//    private fun silentGoogleAuth() {
//        val task = mGoogleSignInClient.silentSignIn()
//        if (task.isSuccessful) {
//            // There's immediate result available.
//            processSilentAuthResult(task.result?.idToken)
//        } else {
//            // There's no immediate result ready, displays progress indicator and waits for the
//            // async callback.
//            authenticationState.value = SignInState.LOADING
//            task.addOnCompleteListener {
//                try {
//                    processSilentAuthResult(it.getResult(ApiException::class.java)?.idToken)
//                } catch (exception: ApiException) {
//                    //TODO format error output
//                    processSilentAuthResult(null)
//                }
//            }
//        }
//    }

//    private fun processSilentAuthResult(idToken: String?) {
//        if (idToken != null) {
//            authGoogleBackend(idToken)
//        } else {
//            authenticationState.value = SignInState.FAILED_AUTHENTICATION
//        }
//    }

    fun signUp(email: String, nickname: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    authRepository.createUser(email, nickname, password)
                        .subscribe({
                            signIn(email, password)
                            Timber.d("User is created at backend")
                        }, { error: Throwable ->
                            //TODO тут реализовать так, чтобы можно было кидать во фрагмент ошибку
                            authenticationState.value =
                                SignInState.FAILED_REGISTERED
                            Timber.e(error, "Can not create user at backend")
                            //todo удалить аккаунт из firebase, если нам не удалось зарегистрироваться на сервере
                            signOut()
                        })
                } else {
                    Timber.e(SignInState.FAILED_REGISTERED.toString())
                    authenticationState.value =
                        SignInState.FAILED_AUTHENTICATION
                    signOut()
                }
            }
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    authenticationState.value =
                        SignInState.AUTHENTICATED
                    Timber.e(SignInState.AUTHENTICATED.toString())
                } else {
                    authenticationState.value =
                        SignInState.FAILED_AUTHENTICATION
                    Timber.e(SignInState.FAILED_AUTHENTICATION.toString())
                }
            }
    }

   fun signOut() {
        auth.signOut()

    }

//    fun handleSignInGoogleResult(task: Task<GoogleSignInAccount>?) {
//        try {
//            val idToken = task?.result?.idToken
//            if (idToken != null) {
//                Timber.d("[TOKEN_ID]${idToken}")
//
////                authGoogleBackend(idToken)
//            } else {
//                authenticationState.value = SignInState.FAILED_AUTHENTICATION
//
//                Timber.e("[ERROR] CANT_GET_GOOGLE_AUTH_TOKEN")
//            }
//        } catch (e: ApiException) {
//            authenticationState.value = SignInState.FAILED_AUTHENTICATION
//            Timber.e("[ERROR] GoogleSignIn: error = $e")
//        }
//    }

    fun isSignedIn() = auth.currentUser != null

    enum class SignInState {
        EXIT,
        FAILED_EXIT,
        FAILED_REGISTERED,
        USER_REGISTERED,
        UNAUTHENTICATED_ON_START,
        UNAUTHENTICATED,  // Initial state, the user needs to authenticate
        LOADING,  // Authentication is loading
        AUTHENTICATED,  // The user has authenticated successfully
        FAILED_AUTHENTICATION, FAILED_AUTHENTICATION_ON_START // Authentication failed
    }
}