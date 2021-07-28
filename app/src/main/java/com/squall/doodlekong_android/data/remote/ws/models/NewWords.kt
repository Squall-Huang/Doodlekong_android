package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.data.remote.ws.models.BaseModel
import com.squall.doodlekong_android.util.Constants.TYPE_NEW_WORDS

data class NewWords(
    val newWords: List<String>
): BaseModel(TYPE_NEW_WORDS)
