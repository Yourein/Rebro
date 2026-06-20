package net.yourein.rebro.feature.registertop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.yourein.rebro.interfaces.IsdnRepository
import net.yourein.rebro.interfaces.NdlRepository
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.isdn.IsdnResponse
import net.yourein.rebro.model.ndl.SruResponse

enum class LookupMode { ISDN, ISBN }

class IsdnDebugViewModel(
    private val isdnRepository: IsdnRepository,
    private val ndlRepository: NdlRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isdnResult = MutableStateFlow<IsdnResponse?>(null)
    val isdnResult: StateFlow<IsdnResponse?> = _isdnResult

    private val _isbnResult = MutableStateFlow<SruResponse?>(null)
    val isbnResult: StateFlow<SruResponse?> = _isbnResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _barcodeAutofillResult = MutableStateFlow<AutofillResult?>(null)
    val barcodeAutofillResult: StateFlow<AutofillResult?> = _barcodeAutofillResult.asStateFlow()

    private var autoApplyOnComplete = false
    private var lastFetchedBarcode: String? = null

    fun consumeBarcodeAutofillResult() {
        _barcodeAutofillResult.value = null
    }

    fun buildAutofillResult(): AutofillResult? {
        _isdnResult.value?.let { response ->
            val item = response.item?.firstOrNull() ?: return null
            val authorNames = item.userOptions
                ?.filter { opt ->
                    val prop = opt.property ?: return@filter false
                    prop.contains("著者") || prop.contains("筆者")
                }
                ?.mapNotNull { it.value }
                ?: emptyList()
            return AutofillResult(
                title = item.productName ?: "",
                bookType = BookType.DOUJIN,
                publisher = "",
                authorNames = authorNames,
                circleName = item.publisherName,
                coverImageUrl = item.sampleImageUri?.takeIf { it.isNotBlank() },
            )
        }
        _isbnResult.value?.let { response ->
            val dc = response.records?.record?.firstOrNull()?.recordData?.dc ?: return null
            return AutofillResult(
                title = dc.title ?: "",
                bookType = BookType.COMMERCIAL,
                publisher = dc.publisher ?: "",
                authorNames = dc.creator
                    ?.split(", ")
                    ?.map { it.replace(ndlRoleSuffixPattern, "").trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList(),
                circleName = null,
                coverImageUrl = null,
            )
        }
        return null
    }

    companion object {
        private val ndlRoleSuffixPattern =
            """\s*[\[［]?(?:著|訳|編|監修|原作|絵|画|イラスト|写真|共著|共編|校注|選|述|解説|構成|作)[\]］]?\s*$""".toRegex()
    }

    fun fetchByBarcode(barcode: String) {
        if (barcode == lastFetchedBarcode) return
        val mode = when {
            barcode.startsWith("278") -> LookupMode.ISDN
            barcode.startsWith("978") -> LookupMode.ISBN
            else -> return
        }
        lastFetchedBarcode = barcode
        autoApplyOnComplete = true
        fetch(barcode, mode)
    }

    fun fetch(code: String, mode: LookupMode) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isdnResult.value = null
            _isbnResult.value = null
            try {
                when (mode) {
                    LookupMode.ISDN -> {
                        val response = isdnRepository.getBookInfo(code)
                        if (response != null) {
                            _isdnResult.value = response
                        } else {
                            _error.value = "レスポンスの取得に失敗しました"
                        }
                    }
                    LookupMode.ISBN -> {
                        val response = ndlRepository.searchByIsbn(code)
                        if (response != null) {
                            _isbnResult.value = response
                        } else {
                            _error.value = "レスポンスの取得に失敗しました"
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "不明なエラー"
            } finally {
                _isLoading.value = false
                if (autoApplyOnComplete) {
                    autoApplyOnComplete = false
                    _barcodeAutofillResult.value = buildAutofillResult()
                }
            }
        }
    }
}
