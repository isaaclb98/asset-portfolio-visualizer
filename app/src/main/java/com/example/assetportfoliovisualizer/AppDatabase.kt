package com.example.assetportfoliovisualizer

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.assetportfoliovisualizer.OwnedAsset
import com.example.assetportfoliovisualizer.OwnedAssetDao

@Database(entities = [OwnedAsset::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ownedAssetDao(): OwnedAssetDao

}
