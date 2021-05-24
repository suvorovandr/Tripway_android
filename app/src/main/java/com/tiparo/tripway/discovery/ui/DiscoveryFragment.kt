package com.tiparo.tripway.discovery.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.FragmentDiscoveryBinding
import com.tiparo.tripway.discovery.api.dto.DiscoveryInfo
import com.tiparo.tripway.discovery.ui.adapters.DiscoveryAdapter
import com.tiparo.tripway.utils.ErrorBody
import kotlinx.android.synthetic.main.fragment_discovery.*
import javax.inject.Inject

class DiscoveryFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    private val vm: DiscoveryViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var adapter: DiscoveryAdapter

    private lateinit var binding: FragmentDiscoveryBinding

    private val args: DiscoveryFragmentArgs by navArgs()

    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_discovery,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycleView()
        setupSnackbar()

        vm.loadFirstPageIntent()

        vm.uiStateLiveData.observe(viewLifecycleOwner, Observer {state: DiscoveryUiState->
            render(state)
        })
    }

    private fun render(state: DiscoveryUiState) {
        state.fold({ renderLoading() }, { discoveryState -> renderData(discoveryState) }) { error -> renderError(error) }
    }

    private fun renderLoading() {
        loading = true
//        signInProgressBar.visibility = View.VISIBLE
    }

    private fun renderData(data: DiscoveryInfo) {
        loading = false

        signInProgressBar.visibility = View.GONE
        adapter.submitList(data.trips)
    }

    private fun renderError(error: ErrorBody) {
        loading = false

        when (error.type) {
            ErrorBody.ErrorType.NO_CONTENT -> {
                Toast.makeText(context, "К сожалению, ваша лента пока пуста, так как мы ничего не нашли", Toast.LENGTH_LONG).show()
            }
            ErrorBody.ErrorType.NO_INTERNET -> {
                Toast.makeText(context, "Не можем установить соединение с сервером", Toast.LENGTH_LONG).show()
            }
            else -> Toast.makeText(context, "Упсс..Что-то сломалось. Мы скоро все исправим", Toast.LENGTH_LONG).show()
        }
        signInProgressBar.visibility = View.GONE
    }

    private fun initRecycleView() {
        val lm = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.tripsList.layoutManager = lm
        binding.tripsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleItemCount = lm.childCount;
                    val totalItemCount = lm.itemCount;
                    val pastVisibleItems = lm.findFirstVisibleItemPosition();

                    if (!loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = true
                            vm.loadNextPageIntent()
                        }
                    }
                }
            }
        })
        adapter = DiscoveryAdapter(
            appExecutors = appExecutors,
            tripClickCallback = { trip ->
                val direction =
                    DiscoveryFragmentDirections.actionDiscoveryFragmentDestToTripDetailFragment(trip.id)
                findNavController().navigate(direction)
            }
        )
        binding.tripsList.adapter = adapter
//        vm.filteredItems.observe(viewLifecycleOwner) {
//            if (it.isNotEmpty()) {
//                binding.tripsList.visibility = View.VISIBLE
//                binding.emptyViewContent.visibility = View.GONE
//                adapter.submitList(it)
//            } else {
//                binding.tripsList.visibility = View.GONE
//                binding.emptyViewContent.visibility = View.VISIBLE
//            }
//        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as BaseApplication).appComponent.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)

        val searchItem = menu.findItem(R.id.search)
//        setupSearchView(searchItem)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupSnackbar() {
//        view?.setupSnackbar(viewLifecycleOwner, vm.snackbarText, Snackbar.LENGTH_LONG)
//        arguments?.let {
//            if (args.userMessage != 0) {
//                vm.showSnackbarMessage(args.userMessage)
//            }
//        }
    }

//    private fun setupSearchView(searchItem: MenuItem) {
//        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
//                (item?.actionView as SearchView).requestFocus()
//                return true
//            }
//
//            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
//                vm.clearSearchingInfo()
//                return true
//            }
//        })
//
//        val searchView = searchItem.actionView as SearchView
//        searchView.isIconified = false
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                vm.query.value = newText
//                return false
//            }
//        })
//
//    }
}
