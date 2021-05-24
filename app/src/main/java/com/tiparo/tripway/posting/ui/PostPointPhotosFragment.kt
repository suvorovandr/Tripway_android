package com.tiparo.tripway.posting.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentPostPointPhotosBinding
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import timber.log.Timber
import javax.inject.Inject


class PostPointPhotosFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    private val tripsViewModel: PostPointViewModel by navGraphViewModels(R.id.postPointGraph) {
        viewModelFactory
    }

    private val REQUEST_CODE_CHOOSE = 23
    private val PERMISSIONS_REQUEST_ACCESS_READ_EXTERNAL = 1
    private var mReadExternalStoragePermissionGranted = false

    private lateinit var binding: FragmentPostPointPhotosBinding
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getReadStoragePermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_post_point_photos,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadImagePicker()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            REQUEST_CODE_CHOOSE -> {
                if (resultCode == Activity.RESULT_OK) {
                    tripsViewModel.savePickedPhotos(Matisse.obtainResult(data))
                    findNavController().navigate(R.id.action_post_point_photos_fragment_dest_to_post_point_description_fragment_dest)

                    Timber.d("Uris: %s", Matisse.obtainResult(data))
                    Timber.d("Paths: %s", Matisse.obtainPathResult(data))
                }
            }
        }
    }

    /**
     * Handles the result of the request for read external storage permission.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        mReadExternalStoragePermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_READ_EXTERNAL -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mReadExternalStoragePermissionGranted = true

                    loadImagePicker()
                }
            }
        }
    }

    private fun loadImagePicker() {
        if (mReadExternalStoragePermissionGranted) {
            Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(100)
                .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(GlideEngine())
                .showPreview(false) // Default is `true`
                .forResult(REQUEST_CODE_CHOOSE)
        }
    }

    /**
     * Prompts the user for permission to read external storage.
     */
    private fun getReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mReadExternalStoragePermissionGranted = true
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_ACCESS_READ_EXTERNAL
            )
        }
    }
}
