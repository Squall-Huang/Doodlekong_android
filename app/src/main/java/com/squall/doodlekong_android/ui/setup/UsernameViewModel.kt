package com.squall.doodlekong_android.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squall.doodlekong_android.data.remote.ws.Room
import com.squall.doodlekong_android.repository.SetupRepository
import com.squall.doodlekong_android.util.Constants.MAX_ROOM_NAME_LENGTH
import com.squall.doodlekong_android.util.Constants.MAX_USERNAME_LENGTH
import com.squall.doodlekong_android.util.Constants.MIN_ROOM_NAME_LENGTH
import com.squall.doodlekong_android.util.Constants.MIN_USERNAME_LENGTH
import com.squall.doodlekong_android.util.DispatcherProvider
import com.squall.doodlekong_android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
    private val repository: SetupRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    sealed class SetupEvent {
        object InputEmptyError : SetupEvent()
        object InputTooShortError : SetupEvent()
        object InputTooLongError : SetupEvent()

        data class NavigateToSelectRoomEvent(val username: String) : SetupEvent()

    }


    private val _setupEvent = MutableSharedFlow<SetupEvent>()
    val setupEvent: SharedFlow<SetupEvent> = _setupEvent


    fun validateUsernameAndNavigateToSelectRoom(username: String): Unit {
        viewModelScope.launch(dispatchers.main) {
            val trimmedUsername = username.trim()
            when {
                trimmedUsername.isEmpty() -> {
                    _setupEvent.emit(SetupEvent.InputEmptyError)
                }
                trimmedUsername.length < MIN_USERNAME_LENGTH -> {
                    _setupEvent.emit(SetupEvent.InputTooShortError)
                }
                trimmedUsername.length > MAX_USERNAME_LENGTH -> {
                    _setupEvent.emit(SetupEvent.InputTooLongError)
                }
                else -> _setupEvent.emit(SetupEvent.NavigateToSelectRoomEvent(username))
            }
        }
    }

}