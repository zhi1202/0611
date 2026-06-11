package com.example.api

import com.example.model.OpenWeatherForecastResponse
import com.example.model.OpenWeatherResponse
import com.example.model.StormGlassTideResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ==========================================
// 1. OpenWeatherMap Retrofit API 介面
// ==========================================
interface OpenWeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "zh_tw"
    ): OpenWeatherResponse

    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "zh_tw"
    ): OpenWeatherForecastResponse
}

// ==========================================
// 2. StormGlass Tide (潮汐) Retrofit API 介面
// ==========================================
interface StormGlassApi {
    @GET("tide/extremes/point")
    suspend fun getTideExtremes(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("start") startEpochSeconds: Long? = null,
        @Query("end") endEpochSeconds: Long? = null,
        @Header("Authorization") apiKey: String
    ): StormGlassTideResponse
}

// ==========================================
// 3. Retrofit 網路服務客戶端
// ==========================================
object RetrofitClient {
    private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val STORM_GLASS_BASE_URL = "https://api.stormglass.io/v2/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val openWeatherService: OpenWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenWeatherApi::class.java)
    }

    val stormGlassService: StormGlassApi by lazy {
        Retrofit.Builder()
            .baseUrl(STORM_GLASS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(StormGlassApi::class.java)
    }
}
