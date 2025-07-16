package com.mentor.application.views.comman.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewbinding.ViewBinding
import com.app.glambar.repository.models.post.PostFacebookLogin
import com.app.glambar.repository.models.post.PostGoogleLogin
import com.facebook.*
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.swingby.app.views.fragments.base.BaseFragment
import com.mentor.application.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * Created by Mukesh on 18/10/18.
 */
abstract class BaseSocialLoginFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : BaseFragment<VB>(bindingInflater) {

    private val mCallbackManager: CallbackManager? by lazy {
        CallbackManager.Factory.create()
    }


    override fun init(savedInstanceState: Bundle?) {
        getSHA1ForFacebook()
    }


    /**
     * Facebook Sign-In
     */
    fun getSHA1ForFacebook() {
        try {
            @SuppressLint("PackageManagerGetSignatures") val info = requireActivity()
                .packageManager.getPackageInfo(
                    requireActivity().packageName, PackageManager.GET_SIGNATURES
                )
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        } catch (ignored: NoSuchAlgorithmException) {
        }

    }

    fun doFacebookLogin() {
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY)
            .logInWithReadPermissions(
                this@BaseSocialLoginFragment,
                mCallbackManager!!,
                listOf("email", "public_profile")
            )

        // For Facebook SignIn
        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    showProgressLoader()
                    val request = GraphRequest.newMeRequest(
                        loginResult.accessToken
                    ) { `object`, response ->
                        if (null != response) {
                            try {
                                LoginManager.getInstance().logOut()
                                Log.e("loginData", "onSuccess: " + `object`)
                                // Create PojoRegister object out of response
                                val fullName = `object`!!.getString("name")
                                val pojoRegister = PostFacebookLogin(
                                    fullName,
                                    `object`.getString("id"),
                                    if (`object`.has("email")) {
                                        `object`.getString("email")
                                    } else {
                                        "${
                                            fullName.lowercase()
                                                .replace(" ", "")
                                        }@facebook.com"
                                    },

                                    "ANDROID",
                                    "gdsggegegee"
                                )
                                hideProgressLoader()
                                onSuccessfullFbLogin(pojoRegister)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                hideProgressLoader()
                                showMessage(resId = R.string.retrofit_failure)
                            }

                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "email,name")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {}

                override fun onError(exception: FacebookException) {
                    Log.e("error", "onError: " + exception.message)
                    var errorMessage = exception.message
                    if ((errorMessage ?: "").contains("ERR_INTERNET_DISCONNECTED")) {
                        errorMessage = getString(R.string.no_internet)
                    }
                    showMessage(message = errorMessage)
                }
            })
    }

    private fun isPackageInstalled(
        packageName: String,
        packageManager: PackageManager
    ): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    /**
     * Google Sign-In
     */
    private val mGoogleSignInClient: GoogleSignInClient? by lazy {
        // Configure sign-in to request the user's ID, email address, and basic
        // user. ID and basic user are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions
                .DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // New Google SignIn Client
        GoogleSignIn.getClient(activityContext, gso)
    }

    fun doGooglePlusLogin() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        googleActivityResultLauncher.launch(signInIntent)
    }

    private var googleActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val googleSignInAccount = task.getResult(ApiException::class.java)
                // Create PojoRegister object out of response
                val pojoGoogleLogin = PostGoogleLogin(
                    googleSignInAccount?.photoUrl.toString(),
                    googleSignInAccount?.displayName ?: "",
                    googleSignInAccount?.id ?: "0",
                    googleSignInAccount?.email ?: ""

                )
                Log.e("socialLogin", ": "+googleSignInAccount.photoUrl )
                onSuccessfullGoogleLogin(pojoGoogleLogin)
                mGoogleSignInClient?.signOut()

            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                showMessage(resId = R.string.retrofit_failure)
            }

        }
    }


    internal abstract fun onSuccessfullFbLogin(postFacebookLogin: PostFacebookLogin)
    internal abstract fun onSuccessfullGoogleLogin(postGoogleLogin: PostGoogleLogin)

}