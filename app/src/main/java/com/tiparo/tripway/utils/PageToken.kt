package com.tiparo.tripway.utils

class PageToken(val anchor: String?, val hasMore: Boolean) {
    companion object {
        val FIRST = PageToken(null, true)
    }
}