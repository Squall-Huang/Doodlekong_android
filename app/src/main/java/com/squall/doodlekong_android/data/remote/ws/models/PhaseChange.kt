package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.data.remote.ws.Room
import com.squall.doodlekong_android.util.Constants.TYPE_PHASE_CHANGE

data class PhaseChange(
    var phase: Room.Phase?,
    var time: Long,
    val drawingPlayer: String? = null
): BaseModel(TYPE_PHASE_CHANGE)
