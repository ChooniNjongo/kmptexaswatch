package com.jetbrains.spacetutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OffenderDetailState {
    data object Loading : OffenderDetailState
    data class Success(val detail: OffenderDetail) : OffenderDetailState
    data class Error(val message: String) : OffenderDetailState
}

class OffenderDetailViewModel(
    private val sdk: TexasWatchSDK,
    private val indIdn: Int,
) : ViewModel() {

    private val _state = MutableStateFlow<OffenderDetailState>(OffenderDetailState.Loading)
    val state: StateFlow<OffenderDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = OffenderDetailState.Loading
            try {
                val detail = sdk.getOffenderDetail(indIdn)
                _state.value = OffenderDetailState.Success(detail)
            } catch (e: Exception) {
                _state.value = OffenderDetailState.Error(e.message ?: "Failed to load offender")
            }
        }
    }
}
