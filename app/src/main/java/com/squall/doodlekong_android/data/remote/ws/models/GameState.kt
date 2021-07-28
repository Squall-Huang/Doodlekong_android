package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.data.remote.ws.models.BaseModel
import com.squall.doodlekong_android.util.Constants.TYPE_GAME_STATE

data class GameState(
    val drawingPlayer:String,
    val word:String
): BaseModel(TYPE_GAME_STATE)
