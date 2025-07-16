package com.mentor.application.views.customer.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.R
import com.mentor.application.databinding.BottomsheetWorkersFilterBinding
import com.mentor.application.databinding.FragmentAvailableWorkersBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.User
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.adapters.AvailableWorkersAdapter
import com.mentor.application.views.customer.interfaces.WorkersInterface
import com.swingby.app.views.fragments.base.BaseRecyclerViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AvailableWorkersFragment :
    BaseRecyclerViewFragment<FragmentAvailableWorkersBinding>(FragmentAvailableWorkersBinding::inflate),
    OnClickListener, WorkersInterface {

    companion object {
        const val BUNDLE_PROFESSION_ID = "professionId"
        const val BUNDLE_SUB_PROFESSION_ID = "subProfessionId"
        const val BUNDLE_SERVICE_NAME = "serviceName"

        fun newInstance(
            professionId: String,
            subProfessionId: String,
            serviceName: String
        ): AvailableWorkersFragment {
            val args = Bundle()
            val fragment = AvailableWorkersFragment()
            args.putString(BUNDLE_PROFESSION_ID, professionId)
            args.putString(BUNDLE_SUB_PROFESSION_ID, subProfessionId)
            args.putString(BUNDLE_SERVICE_NAME, serviceName)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: ServicesViewModel by viewModels()

    @Inject
    lateinit var mAvailableWorkersAdapter: AvailableWorkersAdapter

    private var professionId = ""
    private var subProfessionId = ""
    private var professionName = ""
    private var mPage = 0
    private var maxDistance: Float? = null
    private var minPrice: Float? = null
    private var maxPrice: Float? = null
    private var ratting: Float? = null
    private var isFilterApplied=false

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun setData(savedInstanceState: Bundle?) {

        // get arguments
        arguments.let {
            professionName = it?.getString(BUNDLE_SERVICE_NAME) ?: ""
            professionId = it?.getString(BUNDLE_PROFESSION_ID) ?: ""
            subProfessionId = it?.getString(BUNDLE_SUB_PROFESSION_ID) ?: ""
        }

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = professionName

        // Set click listener
        binding.tvFilterBy.setOnClickListener(this)

        // Api call
        mViewModel.getProfessional(
            professionId, subProfessionId, mPage,
            minPrice, maxPrice, maxDistance, ratting
        )

    }

    override val recyclerViewAdapter: RecyclerView.Adapter<*>?
        get() = mAvailableWorkersAdapter

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
        // Api call
        mPage = 0
        mViewModel.getProfessional(
            professionId, subProfessionId, mPage,
            minPrice, maxPrice, maxDistance, ratting
        )

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetProfessional().observe(this, Observer {
            it?.let { it1 -> mAvailableWorkersAdapter.updateData(it1, mPage) }
        })

    }

    private fun showFilterOptionsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TransparentDialog)
        val view = BottomsheetWorkersFilterBinding.inflate(layoutInflater)

        if (!isFilterApplied){
            view.tvReset.visibility=View.INVISIBLE
        }else{
            view.tvReset.visibility=View.VISIBLE
        }

        view.seekPrice.setValues(minPrice ?: 0f, maxPrice ?: 1000f)
        view.seekDistance.setValues(0f, maxDistance ?: 100f)
        view.ratingbar.rating = ratting ?: 0f

        if (!mUserPrefsManager.isLogin) {
            val color = ContextCompat.getColor(requireContext(), R.color.colorDivider)
            view.tvDistanceLabel.setTextColor(color)
            view.tvDistance.setTextColor(color)
            val thumbColorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_pressed), // Pressed state
                    intArrayOf(android.R.attr.state_enabled), // Default enabled state
                ),
                intArrayOf(
                    color,// Thumb color when pressed
                    color
                )
            )
            view.seekDistance.setThumbTintList(thumbColorStateList)
            view.seekDistance.setTrackActiveTintList(ColorStateList.valueOf(color))

            view.seekDistance.isEnabled = false

        } else {
            val colorHeading = ContextCompat.getColor(requireContext(), R.color.colorPrimaryText)
            val colorBar = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            view.tvDistanceLabel.setTextColor(colorHeading)
            view.tvDistance.setTextColor(colorHeading)
            val thumbColorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_pressed), // Pressed state
                    intArrayOf(android.R.attr.state_enabled), // Default enabled state
                ),
                intArrayOf(
                    colorBar,// Thumb color when pressed
                    colorBar
                )
            )
            view.seekDistance.setThumbTintList(thumbColorStateList)
            view.seekDistance.setTrackActiveTintList(ColorStateList.valueOf(colorBar))
            view.seekDistance.isEnabled = true
        }


        if (maxDistance ==null || maxDistance?.toInt() == 100) {
            "0-${maxDistance?.toInt() ?: 100}+km".also { view.tvDistance.text = it }
        } else {
            "0-${maxDistance?.toInt() ?: 100}km".also { view.tvDistance.text = it }

        }

        if (maxPrice==null || maxPrice?.toInt() == 1000) {
            "$${minPrice?.toInt() ?: 0}-${maxPrice?.toInt() ?: 1000}+".also {
                view.tvPrice.text = it
            }
        } else {
            "$${minPrice?.toInt() ?: 0}-${maxPrice?.toInt() ?: 1000}".also { view.tvPrice.text = it }

        }

        view.seekDistance.stepSize = 1f
        view.seekDistance.addOnChangeListener { slider, value, fromUser ->
            // This will be triggered every time the slider's value changes
            val values = slider.values // List<Float> representing the selected range
            maxDistance = values[1]
            if (maxDistance==null || maxDistance?.toInt() == 100) {
                "0-${maxDistance?.toInt()}+km".also { view.tvDistance.text = it }
            } else {
                "0-${maxDistance?.toInt()}km".also { view.tvDistance.text = it }

            }

        }

        view.seekPrice.stepSize = 1f
        view.seekPrice.addOnChangeListener { slider, value, fromUser ->
            // This will be triggered every time the slider's value changes
            val values = slider.values // List<Float> representing the selected range
            maxPrice = values[1]
            minPrice = values[0]

            if (maxPrice==null || maxPrice?.toInt() == 1000) {
                "$${minPrice?.toInt()}-${maxPrice?.toInt()}+".also { view.tvPrice.text = it }
            } else {
                "$${minPrice?.toInt()}-${maxPrice?.toInt()}".also { view.tvPrice.text = it }

            }

        }

        view.tvReset.setOnClickListener {
            // Reset values
            maxDistance = null
            minPrice = null
            maxPrice = null
            ratting = null

            // Api call
            mViewModel.getProfessional(
                professionId, subProfessionId, mPage,
                minPrice, maxPrice, maxDistance, ratting
            )
            isFilterApplied=false
            bottomSheetDialog.dismiss()
        }

        view.btnSubmit.setOnClickListener {
            mPage = 0
            ratting = view.ratingbar.rating
            mViewModel.getProfessional(
                professionId, subProfessionId, mPage,
                minPrice, maxPrice, maxDistance, view.ratingbar.rating
            )
            isFilterApplied=true
            bottomSheetDialog.dismiss()
        }


        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvFilterBy -> {
                showFilterOptionsBottomSheet()
            }

        }
    }

    override fun onItemClick(user: User) {
        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = WorkerDetailFragment.newInstance(user, professionId, subProfessionId),
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }

    override fun onLoadMore() {
        // Api call
        mPage++
        mViewModel.getProfessional(
            professionId, subProfessionId, mPage,
            minPrice, maxPrice, maxDistance, ratting
        )
    }

}
