package com.example.assetportfoliovisualizer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TickerSearchViewModel : ViewModel() {
    private val _searchResults = MutableLiveData<List<BestMatch>>()
    val searchResults: LiveData<List<BestMatch>> = _searchResults

    // Call this method whenever the user types a new keyword
    fun searchForSymbols(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (keyword.isBlank()) {
                _searchResults.postValue(emptyList())
                return@launch
            }

            try {
                // Use our searchSymbols service... we are sending a GET request with the keyword
                val response = RetrofitInstance.api.searchSymbols(keywords = keyword).execute()

                if (response.isSuccessful) {
                    val data = response.body()
                    // Limit results to the first 5 items
                    val limitedResults = data?.bestMatches?.take(5) ?: emptyList()

                    limitedResults.forEach {
                        Log.d("SearchResult", "${it.symbol} - ${it.name}")
                    }

                    // Update LiveData
                    _searchResults.postValue(limitedResults)
                } else {
                    _searchResults.postValue(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.postValue(emptyList())
            }

        }
    }
}