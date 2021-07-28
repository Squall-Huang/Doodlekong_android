package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.util.Constants.TYPE_PLAYERS_LIST

data class PlayersList(
    val players: List<PlayerData>
): BaseModel(TYPE_PLAYERS_LIST)
