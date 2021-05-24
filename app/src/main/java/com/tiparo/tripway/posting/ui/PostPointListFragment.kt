package com.tiparo.tripway.posting.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentPostPointListBinding
import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.setupSnackbar
import com.tiparo.tripway.posting.ui.adapter.TripsOwnListAdapter
import timber.log.Timber
import javax.inject.Inject

class PostPointListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    private lateinit var adapter: TripsOwnListAdapter

    private val viewModel: PostPointViewModel by navGraphViewModels(R.id.postPointGraph) {
        viewModelFactory
    }

    private lateinit var binding: FragmentPostPointListBinding
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("${this.javaClass.name} :onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_post_point_list,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setupNavigation()
        setupSnackbar()
        initRecycleView()

        viewModel.loadTrips()

        viewModel.trips.observe(viewLifecycleOwner, Observer { state ->
            render(state)
        })
    }

    private fun render(state: OwnTripsListUiState) {
        state.fold({ renderLoading() }, { tripsState -> renderData(tripsState) }) { error -> renderError(error) }
    }

    private fun renderLoading() {
    }

    private fun renderData(data: List<TripsService.Trip>) {
        adapter.submitList(data)
    }

    private fun renderError(error: ErrorBody) {
        when (error.type) {
            ErrorBody.ErrorType.NO_INTERNET -> {
                Toast.makeText(context, "Не можем установить соединение с сервером", Toast.LENGTH_LONG).show()
            }
            else -> Toast.makeText(
                context,
                "Неизвестная ошибка, но мы скоро все исправим",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    private fun setupNavigation() {
        binding.addNewTrip.setOnClickListener {
            viewModel.selectTripToPost(null)
            findNavController().navigate(R.id.action_post_point_list_fragment_dest_to_post_point_map_fragment_dest)
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_LONG)
    }

    private fun initRecycleView() {
        adapter = TripsOwnListAdapter(
            appExecutors = appExecutors,
            tripClickCallback = { trip ->
                viewModel.selectTripToPost(trip)
                findNavController().navigate(R.id.action_post_point_list_fragment_dest_to_post_point_map_fragment_dest)
            })

        binding.postTripsList.adapter = adapter
    }
}
