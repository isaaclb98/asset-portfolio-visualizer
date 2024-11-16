package com.example.assetportfoliovisualizer

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// Retrofit allows us to send HTTP requests easily from our Android app...
// Moshi is a serializer for Android development.
// This singleton class allows us to interface with the AlphaVantage api.
object RetrofitInstance {
    private const val BASE_URL = "https://www.alphavantage.co/"

    // Enables Moshi to work with Kotlin's nullable types and default values
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Add http client with a logging interceptor for use in debugging
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: AlphaVantageService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // use moshi for deserializing JSON
            .build()
            .create(AlphaVantageService::class.java)
    }
}
