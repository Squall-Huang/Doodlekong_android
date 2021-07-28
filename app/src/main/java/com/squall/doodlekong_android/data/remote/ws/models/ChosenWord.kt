package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.data.remote.ws.models.BaseModel
import com.squall.doodlekong_android.util.Constants.TYPE_CHOSEN_WORD

data class ChosenWord(
    val chosenWord: String,
    val roomName:String
): BaseModel(TYPE_CHOSEN_WORD)
