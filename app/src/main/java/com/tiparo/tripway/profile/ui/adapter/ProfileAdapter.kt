package com.tiparo.tripway.profile.ui.adapter

import android.content.Context
import android.content.res.Configuration
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.R
import com.tiparo.tripway.databinding.ItemContentProfileBinding
import com.tiparo.tripway.profile.api.dto.ProfileInfo.Trip
import com.tiparo.tripway.views.common.DataBoundListAdapter
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ProfileAdapter(
    appExecutors: AppExecutors,
    private val tripClickCallback: ((Trip) -> Unit)?
) : DataBoundListAdapter<Trip, ItemContentProfileBinding>(
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

    override fun createBinding(parent: ViewGroup): ItemContentProfileBinding {
        val binding = DataBindingUtil.inflate<ItemContentProfileBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_content_profile,
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

    override fun bind(context: Context, binding: ItemContentProfileBinding, item: Trip) {
        binding.trip = item
        binding.updated.text = item.updated.convertTimestampToRelativeDateTime(context)
        Glide.with(binding.root.context)
            .load(item.photo)
            .placeholder(R.drawable.trip_card_own_placeholder)
            .into(binding.tripImage)
    }

    private fun Timestamp.convertTimestampToRelativeDateTime(context: Context): CharSequence? {
        val simpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            ConfigurationCompat.getLocales(context.resources.configuration)[0]
        )
        return DateUtils.getRelativeDateTimeString(
            context,
            simpleDateFormat.parse(toString())?.time ?: System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS*2,
            DateUtils.FORMAT_ABBREV_RELATIVE

        )
    }
}
