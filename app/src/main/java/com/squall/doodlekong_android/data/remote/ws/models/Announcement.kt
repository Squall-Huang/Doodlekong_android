package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.util.Constants.TYPE_ANNOUNCEMENT

data class Announcement(
    val message:String,
    val timestamp:Long,
    val announcement:Int
): BaseModel(TYPE_ANNOUNCEMENT){
    companion object{
        const val TYPE_PLAYER_GUESSED_WORD = 0
        const val TYPE_PLAYER_JOINED = 1
        const val TYPE_PLAYER_LEFT = 2
        const val TYPE_EVERYBODY_GUESSED_IT = 3
    }
}