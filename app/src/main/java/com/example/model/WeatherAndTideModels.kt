package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// 1. OpenWeatherMap API 資料模型
// ==========================================

@JsonClass(generateAdapter = true)
data class OpenWeatherResponse(
    @Json(name = "coord") val coord: Coord?,
    @Json(name = "weather") val weather: List<WeatherDescription>?,
    @Json(name = "main") val main: MainTemp?,
    @Json(name = "wind") val wind: Wind?,
    @Json(name = "name") val cityName: String?
)

@JsonClass(generateAdapter = true)
data class Coord(
    @Json(name = "lon") val lon: Double,
    @Json(name = "lat") val lat: Double
)

@JsonClass(generateAdapter = true)
data class WeatherDescription(
    @Json(name = "id") val id: Int,
    @Json(name = "main") val main: String,
    @Json(name = "description") val description: String,
    @Json(name = "icon") val icon: String
)

@JsonClass(generateAdapter = true)
data class MainTemp(
    @Json(name = "temp") val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    @Json(name = "pressure") val pressure: Int,
    @Json(name = "humidity") val humidity: Int
)

@JsonClass(generateAdapter = true)
data class Wind(
    @Json(name = "speed") val speed: Double,
    @Json(name = "deg") val deg: Int
)

@JsonClass(generateAdapter = true)
data class OpenWeatherForecastResponse(
    @Json(name = "list") val list: List<ForecastItem>?
)

@JsonClass(generateAdapter = true)
data class ForecastItem(
    @Json(name = "dt") val dt: Long,
    @Json(name = "main") val main: MainTemp?,
    @Json(name = "weather") val weather: List<WeatherDescription>?,
    @Json(name = "wind") val wind: Wind?,
    @Json(name = "dt_txt") val dtTxt: String?
)

// ==========================================
// 2. StormGlass Tide (潮汐) API 資料模型
// ==========================================

@JsonClass(generateAdapter = true)
data class StormGlassTideResponse(
    @Json(name = "data") val data: List<TideExtremeItem>?
)

@JsonClass(generateAdapter = true)
data class TideExtremeItem(
    @Json(name = "height") val height: Double,
    @Json(name = "time") val time: String,
    @Json(name = "type") val type: String // "high" 或 "low"
)

// ==========================================
// 3. UI 呈現專用的統一資料模型 (Clean MVVM Mode)
// ==========================================

data class WeatherUIState(
    val cityName: String = "獲取定位中...",
    val temp: Double = 25.5,
    val description: String = "- -",
    val tempMin: Double = 22.0,
    val tempMax: Double = 29.0,
    val humidity: Int = 75,
    val windSpeed: Double = 3.2,
    val forecast: List<DailyForecast> = emptyList()
)

data class DailyForecast(
    val dayOfWeek: String,
    val temp: Double,
    val icon: String,
    val description: String
)

data class TideUIState(
    val currentHeight: Double = 0.0,
    val highTideTime: String = "--:--",
    val highTideHeight: Double = 0.0,
    val lowTideTime: String = "--:--",
    val lowTideHeight: Double = 0.0,
    val stationName: String = "附近觀測站",
    val hourlyHeights: List<HourlyTide> = emptyList(), // 繪製美觀潮汐波形曲線時使用
    val extremesList: List<TideExtreme> = emptyList()
)

data class HourlyTide(
    val timeLabel: String,
    val height: Double
)

data class TideExtreme(
    val timeLabel: String,
    val height: Double,
    val isHigh: Boolean
)
