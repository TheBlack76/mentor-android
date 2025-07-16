package com.mentor.application.views.customer.fragment

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.mentor.application.R
import com.mentor.application.databinding.FragmentWorkerDetailBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.User
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.ImagePreviewDialogFragment
import com.mentor.application.views.comman.dialgofragments.LoginDialogFragment
import com.mentor.application.views.customer.adapters.CertificationAdapter
import com.mentor.application.views.customer.adapters.PreviousWorkAdapter
import com.mentor.application.views.customer.adapters.ReviewsAdapter
import com.mentor.application.views.customer.interfaces.ReviewsInterface
import com.mentor.application.views.vendor.interfaces.VendorProfileInterface
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WorkerDetailFragment :
    BaseFragment<FragmentWorkerDetailBinding>(FragmentWorkerDetailBinding::inflate),
    OnClickListener, VendorProfileInterface, ReviewsInterface {

    companion object {
        const val BUNDLE_WORKER_DETAIL = "workerDetail"
        const val BUNDLE_PROFESSION_ID = "professionId"
        const val BUNDLE_SUB_PROFESSION_ID = "subProfessionId"

        fun newInstance(
            user: User, professionId: String,
            subProfessionId: String,
        ): WorkerDetailFragment {
            val args = Bundle()
            val fragment = WorkerDetailFragment()
            args.putParcelable(BUNDLE_WORKER_DETAIL, user)
            args.putString(BUNDLE_PROFESSION_ID, professionId)
            args.putString(BUNDLE_SUB_PROFESSION_ID, subProfessionId)
            fragment.arguments = args
            return fragment
        }

    }

    var professionId = ""
    var subProfessionId = ""
    var professionalId = ""
    var professionalName = ""

    @Inject
    lateinit var mCertificationAdapter: CertificationAdapter

    @Inject
    lateinit var mPreviousWorkAdapter: PreviousWorkAdapter

    @Inject
    lateinit var mReviewsAdapter: ReviewsAdapter

    private var mUserData = User()


    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.ivBack.setOnClickListener {
            (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
        }

        // get argument
        arguments?.let {
            mUserData = it.getParcelable(BUNDLE_WORKER_DETAIL) ?: User()
            professionId = it.getString(BUNDLE_PROFESSION_ID) ?: ""
            subProfessionId = it.getString(BUNDLE_SUB_PROFESSION_ID) ?: ""
        }

        // Set adapter
        binding.rvPreviousWork.adapter = mPreviousWorkAdapter
        binding.rvCertificate.adapter = mCertificationAdapter
        binding.rvReviews.adapter = mReviewsAdapter

        // Set click listener
        binding.tvReviewsViewAll.setOnClickListener(this)
        binding.btnScheduleBooking.setOnClickListener(this)
        binding.btnInstantBooking.setOnClickListener(this)

        updateDetail(user = mUserData)

    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    private fun updateDetail(user: User?) {
        professionalId = user?._id.toString()
        binding.sdvUserImage.setImageURI(user?.image?.let { GeneralFunctions.getUserImage(it) })
        binding.tvName.text = user?.fullName
        professionalName = user?.fullName.toString()
        binding.tvAboutLabel.text = getString(R.string.st_about, user?.fullName)
        binding.tvRating.text = getString(R.string.st_rating, user?.averageStars.toString())
        "${user?.experience} Experience".also { binding.tvExp.text = it }
        binding.tvAbout.text = user?.bio

        val professionNames = user?.professions?.map { it.profession }
        binding.tvProfession.text = professionNames?.joinToString(" || ")

        // Set prices
        binding.tv30MinPrice.text = GeneralFunctions.getPrice(
            user?.halfHourlyRate.toString(), "30min,", requireContext()
        )

        binding.tv60MinPrice.text = GeneralFunctions.getPrice(
            user?.hourlyRate.toString(), "60min,", requireContext()
        )

        binding.tv90MinPrice.text = GeneralFunctions.getPrice(
            user?.oneAndHalfHourlyRate.toString(), "90min", requireContext()
        )

        // Update adapter

        if (user?.pastWork?.isEmpty() == true) {
            binding.tvPreviousWork.visibility = View.GONE
            binding.rvPreviousWork.visibility = View.GONE
        } else {
            binding.tvPreviousWork.visibility = View.VISIBLE
            binding.rvPreviousWork.visibility = View.VISIBLE
        }

        mCertificationAdapter.updateData(user?.certificate!!)
        mPreviousWorkAdapter.updateData(user.pastWork!!)

        if (user.reviews.isEmpty()) {
            binding.tvReviews.visibility = View.GONE
            binding.tvReviewsViewAll.visibility = View.GONE
        } else {
            binding.tvReviews.visibility = View.VISIBLE
            if (user.reviews.size == 5) {
                binding.tvReviewsViewAll.visibility = View.VISIBLE
            } else {
                binding.tvReviewsViewAll.visibility = View.GONE
            }
            mReviewsAdapter.updateData(user.reviews)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnScheduleBooking -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = BookNowFragment.newInstance(
                        Booking(), professionalId, professionId,
                        subProfessionId, professionalName
                    ),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }

            R.id.btnInstantBooking -> {
                if (mUserPrefsManager.isLogin) {
//                    (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
//                        fragment = InstantBookingDurationFragment.newInstance(
//                            professionId,
//                            subProfessionId,
//                            mUserData,
//                        ),
//                        containerViewId = R.id.flFragContainerMain,
//                        enterAnimation = R.animator.slide_right_in_fade_in,
//                        exitAnimation = R.animator.scale_fade_out,
//                        popExitAnimation = R.animator.slide_right_out_fade_out
//                    )
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }

            }

            R.id.tvReviewsViewAll -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = AllReviewsFragment.newInstance(professionalId),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
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
