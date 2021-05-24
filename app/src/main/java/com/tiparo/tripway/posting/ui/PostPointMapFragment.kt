package com.tiparo.tripway.posting.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentPostPointMapBinding
import timber.log.Timber
import javax.inject.Inject


class PostPointMapFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    private val tripsViewModel: PostPointViewModel by navGraphViewModels(R.id.postPointGraph) {
        viewModelFactory
    }

    private lateinit var binding: FragmentPostPointMapBinding

    private var mMap: GoogleMap? = null
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mMarker: Marker? = null

    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted = false

    // A default location (Sydney, Australia) and default zoom to use when location permission is not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM: Float = 17F

    // Keys for storing activity state.
    private val KEY_LOCATION = "location"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("${this.javaClass.name} :onCreate()")

//        // Retrieve location and camera position from saved instance state.
//        savedInstanceState?.let {
//            location = it.getParcelable(KEY_LOCATION) ?: mDefaultLocation
//        }

        Places.initialize(requireActivity().applicationContext, getString(R.string.google_maps_key))
        mPlacesClient = Places.createClient(requireActivity())

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_post_point_map,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripsViewModel.locationName.observe(viewLifecycleOwner) {
            binding.locationNameResult = it
            binding.locationName = it.data
        }

        initAutocompleteMapView()
        initMapView()

        binding.saveLocationBtn.setOnClickListener {
            findNavController().navigate(R.id.action_post_point_map_fragment_dest_to_post_point_photos_fragment_dest)
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
//    override fun onSaveInstanceState(outState: Bundle) {
//        if (mMap != null) {
//            outState.putParcelable(KEY_LOCATION, location)
//        }
//        super.onSaveInstanceState(outState)
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    private fun initMapView() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            mMap = map

            getLocationPermission()

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI()

            // Get the current location of the device and set the position of the map.
            getDeviceLocation()

            //TODO If we dragged our marker, then we need to update our location and show address
            mMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(marker: Marker?) {
                    moveMarkerOnMap(marker?.position ?: mDefaultLocation)
                    tripsViewModel.pickedLocation.value = marker?.position ?: mDefaultLocation
                }

                override fun onMarkerDragStart(marker: Marker?) {}

                override fun onMarkerDrag(marker: Marker?) {}
            })

            mMap?.setOnMapClickListener {
                moveMarkerOnMap(it)
                tripsViewModel.pickedLocation.value = it
            }

            //For onMyLocationButton placing under search bar
            mMap?.setPadding(0, 250, 12, 0)
        }
    }

    private fun initAutocompleteMapView() {
        val autocompletFragment =
            childFragmentManager.findFragmentById(R.id.map_search) as AutocompleteSupportFragment
        autocompletFragment.setPlaceFields(
            arrayListOf(
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.ID,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )
        )

        autocompletFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Timber.d(
                    """Place: ${place.name}, 
                    |${place.id},
                    |${place.address},
                    |${place.addressComponents}"""
                )

                tripsViewModel.pickedPlace.value = place
                moveMarkerOnMap(place.latLng ?: mDefaultLocation)
            }

            override fun onError(status: Status) {
                Timber.i("An error occurred: $status")
            }
        })
        //TODO тут надо сделать restrict области поиска!!!
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true

                    updateLocationUI()

                    // Get the current location of the device and set the position of the map.
                    getDeviceLocation()
                }
            }
        }
    }

    private fun updateLocationUI() {
        mMap?.let { map ->
            try {
                if (mLocationPermissionGranted) {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                } else {
                    map.isMyLocationEnabled = false
                    map.uiSettings.isMyLocationButtonEnabled = false
                    getLocationPermission()
                }
            } catch (e: SecurityException) {
                Timber.e("Exception: %s", e.message ?: "Error while updating location UI")
            }
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->

                    var location = mDefaultLocation

                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        location = task.result?.let { LatLng(it.latitude, it.longitude) }
                            ?: mDefaultLocation
                    } else {
                        Timber.d("Current location is null. Using defaults.")
                        Timber.e("Exception: %s", task.exception?.message)
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }

                    tripsViewModel.pickedLocation.value =
                        task.result?.let {
                            LatLng(it.latitude, it.longitude)
                        } ?: mDefaultLocation

                    mMarker = addMarkerOnMap(location)
                }
            }
        } catch (e: SecurityException) {
            Timber.e("Exception: %s", e.message ?: "Error while getting device location")
        }
    }

    private fun addMarkerOnMap(newPosition: LatLng): Marker? {
        val marker = mMap?.addMarker(
            MarkerOptions()
                .position(newPosition)
                .draggable(true)
        )
        moveCamera(newPosition)
        return marker
    }

    private fun moveMarkerOnMap(newPosition: LatLng) {
        mMarker?.position = newPosition
        moveCamera(newPosition)
    }

    private fun moveCamera(newPosition: LatLng) {
        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                newPosition, DEFAULT_ZOOM
            )
        )
    }
}
