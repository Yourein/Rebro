package net.yourein.rebro.feature.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.model.entity.Circle

class CirclesViewModel(
    circleRepository: CircleRepository,
) : ViewModel() {
    val circlesState: StateFlow<LoadingState<List<Circle>>> = circleRepository
        .getCircles()
        .map<List<Circle>, LoadingState<List<Circle>>> {
            LoadingState.Success(it)
        }
        .catch { e -> emit(LoadingState.Error(null, e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Loading(null),
        )
}
