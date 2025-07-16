package com.mentor.application.views.comman.dialgofragments

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mentor.application.databinding.DialogfragmentWebviewBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.viewmodels.comman.BaseViewModel

/**
 * Created by Mukesh on 29/6/18.
 */
class WebViewDialogFragment :
    BaseDialogFragment<DialogfragmentWebviewBinding>(DialogfragmentWebviewBinding::inflate) {

    companion object {
        private const val BUNDLE_EXTRAS_TITLE = "title"
        private const val BUNDLE_EXTRAS_URL = "url"

        fun newInstance(title: String, url: String): WebViewDialogFragment {
            val webViewFragment = WebViewDialogFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_EXTRAS_TITLE, title)
            bundle.putString(BUNDLE_EXTRAS_URL, url)
            webViewFragment.arguments = bundle
            return webViewFragment
        }
    }

    override fun init() {
        // get arguments
        if (null != arguments) {
            binding.toolbar.tvToolbarTitle.text =
                requireArguments().getString(BUNDLE_EXTRAS_TITLE, "")
            binding.webView.loadUrl(requireArguments().getString(BUNDLE_EXTRAS_URL, ""))
        }

        // Enable javascript
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true

        // Set WebView client listener
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                binding.progressBar?.visibility = View.GONE
            }
        }
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    override val isFullScreenDialog: Boolean
        get() = true

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroyView() {
        binding.webView.destroy()
        super.onDestroyView()
    }

    override val toolbar: ToolbarDialogFragmentsBinding
        get() = binding.toolbar
}