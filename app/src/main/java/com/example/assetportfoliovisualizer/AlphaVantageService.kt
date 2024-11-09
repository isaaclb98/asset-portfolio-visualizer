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

