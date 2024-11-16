package com.example.assetportfoliovisualizer

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Json

interface AlphaVantageService {

    @GET("query")
    fun searchSymbols(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String = BuildConfig.ALPHA_VANTAGE_API_KEY
    ): Call<SymbolSearchResponse>

    @GET("query")
    suspend fun getTimeSeriesDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("outputsize") outputsize: String = "full",
        @Query("apikey") apiKey: String = BuildConfig.ALPHA_VANTAGE_API_KEY
    ): TimeSeriesDailyResponse
}


data class SymbolSearchResponse(
    @Json(name = "bestMatches") val bestMatches: List<BestMatch>?
)

data class BestMatch(
    @Json(name = "1. symbol") val symbol: String?,
    @Json(name = "2. name") val name: String?,
    @Json(name = "3. type") val type: String?,
    @Json(name = "4. region") val region: String?,
    @Json(name = "5. marketOpen") val marketOpen: String?,
    @Json(name = "6. marketClose") val marketClose: String?,
    @Json(name = "7. timezone") val timezone: String?,
    @Json(name = "8. currency") val currency: String?,
    @Json(name = "9. matchScore") val matchScore: String?
)

data class TimeSeriesDailyResponse(
    @Json(name = "Meta Data") val metaData: MetaData,
    // We are mapping date -> daily data
    @Json(name = "Time Series (Daily)") val timeSeriesDaily: Map<String, DailyData>
)

data class MetaData(
    @Json(name = "1. Information") val information: String?,
    @Json(name = "2. Symbol") val symbol: String?,
    @Json(name = "3. Last Refreshed") val lastRefreshed: String?,
    @Json(name = "4. Output Size") val outputSize: String?,
    @Json(name = "5. Time Zone:") val timeZone: String?
)

data class DailyData(
    @Json(name = "1. open") val open: String,
    @Json(name = "2. high") val high: String,
    @Json(name = "3. low") val low: String,
    @Json(name = "4. close") val close: String,
    @Json(name = "5. volume") val volume: String
)