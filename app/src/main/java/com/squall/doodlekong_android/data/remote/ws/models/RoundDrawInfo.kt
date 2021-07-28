package com.squall.doodlekong_android.data.remote.ws.models

import com.squall.doodlekong_android.util.Constants.TYPE_CUR_ROUND_DRAW_INFO

data class RoundDrawInfo(
    val data:List<String>
): BaseModel(TYPE_CUR_ROUND_DRAW_INFO)
