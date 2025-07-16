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
import com.mentor.application.databinding.FragmentHomeBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Profession
import com.mentor.application.repository.models.SubProfession
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.LoginDialogFragment
import com.mentor.application.views.customer.adapters.ServiceCategoryAdapter
import com.mentor.application.views.customer.fragment.SelectLocationFragment.Companion.BUNDLE_CUSTOMER
import com.mentor.application.views.customer.interfaces.HomeInterface
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment :
    BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate), OnClickListener,
    HomeInterface {

    companion object {
        const val INTENT_HOME = "homeIntent"
        const val INTENT_NOTIFICATION = "notificationIntent"
    }

    private val mViewModel: ServicesViewModel by viewModels()

    private val mOnBoardingViewModel: OnBoardingViewModel by viewModels()

    @Inject
    lateinit var mServiceCategoryAdapter: ServiceCategoryAdapter

    private var mServicesList: MutableList<Profession> = mutableListOf()

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_HOME), Context.RECEIVER_EXPORTED
            )

        requireContext()
            .registerReceiver(
                mGetNotificationCount,
                IntentFilter(INTENT_NOTIFICATION), Context.RECEIVER_EXPORTED
            )

        // Set adapter
        binding.recyclerView.adapter = mServiceCategoryAdapter

        // update data
        setUserData()

        // Set click listener
        binding.tvLocation.setOnClickListener(this)
        binding.ivNotification.setOnClickListener(this)

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
                mServiceCategoryAdapter.updateData(filterProfessionsBySubProfession(s.toString()))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Api call
        mOnBoardingViewModel.getMessageCount()

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetServices().observe(this, Observer {
            mServiceCategoryAdapter.updateData(it.professionList!!, true)
            mServicesList.clear()
            mServicesList.addAll(it.professionList)
        })

        mViewModel.isShowSwipeRefreshLayout().observe(this, Observer {
            binding.swipeRefreshLayout.isRefreshing = it
        })

        mOnBoardingViewModel.onGetNotificationCount().observe(this, Observer {
            if (it > 0) {
                binding.tvNotificationCount.visibility = View.VISIBLE
            } else {
                binding.tvNotificationCount.visibility = View.GONE
            }
        })
    }


    fun setUserData() {
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
        mUserPrefsManager.loginUser?.let {
            binding.sdvUserImg.setImageURI(path)

            binding.tvUserName.text = it.fullName

            // Set location
            if (it.location.isNotBlank()) {
                binding.tvLocation.text = it.location
            } else {
                binding.tvLocation.text = getString(R.string.st_select_your_location)

            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvLocation -> {
                if (mUserPrefsManager.isLogin) {
                    doTransaction(SelectLocationFragment.newInstance(BUNDLE_CUSTOMER))
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }
            }

            R.id.ivNotification -> {
                if (mUserPrefsManager.isLogin) {
                    doTransaction(NotificationFragment())
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }
            }
        }
    }

    override fun onCategoryClick(
        profession: Profession, subProfession: SubProfession
    ) {
        doTransaction(
            AvailableWorkersFragment.newInstance(
                profession._id,
                subProfession._id,
                subProfession.subProfession
            )
        )
    }

    override fun onInstantClick() {
        if (mUserPrefsManager.isLogin) {
            doTransaction(SelectCategoriesFragment())
        } else {
            LoginDialogFragment().show(childFragmentManager, "")
        }
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

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    setUserData()
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

    private val mGetNotificationCount = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    mOnBoardingViewModel.getMessageCount()
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

}
