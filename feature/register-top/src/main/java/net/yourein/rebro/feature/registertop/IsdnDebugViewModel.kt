package net.yourein.rebro.feature.registertop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.yourein.rebro.interfaces.IsdnRepository
import net.yourein.rebro.model.isdn.IsdnResponse

class IsdnDebugViewModel(
    private val isdnRepository: IsdnRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _result = MutableStateFlow<IsdnResponse?>(null)
    val result: StateFlow<IsdnResponse?> = _result

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetch(isdn: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _result.value = null
            try {
                val response = isdnRepository.getBookInfo(isdn)
                if (response != null) {
                    _result.value = response
                } else {
                    _error.value = "レスポンスの取得に失敗しました"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "不明なエラー"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
