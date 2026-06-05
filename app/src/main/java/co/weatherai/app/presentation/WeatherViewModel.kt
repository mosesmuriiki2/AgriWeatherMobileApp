package co.weatherai.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.weatherai.app.data.model.UsageResponse
import co.weatherai.app.data.model.WeatherResponse
import co.weatherai.app.data.repository.Resource
import co.weatherai.app.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class County(val name: String, val lat: Double, val lon: Double)

enum class WeatherScreenType { Weather, Forestry, Reports }

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val usageData: UsageResponse? = null,
    val alertWarning: String? = null,
    val isFromCache: Boolean = false,
    val error: String? = null,
    val selectedCounty: County? = null,
    val searchQuery: String = "",
    val locationName: String = "Detecting...",
    val currentScreen: WeatherScreenType = WeatherScreenType.Weather
) {
    companion object {
        val counties = listOf(
            County("Nairobi", -1.2921, 36.8219),
            County("Mombasa", -4.0435, 39.6682),
            County("Kisumu", -0.0917, 34.7680),
            County("Nakuru", -0.3031, 36.0800),
            County("Eldoret", 0.5143, 35.2697)
        )
    }
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var weatherJob: Job? = null

    init {
        detectLocationAndLoad()
        loadUsage()
    }

    private fun detectLocationAndLoad() {
        _uiState.value = _uiState.value.copy(selectedCounty = null)
        weatherJob?.cancel()
        weatherJob = repository.getWeatherByIp().onEach { resource ->
            handleResource(resource)
            if (resource is Resource.Success && !resource.isFromCache) {
                loadUsage()
            }
        }.launchIn(viewModelScope)
    }

    fun navigateTo(screen: WeatherScreenType) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun selectCounty(county: County) {
        _uiState.value = _uiState.value.copy(
            selectedCounty = county, 
            searchQuery = "",
            locationName = county.name, // Update Top Bar immediately
            weatherData = null // Clear old data to trigger loading state
        )
        loadWeather(lat = county.lat, lon = county.lon, forceRefresh = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun searchLocation() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        _uiState.value = _uiState.value.copy(
            selectedCounty = null,
            locationName = "Searching: $query...",
            weatherData = null
        )
        
        weatherJob?.cancel()
        weatherJob = repository.searchWeather(query).onEach { resource ->
            handleResource(resource)
            if (resource is Resource.Success && !resource.isFromCache) {
                loadUsage()
            }
        }.launchIn(viewModelScope)
    }

    private fun loadWeather(lat: Double, lon: Double, forceRefresh: Boolean) {
        weatherJob?.cancel()
        weatherJob = repository.getWeatherOfflineFirst(
            lat = lat,
            lon = lon,
            units = "metric",
            forceRefresh = forceRefresh
        ).onEach { resource ->
            handleResource(resource)
        }.launchIn(viewModelScope)
    }

    private fun handleResource(resource: Resource<WeatherResponse>) {
        when (resource) {
            is Resource.Loading -> {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            is Resource.Success -> {
                val serverLocationName = resource.data?.location?.timezone?.split("/")?.last()?.replace("_", " ")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weatherData = resource.data,
                    isFromCache = resource.isFromCache,
                    alertWarning = resource.warning,
                    locationName = serverLocationName ?: _uiState.value.locationName
                )
                // If it was a fresh network fetch, refresh usage
                if (!resource.isFromCache) {
                    loadUsage()
                }
            }
            is Resource.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = resource.message
                )
            }
        }
    }

    fun loadUsage() {
        viewModelScope.launch {
            val resource = repository.getUsage()
            if (resource is Resource.Success) {
                _uiState.value = _uiState.value.copy(usageData = resource.data)
            }
        }
    }

    fun refresh() {
        val weatherData = _uiState.value.weatherData
        if (weatherData != null) {
            loadWeather(
                lat = weatherData.location.lat,
                lon = weatherData.location.lon,
                forceRefresh = true
            )
        } else {
            val currentCounty = _uiState.value.selectedCounty
            if (currentCounty != null) {
                loadWeather(currentCounty.lat, currentCounty.lon, forceRefresh = true)
            } else {
                detectLocationAndLoad()
            }
        }
        loadUsage()
    }
}
