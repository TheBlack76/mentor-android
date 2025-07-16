package com.mentor.application.views.comman.dialgofragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.mentor.application.R
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.repository.preferences.UserPrefsManager
import com.mentor.application.utils.MyCustomLoader
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.MainActivity
import javax.inject.Inject

/**
 * Created by Mukesh on 29/6/18.
 */
abstract class BaseSideMenuDialogFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : DialogFragment() {

    @Inject
    lateinit var mUserPrefsManager: UserPrefsManager

    @Inject
    lateinit var mMyCustomLoader: MyCustomLoader

    private var _bindingInflater: VB? = null
    val binding: VB
        get() = _bindingInflater as VB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        _bindingInflater = bindingInflater.invoke(inflater)
        if (_bindingInflater == null) {
            throw IllegalArgumentException("binding can't be null")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme)

        // Making fragment dialog transparent and of fullscreen width
        val dialog = dialog
        if (null != dialog) {
            if (isFullScreenDialog) {
                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            } else {
                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            dialog.window!!.setWindowAnimations(R.style.MenuDialogAnimations)
        }

        // Set Toolbar
        if (toolbar != null) {
            toolbar!!.toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar!!.toolbar.setNavigationOnClickListener { dismiss() }
        }

        init()
        observeBaseProperties()

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            dialog!!.window!!.statusBarColor = Color.WHITE
        }
    }

    protected fun navigateToMainActivity() {
        mUserPrefsManager.clearUserPrefs()
        startActivity(
            Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        activity?.finish()
    }


    private fun observeBaseProperties() {
        // Observe message
        viewModel?.getSuccessMessage()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            showMessage(null, it)
        })

        // Observe any general exception
        viewModel?.getErrorHandler()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (null != it) {
                showMessage(
                    resId = it.getErrorResource(),
                    isShowSnackbarMessage = false,
                    isError = true
                )
            }
        })

        // Observe retrofit error messages
        viewModel?.getRetrofitErrorMessage()?.observe(viewLifecycleOwner, Observer {
            showMessage(
                resId = it?.errorResId,
                message = it?.errorMessage,
                isShowSnackbarMessage = false,
                isError = true
            )
        })

        // Observe visibility of loader
        viewModel?.isShowLoader()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it!!) {
                showProgressLoader()
            } else {
                hideProgressLoader()
            }
        })

        //Observer
        observeProperties()
    }

    fun showMessage(
        resId: Int? = null, message: String? = null,
        isShowSnackbarMessage: Boolean = false, isError: Boolean = false
    ) {
        if (isShowSnackbarMessage) {
            mMyCustomLoader.showSnackBar(view, message ?: getString(resId!!))
        } else {
            mMyCustomLoader.showToast( message ?: getString(resId!!), isError)
        }
    }

    protected fun showProgressLoader() {
        mMyCustomLoader.showProgressDialog()
    }

    protected fun hideProgressLoader() {
        mMyCustomLoader.dismissProgressDialog()
    }

    abstract val isFullScreenDialog: Boolean

    abstract fun init()

    abstract val viewModel: BaseViewModel?

    abstract fun observeProperties()

    abstract val toolbar: ToolbarDialogFragmentsBinding?
}