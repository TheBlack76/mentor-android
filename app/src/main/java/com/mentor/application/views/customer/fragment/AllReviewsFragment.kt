package com.mentor.application.views.customer.fragment

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mentor.application.R
import com.mentor.application.databinding.FragmentBaseRecyclerViewBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.BookingsViewModel
import com.mentor.application.views.customer.adapters.ReviewsAdapter
import com.mentor.application.views.customer.interfaces.ReviewsInterface
import com.swingby.app.views.fragments.base.BaseRecyclerViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AllReviewsFragment :
    BaseRecyclerViewFragment<FragmentBaseRecyclerViewBinding>(FragmentBaseRecyclerViewBinding::inflate),
    ReviewsInterface {

    companion object {
        const val BUNDLE_PROFESSIONAL_ID = "professionalId"

        fun newInstance(professionalId: String): AllReviewsFragment {
            val args = Bundle()
            val fragment = AllReviewsFragment()
            args.putString(BUNDLE_PROFESSIONAL_ID, professionalId)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: BookingsViewModel by viewModels()

    @Inject
    lateinit var mReviewsAdapter: ReviewsAdapter

    private var mProfessionalId = ""
    private var mPage = 0

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun setData(savedInstanceState: Bundle?) {
        // Get arguments
        mProfessionalId = arguments?.getString(BUNDLE_PROFESSIONAL_ID) ?: ""

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_all_reviews)

        binding.recyclerView.setPadding(0, 64, 0, 64)

        // APi call
        mViewModel.getReviews(mProfessionalId, mPage)

    }

    override val recyclerViewAdapter: RecyclerView.Adapter<*>?
        get() = mReviewsAdapter

    override val layoutManager: RecyclerView.LayoutManager?
        get() = LinearLayoutManager(requireContext())

    override val isShowRecyclerViewDivider: Boolean
        get() = false

    override val recyclerView: RecyclerView
        get() = binding.recyclerView

    override val tvNoData: TextView?
        get() = binding.tvNoData

    override val swipeRefreshLayout: SwipeRefreshLayout?
        get() = binding.swipeRefreshLayout

    override fun onPullDownToRefresh() {
        // APi call
        mPage = 0
        mViewModel.getReviews(mProfessionalId, mPage)
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetReviews().observe(this, Observer {
            it?.let { it1 -> mReviewsAdapter.updateData(it1, mPage) }
        })
    }

    override fun onLoadMore() {
        mPage++
        // APi call
        mViewModel.getReviews(mProfessionalId, mPage)
    }

}
