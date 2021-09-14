package fr.afaucogney.mobile.android.flipper.flipperandroidbackstack.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    private val _state = MutableStateFlow("state")
    val state = _state.asStateFlow()

    private val _shared = MutableSharedFlow<String>(5).apply {
        repeat(10) {
            tryEmit("shared:$it")
        }
    }
    val shared = _shared.asSharedFlow()

}