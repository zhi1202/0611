package com.example.ui

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.WeatherAndTideViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherAndTideScreen(
    viewModel: WeatherAndTideViewModel,
    modifier: Modifier = Modifier
) {
    // 獲取狀態流程 (State Flow)
    val weatherState by viewModel.weatherState.collectAsState()
    val tideState by viewModel.tideState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    val locationLabel by viewModel.currentLocationLabel.collectAsState()

    // 定位權限狀態管理 (使用 Accompanist Permissions)
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 當權限授予完成時，自動觸發重整資料
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.refreshData()
        }
    }

    // 漸層海藍色主題背景
    val oceanBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F2027), // 深海幽藍
            Color(0xFF203A43), // 碧綠海藍
            Color(0xFF2C5364)  // 湛藍沙灘
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "海洋氣象與潮汐預報",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            if (locationPermissionsState.allPermissionsGranted) {
                                viewModel.refreshData()
                            } else {
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "同步重新整理",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重整", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F2027),
                    titleContentColor = Color.White
                ),
                modifier = Modifier.shadow(8.dp)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(oceanBackgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. 定位狀態與權限提示區
                LocationStatusCard(
                    locationLabel = locationLabel,
                    isPermissionsGranted = locationPermissionsState.allPermissionsGranted,
                    onRequestPermissions = {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                )

                // 2. 顯示沙盒模式提示（API 金鑰尚未配置時展示）
                AnimatedVisibility(
                    visible = isDemoMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SandboxIndicatorCard()
                }

                // 3. 錯誤訊息提示區
                errorMessage?.let { msg ->
                    ErrorBanner(message = msg)
                }

                if (isLoading) {
                    // 載入中動畫骨架
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1de9b6))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("正在透過 GPS 定位並串接海象數據...", color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                } else {
                    // 4. 天氣資訊區塊
                    WeatherCardBlock(weatherState = weatherState)

                    // 5. 潮汐資訊與自訂 Canvas 潮位波形曲線圖
                    TideCardBlock(tideState = tideState)
                }
            }
        }
    }
}

/**
 * 頂部定位與授權狀態卡片
 */
@Composable
fun LocationStatusCard(
    locationLabel: String,
    isPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "定位標記",
                    tint = Color(0xFF00B0FF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "當前觀測位置",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = locationLabel,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            // 若權限尚未取得，輔助引導使用者點擊授權
            if (!isPermissionsGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0F2027)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "開啟 GPS 定位"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("授權 GPS 精確定位，獲取鄰近海域即時數據", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 沙盒展示警告提示（友好提醒如何更換 API Keys）
 */
@Composable
fun SandboxIndicatorCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9100).copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "沙盒提醒",
                tint = Color(0xFFFFAB40),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "體驗沙盒模擬模式中",
                    color = Color(0xFFFFB74D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "目前偵測為預設金鑰。系統已啟用高度仿真的海洋潮位運算與局部氣象數據模擬。若要展現真正實時數據，請在代碼 ViewModel 中貼上您的 OpenWeatherMap 與 StormGlass API 密鑰。",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * 異常/錯誤橫幅
 */
@Composable
fun ErrorBanner(message: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD50000).copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = Color(0xFFFF8A80),
            fontSize = 12.sp,
            modifier = Modifier.padding(12.dp),
            lineHeight = 18.sp
        )
    }
}

/**
 * 天氣預報顯示區塊物件 (含當前 + 未來五日)
 */
@Composable
fun WeatherCardBlock(weatherState: WeatherUIState) {
    Column {
        Text(
            text = "◆ 當地港灣氣象",
            color = Color(0xFFE0F7FA),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 當前天氣大數值
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weatherState.cityName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weatherState.description,
                            color = Color(0xFF00E5FF),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 氣溫展示區
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", weatherState.temp),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "°C",
                            color = Color(0xFF00E5FF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 風速、濕度、極大極小氣溫
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherDetailItem(
                        icon = Icons.Default.Thermostat,
                        label = "氣溫區間",
                        value = "${weatherState.tempMin.toInt()}° ~ ${weatherState.tempMax.toInt()}°C",
                        tint = Color(0xFFFFB74D)
                    )
                    WeatherDetailItem(
                        icon = Icons.Default.Waves,
                        label = "相對濕度",
                        value = "${weatherState.humidity}%",
                        tint = Color(0xFF4FC3F7)
                    )
                    WeatherDetailItem(
                        icon = Icons.Default.Air,
                        label = "近海風速",
                        value = String.format(Locale.getDefault(), "%.1f m/s", weatherState.windSpeed),
                        tint = Color(0xFFB2DFDB)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 橫向未來五日預報
                Text(
                    text = "未來 5 日天氣趨勢",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weatherState.forecast) { forecast ->
                        DailyForecastItem(forecast)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastItem(forecast: com.example.model.DailyForecast) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = forecast.dayOfWeek.split(" ").firstOrNull() ?: "",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = forecast.dayOfWeek.split(" ").getOrNull(1) ?: "",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = when {
                    forecast.description.contains("雨") -> Icons.Default.Water
                    forecast.description.contains("雲") -> Icons.Default.Cloud
                    else -> Icons.Default.WbSunny
                },
                contentDescription = forecast.description,
                tint = when {
                    forecast.description.contains("雨") -> Color(0xFF4FC3F7)
                    forecast.description.contains("雲") -> Color(0xFFB0BEC5)
                    else -> Color(0xFFFFD54F)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.1f°C", forecast.temp),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = forecast.description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 客製化天氣單項指標元件
 */
@Composable
fun WeatherDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * 潮汐資訊區塊物件 (包含滿潮/乾潮以及自訂繪製曲線)
 */
@Composable
fun TideCardBlock(tideState: TideUIState) {
    Column {
        Text(
            text = "◆ 附近海域潮汐狀況",
            color = Color(0xFFE0F7FA),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tideState.stationName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF00E5FF), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "潮位實時感測",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // 當前潮位數值
                    Column(horizontalAlignment = Alignment.End) {
                        Text("當前潮水高度", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            val sign = if (tideState.currentHeight >= 0) "+" else ""
                            Text(
                                text = String.format(Locale.getDefault(), "%s%.2f", sign, tideState.currentHeight),
                                color = Color(0xFF00E5FF),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " 米(m)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 乾潮與滿潮卡片並列
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 滿潮顯示
                    Box(modifier = Modifier.weight(1f)) {
                        TideExtremeCard(
                            isHigh = true,
                            time = tideState.highTideTime,
                            height = tideState.highTideHeight,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    // 乾潮顯示
                    Box(modifier = Modifier.weight(1f)) {
                        TideExtremeCard(
                            isHigh = false,
                            time = tideState.lowTideTime,
                            height = tideState.lowTideHeight,
                            color = Color(0xFFFF8A65)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 繪製美觀潮汐正弦形狀波動曲線圖
                Text(
                    text = "未來 24 小時潮汐高度連續波動趨勢",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (tideState.hourlyHeights.isNotEmpty()) {
                    TideCurveChart(
                        hourlyTides = tideState.hourlyHeights,
                        currentHeight = tideState.currentHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .padding(top = 16.dp, bottom = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 極值列表提示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "提示",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "潮位預測受月球與地球潮汐力影響，在沿海港口航行或釣魚務必隨時關注高低潮時間。",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 9.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 滿潮/乾潮極值展示卡片
 */
@Composable
fun TideExtremeCard(
    isHigh: Boolean,
    time: String,
    height: Double,
    color: Color
) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Water,
                        contentDescription = if (isHigh) "滿潮" else "乾潮",
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHigh) "滿潮高潮" else "乾潮低潮",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "預測時間: $time",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = String.format(Locale.getDefault(), "潮位高度: %.2f m", height),
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 自繪 Canvas 頂級潮位波形曲線圖元件
 */
@Composable
fun TideCurveChart(
    hourlyTides: List<HourlyTide>,
    currentHeight: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (hourlyTides.isEmpty()) return@Canvas

        // 1. 取得潮高極值，以動態縮放曲線比例
        val minHeight = hourlyTides.minOf { it.height }
        val maxHeight = hourlyTides.maxOf { it.height }
        val heightRange = (maxHeight - minHeight).coerceAtLeast(0.5)

        val paddingOffset = 15f // 畫布上下邊距
        val usableHeight = canvasHeight - (paddingOffset * 2f)

        // 高度轉換：將潮位高度映射到 Canvas 坐標 Y
        fun mapHeightToY(value: Double): Float {
            val ratio = (value - minHeight) / heightRange
            // Y軸是由上往下(0為頂)，所以要反過來
            return (paddingOffset + usableHeight - (ratio * usableHeight)).toFloat()
        }

        val stepX = canvasWidth / (hourlyTides.size - 1).toFloat()

        // 2. 繪製基底水平參考線 (0.0 米)
        val zeroY = mapHeightToY(0.0)
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, zeroY),
            end = Offset(canvasWidth, zeroY),
            strokeWidth = 2f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // 3. 建立並點綴潮汐波浪曲線
        val wavePath = Path()
        for (i in hourlyTides.indices) {
            val x = i * stepX
            val y = mapHeightToY(hourlyTides[i].height)
            if (i == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        // 繪製半透明海潮漸層填滿區
        val fillPath = Path().apply {
            addPath(wavePath)
            lineTo(canvasWidth, canvasHeight)
            lineTo(0f, canvasHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.25f),
                    Color(0xFF0F2027).copy(alpha = 0.0f)
                )
            )
        )

        // 繪製高擬真潮汐曲線路徑波峰
        drawPath(
            path = wavePath,
            color = Color(0xFF00E5FF),
            style = Stroke(
                width = 4f,
                cap = StrokeCap.Round
            )
        )

        // 4. 繪製精簡的時間橫軸標記
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = (255 * 0.4).toInt() // 40% 不透明度
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        // 我們每過 4 個小時畫一個時間標記，使圖表簡潔
        for (i in hourlyTides.indices step 4) {
            val x = i * stepX
            val timeText = hourlyTides[i].timeLabel
            drawContext.canvas.nativeCanvas.drawText(
                timeText,
                x,
                canvasHeight - 4f,
                textPaint
            )
        }

        // 5. 繪製當前時間的波形跳動標記點（第一個 hours 點代表 '現在'）
        val nowX = 0f
        val nowY = mapHeightToY(currentHeight)

        // 波動外環
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.35f),
            radius = 16f,
            center = Offset(nowX, nowY)
        )
        // 實心內點
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(nowX, nowY)
        )
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = 4f,
            center = Offset(nowX, nowY)
        )
    }
}
