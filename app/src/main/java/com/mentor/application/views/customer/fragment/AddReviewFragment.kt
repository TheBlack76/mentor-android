package com.mentor.application.views.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentAddReviewBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.BookingsViewModel
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.dialgofragments.BaseDialogFragment
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_BOOKING
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddReviewFragment :
    BaseDialogFragment<FragmentAddReviewBinding>(FragmentAddReviewBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_BOOKING = "booking"

        fun newInstance(mBooking: Booking): AddReviewFragment {
            val args = Bundle()
            val fragment = AddReviewFragment()
            args.putParcelable(BUNDLE_BOOKING, mBooking)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: BookingsViewModel by viewModels()

    private var mBooking = Booking()

    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override val isFullScreenDialog: Boolean
        get() = true

    override fun init() {
        // Get arguments
        mBooking = arguments?.getParcelable(BUNDLE_BOOKING) ?: Booking()

        // Set toolbar
        binding.ivBack.setOnClickListener {
            dismiss()
        }

        binding.tvTitle.text =
            getString(R.string.st_your_session_with_is_complete, mBooking.professionalName)
        binding.sdvImageView.setImageURI(GeneralFunctions.getUserImage(mBooking.professionalImage))

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.tvSkip.setOnClickListener(this)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onReviewAdded().observe(this, Observer {
            // Send broadcast
            requireContext().sendBroadcast(Intent(INTENT_BOOKING))
            showMessage(
                null,
                getString(R.string.st_your_review_has_been_added_successfully), true
            )
            dismiss()
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                mViewModel.addReview(
                    mBooking._id,
                    mBooking.professionalId,
                    binding.etComment.text.toString().trim(),
                    binding.ratingbar.rating, mBooking.professionalImage
                )
            }

            R.id.tvSkip -> {
                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

        }
    }

}
