package com.tiparo.tripway.trippage.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tiparo.tripway.trippage.api.dto.TripPageInfo
import com.tiparo.tripway.trippage.ui.PointFragment
import timber.log.Timber

class TripDetailPointPagerAdapter(
    fragment: Fragment,
    private val points: List<TripPageInfo.Point>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = points.size

    override fun createFragment(position: Int): Fragment {
        Timber.d("FragmentStateAdapter.createFragment() = $position")
        return PointFragment().apply {
                arguments = Bundle().apply {
                    putInt(PointFragment.ARG_POSITION, position)
                    putParcelable(PointFragment.ARG_POINT, points[position])
                }
            }
    }
}