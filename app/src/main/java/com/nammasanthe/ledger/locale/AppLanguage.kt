package com.nammasanthe.ledger.locale

import androidx.annotation.StringRes
import com.nammasanthe.ledger.R

enum class AppLanguage(val tag: String, @StringRes val nativeLabelRes: Int) {
    ENGLISH("en", R.string.lang_english),
    KANNADA("kn", R.string.lang_kannada),
    HINDI("hi", R.string.lang_hindi),
    TAMIL("ta", R.string.lang_tamil),
    MALAYALAM("ml", R.string.lang_malayalam);

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.find { it.tag == tag } ?: ENGLISH
    }
}
