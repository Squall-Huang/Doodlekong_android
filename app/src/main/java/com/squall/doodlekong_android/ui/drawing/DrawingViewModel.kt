package com.squall.doodlekong_android.ui.drawing

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.data.remote.ws.DrawingApi
import com.squall.doodlekong_android.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val drawingApi: DrawingApi,
    private val dispatchers: DispatcherProvider,
    private val gson: Gson
) : ViewModel() {


    private val _selectedColorButtonId = MutableStateFlow(R.id.rbBlack)
    val selectedColorButtonId get() = _selectedColorButtonId.asStateFlow()

    fun checkRadioButton(id: Int): Unit {
        _selectedColorButtonId.value = id
    }
}