package com.mentor.application.views.vendor.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentVendorProfileBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.User
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.ImagePreviewDialogFragment
import com.mentor.application.views.customer.adapters.CertificationAdapter
import com.mentor.application.views.customer.adapters.PreviousWorkAdapter
import com.mentor.application.views.customer.adapters.ReviewsAdapter
import com.mentor.application.views.customer.fragment.AllReviewsFragment
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_HOME
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.mentor.application.views.customer.interfaces.ReviewsInterface
import com.mentor.application.views.vendor.interfaces.VendorProfileInterface
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class VendorProfileFragment :
    BaseFragment<FragmentVendorProfileBinding>(FragmentVendorProfileBinding::inflate),
    OnClickListener, VendorProfileInterface, ReviewsInterface {

    private val mViewModel: OnBoardingViewModel by viewModels()

    @Inject
    lateinit var mCertificationAdapter: CertificationAdapter

    @Inject
    lateinit var mPreviousWorkAdapter: PreviousWorkAdapter

    @Inject
    lateinit var mReviewsAdapter: ReviewsAdapter


    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_my_profile)
        binding.appBarLayout.toolbar.navigationIcon = null

        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_PROFILE), Context.RECEIVER_EXPORTED
            )

        // Set adapter
        binding.rvPreviousWork.adapter = mPreviousWorkAdapter
        binding.rvCertificate.adapter = mCertificationAdapter
        binding.rvReviews.adapter = mReviewsAdapter

        // Set click listener
        binding.tvReviewsViewAll.setOnClickListener(this)

        // Set detail
        mUserPrefsManager.loginUser.let {
            binding.sdvProfile.setImageURI(
                GeneralFunctions.getImage(
                    it?.image.toString()
                )
            )

            binding.tvName.text = it?.fullName
        }

        // Api call
        mViewModel.getProfile()

        // Swipe refresh listener
        binding.swipeRefreshLayout.setOnRefreshListener {
            mViewModel.getProfile()
        }

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onProfileUpdate().observe(this, Observer {
            // Set user detail
            requireContext().sendBroadcast(Intent(INTENT_HOME))
            updateDetail(mUserPrefsManager.loginUser)

        })

        mViewModel.isShowSwipeRefreshLayout().observe(this, Observer {
            binding.swipeRefreshLayout.isRefreshing = it

        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvReviewsViewAll -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = AllReviewsFragment.newInstance(mUserPrefsManager.loginUser?._id.toString()),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }
        }
    }

    private fun updateDetail(user: User?) {
        // If the file exist with same name in local show from local else from server
        val mImageFile =
            GeneralFunctions.getLocalMediaFile(
                requireContext(),
                File(mUserPrefsManager.loginUser?.image ?: "").name
            )

        val path = if (mImageFile.exists()) {
            GeneralFunctions.getLocalImageFile(mImageFile)
        } else {
            GeneralFunctions.getUserImage(mUserPrefsManager.loginUser?.image ?: "")
        }

        binding.sdvProfile.setImageURI(path)

        binding.tvName.text = user?.fullName
        binding.tvRating.text =
            getString(
                R.string.st_rating_experince,
                user?.averageStars.toString(),
                user?.experience.toString().ifBlank { "--" })

        if (user?.bio?.isBlank() == true) {
            binding.tvNoBio.visibility = View.VISIBLE
        } else {
            binding.tvNoBio.visibility = View.GONE
        }
        binding.tvAbout.text = user?.bio

        val professionNames = user?.professions?.map { it.profession }
        binding.tvProfession.text =
            professionNames?.joinToString(" || ").toString().ifBlank { "--" }

        // Set prices
        binding.tv30MinPrice.text = GeneralFunctions.getPrice(
            user?.halfHourlyRate.toString().ifBlank { "--" }, "30min,", requireContext()
        )

        binding.tv60MinPrice.text = GeneralFunctions.getPrice(
            user?.hourlyRate.toString().ifBlank { "--" }, "60min,", requireContext()
        )

        binding.tv90MinPrice.text = GeneralFunctions.getPrice(
            user?.oneAndHalfHourlyRate.toString().ifBlank { "--" }, "90min", requireContext()
        )

        binding.tvUpcomingCount.text = user?.upcomingBookingCount.toString()
        binding.tvCompleteCount.text = user?.completedBookingCount.toString()

        // Update adapter
        binding.tvNoCertificate.visibility =
            if (user?.certificate?.isEmpty() == true) View.VISIBLE else View.GONE
        binding.tvNoPreviousWork.visibility =
            if (user?.pastWork?.isEmpty() == true) View.VISIBLE else View.GONE

        mCertificationAdapter.updateData(user?.certificate!!)
        mPreviousWorkAdapter.updateData(user.pastWork!!)

        if (user.reviews.isEmpty()) {
            binding.tvNoReviewFound.visibility = View.VISIBLE
            binding.tvReviewsViewAll.visibility = View.GONE
        } else {
            binding.tvNoReviewFound.visibility = View.GONE
            if (user.reviews.size == 5) {
                binding.tvReviewsViewAll.visibility = View.VISIBLE
            } else {
                binding.tvReviewsViewAll.visibility = View.GONE
            }
            mReviewsAdapter.updateData(user.reviews)
        }
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    // Api call
                    mViewModel.getProfile()
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }

    override fun onCertificateClick(url: String) {
        GeneralFunctions.openPdfInChromeCustomTab(url, requireContext())
    }

    override fun onPastWorkClick(url: String) {
        val mImageList = ArrayList<String>()
        mImageList.add(url)
        ImagePreviewDialogFragment.newInstance(mImageList, 0).show(childFragmentManager, "")
    }

    override fun onLoadMore() {

    }

}
