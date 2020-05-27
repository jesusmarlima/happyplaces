package com.jesusmar.happyplaces.models

import androidx.room.*


@Dao
interface HappyPlaceModelDao {
    @Query("SELECT * FROM HappyPlacesTable")
    fun getAll(): List<HappyPlaceModel>

    @Query("SELECT * FROM HappyPlacesTable WHERE _id IN (:happyPlacesIds)")
    fun loadAllByIds(happyPlacesIds: IntArray): List<HappyPlaceModel>

    @Query("SELECT * FROM HappyPlacesTable WHERE title LIKE :title AND description LIKE :description LIMIT 1")
    fun findByName(title: String, description: String): HappyPlaceModel

    @Insert(entity = HappyPlaceModel::class)
    fun insertAll(vararg happyPlace: HappyPlaceModel?): List<Long>

    @Update(entity = HappyPlaceModel::class)
    fun updateHappyPlace(vararg happyPlace: HappyPlaceModel?): Int

    @Delete(entity = HappyPlaceModel::class)
    fun delete(vararg happyPlace: HappyPlaceModel?): Int
}