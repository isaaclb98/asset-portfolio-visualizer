package com.example.assetportfoliovisualizer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.model.Point
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
                "1Y" to 0.0,
                "3Y" to 0.0,
                "5Y" to 0.0,
                "10Y" to 0.0,
            )
            val performanceResults = mutableMapOf<String, Double>()

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

                    netWorthOverTimePeriod["1Y"] = (netWorthOverTimePeriod["1Y"] ?: 0.0) + ((oneYearPrice ?: 0.0) * it.quantity)
                    netWorthOverTimePeriod["3Y"] = (netWorthOverTimePeriod["3Y"] ?: 0.0) + ((threeYearPrice ?: 0.0) * it.quantity)
                    netWorthOverTimePeriod["5Y"] = (netWorthOverTimePeriod["5Y"] ?: 0.0) + ((fiveYearPrice ?: 0.0) * it.quantity)
                    netWorthOverTimePeriod["10Y"] = (netWorthOverTimePeriod["10Y"] ?: 0.0) + ((tenYearPrice ?: 0.0) * it.quantity)


                } catch (e: Exception) {
                    Log.e("TimeSeriesViewModel", "Error fetching time series for ${it.symbol}", e)
                }
            }

            Log.d("TimeSeriesViewModel", "Net worth over time periods: $netWorthOverTimePeriod")

            var netWorthResult: Double = totalValueResults.values.sum()

            performanceResults["1Y"] = if ((netWorthOverTimePeriod["1Y"] ?: 0.0) > 0) {
                val percentage = ((netWorthResult / (netWorthOverTimePeriod["1Y"] ?: 1.0)) - 1.0) * 100
                Log.d("TimeSeriesViewModel", "1Y: NetWorth=${netWorthResult}, Previous=${netWorthOverTimePeriod["1Y"]}, Percentage=$percentage")
                percentage
            } else {
                Log.d("TimeSeriesViewModel", "1Y data missing or zero")
                0.0
            }

            performanceResults["3Y"] = if ((netWorthOverTimePeriod["3Y"] ?: 0.0) > 0) {
                val percentage = ((netWorthResult / (netWorthOverTimePeriod["3Y"] ?: 1.0)) - 1.0) * 100
                Log.d("TimeSeriesViewModel", "3Y: NetWorth=${netWorthResult}, Previous=${netWorthOverTimePeriod["3Y"]}, Percentage=$percentage")
                percentage
            } else {
                Log.d("TimeSeriesViewModel", "3Y data missing or zero")
                0.0
            }

            performanceResults["5Y"] = if ((netWorthOverTimePeriod["5Y"] ?: 0.0) > 0) {
                val percentage = ((netWorthResult / (netWorthOverTimePeriod["5Y"] ?: 1.0)) - 1.0) * 100
                Log.d("TimeSeriesViewModel", "5Y: NetWorth=${netWorthResult}, Previous=${netWorthOverTimePeriod["5Y"]}, Percentage=$percentage")
                percentage
            } else {
                Log.d("TimeSeriesViewModel", "5Y data missing or zero")
                0.0
            }

            performanceResults["10Y"] = if ((netWorthOverTimePeriod["10Y"] ?: 0.0) > 0) {
                val percentage = ((netWorthResult / (netWorthOverTimePeriod["10Y"] ?: 1.0)) - 1.0) * 100
                Log.d("TimeSeriesViewModel", "10Y: NetWorth=${netWorthResult}, Previous=${netWorthOverTimePeriod["10Y"]}, Percentage=$percentage")
                percentage
            } else {
                Log.d("TimeSeriesViewModel", "10Y data missing or zero")
                0.0
            }

            _timeSeriesData.postValue(timeSeriesResults)
            _currentPrices.postValue(currentPriceResults)
            _assetHoldingTotalValues.postValue(totalValueResults)
            _netWorth.postValue(netWorthResult)
            _performanceOverTimePeriod.postValue(performanceResults)
        }
    }
}