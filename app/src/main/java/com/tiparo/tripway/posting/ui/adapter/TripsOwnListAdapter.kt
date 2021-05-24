package com.tiparo.tripway.posting.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.TripOwnItemBinding
import com.tiparo.tripway.repository.network.api.services.TripsService.*
import com.tiparo.tripway.views.common.DataBoundListAdapter

class TripsOwnListAdapter(
    appExecutors: AppExecutors,
    private val tripClickCallback: ((Trip) -> Unit)?
) : DataBoundListAdapter<Trip, TripOwnItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(
            oldItem: Trip,
            newItem: Trip
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Trip,
            newItem: Trip
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun createBinding(parent: ViewGroup): TripOwnItemBinding {
        val binding = DataBindingUtil.inflate<TripOwnItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.trip_own_item,
            parent,
            false
        )
        binding.root.setOnClickListener {
            binding.trip?.let {
                tripClickCallback?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(context: Context, binding: TripOwnItemBinding, item: Trip) {
        binding.trip = item
        Glide.with(binding.root.context)
            .load(item.photo)
            .placeholder(R.drawable.trip_card_own_placeholder)
            .into(binding.tripImage)
    }
}
