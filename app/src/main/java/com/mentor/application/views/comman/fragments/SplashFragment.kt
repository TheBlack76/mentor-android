package com.mentor.application.views.comman.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import com.mentor.application.databinding.FragmentSplashBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.swingby.app.views.fragments.base.BaseFragment
import com.mentor.application.R
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private var looper = Handler(Looper.getMainLooper())
    private var isSafeToCommit = false

    override val toolbar: ToolbarBinding?
        get() = null

    override fun onStart() {
        super.onStart()
        isSafeToCommit = true
    }

    override fun onStop() {
        super.onStop()
        isSafeToCommit = false
    }

    override fun init(savedInstanceState: Bundle?) {

        // Set animation
        val animObjText = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_text)
        animObjText.duration = 1500
        animObjText.startOffset = 500
        binding.ivLogoText.startAnimation(animObjText)

        // Do transaction
        looper.postDelayed({
            if (!isAdded || !isSafeToCommit) return@postDelayed

            if (mUserPrefsManager.isLogin) {
                startActivity(Intent(requireContext(), HomeActivity::class.java))
                (activityContext as? BaseAppCompactActivity<*>)?.finish()
            } else {
                (activityContext as? BaseAppCompactActivity<*>)?.doFragmentTransaction(
                    fragment = LaunchFragment(),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.fade_out,
                    isAddToBackStack = false
                )
            }
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        (activityContext as? BaseAppCompactActivity<*>)?.changeStatusBarColor(R.color.colorPrimary)
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {
        // No observation logic needed
    }

    override fun onDestroy() {
        super.onDestroy()
        looper.removeCallbacksAndMessages(null)
    }
}
