package com.example.assetportfoliovisualizer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OwnedAssetsViewModel(private val db: AppDatabase) : ViewModel() {
    private val dao = db.ownedAssetDao()

    private val _ownedAssets = MutableLiveData<List<OwnedAsset>>()
    val ownedAssets: LiveData<List<OwnedAsset>> = _ownedAssets

    // Load data when ViewModel is created
    init {
        fetchOwnedAssets()
    }

    // Fetch owned assets from the database
    fun fetchOwnedAssets() {
        viewModelScope.launch {
            val assets = dao.getAll()
            _ownedAssets.postValue(assets)
        }
    }

    fun addOwnedAsset(asset: OwnedAsset) {
        viewModelScope.launch {
            dao.insert(asset)
            fetchOwnedAssets()
        }
    }

    fun deleteOwnedAsset(asset: OwnedAsset) {
        viewModelScope.launch {
            dao.delete(asset)
            fetchOwnedAssets()
        }
    }
}
