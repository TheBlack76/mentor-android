package com.mentor.application.views.customer.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentBaseRecyclerViewBinding
import com.mentor.application.databinding.FragmentHomeBinding
import com.mentor.application.databinding.FragmentSelectLocationBinding
import com.mentor.application.databinding.SelectCategoryFragmentBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Profession
import com.mentor.application.repository.models.SubProfession
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.LoginDialogFragment
import com.mentor.application.views.customer.adapters.ServiceCategoryAdapter
import com.mentor.application.views.customer.fragment.SelectLocationFragment.Companion.BUNDLE_CUSTOMER
import com.mentor.application.views.customer.interfaces.HomeInterface
import com.swingby.app.views.fragments.base.BaseFragment
import com.swingby.app.views.fragments.base.BaseRecyclerViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class SelectCategoriesFragment :
    BaseFragment<SelectCategoryFragmentBinding>(SelectCategoryFragmentBinding::inflate),
    HomeInterface {

    private val mViewModel: ServicesViewModel by viewModels()

    @Inject
    lateinit var mServiceCategoryAdapter: ServiceCategoryAdapter

    private var mServicesList: MutableList<Profession> = mutableListOf()

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_select_categories)

        // Set adapter
        binding.recyclerView.adapter = mServiceCategoryAdapter
        binding.recyclerView.setPadding(0, 48, 0, 8)

        // Api call
        mViewModel.getServices()

        // Set swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Api call
            mViewModel.getServices()
        }

        // Add TextWatcher to filter the list based on input
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mServiceCategoryAdapter.updateData(filterProfessionsBySubProfession(s.toString()),false)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetServices().observe(this, Observer {
            mServiceCategoryAdapter.updateData(it.professionList!!, false)
            mServicesList.clear()
            mServicesList.addAll(it.professionList)
        })

        mViewModel.isShowSwipeRefreshLayout().observe(this, Observer {
            binding.swipeRefreshLayout.isRefreshing = it
        })
    }

    override fun onCategoryClick(
        profession: Profession, subProfession: SubProfession
    ) {
        doTransaction(
            InstantBookingDurationFragment.newInstance(
                profession,
                subProfession
            )
        )
    }

    override fun onInstantClick() {

    }

    private fun filterProfessionsBySubProfession(query: String): List<Profession> {
        val lowerQuery = query.lowercase().take(6)

        return mServicesList.mapNotNull { profession ->
            val professionMatches = profession.profession.lowercase().contains(lowerQuery)

            val filteredSubProfessions = profession.subProfessions?.filter { subProfession ->
                subProfession.subProfession.lowercase().contains(lowerQuery)
            }

            when {
                professionMatches -> {
                    // If profession name matches, return all subProfessions
                    profession.copy(subProfessions = profession.subProfessions)
                }

                !filteredSubProfessions.isNullOrEmpty() -> {
                    // If some subProfessions match, return only those
                    profession.copy(subProfessions = filteredSubProfessions)
                }

                else -> null
            }
        }
    }


    private fun doTransaction(fragment: Fragment) {
        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = fragment,
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }
}
