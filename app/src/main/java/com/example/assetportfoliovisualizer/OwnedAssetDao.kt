package com.example.assetportfoliovisualizer

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OwnedAssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: OwnedAsset)

    @Query("SELECT * FROM OwnedAsset")
    suspend fun getAll(): List<OwnedAsset>

    @Delete
    suspend fun delete(asset: OwnedAsset)
}