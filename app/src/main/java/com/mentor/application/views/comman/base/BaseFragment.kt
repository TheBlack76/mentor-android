package com.swingby.app.views.fragments.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.mentor.application.R
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.preferences.UserPrefsManager
import com.mentor.application.utils.MyCustomLoader
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.MainActivity
import java.lang.IllegalArgumentException
import javax.inject.Inject

/**
 * Created by Mukesh on 19/3/18.
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : Fragment() {

   @Inject lateinit var  mUserPrefsManager: UserPrefsManager
   @Inject lateinit var  mMyCustomLoader: MyCustomLoader

    private var _bindingInflater: VB? = null
    val binding: VB
        get() = _bindingInflater as VB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingInflater = bindingInflater.invoke(inflater)
        if (_bindingInflater == null) {
            throw IllegalArgumentException("binding can't be null")
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSoftKeyboard()

        // Set Toolbar
        if (toolbar != null) {
            toolbar?.toolbar?.setNavigationIcon(R.drawable.ic_back)
            toolbar?.toolbar?.setNavigationOnClickListener {
                hideSoftKeyboard()
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }
        }

        init(savedInstanceState)
        observeBaseProperties()
    }

    private fun observeBaseProperties() {
        // Observe message
        viewModel?.getSuccessMessage()?.observe(viewLifecycleOwner, Observer {
            showMessage(null, it, isShowSnackbarMessage = true)
        })

        // Observe any general exception
        viewModel?.getErrorHandler()?.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                showMessage(
                    resId = it.getErrorResource(),
                    isShowSnackbarMessage = true,
                    isError = true
                )
            }
        })


        // Observe user session expiration
        viewModel?.isSessionExpired()?.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                expireUserSession()
            }
        })


        // Observe visibility of loader
        viewModel?.isShowLoader()?.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                showProgressLoader()
            } else {
                hideProgressLoader()
            }
        })

        // Observe retrofit error messages
        viewModel?.getRetrofitErrorMessage()?.observe(viewLifecycleOwner, Observer {
            showMessage(
                resId = it?.errorResId,
                message = it?.errorMessage,
                isShowSnackbarMessage = true,
                isError = true
            )
        })

        // Observe screen specific data
        observeProperties()
    }

    val activityContext: Context
        get() = requireActivity()

    fun showMessage(
        resId: Int? = null, message: String? = null,
        isShowSnackbarMessage: Boolean = false, isError: Boolean = false
    ) {
        if (isShowSnackbarMessage) {
            (message ?: resId?.let { getString(it) })?.let {
                mMyCustomLoader.showSnackBar(
                    view,
                    it, isError
                )
            }
        } else {
            (message ?: resId?.let { getString(it) })?.let {
                mMyCustomLoader.showToast(
                    it,
                    isError
                )
            }
        }
    }

    fun dismissDialogFragment() {
        (childFragmentManager.findFragmentByTag(getString(R.string.dialog)) as DialogFragment).dismiss()
    }

    protected fun navigateToMainActivity() {
        startActivity(
            Intent(activityContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        activity?.finish()
    }

    protected fun showProgressLoader() {
        mMyCustomLoader.showProgressDialog()
    }

    protected fun hideProgressLoader() {
        mMyCustomLoader.dismissProgressDialog()
    }

    private fun expireUserSession() {
        showMessage(
            R.string.session_expired, null,
            false, isError = true
        )
        mUserPrefsManager.clearUserPrefs()
        startActivity(
            Intent(activity, MainActivity::class.java)
                .addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                )
        )
    }


    fun hideSoftKeyboard() {
        val inputMethodManager = activityContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun showSoftKeyboard() {
        (activityContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun openDialPad(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }


    abstract val toolbar: ToolbarBinding?

    abstract fun init(savedInstanceState: Bundle?)

    abstract val viewModel: BaseViewModel?

    abstract fun observeProperties()

}