package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.data.remote.ws.models.BaseModel
import com.squall.doodlekong_android.util.Constants.TYPE_JOIN_ROOM_HANDSHAKE

data class JoinRoomHandshake(
    val username:String,
    val roomName:String,
    val clientId:String
): BaseModel(TYPE_JOIN_ROOM_HANDSHAKE)
