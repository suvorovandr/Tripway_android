package com.tiparo.tripway.trippage.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentDetailTripBinding
import com.tiparo.tripway.trippage.ui.adapter.TripDetailPointPagerAdapter
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.setupSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_profile_page.*
import timber.log.Timber
import javax.inject.Inject

class TripDetailFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val vm: TripDetailViewModel by viewModels {
        viewModelFactory
    }

    private val args: TripDetailFragmentArgs by navArgs()

    private var mMap: GoogleMap? = null
    private val DEFAULT_ZOOM: Float = 12F
    private val WORLD_ZOOM: Float = 2F

    private lateinit var binding: FragmentDetailTripBinding
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        vm.loadTripPage(args.tripId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail_trip,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupMapView()
        setupSnackbar()
//        setupNavigation()

        vm.uiStateLiveData.observe(viewLifecycleOwner, Observer {
            render(it)
        })
    }

    private fun render(state: TripDetailUiState) {
        state.fold({ renderLoading() }, { data -> renderData(data) }) { error -> renderError(error) }
    }

    private fun renderLoading() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun renderData(data: TripDetailUiState.Data) {
        progress_bar.visibility = View.GONE

        requireActivity().toolbar.title = data.tripRoute

        moveCamera(data.locations.first(), WORLD_ZOOM)
        data.locations.forEach {
            addMarkerOnMap(it)
        }
        binding.stepProgressBar.setStepCount(data.points.size)
        binding.stepProgressBar.setOnStepChangedListener { stepIndex ->
            binding.pointPager.currentItem = stepIndex
        }
        binding.pointPager.adapter =
            TripDetailPointPagerAdapter(
                this,
                data.points
            )
        binding.pointPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //TODO сохранять текущую позицию viewpager для savedInstanceState
                val point = data.points[position]
                moveCamera(LatLng(point.lat, point.lng))
                binding.stepProgressBar.updateStep(position)

                Timber.d("ViewPager: position = $position")
            }
        })
    }

    private fun renderError(error: ErrorBody) {
        progress_bar.visibility = View.GONE
        when (error.type) {
            ErrorBody.ErrorType.NO_INTERNET -> {
                Toast.makeText(
                    context,
                    "Не можем установить соединение с сервером",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            else -> Toast.makeText(
                context,
                "Неизвестная ошибка, но мы скоро все исправим",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.trip_detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deleteTrip -> {
                vm.deleteTrip()
                true
            }
            R.id.deletePoint -> {
                val pointPosition = binding.pointPager.currentItem
                vm.deletePoint(pointPosition)
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    private fun setupMapView() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            mMap = map
        }
        val layoutParams = binding.mapViewToolbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
        layoutParams.behavior = behavior
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(viewLifecycleOwner, vm.snackbarText, Snackbar.LENGTH_LONG)
    }

//    private fun setupNavigation() {
//        vm.deletedEvent.observe(viewLifecycleOwner) {
//            findNavController().popBackStack()
//        }
//    }

    private fun moveCamera(newPosition: LatLng, zoom: Float = DEFAULT_ZOOM) {
        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                newPosition, zoom
            )
        )
    }

    private fun addMarkerOnMap(newPosition: LatLng) {
        mMap?.addMarker(
            MarkerOptions()
                .position(newPosition)
                .draggable(false)
        )
    }
}
