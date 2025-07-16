package com.swingby.app.views.fragments.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.mentor.application.R
import com.mentor.application.views.comman.utils.DefaultDividerItemDecoration


/**
 * Created by Mukesh on 20/3/18.
 */
abstract class BaseRecyclerViewFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : BaseFragment<VB>(bindingInflater) {

    private val mDividerItemDecoration by lazy {
        DefaultDividerItemDecoration(
            LinearLayoutManager.VERTICAL,
            ContextCompat.getDrawable(
                activityContext,
                R.drawable.drawable_recyclerview_divider
            )!!
        )
    }

    override fun init(savedInstanceState: Bundle?) {
        // Set SwipeRefreshLayout
        if (null != swipeRefreshLayout) {
            swipeRefreshLayout!!.setColorSchemeResources(
                R.color.colorAccent, R.color.colorAccent,
                R.color.colorAccent, R.color.colorAccent
            )
            swipeRefreshLayout!!.setOnRefreshListener { onPullDownToRefresh() }
        }

        // Set RecyclerView
        recyclerView.layoutManager =
            if (null == layoutManager) LinearLayoutManager(activity) else (layoutManager)

        if (isShowRecyclerViewDivider) {
            recyclerView.addItemDecoration(mDividerItemDecoration)
        }

        recyclerView.adapter = recyclerViewAdapter
        setData(savedInstanceState)

        // Observe SwipeRefreshLayout
        viewModel?.isShowSwipeRefreshLayout()?.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                showSwipeRefreshLoader()
            } else {
                hideSwipeRefreshLoader()
            }
        })

        // Observe retrofit errors
        viewModel?.getRetrofitErrorDataMessage()?.observe(viewLifecycleOwner, Observer {
            showNoDataText(it?.errorResId, it?.errorMessage)
        })
    }

    private fun showNoDataText(resId: Int? = null, message: String? = null) {
        if (null == resId && null == message) {
            hideNoDataText()
        } else {
            if (getDefaultAdapterCount() < recyclerViewAdapter?.itemCount.toString().toInt()) {
                showMessage(resId, message, isShowSnackbarMessage = true)
            } else {
                tvNoData?.visibility = View.VISIBLE
                tvNoData?.text = message ?: resId?.let { getString(it) }
            }
        }
    }

    private fun getDefaultAdapterCount(): Int {
        return 1
    }

    private fun hideNoDataText() {
        tvNoData?.visibility = View.GONE
    }

    private fun showSwipeRefreshLoader() {
        swipeRefreshLayout?.post {
            if (null != swipeRefreshLayout) {
                swipeRefreshLayout!!.isRefreshing = true
            }
        }
    }

    private fun hideSwipeRefreshLoader() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (null != swipeRefreshLayout && swipeRefreshLayout!!.isRefreshing) {
                swipeRefreshLayout!!.isRefreshing = false
            }
        }, 50)
    }

    abstract fun setData(savedInstanceState: Bundle?)

    abstract val recyclerViewAdapter: RecyclerView.Adapter<*>?

    abstract val layoutManager: RecyclerView.LayoutManager?

    abstract val isShowRecyclerViewDivider: Boolean

    abstract val recyclerView: RecyclerView

    abstract val tvNoData: TextView?

    abstract val swipeRefreshLayout: SwipeRefreshLayout?

    abstract fun onPullDownToRefresh()

}