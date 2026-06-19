package net.yourein.rebro.feature.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.interfaces.SeriesRepository
import net.yourein.rebro.model.entity.Series

class SeriesViewModel(
    seriesRepository: SeriesRepository,
) : ViewModel() {
    val seriesState: StateFlow<LoadingState<List<Series>>> = seriesRepository
        .getSeries()
        .map<List<Series>, LoadingState<List<Series>>> {
            LoadingState.Success(it)
        }
        .catch { e -> emit(LoadingState.Error(null, e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Loading(null),
        )
}
