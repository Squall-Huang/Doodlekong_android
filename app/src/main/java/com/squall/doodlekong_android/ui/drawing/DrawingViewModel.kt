package com.squall.doodlekong_android.ui.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.data.remote.ws.DrawingApi
import com.squall.doodlekong_android.data.remote.ws.Room
import com.squall.doodlekong_android.data.remote.ws.models.*
import com.squall.doodlekong_android.data.remote.ws.models.DrawAction.Companion.ACTION_UNDO
import com.squall.doodlekong_android.ui.views.DrawingView
import com.squall.doodlekong_android.util.Constants.TYPE_DRAW_ACTION
import com.squall.doodlekong_android.util.Constants.TYPE_DRAW_DATA
import com.squall.doodlekong_android.util.CoroutineTimer
import com.squall.doodlekong_android.util.DispatcherProvider
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
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
        data class RoundDrawInfoEvent(val data: List<BaseModel>) : SocketEvent()
        object UndoEvent : SocketEvent()
    }


    private val _pathData = MutableStateFlow(Stack<DrawingView.PathData>())
    val pathData get() = _pathData.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerData>>(listOf())
    val players get() = _players.asStateFlow()

    private val _newWords = MutableStateFlow(NewWords(listOf()))
    val newWords get() = _newWords.asStateFlow()

    private val _phase = MutableStateFlow(PhaseChange(null,0L,null))
    val phase get() = _phase.asStateFlow()

    private val _phaseTime = MutableStateFlow(0L)
    val phaseTime get() = _phaseTime.asStateFlow()

    private val _gameState = MutableStateFlow(GameState("",""))
    val gameState get() = _gameState.asStateFlow()

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

    private val timer = CoroutineTimer()

    private var timerJob: Job? = null

    init {
        observeBaseModels()
        observeEvents()
    }

    private fun setTimer(duration: Long): Unit {
        timerJob?.cancel()
        timerJob = timer.timeAndEmit(duration, viewModelScope) {
            _phaseTime.value = it
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
    }

    fun setPathData(stack: Stack<DrawingView.PathData>): Unit {
        _pathData.value = stack
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
                    is ChatMessage -> {
                        socketEventChannel.send(SocketEvent.ChatMessageEvent(data))
                    }
                    is ChosenWord -> {
                        socketEventChannel.send(SocketEvent.ChosenWordEvent(data))
                    }
                    is RoundDrawInfo -> {
                        val drawActions = mutableListOf<BaseModel>()
                        data.data.forEach { drawAction ->
                            val jsonObject = JsonParser.parseString(drawAction).asJsonObject
                            val type = when (jsonObject.get("type").asString) {
                                TYPE_DRAW_DATA -> DrawData::class.java
                                TYPE_DRAW_ACTION -> DrawAction::class.java
                                else -> BaseModel::class.java
                            }
                            drawActions.add(gson.fromJson(drawAction,type))
                        }
                        socketEventChannel.send(SocketEvent.RoundDrawInfoEvent(drawActions))
                    }
                    is Announcement -> {
                        socketEventChannel.send(SocketEvent.AnnouncementEvent(data))
                    }
                    is GameState -> {
                        _gameState.value = data
                        socketEventChannel.send(SocketEvent.GameStateEvent(data))
                    }
                    is PlayersList -> {
                        _players.value = data.players
                    }
                    is NewWords -> {
                        _newWords.value = data
                        socketEventChannel.send(SocketEvent.NewWordsEvent(data))
                    }
                    is DrawAction -> {
                        when (data.action) {
                            ACTION_UNDO -> socketEventChannel.send(SocketEvent.UndoEvent)
                        }
                    }
                    is PhaseChange -> {
                        data.phase?.let {
                            _phase.value = data
                        }
                        _phaseTime.value = data.time
                        if (data.phase != Room.Phase.WAITING_FOR_PLAYERS) {
                            setTimer(data.time)
                        }
                    }
                    is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(data))
                    is Ping -> sendBaseModel(Ping())
                }
            }
        }
    }

    fun disconnect() {
        sendBaseModel(DisconnectRequest())
    }

    fun chooseWord(word: String, roomName: String): Unit {
        val chosenWord = ChosenWord(word, roomName)
        sendBaseModel(chosenWord)
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