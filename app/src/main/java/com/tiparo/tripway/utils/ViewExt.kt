package com.tiparo.tripway.utils

/**
 * Extension functions and Binding Adapters.
 */

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

/**
 * Transforms static java function Snackbar.make() to an extension function on View.
 */
fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).run {
        show()
    }
}

/**
 * Triggers a snackbar message when the value contained by snackbarTaskMessageLiveEvent is modified.
 */
fun View.setupSnackbar(
    lifecycleOwner: LifecycleOwner,
    snackbarEvent: LiveData<Event<Int>>,
    timeLength: Int
) {
    snackbarEvent.observe(lifecycleOwner, Observer { event ->
        event.getContentIfNotHandled()?.let {
            showSnackbar(context.getString(it), timeLength)
        }
    })
}

//fun Fragment.setupRefreshLayout(
//    refreshLayout: ScrollChildSwipeRefreshLayout,
//    scrollUpChild: View? = null
//) {
//    refreshLayout.setColorSchemeColors(
//        ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
//        ContextCompat.getColor(requireActivity(), R.color.colorAccent),
//        ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark)
//    )
//    // Set the scrolling view in the custom SwipeRefreshLayout.
//    scrollUpChild?.let {
//        refreshLayout.scrollUpChild = it
//    }
//}
