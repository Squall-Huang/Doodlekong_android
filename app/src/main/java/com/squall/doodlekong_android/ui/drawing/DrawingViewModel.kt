package com.squall.doodlekong_android.ui.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.data.remote.ws.DrawingApi
import com.squall.doodlekong_android.data.remote.ws.models.*
import com.squall.doodlekong_android.data.remote.ws.models.DrawAction.Companion.ACTION_UNDO
import com.squall.doodlekong_android.util.DispatcherProvider
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val drawingApi: DrawingApi,
    private val dispatchers: DispatcherProvider,
    private val gson: Gson
) : ViewModel() {

    sealed class SocketEvent {
        data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class GameStateEvent(val data: GameState) : SocketEvent()
        data class DrawDataEvent(val data: DrawData) : SocketEvent()
        data class NewWordsEvent(val data: NewWords) : SocketEvent()
        data class ChosenWordEvent(val data: ChosenWord) : SocketEvent()
        data class GameErrorEvent(val data: GameError) : SocketEvent()
        data class RoundDrawInfoEvent(val data: RoundDrawInfo) : SocketEvent()
        object UndoEvent : SocketEvent()
    }

    private val _chat = MutableStateFlow<List<BaseModel>>(listOf())
    val chat get() = _chat.asStateFlow()

    private val _selectedColorButtonId = MutableStateFlow(R.id.rbBlack)
    val selectedColorButtonId get() = _selectedColorButtonId.asStateFlow()

    private val _connectionProgressBarVisible = MutableStateFlow(true)
    val connectionProgressBarVisible get() = _connectionProgressBarVisible.asStateFlow()

    private val _chooseWordOverlayVisible = MutableStateFlow(false)
    val chooseWordOverlayVisible get() = _chooseWordOverlayVisible.asStateFlow()

    private val connectionEventChannel = Channel<WebSocket.Event>()
    val connectionEvent = connectionEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    private val socketEventChannel = Channel<SocketEvent>()
    val socketEvent = socketEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    init {
        observeBaseModels()
        observeEvents()
    }

    fun setChooseWordOverlayVisibility(isVisible: Boolean): Unit {
        _chooseWordOverlayVisible.value = isVisible
    }

    fun setConnectionProgressBarVisibility(isVisible: Boolean): Unit {
        _connectionProgressBarVisible.value = isVisible
    }

    fun checkRadioButton(id: Int): Unit {
        _selectedColorButtonId.value = id
    }

    private fun observeEvents() {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.observeEvents().collect { event ->
                connectionEventChannel.send(event)
            }
        }
    }

    private fun observeBaseModels() {
        viewModelScope.launch(dispatchers.io){
            drawingApi.observeBaseModels().collect {data ->
                when (data) {
                    is DrawData -> {
                        socketEventChannel.send(SocketEvent.DrawDataEvent(data))
                    }
                    is ChatMessage ->{
                        socketEventChannel.send(SocketEvent.ChatMessageEvent(data))
                    }
                    is Announcement ->{
                        socketEventChannel.send(SocketEvent.AnnouncementEvent(data))
                    }
                    is DrawAction -> {
                        when (data.action) {
                            ACTION_UNDO -> socketEventChannel.send(SocketEvent.UndoEvent)
                        }
                    }
                    is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(data))
                    is Ping -> sendBaseModel(Ping())
                }
            }
        }
    }

    fun sendChatMessage(message: ChatMessage): Unit {
        if (message.message.trim().isEmpty()) {
            return
        }
        viewModelScope.launch(dispatchers.io) {
            drawingApi.sendBaseModel(message)
        }
    }

    fun sendBaseModel(data: BaseModel): Unit {
        viewModelScope.launch(dispatchers.io){
            drawingApi.sendBaseModel(data)
        }
    }
}