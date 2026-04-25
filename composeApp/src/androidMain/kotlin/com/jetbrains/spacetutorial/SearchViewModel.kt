package com.jetbrains.spacetutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import com.jetbrains.spacetutorial.texaswatch.entity.TexasCounty
import com.jetbrains.spacetutorial.texaswatch.entity.TEXAS_COUNTIES
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchCriteria(
    val name: String = "",
    val county: TexasCounty? = null,
    val riskLevels: Set<String> = emptySet(),   // "1","2","3"
    val races: Set<String> = emptySet(),         // "W","B","A","I","O"
    val hairColors: Set<String> = emptySet(),    // "BLK","BRO","BLN","RED","GRY","WHI","BAL"
    val eyeColors: Set<String> = emptySet(),     // "BRO","BLU","GRN","HAZ","GRY","BLK","MAR"
) {
    val hasAnyFilter: Boolean
        get() = name.isNotBlank() || county != null ||
                riskLevels.isNotEmpty() || races.isNotEmpty() ||
                hairColors.isNotEmpty() || eyeColors.isNotEmpty()
}

data class SearchState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val results: List<OffenderSummary> = emptyList(),
    val totalResults: Long = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val error: String? = null,
)

class SearchViewModel(private val sdk: TexasWatchSDK) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _criteria = MutableStateFlow(SearchCriteria())
    val criteria: StateFlow<SearchCriteria> = _criteria.asStateFlow()

    private var searchJob: Job? = null
    private var debounceJob: Job? = null

    fun onNameChanged(value: String) {
        _criteria.value = _criteria.value.copy(name = value)
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(400)
            triggerSearch()
        }
    }

    fun onFilterChanged(newCriteria: SearchCriteria) {
        debounceJob?.cancel()
        _criteria.value = newCriteria
        triggerSearch()
    }

    fun clearAllFilters() {
        debounceJob?.cancel()
        _criteria.value = SearchCriteria(name = _criteria.value.name)
        triggerSearch()
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.isLoadingMore || s.isLoading || s.currentPage + 1 >= s.totalPages) return
        runSearch(page = s.currentPage + 1, reset = false)
    }

    private fun triggerSearch() {
        searchJob?.cancel()
        runSearch(page = 0, reset = true)
    }

    private fun runSearch(page: Int, reset: Boolean) {
        val c = _criteria.value
        if (!c.hasAnyFilter) {
            _state.value = SearchState()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (page == 0) _state.value = _state.value.copy(isLoading = true, error = null)
            else _state.value = _state.value.copy(isLoadingMore = true)

            try {
                val response = sdk.searchComprehensive(
                    name = c.name.trim().ifBlank { null },
                    countyName = c.county?.name,
                    riskLevels = c.riskLevels.toList().ifEmpty { null },
                    races = c.races.toList().ifEmpty { null },
                    hairColors = c.hairColors.toList().ifEmpty { null },
                    eyeColors = c.eyeColors.toList().ifEmpty { null },
                    page = page,
                    size = 20,
                )
                val existing = if (reset) emptyList() else _state.value.results
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    results = existing + response.content,
                    totalResults = response.totalElements,
                    currentPage = page,
                    totalPages = response.totalPages,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Search failed",
                )
            }
        }
    }
}
