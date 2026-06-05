package co.weatherai.app.presentation.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.weatherai.app.presentation.County
import co.weatherai.app.presentation.WeatherUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    onRefresh: () -> Unit,
    onCountySelected: (County) -> Unit,
    onNavigateToForestry: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "RefreshRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F172A),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "WeatherAI Pro",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Search Field in Drawer
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search location...", color = Color(0xFF64748B), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = onSearch) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Search", tint = Color(0xFF6366F1))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF334155),
                        cursorColor = Color(0xFF6366F1),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))
                
                NavigationDrawerItem(
                    label = { Text("Weather Forecast", color = Color.White) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White) },
                    colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Color(0xFF1E293B))
                )

                NavigationDrawerItem(
                    label = { Text("Agroforestry AI", color = Color.White) },
                    selected = false,
                    onClick = { 
                        onNavigateToForestry()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Nature, contentDescription = null, tint = Color.White) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                NavigationDrawerItem(
                    label = { Text("Analytics & Reports", color = Color.White) },
                    selected = false,
                    onClick = { 
                        onNavigateToReports()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Counties",
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                WeatherUiState.counties.forEach { county ->
                    NavigationDrawerItem(
                        label = { Text(county.name, color = Color.White) },
                        selected = state.selectedCounty == county,
                        onClick = {
                            onCountySelected(county)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.weight(1f))
                UsagePanel(state)
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                state.locationName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                if (state.isLoading) "Updating data..." else if (state.isFromCache) "Cached Forecast" else "Real-time Forecast",
                                color = if (state.isLoading) Color.Yellow else if (state.isFromCache) Color(0xFF6366F1) else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White,
                                modifier = Modifier.rotate(if (state.isLoading) rotation else 0f)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))))
            ) {
                // Main Content
                state.weatherData?.let { weather ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MainWeatherCard(weather)
                        }

                        item {
                            Text("Hourly Forecast", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        item {
                            val currentHourItems = remember(weather.hourly, weather.current.time) {
                                weather.hourly.dropWhile { it.time < weather.current.time }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(currentHourItems) { hour ->
                                    HourlyCard(hour)
                                }
                            }
                        }

                        item {
                            Text("7-Day Forecast", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        items(weather.daily) { day ->
                            ForecastRow(day)
                        }
                    }
                }

                // Loading Overlay
                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                }
            }
        }
    }
}

@Composable
fun MainWeatherCard(weather: co.weatherai.app.data.model.WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weather.current.temperature.toInt()}°",
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = getWMOString(weather.current.conditionCode),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SmallStatItem("Wind", "${weather.current.windSpeed}m/s", Icons.Default.Air)
                SmallStatItem("Timezone", weather.location.timezone, Icons.Default.Schedule)
            }
        }
    }
}

@Composable
fun SmallStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
    }
}

@Composable
fun HourlyCard(hour: co.weatherai.app.data.model.HourlyInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(hour.time.split("T").last().take(5), color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.WbSunny, // Simple placeholder
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("${hour.temperature.toInt()}°", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ForecastRow(day: co.weatherai.app.data.model.DailyInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(day.date, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(getWMOString(day.conditionCode), color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${day.tempMax.toInt()}°", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${day.tempMin.toInt()}°", color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun UsagePanel(state: WeatherUiState) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("API Usage", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        val usage = state.usageData
        if (usage != null) {
            val progress = usage.period.requestCount.toFloat() / usage.limits.requests.toFloat()
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (progress > 0.8f) Color.Red else Color(0xFF6366F1),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${usage.remaining.requests} left", color = Color.White, fontSize = 11.sp)
                Text("${usage.limits.requests} limit", color = Color(0xFF64748B), fontSize = 11.sp)
            }
        } else {
            // Placeholder/Loading state
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF334155),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(Modifier.height(8.dp))
            Text("Loading usage...", color = Color(0xFF64748B), fontSize = 11.sp)
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color.White, fontSize = 14.sp)
    }
}

fun getWMOString(code: String): String {
    return when (code) {
        "0" -> "Clear"
        "1", "2", "3" -> "Cloudy"
        "45", "48" -> "Fog"
        "51", "53", "55" -> "Drizzle"
        "61", "63", "65" -> "Rain"
        "71", "73", "75" -> "Snow"
        "95" -> "Storm"
        else -> "Cloudy"
    }
}
