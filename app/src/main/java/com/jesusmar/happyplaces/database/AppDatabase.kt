package com.jesusmar.happyplaces.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jesusmar.happyplaces.models.HappyPlaceModel
import com.jesusmar.happyplaces.models.HappyPlaceModelDao

@Database(entities = [HappyPlaceModel::class], version = 1)
abstract class AppDatabase() : RoomDatabase() {
    abstract fun happyPlacesModelDao(): HappyPlaceModelDao
}