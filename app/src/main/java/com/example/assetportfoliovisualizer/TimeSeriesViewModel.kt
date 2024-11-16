package com.example.assetportfoliovisualizer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeSeriesViewModel : ViewModel() {
    private val _timeSeriesData = MutableLiveData<Map<String, Map<String, DailyData>>>()
    val timeSeriesData: LiveData<Map<String, Map<String, DailyData>>> = _timeSeriesData

    fun fetchTimeSeriesForAssets(ownedAssets: List<OwnedAsset>) {
        viewModelScope.launch {
            val timeSeriesResults = mutableMapOf<String, Map<String, DailyData>>()

            ownedAssets.forEach {
                try {
                    val response = RetrofitInstance.api.getTimeSeriesDaily(symbol = it.symbol)
                    val timeSeries = response.timeSeriesDaily
                    timeSeriesResults[it.symbol] = timeSeries
                } catch (e: Exception) {

                }

                _timeSeriesData.postValue(timeSeriesResults)
            }
        }
    }
}