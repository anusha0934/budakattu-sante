package com.mindmatrix.budakattusante.data.repository

import com.mindmatrix.budakattusante.data.local.dao.MspDao
import com.mindmatrix.budakattusante.data.local.entity.MspEntity
import kotlinx.coroutines.flow.Flow

class MspRepository(private val mspDao: MspDao) {
    val mspData: Flow<List<MspEntity>> = mspDao.getAllMspData()

    suspend fun getMspForCategory(category: String): MspEntity? {
        return mspDao.getMspForCategory(category)
    }

    suspend fun updateMspData(data: List<MspEntity>) {
        mspDao.insertMspData(data)
    }

    // Dummy data for initialization if needed
    suspend fun seedMspData() {
        val dummyData = listOf(
            MspEntity("Wild Honey", 320.0, 450.0, "2024-05-01"),
            MspEntity("Bamboo Crafts", 150.0, 250.0, "2024-05-01"),
            MspEntity("Herbal Oils", 500.0, 750.0, "2024-05-01"),
            MspEntity("Forest Spices", 200.0, 350.0, "2024-05-01")
        )
        mspDao.insertMspData(dummyData)
    }
}
