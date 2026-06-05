package co.weatherai.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.weatherai.app.data.model.WeatherResponse
import co.weatherai.app.data.repository.Resource
import co.weatherai.app.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val alertWarning: String? = null,
    val isFromCache: Boolean = false,
    val aiAgronomyAdvice: String = "",
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(
        apiKey: String,
        lat: Double,
        lon: Double,
        units: String = "metric",
        forceRefresh: Boolean = false
    ) {
        if (apiKey.isBlank()) {
            _uiState.value = WeatherUiState(error = "Configure Private Weather-AI app key inside local.properties")
            return
        }

        repository.getWeatherOfflineFirst(
            token = apiKey,
            lat = lat,
            lon = lon,
            units = units,
            forceRefresh = forceRefresh
        ).onEach { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    _uiState.value = WeatherUiState(
                        isLoading = false,
                        weatherData = resource.data?.first,
                        aiAgronomyAdvice = resource.data?.second ?: "",
                        isFromCache = resource.isFromCache,
                        alertWarning = resource.warning
                    )
                }
                is Resource.Error -> {
                    _uiState.value = WeatherUiState(
                        isLoading = false,
                        error = resource.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}
