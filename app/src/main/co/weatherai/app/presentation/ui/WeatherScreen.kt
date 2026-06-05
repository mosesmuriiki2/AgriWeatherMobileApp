package co.weatherai.app.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.weatherai.app.presentation.WeatherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationTransition = rememberInfiniteTransition(label = "SyncSpinner")
    val rotation by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "syncRotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AgriWeather-AI Console", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
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
            if (state.isLoading && state.weatherData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF6366F1))
            }

            state.error?.let { err ->
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.WifiOff, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(err, color = Color.White, fontSize = 14.sp)
                }
            }

            state.weatherData?.let { weather ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // DB Cache Advisory banner
                    if (state.isFromCache) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x26F59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Cached", tint = Color(0xFFF59E0B))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = state.alertWarning ?: "Loaded Offline room Cache",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Main Temperature Radial Arc Widget
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(190.dp)) {
                                Canvas(modifier = Modifier.size(170.dp)) {
                                    drawArc(
                                        color = Color(0x1AFFFFFF),
                                        startAngle = -220f,
                                        sweepAngle = 260f,
                                        useCenter = false,
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        brush = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))),
                                        startAngle = -220f,
                                        sweepAngle = (260f * (weather.current.temperature.coerceIn(0.0, 45.0) / 45.0)).toFloat(),
                                        useCenter = false,
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${weather.current.temperature}°C", 
                                        fontSize = 42.sp, 
                                        fontWeight = FontWeight.Black, 
                                        color = Color.White
                                    )
                                    Text(
                                        text = "WMO: ${weather.current.conditionCode}", 
                                        fontSize = 13.sp, 
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "${weather.location.timezone}", 
                                        fontSize = 11.sp, 
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    // Secondary stats grid
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsCard(
                                label = "Wind Angle",
                                value = "${weather.current.wind_speed} m/s",
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Country Code",
                                value = weather.location.country,
                                color = Color(0xFF34D399),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // High-test Dynamic AI summary block
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Gemini Live Agronomy Advisor", color = Color(0xFF818CF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.aiAgronomyAdvice, 
                                    color = Color(0xFFE2E8F0), 
                                    fontSize = 12.sp, 
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    item {
                        Text("7-Day Forecast Matrix", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Dynamically map forecast list
                    items(weather.daily) { dayRecord ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(dayRecord.date, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Precipitation: ${dayRecord.precipitationProbability}%", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Row {
                                    Text("${dayRecord.tempMax}°", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${dayRecord.tempMin}°", color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color(0xFF64748B), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
                Spacer(modifier = Modifier.width(6.dp))
                Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
