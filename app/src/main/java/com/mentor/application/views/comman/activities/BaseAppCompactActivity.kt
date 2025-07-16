package com.mentor.application.views.comman.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.viewbinding.ViewBinding
import com.mentor.application.R
import com.mentor.application.repository.preferences.UserPrefsManager
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.utils.MyCustomLoader
import javax.inject.Inject
import androidx.core.view.ViewCompat


abstract class BaseAppCompactActivity<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRAS_IS_FROM_NOTIFICATION = "isFromNotification"
    }

    @Inject
    lateinit var mMyCustomLoader: MyCustomLoader

    @Inject
    lateinit var mUserPrefsManager: UserPrefsManager

    private var _bindingInflater: VB? = null
    val binding: VB
        get() = _bindingInflater as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _bindingInflater = bindingInflater.invoke(layoutInflater)

        if (_bindingInflater == null) {
            throw IllegalArgumentException("binding can't be null")
        }

        setContentView(binding.root)
        if (GeneralFunctions.isAboveLollipopDevice) {
            if (isMakeStatusBarTransparent) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )

            } else {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            }
        }
        init()


    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { view ->
                if (view is EditText) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        view.clearFocus()
                        try {
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(view.windowToken, 0)
                        } catch (e: Exception) {
                            e.printStackTrace() // Avoids crash even if something goes wrong
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    fun changeStatusBarColor(color: Int, lightStatusBar: Boolean = true) {
        window.statusBarColor = ContextCompat.getColor(this, color)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            lightStatusBar
    }

    override fun onSupportNavigateUp(): Boolean {
        // Allows NavigationUI to support proper up navigation or the drawer layout
        // drawer menu, depending on the situation
        return if (null != navHostFragment) findNavController(navHostFragment!!).navigateUp() else false
    }

    fun closeActivity() {
        /**
         * Check if the activity was opened by notification then move to HomeActivity class else
         * normally move to the last fragment or activity in the stack
         */
        if (intent.getBooleanExtra(INTENT_EXTRAS_IS_FROM_NOTIFICATION, false)) {
            startActivity(
                Intent(this, HomeActivity::class.java)
                    .putExtra(INTENT_EXTRAS_IS_FROM_NOTIFICATION, false)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        } else {
            super.finish()
        }
    }

    abstract val isMakeStatusBarTransparent: Boolean

    abstract fun init()

    abstract val navHostFragment: NavHostFragment?
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun AppCompatActivity.doFragmentTransaction(
    fragManager: FragmentManager = supportFragmentManager,
    @IdRes containerViewId: Int,
    fragment: Fragment,
    tag: String = "",
    @AnimatorRes enterAnimation: Int = 0,
    @AnimatorRes exitAnimation: Int = 0,
    @AnimatorRes popEnterAnimation: Int = 0,
    @AnimatorRes popExitAnimation: Int = 0,
    isAddFragment: Boolean = true,
    isAddToBackStack: Boolean = true,
    allowStateLoss: Boolean = false
) {


    val fragmentTransaction = fragManager.beginTransaction()
        .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)

    if (isAddFragment) {
        fragmentTransaction.add(containerViewId, fragment, tag)
    } else {
        fragmentTransaction.replace(containerViewId, fragment, tag)
    }

    if (isAddToBackStack) {
        fragmentTransaction.addToBackStack(null)
    }

    if (allowStateLoss) {
        fragmentTransaction.commitAllowingStateLoss()
    } else {
        fragmentTransaction.commit()
    }
}

fun AppCompatActivity.openShareDialog(
    shareHeading: String = getString(R.string.share_via),
    shareSubject: String = getString(R.string.app_name),
    messageToShare: String
) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = "text/plain"
    share.putExtra(Intent.EXTRA_SUBJECT, shareSubject)
    share.putExtra(Intent.EXTRA_TEXT, messageToShare)
    startActivity(Intent.createChooser(share, shareHeading))
}

fun skipFragment(fragManager: FragmentManager, skipValue: Int) {
    if (fragManager.backStackEntryCount > 0) {
        fragManager.popBackStack(
            fragManager.getBackStackEntryAt(fragManager.backStackEntryCount - skipValue).id,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }
}

fun Menu.changeItemsFont(context: Context) {
    for (i in 0 until this.size()) {
        val menuItem = this.getItem(i)

        val spannableString = SpannableString(menuItem.title)
        val endSpan = spannableString.length

        // Set Typeface span
        spannableString.setSpan(
            com.mentor.application.views.comman.utils.CustomTypefaceSpan(
                ResourcesCompat.getFont(
                    context,
                    R.font.font_roboto_regular
                )
            ), 0, endSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set color span
        spannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    context,
                    R.color.colorWhite
                )
            ), 0, endSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        menuItem.title = spannableString
    }


}
