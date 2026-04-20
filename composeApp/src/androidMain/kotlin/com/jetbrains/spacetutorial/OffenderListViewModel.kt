package com.jetbrains.spacetutorial

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import kotlinx.coroutines.launch

class OffenderListViewModel(private val sdk: TexasWatchSDK) : ViewModel() {

    private val _state = mutableStateOf(OffenderListState())
    val state: State<OffenderListState> = _state

    init {
        loadOffenders()
    }

    fun loadOffenders(forceReload: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val offenders = sdk.getOffenders(forceReload = forceReload)
                _state.value = _state.value.copy(isLoading = false, offenders = offenders)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }


}

data class OffenderListState(
    val isLoading: Boolean = false,
    val offenders: List<OffenderSummary> = emptyList(),
    val error: String? = null
)
