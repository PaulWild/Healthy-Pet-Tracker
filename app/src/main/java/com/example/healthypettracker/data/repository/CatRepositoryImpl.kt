package com.example.healthypettracker.data.repository

import com.example.healthypettracker.data.local.dao.CatDao
import com.example.healthypettracker.data.local.entity.Cat
import com.example.healthypettracker.domain.repository.CatRepository
import kotlinx.coroutines.flow.Flow

class CatRepositoryImpl(private val catDao: CatDao) : CatRepository {
    override fun getAllCats(): Flow<List<Cat>> = catDao.getAllCats()

    override suspend fun getCatById(catId: Long): Cat? = catDao.getCatById(catId)

    override fun getCatByIdFlow(catId: Long): Flow<Cat?> = catDao.getCatByIdFlow(catId)

    override suspend fun insertCat(cat: Cat): Long = catDao.insert(cat)

    override suspend fun updateCat(cat: Cat) = catDao.update(cat)

    override suspend fun deleteCat(cat: Cat) = catDao.delete(cat)

    override suspend fun deleteCatById(catId: Long) = catDao.deleteById(catId)
}
