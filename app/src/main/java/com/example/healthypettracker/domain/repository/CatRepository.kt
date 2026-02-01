package com.example.healthypettracker.domain.repository

import com.example.healthypettracker.data.local.entity.Cat
import kotlinx.coroutines.flow.Flow

interface CatRepository {
    fun getAllCats(): Flow<List<Cat>>
    suspend fun getCatById(catId: Long): Cat?
    fun getCatByIdFlow(catId: Long): Flow<Cat?>
    suspend fun insertCat(cat: Cat): Long
    suspend fun updateCat(cat: Cat)
    suspend fun deleteCat(cat: Cat)
    suspend fun deleteCatById(catId: Long)
}
