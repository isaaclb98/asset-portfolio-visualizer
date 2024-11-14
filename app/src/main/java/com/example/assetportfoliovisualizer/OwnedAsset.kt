package com.example.assetportfoliovisualizer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OwnedAsset(
    @PrimaryKey val symbol: String,
    val name: String,
    val type: String,
    val region: String
)
