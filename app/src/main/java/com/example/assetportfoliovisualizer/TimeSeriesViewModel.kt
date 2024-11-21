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

    private val _performanceOverTimePeriod = MutableLiveData<Map<String, Double>>()
    val performanceOverTimePeriod: LiveData<Map<String, Double>> = _performanceOverTimePeriod

    fun fetchTimeSeriesForAssets(ownedAssets: List<OwnedAsset>) {
        viewModelScope.launch {
            val timeSeriesResults = mutableMapOf<String, Map<String, DailyData>>()
            val currentPriceResults = mutableMapOf<String, Double>()
            val totalValueResults = mutableMapOf<String, Double>()
            val netWorthOverTimePeriod = mutableMapOf(
                "1Y" to mutableListOf<Double>(),
                "3Y" to mutableListOf<Double>(),
                "5Y" to mutableListOf<Double>(),
                "10Y" to mutableListOf<Double>(),
            )
            val performanceResults = mutableMapOf<String, Double>()

            ownedAssets.forEach {
                try {
                    // HTTP response from API
                    val response = RetrofitInstance.api.getTimeSeriesDaily(symbol = it.symbol)

                    // Get the time series data and represent it as a Map<String, DailyData>
                    val timeSeries = response.timeSeriesDaily
                    timeSeriesResults[it.symbol] = timeSeries

                    // Get the newest entry from our time series data
                    val mostRecentEntry = timeSeries.entries.firstOrNull()
                    val mostRecentPrice = mostRecentEntry?.value?.close?.toDoubleOrNull() ?: throw Exception("mostRecentPrice was null.")
                    currentPriceResults[it.symbol] = mostRecentPrice

                    // Current worth of asset based on most recent price
                    val totalValue = it.quantity * mostRecentPrice
                    totalValueResults[it.symbol] = totalValue

                    // Bootleg code to calculate the price of the asset at approximately 1 year, etc. ago.
                    // We don't use actual dates unfortunately
                    // 1 year
                    val oneYearEntry = timeSeries.entries.elementAtOrNull(252)
                    val oneYearPrice = oneYearEntry?.value?.close?.toDoubleOrNull()

                    // 3 year
                    val threeYearEntry = timeSeries.entries.elementAtOrNull(756)
                    val threeYearPrice = threeYearEntry?.value?.close?.toDoubleOrNull()

                    // 5 years
                    val fiveYearEntry = timeSeries.entries.elementAtOrNull(1260)
                    val fiveYearPrice = fiveYearEntry?.value?.close?.toDoubleOrNull()

                    // 10 years
                    val tenYearEntry = timeSeries.entries.elementAtOrNull(2520)
                    val tenYearPrice = tenYearEntry?.value?.close?.toDoubleOrNull()

                    // Calculate the value of an asset at a given time and and it to the list
                    // ...otherwise add 0.0 to the list
                    if (oneYearPrice == null || oneYearPrice == 0.0) {
                        netWorthOverTimePeriod["1Y"]?.add(0.0)
                    } else {
                        netWorthOverTimePeriod["1Y"]?.add(oneYearPrice * it.quantity)
                    }

                    if (threeYearPrice == null || threeYearPrice == 0.0) {
                        netWorthOverTimePeriod["3Y"]?.add(0.0)
                    } else {
                        netWorthOverTimePeriod["3Y"]?.add(threeYearPrice * it.quantity)
                    }

                    if (fiveYearPrice == null || fiveYearPrice == 0.0) {
                        netWorthOverTimePeriod["5Y"]?.add(0.0)
                    } else {
                        netWorthOverTimePeriod["5Y"]?.add(fiveYearPrice * it.quantity)
                    }

                    if (tenYearPrice == null || tenYearPrice == 0.0) {
                        netWorthOverTimePeriod["10Y"]?.add(0.0)
                    } else {
                        netWorthOverTimePeriod["10Y"]?.add(tenYearPrice * it.quantity)
                    }

                } catch (e: Exception) {
                    Log.e("TimeSeriesViewModel", "Error fetching time series for ${it.symbol}", e)
                }
            }

            // Total net worth calculation
            var netWorthResult: Double = totalValueResults.values.sum()

            // Calculating the performance of the portfolio over different time periods. The performance is expressed as
            // a percentage. If any asset's value at a given time was 0.0--which means it wasn't able to be obtained (for
            // instance, if it does not have a 10 year history on the stock market)--then the performance value will be set
            // to 0.0 as well, which will be represented in the UI as N/A.
            performanceResults["1Y"] = if (netWorthOverTimePeriod["1Y"]?.contains(0.0) == true) {
                0.0
            } else {
                (netWorthResult / (netWorthOverTimePeriod["1Y"]?.sum() ?: 1.0) - 1.0) * 100
            }

            performanceResults["3Y"] = if (netWorthOverTimePeriod["3Y"]?.contains(0.0) == true) {
                0.0
            } else {
                (netWorthResult / (netWorthOverTimePeriod["3Y"]?.sum() ?: 1.0) - 1.0) * 100
            }

            performanceResults["5Y"] = if (netWorthOverTimePeriod["5Y"]?.contains(0.0) == true) {
                0.0
            } else {
                (netWorthResult / (netWorthOverTimePeriod["5Y"]?.sum() ?: 1.0) - 1.0) * 100
            }

            performanceResults["10Y"] = if (netWorthOverTimePeriod["10Y"]?.contains(0.0) == true) {
                0.0
            } else {
                (netWorthResult / (netWorthOverTimePeriod["10Y"]?.sum() ?: 1.0) - 1.0) * 100
            }

            // Post the intermediary values to the LiveData for use in the UI
            _timeSeriesData.postValue(timeSeriesResults)
            _currentPrices.postValue(currentPriceResults)
            _assetHoldingTotalValues.postValue(totalValueResults)
            _netWorth.postValue(netWorthResult)
            _performanceOverTimePeriod.postValue(performanceResults)
        }
    }
}