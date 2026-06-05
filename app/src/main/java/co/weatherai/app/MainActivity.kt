package co.weatherai.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import co.weatherai.app.presentation.ForestryViewModel
import co.weatherai.app.presentation.WeatherScreenType
import co.weatherai.app.presentation.WeatherViewModel
import co.weatherai.app.presentation.ui.ForestryScreen
import co.weatherai.app.presentation.ui.ReportsScreen
import co.weatherai.app.presentation.ui.WeatherScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    private val forestryViewModel: ForestryViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkLocationPermission()

        setContent {
            val weatherState by weatherViewModel.uiState.collectAsState()
            val forestryState by forestryViewModel.uiState.collectAsState()

            when (weatherState.currentScreen) {
                WeatherScreenType.Weather -> {
                    WeatherScreen(
                        state = weatherState,
                        onRefresh = { weatherViewModel.refresh() },
                        onCountySelected = { county -> weatherViewModel.selectCounty(county) },
                        onNavigateToForestry = { weatherViewModel.navigateTo(WeatherScreenType.Forestry) },
                        onNavigateToReports = { weatherViewModel.navigateTo(WeatherScreenType.Reports) },
                        onSearchQueryChange = { weatherViewModel.onSearchQueryChange(it) },
                        onSearch = { weatherViewModel.searchLocation() }
                    )
                }
                WeatherScreenType.Forestry -> {
                    ForestryScreen(
                        state = forestryState,
                        onAnalyze = { file, fId, co, ac, loc, nts -> 
                            forestryViewModel.analyzeImage(
                                imageFile = file,
                                farmerId = fId,
                                county = co,
                                landAcres = ac,
                                location = loc,
                                notes = nts
                            ) 
                        },
                        onMenuClick = { weatherViewModel.navigateTo(WeatherScreenType.Weather) }
                    )
                }
                WeatherScreenType.Reports -> {
                    ReportsScreen(
                        weatherData = weatherState.weatherData,
                        onBack = { weatherViewModel.navigateTo(WeatherScreenType.Weather) }
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already have permission
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }
}
