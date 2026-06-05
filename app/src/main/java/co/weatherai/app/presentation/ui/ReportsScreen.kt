package co.weatherai.app.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.weatherai.app.data.model.WeatherResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    weatherData: WeatherResponse?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Trends", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        if (weatherData == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No data available for trends", color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        "7-Day Temperature Trend",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    TemperatureChart(weatherData.daily.map { it.tempMax.toFloat() })
                }

                item {
                    Text(
                        "Precipitation Probability",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    PrecipitationChart(weatherData.daily.map { it.precipitationProbability.toFloat() })
                }

                item {
                    SummaryCard(weatherData)
                }
            }
        }
    }
}

@Composable
fun TemperatureChart(data: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (data.isEmpty()) return@Card
        val maxTemp = data.maxOrNull() ?: 1f
        val minTemp = data.minOrNull() ?: 0f
        val range = (maxTemp - minTemp).coerceAtLeast(1f)

        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val width = size.width
            val height = size.height
            val spacing = width / (data.size - 1)

            val path = Path()
            data.forEachIndexed { index, temp ->
                val x = index * spacing
                val y = height - ((temp - minTemp) / range) * height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                
                drawCircle(
                    color = Color(0xFF6366F1),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = Color(0xFF6366F1),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun PrecipitationChart(data: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { prob ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height((prob * 1.5).dp)
                            .background(Color(0xFF3B82F6), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${prob.toInt()}%", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(weather: WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                val avgMax = weather.daily.map { it.tempMax }.average()
                Text("Insights", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text(
                    "Average high for the week will be ${String.format(java.util.Locale.US, "%.1f", avgMax)}°C",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
