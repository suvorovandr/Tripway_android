package com.tiparo.tripway.utils

import android.os.Build
import android.os.LocaleList
import java.util.*

object LocaleUtil {
    fun getLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault().toLanguageTags()
        } else {
            Locale.getDefault().language
        }
    }
}