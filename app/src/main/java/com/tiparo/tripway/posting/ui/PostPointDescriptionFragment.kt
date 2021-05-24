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
import com.tiparo.tripway.databinding.FragmentPostPointDescriptionBinding
import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.Event
import com.tiparo.tripway.utils.setupSnackbar
import kotlinx.android.synthetic.main.fragment_post_point_description.*
import javax.inject.Inject


class PostPointDescriptionFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    private val viewModel: PostPointViewModel by navGraphViewModels(R.id.postPointGraph) {
        viewModelFactory
    }

    private lateinit var binding: FragmentPostPointDescriptionBinding
    // This property is only valid between onCreateView and
    // onDestroyView.

    val KEY_DESCRIPTION = "describePointEditText"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_post_point_description,
            container,
            false
        )
        binding.viewmodel = viewModel

        // Set the lifecycle owner to the lifecycle of the view
        binding.lifecycleOwner = this.viewLifecycleOwner

        savedInstanceState?.let {
            binding.describePointEditText.setText(it.getString(KEY_DESCRIPTION, ""))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSnackbar()

        publishPointButton.setOnClickListener {
            //работает DataBinding
            val description = viewModel.description.value
            val tripName = viewModel.tripName.value
            if (tripName.isNullOrBlank() && viewModel.isNewPoint) {
                showSnackbarMessage(R.string.snackbar_empty_trip_name_post_message)
                return@setOnClickListener
            }
            viewModel.savePoint(description, tripName)
        }

        viewModel.pointSaved.observe(viewLifecycleOwner, Observer {
            render(it)
        })
    }

    private fun render(state: PostPointUiState) {
        state.fold(
            { renderLoading() },
            { data -> renderData(data) }) { error -> renderError(error) }
    }

    private fun renderLoading() {
        showSnackbarMessage(R.string.snackbar_post_point_saving)
        //todo сделать loading signInProgressBar.visibility = View.VISIBLE
    }

    private fun renderData(data: TripsService.PointPostResult) {
        //todo тут по хорошему нужно не заставлять ждать юзера, а кидать его дальше в Home с последующим обновлением о состоянии публикации
        //но тут могут быть утечки памяти, осторожно!!!
        showSnackbarMessage(R.string.snackbar_post_poin_saved)
        //todo !!!сделать loading пока что!!!

        val direction =
            PostPointDescriptionFragmentDirections.actionPostPointDescriptionFragmentDestToDiscoveryFragmentDest(R.string.snackbar_post_point_saving)
        findNavController().navigate(direction)
    }

    private fun renderError(error: ErrorBody) {
        when (error.type) {
            ErrorBody.ErrorType.NO_INTERNET -> {
                Toast.makeText(context, "Не можем установить соединение с сервером", Toast.LENGTH_LONG).show()
            }
            else -> Toast.makeText(
                context,
                "Упсс..Что-то сломалось. Мы скоро все исправим",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_DESCRIPTION, binding.describePointEditText.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun showSnackbarMessage(messageResource: Int) {
        viewModel.snackbarText.value = Event(messageResource)
    }
}