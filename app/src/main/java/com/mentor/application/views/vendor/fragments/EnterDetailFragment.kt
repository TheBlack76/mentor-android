package com.mentor.application.views.vendor.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.anilokcun.uwmediapicker.model.UwMediaPickerMediaModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.R
import com.mentor.application.databinding.BottomsheetProfessionSelectBinding
import com.mentor.application.databinding.FragmentEnterDetailBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.LocationData
import com.mentor.application.repository.models.Profession
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.utils.MarshMallowPermissions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.vendor.PersonalisationViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.ImagePreviewDialogFragment
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.mentor.application.views.customer.fragment.SelectLocationFragment
import com.mentor.application.views.customer.fragment.SelectLocationFragment.Companion.BUNDLE_PROFESSIONAL
import com.mentor.application.views.vendor.adapters.ProfessionCategoryAdapter
import com.mentor.application.views.vendor.adapters.UploadCertificateAdapter
import com.mentor.application.views.vendor.adapters.UploadPastWorkAdapter
import com.mentor.application.views.vendor.fragments.AccountDetailSetupFragment.Companion
import com.mentor.application.views.vendor.interfaces.EnterDetailInterface
import com.sportex.app.appCricket.views.adapter.ProfessionCheckListAdapter
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class EnterDetailFragment :
    BaseFragment<FragmentEnterDetailBinding>(FragmentEnterDetailBinding::inflate), OnClickListener,
    EnterDetailInterface {

    companion object {
        const val INTENT_ENTER_DETAIL = "intentEnterDetail"
        const val INTENT_LOCATION_DATA = "location"
        const val BUNDLE_VIEW_TYPE = "viewType"
        const val BUNDLE_VIEW_TYPE_CREATE = 0
        const val BUNDLE_VIEW_TYPE_EDIT = 1

        fun newInstance(viewType: Int): EnterDetailFragment {
            val args = Bundle()
            val fragment = EnterDetailFragment()
            args.putInt(BUNDLE_VIEW_TYPE, viewType)
            fragment.arguments = args
            return fragment
        }
    }

    private val mMarshMallowPermissions by lazy { MarshMallowPermissions(this) }
    private val mViewModel: PersonalisationViewModel by viewModels()


    @Inject
    lateinit var mUploadCertificateAdapter: UploadCertificateAdapter

    @Inject
    lateinit var mUploadPastWorkAdapter: UploadPastWorkAdapter

    @Inject
    lateinit var mProfessionCategoryAdapter: ProfessionCategoryAdapter

    @Inject
    lateinit var mProfessionCheckListAdapter: ProfessionCheckListAdapter

    private var mViewType = BUNDLE_VIEW_TYPE_CREATE

    private var mProfessionList = mutableListOf<Profession>()

    private var mSelectedProfession = mutableListOf<Profession>()

    private var mCertificateFiles = mutableListOf<String>()
    private var mPastWorkFiles = mutableListOf<String>()

    private var mLocationData = LocationData()

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {
        // Get arguments
        mViewType = arguments?.getInt(BUNDLE_VIEW_TYPE) ?: BUNDLE_VIEW_TYPE_CREATE

        // Set toolbar
        if (mViewType == BUNDLE_VIEW_TYPE_CREATE) {
            binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(), R.color.colorWhite
            )
            binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_black_back)


        } else {
            binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_personalisation)
            binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(), R.color.colorPrimary
            )
            binding.appBarLayout.toolbar.navigationIcon?.setTint(Color.WHITE)
            binding.tvSkip.visibility = View.GONE
            binding.tvHeader.visibility = View.GONE
            binding.tvTagline.visibility = View.GONE
            binding.tvProfession.text = getString(R.string.st_edit_profession)
            binding.tvBio.text = getString(R.string.st_edit_bio)
            binding.btnSubmit.text = getString(R.string.st_save_changes)

        }

        // Set adapter
        binding.recyclerView.adapter = mUploadCertificateAdapter
        binding.rvPastWork.adapter = mUploadPastWorkAdapter
        binding.rvProfession.adapter = mProfessionCategoryAdapter

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.tvSkip.setOnClickListener(this)
        binding.spnrProfession.setOnClickListener(this)
        binding.etLocation.setOnClickListener(this)

        // Api call
        mViewModel.getProfessions()

    }


    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetProfessionalData().observe(this, Observer {
            mProfessionList = it.professionList as MutableList<Profession>
            setDetail()
        })

        mViewModel.onDetailSubmitted().observe(this, Observer {
            if (mViewType == AccountDetailSetupFragment.BUNDLE_VIEW_TYPE_CREATE) {
                requireContext().startActivity(
                    Intent(requireContext(), HomeActivity::class.java)
                )
                (activity as BaseAppCompactActivity<*>).finish()

            } else {
                showMessage(null, getString(R.string.st_detail_submitted_successfully), true)
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
                // Send broadcast
                requireContext().sendBroadcast(Intent(INTENT_PROFILE))
            }
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                mViewModel.addPersonalisation(
                    mSelectedProfession,
                    binding.etExperience.text.toString().trim(),
                    binding.etBio.text.toString().trim(),
                    mCertificateFiles,
                    mPastWorkFiles,
                    mLocationData.lat,
                    mLocationData.lng,
                    mLocationData.name,
                    binding.etHalfHourlyPrice.text.toString().trim(),
                    binding.etHourlyPrice.text.toString().trim(),
                    binding.etOneHalfHourlyPrice.text.toString().trim(),

                    )
            }

            R.id.tvSkip -> {
                requireContext().startActivity(
                    Intent(requireContext(), HomeActivity::class.java)
                )
                (activity as BaseAppCompactActivity<*>).finish()
            }

            R.id.etLocation -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = SelectLocationFragment.newInstance(BUNDLE_PROFESSIONAL),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }

            R.id.spnrProfession -> {
                showMultiChoiceDialog(
                    getString(R.string.st_select_your_profession),
                    mProfessionList,
                    mSelectedProfession,
                ) { selectedList ->

                    mSelectedProfession = selectedList.toMutableList()
                    mProfessionCategoryAdapter.updateData(mSelectedProfession)

                }
            }

        }
    }

    override fun onAddCertificateClick() {
        openFilePicker()
    }

    override fun onAddWorkClick() {
        checkForPermissions()

    }

    override fun onDocumentClick(file: String) {
        GeneralFunctions.openPdfInChromeCustomTab(file, requireContext())
    }

    override fun onWorkClick(file: String) {
        val mImageList = ArrayList<String>()
        mImageList.add(file)
        ImagePreviewDialogFragment.newInstance(mImageList, 0).show(childFragmentManager, "")
    }

    override fun onDeletePastWork(absoluteAdapterPosition: Int) {
        mPastWorkFiles.removeAt(absoluteAdapterPosition)
        mUploadPastWorkAdapter.updateData(mPastWorkFiles)

    }

    override fun onDeleteCertificate(absoluteAdapterPosition: Int) {
        mCertificateFiles.removeAt(absoluteAdapterPosition)
        mUploadCertificateAdapter.updateData(mCertificateFiles)
    }

    private fun setDetail() {
        mUserPrefsManager.loginUser.let { user ->
            user?.professions?.forEach { item ->
                val mData = mProfessionList.find { it._id == item._id }

                mData?.subProfessions?.forEach { subitem ->
                    subitem.isChecked = item.subProfessions?.any { it._id == subitem._id } == true

                }

                mData?.let { mSelectedProfession.add(it) }
            }

            mProfessionCategoryAdapter.updateData(mSelectedProfession)

            binding.etExperience.setText(user?.experience)
            binding.etBio.setText(user?.bio)
            binding.etHalfHourlyPrice.setText(user?.halfHourlyRate)
            binding.etHourlyPrice.setText(user?.hourlyRate)
            binding.etOneHalfHourlyPrice.setText(user?.oneAndHalfHourlyRate)

            mLocationData.lat = user?.loc?.coordinates?.get(0) ?: 0.0
            mLocationData.lng = user?.loc?.coordinates?.get(1) ?: 0.0
            mLocationData.name = user?.location.toString()
            binding.etLocation.text = user?.location.toString()

            mCertificateFiles = user?.certificate as MutableList<String>
            mPastWorkFiles = user?.pastWork as MutableList<String>

            mUploadCertificateAdapter.updateData(mCertificateFiles)
            mUploadPastWorkAdapter.updateData(mPastWorkFiles)

        }

    }


    private fun showMultiChoiceDialog(
        label: String,
        items: List<Profession>,
        selectedItems: List<Profession>,
        onSelectionDone: (List<Profession>) -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TransparentDialog)
        val view = BottomsheetProfessionSelectBinding.inflate(layoutInflater)


        view.tvTitle.text = label

        // Update adapter
        view.recyclerView.adapter = mProfessionCheckListAdapter
        mProfessionCheckListAdapter.updateData(items)

        view.btnApply.setOnClickListener {
            val filteredProfessionList =
                mProfessionCheckListAdapter.getSelectedList().mapNotNull { profession ->
                    val selectedSubs = profession.subProfessions?.filter { it.isChecked }
                    if (!selectedSubs.isNullOrEmpty()) {
                        profession.copy(subProfessions = selectedSubs)
                    } else {
                        null // Remove professions with no selected subProfessions
                    }
                }

            onSelectionDone(filteredProfessionList)
            bottomSheetDialog.dismiss()
        }


        view.btnReset.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view.root)
        // Force bottom sheet to open fully expanded
        view.root.post {
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true // Optional: Prevents going to half state
            }
        }
        bottomSheetDialog.show()
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"  // General filter for files
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // Enable multi-select

        // MIME types for PDF, DOC, DOCX files
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        captureImageResultLauncher.launch(intent)
    }


    private val captureImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null && result.resultCode == RESULT_OK) {
                // Handle multiple selections
                result.data?.let { intentData ->
                    if (intentData.clipData != null) {
                        // Multiple files selected
                        val clipData = intentData.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            // Handle the URI (e.g., upload the file, read its content, etc.)
                            mCertificateFiles.add(
                                GeneralFunctions.getFilePathFromUri(
                                    uri!!, requireContext()
                                )!!
                            )
                        }

                        mUploadCertificateAdapter.updateData(mCertificateFiles)
                        binding.rvProfession.scrollToPosition(mCertificateFiles.size)

                    } else if (intentData.data != null) {
                        // Single file selected
                        val uri = intentData.data
                        mCertificateFiles.add(
                            GeneralFunctions.getFilePathFromUri(
                                uri!!, requireContext()
                            )!!
                        )
                        mUploadCertificateAdapter.updateData(mCertificateFiles)
                        binding.rvProfession.scrollToPosition(mCertificateFiles.size)
                        // Handle the single file URI
                    }
                }
            }
        }


    private fun onMediaSelected(selectedMediaList: List<UwMediaPickerMediaModel>?) {
        selectedMediaList?.forEach { media ->
            // Check if the media is a video
            if (GeneralFunctions.isVideo(media.mediaPath)) {
                // Change the directory of the video file
                val newPath =
                    GeneralFunctions.copyVideoToDirectory(media.mediaPath, requireContext())
                mPastWorkFiles.add(newPath)
            } else {
                // If it's not a video, just add the original path
                mPastWorkFiles.add(media.mediaPath)
            }
        }

        // Update the adapter with the new list of file paths
        mUploadPastWorkAdapter.updateData(mPastWorkFiles)

        // Scroll to the last added item
        binding.rvPastWork.scrollToPosition(mPastWorkFiles.size - 1)
    }

    override fun onResume() {
        super.onResume()
        // Initialize receiver
        requireContext().registerReceiver(
            mGetUpdateDataBroadcastReceiver,
            IntentFilter(INTENT_ENTER_DETAIL),
            Context.RECEIVER_EXPORTED
        )
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    mLocationData =
                        p1?.getParcelableExtra<LocationData>(INTENT_LOCATION_DATA) ?: LocationData()
                    binding.etLocation.setText(mLocationData.name.toString())
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }

    private fun checkForPermissions() {
        // Check if the app has permission to read media access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mMarshMallowPermissions.giveReadMediaImagePermission()
            } else {
                UwMediaPicker.with(this)
                    .setGalleryMode(UwMediaPicker.GalleryMode.ImageAndVideoGallery)
                    .setGridColumnCount(2).setMaxSelectableMediaCount(5).setLightStatusBar(true)
                    .enableImageCompression(true).setCompressFormat(Bitmap.CompressFormat.PNG)
                    .setCompressionQuality(100).setCompressedFileDestinationPath(
                        GeneralFunctions.getOutputDirectory(
                            requireContext()
                        ).absolutePath
                    ).setCancelCallback { }.launch(::onMediaSelected)
            }
        } else {
            if (mMarshMallowPermissions.isPermissionGrantedForWriteExtStorage) {
                UwMediaPicker.with(this)
                    .setGalleryMode(UwMediaPicker.GalleryMode.ImageAndVideoGallery)
                    .setGridColumnCount(2).setMaxSelectableMediaCount(5).setLightStatusBar(true)
                    .enableImageCompression(true).setCompressFormat(Bitmap.CompressFormat.PNG)
                    .setCompressionQuality(100).setCompressedFileDestinationPath(
                        GeneralFunctions.getOutputDirectory(
                            requireContext()
                        ).absolutePath
                    ).setCancelCallback { }.launch(::onMediaSelected)
            } else {
                mMarshMallowPermissions.requestPermissionForWriteExtStorage()
            }
        }
    }


}


