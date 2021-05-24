package com.tiparo.tripway.profile.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentProfilePageBinding
import com.tiparo.tripway.profile.api.dto.ProfileInfo
import com.tiparo.tripway.profile.ui.adapter.ProfileAdapter
import com.tiparo.tripway.utils.ErrorBody
import kotlinx.android.synthetic.main.fragment_profile_page.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class ProfileFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    private lateinit var binding: FragmentProfilePageBinding
    private lateinit var adapter: ProfileAdapter
    private val vm: ProfileViewModel by viewModels {
        viewModelFactory
    }

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(ARG_USER_ID)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_page, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycleView()

        vm.loadProfile(userId)

        vm.uiStateLiveData.observe(viewLifecycleOwner, Observer {
            render(it)
        })
    }

    //todo в теории это все можно обернуть и предоставить фрагментам интерфейс (аля обертка для MVI)
    private fun render(state: ProfileUiState) {
        state.fold({ renderLoading() }, { data -> renderData(data) }) { error -> renderError(error) }
    }

    private fun renderLoading() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun renderData(data: ProfileInfo) {
        progress_bar.visibility = View.GONE

        nickname.text = data.nickname
        trips_count.text = data.trips.size.toString()
        subscribersCount.text = data.subscribersCount.toString()
        subscriptionsCount.text = data.subscriptionsCount.toString()
        //todo avatar.setImageURI(data.avatar)
        adapter.submitList(data.trips)
    }

    private fun renderError(error: ErrorBody) {
        progress_bar.visibility = View.GONE
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

    private fun initRecycleView() {
        val lm = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
        binding.trips.layoutManager = lm
        adapter = ProfileAdapter(
            appExecutors = appExecutors,
            tripClickCallback = { trip ->
//                val direction =
//                    HomeFragmentDirections.actionHomeFragmentDestToTripDetailFragment(trip.id)
//                findNavController().navigate(direction)
            }
        )
        binding.trips.adapter = adapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    companion object{
        const val ARG_USER_ID = "user_id"
    }
}