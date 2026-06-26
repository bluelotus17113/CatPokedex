package com.catpokedex.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cats ORDER BY capturedAt DESC")
    fun getAllCats(): Flow<List<Cat>>

    @Query("SELECT * FROM cats WHERE id = :id")
    suspend fun getCatById(id: Int): Cat?

    @Query("SELECT COUNT(*) FROM cats")
    suspend fun getCatCount(): Int

    @Insert
    suspend fun insertCat(cat: Cat): Long

    @Delete
    suspend fun deleteCat(cat: Cat)
}
