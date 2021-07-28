package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.data.remote.ws.models.BaseModel
import com.squall.doodlekong_android.util.Constants.TYPE_CHAT_MESSAGE

data class ChatMessage(
    val from: String,
    val roomName: String,
    val message: String,
    val timestamp: Long
) : BaseModel(TYPE_CHAT_MESSAGE)
