package co.weatherai.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.weatherai.app.data.model.AgroforestryResponse
import co.weatherai.app.data.repository.Resource
import co.weatherai.app.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ForestryUiState(
    val isLoading: Boolean = false,
    val analysisResult: AgroforestryResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ForestryViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForestryUiState())
    val uiState: StateFlow<ForestryUiState> = _uiState.asStateFlow()

    fun analyzeImage(
        imageFile: File,
        farmerId: String? = null,
        county: String? = null,
        landAcres: Double? = null,
        location: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, analysisResult = null)
            val result = repository.analyzeForestry(
                imageFile = imageFile,
                farmerId = farmerId,
                county = county,
                landAcres = landAcres,
                location = location,
                notes = notes
            )
            
            when (result) {
                is Resource.Success -> {
                    _uiState.value = ForestryUiState(isLoading = false, analysisResult = result.data)
                }
                is Resource.Error -> {
                    _uiState.value = ForestryUiState(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }
}
