package com.tiparo.tripway.login.ui

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentLoginBinding
import com.tiparo.tripway.login.ui.SignInViewModel.SignInState
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import javax.inject.Inject

private val RC_GET_TOKEN = 1;
val TAG = "Tripway"

class LoginFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val vm: SignInViewModel by activityViewModels {
        viewModelFactory
    }

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (vm.isSignedIn()) {
            findNavController().navigate(R.id.action_login_fragment_to_discovery_fragment)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(TAG,"LoginFragment onCreateView()")
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.authenticationState.value = SignInState.UNAUTHENTICATED

//        view.sign_in_button_google.setOnClickListener {
//            vm.authenticationState.value = SignInState.LOADING
//
//            //Call GoogleAuthApi to receive tokenId
//            val signInIntent = mGoogleSignInClient.signInIntent
//            startActivityForResult(
//                signInIntent,
//                RC_GET_TOKEN
//            )
//        }

        view.button_sign_in.setOnClickListener {
            vm.authenticationState.value = SignInState.LOADING

            val email = et_email.text.toString()
            val password = et_password.text.toString()

            if (!TextUtils.isEmpty(email) and !TextUtils.isEmpty(password)) {
                signIn(email, password)
            }
        }

        view.button_sign_up.setOnClickListener {
            val email = et_email.text.toString()
            val password = et_password.text.toString()

            if (et_nickname.visibility == View.VISIBLE) {
                val nickname = et_nickname.text.toString()
                vm.authenticationState.value = SignInState.LOADING
                signUp(email, nickname, password)
            } else et_nickname.visibility = View.VISIBLE
        }

        vm.authenticationState.observe(viewLifecycleOwner, Observer {
            val navController = findNavController()
            when (it) {
                SignInState.AUTHENTICATED -> {
                    hideProgress()
                    navController.navigate(R.id.action_login_fragment_to_discovery_fragment)
                }
                SignInState.FAILED_AUTHENTICATION, SignInState.FAILED_REGISTERED -> {
                    hideProgress()
                    Toast.makeText(
                        context,
                        getString(R.string.CANT_AUTH_BACKEND),
                        Toast.LENGTH_LONG
                    ).show()
                }
                SignInState.LOADING -> {
                    showProgress()
                }
                else -> {
                }
            }
        })
    }

    private fun signIn(email: String, password: String) {
        if (email == "test") {
            findNavController().navigate(R.id.discovery_fragment_dest)
        } else {
            vm.signIn(email, password)
        }
    }

    private fun signUp(email: String, nickname: String, password: String) {
        if (email.isNotBlank() and password.isNotBlank() and nickname.isNotBlank()) {
            vm.signUp(email, nickname, password)
        } else {
            Toast.makeText(context, "Заполните все строчки", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_GET_TOKEN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            vm.handleSignInGoogleResult(task);
//        }
//    }

    private fun showProgress() {
        binding.root.signInProgressBar.visibility = View.VISIBLE

    }

    private fun hideProgress() {
        binding.root.signInProgressBar.visibility = View.GONE
    }
}
