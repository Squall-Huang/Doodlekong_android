package com.squall.doodlekong_android.repository

import com.squall.doodlekong_android.data.remote.responses.BasicApiResponse
import com.squall.doodlekong_android.data.remote.ws.Room
import com.squall.doodlekong_android.util.Resource

interface SetupRepository {

    suspend fun createRoom(room: Room): Resource<Unit>

    suspend fun getRooms(searchQuery: String): Resource<List<Room>>

    suspend fun joinRoom(username:String,roomName:String):Resource<Unit>
}