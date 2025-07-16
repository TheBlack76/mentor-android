package com.mentor.application.views.comman.dialgofragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.mentor.application.R
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.repository.preferences.UserPrefsManager
import com.mentor.application.utils.MyCustomLoader
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.MainActivity
import com.mentor.application.views.comman.activities.MainActivity.Companion.INTENT_LOGIN
import com.mentor.application.views.comman.activities.MainActivity.Companion.INTENT_VIEW_TYPE
import java.lang.IllegalArgumentException
import javax.inject.Inject

/**
 * Created by Mukesh on 29/6/18.
 */
abstract class BaseDialogFragment<VB : ViewBinding>(
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
            dialog.window!!.setWindowAnimations(R.style.DialogFragmentAnimations)
        }

        // Set Toolbar
        if (toolbar != null) {
            toolbar!!.toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar!!.toolbar.setNavigationOnClickListener { dismiss() }
        }

        init()
        observeBaseProperties()

    }

    private fun observeBaseProperties() {
        // Observe message
        viewModel?.getSuccessMessage()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            showMessage(null, it, isShowSnackbarMessage = true)
        })

        // Observe any general exception
        viewModel?.getErrorHandler()?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (null != it) {
                showMessage(
                    resId = it.getErrorResource(),
                    isShowSnackbarMessage = true,
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

    protected fun navigateToMainActivity() {
        startActivity(
            Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(INTENT_VIEW_TYPE, INTENT_LOGIN)
        )
        activity?.finish()
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