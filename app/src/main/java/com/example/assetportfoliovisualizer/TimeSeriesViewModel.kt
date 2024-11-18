package com.example.assetportfoliovisualizer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TimeSeriesViewModel : ViewModel() {
    private val _timeSeriesData = MutableLiveData<Map<String, Map<String, DailyData>>>()
    val timeSeriesData: LiveData<Map<String, Map<String, DailyData>>> = _timeSeriesData

    private val _currentPrices = MutableLiveData<Map<String, Double>>()
    val currentPrices: LiveData<Map<String, Double>> = _currentPrices

    private val _assetHoldingTotalValues = MutableLiveData<Map<String, Double>>()
    val assetHoldingTotalValues: LiveData<Map<String, Double>> = _assetHoldingTotalValues

    private val _netWorth = MutableLiveData<Double>()
    val netWorth: LiveData<Double> = _netWorth

    fun fetchTimeSeriesForAssets(ownedAssets: List<OwnedAsset>) {
        viewModelScope.launch {
            val timeSeriesResults = mutableMapOf<String, Map<String, DailyData>>()
            val currentPriceResults = mutableMapOf<String, Double>()
            val totalValueResults = mutableMapOf<String, Double>()

            ownedAssets.forEach {
                try {
                    val response = RetrofitInstance.api.getTimeSeriesDaily(symbol = it.symbol)
                    val timeSeries = response.timeSeriesDaily
                    timeSeriesResults[it.symbol] = timeSeries

                    val mostRecentEntry = timeSeries.entries.firstOrNull()
                    val mostRecentPrice = mostRecentEntry?.value?.close
                    val mostRecentPriceDouble = mostRecentPrice?.toDoubleOrNull() ?: throw Exception("mostRecentPrice was null.")

                    currentPriceResults[it.symbol] = mostRecentPriceDouble

                    Log.d(
                        "TimeSeriesViewModel",
                        "Most recent price for ${it.symbol}: $mostRecentPriceDouble"
                    )

                    val totalValue = it.quantity * mostRecentPriceDouble
                    totalValueResults[it.symbol] = totalValue

                } catch (e: Exception) {
                    Log.e("TimeSeriesViewModel", "Error fetching time series for ${it.symbol}", e)
                }
            }

            var netWorthResult: Double = totalValueResults.values.sum()

            _timeSeriesData.postValue(timeSeriesResults)
            _currentPrices.postValue(currentPriceResults)
            _assetHoldingTotalValues.postValue(totalValueResults)
            _netWorth.postValue(netWorthResult)
        }
    }
}