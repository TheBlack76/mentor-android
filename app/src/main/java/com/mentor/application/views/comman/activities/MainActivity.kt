package com.mentor.application.views.comman.activities

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.mentor.application.R
import com.mentor.application.databinding.ActivityMainBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.comman.fragments.SelectUserTypeFragment
import com.mentor.application.views.comman.fragments.SplashFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseAppCompactActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    companion object{
        const val INTENT_VIEW_TYPE = "viewType"
        const val INTENT_LOGIN = 0
        const val INTENT_SPLASH = 1
    }


    override val isMakeStatusBarTransparent: Boolean
        get() = false

    override fun init() {
        // Set Splash Screen
        if (intent?.getIntExtra(INTENT_VIEW_TYPE,INTENT_SPLASH) == INTENT_SPLASH){
            doFragmentTransaction(
                fragment = SplashFragment(),
                containerViewId = R.id.flFragContainerMain, isAddToBackStack = false
            )
        }else{
            doFragmentTransaction(
                fragment = SelectUserTypeFragment(),
                containerViewId = R.id.flFragContainerMain, isAddToBackStack = false
            )
        }

        GeneralFunctions.windowDecoder(binding.flFragContainerMain)

    }

    override val navHostFragment: NavHostFragment?
        get() = null

}
